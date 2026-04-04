/** Screen 9: Playlist edit */
import { useState, useEffect, useCallback } from 'react';
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
import { TITLE_PLAYLIST_MAX } from '@/utils/validation';
import Button from '@/components/ui/Button';
import Modal from '@/components/ui/Modal';
import styles from './PlaylistEditPage.module.css';

export default function PlaylistEditPage() {
  const { playlistId } = useParams<{ playlistId: string }>();
  const navigate = useNavigate();
  const id = Number(playlistId);

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

  /* Delete confirm */
  const [showDeletePl, setShowDeletePl] = useState(false);
  const [deletingPl, setDeletingPl] = useState(false);

  /* Remove track confirm */
  const [removeTarget, setRemoveTarget] = useState<PlaylistTrack | null>(null);
  const [removing, setRemoving] = useState(false);

  /* Dirty check */
  const isDirty =
    detail !== null &&
    (title !== detail.title ||
      description !== (detail.description ?? '') ||
      thumbFile !== null ||
      JSON.stringify(tracks.map((t) => t.trackId)) !==
        JSON.stringify(detail.tracks.map((t) => t.trackId)));

  /* ── Fetch ── */
  const load = useCallback(async () => {
    if (!id || isNaN(id)) return;
    try {
      setLoading(true);
      setError(null);
      const data = await fetchPlaylistDetail(id);
      setDetail(data);
      setTitle(data.title);
      setDescription(data.description ?? '');
      setThumbFile(null);
      setThumbPreview(toUploadUrl(data.thumbnail));
      setTracks([...data.tracks]);
    } catch {
      setError('재생목록을 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    load();
  }, [load]);

  /* ── Handlers ── */

  function handleThumbChange(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0] ?? null;
    setThumbFile(file);
    if (file) {
      setThumbPreview(URL.createObjectURL(file));
    } else {
      setThumbPreview(toUploadUrl(detail?.thumbnail));
    }
  }

  function moveTrack(index: number, direction: -1 | 1) {
    const target = index + direction;
    if (target < 0 || target >= tracks.length) return;
    const next = [...tracks];
    [next[index], next[target]] = [next[target], next[index]];
    setTracks(next);
  }

  async function handleSave() {
    if (!detail) return;
    try {
      setSaving(true);
      setError(null);

      /* Update title / description / thumbnail */
      const infoChanged =
        title !== detail.title ||
        description !== (detail.description ?? '') ||
        thumbFile !== null;

      if (infoChanged) {
        await updatePlaylist(id, {
          title: title.trim(),
          description: description.trim() || undefined,
          thumbnail: thumbFile ?? undefined,
        });
      }

      /* Update track order if changed */
      const orderChanged =
        JSON.stringify(tracks.map((t) => t.trackId)) !==
        JSON.stringify(detail.tracks.map((t) => t.trackId));

      if (orderChanged && tracks.length > 0) {
        await reorderTracks(
          id,
          tracks.map((t, i) => ({ trackId: t.trackId, trackOrder: i + 1 })),
        );
      }

      navigate(`/playlists/${id}`);
    } catch {
      setError('저장에 실패했습니다.');
    } finally {
      setSaving(false);
    }
  }

  async function handleRemoveTrack() {
    if (!removeTarget) return;
    try {
      setRemoving(true);
      await removeTrackFromPlaylist(id, removeTarget.trackId);
      setRemoveTarget(null);
      await load();
    } catch {
      setError('곡 삭제에 실패했습니다.');
    } finally {
      setRemoving(false);
    }
  }

  async function handleDeletePlaylist() {
    try {
      setDeletingPl(true);
      await deletePlaylist(id);
      navigate('/playlists');
    } catch {
      setError('재생목록 삭제에 실패했습니다.');
      setDeletingPl(false);
    }
  }

  /* ── Render ── */
  if (loading) {
    return (
      <div className={styles.page}>
        <div className={styles.status}>{'불러오는 중...'}</div>
      </div>
    );
  }

  if (error && !detail) {
    return (
      <div className={styles.page}>
        <div className={styles.statusError}>{error}</div>
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
          <Button
            variant="ghost"
            size="sm"
            onClick={() => navigate(`/playlists/${id}`)}
          >
            {'취소'}
          </Button>
          <Button
            size="sm"
            onClick={handleSave}
            loading={saving}
            disabled={!isDirty && !saving}
          >
            {'저장'}
          </Button>
        </div>
      </div>

      {error && <div className={styles.errorBanner}>{error}</div>}

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
          <span className={styles.trackCount}>{tracks.length}곡</span>
        </h2>

        {tracks.length === 0 ? (
          <div className={styles.emptyTracks}>
            {'수록곡이 없습니다. 음원 목록에서 곡을 추가해보세요.'}
          </div>
        ) : (
          <div className={styles.trackList}>
            {tracks.map((track, idx) => (
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
                    disabled={idx === tracks.length - 1}
                    onClick={() => moveTrack(idx, 1)}
                    aria-label="Move down"
                    title="아래로"
                  >
                    {'\u25BC'}
                  </button>
                  <button
                    className={styles.removeBtn}
                    onClick={() => setRemoveTarget(track)}
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
          <div className={styles.dangerDesc}>
            {'삭제된 재생목록은 복구할 수 없습니다.'}
          </div>
        </div>
        <Button variant="danger" size="sm" onClick={() => setShowDeletePl(true)}>
          {'삭제'}
        </Button>
      </section>

      {/* ── Remove Track Modal ── */}
      <Modal
        open={removeTarget !== null}
        onClose={() => setRemoveTarget(null)}
        title="곡 삭제"
      >
        <div className={styles.modalBody}>
          <p>
            {'정말 '}
            <strong>{removeTarget?.title}</strong>
            {'을(를) 재생목록에서 삭제하시겠습니까?'}
          </p>
        </div>
        <div className={styles.modalFooter}>
          <Button variant="ghost" onClick={() => setRemoveTarget(null)}>
            {'취소'}
          </Button>
          <Button variant="danger" onClick={handleRemoveTrack} loading={removing}>
            {'삭제'}
          </Button>
        </div>
      </Modal>

      {/* ── Delete Playlist Modal ── */}
      <Modal
        open={showDeletePl}
        onClose={() => setShowDeletePl(false)}
        title="재생목록 삭제"
      >
        <div className={styles.modalBody}>
          <p>
            {'정말 '}
            <strong>{detail?.title}</strong>
            {' 재생목록을 삭제하시겠습니까?'}
            <br />
            {'이 작업은 되돌릴 수 없습니다.'}
          </p>
        </div>
        <div className={styles.modalFooter}>
          <Button variant="ghost" onClick={() => setShowDeletePl(false)}>
            {'취소'}
          </Button>
          <Button
            variant="danger"
            onClick={handleDeletePlaylist}
            loading={deletingPl}
          >
            {'삭제'}
          </Button>
        </div>
      </Modal>
    </div>
  );
}
