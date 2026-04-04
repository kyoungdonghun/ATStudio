import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchMyPlaylists, createPlaylist, deletePlaylist } from '@/api/playlists';
import { toUploadUrl } from '@/api/client';
import { useAuthStore } from '@/store/authStore';
import type { Playlist } from '@/types';
import { TITLE_PLAYLIST_MAX } from '@/utils/validation';
import Button from '@/components/ui/Button';
import Modal from '@/components/ui/Modal';
import styles from './PlaylistListPage.module.css';

/** Maximum number of playlists a subscriber can create */
const MAX_PLAYLISTS_SUBSCRIBER = 3;

/** Placeholder notes for the 4-cell mosaic thumb */
const NOTES = ['\u266A', '\u266B', '\u2669', '\u266C'];

export default function PlaylistListPage() {
  const navigate = useNavigate();
  const role = useAuthStore((s) => s.role);
  const isAdmin = role === 'ADMIN';
  const MAX_PLAYLISTS = isAdmin ? Infinity : MAX_PLAYLISTS_SUBSCRIBER;

  /* ── State ── */
  const [playlists, setPlaylists] = useState<Playlist[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  /* ── Create modal ── */
  const [showCreate, setShowCreate] = useState(false);
  const [newTitle, setNewTitle] = useState('');
  const [newDesc, setNewDesc] = useState('');
  const [newThumbFile, setNewThumbFile] = useState<File | null>(null);
  const [newThumbPreview, setNewThumbPreview] = useState<string | null>(null);
  const [creating, setCreating] = useState(false);

  /* ── Delete confirm ── */
  const [deleteTarget, setDeleteTarget] = useState<Playlist | null>(null);
  const [deleting, setDeleting] = useState(false);

  /* ── Fetch playlists ── */
  const load = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const res = await fetchMyPlaylists();
      setPlaylists(res.dataList);
    } catch (err) {
      setError(err instanceof Error ? err.message : '재생목록을 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  /* ── Derived ── */
  const count = playlists.length;
  const canCreate = count < MAX_PLAYLISTS;
  const fillPercent = Math.round((count / MAX_PLAYLISTS) * 100);

  /* ── Handlers ── */

  function openCreateModal() {
    setNewTitle('');
    setNewDesc('');
    setNewThumbFile(null);
    setNewThumbPreview(null);
    setShowCreate(true);
  }

  function handleThumbChange(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0] ?? null;
    setNewThumbFile(file);
    if (file) {
      const url = URL.createObjectURL(file);
      setNewThumbPreview(url);
    } else {
      setNewThumbPreview(null);
    }
  }

  async function handleCreate() {
    if (!newTitle.trim()) return;
    try {
      setCreating(true);
      await createPlaylist({
        title: newTitle.trim(),
        description: newDesc.trim() || undefined,
        thumbnail: newThumbFile ?? undefined,
      });
      setShowCreate(false);
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : '재생목록 생성에 실패했습니다.');
    } finally {
      setCreating(false);
    }
  }

  async function handleDelete() {
    if (!deleteTarget) return;
    try {
      setDeleting(true);
      await deletePlaylist(deleteTarget.id);
      setDeleteTarget(null);
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : '재생목록 삭제에 실패했습니다.');
    } finally {
      setDeleting(false);
    }
  }

  function handleCardClick(playlist: Playlist) {
    navigate(`/playlists/${playlist.id}`);
  }

  /* ── Render ── */

  return (
    <div className={styles.page}>
      {/* Page Header */}
      <div className={styles.pageHeader}>
        <div className={styles.pageTitle}>
          {'내 재생목록 '}
          <span className={styles.pageTitleCount}>
            {isAdmin ? `${count}개` : `${count} / ${MAX_PLAYLISTS_SUBSCRIBER}개`}
          </span>
        </div>
        {canCreate && (
          <button
            className={styles.btnNewPl}
            onClick={openCreateModal}
          >
            + 새 재생목록
          </button>
        )}
      </div>

      {/* Plan Notice — hide for ADMIN (no playlist limit) */}
      {!isAdmin && (
        <div className={styles.planNotice}>
          <div className={styles.pnLeft}>
            <span className={styles.pnIcon}>{'\uD83D\uDCCB'}</span>
            <div className={styles.pnText}>
              <span className={styles.pnStrong}>{'구독 플랜'}</span>
              {' \u2014 재생목록은 최대 '}
              {MAX_PLAYLISTS_SUBSCRIBER}
              {'개까지 만들 수 있어요.'}
            </div>
          </div>
          <div className={styles.pnBarWrap}>
            <div className={styles.pnBar}>
              <div
                className={styles.pnBarFill}
                style={{ width: `${fillPercent}%` }}
              />
            </div>
            <span className={styles.pnCount}>
              {count} / {MAX_PLAYLISTS_SUBSCRIBER}
            </span>
          </div>
        </div>
      )}

      {/* Content */}
      {loading ? (
        <div className={styles.loading}>{'재생목록을 불러오는 중...'}</div>
      ) : error ? (
        <div className={styles.error}>{error}</div>
      ) : (
        <div className={styles.plGrid}>
          {playlists.map((pl) => (
            <div
              key={pl.id}
              className={styles.myCard}
              onClick={() => handleCardClick(pl)}
            >
              <div className={styles.plThumb}>
                {pl.thumbnail ? (
                  <img
                    className={styles.plThumbImg}
                    src={toUploadUrl(pl.thumbnail)!}
                    alt={pl.title}
                  />
                ) : pl.trackCount >= 4 ? (
                  <div className={styles.plThumbGrid}>
                    {NOTES.map((note, i) => (
                      <div key={i} className={styles.plThumbCell}>
                        {note}
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className={styles.plThumbSingle}>{'\u266A'}</div>
                )}
                <div className={styles.plOverlay}>
                  <div />
                  <button
                    className={styles.deleteBtn}
                    onClick={(e) => {
                      e.stopPropagation();
                      setDeleteTarget(pl);
                    }}
                    aria-label="Delete playlist"
                  >
                    {'\u2715'}
                  </button>
                </div>
                <div className={styles.plPlayOverlay}>
                  <button className={styles.plPlayBtn} aria-label="Play">
                    {'\u25B6'}
                  </button>
                </div>
              </div>
              <div className={styles.plBody}>
                <div className={styles.plName}>{pl.title}</div>
                <div className={styles.plMeta}>
                  {pl.trackCount}곡
                </div>
              </div>
            </div>
          ))}

          {/* Add New Card (only when under limit) */}
          {canCreate && (
            <div
              className={styles.addNewCard}
              onClick={openCreateModal}
            >
              <div className={styles.addIcon}>+</div>
              <div className={styles.addLabel}>{'새 재생목록'}</div>
            </div>
          )}
        </div>
      )}

      {/* ── Create Playlist Modal ── */}
      <Modal
        open={showCreate}
        onClose={() => setShowCreate(false)}
        title="새 재생목록 만들기"
      >
        <div className={styles.modalBody}>
          <div className={styles.formGroup}>
            <label className={styles.formLabel}>{'이름'}</label>
            <input
              className={styles.formInput}
              type="text"
              placeholder="재생목록 이름"
              value={newTitle}
              onChange={(e) => setNewTitle(e.target.value)}
              maxLength={TITLE_PLAYLIST_MAX}
            />
          </div>
          <div className={styles.formGroup}>
            <label className={styles.formLabel}>{'설명 (선택)'}</label>
            <textarea
              className={styles.formTextarea}
              placeholder="재생목록 설명"
              value={newDesc}
              onChange={(e) => setNewDesc(e.target.value)}
              rows={3}
            />
          </div>
          <div className={styles.formGroup}>
            <label className={styles.formLabel}>{'썸네일 (선택)'}</label>
            <div className={styles.thumbUpload}>
              {newThumbPreview ? (
                <div className={styles.thumbPreview}>
                  <img src={newThumbPreview} alt="Preview" />
                  <button
                    className={styles.thumbRemoveBtn}
                    type="button"
                    onClick={() => { setNewThumbFile(null); setNewThumbPreview(null); }}
                  >
                    {'\u2715'}
                  </button>
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
        </div>
        <div className={styles.modalFooter}>
          <Button variant="ghost" onClick={() => setShowCreate(false)}>
            {'취소'}
          </Button>
          <Button
            variant="primary"
            onClick={handleCreate}
            loading={creating}
            disabled={!newTitle.trim()}
          >
            {'만들기'}
          </Button>
        </div>
      </Modal>

      {/* ── Delete Confirm Modal ── */}
      <Modal
        open={deleteTarget !== null}
        onClose={() => setDeleteTarget(null)}
        title="재생목록 삭제"
      >
        <div className={styles.modalBody}>
          <p>
            {'정말 '}
            <strong>{deleteTarget?.title}</strong>
            {' 재생목록을 삭제하시겠습니까?'}
          </p>
        </div>
        <div className={styles.modalFooter}>
          <Button variant="ghost" onClick={() => setDeleteTarget(null)}>
            {'취소'}
          </Button>
          <Button
            variant="danger"
            onClick={handleDelete}
            loading={deleting}
          >
            {'삭제'}
          </Button>
        </div>
      </Modal>
    </div>
  );
}
