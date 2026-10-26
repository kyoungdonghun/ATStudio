"""Offline exact-JAR extraction and Java17 compile; never invokes the app."""
import hashlib,json,os,subprocess,zipfile
from pathlib import Path

HERE=Path(__file__).resolve().parent
JAR=HERE.parent/'WI068-release-run-02/build/libs/ATStudio-0.0.1-SNAPSHOT.jar'
PIN='f3b748f8b3fb4fbd4027b797ebf0bc79db62bc9671e5d7e45a8397732d2aef81'

if __name__=='__main__':
    assert hashlib.sha256(JAR.read_bytes()).hexdigest()==PIN
    libs=HERE/'crypto'
    libs.mkdir(exist_ok=True)
    with zipfile.ZipFile(JAR) as archive:
        for name in ('spring-security-crypto-7.0.7.jar','commons-logging-1.3.6.jar'):
            raw=archive.read('BOOT-INF/lib/'+name)
            target=libs/name
            if target.exists():assert target.read_bytes()==raw
            else:target.write_bytes(raw)
    java=Path('C:/Program Files/Java/jdk-17/bin')
    cp=os.pathsep.join(str(p) for p in sorted(libs.glob('*.jar')))
    subprocess.run([str(java/'javac.exe'),'--release','17','-encoding','UTF-8','-cp',cp,'-d',str(libs),str(HERE/'FixturePasswords.java')],check=True,capture_output=True,timeout=30)
    result=subprocess.run([str(java/'java.exe'),'-cp',str(libs)+os.pathsep+cp,'FixturePasswords','self-test'],check=True,capture_output=True,timeout=15)
    receipt=json.loads(result.stdout)
    receipt['jar_sha256']=PIN
    receipt['files']={p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in sorted(libs.iterdir()) if p.is_file()}
    (HERE/'crypto-receipt.json').write_text(json.dumps(receipt,indent=2)+'\n')
    print(json.dumps(receipt))
