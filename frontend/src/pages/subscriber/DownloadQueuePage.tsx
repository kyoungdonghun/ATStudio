/** Screen 11: Download queue (cart) */
import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import {
  fetchDownloadQueue,
  removeFromDownloadQueue,
  type QueueListItem,
} from '@/api/downloadQueue';
import { downloadTrack, triggerBlobDownload } from '@/api/downloads';
import { toUploadUrl } from '@/api/client';
import { usePlayerStore } from '@/store/playerStore';
import styles from './DownloadQueuePage.module.css';

export default function DownloadQueuePage() {
  const [items, setItems] = useState<QueueListItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [downloading, setDownloading] = useState<number | null>(null);

  const playTrack = usePlayerStore((s) => s.play);

  const load = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const result = await fetchDownloadQueue();
      setItems(result.dataList ?? []);
    } catch {
      setError('다운로드 대기열을 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  }, []);

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
      // 다운로드 성공 시 대기열에서 제거
      await removeFromDownloadQueue(item.trackId);
      setItems((prev) => prev.filter((i) => i.trackId !== item.trackId));
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
      await handleDownload(item);
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

  return (
    <div className={styles.page}>
      <div className={styles.pageHeader}>
        <h1 className={styles.pageTitle}>
          {'다운로드 대기열'}
          <span className={styles.count}>{items.length}곡</span>
        </h1>
        {items.length > 0 && (
          <button className={styles.btnDownloadAll} onClick={handleDownloadAll}>
            {'전체 다운로드'}
          </button>
        )}
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
                <th className={styles.thActs}>{'관리'}</th>
              </tr>
            </thead>
            <tbody>
              {items.map((item, idx) => (
                <tr key={item.trackId} className={styles.row}>
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
                  <td className={styles.cellActs}>
                    <button
                      className={styles.dlBtn}
                      onClick={() => handleDownload(item)}
                      disabled={downloading === item.trackId}
                      title="다운로드"
                    >
                      {downloading === item.trackId ? '...' : '\u2B07'}
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
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
