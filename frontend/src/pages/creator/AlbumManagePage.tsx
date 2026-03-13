import { useState, useEffect, useCallback, type FormEvent, type ChangeEvent } from 'react';
import {
  fetchAlbums,
  createAlbum,
  updateAlbum,
  deleteAlbum,
  type AlbumDetail,
  fetchAlbumDetail,
} from '@/api/albums';
import { toUploadUrl } from '@/api/client';
import type { Album } from '@/types';
import Button from '@/components/ui/Button';
import Modal from '@/components/ui/Modal';
import styles from './AlbumManagePage.module.css';

/** Album manage page -- list + create/edit/delete via modals */
export default function AlbumManagePage() {
  /* ── Album list state ── */
  const [albums, setAlbums] = useState<Album[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  /* ── Create / Edit modal ── */
  const [formOpen, setFormOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [formTitle, setFormTitle] = useState('');
  const [formDescription, setFormDescription] = useState('');
  const [formThumbnail, setFormThumbnail] = useState<File | null>(null);
  const [formSubmitting, setFormSubmitting] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);

  /* ── Delete modal ── */
  const [deleteTarget, setDeleteTarget] = useState<Album | null>(null);
  const [deleting, setDeleting] = useState(false);

  /* ── Load albums ── */
  const loadAlbums = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      const res = await fetchAlbums({ size: 100 });
      setAlbums(res.dataList);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load albums');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadAlbums();
  }, [loadAlbums]);

  /* ── Open create modal ── */
  function openCreateModal() {
    setEditingId(null);
    setFormTitle('');
    setFormDescription('');
    setFormThumbnail(null);
    setFormError(null);
    setFormOpen(true);
  }

  /* ── Open edit modal ── */
  async function openEditModal(album: Album) {
    setEditingId(album.id);
    setFormTitle(album.title);
    setFormThumbnail(null);
    setFormError(null);
    setFormOpen(true);

    /* Load full detail for description */
    try {
      const detail: AlbumDetail = await fetchAlbumDetail(album.id);
      setFormDescription(detail.description ?? '');
    } catch {
      setFormDescription('');
    }
  }

  /* ── Submit create/edit ── */
  async function handleFormSubmit(e: FormEvent) {
    e.preventDefault();
    setFormError(null);

    if (!formTitle.trim()) {
      setFormError('제목을 입력해주세요.');
      return;
    }

    const formData = new FormData();
    formData.append('title', formTitle.trim());
    if (formDescription.trim()) {
      formData.append('description', formDescription.trim());
    }
    if (formThumbnail) {
      formData.append('thumbnailFile', formThumbnail);
    }

    setFormSubmitting(true);
    try {
      if (editingId) {
        await updateAlbum(editingId, formData);
      } else {
        await createAlbum(formData);
      }
      setFormOpen(false);
      loadAlbums();
    } catch (err) {
      setFormError(
        err instanceof Error ? err.message : '저장에 실패했습니다.',
      );
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
      loadAlbums();
    } catch (err) {
      setError(err instanceof Error ? err.message : '삭제에 실패했습니다.');
      setDeleteTarget(null);
    } finally {
      setDeleting(false);
    }
  }

  /* ── Thumbnail file handler ── */
  function handleThumbnailChange(e: ChangeEvent<HTMLInputElement>) {
    setFormThumbnail(e.target.files?.[0] ?? null);
  }

  return (
    <div className={styles.page}>
      {/* Header */}
      <div className={styles.pageHeader}>
        <div>
          <span className={styles.pageTitle}>{'앨범 관리'}</span>
          {albums.length > 0 && (
            <span className={styles.pageTitleCount}>
              {`(${albums.length})`}
            </span>
          )}
        </div>
        <Button size="sm" onClick={openCreateModal}>
          {'+ 새 앨범'}
        </Button>
      </div>

      {error && <div className={styles.error}>{error}</div>}

      {/* Album grid */}
      {loading ? (
        <div className={styles.loading}>{'Loading...'}</div>
      ) : albums.length === 0 ? (
        <div className={styles.empty}>{'등록된 앨범이 없습니다.'}</div>
      ) : (
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
                    onClick={() => openEditModal(album)}
                  >
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
      )}

      {/* Create / Edit Modal */}
      <Modal
        open={formOpen}
        onClose={() => setFormOpen(false)}
        title={editingId ? '앨범 수정' : '새 앨범'}
      >
        <form className={styles.modalForm} onSubmit={handleFormSubmit}>
          {formError && <div className={styles.modalError}>{formError}</div>}

          <div className={styles.field}>
            <label className={`${styles.label} ${styles.required}`}>
              {'제목'}
            </label>
            <input
              className={styles.input}
              type="text"
              maxLength={100}
              value={formTitle}
              onChange={(e) => setFormTitle(e.target.value)}
              placeholder="앨범 제목"
            />
          </div>

          <div className={styles.field}>
            <label className={styles.label}>{'설명'}</label>
            <textarea
              className={styles.textarea}
              value={formDescription}
              onChange={(e) => setFormDescription(e.target.value)}
              placeholder="앨범에 대한 설명 (선택사항)"
            />
          </div>

          <div className={styles.field}>
            <label className={styles.label}>{'썸네일'}</label>
            <label
              className={`${styles.fileLabel} ${formThumbnail ? styles.fileLabelSelected : ''}`}
            >
              <input
                type="file"
                accept="image/*"
                className={styles.fileHidden}
                onChange={handleThumbnailChange}
              />
              {formThumbnail ? formThumbnail.name : '이미지 선택'}
            </label>
          </div>

          <div className={styles.modalActions}>
            <Button
              variant="ghost"
              size="sm"
              type="button"
              onClick={() => setFormOpen(false)}
              disabled={formSubmitting}
            >
              {'취소'}
            </Button>
            <Button size="sm" type="submit" loading={formSubmitting}>
              {editingId ? '저장' : '생성'}
            </Button>
          </div>
        </form>
      </Modal>

      {/* Delete Confirm Modal */}
      <Modal
        open={deleteTarget !== null}
        onClose={() => setDeleteTarget(null)}
        title="앨범 삭제"
      >
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
          <Button
            variant="danger"
            size="sm"
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
