import { useState, type FormEvent, type ChangeEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { createAlbum } from '@/api/albums';
import Button from '@/components/ui/Button';
import styles from './AlbumCreatePage.module.css';

/** Screen L-4: Album create */
export default function AlbumCreatePage() {
  const navigate = useNavigate();

  /* ── Form state ── */
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [thumbnail, setThumbnail] = useState<File | null>(null);
  const [thumbPreview, setThumbPreview] = useState<string | null>(null);

  /* ── UI state ── */
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  /* ── Thumbnail handler ── */
  function handleThumbnailChange(e: ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0] ?? null;
    setThumbnail(file);

    if (thumbPreview) {
      URL.revokeObjectURL(thumbPreview);
    }
    setThumbPreview(file ? URL.createObjectURL(file) : null);
  }

  /* ── Submit ── */
  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);

    if (!title.trim()) {
      setError('앨범 제목을 입력해주세요.');
      return;
    }

    const formData = new FormData();
    formData.append('title', title.trim());
    if (description.trim()) {
      formData.append('description', description.trim());
    }
    if (thumbnail) {
      formData.append('thumbnailFile', thumbnail);
    }

    setSubmitting(true);
    try {
      await createAlbum(formData);
      navigate('/admin/albums');
    } catch (err) {
      const msg =
        err instanceof Error ? err.message : '앨범 생성에 실패했습니다.';
      setError(msg);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className={styles.page}>
      <h1 className={styles.pageTitle}>{'새 앨범 만들기'}</h1>

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
            placeholder="앨범 제목"
          />
        </div>

        {/* Description */}
        <div className={styles.field}>
          <label className={styles.label}>{'설명'}</label>
          <textarea
            className={styles.textarea}
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="앨범에 대한 설명 (선택사항)"
          />
        </div>

        {/* Thumbnail */}
        <div className={styles.field}>
          <span className={styles.label}>{'썸네일'}</span>
          <div className={styles.thumbArea}>
            {thumbPreview && (
              <img
                src={thumbPreview}
                alt="썸네일 미리보기"
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
              {thumbnail ? thumbnail.name : '이미지 선택'}
            </label>
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
            {'만들기'}
          </Button>
        </div>
      </form>
    </div>
  );
}
