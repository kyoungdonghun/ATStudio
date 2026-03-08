/** Screen 21-2: Notice edit (admin) */
import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { fetchNotice, updateNotice, deleteNotice } from '@/api/notices';
import Modal from '@/components/ui/Modal';
import Button from '@/components/ui/Button';
import styles from './NoticeEditPage.module.css';

export default function NoticeEditPage() {
  const { noticeId } = useParams<{ noticeId: string }>();
  const navigate = useNavigate();

  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [isPinned, setIsPinned] = useState(false);

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  /* Delete modal */
  const [deleteOpen, setDeleteOpen] = useState(false);
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    if (!noticeId) return;
    setLoading(true);
    setError(null);
    fetchNotice(Number(noticeId))
      .then((notice) => {
        setTitle(notice.title);
        setContent(notice.content);
        setIsPinned(notice.isPinned);
      })
      .catch(() => setError('Failed to load notice'))
      .finally(() => setLoading(false));
  }, [noticeId]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!noticeId || !title.trim() || !content.trim()) return;

    setSaving(true);
    setError(null);
    try {
      await updateNotice(Number(noticeId), {
        title: title.trim(),
        content: content.trim(),
        isPinned,
      });
      navigate(`/notices/${noticeId}`);
    } catch {
      setError('Failed to update notice');
    } finally {
      setSaving(false);
    }
  };

  const confirmDelete = async () => {
    if (!noticeId) return;
    setDeleting(true);
    try {
      await deleteNotice(Number(noticeId));
      navigate('/notices');
    } catch {
      setError('Failed to delete notice');
    } finally {
      setDeleting(false);
      setDeleteOpen(false);
    }
  };

  if (loading) {
    return (
      <div className={styles.page}>
        <div className={styles.loading}>Loading...</div>
      </div>
    );
  }

  if (error && !title) {
    return (
      <div className={styles.page}>
        <div className={styles.error}>{error}</div>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <h1 className={styles.title}>Edit Notice</h1>

      {error && <div className={styles.error}>{error}</div>}

      <form className={styles.form} onSubmit={handleSubmit}>
        <div className={styles.formGroup}>
          <label className={styles.formLabel}>Title</label>
          <input
            className={styles.formInput}
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            required
          />
        </div>

        <div className={styles.formGroup}>
          <label className={styles.formLabel}>Content</label>
          <textarea
            className={styles.formTextarea}
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
            variant="danger"
            type="button"
            onClick={() => setDeleteOpen(true)}
          >
            Delete
          </Button>
          <div className={styles.formActionsRight}>
            <Button
              variant="ghost"
              type="button"
              onClick={() => navigate(-1)}
            >
              Cancel
            </Button>
            <Button type="submit" loading={saving}>
              Save
            </Button>
          </div>
        </div>
      </form>

      {/* Delete confirm modal */}
      <Modal
        open={deleteOpen}
        onClose={() => setDeleteOpen(false)}
        title="Delete Notice"
      >
        <div className={styles.deleteText}>
          Are you sure you want to delete this notice? This action cannot be
          undone.
        </div>
        <div className={styles.modalActions}>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => setDeleteOpen(false)}
          >
            Cancel
          </Button>
          <Button
            variant="danger"
            size="sm"
            loading={deleting}
            onClick={confirmDelete}
          >
            Delete
          </Button>
        </div>
      </Modal>
    </div>
  );
}
