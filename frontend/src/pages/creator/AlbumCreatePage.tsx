import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { createAlbum } from '@/api/albums';
import { TITLE_ALBUM_MAX, DESCRIPTION_MAX } from '@/utils/validation';
import Button from '@/components/ui/Button';
import AlbumThumbnailField from './AlbumThumbnailField';
import { emptyAlbumThumbnailSelection, isAlbumThumbnailBlocked } from './albumThumbnail';
import styles from './AlbumCreatePage.module.css';

/** Screen L-4: Album create */
export default function AlbumCreatePage() {
  const navigate = useNavigate();

  /* ── Form state ── */
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [thumbnail, setThumbnail] = useState(emptyAlbumThumbnailSelection);

  /* ── UI state ── */
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  /* ── Submit ── */
  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);

    if (isAlbumThumbnailBlocked(thumbnail)) return;

    if (!title.trim()) {
      setError('앨범 제목을 입력해주세요.');
      return;
    }

    const formData = new FormData();
    formData.append('title', title.trim());
    if (description.trim()) {
      formData.append('description', description.trim());
    }
    if (thumbnail.file) {
      formData.append('thumbnailFile', thumbnail.file);
    }

    setSubmitting(true);
    try {
      await createAlbum(formData);
      navigate('/admin/albums');
    } catch (err) {
      const msg = err instanceof Error ? err.message : '앨범 생성에 실패했습니다.';
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
            maxLength={TITLE_ALBUM_MAX}
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
            maxLength={DESCRIPTION_MAX}
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="앨범에 대한 설명 (선택사항)"
          />
        </div>

        <AlbumThumbnailField value={thumbnail} onChange={setThumbnail} disabled={submitting} />

        {/* Actions */}
        <div className={styles.actions}>
          <Button variant="ghost" type="button" onClick={() => navigate(-1)} disabled={submitting}>
            {'취소'}
          </Button>
          <Button type="submit" loading={submitting} disabled={isAlbumThumbnailBlocked(thumbnail)}>
            {'만들기'}
          </Button>
        </div>
      </form>
    </div>
  );
}
