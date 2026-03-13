import { useState, useEffect, type FormEvent, type ChangeEvent } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { fetchAlbumDetail, updateAlbum } from '@/api/albums';
import { toUploadUrl } from '@/api/client';
import Button from '@/components/ui/Button';
import styles from './AlbumEditPage.module.css';

/** Screen L-5: Album edit */
export default function AlbumEditPage() {
  const { albumId: id } = useParams<{ albumId: string }>();
  const navigate = useNavigate();

  /* ── Form state ── */
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [thumbnail, setThumbnail] = useState<File | null>(null);
  const [thumbPreview, setThumbPreview] = useState<string | null>(null);

  /* ── Existing data ── */
  const [currentThumbUrl, setCurrentThumbUrl] = useState<string | null>(null);

  /* ── UI state ── */
  const [pageLoading, setPageLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  /* ── Load existing album ── */
  useEffect(() => {
    let cancelled = false;

    async function loadAlbum() {
      if (!id) return;

      try {
        const album = await fetchAlbumDetail(Number(id));
        if (cancelled) return;

        setTitle(album.title);
        setDescription(album.description ?? '');
        setCurrentThumbUrl(toUploadUrl(album.thumbnailUrl));
      } catch (err) {
        if (!cancelled) {
          setError(
            err instanceof Error ? err.message : '앨범 정보를 불러올 수 없습니다.',
          );
        }
      } finally {
        if (!cancelled) setPageLoading(false);
      }
    }

    loadAlbum();
    return () => { cancelled = true; };
  }, [id]);

  /* ── Thumbnail handler ── */
  function handleThumbnailChange(e: ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0] ?? null;
    setThumbnail(file);

    if (thumbPreview) {
      URL.revokeObjectURL(thumbPreview);
    }
    setThumbPreview(file ? URL.createObjectURL(file) : null);
  }

  /* ── Displayed thumbnail: new preview > current ── */
  const displayThumb = thumbPreview ?? currentThumbUrl;

  /* ── Submit ── */
  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);

    if (!id) return;

    if (!title.trim()) {
      setError('앨범 제목을 입력해주세요.');
      return;
    }

    const formData = new FormData();
    formData.append('title', title.trim());
    formData.append('description', description.trim());
    if (thumbnail) {
      formData.append('thumbnailFile', thumbnail);
    }

    setSubmitting(true);
    try {
      await updateAlbum(Number(id), formData);
      navigate('/admin/albums');
    } catch (err) {
      const msg =
        err instanceof Error ? err.message : '앨범 수정에 실패했습니다.';
      setError(msg);
    } finally {
      setSubmitting(false);
    }
  }

  if (pageLoading) {
    return (
      <div className={styles.page}>
        <div className={styles.loading}>{'Loading...'}</div>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <h1 className={styles.pageTitle}>{'앨범 수정'}</h1>

      <form className={styles.form} onSubmit={handleSubmit}>
        {error && <div className={styles.error}>{error}</div>}

        {/* Title */}
        <div className={styles.field}>
          <label className={`${styles.label} ${styles.required}`}>{'제목'}</label>
          <input
            className={styles.input}
            type="text"
            maxLength={100}
            value={title}
            onChange={(e) => setTitle(e.target.value)}
          />
        </div>

        {/* Description */}
        <div className={styles.field}>
          <label className={styles.label}>{'설명'}</label>
          <textarea
            className={styles.textarea}
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />
        </div>

        {/* Thumbnail */}
        <div className={styles.field}>
          <span className={styles.label}>{'썸네일'}</span>
          <div className={styles.thumbArea}>
            {displayThumb && (
              <img
                src={displayThumb}
                alt="앨범 썸네일"
                className={styles.thumbPreview}
              />
            )}
            <label
              className={`${styles.fileLabel} ${thumbnail ? styles.fileLabelSelected : ''}`}
            >
              <input
                type="file"
                accept="image/*"
                className={styles.fileHidden}
                onChange={handleThumbnailChange}
              />
              {thumbnail ? thumbnail.name : '새 이미지 선택'}
            </label>
            {currentThumbUrl && !thumbnail && (
              <span className={styles.currentFile}>
                {'현재 썸네일이 설정되어 있습니다'}
              </span>
            )}
          </div>
        </div>

        {/* Actions */}
        <div className={styles.actions}>
          <Button
            variant="ghost"
            type="button"
            onClick={() => navigate(-1)}
            disabled={submitting}
          >
            {'취소'}
          </Button>
          <Button type="submit" loading={submitting}>
            {'저장'}
          </Button>
        </div>
      </form>
    </div>
  );
}
