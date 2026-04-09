import { useState, useEffect, useCallback } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';
import {
  fetchMySubscription,
  cancelMySubscription,
  changeMySubscription,
  fetchSubscriptionChangePreview,
  type MySubscription,
  type SubscriptionChangePreview,
} from '@/api/userSubscriptions';
import {
  fetchSubscriptionPlans,
  type SubscriptionPlan,
} from '@/api/subscriptions';
import { formatDate } from '@/utils/format';
import Button from '@/components/ui/Button';
import Modal from '@/components/ui/Modal';
import styles from './SubscriptionManagePage.module.css';

/** Format amount with comma separator */
function formatAmount(amount: number): string {
  return amount.toLocaleString('ko-KR');
}

/** Plan display name mapping */
function getDisplayName(name: string): string {
  switch (name) {
    case 'STANDARD': return '스탠다드';
    case 'DELUXE': return '\uB514\uB7ED\uC2A4';
    case 'PREMIUM': return '\uD504\uB9AC\uBBF8\uC5C4';
    default: return name;
  }
}

export default function SubscriptionManagePage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const role = useAuthStore((s) => s.role);

  useEffect(() => {
    if (role === 'ADMIN') navigate('/admin', { replace: true });
  }, [role, navigate]);

  /* ── State ── */
  const [sub, setSub] = useState<MySubscription | null>(null);
  const [plans, setPlans] = useState<SubscriptionPlan[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  /* ── Change Plan ── */
  const [selectedPlan, setSelectedPlan] = useState<SubscriptionPlan | null>(null);
  const [selectedCycle, setSelectedCycle] = useState<'MONTHLY' | 'YEARLY'>('MONTHLY');
  const [preview, setPreview] = useState<SubscriptionChangePreview | null>(null);
  const [loadingPreview, setLoadingPreview] = useState(false);
  const [changingPlan, setChangingPlan] = useState(false);
  const [changeMsg, setChangeMsg] = useState<string | null>(null);
  const [changeError, setChangeError] = useState<string | null>(null);

  /* ── Cancel ── */
  const [showCancelModal, setShowCancelModal] = useState(false);
  const [cancelling, setCancelling] = useState(false);

  /* ── Fetch ── */
  const load = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const userType = useAuthStore.getState().user?.userType ?? 'INDIVIDUAL';
      const [subRes, plansRes] = await Promise.all([
        fetchMySubscription(),
        fetchSubscriptionPlans(userType),
      ]);
      setSub(subRes);
      setPlans(plansRes);
    } catch (err) {
      setError(
        err instanceof Error ? err.message : '구독 정보를 불러오지 못했습니다.',
      );
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  /* ── Pre-select plan from URL params (from SubscriptionPlanPage) ── */
  useEffect(() => {
    const urlPlan = searchParams.get('plan');
    const urlCycle = searchParams.get('cycle');
    if (urlPlan && plans.length > 0 && sub && !selectedPlan) {
      const found = plans.find((p) => p.name.toUpperCase() === urlPlan.toUpperCase());
      if (found && found.id !== sub.subscription.id) {
        setSelectedPlan(found);
        if (urlCycle === 'MONTHLY' || urlCycle === 'YEARLY') {
          setSelectedCycle(urlCycle);
        }
      }
    }
  }, [plans, sub, searchParams, selectedPlan]);

  /* ── Preview change when plan/cycle selected ── */
  useEffect(() => {
    if (!selectedPlan || !sub) {
      setPreview(null);
      return;
    }

    let cancelled = false;

    async function loadPreview() {
      try {
        setLoadingPreview(true);
        const res = await fetchSubscriptionChangePreview(
          selectedPlan!.id,
          selectedCycle,
        );
        if (!cancelled) setPreview(res);
      } catch {
        if (!cancelled) setPreview(null);
      } finally {
        if (!cancelled) setLoadingPreview(false);
      }
    }

    loadPreview();
    return () => {
      cancelled = true;
    };
  }, [selectedPlan, selectedCycle, sub]);

  /* ── Change subscription ── */
  async function handleChangePlan() {
    if (!selectedPlan) return;
    try {
      setChangingPlan(true);
      setChangeError(null);
      setChangeMsg(null);
      const res = await changeMySubscription({
        subscriptionId: selectedPlan.id,
        billingCycle: selectedCycle,
      });
      if (res.changeType === 'UPGRADE') {
        setChangeMsg(
          `${getDisplayName(res.subscription.name)} 플랜으로 업그레이드되었습니다. 비례 요금: ${formatAmount(res.proratedAmount)}원`,
        );
      } else {
        setChangeMsg(
          `다운그레이드가 예약되었습니다. 현재 구독 만료 후 ${getDisplayName(res.subscription.name)} 플랜이 적용됩니다.`,
        );
      }
      setSelectedPlan(null);
      setPreview(null);
      await load();
    } catch (err) {
      setChangeError(
        err instanceof Error ? err.message : '플랜 변경에 실패했습니다.',
      );
    } finally {
      setChangingPlan(false);
    }
  }

  /* ── Cancel subscription ── */
  async function handleCancel() {
    try {
      setCancelling(true);
      await cancelMySubscription();
      setShowCancelModal(false);
      await load();
    } catch (err) {
      setError(
        err instanceof Error ? err.message : '구독 취소에 실패했습니다.',
      );
    } finally {
      setCancelling(false);
    }
  }

  /* ── Status badge ── */
  function getStatusClass(status: string): string {
    switch (status) {
      case 'ACTIVE':
        return styles.statusActive;
      case 'CANCELLED':
        return styles.statusCancelled;
      default:
        return styles.statusExpired;
    }
  }

  function getStatusLabel(status: string): string {
    switch (status) {
      case 'ACTIVE':
        return '활성';
      case 'CANCELLED':
        return '취소됨 (유예 중)';
      default:
        return '만료';
    }
  }

  /* ── Render ── */

  if (loading) {
    return (
      <div className={styles.page}>
        <div className={styles.loading}>{'구독 정보를 불러오는 중...'}</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className={styles.page}>
        <div className={styles.error}>{error}</div>
      </div>
    );
  }

  if (!sub) {
    return (
      <div className={styles.page}>
        <h1 className={styles.pageTitle}>{'내 구독'}</h1>
        <div className={styles.noSub}>
          {'현재 활성 구독이 없습니다. '}
          <Link to="/subscriptions" className={styles.noSubLink}>
            {'구독 플랜 보기'}
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <h1 className={styles.pageTitle}>{'내 구독'}</h1>

      {/* ── Current Plan Card ── */}
      <div className={styles.planCard}>
        <div className={styles.planHeader}>
          <div className={styles.planName}>{getDisplayName(sub.subscription.name)}</div>
          <span className={getStatusClass(sub.status)}>
            {getStatusLabel(sub.status)}
          </span>
        </div>
        <div className={styles.planInfo}>
          <div className={styles.infoItem}>
            <span className={styles.infoLabel}>{'결제 주기'}</span>
            <span className={styles.infoValue}>
              {sub.billingCycle === 'MONTHLY' ? '월간' : '연간'}
            </span>
          </div>
          <div className={styles.infoItem}>
            <span className={styles.infoLabel}>{'시작일'}</span>
            <span className={styles.infoValue}>
              {formatDate(sub.startedAt)}
            </span>
          </div>
          <div className={styles.infoItem}>
            <span className={styles.infoLabel}>{'만료일'}</span>
            <span className={styles.infoValue}>
              {formatDate(sub.expiresAt)}
            </span>
          </div>
        </div>

        {/* Pending downgrade notice */}
        {sub.pendingSubscriptionId && (
          <div className={styles.pendingNotice}>
            <span className={styles.pendingIcon}>{'\u23F3'}</span>
            <span className={styles.pendingText}>
              {'다운그레이드가 예약되어 있습니다. 만료일 이후 새 플랜이 적용됩니다.'}
            </span>
          </div>
        )}
      </div>

      {/* ── Change Plan Section ── */}
      {sub.status === 'ACTIVE' && !sub.pendingSubscriptionId && (
        <div className={styles.actionSection}>
          <div className={styles.actionTitle}>{'플랜 변경'}</div>
          <div className={styles.actionDesc}>
            {'업그레이드는 즉시 적용되며 비례 요금이 청구됩니다. 다운그레이드는 현재 구독 만료 후 적용됩니다.'}
          </div>

          {/* Plan options */}
          <div className={styles.planGrid}>
            {plans
              .filter((p) => p.id !== sub.subscription.id && p.isActive)
              .map((plan) => (
                <div
                  key={plan.id}
                  className={
                    selectedPlan?.id === plan.id
                      ? styles.planOptionSelected
                      : styles.planOption
                  }
                  onClick={() => setSelectedPlan(plan)}
                >
                  <div className={styles.planOptionName}>{getDisplayName(plan.name)}</div>
                  <div className={styles.planOptionPrice}>
                    {'\u20A9'}
                    {formatAmount(plan.priceMonthly)}
                    {'/월'}
                  </div>
                </div>
              ))}
          </div>

          {/* Billing cycle */}
          {selectedPlan && (
            <div className={styles.actionButtons}>
              <Button
                variant={selectedCycle === 'MONTHLY' ? 'primary' : 'ghost'}
                size="sm"
                onClick={() => setSelectedCycle('MONTHLY')}
              >
                {'월간'}
              </Button>
              <Button
                variant={selectedCycle === 'YEARLY' ? 'primary' : 'ghost'}
                size="sm"
                onClick={() => setSelectedCycle('YEARLY')}
              >
                {'연간'}
              </Button>
            </div>
          )}

          {/* Preview */}
          {loadingPreview && (
            <div className={styles.loading}>{'변경 내역을 미리 보는 중...'}</div>
          )}
          {preview && !loadingPreview && (
            <div className={styles.previewBox}>
              <div className={styles.previewRow}>
                <span className={styles.previewLabel}>{'변경 유형'}</span>
                <span
                  className={
                    preview.changeType === 'UPGRADE'
                      ? styles.previewUpgrade
                      : styles.previewDowngrade
                  }
                >
                  {preview.changeType === 'UPGRADE' ? '업그레이드' : '다운그레이드'}
                </span>
              </div>
              <div className={styles.previewRow}>
                <span className={styles.previewLabel}>{'새 플랜'}</span>
                <span className={styles.previewValue}>
                  {preview.newPlanName} ({preview.newBillingCycle === 'MONTHLY' ? '월간' : '연간'})
                </span>
              </div>
              <div className={styles.previewRow}>
                <span className={styles.previewLabel}>
                  {preview.changeType === 'UPGRADE' ? '비례 요금' : '크레딧'}
                </span>
                <span className={styles.previewValue}>
                  {'\u20A9'}
                  {formatAmount(preview.proratedAmount)}
                </span>
              </div>
              <div className={styles.previewRow}>
                <span className={styles.previewLabel}>{'적용일'}</span>
                <span className={styles.previewValue}>
                  {formatDate(preview.effectiveDate)}
                </span>
              </div>
            </div>
          )}

          {/* Confirm change button */}
          {selectedPlan && preview && !loadingPreview && (
            <div className={styles.actionButtons}>
              <Button
                variant="ghost"
                onClick={() => {
                  setSelectedPlan(null);
                  setPreview(null);
                }}
              >
                {'취소'}
              </Button>
              <Button
                variant="primary"
                onClick={handleChangePlan}
                loading={changingPlan}
              >
                {'플랜 변경 확인'}
              </Button>
            </div>
          )}

          {changeMsg && (
            <div className={styles.successMsg}>{changeMsg}</div>
          )}
          {changeError && (
            <div className={styles.errorMsg}>{changeError}</div>
          )}
        </div>
      )}

      {/* ── Cancel Subscription ── */}
      {sub.status === 'ACTIVE' && (
        <div className={styles.cancelSection}>
          <div className={styles.cancelTitle}>{'구독 취소'}</div>
          <div className={styles.cancelDesc}>
            {'구독을 취소하면 만료일('}
            {formatDate(sub.expiresAt)}
            {')까지는 서비스를 계속 이용할 수 있습니다. 이후 자동으로 만료됩니다.'}
          </div>
          <Button
            variant="danger"
            onClick={() => setShowCancelModal(true)}
          >
            {'구독 취소'}
          </Button>
        </div>
      )}

      {/* ── Cancel Confirm Modal ── */}
      <Modal
        open={showCancelModal}
        onClose={() => setShowCancelModal(false)}
        title="구독 취소 확인"
      >
        <div className={styles.modalBody}>
          <p>
            {'정말 구독을 취소하시겠습니까?'}
          </p>
          <p style={{ marginTop: 8, fontSize: 13, color: 'var(--text2)' }}>
            {'만료일('}
            {sub ? formatDate(sub.expiresAt) : ''}
            {')까지 서비스 이용이 가능합니다.'}
          </p>
        </div>
        <div className={styles.modalFooter}>
          <Button variant="ghost" onClick={() => setShowCancelModal(false)}>
            {'돌아가기'}
          </Button>
          <Button
            variant="danger"
            onClick={handleCancel}
            loading={cancelling}
          >
            {'취소 확정'}
          </Button>
        </div>
      </Modal>
    </div>
  );
}
