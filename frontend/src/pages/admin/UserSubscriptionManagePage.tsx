/** Screen K-4: Admin user subscription management */
import { useEffect, useState, useCallback, useRef } from 'react';
import {
  fetchAdminUserSubscriptions,
  updateAdminUserSubscription,
  deleteAdminUserSubscription,
  type MySubscription,
  type AdminUpdateSubscriptionRequest,
} from '@/api/userSubscriptions';
import { fetchAdminSubscriptionPlans, type SubscriptionPlan } from '@/api/subscriptions';
import { classifyLoadError } from '@/api/loadError';
import type { PageInfo } from '@/types';
import { formatDate } from '@/utils/format';
import Modal from '@/components/ui/Modal';
import Button from '@/components/ui/Button';
import Pagination from '@/components/ui/Pagination';
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
  const [error, setError] = useState<string | null>(null);
  const loadGenerationRef = useRef(0);

  /* Plans for change modal */
  const [plans, setPlans] = useState<SubscriptionPlan[]>([]);

  /* Edit modal */
  const [editTarget, setEditTarget] = useState<MySubscription | null>(null);
  const [editStatus, setEditStatus] = useState('');
  const [editBilling, setEditBilling] = useState('');
  const [editExpires, setEditExpires] = useState('');
  const [editLoading, setEditLoading] = useState(false);

  /* Cancel modal */
  const [cancelTarget, setCancelTarget] = useState<MySubscription | null>(null);
  const [cancelLoading, setCancelLoading] = useState(false);

  const loadData = useCallback(
    (signal?: AbortSignal) => {
      const generation = ++loadGenerationRef.current;
      setLoading(true);
      setError(null);
      fetchAdminUserSubscriptions(page, 20, signal)
        .then((result) => {
          if (loadGenerationRef.current === generation) {
            setSubscriptions(result.dataList);
            setPageInfo(result.pageInfo);
          }
        })
        .catch((loadError: unknown) => {
          if (
            loadGenerationRef.current === generation &&
            classifyLoadError(loadError) !== 'cancelled'
          ) {
            setError('구독 목록을 불러오지 못했습니다.');
          }
        })
        .finally(() => {
          if (loadGenerationRef.current === generation) setLoading(false);
        });
    },
    [page],
  );

  useEffect(() => {
    const controller = new AbortController();
    loadData(controller.signal);
    return () => {
      controller.abort();
      loadGenerationRef.current += 1;
    };
  }, [loadData]);

  useEffect(() => {
    const controller = new AbortController();
    fetchAdminSubscriptionPlans(controller.signal)
      .then((plans) => setPlans(plans))
      .catch((loadError: unknown) => {
        if (classifyLoadError(loadError) !== 'cancelled') setPlans([]);
      });
    return () => controller.abort();
  }, []);

  /* ── Edit handlers ── */

  function openEdit(sub: MySubscription) {
    setEditTarget(sub);
    setEditStatus(sub.status);
    setEditBilling(sub.billingCycle);
    setEditExpires(sub.expiresAt);
  }

  async function confirmEdit() {
    if (!editTarget) return;
    setEditLoading(true);
    try {
      const req: AdminUpdateSubscriptionRequest = {};
      if (editStatus !== editTarget.status) req.status = editStatus;
      if (editBilling !== editTarget.billingCycle)
        req.billingCycle = editBilling as 'MONTHLY' | 'YEARLY';
      if (editExpires !== editTarget.expiresAt) req.expiresAt = editExpires;

      await updateAdminUserSubscription(editTarget.id, req);
      setEditTarget(null);
      loadData();
    } catch {
      setError('구독 수정에 실패했습니다.');
    } finally {
      setEditLoading(false);
    }
  }

  /* ── Cancel handlers ── */

  async function confirmCancel() {
    if (!cancelTarget) return;
    setCancelLoading(true);
    try {
      await deleteAdminUserSubscription(cancelTarget.id);
      setCancelTarget(null);
      loadData();
    } catch {
      setError('구독 취소에 실패했습니다.');
    } finally {
      setCancelLoading(false);
    }
  }

  if (loading) {
    return (
      <div className={styles.page}>
        <div className={styles.loading}>{'불러오는 중...'}</div>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <h1 className={styles.title}>{'사용자 구독 관리'}</h1>

      {error && <div className={styles.error}>{error}</div>}

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
                <td>{sub.userNickname ?? `User #${sub.userId}`}</td>
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
                      `Plan #${sub.pendingSubscriptionId}`)
                    : '-'}
                </td>
                <td>
                  <div className={styles.actionBtns}>
                    <Button size="sm" onClick={() => openEdit(sub)}>
                      {'수정'}
                    </Button>
                    {sub.status === 'ACTIVE' && (
                      <Button size="sm" variant="danger" onClick={() => setCancelTarget(sub)}>
                        {'취소'}
                      </Button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {pageInfo && pageInfo.total > pageInfo.size && (
        <Pagination pageInfo={pageInfo} currentPage={page} onPageChange={setPage} />
      )}

      {/* Edit modal */}
      <Modal open={editTarget !== null} onClose={() => setEditTarget(null)} title="구독 수정">
        {editTarget && (
          <>
            <div className={styles.modalBody}>
              <div className={styles.modalInfo}>
                <strong>{editTarget.userNickname}</strong>
                {' — '}
                {editTarget.subscription.name}
              </div>

              <div className={styles.modalField}>
                <label className={styles.modalLabel}>{'상태'}</label>
                <select
                  className={styles.modalSelect}
                  value={editStatus}
                  onChange={(e) => setEditStatus(e.target.value)}
                >
                  <option value="ACTIVE">{'활성'}</option>
                  <option value="CANCELLED">{'취소됨'}</option>
                  <option value="EXPIRED">{'만료'}</option>
                </select>
              </div>

              <div className={styles.modalField}>
                <label className={styles.modalLabel}>{'결제 주기'}</label>
                <select
                  className={styles.modalSelect}
                  value={editBilling}
                  onChange={(e) => setEditBilling(e.target.value)}
                >
                  <option value="MONTHLY">{'월간'}</option>
                  <option value="YEARLY">{'연간'}</option>
                </select>
              </div>

              <div className={styles.modalField}>
                <label className={styles.modalLabel}>{'만료일'}</label>
                <input
                  type="date"
                  className={styles.modalInput}
                  value={editExpires}
                  onChange={(e) => setEditExpires(e.target.value)}
                />
              </div>
            </div>
            <div className={styles.modalActions}>
              <Button variant="ghost" size="sm" onClick={() => setEditTarget(null)}>
                {'취소'}
              </Button>
              <Button size="sm" loading={editLoading} onClick={confirmEdit}>
                {'저장'}
              </Button>
            </div>
          </>
        )}
      </Modal>

      {/* Cancel modal */}
      <Modal open={cancelTarget !== null} onClose={() => setCancelTarget(null)} title="구독 취소">
        {cancelTarget && (
          <>
            <div className={styles.modalBody}>
              <strong>{cancelTarget.userNickname}</strong>
              {'님의 '}
              <strong>{cancelTarget.subscription.name}</strong>
              {' 구독을 취소하시겠습니까?'}
              <br />
              {'만료일('}
              {formatDate(cancelTarget.expiresAt)}
              {')까지는 서비스가 유지됩니다.'}
            </div>
            <div className={styles.modalActions}>
              <Button variant="ghost" size="sm" onClick={() => setCancelTarget(null)}>
                {'아니오'}
              </Button>
              <Button variant="danger" size="sm" loading={cancelLoading} onClick={confirmCancel}>
                {'구독 취소'}
              </Button>
            </div>
          </>
        )}
      </Modal>
    </div>
  );
}
