/** Screen 22: Notice detail */
import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { fetchNotice } from '@/api/notices';
import type { Notice } from '@/types';
import styles from './NoticeDetailPage.module.css';

function formatDate(iso: string): string {
  const d = new Date(iso);
  return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, '0')}.${String(d.getDate()).padStart(2, '0')}`;
}

export default function NoticeDetailPage() {
  const { noticeId } = useParams<{ noticeId: string }>();
  const [notice, setNotice] = useState<Notice | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!noticeId) return;

    setLoading(true);
    setError(null);
    fetchNotice(Number(noticeId))
      .then(setNotice)
      .catch(() => setError('Failed to load notice'))
      .finally(() => setLoading(false));
  }, [noticeId]);

  if (loading) {
    return (
      <div className={styles.page}>
        <div className={styles.loading}>Loading...</div>
      </div>
    );
  }

  if (error || !notice) {
    return (
      <div className={styles.page}>
        <div className={styles.error}>{error ?? 'Notice not found'}</div>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      {/* Breadcrumb */}
      <nav className={styles.breadcrumb}>
        <Link to="/">Home</Link>
        <span>&rsaquo;</span>
        <Link to="/notices">공지사항</Link>
        <span>&rsaquo;</span>
        <span className={styles.breadcrumbCurrent}>{notice.title}</span>
      </nav>

      {/* Header */}
      <header className={styles.header}>
        <div className={styles.headerTop}>
          {notice.isPinned && (
            <span className={styles.pinBadge}>고정</span>
          )}
          <h1 className={styles.title}>{notice.title}</h1>
        </div>
        <div className={styles.meta}>
          <span>{formatDate(notice.createdAt)}</span>
          <span className={styles.metaDivider}>{'\u00B7'}</span>
          <span>조회 {notice.viewCount.toLocaleString()}</span>
        </div>
      </header>

      {/* Content */}
      <article className={styles.content}>
        {notice.content.split('\n').map((line, idx) => (
          <p key={idx}>{line || '\u00A0'}</p>
        ))}
      </article>

      {/* Back */}
      <div className={styles.footer}>
        <Link to="/notices" className={styles.backLink}>
          &larr; 목록으로 돌아가기
        </Link>
      </div>
    </div>
  );
}
