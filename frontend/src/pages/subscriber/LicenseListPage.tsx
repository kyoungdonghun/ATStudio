import { useState, useEffect, useRef } from 'react';
import { fetchMyLicenses, type LicenseListItem } from '@/api/licenses';
import { classifyLoadError } from '@/api/loadError';
import { downloadTrack, triggerBlobDownload } from '@/api/downloads';
import { formatDate } from '@/utils/format';
import { useToastStore } from '@/store/toastStore';
import { useAuthStore } from '@/store/authStore';
import type { PageInfo } from '@/types';
import Pagination from '@/components/ui/Pagination';
import Modal from '@/components/ui/Modal';
import { createOwnerKey, createReadKey, getCurrentOwnerKey } from '@/utils/ownerProjection';
import styles from './LicenseListPage.module.css';

const PAGE_SIZE = 20;

/** Truncate license code for display */
function truncateCode(code: string): string {
  return code.length > 16 ? `${code.slice(0, 16)}...` : code;
}

export default function LicenseListPage() {
  const toast = useToastStore((s) => s.show);
  const userID = useAuthStore((s) => s.user?.id ?? null);
  const accessToken = useAuthStore((s) => s.accessToken);
  const requestGeneration = useRef(0);
  const ownerKey = createOwnerKey(userID, accessToken);

  /* ── State ── */
  const [licenses, setLicenses] = useState<LicenseListItem[]>([]);
  const [pageInfo, setPageInfo] = useState<PageInfo | null>(null);
  const [currentPage, setCurrentPage] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [modalLicense, setModalLicense] = useState<LicenseListItem | null>(null);
  const readKey = createReadKey(ownerKey, 'license-list', currentPage);
  const currentReadKeyRef = useRef(readKey);
  const projectionKeyRef = useRef<string | null>(null);
  const [projectionKey, setProjectionKey] = useState<string | null>(null);
  const [errorKey, setErrorKey] = useState<string | null>(null);
  currentReadKeyRef.current = readKey;

  const projectionCurrent = readKey !== null && projectionKey === readKey;
  const currentLicenses = projectionCurrent ? licenses : [];
  const currentPageInfo = projectionCurrent ? pageInfo : null;
  const currentModalLicense = projectionCurrent ? modalLicense : null;
  const currentError = errorKey === readKey ? error : null;
  const currentLoading = loading || (!projectionCurrent && currentError === null);

  function isCurrentProjection(expectedReadKey = readKey): boolean {
    return (
      expectedReadKey !== null &&
      currentReadKeyRef.current === expectedReadKey &&
      projectionKeyRef.current === expectedReadKey &&
      getCurrentOwnerKey(ownerKey) === ownerKey
    );
  }

  /* ── Fetch ── */
  useEffect(() => {
    const requestKey = readKey;
    const requestOwnerKey = ownerKey;
    const generation = ++requestGeneration.current;
    const controller = new AbortController();
    const isCurrent = () =>
      requestKey !== null &&
      generation === requestGeneration.current &&
      currentReadKeyRef.current === requestKey &&
      getCurrentOwnerKey(requestOwnerKey) === requestOwnerKey;

    setLicenses([]);
    setPageInfo(null);
    setModalLicense(null);
    setLoading(true);
    setError(null);
    void fetchMyLicenses(currentPage, PAGE_SIZE, controller.signal)
      .then((res) => {
        if (!isCurrent()) return;
        setLicenses(res.dataList);
        setPageInfo(res.pageInfo);
        projectionKeyRef.current = requestKey;
        setProjectionKey(requestKey);
      })
      .catch((loadError: unknown) => {
        if (isCurrent() && classifyLoadError(loadError) !== 'cancelled') {
          setError('라이선스를 불러오지 못했습니다.');
          setErrorKey(requestKey);
        }
      })
      .finally(() => {
        if (isCurrent()) setLoading(false);
      });

    return () => {
      controller.abort();
      if (requestGeneration.current === generation) requestGeneration.current += 1;
    };
  }, [currentPage, ownerKey, readKey]);

  /* ── Re-download handler ── */
  async function handleRedownload(lic: LicenseListItem) {
    const operationKey = readKey;
    if (!isCurrentProjection(operationKey) || !currentLicenses.some((item) => item.id === lic.id)) {
      return;
    }
    try {
      const blob = await downloadTrack(lic.track.id);
      if (!isCurrentProjection(operationKey)) return;
      triggerBlobDownload(blob, `${lic.track.title}.mp3`);
      toast('success', '다운로드를 시작합니다.');
    } catch {
      if (!isCurrentProjection(operationKey)) return;
      toast('error', '다운로드에 실패했습니다. 구독이 활성 상태인지 확인하세요.');
    }
  }

  /* ── Copy license code ── */
  async function copyCode(code: string) {
    const operationKey = readKey;
    if (
      !isCurrentProjection(operationKey) ||
      !currentLicenses.some((item) => item.licenseCode === code)
    ) {
      return;
    }
    try {
      await navigator.clipboard.writeText(code);
      if (!isCurrentProjection(operationKey)) return;
      toast('success', '라이선스 코드가 복사되었습니다.');
    } catch {
      if (!isCurrentProjection(operationKey)) return;
      toast('error', '복사에 실패했습니다.');
    }
  }

  /* ── Render ── */
  return (
    <div className={styles.page}>
      {/* Header */}
      <div className={styles.pageHeader}>
        <div className={styles.pageTitle}>
          {'내 라이선스'}
          {currentPageInfo && (
            <span className={styles.pageTitleCount}>
              {'총 '}
              {currentPageInfo.total}건
            </span>
          )}
        </div>
      </div>

      {/* Content */}
      {currentLoading ? (
        <div className={styles.loading}>{'라이선스를 불러오는 중...'}</div>
      ) : currentError ? (
        <div className={styles.error}>{currentError}</div>
      ) : currentLicenses.length === 0 ? (
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
              {currentLicenses.map((lic) => (
                <tr key={lic.id}>
                  <td className={styles.cellTitle}>{lic.track.title}</td>
                  <td className={styles.cellCode}>{truncateCode(lic.licenseCode)}</td>
                  <td className={styles.cellDate}>{formatDate(lic.issuedAt)}</td>
                  <td className={styles.cellActions}>
                    <button
                      className={styles.dlBtn}
                      onClick={() => handleRedownload(lic)}
                      title="다시 다운로드"
                    >
                      {'↓ 다운로드'}
                    </button>
                    <button className={styles.detailLink} onClick={() => setModalLicense(lic)}>
                      {'상세'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {currentPageInfo && (
            <Pagination
              pageInfo={currentPageInfo}
              currentPage={currentPage}
              onPageChange={(p) => {
                setCurrentPage(p);
                window.scrollTo({ top: 0, behavior: 'smooth' });
              }}
            />
          )}
        </>
      )}

      {/* License Detail Modal */}
      <Modal
        open={currentModalLicense !== null}
        onClose={() => setModalLicense(null)}
        title="라이선스 상세"
      >
        {currentModalLicense && (
          <>
            <div className={styles.modalBody}>
              <div className={styles.modalRow}>
                <span className={styles.modalLabel}>{'곡명'}</span>
                <span className={styles.modalValue}>{currentModalLicense.track.title}</span>
              </div>
              <div className={styles.modalRow}>
                <span className={styles.modalLabel}>{'발급일'}</span>
                <span className={styles.modalValue}>
                  {formatDate(currentModalLicense.issuedAt)}
                </span>
              </div>
              <div className={styles.modalRow}>
                <span className={styles.modalLabel}>{'라이선스 코드'}</span>
              </div>
              <div className={styles.codeBox}>{currentModalLicense.licenseCode}</div>
              <button
                className={styles.copyBtn}
                onClick={() => copyCode(currentModalLicense.licenseCode)}
              >
                {'코드 복사'}
              </button>
            </div>
            <div className={styles.modalFooter}>
              <button
                className={styles.dlBtn}
                onClick={() => handleRedownload(currentModalLicense)}
                style={{ fontSize: 14 }}
              >
                {'↓ 다시 다운로드'}
              </button>
            </div>
          </>
        )}
      </Modal>
    </div>
  );
}
