/** Screen 11: Download queue (cart) */
import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import {
  fetchDownloadQueue,
  removeFromDownloadQueue,
  type QueueListItem,
} from '@/api/downloadQueue';
import { downloadTrack, triggerBlobDownload, fetchDownloadCount, type DownloadCount } from '@/api/downloads';
import { fetchMyLicenses } from '@/api/licenses';
import { toUploadUrl } from '@/api/client';
import { usePlayerStore } from '@/store/playerStore';
import { useAuthStore } from '@/store/authStore';
import { useToastStore } from '@/store/toastStore';
import styles from './DownloadQueuePage.module.css';

export default function DownloadQueuePage() {
  const [items, setItems] = useState<QueueListItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [downloading, setDownloading] = useState<number | null>(null);
  const [dlCount, setDlCount] = useState<DownloadCount | null>(null);
  const [downloadedIds, setDownloadedIds] = useState<Set<number>>(new Set());

  const playTrack = usePlayerStore((s) => s.play);
  const role = useAuthStore((s) => s.role);
  const isAdmin = role === 'ADMIN';
  const toast = useToastStore((s) => s.show);

  const load = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const [result, countResult, licensesResult] = await Promise.all([
        fetchDownloadQueue(),
        isAdmin ? Promise.resolve(null) : fetchDownloadCount().catch(() => null),
        fetchMyLicenses(1, 9999).catch(() => null),
      ]);
      setItems(result.dataList ?? []);
      setDlCount(countResult);
      if (licensesResult) {
        setDownloadedIds(new Set(licensesResult.dataList.map((l) => l.track.id)));
      }
    } catch {
      setError('다운로드 대기열을 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  }, [isAdmin]);

  useEffect(() => {
    load();
  }, [load]);

  async function handleRemove(trackId: number) {
    try {
      await removeFromDownloadQueue(trackId);
      setItems((prev) => prev.filter((i) => i.trackId !== trackId));
    } catch {
      setError('삭제에 실패했습니다.');
    }
  }

  async function handleDownload(item: QueueListItem) {
    try {
      setDownloading(item.trackId);
      const blob = await downloadTrack(item.trackId);
      triggerBlobDownload(blob, `${item.title}.mp3`);
      // Mark as downloaded (keep in queue)
      setDownloadedIds((prev) => new Set(prev).add(item.trackId));
      // Refresh download count
      if (!isAdmin) {
        fetchDownloadCount().then(setDlCount).catch(() => {});
      }
      toast('success', `${item.title} 다운로드 완료`);
    } catch (err: unknown) {
      const msg =
        err instanceof Error ? err.message : '다운로드에 실패했습니다.';
      setError(msg);
    } finally {
      setDownloading(null);
    }
  }

  async function handleDownloadAll() {
    for (const item of items) {
      if (!downloadedIds.has(item.trackId)) {
        await handleDownload(item);
      }
    }
  }

  function handlePlay(item: QueueListItem) {
    playTrack({
      id: item.trackId,
      title: item.title,
      artistName: '',
      duration: 0,
      bpm: item.bpm,
      tonality: item.tonality,
      description: null,
      audioFile: `/api/tracks/${item.trackId}/stream`,
      thumbnail: toUploadUrl(item.thumbnail),
      tags: [],
      isActive: true,
      playCount: 0,
      createdAt: item.createdAt,
      updatedAt: item.createdAt,
    });
  }

  const pendingCount = items.filter((i) => !downloadedIds.has(i.trackId)).length;

  return (
    <div className={styles.page}>
      <div className={styles.pageHeader}>
        <h1 className={styles.pageTitle}>
          {'다운로드 대기열'}
          <span className={styles.count}>{items.length}곡</span>
        </h1>
        {pendingCount > 0 && (
          <button className={styles.btnDownloadAll} onClick={handleDownloadAll}>
            {'미다운로드 전체 받기 ('}
            {pendingCount}
            {'곡)'}
          </button>
        )}
      </div>

      {/* Download limit info */}
      <div className={styles.limitInfo}>
        {isAdmin ? (
          <span className={styles.limitBadge}>{'무제한 다운로드'}</span>
        ) : dlCount ? (
          <span className={styles.limitBadge}>
            {'오늘 '}
            {dlCount.todayDownloads}
            {' / '}
            {dlCount.dailyLimit === -1 ? '무제한' : `${dlCount.dailyLimit}곡`}
            {dlCount.dailyLimit !== -1 && ` (남은 횟수: ${dlCount.remaining})`}
          </span>
        ) : null}
      </div>

      {loading ? (
        <div className={styles.loading}>{'불러오는 중...'}</div>
      ) : error ? (
        <div className={styles.error}>{error}</div>
      ) : items.length === 0 ? (
        <div className={styles.empty}>
          <p>{'대기열이 비어 있습니다.'}</p>
          <Link to="/tracks" className={styles.emptyLink}>
            {'음원 둘러보기'}
          </Link>
        </div>
      ) : (
        <div className={styles.tableWrap}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th className={styles.thNum}>#</th>
                <th>{'음원'}</th>
                <th className={`${styles.thRight} ${styles.thBpm}`}>BPM</th>
                <th className={`${styles.thCenter} ${styles.thKey}`}>Key</th>
                <th className={`${styles.thCenter} ${styles.thStatus}`}>{'상태'}</th>
                <th className={styles.thActs}>{'관리'}</th>
              </tr>
            </thead>
            <tbody>
              {items.map((item, idx) => {
                const isDone = downloadedIds.has(item.trackId);
                return (
                  <tr key={item.trackId} className={`${styles.row} ${isDone ? styles.rowDone : ''}`}>
                    <td className={styles.cellNum}>{idx + 1}</td>
                    <td className={styles.cellInfo}>
                      <div className={styles.info}>
                        <div
                          className={styles.thumb}
                          onClick={() => handlePlay(item)}
                        >
                          {item.thumbnail ? (
                            <img
                              src={toUploadUrl(item.thumbnail)!}
                              alt={item.title}
                            />
                          ) : (
                            '\u266A'
                          )}
                        </div>
                        <div className={styles.infoText}>
                          <Link
                            to={`/tracks/${item.trackId}`}
                            className={styles.titleLink}
                          >
                            {item.title}
                          </Link>
                        </div>
                      </div>
                    </td>
                    <td className={styles.cellBpm}>{item.bpm ?? '-'}</td>
                    <td className={styles.cellKey}>{item.tonality ?? '-'}</td>
                    <td className={styles.cellStatus}>
                      {isDone ? (
                        <span className={styles.badgeDone} title="다운로드 완료">{'✅ 완료'}</span>
                      ) : (
                        <span className={styles.badgePending}>{'대기 중'}</span>
                      )}
                    </td>
                    <td className={styles.cellActs}>
                      <button
                        className={styles.dlBtn}
                        onClick={() => handleDownload(item)}
                        disabled={downloading === item.trackId}
                        title={isDone ? '다시 다운로드 (카운트 차감 없음)' : '다운로드'}
                      >
                        {downloading === item.trackId ? '...' : isDone ? '\u{1F504}' : '\u2B07'}
                      </button>
                      <button
                        className={styles.removeBtn}
                        onClick={() => handleRemove(item.trackId)}
                        title="삭제"
                      >
                        {'\u2715'}
                      </button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
