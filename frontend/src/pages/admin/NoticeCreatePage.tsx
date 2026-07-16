/** Screen 21: Notice create (admin) */
import { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { createNotice } from '@/api/notices';
import {
  TITLE_NOTICE_MAX,
  ATTACHMENT_MAX_COUNT,
  ATTACHMENT_MAX_SIZE_MB,
  isFileSizeOk,
} from '@/utils/validation';
import Button from '@/components/ui/Button';
import styles from './NoticeCreatePage.module.css';

function formatFileSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

export default function NoticeCreatePage() {
  const navigate = useNavigate();
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [isPinned, setIsPinned] = useState(false);
  const [files, setFiles] = useState<File[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fileInputRef = useRef<HTMLInputElement>(null);

  function handleFileChange(e: React.ChangeEvent<HTMLInputElement>) {
    const selected = e.target.files;
    if (!selected) return;
    const added = Array.from(selected);

    // File count check
    if (files.length + added.length > ATTACHMENT_MAX_COUNT) {
      setError(`첨부파일은 최대 ${ATTACHMENT_MAX_COUNT}개까지 첨부할 수 있습니다.`);
      if (fileInputRef.current) fileInputRef.current.value = '';
      return;
    }

    // File size check
    const oversized = added.filter((f) => !isFileSizeOk(f, ATTACHMENT_MAX_SIZE_MB));
    if (oversized.length > 0) {
      setError(
        `첨부파일은 ${ATTACHMENT_MAX_SIZE_MB}MB 이하만 업로드할 수 있습니다. (초과: ${oversized.map((f) => f.name).join(', ')})`,
      );
      if (fileInputRef.current) fileInputRef.current.value = '';
      return;
    }

    setFiles((prev) => [...prev, ...added]);
    // Reset so same file can be re-selected
    if (fileInputRef.current) fileInputRef.current.value = '';
  }

  function removeFile(idx: number) {
    setFiles((prev) => prev.filter((_, i) => i !== idx));
  }

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
        attachments: files.length > 0 ? files : undefined,
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

        {/* Attachments */}
        <div className={styles.formGroup}>
          <label className={styles.formLabel}>{'첨부파일'}</label>
          <label className={styles.fileLabel}>
            <input
              ref={fileInputRef}
              type="file"
              multiple
              className={styles.fileHidden}
              onChange={handleFileChange}
            />
            {'파일 선택'}
          </label>
          {files.length > 0 && (
            <ul className={styles.fileList}>
              {files.map((file, idx) => (
                <li key={idx} className={styles.fileItem}>
                  <span className={styles.fileName}>{file.name}</span>
                  <span className={styles.fileSize}>{formatFileSize(file.size)}</span>
                  <button
                    type="button"
                    className={styles.fileRemove}
                    onClick={() => removeFile(idx)}
                  >
                    {'×'}
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>

        <div className={styles.formActions}>
          <Button variant="ghost" type="button" onClick={() => navigate(-1)}>
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
