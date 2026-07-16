/** Screen 14: Create question */
import { useState, useRef, type FormEvent, type ChangeEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { createQuestion } from '@/api/questions';
import {
  TITLE_QUESTION_MAX,
  ATTACHMENT_MAX_COUNT,
  ATTACHMENT_MAX_SIZE_MB,
  isFileSizeOk,
} from '@/utils/validation';
import Button from '@/components/ui/Button';
import styles from './QuestionCreatePage.module.css';

/* ── Constants ── */

const CATEGORY_OPTIONS = [
  { label: '카테고리 선택', value: '' },
  { label: '다운로드', value: 'DOWNLOAD' },
  { label: '결제', value: 'PAYMENT' },
  { label: '저작권', value: 'COPYRIGHT' },
  { label: '제작', value: 'PRODUCTION' },
  { label: '기타', value: 'OTHER' },
];

function formatFileSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

export default function QuestionCreatePage() {
  const navigate = useNavigate();
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [category, setCategory] = useState('');
  const [isPublic, setIsPublic] = useState(true);
  const [files, setFiles] = useState<File[]>([]);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  function handleFileChange(e: ChangeEvent<HTMLInputElement>) {
    if (!e.target.files) return;
    const added = Array.from(e.target.files);

    // File count check
    const totalCount = files.length + added.length;
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

    setFiles((prev) => [...prev, ...added]);
    // Reset input so same file can be added again if removed
    e.target.value = '';
  }

  function removeFile(index: number) {
    setFiles((prev) => prev.filter((_, i) => i !== index));
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);

    if (!title.trim()) {
      setError('제목을 입력해 주세요.');
      return;
    }
    if (!content.trim()) {
      setError('내용을 입력해 주세요.');
      return;
    }
    if (!category) {
      setError('카테고리를 선택해 주세요.');
      return;
    }

    try {
      setSubmitting(true);
      await createQuestion({
        title: title.trim(),
        content: content.trim(),
        category,
        isPublic,
        attachments: files.length > 0 ? files : undefined,
      });
      navigate('/questions');
    } catch {
      setError('문의 등록에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className={styles.page}>
      <h1 className={styles.pageTitle}>{'새 문의'}</h1>

      <form className={styles.form} onSubmit={handleSubmit}>
        {error && <div className={styles.error}>{error}</div>}

        {/* Title */}
        <div className={styles.field}>
          <label className={styles.label} htmlFor="q-title">
            {'제목'}
          </label>
          <input
            id="q-title"
            className={styles.input}
            type="text"
            maxLength={TITLE_QUESTION_MAX}
            placeholder="문의 제목을 입력하세요"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
          />
          <span className={styles.charCount}>
            {title.length}/{TITLE_QUESTION_MAX}
          </span>
        </div>

        {/* Category */}
        <div className={styles.field}>
          <label className={styles.label} htmlFor="q-category">
            {'카테고리'}
          </label>
          <select
            id="q-category"
            className={styles.select}
            value={category}
            onChange={(e) => setCategory(e.target.value)}
          >
            {CATEGORY_OPTIONS.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
        </div>

        {/* Content */}
        <div className={styles.field}>
          <label className={styles.label} htmlFor="q-content">
            {'내용'}
          </label>
          <textarea
            id="q-content"
            className={styles.textarea}
            placeholder="문의 내용을 입력하세요"
            rows={8}
            value={content}
            onChange={(e) => setContent(e.target.value)}
          />
        </div>

        {/* Public checkbox */}
        <div className={styles.checkField}>
          <label className={styles.checkLabel}>
            <input
              type="checkbox"
              checked={isPublic}
              onChange={(e) => setIsPublic(e.target.checked)}
            />
            <span>{'공개 문의'}</span>
          </label>
          <span className={styles.checkHint}>{'비공개 시 관리자만 열람할 수 있습니다.'}</span>
        </div>

        {/* File attachments */}
        <div className={styles.field}>
          <label className={styles.label}>{'첨부파일'}</label>
          <button
            type="button"
            className={styles.fileBtn}
            onClick={() => fileInputRef.current?.click()}
          >
            {'파일 선택'}
          </button>
          <input
            ref={fileInputRef}
            type="file"
            multiple
            className={styles.fileInput}
            onChange={handleFileChange}
          />
          {files.length > 0 && (
            <ul className={styles.fileList}>
              {files.map((file, idx) => (
                <li key={`${file.name}-${idx}`} className={styles.fileItem}>
                  <span className={styles.fileName}>{file.name}</span>
                  <span className={styles.fileSize}>{formatFileSize(file.size)}</span>
                  <button
                    type="button"
                    className={styles.fileRemove}
                    onClick={() => removeFile(idx)}
                  >
                    {'\u2715'}
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>

        {/* Actions */}
        <div className={styles.actions}>
          <Button variant="ghost" type="button" onClick={() => navigate(-1)}>
            {'취소'}
          </Button>
          <Button variant="primary" type="submit" loading={submitting}>
            {'등록'}
          </Button>
        </div>
      </form>
    </div>
  );
}
