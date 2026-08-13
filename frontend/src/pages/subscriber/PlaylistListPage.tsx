import { useState, useEffect, useCallback, useRef } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { fetchMyPlaylists, createPlaylist, deletePlaylist } from '@/api/playlists';
import { fetchMySubscription } from '@/api/userSubscriptions';
import { toUploadUrl, getApiErrorCode } from '@/api/client';
import { useToastStore } from '@/store/toastStore';
import type { Playlist } from '@/types';
import { TITLE_PLAYLIST_MAX } from '@/utils/validation';
import Button from '@/components/ui/Button';
import Modal from '@/components/ui/Modal';
import styles from './PlaylistListPage.module.css';

const DEFAULT_MAX_PLAYLISTS = 3;
const NOTES = ['\u266A', '\u266B', '\u2669', '\u266C'];

export default function PlaylistListPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const showToast = useToastStore((s) => s.show);
  const createRequested = (location.state as { openCreate?: boolean } | null)?.openCreate === true;
  const handledCreateRequestKeyRef = useRef<string | null>(null);

  const [playlists, setPlaylists] = useState<Playlist[]>([]);
  const [maxPlaylists, setMaxPlaylists] = useState(DEFAULT_MAX_PLAYLISTS);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [showCreate, setShowCreate] = useState(false);
  const [newTitle, setNewTitle] = useState('');
  const [newDesc, setNewDesc] = useState('');
  const [newThumbFile, setNewThumbFile] = useState<File | null>(null);
  const [newThumbPreview, setNewThumbPreview] = useState<string | null>(null);
  const [creating, setCreating] = useState(false);

  const [deleteTarget, setDeleteTarget] = useState<Playlist | null>(null);
  const [deleting, setDeleting] = useState(false);

  const load = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);

      const [playlistRes, subRes] = await Promise.allSettled([
        fetchMyPlaylists(),
        fetchMySubscription(),
      ]);

      if (playlistRes.status === 'rejected') {
        throw playlistRes.reason;
      }

      setPlaylists(playlistRes.value.dataList);

      if (subRes.status === 'fulfilled' && subRes.value.subscription?.maxPlaylists) {
        setMaxPlaylists(subRes.value.subscription.maxPlaylists);
      } else {
        setMaxPlaylists(DEFAULT_MAX_PLAYLISTS);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : '재생목록을 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const count = playlists.length;
  const canCreate = count < maxPlaylists;
  const fillPercent =
    maxPlaylists > 0 ? Math.min(100, Math.round((count / maxPlaylists) * 100)) : 0;

  function openCreateModal() {
    setNewTitle('');
    setNewDesc('');
    setNewThumbFile(null);
    setNewThumbPreview(null);
    setShowCreate(true);
  }

  function closeCreateModal() {
    setShowCreate(false);
    if (createRequested) {
      navigate('/playlists', { replace: true });
    }
  }

  useEffect(() => {
    if (
      loading ||
      error ||
      !createRequested ||
      handledCreateRequestKeyRef.current === location.key
    ) {
      return;
    }

    handledCreateRequestKeyRef.current = location.key;

    if (!canCreate) {
      showToast('error', '구독 플랜의 재생목록 한도를 초과했습니다. 플랜을 업그레이드해 주세요.');
      navigate('/playlists', { replace: true });
      return;
    }

    setNewTitle('');
    setNewDesc('');
    setNewThumbFile(null);
    setNewThumbPreview(null);
    setShowCreate(true);
  }, [canCreate, createRequested, error, loading, location.key, navigate, showToast]);

  function handleThumbChange(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0] ?? null;
    setNewThumbFile(file);

    if (!file) {
      setNewThumbPreview(null);
      return;
    }

    const previewUrl = URL.createObjectURL(file);
    setNewThumbPreview(previewUrl);
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
      closeCreateModal();
      await load();
    } catch (err) {
      const code = await getApiErrorCode(err);
      if (code === 'PLAYLIST_LIMIT_EXCEEDED') {
        showToast('error', '구독 플랜의 재생목록 한도를 초과했습니다. 플랜을 업그레이드해 주세요.');
        return;
      }

      setError(err instanceof Error ? err.message : '재생목록을 생성하지 못했습니다.');
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
      setError(err instanceof Error ? err.message : '재생목록을 삭제하지 못했습니다.');
    } finally {
      setDeleting(false);
    }
  }

  function handleCardClick(playlist: Playlist) {
    navigate(`/playlists/${playlist.id}`);
  }

  return (
    <div className={styles.page}>
      <div className={styles.pageHeader}>
        <div className={styles.pageTitle}>
          내 재생목록{' '}
          <span className={styles.pageTitleCount}>
            {count} / {maxPlaylists}개
          </span>
        </div>
        {canCreate && (
          <button type="button" className={styles.btnNewPl} onClick={openCreateModal}>
            + 새 재생목록
          </button>
        )}
      </div>

      <div className={styles.planNotice}>
        <div className={styles.pnLeft}>
          <span className={styles.pnIcon}>{'\uD83D\uDCCB'}</span>
          <div className={styles.pnText}>
            <span className={styles.pnStrong}>구독 플랜</span>
            {' - 재생목록은 최대 '}
            {maxPlaylists}
            {'개까지 만들 수 있어요'}
          </div>
        </div>
        <div className={styles.pnBarWrap}>
          <div className={styles.pnBar}>
            <div className={styles.pnBarFill} style={{ width: `${fillPercent}%` }} />
          </div>
          <span className={styles.pnCount}>
            {count} / {maxPlaylists}
          </span>
        </div>
      </div>

      {loading ? (
        <div className={styles.loading}>재생목록을 불러오는 중...</div>
      ) : error ? (
        <div className={styles.error}>{error}</div>
      ) : (
        <div className={styles.plGrid}>
          {playlists.map((pl) => (
            <div key={pl.id} className={styles.myCard} onClick={() => handleCardClick(pl)}>
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
                    type="button"
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
                  <button type="button" className={styles.plPlayBtn} aria-label="Play">
                    {'\u25B6'}
                  </button>
                </div>
              </div>
              <div className={styles.plBody}>
                <div className={styles.plName}>{pl.title}</div>
                <div className={styles.plMeta}>{pl.trackCount}곡</div>
              </div>
            </div>
          ))}

          {canCreate && (
            <div className={styles.addNewCard} onClick={openCreateModal}>
              <div className={styles.addIcon}>+</div>
              <div className={styles.addLabel}>새 재생목록</div>
            </div>
          )}
        </div>
      )}

      <Modal open={showCreate} onClose={closeCreateModal} title="새 재생목록 만들기">
        <div className={styles.modalBody}>
          <div className={styles.formGroup}>
            <label className={styles.formLabel}>이름</label>
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
            <label className={styles.formLabel}>설명 (선택)</label>
            <textarea
              className={styles.formTextarea}
              placeholder="재생목록 설명"
              value={newDesc}
              onChange={(e) => setNewDesc(e.target.value)}
              rows={3}
            />
          </div>
          <div className={styles.formGroup}>
            <label className={styles.formLabel}>썸네일 (선택)</label>
            <div className={styles.thumbUpload}>
              {newThumbPreview ? (
                <div className={styles.thumbPreview}>
                  <img src={newThumbPreview} alt="Preview" />
                  <button
                    type="button"
                    className={styles.thumbRemoveBtn}
                    onClick={() => {
                      setNewThumbFile(null);
                      setNewThumbPreview(null);
                    }}
                  >
                    {'\u2715'}
                  </button>
                </div>
              ) : (
                <label className={styles.thumbDropArea}>
                  <span className={styles.thumbDropIcon}>{'\uD83D\uDDBC'}</span>
                  <span className={styles.thumbDropText}>이미지 선택</span>
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
          <Button variant="ghost" onClick={closeCreateModal}>
            취소
          </Button>
          <Button
            variant="primary"
            onClick={handleCreate}
            loading={creating}
            disabled={!newTitle.trim() || creating}
          >
            만들기
          </Button>
        </div>
      </Modal>

      <Modal
        open={deleteTarget !== null}
        onClose={() => setDeleteTarget(null)}
        title="재생목록 삭제"
      >
        <div className={styles.modalBody}>
          <p>
            정말 <strong>{deleteTarget?.title}</strong> 재생목록을 삭제하시겠습니까?
          </p>
        </div>
        <div className={styles.modalFooter}>
          <Button variant="ghost" onClick={() => setDeleteTarget(null)}>
            취소
          </Button>
          <Button variant="danger" onClick={handleDelete} loading={deleting}>
            삭제
          </Button>
        </div>
      </Modal>
    </div>
  );
}
