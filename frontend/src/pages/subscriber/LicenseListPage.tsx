import { useState, useEffect, useCallback } from 'react';
import { fetchMyLicenses, type LicenseListItem } from '@/api/licenses';
import { downloadTrack, triggerBlobDownload } from '@/api/downloads';
import { formatDate } from '@/utils/format';
import { useToastStore } from '@/store/toastStore';
import type { PageInfo } from '@/types';
import Pagination from '@/components/ui/Pagination';
import Modal from '@/components/ui/Modal';
import styles from './LicenseListPage.module.css';

const PAGE_SIZE = 20;

/** Truncate license code for display */
function truncateCode(code: string): string {
  return code.length > 16 ? `${code.slice(0, 16)}...` : code;
}

export default function LicenseListPage() {
  const toast = useToastStore((s) => s.show);

  /* ── State ── */
  const [licenses, setLicenses] = useState<LicenseListItem[]>([]);
  const [pageInfo, setPageInfo] = useState<PageInfo | null>(null);
  const [currentPage, setCurrentPage] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [modalLicense, setModalLicense] = useState<LicenseListItem | null>(null);

  /* ── Fetch ── */
  const load = useCallback(async (page: number) => {
    try {
      setLoading(true);
      setError(null);
      const res = await fetchMyLicenses(page, PAGE_SIZE);
      setLicenses(res.dataList);
      setPageInfo(res.pageInfo);
    } catch (err) {
      setError(err instanceof Error ? err.message : '라이선스를 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load(currentPage);
  }, [load, currentPage]);

  /* ── Re-download handler ── */
  async function handleRedownload(lic: LicenseListItem) {
    try {
      const blob = await downloadTrack(lic.track.id);
      triggerBlobDownload(blob, `${lic.track.title}.mp3`);
      toast('success', '다운로드를 시작합니다.');
    } catch {
      toast('error', '다운로드에 실패했습니다. 구독이 활성 상태인지 확인하세요.');
    }
  }

  /* ── Copy license code ── */
  async function copyCode(code: string) {
    try {
      await navigator.clipboard.writeText(code);
      toast('success', '라이선스 코드가 복사되었습니다.');
    } catch {
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

      {/* License Detail Modal */}
      <Modal
        open={modalLicense !== null}
        onClose={() => setModalLicense(null)}
        title="라이선스 상세"
      >
        {modalLicense && (
          <>
            <div className={styles.modalBody}>
              <div className={styles.modalRow}>
                <span className={styles.modalLabel}>{'곡명'}</span>
                <span className={styles.modalValue}>{modalLicense.track.title}</span>
              </div>
              <div className={styles.modalRow}>
                <span className={styles.modalLabel}>{'발급일'}</span>
                <span className={styles.modalValue}>{formatDate(modalLicense.issuedAt)}</span>
              </div>
              <div className={styles.modalRow}>
                <span className={styles.modalLabel}>{'라이선스 코드'}</span>
              </div>
              <div className={styles.codeBox}>{modalLicense.licenseCode}</div>
              <button className={styles.copyBtn} onClick={() => copyCode(modalLicense.licenseCode)}>
                {'코드 복사'}
              </button>
            </div>
            <div className={styles.modalFooter}>
              <button
                className={styles.dlBtn}
                onClick={() => handleRedownload(modalLicense)}
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
