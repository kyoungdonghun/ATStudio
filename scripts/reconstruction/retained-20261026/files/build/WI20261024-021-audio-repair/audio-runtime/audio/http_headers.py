"""Bounded response headers; Vary is the only repeatable field in this client."""
import http.client
import re
from audio_trial import need

TOKEN = re.compile(r"[!#$%&'*+.^_`|~0-9A-Za-z-]+")
FAILURE_CODES = frozenset('''DNS_CHANGED BASELINE_CHANGED NGINX_BASELINE RELEASE_TIME
SMTP_MAP_BYTES SMTP_MAP_EXPIRED LOGIN_HEADERS LOGIN_FRAMING LOGIN_LENGTH LOGIN_READ
LOGIN_STATUS LOGIN_RESPONSE LOGIN_TOKENS LOGIN_TOKEN_DISTINCT LOGIN_UNCERTAIN
RESPONSE_HEADERS DUPLICATE_HEADERS RESPONSE_ENCODING_REDIRECT RESPONSE_LENGTH
RESPONSE_BOUND RESPONSE_TRUNCATED RESPONSE_STATUS HTTP_HEADER_SYNTAX HTTP_VARY
HTTP_TIMEOUT HTTP_PROTOCOL IO_ERROR ACTOR_IDENTITY POST_LOGIN_DELTA UNCERTAIN_REQUEST
DATE_ROLLOVER CLOSED DEADLINE INTERRUPTED UNCLASSIFIED'''.split())


def normalize(pairs):
    need(type(pairs) is list and len(pairs) <= 32, 'RESPONSE_HEADERS')
    need(all(type(row) is tuple and len(row) == 2 and all(type(v) is str for v in row)
             for row in pairs), 'HTTP_HEADER_SYNTAX')
    need(sum(len(k) + len(v) for k, v in pairs) <= 8192, 'RESPONSE_HEADERS')
    result = {}; vary = []; seen = set()
    for name, value in pairs:
        need(TOKEN.fullmatch(name) is not None and all(c == '\t' or 32 <= ord(c) <= 255 and ord(c) != 127
             for c in value), 'HTTP_HEADER_SYNTAX')
        key = name.lower()
        if key == 'vary':
            for item in value.split(','):
                item = item.strip(' \t')
                need(TOKEN.fullmatch(item) is not None, 'HTTP_VARY')
                if item.lower() not in seen: vary.append(item); seen.add(item.lower())
        else:
            need(key not in result, 'DUPLICATE_HEADERS')
            result[key] = value
    if vary:
        need('*' not in seen or len(seen) == 1, 'HTTP_VARY')
        result['vary'] = ', '.join(vary)
    return result


def failure_code(error):
    if isinstance(error, TimeoutError): return 'HTTP_TIMEOUT'
    if isinstance(error, http.client.HTTPException): return 'HTTP_PROTOCOL'
    if isinstance(error, OSError): return 'IO_ERROR'
    code = str(error) if isinstance(error, ValueError) else ''
    return code if code in FAILURE_CODES else 'UNCLASSIFIED'
