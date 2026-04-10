/** Screen D-1: Liked tracks + albums (tabbed) */
import { useState, useEffect, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { fetchLikes, removeLike, fetchAlbumLikes, removeAlbumLike } from '@/api/likes';
import { downloadTrack, triggerBlobDownload } from '@/api/downloads';
import { toUploadUrl } from '@/api/client';
import { formatDate } from '@/utils/format';
import { usePlayerStore } from '@/store/playerStore';
import { useLikeStore } from '@/store/likeStore';
import { useAlbumLikeStore } from '@/store/albumLikeStore';
import { useToastStore } from '@/store/toastStore';
import AddToPlaylistModal from '@/components/playlist/AddToPlaylistModal';
import type { LikeItem, AlbumLikeItem } from '@/types';
import styles from './LikeListPage.module.css';

type TabKey = 'tracks' | 'albums';

export default function LikeListPage() {
  const [tab, setTab] = useState<TabKey>('tracks');

  /* ── Track state ── */
  const [items, setItems] = useState<LikeItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  /* ── Album state ── */
  const [albumItems, setAlbumItems] = useState<AlbumLikeItem[]>([]);
  const [albumLoading, setAlbumLoading] = useState(false);
  const [albumError, setAlbumError] = useState<string | null>(null);

  const [addToPlTrackId, setAddToPlTrackId] = useState<number | null>(null);
  const currentTrack = usePlayerStore((s) => s.currentTrack);
  const isPlayerPlaying = usePlayerStore((s) => s.isPlaying);
  const playTrack = usePlayerStore((s) => s.play);
  const pauseTrack = usePlayerStore((s) => s.pause);
  const resumeTrack = usePlayerStore((s) => s.resume);
  const likeStore = useLikeStore();
  const albumLikeStore = useAlbumLikeStore();
  const toast = useToastStore((s) => s.show);
  const navigate = useNavigate();

  /* ── Load tracks ── */
  const loadTracks = useCallback(async () => {
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

  /* ── Load albums ── */
  const loadAlbums = useCallback(async () => {
    try {
      setAlbumLoading(true);
      setAlbumError(null);
      const res = await fetchAlbumLikes();
      setAlbumItems(res.dataList ?? []);
    } catch {
      setAlbumError('좋아요 앨범을 불러오지 못했습니다.');
    } finally {
      setAlbumLoading(false);
    }
  }, []);

  useEffect(() => {
    if (tab === 'tracks') loadTracks();
    else loadAlbums();
  }, [tab, loadTracks, loadAlbums]);

  /* ── Track handlers ── */
  async function handleUnlike(trackId: number) {
    try {
      await removeLike(trackId);
      setItems((prev) => prev.filter((it) => it.trackId !== trackId));
      likeStore.remove(trackId);
    } catch {
      /* ignore */
    }
  }

  async function handleDownload(item: LikeItem) {
    try {
      const blob = await downloadTrack(item.trackId);
      triggerBlobDownload(blob, `${item.title}.mp3`);
    } catch {
      toast('error', '다운로드에 실패했습니다.');
    }
  }

  function handlePlay(item: LikeItem) {
    if (currentTrack?.id === item.trackId) {
      if (isPlayerPlaying) pauseTrack();
      else resumeTrack();
      return;
    }
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
      likeCount: 0,
      downloadCount: 0,
      createdAt: item.createdAt,
      updatedAt: item.createdAt,
    });
  }

  /* ── Album handlers ── */
  async function handleUnlikeAlbum(albumId: number) {
    try {
      await removeAlbumLike(albumId);
      setAlbumItems((prev) => prev.filter((it) => it.albumId !== albumId));
      albumLikeStore.remove(albumId);
    } catch {
      /* ignore */
    }
  }

  return (
    <div className={styles.page}>
      <div className={styles.pageHeader}>
        <div className={styles.pageTitle}>
          {'좋아요'}
          <span className={styles.pageTitleCount}>
            {tab === 'tracks' ? `${items.length}곡` : `${albumItems.length}앨범`}
          </span>
        </div>
      </div>

      {/* ── Tabs ── */}
      <div className={styles.tabs}>
        <button
          className={`${styles.tabBtn} ${tab === 'tracks' ? styles.tabBtnActive : ''}`}
          onClick={() => setTab('tracks')}
        >
          {'음원'}
        </button>
        <button
          className={`${styles.tabBtn} ${tab === 'albums' ? styles.tabBtnActive : ''}`}
          onClick={() => setTab('albums')}
        >
          {'앨범'}
        </button>
      </div>

      {/* ── Track tab ── */}
      {tab === 'tracks' && (
        <>
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
            <div className={styles.tableWrap}>
            <table className={styles.trackTable}>
              <thead>
                <tr>
                  <th className={styles.thCenter}>#</th>
                  <th>{'음원'}</th>
                  <th className={`${styles.thRight} ${styles.cellBpm}`}>BPM</th>
                  <th className={`${styles.thCenter} ${styles.cellKey}`}>Key</th>
                  <th className={`${styles.thRight} ${styles.cellDate}`}>{'추가일'}</th>
                  <th className={styles.cellActions}>{'관리'}</th>
                </tr>
              </thead>
              <tbody>
                {items.map((item, idx) => (
                  <tr
                    key={item.trackId}
                    className={`${styles.row} ${currentTrack?.id === item.trackId && isPlayerPlaying ? styles.rowPlaying : ''}`}
                  >
                    <td className={styles.cellNum}>
                      <span className={styles.num}>{idx + 1}</span>
                      <button
                        className={styles.playBtn}
                        onClick={() => handlePlay(item)}
                        aria-label={currentTrack?.id === item.trackId && isPlayerPlaying ? 'Pause' : 'Play'}
                      >
                        {currentTrack?.id === item.trackId && isPlayerPlaying ? '\u23F8' : '\u25B6'}
                      </button>
                    </td>
                    <td className={styles.cellInfo}>
                      <div className={styles.info}>
                        <div className={styles.thumb}>
                          {item.thumbnail ? (
                            <img src={toUploadUrl(item.thumbnail)!} alt={item.title} />
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
                    <td className={styles.cellDate}>
                      {formatDate(item.createdAt)}
                    </td>
                    <td className={styles.cellActions}>
                      <span className={styles.hoverActions}>
                        <button
                          className={styles.addPlBtn}
                          onClick={() => setAddToPlTrackId(item.trackId)}
                          title="재생목록에 추가"
                        >
                          +
                        </button>
                        <button
                          className={styles.dlBtn}
                          onClick={() => handleDownload(item)}
                          title="다운로드"
                        >
                          {'\u2193'}
                        </button>
                      </span>
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
            </div>
          )}
          <AddToPlaylistModal
            open={addToPlTrackId !== null}
            trackId={addToPlTrackId}
            onClose={() => setAddToPlTrackId(null)}
          />
        </>
      )}

      {/* ── Album tab ── */}
      {tab === 'albums' && (
        <>
          {albumLoading ? (
            <div className={styles.loading}>Loading...</div>
          ) : albumError ? (
            <div className={styles.error}>{albumError}</div>
          ) : albumItems.length === 0 ? (
            <div className={styles.empty}>
              {'좋아요한 앨범이 없습니다.'}
              <br />
              <Link to="/albums" className={styles.emptyLink}>
                {'앨범 둘러보기'}
              </Link>
            </div>
          ) : (
            <div className={styles.tableWrap}>
            <table className={styles.trackTable}>
              <thead>
                <tr>
                  <th>{'앨범'}</th>
                  <th className={`${styles.thRight} ${styles.cellCount}`}>{'곡 수'}</th>
                  <th className={`${styles.thRight} ${styles.cellDate}`}>{'추가일'}</th>
                  <th className={styles.cellActions}>{'관리'}</th>
                </tr>
              </thead>
              <tbody>
                {albumItems.map((item) => (
                  <tr
                    key={item.albumId}
                    className={styles.row}
                    onClick={() => navigate(`/albums/${item.albumId}`)}
                  >
                    <td className={styles.cellInfo}>
                      <div className={styles.info}>
                        <div className={styles.thumb}>
                          {item.thumbnailUrl ? (
                            <img src={toUploadUrl(item.thumbnailUrl)!} alt={item.title} />
                          ) : (
                            '\u266A'
                          )}
                        </div>
                        <div className={styles.infoText}>
                          <span className={styles.titleLink}>{item.title}</span>
                        </div>
                      </div>
                    </td>
                    <td className={styles.cellCount}>{`${item.trackCount}곡`}</td>
                    <td className={styles.cellDate}>
                      {formatDate(item.createdAt)}
                    </td>
                    <td className={styles.cellActions}>
                      <button
                        className={styles.unlikeBtn}
                        onClick={(e) => {
                          e.stopPropagation();
                          handleUnlikeAlbum(item.albumId);
                        }}
                        title="좋아요 해제"
                      >
                        {'\u2665'}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            </div>
          )}
        </>
      )}
    </div>
  );
}
