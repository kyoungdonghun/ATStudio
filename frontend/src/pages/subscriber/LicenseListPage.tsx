import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { fetchMyLicenses, type LicenseListItem } from '@/api/licenses';
import type { PageInfo } from '@/types';
import Pagination from '@/components/ui/Pagination';
import styles from './LicenseListPage.module.css';

const PAGE_SIZE = 20;

/** Format ISO date to YYYY-MM-DD */
function formatDate(iso: string): string {
  return iso.slice(0, 10);
}

/** Truncate license code for display */
function truncateCode(code: string): string {
  return code.length > 16 ? `${code.slice(0, 16)}...` : code;
}

export default function LicenseListPage() {
  /* ── State ── */
  const [licenses, setLicenses] = useState<LicenseListItem[]>([]);
  const [pageInfo, setPageInfo] = useState<PageInfo | null>(null);
  const [currentPage, setCurrentPage] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  /* ── Fetch ── */
  const load = useCallback(async (page: number) => {
    try {
      setLoading(true);
      setError(null);
      const res = await fetchMyLicenses(page, PAGE_SIZE);
      setLicenses(res.dataList);
      setPageInfo(res.pageInfo);
    } catch (err) {
      setError(
        err instanceof Error ? err.message : '라이선스를 불러오지 못했습니다.',
      );
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load(currentPage);
  }, [load, currentPage]);

  /* ── Render ── */
  return (
    <div className={styles.page}>
      {/* Header */}
      <div className={styles.pageHeader}>
        <div className={styles.pageTitle}>
          {'내 라이선스'}
          {pageInfo && (
            <span className={styles.pageTitleCount}>
              {'총 '}
              {pageInfo.total}건
            </span>
          )}
        </div>
      </div>

      {/* Content */}
      {loading ? (
        <div className={styles.loading}>{'라이선스를 불러오는 중...'}</div>
      ) : error ? (
        <div className={styles.error}>{error}</div>
      ) : licenses.length === 0 ? (
        <div className={styles.empty}>
          {'보유한 라이선스가 없습니다. 음원을 다운로드하면 자동으로 발급됩니다.'}
        </div>
      ) : (
        <>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>{'곡명'}</th>
                <th>{'라이선스 코드'}</th>
                <th>{'발급일'}</th>
                <th className={styles.cellActions} />
              </tr>
            </thead>
            <tbody>
              {licenses.map((lic) => (
                <tr key={lic.id}>
                  <td className={styles.cellTitle}>{lic.track.title}</td>
                  <td className={styles.cellCode}>
                    {truncateCode(lic.licenseCode)}
                  </td>
                  <td className={styles.cellDate}>
                    {formatDate(lic.issuedAt)}
                  </td>
                  <td className={styles.cellActions}>
                    <Link
                      to={`/licenses/${lic.id}`}
                      className={styles.detailLink}
                    >
                      {'상세'}
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {pageInfo && (
            <Pagination
              pageInfo={pageInfo}
              currentPage={currentPage}
              onPageChange={(p) => {
                setCurrentPage(p);
                window.scrollTo({ top: 0, behavior: 'smooth' });
              }}
            />
          )}
        </>
      )}
    </div>
  );
}
