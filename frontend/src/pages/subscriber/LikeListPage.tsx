/** Screen D-1: Liked tracks + albums (tabbed) */
import { useState, useEffect, useLayoutEffect, useMemo, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { fetchLikes, removeLike, fetchAlbumLikes, removeAlbumLike } from '@/api/likes';
import { classifyLoadError } from '@/api/loadError';
import { downloadTrack, triggerBlobDownload } from '@/api/downloads';
import { toUploadUrl } from '@/api/client';
import { formatDate } from '@/utils/format';
import { usePlayerStore } from '@/store/playerStore';
import { useLikeStore } from '@/store/likeStore';
import { useAlbumLikeStore } from '@/store/albumLikeStore';
import { useToastStore } from '@/store/toastStore';
import { useAuthStore } from '@/store/authStore';
import AddToPlaylistModal from '@/components/playlist/AddToPlaylistModal';
import type { LikeItem, AlbumLikeItem } from '@/types';
import { toPlayableTrack } from '@/utils/playableTrack';
import { createOwnerKey, createReadKey, getCurrentOwnerKey } from '@/utils/ownerProjection';
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
  const setTrackListContext = usePlayerStore((s) => s.setTrackListContext);
  const likeStore = useLikeStore();
  const albumLikeStore = useAlbumLikeStore();
  const toast = useToastStore((s) => s.show);
  const userID = useAuthStore((s) => s.user?.id ?? null);
  const accessToken = useAuthStore((s) => s.accessToken);
  const navigate = useNavigate();
  const requestGeneration = useRef(0);
  const ownerKey = createOwnerKey(userID, accessToken);
  const readKey = createReadKey(ownerKey, 'like-list', tab);
  const currentReadKeyRef = useRef(readKey);
  const projectionKeyRef = useRef<string | null>(null);
  const [projectionKey, setProjectionKey] = useState<string | null>(null);
  const [errorKey, setErrorKey] = useState<string | null>(null);
  currentReadKeyRef.current = readKey;
  const projectionCurrent = readKey !== null && projectionKey === readKey;
  const currentItems = useMemo(
    () => (projectionCurrent && tab === 'tracks' ? items : []),
    [items, projectionCurrent, tab],
  );
  const currentAlbumItems = useMemo(
    () => (projectionCurrent && tab === 'albums' ? albumItems : []),
    [albumItems, projectionCurrent, tab],
  );
  const currentError = errorKey === readKey ? (tab === 'tracks' ? error : albumError) : null;
  const currentLoading =
    (tab === 'tracks' ? loading : albumLoading) || (!projectionCurrent && currentError === null);

  function isCurrentProjection(expectedReadKey = readKey): boolean {
    return (
      expectedReadKey !== null &&
      currentReadKeyRef.current === expectedReadKey &&
      projectionKeyRef.current === expectedReadKey &&
      getCurrentOwnerKey(ownerKey) === ownerKey
    );
  }

  useEffect(() => {
    const requestKey = readKey;
    const requestOwnerKey = ownerKey;
    const generation = ++requestGeneration.current;
    const controller = new AbortController();
    const isCurrent = () =>
      requestKey !== null &&
      generation === requestGeneration.current &&
      currentReadKeyRef.current === requestKey &&
      getCurrentOwnerKey(requestOwnerKey) === requestOwnerKey;

    if (tab === 'tracks') {
      setItems([]);
      setLoading(true);
      setError(null);
      void fetchLikes(controller.signal)
        .then((res) => {
          if (isCurrent()) {
            setItems(res.dataList ?? []);
            projectionKeyRef.current = requestKey;
            setProjectionKey(requestKey);
          }
        })
        .catch((loadError: unknown) => {
          if (isCurrent() && classifyLoadError(loadError) !== 'cancelled') {
            setError('좋아요 목록을 불러오지 못했습니다.');
            setErrorKey(requestKey);
          }
        })
        .finally(() => {
          if (isCurrent()) setLoading(false);
        });
    } else {
      setAlbumItems([]);
      setAlbumLoading(true);
      setAlbumError(null);
      void fetchAlbumLikes(controller.signal)
        .then((res) => {
          if (isCurrent()) {
            setAlbumItems(res.dataList ?? []);
            projectionKeyRef.current = requestKey;
            setProjectionKey(requestKey);
          }
        })
        .catch((loadError: unknown) => {
          if (isCurrent() && classifyLoadError(loadError) !== 'cancelled') {
            setAlbumError('좋아요 앨범을 불러오지 못했습니다.');
            setErrorKey(requestKey);
          }
        })
        .finally(() => {
          if (isCurrent()) setAlbumLoading(false);
        });
    }

    return () => {
      controller.abort();
      if (requestGeneration.current === generation) requestGeneration.current += 1;
    };
  }, [ownerKey, readKey, tab]);

  /* SR-83: Publish liked tracks as player context so Next/Prev traverses them. */
  useLayoutEffect(() => {
    if (tab !== 'tracks') return;
    const tracks = currentItems.map((item) => toPlayableTrack(item));
    return setTrackListContext(tracks);
  }, [tab, currentItems, setTrackListContext]);

  /* ── Track handlers ── */
  async function handleUnlike(trackId: number) {
    const operationKey = readKey;
    if (
      !isCurrentProjection(operationKey) ||
      !currentItems.some((item) => item.trackId === trackId)
    ) {
      return;
    }
    try {
      await removeLike(trackId);
      if (!isCurrentProjection(operationKey)) return;
      setItems((prev) => prev.filter((it) => it.trackId !== trackId));
      likeStore.remove(trackId);
    } catch {
      /* ignore */
    }
  }

  async function handleDownload(item: LikeItem) {
    const operationKey = readKey;
    if (
      !isCurrentProjection(operationKey) ||
      !currentItems.some((candidate) => candidate.trackId === item.trackId)
    ) {
      return;
    }
    try {
      const blob = await downloadTrack(item.trackId);
      if (!isCurrentProjection(operationKey)) return;
      triggerBlobDownload(blob, `${item.title}.mp3`);
    } catch {
      if (!isCurrentProjection(operationKey)) return;
      toast('error', '다운로드에 실패했습니다.');
    }
  }

  function handlePlay(item: LikeItem) {
    if (
      !isCurrentProjection() ||
      !currentItems.some((candidate) => candidate.trackId === item.trackId)
    ) {
      return;
    }
    if (currentTrack?.id === item.trackId) {
      if (isPlayerPlaying) pauseTrack();
      else resumeTrack();
      return;
    }
    playTrack(toPlayableTrack(item));
  }

  /* ── Album handlers ── */
  async function handleUnlikeAlbum(albumId: number) {
    const operationKey = readKey;
    if (
      !isCurrentProjection(operationKey) ||
      !currentAlbumItems.some((item) => item.albumId === albumId)
    ) {
      return;
    }
    try {
      await removeAlbumLike(albumId);
      if (!isCurrentProjection(operationKey)) return;
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
            {tab === 'tracks' ? `${currentItems.length}곡` : `${currentAlbumItems.length}앨범`}
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
          {currentLoading ? (
            <div className={styles.loading}>Loading...</div>
          ) : currentError ? (
            <div className={styles.error}>{currentError}</div>
          ) : currentItems.length === 0 ? (
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
                  {currentItems.map((item, idx) => (
                    <tr
                      key={item.trackId}
                      className={`${styles.row} ${currentTrack?.id === item.trackId && isPlayerPlaying ? styles.rowPlaying : ''}`}
                    >
                      <td className={styles.cellNum}>
                        <span className={styles.num}>{idx + 1}</span>
                        <button
                          className={styles.playBtn}
                          onClick={() => handlePlay(item)}
                          aria-label={
                            currentTrack?.id === item.trackId && isPlayerPlaying ? 'Pause' : 'Play'
                          }
                        >
                          {currentTrack?.id === item.trackId && isPlayerPlaying
                            ? '\u23F8'
                            : '\u25B6'}
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
                            <Link to={`/tracks/${item.trackId}`} className={styles.titleLink}>
                              {item.title}
                            </Link>
                          </div>
                        </div>
                      </td>
                      <td className={styles.cellBpm}>{item.bpm ?? '-'}</td>
                      <td className={styles.cellKey}>{item.tonality ?? '-'}</td>
                      <td className={styles.cellDate}>{formatDate(item.createdAt)}</td>
                      <td className={styles.cellActions}>
                        <span className={styles.hoverActions}>
                          <button
                            className={styles.addPlBtn}
                            onClick={() => {
                              if (isCurrentProjection()) setAddToPlTrackId(item.trackId);
                            }}
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
            open={projectionCurrent && addToPlTrackId !== null}
            trackId={projectionCurrent ? addToPlTrackId : null}
            onClose={() => setAddToPlTrackId(null)}
          />
        </>
      )}

      {/* ── Album tab ── */}
      {tab === 'albums' && (
        <>
          {currentLoading ? (
            <div className={styles.loading}>Loading...</div>
          ) : currentError ? (
            <div className={styles.error}>{currentError}</div>
          ) : currentAlbumItems.length === 0 ? (
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
                  {currentAlbumItems.map((item) => (
                    <tr
                      key={item.albumId}
                      className={styles.row}
                      onClick={() => {
                        if (isCurrentProjection()) navigate(`/albums/${item.albumId}`);
                      }}
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
                      <td className={styles.cellDate}>{formatDate(item.createdAt)}</td>
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
