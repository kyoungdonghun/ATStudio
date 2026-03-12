import type { ReactNode } from 'react';
import styles from './DataTable.module.css';

export interface Column<T> {
  key: string;
  label: string;
  align?: 'left' | 'center' | 'right';
  width?: number | string;
  className?: string;
  thClassName?: string;
  render: (row: T, index: number) => ReactNode;
}

interface DataTableProps<T> {
  columns: Column<T>[];
  data: T[];
  rowKey: (row: T, index: number) => string | number;
  loading?: boolean;
  error?: string | null;
  emptyText?: string;
  rowClassName?: (row: T, index: number) => string;
  onRowClick?: (row: T, index: number) => void;
  className?: string;
}

export default function DataTable<T>({
  columns,
  data,
  rowKey,
  loading = false,
  error = null,
  emptyText = '데이터가 없습니다.',
  rowClassName,
  onRowClick,
  className,
}: DataTableProps<T>) {
  if (loading) {
    return <div className={styles.loading}>{'불러오는 중...'}</div>;
  }

  if (error) {
    return <div className={styles.error}>{error}</div>;
  }

  if (data.length === 0) {
    return <div className={styles.empty}>{emptyText}</div>;
  }

  const alignClass = (align?: string) => {
    if (align === 'center') return styles.thCenter;
    if (align === 'right') return styles.thRight;
    return '';
  };

  return (
    <div className={styles.tableWrap}>
      <table className={`${styles.table} ${className ?? ''}`}>
        <thead>
          <tr>
            {columns.map((col) => (
              <th
                key={col.key}
                className={`${alignClass(col.align)} ${col.thClassName ?? ''}`}
                style={col.width ? { width: typeof col.width === 'number' ? `${col.width}px` : col.width } : undefined}
              >
                {col.label}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {data.map((row, idx) => (
            <tr
              key={rowKey(row, idx)}
              className={`${styles.row} ${rowClassName?.(row, idx) ?? ''}`}
              onClick={onRowClick ? () => onRowClick(row, idx) : undefined}
              style={onRowClick ? { cursor: 'pointer' } : undefined}
            >
              {columns.map((col) => (
                <td
                  key={col.key}
                  className={col.className ?? ''}
                  style={col.align ? { textAlign: col.align } : undefined}
                >
                  {col.render(row, idx)}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
