/** Screen D-1: Liked tracks */
import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { fetchLikes, removeLike } from '@/api/likes';
import { usePlayerStore } from '@/store/playerStore';
import { useLikeStore } from '@/store/likeStore';
import AddToPlaylistModal from '@/components/playlist/AddToPlaylistModal';
import type { LikeItem } from '@/types';
import styles from './LikeListPage.module.css';

function formatDate(iso: string): string {
  return iso.substring(0, 10);
}

export default function LikeListPage() {
  const [items, setItems] = useState<LikeItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [addToPlTrackId, setAddToPlTrackId] = useState<number | null>(null);
  const currentTrack = usePlayerStore((s) => s.currentTrack);
  const playTrack = usePlayerStore((s) => s.play);
  const likeStore = useLikeStore();

  const load = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const res = await fetchLikes();
      setItems(res.dataList ?? []);
    } catch {
      setError('좋아요 목록을 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  async function handleUnlike(trackId: number) {
    try {
      await removeLike(trackId);
      setItems((prev) => prev.filter((it) => it.trackId !== trackId));
      // Sync global likeStore
      likeStore.likedIds.delete(trackId);
    } catch {
      /* ignore */
    }
  }

  function handlePlay(item: LikeItem) {
    playTrack({
      id: item.trackId,
      title: item.title,
      artistName: '',
      duration: 0,
      bpm: item.bpm,
      tonality: item.tonality,
      description: null,
      audioFile: null,
      thumbnail: item.thumbnail,
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
        <div className={styles.pageTitle}>
          {'좋아요'}
          <span className={styles.pageTitleCount}>
            {`${items.length}곡`}
          </span>
        </div>
      </div>

      {loading ? (
        <div className={styles.loading}>Loading...</div>
      ) : error ? (
        <div className={styles.error}>{error}</div>
      ) : items.length === 0 ? (
        <div className={styles.empty}>
          {'좋아요한 음원이 없습니다.'}
          <br />
          <Link to="/tracks" className={styles.emptyLink}>
            {'음원 둘러보기'}
          </Link>
        </div>
      ) : (
        <table className={styles.trackTable}>
          <thead>
            <tr>
              <th className={styles.thCenter}>#</th>
              <th>{'음원'}</th>
              <th className={styles.thRight}>BPM</th>
              <th className={styles.thCenter}>Key</th>
              <th className={styles.thRight}>{'추가일'}</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {items.map((item, idx) => (
              <tr
                key={item.trackId}
                className={`${styles.row} ${currentTrack?.id === item.trackId ? styles.rowPlaying : ''}`}
              >
                <td className={styles.cellNum}>
                  <span className={styles.num}>{idx + 1}</span>
                  <button
                    className={styles.playBtn}
                    onClick={() => handlePlay(item)}
                    aria-label="Play"
                  >
                    &#9654;
                  </button>
                </td>
                <td className={styles.cellInfo}>
                  <div className={styles.info}>
                    <div className={styles.thumb}>
                      {item.thumbnail ? (
                        <img src={item.thumbnail} alt={item.title} />
                      ) : (
                        '\u266A'
                      )}
                    </div>
                    <div>
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
                <td className={styles.cellDate}>
                  {formatDate(item.createdAt)}
                </td>
                <td className={styles.cellActions}>
                  <button
                    className={styles.addPlBtn}
                    onClick={() => setAddToPlTrackId(item.trackId)}
                    title="재생목록에 추가"
                  >
                    +
                  </button>
                  <button
                    className={styles.unlikeBtn}
                    onClick={() => handleUnlike(item.trackId)}
                    title="좋아요 해제"
                  >
                    {'\u2665'}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
      <AddToPlaylistModal
        open={addToPlTrackId !== null}
        trackId={addToPlTrackId}
        onClose={() => setAddToPlTrackId(null)}
      />
    </div>
  );
}
