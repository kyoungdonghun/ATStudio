/** Screen K-3: License lookup by user */
import { useState, useCallback, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { fetchUserLicenses, type LicenseListItem } from '@/api/licenses';
import type { PageInfo } from '@/types';
import { formatDate } from '@/utils/format';
import Button from '@/components/ui/Button';
import Pagination from '@/components/ui/Pagination';
import styles from './LicenseManagePage.module.css';

export default function LicenseManagePage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const currentPage = Number(searchParams.get('page')) || 1;
  const activeUserId = searchParams.get('userId') || '';

  const [userIdInput, setUserIdInput] = useState(activeUserId);
  const [licenses, setLicenses] = useState<LicenseListItem[]>([]);
  const [pageInfo, setPageInfo] = useState<PageInfo | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [searched, setSearched] = useState(false);

  const loadLicenses = useCallback(async (userId: string, page: number) => {
    if (!userId) return;
    setLoading(true);
    setError(null);
    setSearched(true);
    try {
      const result = await fetchUserLicenses(Number(userId), page, 20);
      setLicenses(result.dataList);
      setPageInfo(result.pageInfo);
    } catch {
      setError('라이선스 목록을 불러올 수 없습니다.');
      setLicenses([]);
      setPageInfo(null);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (activeUserId) {
      loadLicenses(activeUserId, currentPage);
    }
  }, [activeUserId, currentPage, loadLicenses]);

  const handleSearch = () => {
    const trimmed = userIdInput.trim();
    if (!trimmed) return;
    setSearchParams({ userId: trimmed, page: '1' });
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') handleSearch();
  };

  const handlePageChange = (page: number) => {
    setSearchParams({ userId: activeUserId, page: String(page) });
  };

  return (
    <div className={styles.page}>
      <h1 className={styles.title}>라이선스 관리</h1>

      {/* User ID search */}
      <div className={styles.searchBar}>
        <input
          className={styles.searchInput}
          type="text"
          inputMode="numeric"
          placeholder="사용자 ID를 입력하세요..."
          value={userIdInput}
          onChange={(e) => setUserIdInput(e.target.value.replace(/[^0-9]/g, ''))}
          onKeyDown={handleKeyDown}
        />
        <Button size="sm" onClick={handleSearch}>
          조회
        </Button>
      </div>

      {loading && (
        <div className={styles.loading}>Loading...</div>
      )}

      {error && (
        <div className={styles.error}>{error}</div>
      )}

      {!loading && !error && searched && (
        <>
          <div className={styles.tableWrap}>
            <table className={styles.table}>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>곡명</th>
                  <th>라이선스 코드</th>
                  <th>발급일</th>
                </tr>
              </thead>
              <tbody>
                {licenses.length === 0 ? (
                  <tr>
                    <td colSpan={4} className={styles.empty}>
                      라이선스가 없습니다.
                    </td>
                  </tr>
                ) : (
                  licenses.map((lic) => (
                    <tr key={lic.id} className={styles.row}>
                      <td>{lic.id}</td>
                      <td>{lic.track.title}</td>
                      <td className={styles.code}>{lic.licenseCode}</td>
                      <td>{formatDate(lic.issuedAt)}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>

          {pageInfo && pageInfo.total > pageInfo.size && (
            <Pagination
              pageInfo={pageInfo}
              currentPage={currentPage}
              onPageChange={handlePageChange}
            />
          )}
        </>
      )}
    </div>
  );
}
