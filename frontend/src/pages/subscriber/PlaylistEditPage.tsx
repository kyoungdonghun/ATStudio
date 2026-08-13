/** Screen 9: Playlist edit */
import { useState, useEffect, useCallback, useRef } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import {
  fetchPlaylistDetail,
  updatePlaylist,
  deletePlaylist,
  removeTrackFromPlaylist,
  reorderTracks,
  type PlaylistDetail,
  type PlaylistTrack,
} from '@/api/playlists';
import { toUploadUrl } from '@/api/client';
import { classifyLoadError } from '@/api/loadError';
import { useAuthStore } from '@/store/authStore';
import { TITLE_PLAYLIST_MAX } from '@/utils/validation';
import { parsePositiveDecimalRouteID } from '@/utils/routeId';
import { createOwnerKey, createReadKey, getCurrentOwnerKey } from '@/utils/ownerProjection';
import Button from '@/components/ui/Button';
import Modal from '@/components/ui/Modal';
import styles from './PlaylistEditPage.module.css';

export default function PlaylistEditPage() {
  const { playlistId } = useParams<{ playlistId: string }>();
  const navigate = useNavigate();
  const id = parsePositiveDecimalRouteID(playlistId);
  const validID = id !== null;
  const userID = useAuthStore((s) => s.user?.id ?? null);
  const accessToken = useAuthStore((s) => s.accessToken);

  /* ── State ── */
  const [detail, setDetail] = useState<PlaylistDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [thumbFile, setThumbFile] = useState<File | null>(null);
  const [thumbPreview, setThumbPreview] = useState<string | null>(null);
  const [tracks, setTracks] = useState<PlaylistTrack[]>([]);
  const [saving, setSaving] = useState(false);
  const requestGeneration = useRef(0);
  const requestController = useRef<AbortController | null>(null);
  const ownerKey = createOwnerKey(userID, accessToken);
  const readKey = createReadKey(ownerKey, 'playlist-edit', id);
  const currentReadKeyRef = useRef(readKey);
  const projectionKeyRef = useRef<string | null>(null);
  const [projectionKey, setProjectionKey] = useState<string | null>(null);
  const [errorKey, setErrorKey] = useState<string | null>(null);
  currentReadKeyRef.current = readKey;

  /* Delete confirm */
  const [showDeletePl, setShowDeletePl] = useState(false);
  const [deletePlaylistKey, setDeletePlaylistKey] = useState<string | null>(null);
  const [deletingPl, setDeletingPl] = useState(false);

  /* Remove track confirm */
  const [removeTarget, setRemoveTarget] = useState<PlaylistTrack | null>(null);
  const [removeTargetKey, setRemoveTargetKey] = useState<string | null>(null);
  const [removing, setRemoving] = useState(false);

  /* Dirty check */
  const projectionCurrent = readKey !== null && projectionKey === readKey;
  const currentDetail = projectionCurrent ? detail : null;
  const currentTracks = projectionCurrent ? tracks : [];
  const currentError = errorKey === readKey ? error : null;
  const currentLoading = loading || (!projectionCurrent && currentError === null);
  const currentRemoveTarget = removeTargetKey === readKey ? removeTarget : null;
  const showCurrentDeletePlaylist = showDeletePl && deletePlaylistKey === readKey;

  function isCurrentProjection(expectedReadKey = readKey): boolean {
    return (
      expectedReadKey !== null &&
      currentReadKeyRef.current === expectedReadKey &&
      projectionKeyRef.current === expectedReadKey &&
      getCurrentOwnerKey(ownerKey) === ownerKey
    );
  }

  const isDirty =
    currentDetail !== null &&
    (title !== currentDetail.title ||
      description !== (currentDetail.description ?? '') ||
      thumbFile !== null ||
      JSON.stringify(currentTracks.map((t) => t.trackId)) !==
        JSON.stringify(currentDetail.tracks.map((t) => t.trackId)));

  /* ── Fetch ── */
  const load = useCallback(async () => {
    const requestKey = readKey;
    const requestOwnerKey = ownerKey;
    if (id === null || requestKey === null) return;
    requestController.current?.abort();
    const controller = new AbortController();
    requestController.current = controller;
    const generation = ++requestGeneration.current;
    const isCurrent = () =>
      generation === requestGeneration.current &&
      currentReadKeyRef.current === requestKey &&
      getCurrentOwnerKey(requestOwnerKey) === requestOwnerKey;
    try {
      setLoading(true);
      setError(null);
      setDetail(null);
      setTitle('');
      setDescription('');
      setThumbFile(null);
      setThumbPreview(null);
      setTracks([]);
      const data = await fetchPlaylistDetail(id, controller.signal);
      if (!isCurrent()) return;
      setDetail(data);
      setTitle(data.title);
      setDescription(data.description ?? '');
      setThumbFile(null);
      setThumbPreview(toUploadUrl(data.thumbnail));
      setTracks([...data.tracks]);
      projectionKeyRef.current = requestKey;
      setProjectionKey(requestKey);
    } catch (loadError) {
      if (isCurrent() && classifyLoadError(loadError) !== 'cancelled') {
        setError('재생목록을 불러오지 못했습니다.');
        setErrorKey(requestKey);
      }
    } finally {
      if (isCurrent()) setLoading(false);
    }
  }, [id, ownerKey, readKey]);

  useEffect(() => {
    if (!validID) {
      requestController.current?.abort();
      requestGeneration.current += 1;
      setDetail(null);
      setLoading(false);
      setError(null);
      return;
    }
    void load();
    return () => {
      requestController.current?.abort();
      requestGeneration.current += 1;
    };
  }, [accessToken, load, userID, validID]);

  /* ── Handlers ── */

  function handleThumbChange(e: React.ChangeEvent<HTMLInputElement>) {
    if (!isCurrentProjection()) return;
    const file = e.target.files?.[0] ?? null;
    setThumbFile(file);
    if (file) {
      setThumbPreview(URL.createObjectURL(file));
    } else {
      setThumbPreview(toUploadUrl(currentDetail?.thumbnail));
    }
  }

  function moveTrack(index: number, direction: -1 | 1) {
    if (!isCurrentProjection()) return;
    const target = index + direction;
    if (target < 0 || target >= currentTracks.length) return;
    const next = [...currentTracks];
    [next[index], next[target]] = [next[target], next[index]];
    setTracks(next);
  }

  async function handleSave() {
    const operationKey = readKey;
    if (!currentDetail || id === null || !isCurrentProjection(operationKey)) return;
    try {
      setSaving(true);
      setError(null);

      /* Update title / description / thumbnail */
      const infoChanged =
        title !== currentDetail.title ||
        description !== (currentDetail.description ?? '') ||
        thumbFile !== null;

      if (infoChanged) {
        await updatePlaylist(id, {
          title: title.trim(),
          description: description.trim() || undefined,
          thumbnail: thumbFile ?? undefined,
        });
        if (!isCurrentProjection(operationKey)) return;
      }

      /* Update track order if changed */
      const orderChanged =
        JSON.stringify(currentTracks.map((t) => t.trackId)) !==
        JSON.stringify(currentDetail.tracks.map((t) => t.trackId));

      if (orderChanged && currentTracks.length > 0) {
        await reorderTracks(
          id,
          currentTracks.map((t, i) => ({ trackId: t.trackId, trackOrder: i })),
        );
        if (!isCurrentProjection(operationKey)) return;
      }

      navigate(`/playlists/${id}`);
    } catch {
      if (!isCurrentProjection(operationKey)) return;
      setError('저장에 실패했습니다.');
      setErrorKey(operationKey);
    } finally {
      if (isCurrentProjection(operationKey)) setSaving(false);
    }
  }

  async function handleRemoveTrack() {
    const operationKey = readKey;
    if (
      !currentRemoveTarget ||
      id === null ||
      !isCurrentProjection(operationKey) ||
      !currentTracks.some((track) => track.trackId === currentRemoveTarget.trackId)
    ) {
      return;
    }
    try {
      setRemoving(true);
      await removeTrackFromPlaylist(id, currentRemoveTarget.trackId);
      if (!isCurrentProjection(operationKey)) return;
      setRemoveTarget(null);
      setRemoveTargetKey(null);
      await load();
    } catch {
      if (!isCurrentProjection(operationKey)) return;
      setError('곡 삭제에 실패했습니다.');
      setErrorKey(operationKey);
    } finally {
      if (isCurrentProjection(operationKey)) setRemoving(false);
    }
  }

  async function handleDeletePlaylist() {
    const operationKey = readKey;
    if (id === null || deletePlaylistKey !== operationKey || !isCurrentProjection(operationKey)) {
      return;
    }
    try {
      setDeletingPl(true);
      await deletePlaylist(id);
      if (!isCurrentProjection(operationKey)) return;
      navigate('/playlists');
    } catch {
      if (!isCurrentProjection(operationKey)) return;
      setError('재생목록 삭제에 실패했습니다.');
      setErrorKey(operationKey);
      setDeletingPl(false);
    }
  }

  /* ── Render ── */
  if (id === null) {
    return (
      <div className={styles.page}>
        <div className={styles.statusError}>{'재생목록 주소가 올바르지 않습니다.'}</div>
        <Link to="/playlists" className={styles.backLink}>
          {'재생목록 목록으로'}
        </Link>
      </div>
    );
  }

  if (currentLoading) {
    return (
      <div className={styles.page}>
        <div className={styles.status}>{'불러오는 중...'}</div>
      </div>
    );
  }

  if (currentError && !currentDetail) {
    return (
      <div className={styles.page}>
        <div className={styles.statusError}>{currentError}</div>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      {/* Back link */}
      <Link to={`/playlists/${id}`} className={styles.backLink}>
        {'\u2190 재생목록으로 돌아가기'}
      </Link>

      {/* Page header */}
      <div className={styles.pageHeader}>
        <h1 className={styles.pageTitle}>{'재생목록 편집'}</h1>
        <div className={styles.headerActions}>
          <Button variant="ghost" size="sm" onClick={() => navigate(`/playlists/${id}`)}>
            {'취소'}
          </Button>
          <Button size="sm" onClick={handleSave} loading={saving} disabled={!isDirty && !saving}>
            {'저장'}
          </Button>
        </div>
      </div>

      {currentError && <div className={styles.errorBanner}>{currentError}</div>}

      {/* ── Info form ── */}
      <section className={styles.section}>
        <h2 className={styles.sectionTitle}>{'기본 정보'}</h2>
        <div className={styles.formGroup}>
          <label className={styles.formLabel}>{'이름'}</label>
          <input
            className={styles.formInput}
            type="text"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            maxLength={TITLE_PLAYLIST_MAX}
            placeholder="재생목록 이름"
          />
        </div>
        <div className={styles.formGroup}>
          <label className={styles.formLabel}>{'설명 (선택)'}</label>
          <textarea
            className={styles.formTextarea}
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            rows={3}
            placeholder="재생목록 설명"
          />
        </div>
        <div className={styles.formGroup}>
          <label className={styles.formLabel}>{'썸네일'}</label>
          <div className={styles.thumbUpload}>
            {thumbPreview ? (
              <div className={styles.thumbPreviewWrap}>
                <div className={styles.thumbPreview}>
                  <img src={thumbPreview} alt="Thumbnail" />
                </div>
                <label className={styles.thumbChangeBtn}>
                  {'변경'}
                  <input
                    type="file"
                    accept="image/*"
                    className={styles.thumbFileInput}
                    onChange={handleThumbChange}
                  />
                </label>
              </div>
            ) : (
              <label className={styles.thumbDropArea}>
                <span className={styles.thumbDropIcon}>{'\uD83D\uDDBC'}</span>
                <span className={styles.thumbDropText}>{'이미지 선택'}</span>
                <input
                  type="file"
                  accept="image/*"
                  className={styles.thumbFileInput}
                  onChange={handleThumbChange}
                />
              </label>
            )}
          </div>
        </div>
      </section>

      {/* ── Track list ── */}
      <section className={styles.section}>
        <h2 className={styles.sectionTitle}>
          {'수록곡'}
          <span className={styles.trackCount}>{currentTracks.length}곡</span>
        </h2>

        {currentTracks.length === 0 ? (
          <div className={styles.emptyTracks}>
            {'수록곡이 없습니다. 음원 목록에서 곡을 추가해보세요.'}
          </div>
        ) : (
          <div className={styles.trackList}>
            {currentTracks.map((track, idx) => (
              <div key={track.trackId} className={styles.trackItem}>
                <div className={styles.trackOrder}>{idx + 1}</div>
                <div className={styles.trackInfo}>
                  <div className={styles.trackTitle}>{track.title}</div>
                  <div className={styles.trackMeta}>
                    {track.bpm ? `${track.bpm} BPM` : ''}
                    {track.bpm && track.tonality ? ' \u00B7 ' : ''}
                    {track.tonality ?? ''}
                  </div>
                </div>
                <div className={styles.trackActions}>
                  <button
                    className={styles.orderBtn}
                    disabled={idx === 0}
                    onClick={() => moveTrack(idx, -1)}
                    aria-label="Move up"
                    title="위로"
                  >
                    {'\u25B2'}
                  </button>
                  <button
                    className={styles.orderBtn}
                    disabled={idx === currentTracks.length - 1}
                    onClick={() => moveTrack(idx, 1)}
                    aria-label="Move down"
                    title="아래로"
                  >
                    {'\u25BC'}
                  </button>
                  <button
                    className={styles.removeBtn}
                    onClick={() => {
                      if (isCurrentProjection()) {
                        setRemoveTarget(track);
                        setRemoveTargetKey(readKey);
                      }
                    }}
                    title="삭제"
                  >
                    {'\u2715'}
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </section>

      {/* ── Danger zone ── */}
      <section className={styles.dangerZone}>
        <div className={styles.dangerInfo}>
          <div className={styles.dangerTitle}>{'재생목록 삭제'}</div>
          <div className={styles.dangerDesc}>{'삭제된 재생목록은 복구할 수 없습니다.'}</div>
        </div>
        <Button
          variant="danger"
          size="sm"
          onClick={() => {
            if (isCurrentProjection()) {
              setShowDeletePl(true);
              setDeletePlaylistKey(readKey);
            }
          }}
        >
          {'삭제'}
        </Button>
      </section>

      {/* ── Remove Track Modal ── */}
      <Modal
        open={currentRemoveTarget !== null}
        onClose={() => {
          setRemoveTarget(null);
          setRemoveTargetKey(null);
        }}
        title="곡 삭제"
      >
        <div className={styles.modalBody}>
          <p>
            {'정말 '}
            <strong>{currentRemoveTarget?.title}</strong>
            {'을(를) 재생목록에서 삭제하시겠습니까?'}
          </p>
        </div>
        <div className={styles.modalFooter}>
          <Button
            variant="ghost"
            onClick={() => {
              setRemoveTarget(null);
              setRemoveTargetKey(null);
            }}
          >
            {'취소'}
          </Button>
          <Button variant="danger" onClick={handleRemoveTrack} loading={removing}>
            {'삭제'}
          </Button>
        </div>
      </Modal>

      {/* ── Delete Playlist Modal ── */}
      <Modal
        open={showCurrentDeletePlaylist}
        onClose={() => {
          setShowDeletePl(false);
          setDeletePlaylistKey(null);
        }}
        title="재생목록 삭제"
      >
        <div className={styles.modalBody}>
          <p>
            {'정말 '}
            <strong>{currentDetail?.title}</strong>
            {' 재생목록을 삭제하시겠습니까?'}
            <br />
            {'이 작업은 되돌릴 수 없습니다.'}
          </p>
        </div>
        <div className={styles.modalFooter}>
          <Button variant="ghost" onClick={() => setShowDeletePl(false)}>
            {'취소'}
          </Button>
          <Button variant="danger" onClick={handleDeletePlaylist} loading={deletingPl}>
            {'삭제'}
          </Button>
        </div>
      </Modal>
    </div>
  );
}
