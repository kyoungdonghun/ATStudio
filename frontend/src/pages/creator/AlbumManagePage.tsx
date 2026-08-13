import { useState, useEffect, useCallback, useRef, type FormEvent } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import {
  fetchAlbums,
  createAlbum,
  updateAlbum,
  deleteAlbum,
  type AlbumDetail,
  fetchAlbumDetail,
} from '@/api/albums';
import { toUploadUrl } from '@/api/client';
import type { Album, PageInfo } from '@/types';
import { TITLE_ALBUM_MAX, DESCRIPTION_MAX } from '@/utils/validation';
import {
  getCatalogTotalPages,
  normalizeCatalogPage,
  PUBLIC_CATALOG_PAGE_SIZE,
} from '@/utils/catalogPagination';
import Button from '@/components/ui/Button';
import Modal from '@/components/ui/Modal';
import Pagination from '@/components/ui/Pagination';
import AlbumThumbnailField from './AlbumThumbnailField';
import { emptyAlbumThumbnailSelection, isAlbumThumbnailBlocked } from './albumThumbnail';
import styles from './AlbumManagePage.module.css';

/** Album manage page -- list + create/edit/delete via modals */
export default function AlbumManagePage() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const latestSearchParamsRef = useRef(searchParams);
  latestSearchParamsRef.current = searchParams;
  const rawPage = searchParams.get('page');
  const currentPage = normalizeCatalogPage(rawPage);
  const pageNeedsNormalization = rawPage !== null && rawPage !== String(currentPage);

  /* ── Album list state ── */
  const [albums, setAlbums] = useState<Album[]>([]);
  const [pageInfo, setPageInfo] = useState<PageInfo | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const listRequestGenerationRef = useRef(0);
  const listRequestControllerRef = useRef<AbortController | null>(null);
  const listRetryInFlightRef = useRef(false);

  /* ── Create / Edit modal ── */
  const [formOpen, setFormOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [formTitle, setFormTitle] = useState('');
  const [formDescription, setFormDescription] = useState('');
  const [formThumbnail, setFormThumbnail] = useState(emptyAlbumThumbnailSelection);
  const [formCurrentThumbnail, setFormCurrentThumbnail] = useState<string | null>(null);
  const [formDetailStatus, setFormDetailStatus] = useState<'idle' | 'loading' | 'ready' | 'error'>(
    'idle',
  );
  const [formSubmitting, setFormSubmitting] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const formDetailGenerationRef = useRef(0);
  const formDetailControllerRef = useRef<AbortController | null>(null);
  const editingIdRef = useRef<number | null>(null);
  editingIdRef.current = editingId;

  /* ── Delete modal ── */
  const [deleteTarget, setDeleteTarget] = useState<Album | null>(null);
  const [deleting, setDeleting] = useState(false);

  /* ── Load albums ── */
  const loadAlbums = useCallback(async () => {
    listRequestControllerRef.current?.abort();
    const controller = new AbortController();
    listRequestControllerRef.current = controller;
    const generation = ++listRequestGenerationRef.current;
    const isCurrent = () =>
      generation === listRequestGenerationRef.current && !controller.signal.aborted;
    setLoading(true);
    setError(null);

    try {
      const res = await fetchAlbums(
        { page: currentPage, size: PUBLIC_CATALOG_PAGE_SIZE },
        controller.signal,
      );
      if (!isCurrent()) return;
      const totalPages = getCatalogTotalPages(res.pageInfo.total, PUBLIC_CATALOG_PAGE_SIZE);
      if (currentPage > totalPages && res.dataList.length === 0) {
        const next = new URLSearchParams(latestSearchParamsRef.current);
        next.set('page', String(totalPages));
        setSearchParams(next, { replace: true });
        return;
      }
      setAlbums(res.dataList);
      setPageInfo(res.pageInfo);
    } catch {
      if (!isCurrent()) return;
      setAlbums([]);
      setPageInfo(null);
      setError('앨범 목록을 불러오지 못했습니다. 잠시 후 다시 시도해주세요.');
    } finally {
      if (isCurrent()) {
        setLoading(false);
        listRequestControllerRef.current = null;
      }
    }
  }, [currentPage, setSearchParams]);

  useEffect(() => {
    if (rawPage === null || rawPage === String(currentPage)) return;
    const next = new URLSearchParams(searchParams);
    next.set('page', String(currentPage));
    setSearchParams(next, { replace: true });
  }, [currentPage, rawPage, searchParams, setSearchParams]);

  useEffect(() => {
    if (pageNeedsNormalization) return;
    void loadAlbums();
    return () => {
      listRequestControllerRef.current?.abort();
      listRequestControllerRef.current = null;
      listRequestGenerationRef.current += 1;
    };
  }, [loadAlbums, pageNeedsNormalization]);

  const retryLoadAlbums = useCallback(() => {
    if (loading || listRetryInFlightRef.current) return;
    listRetryInFlightRef.current = true;
    void loadAlbums().finally(() => {
      listRetryInFlightRef.current = false;
    });
  }, [loadAlbums, loading]);

  /* ── Open create modal ── */
  function openCreateModal() {
    formDetailControllerRef.current?.abort();
    formDetailGenerationRef.current += 1;
    editingIdRef.current = null;
    setEditingId(null);
    setFormTitle('');
    setFormDescription('');
    setFormThumbnail(emptyAlbumThumbnailSelection());
    setFormCurrentThumbnail(null);
    setFormDetailStatus('ready');
    setFormError(null);
    setFormOpen(true);
  }

  /* ── Open edit modal ── */
  async function loadEditDetail(albumId: number) {
    formDetailControllerRef.current?.abort();
    const controller = new AbortController();
    formDetailControllerRef.current = controller;
    const generation = ++formDetailGenerationRef.current;
    const isCurrent = () =>
      generation === formDetailGenerationRef.current &&
      editingIdRef.current === albumId &&
      !controller.signal.aborted;

    setFormDetailStatus('loading');
    setFormError(null);

    try {
      const detail: AlbumDetail = await fetchAlbumDetail(albumId, controller.signal);
      if (!isCurrent()) return;
      setFormTitle(detail.title);
      setFormDescription(detail.description ?? '');
      setFormCurrentThumbnail(toUploadUrl(detail.thumbnailUrl));
      setFormDetailStatus('ready');
    } catch {
      if (!isCurrent()) return;
      setFormDetailStatus('error');
      setFormError('앨범 정보를 불러오지 못했습니다.');
    } finally {
      if (isCurrent()) formDetailControllerRef.current = null;
    }
  }

  function openEditModal(album: Album) {
    editingIdRef.current = album.id;
    setEditingId(album.id);
    setFormTitle('');
    setFormDescription('');
    setFormThumbnail(emptyAlbumThumbnailSelection());
    setFormCurrentThumbnail(null);
    setFormError(null);
    setFormOpen(true);
    void loadEditDetail(album.id);
  }

  function closeFormModal(force = false) {
    if (formSubmitting && !force) return;
    formDetailControllerRef.current?.abort();
    formDetailControllerRef.current = null;
    formDetailGenerationRef.current += 1;
    editingIdRef.current = null;
    setFormOpen(false);
    setEditingId(null);
    setFormThumbnail(emptyAlbumThumbnailSelection());
    setFormCurrentThumbnail(null);
    setFormDetailStatus('idle');
    setFormError(null);
  }

  /* ── Submit create/edit ── */
  async function handleFormSubmit(e: FormEvent) {
    e.preventDefault();
    setFormError(null);

    if (formDetailStatus !== 'ready' || isAlbumThumbnailBlocked(formThumbnail)) return;

    if (!formTitle.trim()) {
      setFormError('제목을 입력해주세요.');
      return;
    }

    const formData = new FormData();
    formData.append('title', formTitle.trim());
    if (editingId !== null || formDescription.trim()) {
      formData.append('description', formDescription.trim());
    }
    if (formThumbnail.file) {
      formData.append('thumbnailFile', formThumbnail.file);
    }

    const targetId = editingId;
    setFormSubmitting(true);
    try {
      if (targetId !== null) {
        await updateAlbum(targetId, formData);
      } else {
        await createAlbum(formData);
      }
      closeFormModal(true);
      await loadAlbums();
    } catch (err) {
      setFormError(err instanceof Error ? err.message : '저장에 실패했습니다.');
    } finally {
      setFormSubmitting(false);
    }
  }

  /* ── Delete ── */
  async function handleDelete() {
    if (!deleteTarget) return;
    setDeleting(true);

    try {
      await deleteAlbum(deleteTarget.id);
      setDeleteTarget(null);
      await loadAlbums();
    } catch (err) {
      setError(err instanceof Error ? err.message : '삭제에 실패했습니다.');
      setDeleteTarget(null);
    } finally {
      setDeleting(false);
    }
  }

  function goToPage(page: number) {
    const next = new URLSearchParams(searchParams);
    next.set('page', String(page));
    setSearchParams(next);
  }

  useEffect(
    () => () => {
      formDetailControllerRef.current?.abort();
      formDetailGenerationRef.current += 1;
      editingIdRef.current = null;
    },
    [],
  );

  return (
    <div className={styles.page}>
      {/* Header */}
      <div className={styles.pageHeader}>
        <div>
          <span className={styles.pageTitle}>{'앨범 관리'}</span>
          {pageInfo && (
            <span className={styles.pageTitleCount}>{`(${pageInfo.total.toLocaleString()})`}</span>
          )}
        </div>
        <Button size="sm" onClick={openCreateModal}>
          {'+ 새 앨범'}
        </Button>
      </div>

      {/* Album grid */}
      {loading ? (
        <div className={styles.loading}>{'앨범 목록을 불러오는 중...'}</div>
      ) : error ? (
        <div className={styles.error} role="alert" aria-label="앨범 목록 불러오기 실패">
          <p>{error}</p>
          <button type="button" onClick={retryLoadAlbums}>
            앨범 목록 다시 시도
          </button>
        </div>
      ) : albums.length === 0 ? (
        <div className={styles.empty}>{'등록된 앨범이 없습니다.'}</div>
      ) : (
        <>
          <div className={styles.albumGrid}>
            {albums.map((album) => (
              <div key={album.id} className={styles.albumCard}>
                <div className={styles.albumThumb}>
                  {album.thumbnailUrl ? (
                    <img src={toUploadUrl(album.thumbnailUrl)!} alt={album.title} />
                  ) : (
                    '\u266A'
                  )}
                </div>
                <div className={styles.albumBody}>
                  <div className={styles.albumTitle}>{album.title}</div>
                  <div className={styles.albumMeta}>
                    {album.trackCount > 0 ? `${album.trackCount}곡` : '트랙 없음'}
                  </div>
                  <div className={styles.albumActions}>
                    <button
                      className={styles.albumActBtn}
                      onClick={() => navigate(`/admin/albums/${album.id}/edit`)}
                    >
                      {'음원 관리'}
                    </button>
                    <button className={styles.albumActBtn} onClick={() => openEditModal(album)}>
                      {'수정'}
                    </button>
                    <button
                      className={styles.albumActBtnDanger}
                      onClick={() => setDeleteTarget(album)}
                    >
                      {'삭제'}
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
          {pageInfo && (
            <Pagination pageInfo={pageInfo} currentPage={currentPage} onPageChange={goToPage} />
          )}
        </>
      )}

      {/* Create / Edit Modal */}
      <Modal
        open={formOpen}
        onClose={() => closeFormModal()}
        title={editingId !== null ? '앨범 수정' : '새 앨범'}
      >
        <form className={styles.modalForm} onSubmit={handleFormSubmit}>
          {formDetailStatus === 'loading' && (
            <div className={styles.modalStatus} role="status">
              앨범 정보를 불러오는 중...
            </div>
          )}
          {formError && (
            <div
              className={styles.modalError}
              role="alert"
              aria-label={
                formDetailStatus === 'error' ? '앨범 정보 불러오기 실패' : '앨범 저장 실패'
              }
            >
              <span>{formError}</span>
              {formDetailStatus === 'error' && editingId !== null && (
                <button type="button" onClick={() => void loadEditDetail(editingId)}>
                  앨범 정보 다시 시도
                </button>
              )}
            </div>
          )}

          <div className={styles.field}>
            <label className={`${styles.label} ${styles.required}`}>{'제목'}</label>
            <input
              className={styles.input}
              type="text"
              maxLength={TITLE_ALBUM_MAX}
              value={formTitle}
              onChange={(e) => setFormTitle(e.target.value)}
              placeholder="앨범 제목"
              disabled={formSubmitting || formDetailStatus !== 'ready'}
            />
          </div>

          <div className={styles.field}>
            <label className={styles.label}>{'설명'}</label>
            <textarea
              className={styles.textarea}
              maxLength={DESCRIPTION_MAX}
              value={formDescription}
              onChange={(e) => setFormDescription(e.target.value)}
              placeholder="앨범에 대한 설명 (선택사항)"
              disabled={formSubmitting || formDetailStatus !== 'ready'}
            />
          </div>

          <AlbumThumbnailField
            value={formThumbnail}
            onChange={setFormThumbnail}
            existingImageUrl={formCurrentThumbnail}
            disabled={formSubmitting || formDetailStatus !== 'ready'}
          />

          <div className={styles.modalActions}>
            <Button
              variant="ghost"
              size="sm"
              type="button"
              onClick={() => closeFormModal()}
              disabled={formSubmitting}
            >
              {'취소'}
            </Button>
            <Button
              size="sm"
              type="submit"
              loading={formSubmitting}
              disabled={formDetailStatus !== 'ready' || isAlbumThumbnailBlocked(formThumbnail)}
            >
              {editingId !== null ? '저장' : '생성'}
            </Button>
          </div>
        </form>
      </Modal>

      {/* Delete Confirm Modal */}
      <Modal open={deleteTarget !== null} onClose={() => setDeleteTarget(null)} title="앨범 삭제">
        <div className={styles.modalBody}>
          <strong>{deleteTarget?.title}</strong>
          {' 앨범을 삭제하시겠습니까?'}
          <br />
          {'앨범에 포함된 트랙은 삭제되지 않습니다.'}
        </div>
        <div className={styles.modalActions}>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => setDeleteTarget(null)}
            disabled={deleting}
          >
            {'취소'}
          </Button>
          <Button variant="danger" size="sm" onClick={handleDelete} loading={deleting}>
            {'삭제'}
          </Button>
        </div>
      </Modal>
    </div>
  );
}
