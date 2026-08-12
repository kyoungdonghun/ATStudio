/** Admin whitelist channel operations */
import { useCallback, useEffect, useRef, useState } from 'react';
import {
  downloadAdminWhitelistExportBatch,
  exportAdminWhitelistChannels,
  fetchAdminWhitelistChannels,
  fetchRecentAdminWhitelistExports,
  updateAdminWhitelistChannelStatus,
  type AdminWhitelistChannel,
  type AdminWhitelistExportSummary,
} from '@/api/admin';
import Button from '@/components/ui/Button';
import ConfirmDialog from '@/components/ui/ConfirmDialog';
import Pagination from '@/components/ui/Pagination';
import type { PageInfo, WhitelistChannelStatus } from '@/types';
import { formatDateTime } from '@/utils/format';
import { getSafeYoutubeUrl } from '@/utils/safeYoutubeUrl';
import styles from './WhitelistChannelManagePage.module.css';
import { ADMIN_STATUS_TRANSITIONS } from './whitelistStatusTransitions';

const STATUSES: Array<WhitelistChannelStatus | ''> = [
  '',
  'DRAFT',
  'PENDING',
  'EXPORTED',
  'REGISTERED',
  'REVISION_REQUESTED',
  'REJECTED',
  'CANCELLED',
  'REMOVAL_REQUESTED',
];

const STATUS_LABELS: Record<WhitelistChannelStatus, string> = {
  DRAFT: '저장됨',
  PENDING: '등록 요청',
  EXPORTED: '외부 처리 중',
  REGISTERED: '등록 완료',
  REVISION_REQUESTED: '수정 요청',
  REJECTED: '반려',
  CANCELLED: '해제 완료',
  REMOVAL_REQUESTED: '해제 요청',
};

interface StatusEdit {
  status: WhitelistChannelStatus;
  adminNote: string;
}

interface ConfirmationState {
  title: string;
  message: string;
  confirmLabel: string;
  confirmVariant?: 'primary' | 'danger';
  action: () => Promise<void>;
}

function downloadBlob(blob: Blob, fileName: string) {
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = fileName;
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(url);
}

function exportConfirmationMessage(status: WhitelistChannelStatus | '', keyword: string): string {
  const statusScope = status ? `${STATUS_LABELS[status]}(${status})` : '전체(모든 상태)';
  const keywordScope = keyword ? `"${keyword}"` : '적용 안 함';
  const scope = `적용 범위는 상태: ${statusScope}, 검색어: ${keywordScope}입니다.`;

  if (!status) {
    return `${scope} 일치하는 모든 상태의 채널을 CSV로 내보냅니다. 그중 등록 요청(PENDING) 채널은 외부 처리 중(EXPORTED)으로 전환되고, 다른 상태는 변경되지 않습니다.`;
  }
  if (status === 'PENDING') {
    return `${scope} 일치하는 채널을 CSV로 내보내고 모두 외부 처리 중(EXPORTED)으로 전환합니다.`;
  }
  return `${scope} 일치하는 채널을 CSV로 내보냅니다. 채널 상태는 변경되지 않습니다.`;
}

function isDefinitiveClientFailure(error: unknown): boolean {
  const status = (error as { response?: { status?: number } })?.response?.status;
  return typeof status === 'number' && status >= 400 && status < 500;
}

export default function WhitelistChannelManagePage() {
  const [channels, setChannels] = useState<AdminWhitelistChannel[]>([]);
  const [pageInfo, setPageInfo] = useState<PageInfo | null>(null);
  const [page, setPage] = useState(1);
  const [statusFilter, setStatusFilter] = useState<WhitelistChannelStatus | ''>('PENDING');
  const [keywordInput, setKeywordInput] = useState('');
  const [keyword, setKeyword] = useState('');
  const [edits, setEdits] = useState<Record<number, StatusEdit>>({});
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [batchIDInput, setBatchIDInput] = useState('');
  const [recentExports, setRecentExports] = useState<AdminWhitelistExportSummary[]>([]);
  const [exportOutcomeUnknown, setExportOutcomeUnknown] = useState(false);
  const [confirmation, setConfirmation] = useState<ConfirmationState | null>(null);
  const [confirmationBusy, setConfirmationBusy] = useState(false);
  const listRequestId = useRef(0);

  const load = useCallback(async () => {
    const currentRequestId = ++listRequestId.current;
    try {
      setLoading(true);
      setError(null);
      const result = await fetchAdminWhitelistChannels({
        page,
        size: 20,
        status: statusFilter || undefined,
        keyword: keyword || undefined,
      });
      if (currentRequestId !== listRequestId.current) return;
      setChannels(result.dataList);
      setPageInfo(result.pageInfo);
      setEdits(
        Object.fromEntries(
          result.dataList.map((channel) => [
            channel.id,
            { status: channel.status, adminNote: channel.adminNote ?? '' },
          ]),
        ),
      );
    } catch {
      if (currentRequestId !== listRequestId.current) return;
      setChannels([]);
      setPageInfo(null);
      setEdits({});
      setError('화이트리스트 채널 목록을 불러오지 못했습니다.');
    } finally {
      if (currentRequestId === listRequestId.current) {
        setLoading(false);
      }
    }
  }, [keyword, page, statusFilter]);

  useEffect(() => {
    void load();
    return () => {
      listRequestId.current += 1;
    };
  }, [load]);

  function handleSearch() {
    setKeyword(keywordInput.trim());
    setPage(1);
  }

  function requestConfirmation(next: ConfirmationState) {
    setConfirmation((current) => current ?? next);
  }

  async function confirmOperation() {
    const current = confirmation;
    if (!current || confirmationBusy) return;
    setConfirmationBusy(true);
    try {
      await current.action();
      setConfirmation(null);
    } finally {
      setConfirmationBusy(false);
    }
  }

  function cancelConfirmation() {
    if (!confirmationBusy) setConfirmation(null);
  }

  function handleStatusUpdate(channel: AdminWhitelistChannel) {
    const edit = edits[channel.id];
    if (!edit) return;
    const request = {
      status: edit.status,
      adminNote: edit.adminNote.trim() || undefined,
    };
    requestConfirmation({
      title: '채널 상태 변경',
      message: `${channel.channelName} 상태를 ${STATUS_LABELS[edit.status]}(으)로 변경할까요?`,
      confirmLabel: '변경',
      confirmVariant: 'danger',
      action: async () => {
        try {
          setBusy(`status-${channel.id}`);
          setError(null);
          setMessage(null);
          await updateAdminWhitelistChannelStatus(channel.id, request);
          setMessage('상태가 저장되었습니다.');
          await load();
        } catch {
          setError('상태 저장에 실패했습니다.');
        } finally {
          setBusy(null);
        }
      },
    });
  }

  function handleExport() {
    if (!statusFilter && !keyword) {
      setError('CSV 범위를 기록하려면 상태 또는 검색어를 적용해주세요.');
      return;
    }

    const request = {
      status: statusFilter || undefined,
      keyword: keyword || undefined,
    };
    requestConfirmation({
      title: '화이트리스트 CSV 내보내기',
      message: exportConfirmationMessage(statusFilter, keyword),
      confirmLabel: '내보내기',
      action: async () => {
        try {
          setBusy('export');
          setError(null);
          setMessage(null);
          setRecentExports([]);
          setExportOutcomeUnknown(false);
          const { batchId, blob, fileName } = await exportAdminWhitelistChannels(request);
          downloadBlob(blob, fileName);
          setBatchIDInput(String(batchId));
          setMessage(`CSV export가 완료되었습니다. Batch ${batchId}`);
          await load();
        } catch (exportError) {
          if (isDefinitiveClientFailure(exportError)) {
            setError('CSV export에 실패했습니다.');
            return;
          }

          listRequestId.current += 1;
          setLoading(false);
          setChannels([]);
          setPageInfo(null);
          setEdits({});
          setExportOutcomeUnknown(true);
          try {
            const candidates = await fetchRecentAdminWhitelistExports(request);
            setRecentExports(candidates);
            setError(
              'CSV 내보내기 결과를 확인할 수 없습니다. 같은 범위의 최근 batch를 확인했습니다.',
            );
          } catch {
            setRecentExports([]);
            setError(
              'CSV 내보내기 결과를 확인할 수 없습니다. 같은 범위의 최근 batch 조회도 실패했습니다.',
            );
          }
        } finally {
          setBusy(null);
        }
      },
    });
  }

  async function handleBatchDownload(requestedBatchID?: number) {
    const batchID = requestedBatchID ?? Number(batchIDInput);
    if (!Number.isInteger(batchID) || batchID <= 0) {
      setError('올바른 export batch ID를 입력해주세요.');
      return;
    }

    try {
      setBusy('batch-download');
      setError(null);
      setMessage(null);
      const { blob, fileName } = await downloadAdminWhitelistExportBatch(batchID);
      downloadBlob(blob, fileName);
      setBatchIDInput(String(batchID));
      setMessage(`Batch ${batchID} CSV를 다시 내려받았습니다.`);
    } catch {
      setError('Export batch 재다운로드에 실패했습니다.');
    } finally {
      setBusy(null);
    }
  }

  return (
    <div className={styles.page}>
      <div className={styles.header}>
        <div>
          <h1 className={styles.title}>{'화이트리스트 운영'}</h1>
          <p className={styles.subtitle}>
            {'사용자가 요청한 YouTube 채널을 확인하고, 외부 등록 처리용 CSV를 내보냅니다.'}
          </p>
        </div>
        <Button
          onClick={() => void handleExport()}
          loading={busy === 'export'}
          disabled={!statusFilter && !keyword}
        >
          {'CSV 내보내기'}
        </Button>
      </div>

      <div className={styles.toolbar}>
        <label className={styles.filter}>
          <span>{'상태'}</span>
          <select
            value={statusFilter}
            onChange={(e) => {
              setStatusFilter(e.target.value as WhitelistChannelStatus | '');
              setPage(1);
            }}
          >
            {STATUSES.map((status) => (
              <option key={status || 'ALL'} value={status}>
                {status ? STATUS_LABELS[status] : '전체'}
              </option>
            ))}
          </select>
        </label>
        <input
          className={styles.searchInput}
          value={keywordInput}
          placeholder="이메일, 닉네임, 채널명, URL 검색"
          onChange={(e) => setKeywordInput(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === 'Enter') handleSearch();
          }}
        />
        <Button variant="ghost" size="sm" onClick={handleSearch}>
          {'검색'}
        </Button>
        <label className={styles.filter}>
          <span>{'Export batch ID'}</span>
          <input
            className={styles.batchInput}
            inputMode="numeric"
            value={batchIDInput}
            onChange={(event) => setBatchIDInput(event.target.value)}
          />
        </label>
        <Button
          variant="outline"
          size="sm"
          onClick={() => void handleBatchDownload()}
          loading={busy === 'batch-download'}
        >
          {'다시 받기'}
        </Button>
      </div>

      {message && <div className={styles.success}>{message}</div>}
      {error && <div className={styles.error}>{error}</div>}

      {exportOutcomeUnknown && (
        <section className={styles.recovery} aria-labelledby="recent-export-heading">
          <h2 id="recent-export-heading">{'같은 범위의 최근 export batch'}</h2>
          {recentExports.length === 0 ? (
            <p>{'확인된 후보 batch가 없습니다.'}</p>
          ) : (
            <ul className={styles.recoveryList}>
              {recentExports.map((batch) => (
                <li key={batch.batchId} className={styles.recoveryItem}>
                  <div>
                    <strong>{batch.fileName}</strong>
                    <span>{`Batch ${batch.batchId}`}</span>
                    <span>{`${batch.itemCount}건`}</span>
                    <span>{`상태: ${batch.status ? `${STATUS_LABELS[batch.status]}(${batch.status})` : '전체'}`}</span>
                    <span>{`검색어: ${batch.keyword ? `"${batch.keyword}"` : '적용 안 함'}`}</span>
                    <time dateTime={batch.createdAt}>{formatDateTime(batch.createdAt)}</time>
                  </div>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => void handleBatchDownload(batch.batchId)}
                    loading={busy === 'batch-download'}
                  >
                    {`Batch ${batch.batchId} 다시 받기`}
                  </Button>
                </li>
              ))}
            </ul>
          )}
        </section>
      )}

      {loading ? (
        <div className={styles.loading}>{'Loading...'}</div>
      ) : (
        <div className={styles.tableWrap}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>{'요청자'}</th>
                <th>{'채널'}</th>
                <th>{'상태'}</th>
                <th>{'플랜'}</th>
                <th>{'요청일'}</th>
                <th>{'처리'}</th>
              </tr>
            </thead>
            <tbody>
              {channels.length === 0 && (
                <tr>
                  <td colSpan={6} className={styles.empty}>
                    {'조회된 채널이 없습니다.'}
                  </td>
                </tr>
              )}
              {channels.map((channel) => {
                const edit = edits[channel.id] ?? {
                  status: channel.status,
                  adminNote: channel.adminNote ?? '',
                };
                const statusTargets = ADMIN_STATUS_TRANSITIONS[channel.status];
                const safeChannelUrl = getSafeYoutubeUrl(channel.channelUrl);
                return (
                  <tr key={channel.id}>
                    <td>
                      <strong>{channel.userNickname}</strong>
                      <span className={styles.subText}>{channel.userEmail}</span>
                    </td>
                    <td className={styles.channelCell}>
                      <strong>{channel.channelName}</strong>
                      {safeChannelUrl ? (
                        <a href={safeChannelUrl} target="_blank" rel="noopener noreferrer">
                          {channel.channelUrl}
                        </a>
                      ) : (
                        <span>{channel.channelUrl}</span>
                      )}
                      <span>{channel.youtubeHandle || '-'}</span>
                      <span>{channel.youtubeChannelId || '채널 ID 미입력'}</span>
                    </td>
                    <td>
                      <span
                        className={`${styles.statusBadge} ${styles[`status${channel.status}`]}`}
                      >
                        {STATUS_LABELS[channel.status]}
                      </span>
                      {channel.primary && <span className={styles.primaryBadge}>{'대표'}</span>}
                    </td>
                    <td>
                      <span>{channel.planName ?? '-'}</span>
                      <span className={styles.subText}>{channel.billingCycle ?? '-'}</span>
                    </td>
                    <td>
                      {channel.requestedAt ? formatDateTime(channel.requestedAt) : '-'}
                      {channel.exportedAt && (
                        <span className={styles.subText}>
                          {`export ${formatDateTime(channel.exportedAt)}`}
                        </span>
                      )}
                    </td>
                    <td className={styles.actionCell}>
                      <select
                        value={edit.status}
                        disabled={statusTargets.length === 0}
                        onChange={(e) =>
                          setEdits((prev) => ({
                            ...prev,
                            [channel.id]: {
                              ...edit,
                              status: e.target.value as WhitelistChannelStatus,
                            },
                          }))
                        }
                      >
                        {[channel.status, ...statusTargets].map((status) => (
                          <option key={status} value={status}>
                            {STATUS_LABELS[status]}
                          </option>
                        ))}
                      </select>
                      <textarea
                        value={edit.adminNote}
                        placeholder="운영자 메모"
                        onChange={(e) =>
                          setEdits((prev) => ({
                            ...prev,
                            [channel.id]: {
                              ...edit,
                              adminNote: e.target.value,
                            },
                          }))
                        }
                      />
                      <Button
                        size="sm"
                        onClick={() => void handleStatusUpdate(channel)}
                        loading={busy === `status-${channel.id}`}
                        disabled={edit.status === channel.status}
                      >
                        {'저장'}
                      </Button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}

      {pageInfo && pageInfo.total > pageInfo.size && (
        <Pagination pageInfo={pageInfo} currentPage={page} onPageChange={setPage} />
      )}

      <ConfirmDialog
        open={confirmation !== null}
        title={confirmation?.title ?? ''}
        message={confirmation?.message ?? ''}
        confirmLabel={confirmation?.confirmLabel}
        confirmVariant={confirmation?.confirmVariant}
        busy={confirmationBusy}
        onConfirm={() => void confirmOperation()}
        onCancel={cancelConfirmation}
      />
    </div>
  );
}
