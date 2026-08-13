/** Screen K-3: License management (admin) */
import { useCallback, useEffect, useRef, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { fetchUserLicenses, type LicenseListItem } from '@/api/licenses';
import {
  fetchUserDetail,
  fetchUsers,
  type AdminUserDetail,
  type AdminUserListItem,
} from '@/api/admin';
import type { PageInfo } from '@/types';
import { formatDate } from '@/utils/format';
import { parsePositiveDecimalRouteID } from '@/utils/routeId';
import Button from '@/components/ui/Button';
import Pagination from '@/components/ui/Pagination';
import styles from './LicenseManagePage.module.css';

export default function LicenseManagePage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const currentPage = parsePositiveDecimalRouteID(searchParams.get('page') ?? undefined) ?? 1;
  const rawActiveUserID = searchParams.get('userId');
  const activeUserID = parsePositiveDecimalRouteID(rawActiveUserID ?? undefined);
  const searchContextKey = `${rawActiveUserID ?? ''}:${currentPage}`;

  const [keyword, setKeyword] = useState('');
  const [searchResults, setSearchResults] = useState<AdminUserListItem[]>([]);
  const [showResults, setShowResults] = useState(false);
  const [selectedUser, setSelectedUser] = useState<AdminUserDetail | null>(null);
  const [licenses, setLicenses] = useState<LicenseListItem[]>([]);
  const [pageInfo, setPageInfo] = useState<PageInfo | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const listGenerationRef = useRef(0);
  const searchGenerationRef = useRef(0);
  const searchControllerRef = useRef<AbortController | null>(null);
  const keywordRef = useRef('');
  const searchContextKeyRef = useRef(searchContextKey);
  searchContextKeyRef.current = searchContextKey;

  const retireSearch = useCallback(() => {
    searchControllerRef.current?.abort();
    searchControllerRef.current = null;
    searchGenerationRef.current += 1;
    setSearchResults([]);
    setShowResults(false);
  }, []);

  useEffect(() => {
    retireSearch();
    const generation = ++listGenerationRef.current;
    const controller = new AbortController();
    setSelectedUser(null);
    setLicenses([]);
    setPageInfo(null);
    setError(null);

    if (activeUserID === null) {
      setLoading(false);
      if (rawActiveUserID) setError('올바른 사용자를 선택해 주세요.');
      return () => controller.abort();
    }

    setLoading(true);
    void Promise.all([
      fetchUserDetail(activeUserID, controller.signal),
      fetchUserLicenses(activeUserID, currentPage, 20, controller.signal),
    ])
      .then(([user, result]) => {
        if (controller.signal.aborted || listGenerationRef.current !== generation) return;
        setSelectedUser(user);
        keywordRef.current = user.email;
        setKeyword(user.email);
        setLicenses(result.dataList);
        setPageInfo(result.pageInfo);
      })
      .catch(() => {
        if (controller.signal.aborted || listGenerationRef.current !== generation) return;
        setError('사용자 또는 라이선스 목록을 불러올 수 없습니다.');
      })
      .finally(() => {
        if (listGenerationRef.current === generation) setLoading(false);
      });

    return () => {
      controller.abort();
      if (listGenerationRef.current === generation) listGenerationRef.current += 1;
    };
  }, [activeUserID, currentPage, rawActiveUserID, retireSearch]);

  useEffect(
    () => () => {
      searchControllerRef.current?.abort();
      searchGenerationRef.current += 1;
    },
    [],
  );

  const handleSearch = async () => {
    const trimmed = keyword.trim();
    if (!trimmed) return;

    searchControllerRef.current?.abort();
    const controller = new AbortController();
    const generation = ++searchGenerationRef.current;
    const submittedContextKey = searchContextKey;
    searchControllerRef.current = controller;
    const isCurrentSearch = () =>
      !controller.signal.aborted &&
      searchGenerationRef.current === generation &&
      keywordRef.current.trim() === trimmed &&
      searchContextKeyRef.current === submittedContextKey;
    try {
      const result = await fetchUsers({ keyword: trimmed, page: 1, size: 10 }, controller.signal);
      if (!isCurrentSearch()) return;
      setSearchResults(result.dataList);
      setShowResults(true);
    } catch {
      if (!isCurrentSearch()) return;
      setSearchResults([]);
      setShowResults(true);
    } finally {
      if (searchControllerRef.current === controller) searchControllerRef.current = null;
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') handleSearch();
  };

  const selectUser = (user: AdminUserListItem) => {
    retireSearch();
    keywordRef.current = user.email;
    setKeyword(user.email);
    setSearchParams({ userId: String(user.id), page: '1' });
  };

  const handlePageChange = (page: number) => {
    if (activeUserID === null) return;
    setSearchParams({ userId: String(activeUserID), page: String(page) });
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
              keywordRef.current = e.target.value;
              setKeyword(e.target.value);
              retireSearch();
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

      {loading && <div className={styles.loading}>Loading...</div>}

      {error && <div className={styles.error}>{error}</div>}

      {!loading && !error && activeUserID !== null && (
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
