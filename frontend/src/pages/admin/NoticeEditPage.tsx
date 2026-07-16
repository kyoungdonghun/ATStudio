/** Screen 21-2: Notice edit (admin) */
import { useEffect, useState, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { fetchNotice, updateNotice, deleteNotice } from '@/api/notices';
import type { NoticeAttachmentInfo } from '@/types';
import {
  TITLE_NOTICE_MAX,
  ATTACHMENT_MAX_COUNT,
  ATTACHMENT_MAX_SIZE_MB,
  isFileSizeOk,
} from '@/utils/validation';
import Modal from '@/components/ui/Modal';
import Button from '@/components/ui/Button';
import styles from './NoticeEditPage.module.css';

function formatFileSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

export default function NoticeEditPage() {
  const { noticeId } = useParams<{ noticeId: string }>();
  const navigate = useNavigate();

  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [isPinned, setIsPinned] = useState(false);

  /* Existing attachments */
  const [existingAttachments, setExistingAttachments] = useState<NoticeAttachmentInfo[]>([]);
  const [deleteAttachmentIds, setDeleteAttachmentIds] = useState<number[]>([]);

  /* New attachments */
  const [newFiles, setNewFiles] = useState<File[]>([]);
  const fileInputRef = useRef<HTMLInputElement>(null);

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
        setExistingAttachments(notice.attachments ?? []);
      })
      .catch(() => setError('Failed to load notice'))
      .finally(() => setLoading(false));
  }, [noticeId]);

  function handleFileChange(e: React.ChangeEvent<HTMLInputElement>) {
    if (!e.target.files) return;
    const added = Array.from(e.target.files);

    // File count check (existing + new)
    const totalCount = existingAttachments.length + newFiles.length + added.length;
    if (totalCount > ATTACHMENT_MAX_COUNT) {
      setError(`첨부파일은 최대 ${ATTACHMENT_MAX_COUNT}개까지 첨부할 수 있습니다.`);
      e.target.value = '';
      return;
    }

    // File size check
    const oversized = added.filter((f) => !isFileSizeOk(f, ATTACHMENT_MAX_SIZE_MB));
    if (oversized.length > 0) {
      setError(
        `첨부파일은 ${ATTACHMENT_MAX_SIZE_MB}MB 이하만 업로드할 수 있습니다. (초과: ${oversized.map((f) => f.name).join(', ')})`,
      );
      e.target.value = '';
      return;
    }

    setNewFiles((prev) => [...prev, ...added]);
    e.target.value = '';
  }

  function removeExistingAttachment(attachmentId: number) {
    setDeleteAttachmentIds((prev) => [...prev, attachmentId]);
    setExistingAttachments((prev) => prev.filter((a) => a.id !== attachmentId));
  }

  function removeNewFile(index: number) {
    setNewFiles((prev) => prev.filter((_, i) => i !== index));
  }

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
        deleteAttachmentIds: deleteAttachmentIds.length > 0 ? deleteAttachmentIds : undefined,
        newAttachments: newFiles.length > 0 ? newFiles : undefined,
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
            maxLength={TITLE_NOTICE_MAX}
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

        {/* Attachments */}
        <div className={styles.formGroup}>
          <label className={styles.formLabel}>Attachments</label>
          <label className={styles.fileLabel}>
            {'+ Add files'}
            <input
              ref={fileInputRef}
              type="file"
              multiple
              className={styles.fileHidden}
              onChange={handleFileChange}
            />
          </label>

          {(existingAttachments.length > 0 || newFiles.length > 0) && (
            <ul className={styles.fileList}>
              {existingAttachments.map((att) => (
                <li key={`existing-${att.id}`} className={styles.fileItem}>
                  <span className={styles.existingBadge}>existing</span>
                  <span className={styles.fileName}>{att.originalName}</span>
                  <span className={styles.fileSize}>{formatFileSize(att.fileSize)}</span>
                  <button
                    type="button"
                    className={styles.fileRemove}
                    onClick={() => removeExistingAttachment(att.id)}
                  >
                    {'\u2715'}
                  </button>
                </li>
              ))}
              {newFiles.map((file, i) => (
                <li key={`new-${i}`} className={styles.fileItem}>
                  <span className={styles.fileName}>{file.name}</span>
                  <span className={styles.fileSize}>{formatFileSize(file.size)}</span>
                  <button
                    type="button"
                    className={styles.fileRemove}
                    onClick={() => removeNewFile(i)}
                  >
                    {'\u2715'}
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>

        <div className={styles.formActions}>
          <Button variant="danger" type="button" onClick={() => setDeleteOpen(true)}>
            Delete
          </Button>
          <div className={styles.formActionsRight}>
            <Button variant="ghost" type="button" onClick={() => navigate(-1)}>
              Cancel
            </Button>
            <Button type="submit" loading={saving}>
              Save
            </Button>
          </div>
        </div>
      </form>

      {/* Delete confirm modal */}
      <Modal open={deleteOpen} onClose={() => setDeleteOpen(false)} title="Delete Notice">
        <div className={styles.deleteText}>
          Are you sure you want to delete this notice? This action cannot be undone.
        </div>
        <div className={styles.modalActions}>
          <Button variant="ghost" size="sm" onClick={() => setDeleteOpen(false)}>
            Cancel
          </Button>
          <Button variant="danger" size="sm" loading={deleting} onClick={confirmDelete}>
            Delete
          </Button>
        </div>
      </Modal>
    </div>
  );
}
