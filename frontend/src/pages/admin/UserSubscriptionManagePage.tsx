/** Screen K-4: Admin user subscription management */
import { useEffect, useState, useCallback, useRef } from 'react';
import { fetchAdminUserSubscriptions, type MySubscription } from '@/api/userSubscriptions';
import { fetchAdminSubscriptionPlans, type SubscriptionPlan } from '@/api/subscriptions';
import { classifyLoadError } from '@/api/loadError';
import type { PageInfo } from '@/types';
import { formatDate } from '@/utils/format';
import Button from '@/components/ui/Button';
import Pagination from '@/components/ui/Pagination';
import UserSubscriptionCorrectionModal from './UserSubscriptionCorrectionModal';
import styles from './UserSubscriptionManagePage.module.css';

const STATUS_LABELS: Record<string, string> = {
  ACTIVE: '활성',
  CANCELLED: '취소됨',
  EXPIRED: '만료',
};

const BILLING_LABELS: Record<string, string> = {
  MONTHLY: '월간',
  YEARLY: '연간',
};

export default function UserSubscriptionManagePage() {
  const [subscriptions, setSubscriptions] = useState<MySubscription[]>([]);
  const [pageInfo, setPageInfo] = useState<PageInfo | null>(null);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(true);
  const [listError, setListError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const loadGenerationRef = useRef(0);
  const loadControllerRef = useRef<AbortController | null>(null);

  const [plans, setPlans] = useState<SubscriptionPlan[]>([]);
  const [planError, setPlanError] = useState<string | null>(null);
  const planGenerationRef = useRef(0);

  const [correctionTarget, setCorrectionTarget] = useState<MySubscription | null>(null);

  const loadData = useCallback(async (): Promise<boolean> => {
    loadControllerRef.current?.abort();
    const controller = new AbortController();
    const generation = ++loadGenerationRef.current;
    loadControllerRef.current = controller;
    setLoading(true);
    setListError(null);
    try {
      const result = await fetchAdminUserSubscriptions(page, 20, controller.signal);
      if (loadGenerationRef.current !== generation || controller.signal.aborted) return false;
      setSubscriptions(result.dataList);
      setPageInfo(result.pageInfo);
      return true;
    } catch (loadError: unknown) {
      if (
        loadGenerationRef.current === generation &&
        classifyLoadError(loadError) !== 'cancelled'
      ) {
        setListError('구독 목록을 불러오지 못했습니다. 기존 목록은 유지됩니다.');
      }
      return false;
    } finally {
      if (loadGenerationRef.current === generation) setLoading(false);
    }
  }, [page]);

  useEffect(() => {
    void loadData();
    return () => {
      loadControllerRef.current?.abort();
      loadGenerationRef.current += 1;
    };
  }, [loadData]);

  useEffect(() => {
    const controller = new AbortController();
    const generation = ++planGenerationRef.current;
    setPlanError(null);
    fetchAdminSubscriptionPlans(controller.signal)
      .then((result) => {
        if (planGenerationRef.current === generation && !controller.signal.aborted) {
          setPlans(result);
        }
      })
      .catch((loadError: unknown) => {
        if (
          planGenerationRef.current === generation &&
          classifyLoadError(loadError) !== 'cancelled'
        ) {
          setPlanError('플랜 목록을 불러오지 못했습니다. 현재 활성 플랜만 선택할 수 있습니다.');
        }
      });
    return () => {
      controller.abort();
      planGenerationRef.current += 1;
    };
  }, []);

  async function handleSucceeded(correctionId: number) {
    const refreshed = await loadData();
    setSuccess(
      refreshed
        ? `권한 보정 #${correctionId} 실행이 완료되어 최신 구독 목록에 반영했습니다.`
        : `권한 보정 #${correctionId} 실행은 완료되었지만 목록 새로고침에 실패했습니다.`,
    );
  }

  if (loading && pageInfo === null && subscriptions.length === 0 && !listError) {
    return (
      <div className={styles.page}>
        <div className={styles.loading}>{'불러오는 중...'}</div>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <h1 className={styles.title}>{'사용자 구독 관리'}</h1>

      {success ? (
        <div className={styles.successBanner} role="status">
          {success}
        </div>
      ) : null}
      {listError ? (
        <div className={styles.errorBanner} role="alert">
          {listError}
        </div>
      ) : null}

      <div className={styles.tableWrap}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>ID</th>
              <th>{'사용자'}</th>
              <th>{'플랜'}</th>
              <th>{'주기'}</th>
              <th>{'상태'}</th>
              <th>{'시작일'}</th>
              <th>{'만료일'}</th>
              <th>{'대기 플랜'}</th>
              <th>{'관리'}</th>
            </tr>
          </thead>
          <tbody>
            {subscriptions.length === 0 && (
              <tr>
                <td colSpan={9} className={styles.empty}>
                  {'구독 내역이 없습니다.'}
                </td>
              </tr>
            )}
            {subscriptions.map((sub) => (
              <tr key={sub.id} className={styles.row}>
                <td>{sub.id}</td>
                <td>{sub.userNickname ?? `사용자 #${sub.userId}`}</td>
                <td>{sub.subscription.name}</td>
                <td>{BILLING_LABELS[sub.billingCycle] ?? sub.billingCycle}</td>
                <td>
                  <span className={`${styles.statusBadge} ${styles[`status${sub.status}`] ?? ''}`}>
                    {STATUS_LABELS[sub.status] ?? sub.status}
                  </span>
                </td>
                <td>{formatDate(sub.startedAt)}</td>
                <td>{formatDate(sub.expiresAt)}</td>
                <td>
                  {sub.pendingSubscriptionId
                    ? (plans.find((p) => p.id === sub.pendingSubscriptionId)?.name ??
                      `플랜 #${sub.pendingSubscriptionId}`)
                    : '-'}
                </td>
                <td>
                  <Button
                    size="sm"
                    onClick={() => {
                      setSuccess(null);
                      setCorrectionTarget(sub);
                    }}
                  >
                    권한 보정
                  </Button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {pageInfo && pageInfo.total > pageInfo.size && (
        <Pagination pageInfo={pageInfo} currentPage={page} onPageChange={setPage} />
      )}

      <UserSubscriptionCorrectionModal
        target={correctionTarget}
        plans={plans}
        planError={planError}
        onClose={() => setCorrectionTarget(null)}
        onSucceeded={(correction) => handleSucceeded(correction.id)}
      />
    </div>
  );
}
