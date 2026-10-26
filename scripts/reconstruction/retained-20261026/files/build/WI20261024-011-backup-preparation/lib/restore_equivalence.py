"""Bounded, byte-preserving proof for the observed MySQL 8.0.46 column DDL delta."""

from __future__ import annotations

from dataclasses import dataclass
import re

import backup_checks as checks


MAX_SQL = 2 * 1024**2
INSERTION = b' CHARACTER SET utf8mb4'
COLLATION = b' COLLATE utf8mb4_unicode_ci'
STRING_TYPES = {b'char', b'varchar', b'tinytext', b'text', b'mediumtext', b'longtext', b'enum', b'set'}


def need(value: bool, code: str = 'DDL_LEXICAL_PROOF') -> None:
    checks.require(value, code)


def column_query(schema: str) -> bytes:
    """Hex value tags distinguish NULL from empty values without delimiter ambiguity."""
    name = checks.identifier(schema)
    fields = ('TABLE_NAME', 'COLUMN_NAME', 'ORDINAL_POSITION', 'COLUMN_TYPE',
              'CHARACTER_SET_NAME', 'COLLATION_NAME')
    encoded = ','.join("IF(" + field + " IS NULL,'N',CONCAT('V',HEX(CAST(" + field
                       + ' AS CHAR CHARACTER SET utf8mb4))))' for field in fields)
    return ("SELECT " + encoded + " FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='"
            + name + "' ORDER BY BINARY TABLE_NAME,ORDINAL_POSITION;\n").encode('ascii')


def parse_columns(raw: bytes, tables: set[str]) -> dict:
    """Require complete ordered unique identities and bounded, typed column metadata."""
    need(0 < len(raw) <= MAX_SQL, 'COLUMN_METADATA')
    result = {}
    previous = None
    ordinals = {}
    names = set()
    for line in raw.splitlines():
        parts = line.split(b'\t')
        need(len(parts) == 6 and len(result) < 5000, 'COLUMN_METADATA')
        values = []
        for part in parts:
            need(part == b'N' or re.fullmatch(rb'V(?:[0-9A-F]{2})*', part) is not None, 'COLUMN_METADATA')
            try:
                values.append(None if part == b'N' else bytes.fromhex(part[1:].decode('ascii')).decode('utf-8'))
            except (ValueError, UnicodeError):
                raise checks.GateError('COLUMN_METADATA') from None
        table, column, ordinal, column_type, charset, collation = values
        need(type(table) is str and table in tables and type(column) is str
             and re.fullmatch(r'[A-Za-z_][A-Za-z0-9_]{0,63}', column) is not None
             and type(ordinal) is str and re.fullmatch('[1-9][0-9]{0,3}', ordinal) is not None
             and type(column_type) is str and 0 < len(column_type) <= 65536, 'COLUMN_METADATA')
        number = int(ordinal)
        key = (table, column, number)
        order = (table, number)
        need(number == ordinals.get(table, 0) + 1 and (previous is None or previous < order)
             and key not in result and (table, column) not in names
             and (charset is None) == (collation is None), 'COLUMN_METADATA')
        names.add((table, column))
        ordinals[table] = number
        previous = order
        result[key] = (column_type, charset, collation)
    need(set(ordinals) == tables, 'COLUMN_METADATA')
    return result


@dataclass(frozen=True)
class Token:
    start: int
    end: int
    kind: str
    value: bytes


def tokens(raw: bytes):
    """Lex quoted/escaped tokens and opaque comments; preserve every original offset."""
    need(0 < len(raw) <= MAX_SQL, 'DDL_INPUT_LIMIT')
    index = 0
    count = 0
    while index < len(raw):
        start = index
        char = raw[index]
        if char in b' \t\r\n':
            index += 1
            continue
        if raw[index:index + 2] == b'/*':
            end = raw.find(b'*/', index + 2)
            need(end >= 0 and b'/*' not in raw[index + 2:end])
            index = end + 2
            continue
        if char == 35 or (raw[index:index + 2] == b'--' and raw[index + 2:index + 3] in (b' ', b'\t', b'\r', b'\n')):
            end = raw.find(b'\n', index)
            index = len(raw) if end < 0 else end + 1
            continue
        if char in (39, 34, 96):
            index += 1
            while index < len(raw):
                if raw[index] == 92:
                    index += 2
                    continue
                if raw[index] == char:
                    if raw[index + 1:index + 2] == bytes([char]):
                        index += 2
                        continue
                    index += 1
                    break
                index += 1
            else:
                raise checks.GateError('DDL_LEXICAL_PROOF')
            need(index <= len(raw))
            kind = 'identifier' if char == 96 else 'string'
        elif chr(char).isascii() and (chr(char).isalnum() or char == 95):
            index += 1
            while index < len(raw) and (65 <= raw[index] <= 90 or 97 <= raw[index] <= 122
                                       or 48 <= raw[index] <= 57 or raw[index] == 95):
                index += 1
            kind = 'word'
        else:
            index += 1
            kind = 'symbol'
        count += 1
        need(count <= 200000, 'DDL_INPUT_LIMIT')
        yield Token(start, index, kind, raw[start:index])


def simple_identifier(token: Token) -> str:
    need(token.kind == 'identifier' and re.fullmatch(rb'`[A-Za-z_][A-Za-z0-9_]{0,63}`', token.value) is not None)
    return token.value[1:-1].decode('ascii')


def column_sites(raw: bytes, metadata: dict) -> dict:
    """Only the type/charset/collation prefix of real CREATE TABLE columns is eligible."""
    sites = {}
    seen = set()
    statement = []
    depth = 0

    def inspect(items: list[Token]) -> None:
        if not items:
            return
        need(items[0].value.upper() != b'DELIMITER')
        if items[0].value.upper() != b'CREATE':
            return
        if len(items) == 3 and items[1].value == b'DATABASE':
            need(simple_identifier(items[2]) == 'atstudio_prod')
            return
        need(len(items) >= 5 and [item.value.upper() for item in items[:2]] == [b'CREATE', b'TABLE'])
        table = simple_identifier(items[2])
        need(items[3].value == b'(')
        ordinal = 0
        nesting = 1
        chunk = []
        closed = False
        for token in items[4:]:
            if closed:
                continue
            if token.kind == 'symbol' and token.value == b'(':
                nesting += 1
            if token.kind == 'symbol' and token.value == b')':
                nesting -= 1
            if (token.value == b',' and nesting == 1 and token.kind == 'symbol') or nesting == 0:
                need(bool(chunk))
                if chunk[0].kind == 'identifier':
                    ordinal += 1
                    column = simple_identifier(chunk[0])
                    key = (table, column, ordinal)
                    need(key in metadata and key not in seen, 'COLUMN_IDENTITY')
                    seen.add(key)
                    need(len(chunk) >= 2 and chunk[1].kind == 'word')
                    pos = 2
                    if pos < len(chunk) and chunk[pos].value == b'(':
                        level = 1
                        pos += 1
                        while pos < len(chunk) and level:
                            if chunk[pos].kind == 'symbol':
                                level += (chunk[pos].value == b'(') - (chunk[pos].value == b')')
                            pos += 1
                        need(level == 0)
                    if (chunk[1].value.lower() in STRING_TYPES and pos + 1 < len(chunk)
                            and chunk[pos].kind == 'word' and chunk[pos].value == b'COLLATE'
                            and chunk[pos + 1].value == b'utf8mb4_unicode_ci'):
                        start = chunk[pos].start - 1
                        need(raw[start:chunk[pos + 1].end] == COLLATION)
                        if metadata[key][1:] == ('utf8mb4', 'utf8mb4_unicode_ci'):
                            sites[start] = key
                else:
                    need(chunk[0].value.upper() in {b'PRIMARY', b'UNIQUE', b'KEY', b'CONSTRAINT', b'FULLTEXT', b'SPATIAL', b'CHECK'})
                chunk = []
                closed = nesting == 0
            else:
                chunk.append(token)
        need(closed and ordinal > 0)

    for token in tokens(raw):
        if token.kind == 'symbol':
            depth += (token.value == b'(') - (token.value == b')')
            need(depth >= 0)
        if token.kind == 'symbol' and token.value == b';':
            need(depth == 0)
            inspect(statement)
            statement = []
        else:
            statement.append(token)
    need(depth == 0 and not statement)
    need(seen == set(metadata), 'COLUMN_IDENTITY')
    return sites


def compare(source: bytes, restored: bytes, metadata: dict) -> tuple:
    """Witness only one-way exact insertions; all other bytes must be identical."""
    need(0 < len(restored) <= MAX_SQL, 'DDL_INPUT_LIMIT')
    sites = column_sites(source, metadata)
    left = 0
    right = 0
    witnessed = []
    for position, key in sorted(sites.items()):
        length = position - left
        need(source[left:position] == restored[right:right + length], 'DDL_OTHER_BYTES')
        right += length
        if restored[right:right + len(INSERTION + COLLATION)] == INSERTION + COLLATION:
            witnessed.append(key)
            right += len(INSERTION)
        left = position
    need(source[left:] == restored[right:], 'DDL_OTHER_BYTES')
    need(len(witnessed) == len(set(witnessed)), 'DDL_WITNESSES')
    return tuple(witnessed)


def verify(schema: bytes, full: bytes, recovered_schema: bytes, recovered_full: bytes,
           source_columns: dict, restored_columns: dict) -> dict:
    """Keep raw identity separate from the same 209 metadata-backed edits in both dumps."""
    need(source_columns == restored_columns and bool(source_columns), 'COLUMN_METADATA_DRIFT')
    schema_edits = compare(schema, recovered_schema, source_columns)
    full_edits = compare(full, recovered_full, source_columns)
    need(len(schema_edits) == 209 and schema_edits == full_edits, 'DDL_WITNESSES')
    return dict(proof='MYSQL_8046_REDUNDANT_COLUMN_CHARSET_INSERTIONS', edits=209,
                identities=[list(key) for key in schema_edits], raw_hashes_equal=False,
                all_other_bytes_equal=True, actual_column_metadata_equal=True)
