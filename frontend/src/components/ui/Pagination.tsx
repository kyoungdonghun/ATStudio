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
    <div className={styles.pagination}>
      <button
        className={styles.btn}
        disabled={!pageInfo.prev}
        onClick={() => onPageChange(currentPage - 1)}
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
            key={item}
            className={`${styles.btn} ${item === currentPage ? styles.btnActive : ''}`}
            onClick={() => onPageChange(item)}
          >
            {item}
          </button>
        ),
      )}

      <button
        className={styles.btn}
        disabled={!pageInfo.next}
        onClick={() => onPageChange(currentPage + 1)}
      >
        {'\u203A'}
      </button>
    </div>
  );
}
