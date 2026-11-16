import type { ListeningCatalogState } from '@/hooks/useListeningCatalog';
import Button from '@/components/ui/Button';
import styles from './ListeningCatalogControls.module.css';

export function ListeningCatalogStatus({
  catalog,
  onReload = catalog.reload,
  onReconcile = catalog.reconcile,
}: {
  catalog: ListeningCatalogState;
  onReload?: () => Promise<unknown>;
  onReconcile?: () => Promise<unknown>;
}) {
  return (
    <section className={styles.panel} aria-label="공개 목록 상태">
      <h2>공개 청취 목록</h2>
      <p id="publication-notice">
        공개 버튼을 누르면 누구나 로그인 없이 들을 수 있습니다. 공개할 권리가 있는 음원만 공개해
        주세요. 준비된 비활성 음원은 공개와 함께 활성화됩니다. 공개 중인 곡은 해제 후 수정할 수
        있습니다.
      </p>
      {catalog.loading ? (
        <p>공개 목록 확인 중...</p>
      ) : catalog.snapshot ? (
        <p>공개 {catalog.snapshot.trackIds.length}곡</p>
      ) : (
        <p>공개 상태를 확인할 수 없어 변경을 잠시 중지합니다.</p>
      )}
      {catalog.snapshot?.source === 'STATIC' && <p>현재 공개 목록은 읽기 전용입니다.</p>}
      <Button
        size="sm"
        variant="outline"
        disabled={catalog.pending || catalog.loading}
        onClick={() => void onReload()}
      >
        목록 새로고침
      </Button>
      {catalog.message && <p role="status">{catalog.message}</p>}
      {catalog.hold && (
        <div className={styles.hold} role="alert">
          <strong>이전 변경 결과를 확인해야 합니다.</strong>
          <p>
            중복 변경을 막기 위해 공개·해제를 잠시 중지했습니다. 아래 버튼으로 이전 요청의 처리
            기록을 확인해 주세요. 확인되지 않으면 운영 담당자에게 문의해 주세요.
          </p>
          <Button
            size="sm"
            disabled={!catalog.journalText || catalog.pending}
            onClick={() => void onReconcile()}
          >
            이전 변경 결과 확인
          </Button>
        </div>
      )}
      <details>
        <summary>운영 진단 정보</summary>
        {catalog.snapshot && (
          <p>
            설정: {catalog.snapshot.source} · revision {catalog.snapshot.revision}
            <br />
            선정 ID: {catalog.snapshot.trackIds.join(', ') || '없음'}
            <br />
            digest: {catalog.snapshot.digest}
          </p>
        )}
        {catalog.hold && (
          <>
            <p>
              HOLD: 현재 목록 조회만으로 해제하지 않습니다. 서버에서 이 실행자·시도의 정확한 변경
              기록과 최신 목록을 대조하며, 실패하거나 불일치하면 기록과 보류를 유지합니다. 이전
              요청을 자동으로 재전송하지 않습니다.
            </p>
            <Button
              size="sm"
              disabled={!catalog.journalText || catalog.pending}
              onClick={catalog.exportJournal}
            >
              시도 기록 저장
            </Button>
            <pre style={{ whiteSpace: 'pre-wrap', overflowWrap: 'anywhere' }}>
              {catalog.journalText ?? '저장소에 접근할 수 없습니다.'}
            </pre>
          </>
        )}
      </details>
      <p>공개해제 전에 시작된 재생·이미 받은 파일은 회수되지 않습니다.</p>
    </section>
  );
}
