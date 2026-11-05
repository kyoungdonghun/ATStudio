import { useState } from 'react';
import { Link } from 'react-router-dom';
import type { ListeningCatalogState } from '@/hooks/useListeningCatalog';
import Modal from '@/components/ui/Modal';
import Button from '@/components/ui/Button';
import styles from './ListeningCatalogControls.module.css';

export function ListeningCatalogStatus({ catalog }: { catalog: ListeningCatalogState }) {
  return (
    <section className={styles.panel} aria-label="공개 목록 상태">
      <h2>공개 청취 목록</h2>
      <p>
        업로드·활성화만으로 공개되지 않습니다. 활성 상태, 변환 완료, 크레딧·권리를 확인한 뒤
        명시적으로 공개하세요. 공개 중인 곡은 해제 후 수정합니다.
      </p>
      {catalog.loading ? (
        <p>공개 목록 확인 중...</p>
      ) : catalog.snapshot ? (
        <p>
          설정: {catalog.snapshot.source} · revision {catalog.snapshot.revision} · 공개{' '}
          {catalog.snapshot.trackIds.length}곡<br />
          선정 ID: {catalog.snapshot.trackIds.join(', ') || '없음'}
        </p>
      ) : (
        <p>공개 상태 확인 불가 — 변경 중지</p>
      )}
      {catalog.snapshot?.source === 'STATIC' && (
        <p>
          현재는 읽기 전용 고정 목록입니다. 운영 설정 전환이 필요하며 이 화면에서 초기화하지
          않습니다.
        </p>
      )}
      <Button
        size="sm"
        variant="outline"
        disabled={catalog.pending || catalog.loading}
        onClick={() => void catalog.reload()}
      >
        현재 상태 다시 조회
      </Button>
      {catalog.message && <p role="status">{catalog.message}</p>}
      {catalog.hold && (
        <div className={styles.hold} role="alert">
          <strong>HOLD — 변경 결과 확인 전 요청 반복 금지</strong>
          <p>
            현재 목록은 이전 요청의 성공을 입증하지 않습니다. 승인된 운영 담당자가 별도 읽기 전용
            감사에서 LISTENING_CATALOG_REPLACE 기록의 실행자·사유·시각·전후 revision/ID/digest를
            대조하고 결과를 기록해야 합니다. 서버 감사 대조는 이 시도의 실행자·전체 제안과 정확히
            일치할 때만 확인됩니다. 이 운영자와 현재 브라우저 탭에 보류를 유지합니다.
          </p>
          <p>
            현재 목록 조회만으로 HOLD를 해제하지 않습니다. 서버에서 정확히 일치하는 커밋 감사와 최신
            목록을 확인해야 합니다. 확인되지 않으면 요청 기록을 보존하고 별도로 승인된 정확한 감사
            대조·복구 절차를 진행하세요. 현재 상태가 같아도 완료로 처리하지 않습니다.
          </p>
          <Button
            size="sm"
            disabled={!catalog.journalText || catalog.pending}
            onClick={catalog.exportJournal}
          >
            시도 기록 저장
          </Button>
          <Button
            size="sm"
            disabled={!catalog.journalText || catalog.pending}
            onClick={() => void catalog.reconcile()}
          >
            서버 감사기록 확인
          </Button>
          <details>
            <summary>현재 시도 기록</summary>
            <pre style={{ whiteSpace: 'pre-wrap', overflowWrap: 'anywhere' }}>
              {catalog.journalText ?? '저장소에 접근할 수 없습니다. 변경 보류 상태를 확인하세요.'}
            </pre>
          </details>
        </div>
      )}
      <p>공개해제 전에 시작된 재생·이미 받은 파일은 회수되지 않습니다.</p>
    </section>
  );
}

export interface PublicationTarget {
  id: number;
  title: string;
  publish: boolean;
  revision: number;
}
export function ListeningPublicationDialog({
  catalog,
  target,
  onClose,
}: {
  catalog: ListeningCatalogState;
  target: PublicationTarget | null;
  onClose: () => void;
}) {
  const [reason, setReason] = useState('');
  const [credits, setCredits] = useState(false);
  const stale = target !== null && target.revision !== catalog.snapshot?.revision;
  return (
    <Modal
      open={target !== null}
      onClose={onClose}
      title={target?.publish ? '음원 공개 확인' : '음원 공개해제 확인'}
      busy={catalog.pending}
    >
      <div className={styles.panel}>
        <p>
          {target?.title} (ID {target?.id})
        </p>
        <p>
          {target?.publish
            ? '활성 상태·변환 완료·크레딧과 권리를 확인하세요. 업로드는 공개 승인이 아닙니다.'
            : '공개해제 응답 확인 후 수정할 수 있습니다. 수정·변환·표기를 확인한 뒤 별도로 다시 공개하세요.'}
        </p>
        <label>
          변경 사유
          <textarea
            maxLength={500}
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            disabled={catalog.pending}
          />
        </label>
        <label>
          <input
            type="checkbox"
            checked={credits}
            onChange={(e) => setCredits(e.target.checked)}
            disabled={catalog.pending}
          />
          이 변경의 권리·크레딧 확인을 완료했습니다.
        </label>
        {stale && <p role="alert">목록이 바뀌었습니다. 닫은 후 최신 목록에서 다시 선택하세요.</p>}
        {catalog.message && <p role="status">{catalog.message}</p>}
        <Button
          disabled={
            catalog.pending ||
            catalog.hold ||
            catalog.loading ||
            stale ||
            catalog.snapshot?.source !== 'DATABASE' ||
            !reason.trim() ||
            !credits
          }
          onClick={async () => {
            if (target && (await catalog.change(target.id, target.publish, reason, credits)))
              onClose();
          }}
        >
          {' '}
          {target?.publish ? '공개 확정' : '공개해제 확정'}{' '}
        </Button>
        <Button variant="ghost" disabled={catalog.pending} onClick={onClose}>
          닫기
        </Button>
        {!target?.publish && (
          <p>
            해제 후 <Link to="/admin/track-manage">음원 관리</Link>에서 수정하세요.
          </p>
        )}
      </div>
    </Modal>
  );
}
