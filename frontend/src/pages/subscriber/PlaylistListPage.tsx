import { useState, useEffect, useCallback, useRef } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { fetchMyPlaylists, createPlaylist, deletePlaylist } from '@/api/playlists';
import { fetchMySubscription } from '@/api/userSubscriptions';
import { toUploadUrl, getApiErrorCode } from '@/api/client';
import { classifyLoadError } from '@/api/loadError';
import { useAuthStore } from '@/store/authStore';
import { useToastStore } from '@/store/toastStore';
import type { Playlist } from '@/types';
import { TITLE_PLAYLIST_MAX } from '@/utils/validation';
import { createOwnerKey, getCurrentOwnerKey } from '@/utils/ownerProjection';
import Button from '@/components/ui/Button';
import Modal from '@/components/ui/Modal';
import styles from './PlaylistListPage.module.css';

const NOTES = ['\u266A', '\u266B', '\u2669', '\u266C'];
type CapacityState = 'loading' | 'known' | 'error';

export default function PlaylistListPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const showToast = useToastStore((s) => s.show);
  const userID = useAuthStore((s) => s.user?.id ?? null);
  const accessToken = useAuthStore((s) => s.accessToken);
  const ownerKey = createOwnerKey(userID, accessToken);
  const createRequested = (location.state as { openCreate?: boolean } | null)?.openCreate === true;
  const handledCreateRequestKeyRef = useRef<string | null>(null);
  const playlistGenerationRef = useRef(0);
  const playlistControllerRef = useRef<AbortController | null>(null);
  const playlistOwnerKeyRef = useRef<string | null>(null);
  const capacityGenerationRef = useRef(0);
  const capacityControllerRef = useRef<AbortController | null>(null);
  const capacityInFlightRef = useRef(false);
  const capacityOwnerKeyRef = useRef<string | null>(null);

  const [playlists, setPlaylists] = useState<Playlist[]>([]);
  const [playlistOwnerKey, setPlaylistOwnerKey] = useState<string | null>(null);
  const [maxPlaylists, setMaxPlaylists] = useState<number | null>(null);
  const [capacityOwnerKey, setCapacityOwnerKey] = useState<string | null>(null);
  const [capacityState, setCapacityState] = useState<CapacityState>('loading');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [errorOwnerKey, setErrorOwnerKey] = useState<string | null>(null);

  const [showCreate, setShowCreate] = useState(false);
  const [createModalOwnerKey, setCreateModalOwnerKey] = useState<string | null>(null);
  const [newTitle, setNewTitle] = useState('');
  const [newDesc, setNewDesc] = useState('');
  const [newThumbFile, setNewThumbFile] = useState<File | null>(null);
  const [newThumbPreview, setNewThumbPreview] = useState<string | null>(null);
  const [creating, setCreating] = useState(false);

  const [deleteTarget, setDeleteTarget] = useState<Playlist | null>(null);
  const [deleteTargetOwnerKey, setDeleteTargetOwnerKey] = useState<string | null>(null);
  const [deleting, setDeleting] = useState(false);

  const loadPlaylists = useCallback(async () => {
    playlistControllerRef.current?.abort();
    const controller = new AbortController();
    playlistControllerRef.current = controller;
    const generation = ++playlistGenerationRef.current;
    const requestOwnerKey = ownerKey;
    const isCurrent = () =>
      requestOwnerKey !== null &&
      generation === playlistGenerationRef.current &&
      getCurrentOwnerKey(requestOwnerKey) === requestOwnerKey;
    playlistOwnerKeyRef.current = null;
    setPlaylistOwnerKey(null);
    try {
      setLoading(true);
      setError(null);
      setPlaylists([]);
      if (ownerKey === null) return;
      const playlistRes = await fetchMyPlaylists(controller.signal);
      if (isCurrent()) {
        playlistOwnerKeyRef.current = ownerKey;
        setPlaylistOwnerKey(ownerKey);
        setPlaylists(playlistRes.dataList);
      }
    } catch (loadError) {
      if (isCurrent() && classifyLoadError(loadError) !== 'cancelled') {
        setError('재생목록을 불러오지 못했습니다.');
        setErrorOwnerKey(requestOwnerKey);
      }
    } finally {
      if (isCurrent()) setLoading(false);
    }
  }, [ownerKey]);

  const loadCapacity = useCallback(async () => {
    if (capacityInFlightRef.current) return;
    capacityInFlightRef.current = true;
    capacityControllerRef.current?.abort();
    const controller = new AbortController();
    capacityControllerRef.current = controller;
    const generation = ++capacityGenerationRef.current;
    const requestOwnerKey = ownerKey;
    const isCurrent = () =>
      requestOwnerKey !== null &&
      generation === capacityGenerationRef.current &&
      getCurrentOwnerKey(requestOwnerKey) === requestOwnerKey;

    capacityOwnerKeyRef.current = null;
    setMaxPlaylists(null);
    setCapacityOwnerKey(null);
    setCapacityState('loading');
    try {
      if (ownerKey === null) return;
      const response = await fetchMySubscription(controller.signal);
      const capacity = response.subscription?.maxPlaylists;
      if (!Number.isSafeInteger(capacity) || (capacity ?? 0) <= 0) {
        throw new Error('Invalid playlist capacity');
      }
      if (isCurrent()) {
        capacityOwnerKeyRef.current = ownerKey;
        setMaxPlaylists(capacity!);
        setCapacityOwnerKey(ownerKey);
        setCapacityState('known');
      }
    } catch (loadError) {
      if (isCurrent() && classifyLoadError(loadError) !== 'cancelled') {
        setCapacityOwnerKey(requestOwnerKey);
        setCapacityState('error');
      }
    } finally {
      if (isCurrent()) capacityInFlightRef.current = false;
    }
  }, [ownerKey]);

  useEffect(() => {
    void loadPlaylists();
    void loadCapacity();
    return () => {
      playlistControllerRef.current?.abort();
      capacityControllerRef.current?.abort();
      playlistGenerationRef.current += 1;
      capacityGenerationRef.current += 1;
      capacityInFlightRef.current = false;
      playlistOwnerKeyRef.current = null;
      capacityOwnerKeyRef.current = null;
    };
  }, [loadCapacity, loadPlaylists]);

  const currentPlaylists = playlistOwnerKey === ownerKey ? playlists : [];
  const currentError = errorOwnerKey === ownerKey ? error : null;
  const count = currentPlaylists.length;
  const playlistCurrent =
    ownerKey !== null && playlistOwnerKey === ownerKey && !loading && currentError === null;
  const capacityKnown =
    ownerKey !== null &&
    capacityState === 'known' &&
    maxPlaylists !== null &&
    capacityOwnerKey === ownerKey;
  const canCreate = playlistCurrent && capacityKnown && count < maxPlaylists;
  const fillPercent = capacityKnown ? Math.min(100, Math.round((count / maxPlaylists) * 100)) : 0;
  const currentCapacityState: CapacityState =
    capacityOwnerKey === ownerKey || capacityState === 'loading' ? capacityState : 'loading';
  const currentLoading = loading || (playlistOwnerKey !== ownerKey && currentError === null);
  const showCreateCurrent = showCreate && createModalOwnerKey === ownerKey && canCreate;
  const currentDeleteTarget =
    deleteTargetOwnerKey === ownerKey &&
    deleteTarget !== null &&
    currentPlaylists.some((playlist) => playlist.id === deleteTarget.id)
      ? deleteTarget
      : null;

  const resetCreateForm = useCallback(() => {
    setNewTitle('');
    setNewDesc('');
    setNewThumbFile(null);
    setNewThumbPreview(null);
  }, []);

  useEffect(() => {
    setShowCreate(false);
    setCreateModalOwnerKey(null);
    setDeleteTarget(null);
    setDeleteTargetOwnerKey(null);
    resetCreateForm();
  }, [ownerKey, resetCreateForm]);

  useEffect(() => {
    if (!showCreate || canCreate) return;
    setShowCreate(false);
    resetCreateForm();
  }, [canCreate, resetCreateForm, showCreate]);

  function openCreateModal() {
    if (!canCreate) return;
    resetCreateForm();
    setShowCreate(true);
    setCreateModalOwnerKey(ownerKey);
  }

  function closeCreateModal() {
    setShowCreate(false);
    setCreateModalOwnerKey(null);
    resetCreateForm();
    if (createRequested) {
      navigate('/playlists', { replace: true });
    }
  }

  useEffect(() => {
    if (
      loading ||
      error ||
      !capacityKnown ||
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

    resetCreateForm();
    setShowCreate(true);
    setCreateModalOwnerKey(ownerKey);
  }, [
    canCreate,
    capacityKnown,
    createRequested,
    error,
    loading,
    location.key,
    navigate,
    ownerKey,
    resetCreateForm,
    showToast,
  ]);

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
    const operationOwnerKey = ownerKey;
    if (
      !newTitle.trim() ||
      creating ||
      !canCreate ||
      ownerKey === null ||
      getCurrentOwnerKey(operationOwnerKey) !== operationOwnerKey ||
      playlistOwnerKeyRef.current !== ownerKey ||
      capacityOwnerKeyRef.current !== ownerKey ||
      capacityInFlightRef.current
    ) {
      return;
    }

    try {
      setCreating(true);
      await createPlaylist({
        title: newTitle.trim(),
        description: newDesc.trim() || undefined,
        thumbnail: newThumbFile ?? undefined,
      });
      if (getCurrentOwnerKey(operationOwnerKey) !== operationOwnerKey) return;
      closeCreateModal();
      await loadPlaylists();
    } catch (err) {
      if (getCurrentOwnerKey(operationOwnerKey) !== operationOwnerKey) return;
      const code = await getApiErrorCode(err);
      if (code === 'PLAYLIST_LIMIT_EXCEEDED') {
        showToast('error', '구독 플랜의 재생목록 한도를 초과했습니다. 플랜을 업그레이드해 주세요.');
        return;
      }

      setError(err instanceof Error ? err.message : '재생목록을 생성하지 못했습니다.');
      setErrorOwnerKey(operationOwnerKey);
    } finally {
      if (getCurrentOwnerKey(operationOwnerKey) === operationOwnerKey) setCreating(false);
    }
  }

  async function handleDelete() {
    const operationOwnerKey = ownerKey;
    if (
      !currentDeleteTarget ||
      operationOwnerKey === null ||
      getCurrentOwnerKey(operationOwnerKey) !== operationOwnerKey ||
      playlistOwnerKeyRef.current !== operationOwnerKey ||
      deleteTargetOwnerKey !== operationOwnerKey
    ) {
      return;
    }

    try {
      setDeleting(true);
      await deletePlaylist(currentDeleteTarget.id);
      if (getCurrentOwnerKey(operationOwnerKey) !== operationOwnerKey) return;
      setDeleteTarget(null);
      setDeleteTargetOwnerKey(null);
      await loadPlaylists();
    } catch (err) {
      if (getCurrentOwnerKey(operationOwnerKey) !== operationOwnerKey) return;
      setError(err instanceof Error ? err.message : '재생목록을 삭제하지 못했습니다.');
      setErrorOwnerKey(operationOwnerKey);
    } finally {
      if (getCurrentOwnerKey(operationOwnerKey) === operationOwnerKey) setDeleting(false);
    }
  }

  function handleCardClick(playlist: Playlist) {
    if (
      ownerKey === null ||
      getCurrentOwnerKey(ownerKey) !== ownerKey ||
      playlistOwnerKeyRef.current !== ownerKey ||
      !currentPlaylists.some((item) => item.id === playlist.id)
    ) {
      return;
    }
    navigate(`/playlists/${playlist.id}`);
  }

  return (
    <div className={styles.page}>
      <div className={styles.pageHeader}>
        <div className={styles.pageTitle}>
          내 재생목록{' '}
          <span className={styles.pageTitleCount}>
            {capacityKnown ? `${count} / ${maxPlaylists}개` : `${count}개`}
          </span>
        </div>
        {canCreate && (
          <button type="button" className={styles.btnNewPl} onClick={openCreateModal}>
            새 재생목록
          </button>
        )}
      </div>

      <div className={styles.planNotice}>
        <div className={styles.pnLeft}>
          <span className={styles.pnIcon}>{'\uD83D\uDCCB'}</span>
          <div className={styles.pnText}>
            <span className={styles.pnStrong}>구독 플랜</span>
            {currentCapacityState === 'loading' && (
              <span>{'재생목록 생성 한도를 확인하는 중입니다.'}</span>
            )}
            {currentCapacityState === 'known' && capacityKnown && (
              <span>{`재생목록은 최대 ${maxPlaylists}개까지 만들 수 있어요`}</span>
            )}
            {currentCapacityState === 'error' && (
              <span>{'재생목록 생성 한도를 확인하지 못했습니다.'}</span>
            )}
          </div>
        </div>
        {capacityKnown ? (
          <div className={styles.pnBarWrap}>
            <div className={styles.pnBar}>
              <div className={styles.pnBarFill} style={{ width: `${fillPercent}%` }} />
            </div>
            <span className={styles.pnCount}>
              {count} / {maxPlaylists}
            </span>
          </div>
        ) : currentCapacityState === 'error' ? (
          <Button variant="ghost" size="sm" onClick={() => void loadCapacity()}>
            {'한도 다시 확인'}
          </Button>
        ) : null}
      </div>

      {currentLoading ? (
        <div className={styles.loading}>재생목록을 불러오는 중...</div>
      ) : currentError ? (
        <div className={styles.error}>{currentError}</div>
      ) : (
        <div className={styles.plGrid}>
          {currentPlaylists.map((pl) => (
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
                      if (playlistCurrent) setDeleteTarget(pl);
                      if (playlistCurrent) setDeleteTargetOwnerKey(ownerKey);
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

      <Modal open={showCreateCurrent} onClose={closeCreateModal} title="새 재생목록 만들기">
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
        open={currentDeleteTarget !== null}
        onClose={() => {
          setDeleteTarget(null);
          setDeleteTargetOwnerKey(null);
        }}
        title="재생목록 삭제"
      >
        <div className={styles.modalBody}>
          <p>
            정말 <strong>{currentDeleteTarget?.title}</strong> 재생목록을 삭제하시겠습니까?
          </p>
        </div>
        <div className={styles.modalFooter}>
          <Button
            variant="ghost"
            onClick={() => {
              setDeleteTarget(null);
              setDeleteTargetOwnerKey(null);
            }}
          >
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
