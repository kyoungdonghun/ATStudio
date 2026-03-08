/** Screen 21: Notice create (admin) */
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createNotice } from '@/api/notices';
import Button from '@/components/ui/Button';
import styles from './NoticeCreatePage.module.css';

export default function NoticeCreatePage() {
  const navigate = useNavigate();
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [isPinned, setIsPinned] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim() || !content.trim()) return;

    setLoading(true);
    setError(null);
    try {
      await createNotice({
        title: title.trim(),
        content: content.trim(),
        isPinned,
      });
      navigate('/notices');
    } catch {
      setError('Failed to create notice');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.page}>
      <h1 className={styles.title}>Create Notice</h1>

      {error && <div className={styles.error}>{error}</div>}

      <form className={styles.form} onSubmit={handleSubmit}>
        <div className={styles.formGroup}>
          <label className={styles.formLabel}>Title</label>
          <input
            className={styles.formInput}
            placeholder="Notice title"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            required
          />
        </div>

        <div className={styles.formGroup}>
          <label className={styles.formLabel}>Content</label>
          <textarea
            className={styles.formTextarea}
            placeholder="Notice content..."
            value={content}
            onChange={(e) => setContent(e.target.value)}
            required
          />
        </div>

        <div className={styles.formGroup}>
          <div className={styles.checkboxRow}>
            <input
              type="checkbox"
              id="isPinned"
              checked={isPinned}
              onChange={(e) => setIsPinned(e.target.checked)}
            />
            <label htmlFor="isPinned" className={styles.checkboxLabel}>
              Pin this notice
            </label>
          </div>
        </div>

        <div className={styles.formActions}>
          <Button
            variant="ghost"
            type="button"
            onClick={() => navigate(-1)}
          >
            Cancel
          </Button>
          <Button type="submit" loading={loading}>
            Create
          </Button>
        </div>
      </form>
    </div>
  );
}
