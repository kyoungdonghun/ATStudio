/** Screen 22: Notice detail */
import { useEffect, useRef, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { downloadNoticeAttachment, fetchNotice } from '@/api/notices';
import { createDownloadFallbackFileName, triggerBlobDownload } from '@/api/downloads';
import { classifyLoadError, getLoadErrorMessageForKind, type LoadErrorKind } from '@/api/loadError';
import type { Notice, NoticeAttachmentInfo } from '@/types';
import { formatDate } from '@/utils/format';
import { parsePositiveDecimalRouteID } from '@/utils/routeId';
import styles from './NoticeDetailPage.module.css';

function formatFileSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

type VisibleLoadError = Exclude<LoadErrorKind, 'cancelled'>;

interface DownloadOperation {
  targetID: number;
  attachmentID: number;
  controller: AbortController;
}

export default function NoticeDetailPage() {
  const { noticeId } = useParams<{ noticeId: string }>();
  const parsedNoticeID = parsePositiveDecimalRouteID(noticeId);
  const currentTargetRef = useRef(parsedNoticeID);
  currentTargetRef.current = parsedNoticeID;

  const [notice, setNotice] = useState<Notice | null>(null);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<VisibleLoadError | null>(null);
  const [retryGeneration, setRetryGeneration] = useState(0);
  const loadGenerationRef = useRef(0);
  const loadInFlightRef = useRef(false);

  const downloadOperationsRef = useRef(new Map<number, DownloadOperation>());
  const [pendingDownloadIDs, setPendingDownloadIDs] = useState<Set<number>>(new Set());
  const [downloadErrors, setDownloadErrors] = useState<Record<number, string>>({});

  useEffect(() => {
    const retireDownloads = () => {
      downloadOperationsRef.current.forEach((operation) => operation.controller.abort());
      downloadOperationsRef.current.clear();
      setPendingDownloadIDs(new Set());
      setDownloadErrors({});
    };

    retireDownloads();
    if (parsedNoticeID === null) {
      loadGenerationRef.current += 1;
      setNotice(null);
      setLoadError('not-found');
      setLoading(false);
      loadInFlightRef.current = false;
      return retireDownloads;
    }

    const generation = ++loadGenerationRef.current;
    const controller = new AbortController();
    loadInFlightRef.current = true;
    setNotice(null);
    setLoadError(null);
    setLoading(true);

    fetchNotice(parsedNoticeID, controller.signal)
      .then((result) => {
        if (
          generation === loadGenerationRef.current &&
          !controller.signal.aborted &&
          currentTargetRef.current === parsedNoticeID
        ) {
          setNotice(result);
        }
      })
      .catch((error: unknown) => {
        if (
          generation !== loadGenerationRef.current ||
          controller.signal.aborted ||
          currentTargetRef.current !== parsedNoticeID
        ) {
          return;
        }
        const kind = classifyLoadError(error);
        if (kind !== 'cancelled') setLoadError(kind);
      })
      .finally(() => {
        if (
          generation === loadGenerationRef.current &&
          !controller.signal.aborted &&
          currentTargetRef.current === parsedNoticeID
        ) {
          setLoading(false);
          loadInFlightRef.current = false;
        }
      });

    return () => {
      controller.abort();
      if (generation === loadGenerationRef.current) loadGenerationRef.current += 1;
      loadInFlightRef.current = false;
      retireDownloads();
    };
  }, [parsedNoticeID, retryGeneration]);

  function retryLoad() {
    if (parsedNoticeID === null || loadInFlightRef.current) return;
    loadInFlightRef.current = true;
    setRetryGeneration((generation) => generation + 1);
  }

  async function handleAttachmentDownload(attachment: NoticeAttachmentInfo) {
    if (
      parsedNoticeID === null ||
      currentTargetRef.current !== parsedNoticeID ||
      downloadOperationsRef.current.has(attachment.id)
    ) {
      return;
    }

    const controller = new AbortController();
    const operation = {
      targetID: parsedNoticeID,
      attachmentID: attachment.id,
      controller,
    };
    downloadOperationsRef.current.set(attachment.id, operation);
    const isCurrent = () =>
      downloadOperationsRef.current.get(attachment.id) === operation &&
      currentTargetRef.current === operation.targetID &&
      !controller.signal.aborted;

    setPendingDownloadIDs((current) => new Set(current).add(attachment.id));
    setDownloadErrors((current) => {
      const next = { ...current };
      delete next[attachment.id];
      return next;
    });

    try {
      const download = await downloadNoticeAttachment(
        parsedNoticeID,
        attachment.id,
        createDownloadFallbackFileName('notice-attachment', attachment.id, attachment.originalName),
        controller.signal,
      );
      if (isCurrent()) triggerBlobDownload(download);
    } catch (error) {
      if (!isCurrent() || classifyLoadError(error) === 'cancelled') return;
      setDownloadErrors((current) => ({
        ...current,
        [attachment.id]: `${attachment.originalName} 다운로드에 실패했습니다.`,
      }));
    } finally {
      if (downloadOperationsRef.current.get(attachment.id) === operation) {
        downloadOperationsRef.current.delete(attachment.id);
        setPendingDownloadIDs((current) => {
          const next = new Set(current);
          next.delete(attachment.id);
          return next;
        });
      }
    }
  }

  if (loading) {
    return (
      <div className={styles.page}>
        <div className={styles.loading}>공지사항을 불러오는 중...</div>
      </div>
    );
  }

  if (loadError || !notice) {
    const missing = loadError === 'not-found';
    return (
      <div className={styles.page}>
        <section className={styles.recovery} role="alert">
          <h1 className={styles.recoveryTitle}>
            {missing ? '공지사항을 찾을 수 없습니다' : '공지사항을 불러오지 못했습니다'}
          </h1>
          <p className={styles.recoveryMessage}>
            {missing
              ? '삭제되었거나 존재하지 않는 공지사항입니다.'
              : getLoadErrorMessageForKind(loadError ?? 'unknown', '공지사항')}
          </p>
          <div className={styles.recoveryActions}>
            <Link to="/notices" className={styles.backLink}>
              공지사항 목록으로
            </Link>
            {!missing && (
              <button type="button" className={styles.retryButton} onClick={retryLoad}>
                다시 시도
              </button>
            )}
          </div>
        </section>
      </div>
    );
  }

  const attachments = notice.attachments ?? [];

  return (
    <div className={styles.page}>
      <nav className={styles.breadcrumb} aria-label="경로">
        <Link to="/">홈</Link>
        <span>&rsaquo;</span>
        <Link to="/notices">공지사항</Link>
        <span>&rsaquo;</span>
        <span className={styles.breadcrumbCurrent}>{notice.title}</span>
      </nav>

      <header className={styles.header}>
        <div className={styles.headerTop}>
          {notice.isPinned && <span className={styles.pinBadge}>고정</span>}
          <h1 className={styles.title}>{notice.title}</h1>
        </div>
        <div className={styles.meta}>
          <span>{formatDate(notice.createdAt)}</span>
          <span className={styles.metaDivider}>{'|'}</span>
          <span>{`조회 ${notice.viewCount.toLocaleString()}`}</span>
        </div>
      </header>

      <article className={styles.content}>
        {notice.content.split('\n').map((line, index) => (
          <p key={index}>{line || '\u00A0'}</p>
        ))}
      </article>

      {attachments.length > 0 && (
        <section className={styles.attachSection} aria-labelledby="notice-attachments-title">
          <h2 id="notice-attachments-title" className={styles.attachTitle}>
            첨부파일
          </h2>
          <ul className={styles.attachList} aria-label="첨부파일">
            {attachments.map((attachment) => {
              const pending = pendingDownloadIDs.has(attachment.id);
              return (
                <li key={attachment.id} className={styles.attachItem}>
                  <div className={styles.attachRow}>
                    <button
                      type="button"
                      className={styles.attachLink}
                      disabled={pending}
                      aria-label={
                        pending ? `${attachment.originalName} 다운로드 중` : attachment.originalName
                      }
                      onClick={() => void handleAttachmentDownload(attachment)}
                    >
                      {attachment.originalName}
                    </button>
                    <span className={styles.attachSize}>{formatFileSize(attachment.fileSize)}</span>
                  </div>
                  {downloadErrors[attachment.id] && (
                    <p className={styles.downloadError} role="alert">
                      {downloadErrors[attachment.id]}
                    </p>
                  )}
                </li>
              );
            })}
          </ul>
        </section>
      )}

      <div className={styles.footer}>
        <Link to="/notices" className={styles.backLink}>
          공지사항 목록으로
        </Link>
      </div>
    </div>
  );
}
