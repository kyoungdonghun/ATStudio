"""WI-20261026-ATS-004 local candidate triage. Never emits matched values.
This is a bounded pattern gate, not a universal absence-of-secrets guarantee.
No staging, network, candidate writes, secret-file or ignored-tree traversal.
"""
from __future__ import annotations
import argparse, hashlib, ipaddress, json, pathlib, re, subprocess
from collections import Counter
from datetime import datetime, timezone

EMAIL = re.compile(r"(?<![\w.-])[\w.+-]+@(?:[\w-]+\.)+[A-Za-z]{2,}(?![\w.-])")
IP = re.compile(r"(?<!\d)(?:\d{1,3}\.){3}\d{1,3}(?!\d)")
PATTERNS = {
 "private_key_marker": re.compile(r"-----BEGIN (?:RSA |EC |OPENSSH |DSA )?PRIVATE KEY-----"),
 "jwt_literal": re.compile(r"\beyJ[A-Za-z0-9_-]{10,}\.[A-Za-z0-9_-]{10,}\.[A-Za-z0-9_-]{10,}"),
 "provider_key_literal": re.compile(r"\b(?:live_[gs]sk|live_[gs]ck|test_[gs]sk|test_[gs]ck|sk_live|sk_test|AKIA)[A-Za-z0-9_-]{12,}\b"),
 "korean_phone": re.compile(r"(?<![A-Za-z0-9])01[016789][ -]?\d{3,4}[ -]?\d{4}(?![A-Za-z0-9])"),
 "korean_identity_number": re.compile(r"(?<!\d)\d{6}-[1-8]\d{6}(?!\d)"),
 "business_registration_number": re.compile(r"(?<!\d)\d{3}-\d{2}-\d{5}(?!\d)"),
 "userinfo_url": re.compile(r"\b(?:https?|mysql|jdbc:mysql|s3)://[^/\s:@]+:[^/\s@]+@"),
 "personal_home_path": re.compile(r"(?:[A-Z]:[\\/]+Users[\\/]+|/home/)[A-Za-z0-9_.-]+", re.I),
 "literal_sensitive_assignment": re.compile(r"""(?ix)\b(?:password|passwd|secret|api[_-]?key|access[_-]?token|refresh[_-]?token)\b["']?\s*[:=]\s*["']([^"'\r\n]{8,})["']"""),
 "private_input_reference": re.compile(r"(?i)(?:private-input|private_input|actor-credentials|recovery-password|recovery_password)\.(?:json|txt|env)"),
}
TEXT_EXTS={".md",".py",".java",".json",".yml",".yaml",".xml",".ps1",".sh",".bat",".cmd",".conf",".cnf",".template",".example",".service",".timer",".candidate",".txt",".properties",".toml",".gradle",".html",".css",".ts",".tsx",".js"}
NO_BODY_EXTS={".pem",".key",".p12",".jks",".sql",".hwp",".hwpx"}
SAFE_DOMAINS={"example.com","example.org","example.net","example.test","test.local","localhost","mail.test","test.com","test.test","test.invalid","invalid"}
SAFE_IPS={"0.0.0.0","127.0.0.1","255.255.255.255","255.255.255.0"}
def digest(data): return hashlib.sha256(data).hexdigest()
def occurrences(text):
    out=[]
    for kind,pat in PATTERNS.items():
        for match in pat.finditer(text):
            # Real secrets can match any literal; review synthetic fixtures separately.
            out.append({"kind":kind,"line":text.count("\n",0,match.start())+1})
    for m in EMAIL.finditer(text):
        domain=m.group().split("@",1)[1].lower()
        if domain in SAFE_DOMAINS or domain.endswith((".example",".invalid",".test")): continue
        out.append({"kind":"email_identifier","line":text.count("\n",0,m.start())+1})
    for m in IP.finditer(text):
        try: ip=ipaddress.ip_address(m.group())
        except ValueError: continue
        if str(ip) in SAFE_IPS or str(ip).startswith(("127.","192.0.2.","198.51.100.","203.0.113.")): continue
        out.append({"kind":"non_example_ip","line":text.count("\n",0,m.start())+1})
    unique={(x["kind"],x["line"]) for x in out}
    return [dict(kind=k,line=n) for k,n in sorted(unique)]
def files(root, explicit):
    if explicit:
        value=json.loads(pathlib.Path(explicit).read_text(encoding="utf-8-sig"))
        paths=value if isinstance(value,list) else value["paths"]
        return [("EXPLICIT",p) for p in paths]
    raw=subprocess.check_output(["git","status","--porcelain=v1","-z","--untracked-files=all"],cwd=root)
    chunks=raw.decode("utf-8").split("\0"); rows=[]; i=0
    while i<len(chunks):
        row=chunks[i];i+=1
        if not row: continue
        if row[0] in "RC" or row[1] in "RC": raise RuntimeError("RENAME_REQUIRES_EXPLICIT_REVIEW")
        rows.append((row[:2],row[3:]))
    return rows
def main():
    parser=argparse.ArgumentParser();parser.add_argument("--root",default=".")
    parser.add_argument("--paths");parser.add_argument("--output",required=True)
    args=parser.parse_args();root=pathlib.Path(args.root).resolve();rows=[]
    for status,name in files(root,args.paths):
        rel=pathlib.PurePosixPath(name)
        if rel.is_absolute() or ".." in rel.parts: raise RuntimeError("PATH_ESCAPE")
        path=root.joinpath(*rel.parts)
        if not path.is_relative_to(root) or path.is_symlink(): raise RuntimeError("SYMLINK_OR_ESCAPE")
        row={"path":name,"git_status":status}
        if not path.is_file():
            row.update(disposition="HOLD_MISSING",findings=[]);rows.append(row);continue
        if path.name.startswith(".env") or path.suffix.lower() in NO_BODY_EXTS:
            row.update(disposition="EXCLUDE_PRIVATE_TYPE_NO_BODY_READ",bytes=path.stat().st_size,findings=[])
            rows.append(row);continue
        data=path.read_bytes();row.update(bytes=len(data),sha256=digest(data))
        if path.suffix.lower() not in TEXT_EXTS and path.name not in {"AGENTS.md","CLAUDE.md","README.md","90-atstudio-admin"}:
            row.update(disposition="EXCLUDE_BINARY_OR_UNKNOWN",findings=[]);rows.append(row);continue
        try: body=data.decode("utf-8-sig")
        except UnicodeDecodeError:
            row.update(disposition="EXCLUDE_NON_UTF8",findings=[]);rows.append(row);continue
        if "\0" in body:
            row.update(disposition="EXCLUDE_NUL",findings=[]);rows.append(row);continue
        found=occurrences(body);row["findings"]=found
        row["disposition"]="REVIEW_MATCHES" if found else "TEXT_PATTERN_CLEAR_CANDIDATE"
        rows.append(row)
    result={"schema":1,"wi":"WI-20261026-ATS-004","checked_at":datetime.now(timezone.utc).isoformat(),
      "head":subprocess.check_output(["git","rev-parse","HEAD"],cwd=root,text=True).strip(),
      "scope":"Named visible candidates only; body values never included; pattern-clear is not final approval",
      "counts":dict(Counter(r["disposition"] for r in rows)),"files":rows}
    out=pathlib.Path(args.output);out.parent.mkdir(parents=True,exist_ok=True)
    if out.exists():raise RuntimeError("PRESERVE_PRIOR_RECEIPT")
    out.write_text(json.dumps(result,indent=2,ensure_ascii=False)+"\n",encoding="utf-8")
    print(json.dumps({"files":len(rows),"counts":result["counts"]},ensure_ascii=False))
if __name__=="__main__":main()
