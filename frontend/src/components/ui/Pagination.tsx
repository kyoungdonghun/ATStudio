import type { PageInfo } from '@/types';
import styles from './Pagination.module.css';

interface PaginationProps {
  pageInfo: PageInfo;
  currentPage: number;
  onPageChange: (page: number) => void;
}

function buildPageRange(pageInfo: PageInfo): (number | 'ellipsis')[] {
  const { start, end } = pageInfo;
  const totalPages = Math.ceil(pageInfo.total / pageInfo.size);
  const result: (number | 'ellipsis')[] = [];

  for (let i = start; i <= end; i++) {
    result.push(i);
  }

  if (end < totalPages) {
    result.push('ellipsis');
    result.push(totalPages);
  }

  return result;
}

export default function Pagination({ pageInfo, currentPage, onPageChange }: PaginationProps) {
  if (pageInfo.total === 0) return null;

  return (
    <nav className={styles.pagination} aria-label="페이지 탐색">
      <button
        type="button"
        className={styles.btn}
        disabled={!pageInfo.prev}
        onClick={() => onPageChange(currentPage - 1)}
        aria-label="이전 페이지"
        title="이전 페이지"
      >
        {'\u2039'}
      </button>

      {buildPageRange(pageInfo).map((item, idx) =>
        item === 'ellipsis' ? (
          <span key={`e-${idx}`} className={styles.ellipsis}>
            {'\u2026'}
          </span>
        ) : (
          <button
            type="button"
            key={item}
            className={`${styles.btn} ${item === currentPage ? styles.btnActive : ''}`}
            onClick={() => onPageChange(item)}
            aria-label={`${item}페이지`}
            aria-current={item === currentPage ? 'page' : undefined}
          >
            {item}
          </button>
        ),
      )}

      <button
        type="button"
        className={styles.btn}
        disabled={!pageInfo.next}
        onClick={() => onPageChange(currentPage + 1)}
        aria-label="다음 페이지"
        title="다음 페이지"
      >
        {'\u203A'}
      </button>
    </nav>
  );
}
