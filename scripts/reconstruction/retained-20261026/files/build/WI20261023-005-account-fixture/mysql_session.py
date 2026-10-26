"""One bounded mysql CLI session, no --force, private outputs and explicit ACK."""
import json
import queue
import secrets
import subprocess
import threading
import time
from fixture_core import need


class MysqlSession:
    def __init__(self,argv,env,seconds=160):
        need(0<seconds<=160 and '--force' not in argv,'SESSION_CONTRACT')
        self.deadline=time.monotonic()+seconds
        self.lines=queue.Queue(maxsize=1024)
        self.failed=threading.Event()
        # Merge at the OS pipe: a preceding ERROR cannot race an ACK on another pipe.
        # Non-JSON diagnostics are rejected without ever being returned or printed.
        self.process=subprocess.Popen(argv,stdin=subprocess.PIPE,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,env=env,shell=False)
        def output():
            try:
                while True:
                    row=self.process.stdout.readline(262145)
                    if not row:break
                    need(len(row)<=262144 and row.endswith(b'\n'),'LINE_BOUND')
                    self.lines.put_nowait(row.rstrip(b'\r\n'))
            except BaseException:self.failed.set()
            finally:
                try:self.lines.put_nowait(None)
                except queue.Full:self.failed.set()
        self.threads=[threading.Thread(target=output)]
        for thread in self.threads:thread.start()

    def query(self,statement):
        need(type(statement) is str and 0<len(statement.encode())<=65536,'STATEMENT_BOUND')
        marker='ATS_ACK_'+secrets.token_hex(16)
        need(not self.failed.is_set() and self.process.poll() is None,'SESSION_CLOSED')
        wire=(statement.rstrip(';')+";\nSELECT '"+marker+"';\n").encode()
        # Every command is <=64KiB and server consumption is under query/lock limits.
        # A writer thread avoids an unbounded synchronous stdin pipe write.
        error=[]
        def feed():
            try:
                self.process.stdin.write(wire)
                self.process.stdin.flush()
            except BaseException:error.append(True)
        writer=threading.Thread(target=feed)
        self.threads.append(writer)
        writer.start()
        rows=[]
        size=0
        deadline=min(self.deadline,time.monotonic()+12)
        try:
            while True:
                need(not self.failed.is_set() and not error,'MYSQL_REJECTED')
                remaining=deadline-time.monotonic()
                need(remaining>0,'MYSQL_TIMEOUT')
                row=self.lines.get(timeout=min(remaining,0.2)) if not self.lines.empty() else None
                if row is None:
                    if self.process.poll() is not None:raise ValueError('MYSQL_EOF')
                    time.sleep(min(0.01,remaining))
                    continue
                if row==marker.encode():
                    writer.join(timeout=1)
                    need(not writer.is_alive() and not error and not self.failed.is_set(),'MYSQL_ACK')
                    return rows
                size+=len(row)
                need(size<=4*1024*1024 and len(rows)<1024,'OUTPUT_BOUND')
                rows.append(json.loads(row))
        except BaseException:
            self.failed.set()
            if self.process.poll() is None:self.process.kill()
            writer.join(timeout=2)
            raise ValueError('MYSQL_SESSION_HOLD') from None

    def close(self):
        failures=[]
        try:
            if self.process.poll() is None:
                self.process.stdin.close()
                try:self.process.wait(timeout=2)
                except subprocess.TimeoutExpired:self.process.kill()
            self.process.wait(timeout=2)
        except BaseException:failures.append(True)
        for thread in self.threads:
            thread.join(timeout=2)
            if thread.is_alive():failures.append(True)
        for stream in (self.process.stdin,self.process.stdout):
            try:stream.close()
            except BaseException:failures.append(True)
        need(not failures and self.process.returncode==0 and not self.failed.is_set(),'MYSQL_REAP')
