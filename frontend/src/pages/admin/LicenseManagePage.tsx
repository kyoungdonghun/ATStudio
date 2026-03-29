/** Screen K-3: License management (admin) */
import { useState, useCallback, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { fetchUserLicenses, type LicenseListItem } from '@/api/licenses';
import { fetchUsers } from '@/api/admin';
import type { PageInfo, User } from '@/types';
import { formatDate } from '@/utils/format';
import Button from '@/components/ui/Button';
import Pagination from '@/components/ui/Pagination';
import styles from './LicenseManagePage.module.css';

export default function LicenseManagePage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const currentPage = Number(searchParams.get('page')) || 1;
  const activeUserId = searchParams.get('userId') || '';

  const [keyword, setKeyword] = useState('');
  const [searchResults, setSearchResults] = useState<User[]>([]);
  const [showResults, setShowResults] = useState(false);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [licenses, setLicenses] = useState<LicenseListItem[]>([]);
  const [pageInfo, setPageInfo] = useState<PageInfo | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadLicenses = useCallback(async (userId: string, page: number) => {
    if (!userId) return;
    setLoading(true);
    setError(null);
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

  const handleSearch = async () => {
    const trimmed = keyword.trim();
    if (!trimmed) return;

    try {
      const result = await fetchUsers({ keyword: trimmed, page: 1, size: 10 });
      setSearchResults(result.dataList);
      setShowResults(true);
    } catch {
      setSearchResults([]);
      setShowResults(true);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') handleSearch();
  };

  const selectUser = (user: User) => {
    setSelectedUser(user);
    setShowResults(false);
    setKeyword(user.email);
    setSearchParams({ userId: String(user.id), page: '1' });
  };

  const handlePageChange = (page: number) => {
    setSearchParams({ userId: activeUserId, page: String(page) });
  };

  return (
    <div className={styles.page}>
      <h1 className={styles.title}>라이선스 관리</h1>

      {/* User search */}
      <div className={styles.searchBar}>
        <div className={styles.searchGroup}>
          <input
            className={styles.searchInput}
            type="text"
            placeholder="이메일 또는 닉네임으로 사용자 검색..."
            value={keyword}
            onChange={(e) => {
              setKeyword(e.target.value);
              setShowResults(false);
            }}
            onKeyDown={handleKeyDown}
          />
          <Button size="sm" onClick={handleSearch}>
            검색
          </Button>
        </div>

        {/* Search results dropdown */}
        {showResults && (
          <div className={styles.dropdown}>
            {searchResults.length === 0 ? (
              <div className={styles.dropdownEmpty}>검색 결과가 없습니다.</div>
            ) : (
              searchResults.map((user) => (
                <button
                  key={user.id}
                  className={styles.dropdownItem}
                  onClick={() => selectUser(user)}
                >
                  <span className={styles.dropdownNickname}>{user.nickname}</span>
                  <span className={styles.dropdownEmail}>{user.email}</span>
                  <span className={styles.dropdownRole}>{user.role}</span>
                </button>
              ))
            )}
          </div>
        )}
      </div>

      {/* Selected user info */}
      {selectedUser && (
        <div className={styles.selectedUser}>
          <span>{selectedUser.nickname}</span>
          <span className={styles.selectedEmail}>{selectedUser.email}</span>
          <span className={styles.selectedId}>ID: {selectedUser.id}</span>
        </div>
      )}

      {loading && (
        <div className={styles.loading}>Loading...</div>
      )}

      {error && (
        <div className={styles.error}>{error}</div>
      )}

      {!loading && !error && activeUserId && (
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
