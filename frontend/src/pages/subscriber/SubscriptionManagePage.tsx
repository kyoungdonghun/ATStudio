import { useState, useEffect, useCallback } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';
import {
  fetchMySubscription,
  cancelMySubscription,
  reactivateMySubscription,
  changeMySubscription,
  fetchSubscriptionChangePreview,
  type MySubscription,
  type SubscriptionChangePreview,
  type SubscriptionChangeType,
} from '@/api/userSubscriptions';
import { fetchSubscriptionPlans, type SubscriptionPlan } from '@/api/subscriptions';
import { fetchMyBillingAgreement, type BillingAgreementResponse } from '@/api/payments';
import { isSubscriptionRequired } from '@/api/client';
import { formatDate } from '@/utils/format';
import Button from '@/components/ui/Button';
import Modal from '@/components/ui/Modal';
import styles from './SubscriptionManagePage.module.css';

type BillingCycle = 'MONTHLY' | 'YEARLY';

/** Format amount with comma separator */
function formatAmount(amount: number): string {
  return amount.toLocaleString('ko-KR');
}

/** Plan display name mapping */
function getDisplayName(name: string): string {
  switch (name) {
    case 'STANDARD':
      return '스탠다드';
    case 'DELUXE':
      return '\uB514\uB7ED\uC2A4';
    case 'PREMIUM':
      return '\uD504\uB9AC\uBBF8\uC5C4';
    default:
      return name;
  }
}

function getBillingCycleLabel(cycle: BillingCycle): string {
  return cycle === 'MONTHLY' ? '월간' : '연간';
}

function getChangeTypeLabel(type: SubscriptionChangeType): string {
  switch (type) {
    case 'UPGRADE':
      return '업그레이드';
    case 'NO_CHANGE':
      return '예약 해제';
    case 'SCHEDULED_CHANGE':
    case 'DOWNGRADE':
      return '다음 결제일 변경';
    default:
      return type;
  }
}

function getConfirmButtonLabel(type: SubscriptionChangeType): string {
  switch (type) {
    case 'NO_CHANGE':
      return '예약 해제 확인';
    case 'SCHEDULED_CHANGE':
    case 'DOWNGRADE':
      return '변경 예약 확인';
    default:
      return '플랜 변경 확인';
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
  const [billingAgreement, setBillingAgreement] = useState<BillingAgreementResponse | null>(null);

  /* ── Change Plan ── */
  const [selectedPlan, setSelectedPlan] = useState<SubscriptionPlan | null>(null);
  const [selectedCycle, setSelectedCycle] = useState<BillingCycle>('MONTHLY');
  const [preview, setPreview] = useState<SubscriptionChangePreview | null>(null);
  const [loadingPreview, setLoadingPreview] = useState(false);
  const [changingPlan, setChangingPlan] = useState(false);
  const [changeMsg, setChangeMsg] = useState<string | null>(null);
  const [changeError, setChangeError] = useState<string | null>(null);

  /* ── Cancel ── */
  const [showCancelModal, setShowCancelModal] = useState(false);
  const [cancelling, setCancelling] = useState(false);
  const [reactivating, setReactivating] = useState(false);

  /* ── Fetch ── */
  const load = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const userType = useAuthStore.getState().user?.userType ?? 'INDIVIDUAL';
      const plansRes = await fetchSubscriptionPlans(userType);
      setPlans(plansRes);
      try {
        const subRes = await fetchMySubscription();
        setSub(subRes);
        try {
          const billingRes = await fetchMyBillingAgreement();
          setBillingAgreement(billingRes);
        } catch {
          setBillingAgreement(null);
        }
      } catch (err) {
        if (isSubscriptionRequired(err)) {
          setSub(null);
          setBillingAgreement(null);
          return;
        }
        throw err;
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : '구독 정보를 불러오지 못했습니다.');
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
        } else {
          setSelectedCycle(sub.billingCycle);
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
        const res = await fetchSubscriptionChangePreview(selectedPlan!.id, selectedCycle);
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
  function handleSelectPlan(plan: SubscriptionPlan) {
    setSelectedPlan(plan);
    if (sub && plan.id === sub.subscription.id) {
      setSelectedCycle(sub.billingCycle);
      return;
    }
    setSelectedCycle(
      plan.id === sub?.pendingSubscriptionId && sub.pendingBillingCycle
        ? sub.pendingBillingCycle
        : (sub?.billingCycle ?? 'MONTHLY'),
    );
  }

  async function handleChangePlan() {
    if (!selectedPlan || !preview) return;
    const hasReusableBillingAgreement =
      billingAgreement?.status === 'ACTIVE' ||
      (sub?.status === 'CANCELLED' && billingAgreement?.status === 'CANCELLED');
    if (preview.changeType === 'UPGRADE' && !hasReusableBillingAgreement) {
      setChangeError(
        '업그레이드는 등록된 결제수단이 필요합니다. 현재 구독 만료 후 새 정기결제로 다시 가입하거나 관리자에게 문의해주세요.',
      );
      return;
    }
    try {
      setChangingPlan(true);
      setChangeError(null);
      setChangeMsg(null);

      const res = await changeMySubscription({
        subscriptionId: selectedPlan.id,
        billingCycle: selectedCycle,
      });

      const reactivatedPrefix = sub?.status === 'CANCELLED' ? '구독 취소가 철회되었습니다. ' : '';

      if (preview.changeType === 'UPGRADE') {
        const chargeMessage =
          res.proratedAmount > 0
            ? `차액 ${formatAmount(res.proratedAmount)}원이 등록된 결제수단으로 결제되었고,`
            : '즉시 결제할 차액은 없고,';
        const nextCycleMessage =
          selectedCycle !== sub?.billingCycle
            ? ` 다음 결제일부터 ${getBillingCycleLabel(selectedCycle)} 결제로 전환됩니다.`
            : '';
        setChangeMsg(
          `${reactivatedPrefix}업그레이드가 적용되었습니다. ${chargeMessage} 다음 결제일(${formatDate(res.expiresAt)})은 유지됩니다.${nextCycleMessage}`,
        );
      } else if (preview.changeType === 'NO_CHANGE') {
        setChangeMsg(
          hasPendingChange
            ? `${reactivatedPrefix}예약된 플랜 변경이 해제되었습니다. 현재 플랜이 유지됩니다.`
            : `${reactivatedPrefix}현재 플랜이 유지됩니다.`,
        );
      } else {
        setChangeMsg(
          `${reactivatedPrefix}변경이 예약되었습니다. 현재 구독 만료 후 ${getDisplayName(res.subscription.name)} 플랜이 적용됩니다.`,
        );
      }
      setSelectedPlan(null);
      setPreview(null);
      await load();
    } catch (err) {
      setChangeError(err instanceof Error ? err.message : '플랜 변경에 실패했습니다.');
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
      setError(err instanceof Error ? err.message : '구독 취소에 실패했습니다.');
    } finally {
      setCancelling(false);
    }
  }

  async function handleReactivate() {
    try {
      setReactivating(true);
      setChangeError(null);
      await reactivateMySubscription();
      setChangeMsg('구독 취소가 철회되었습니다. 다음 결제일부터 자동 갱신이 다시 진행됩니다.');
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : '구독 취소 철회에 실패했습니다.');
    } finally {
      setReactivating(false);
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

  const hasPendingChange = Boolean(sub.pendingSubscriptionId || sub.pendingBillingCycle);
  const pendingPlan =
    !sub.pendingSubscriptionId || sub.pendingSubscriptionId === sub.subscription.id
      ? sub.subscription
      : plans.find((plan) => plan.id === sub.pendingSubscriptionId);
  const pendingPlanName = pendingPlan ? getDisplayName(pendingPlan.name) : '선택한 플랜';
  const pendingCycleLabel = sub.pendingBillingCycle
    ? getBillingCycleLabel(sub.pendingBillingCycle)
    : null;
  const pendingChangeText = pendingCycleLabel
    ? `다음 결제일부터 ${pendingPlanName} (${pendingCycleLabel})이 적용됩니다.`
    : `다음 결제일부터 ${pendingPlanName}이 적용됩니다.`;

  return (
    <div className={styles.page}>
      <h1 className={styles.pageTitle}>{'내 구독'}</h1>

      {/* ── Current Plan Card ── */}
      <div className={styles.planCard}>
        <div className={styles.planHeader}>
          <div className={styles.planName}>{getDisplayName(sub.subscription.name)}</div>
          <span className={getStatusClass(sub.status)}>{getStatusLabel(sub.status)}</span>
        </div>
        <div className={styles.planInfo}>
          <div className={styles.infoItem}>
            <span className={styles.infoLabel}>{'결제 주기'}</span>
            <span className={styles.infoValue}>{getBillingCycleLabel(sub.billingCycle)}</span>
          </div>
          <div className={styles.infoItem}>
            <span className={styles.infoLabel}>{'시작일'}</span>
            <span className={styles.infoValue}>{formatDate(sub.startedAt)}</span>
          </div>
          <div className={styles.infoItem}>
            <span className={styles.infoLabel}>{'만료일'}</span>
            <span className={styles.infoValue}>{formatDate(sub.expiresAt)}</span>
          </div>
        </div>

        {/* Pending change notice */}
        {hasPendingChange && (
          <div className={styles.pendingNotice}>
            <span className={styles.pendingIcon}>{'\u23F3'}</span>
            <span className={styles.pendingText}>{pendingChangeText}</span>
          </div>
        )}
      </div>

      {/* ── Payment Info ── */}
      <div className={styles.actionSection}>
        <div className={styles.actionTitle}>{'결제 정보'}</div>
        {billingAgreement ? (
          <>
            <div className={styles.planInfo}>
              <div className={styles.infoItem}>
                <span className={styles.infoLabel}>{'상태'}</span>
                <span className={styles.infoValue}>
                  {getBillingStatusLabel(billingAgreement.status)}
                </span>
              </div>
              <div className={styles.infoItem}>
                <span className={styles.infoLabel}>{'결제수단'}</span>
                <span className={styles.infoValue}>
                  {billingAgreement.maskedMethod
                    ? `${billingAgreement.payMethod ?? 'CARD'} ${billingAgreement.maskedMethod}`
                    : (billingAgreement.payMethod ?? '등록됨')}
                </span>
              </div>
              <div className={styles.infoItem}>
                <span className={styles.infoLabel}>{'다음 결제일'}</span>
                <span className={styles.infoValue}>
                  {billingAgreement.nextBillingAt
                    ? formatDate(billingAgreement.nextBillingAt)
                    : '-'}
                </span>
              </div>
            </div>
            {sub.status === 'CANCELLED' && (
              <div className={styles.actionDesc}>
                {
                  '구독 취소 상태입니다. 만료일까지 이용할 수 있고, 구독을 유지하면 다음 결제가 다시 예약됩니다.'
                }
              </div>
            )}
          </>
        ) : (
          <div className={styles.actionDesc}>{'현재 등록된 정기결제 수단이 없습니다.'}</div>
        )}
      </div>

      {/* ── Change Plan Section ── */}
      {(sub.status === 'ACTIVE' || sub.status === 'CANCELLED') && (
        <div className={styles.actionSection}>
          <div className={styles.actionTitle}>{'플랜 변경'}</div>
          <div className={styles.actionDesc}>
            {sub.status === 'CANCELLED'
              ? '플랜 변경을 확정하면 구독 취소가 철회됩니다. 업그레이드는 남은 기간 차액을 즉시 결제하고, 그 외 변경은 현재 구독 만료 후 적용됩니다.'
              : '업그레이드는 등록된 결제수단으로 남은 기간 차액을 즉시 결제한 뒤 적용됩니다. 그 외 변경은 현재 구독 만료 후 적용되며, 예약된 변경은 다시 바꿀 수 있습니다.'}
          </div>

          {/* Plan options */}
          <div className={styles.planGrid}>
            {plans
              .filter((p) => p.isActive)
              .map((plan) => (
                <div
                  key={plan.id}
                  className={
                    selectedPlan?.id === plan.id ? styles.planOptionSelected : styles.planOption
                  }
                  onClick={() => handleSelectPlan(plan)}
                >
                  <div className={styles.planOptionName}>{getDisplayName(plan.name)}</div>
                  <div className={styles.planOptionPrice}>
                    {'\u20A9'}
                    {formatAmount(plan.priceMonthly)}
                    {'/월'}
                  </div>
                  {plan.id === sub.subscription.id && (
                    <div className={styles.planOptionCurrent}>{'현재 플랜'}</div>
                  )}
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
                {getBillingCycleLabel('MONTHLY')}
              </Button>
              <Button
                variant={selectedCycle === 'YEARLY' ? 'primary' : 'ghost'}
                size="sm"
                onClick={() => setSelectedCycle('YEARLY')}
              >
                {getBillingCycleLabel('YEARLY')}
              </Button>
            </div>
          )}

          {/* Preview */}
          {loadingPreview && <div className={styles.loading}>{'변경 내역을 미리 보는 중...'}</div>}
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
                  {getChangeTypeLabel(preview.changeType)}
                </span>
              </div>
              <div className={styles.previewRow}>
                <span className={styles.previewLabel}>{'새 플랜'}</span>
                <span className={styles.previewValue}>{preview.newPlanName}</span>
              </div>
              <div className={styles.previewRow}>
                <span className={styles.previewLabel}>{'다음 결제 주기'}</span>
                <span className={styles.previewValue}>
                  {getBillingCycleLabel(preview.newBillingCycle)}
                </span>
              </div>
              <div className={styles.previewRow}>
                <span className={styles.previewLabel}>
                  {preview.changeType === 'UPGRADE' ? '즉시 결제 차액' : '즉시 결제'}
                </span>
                <span className={styles.previewValue}>
                  {preview.changeType === 'UPGRADE'
                    ? `\u20A9${formatAmount(preview.proratedAmount)}`
                    : '없음'}
                </span>
              </div>
              <div className={styles.previewRow}>
                <span className={styles.previewLabel}>{'적용일'}</span>
                <span className={styles.previewValue}>{formatDate(preview.effectiveDate)}</span>
              </div>
              <div className={styles.previewRow}>
                <span className={styles.previewLabel}>{'다음 결제일'}</span>
                <span className={styles.previewValue}>{formatDate(preview.nextBillingDate)}</span>
              </div>
              <div className={styles.previewRow}>
                <span className={styles.previewLabel}>{'다음 결제 금액'}</span>
                <span className={styles.previewValue}>
                  {'\u20A9'}
                  {formatAmount(preview.nextBillingAmount)}
                </span>
              </div>
            </div>
          )}

          {preview?.changeType === 'UPGRADE' &&
            billingAgreement?.status !== 'ACTIVE' &&
            !(sub.status === 'CANCELLED' && billingAgreement?.status === 'CANCELLED') && (
              <div className={styles.errorMsg}>
                {
                  '업그레이드는 등록된 결제수단이 필요합니다. 기존 단건 결제창으로는 진행하지 않습니다.'
                }
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
              <Button variant="primary" onClick={handleChangePlan} loading={changingPlan}>
                {getConfirmButtonLabel(preview.changeType)}
              </Button>
            </div>
          )}

          {changeMsg && <div className={styles.successMsg}>{changeMsg}</div>}
          {changeError && <div className={styles.errorMsg}>{changeError}</div>}
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
          <Button variant="danger" onClick={() => setShowCancelModal(true)}>
            {'구독 취소'}
          </Button>
        </div>
      )}

      {sub.status === 'CANCELLED' && (
        <div className={styles.actionSection}>
          <div className={styles.actionTitle}>{'구독 취소됨'}</div>
          <div className={styles.actionDesc}>
            {'구독은 '}
            {formatDate(sub.expiresAt)}
            {'까지 유지됩니다. 구독을 계속 사용하려면 취소를 철회할 수 있습니다.'}
          </div>
          <Button variant="primary" onClick={handleReactivate} loading={reactivating}>
            {'구독 유지하기'}
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
          <p>{'정말 구독을 취소하시겠습니까?'}</p>
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
          <Button variant="danger" onClick={handleCancel} loading={cancelling}>
            {'취소 확정'}
          </Button>
        </div>
      </Modal>
    </div>
  );
}

function getBillingStatusLabel(status: BillingAgreementResponse['status']): string {
  switch (status) {
    case 'ACTIVE':
      return '자동 갱신 중';
    case 'READY':
      return '등록 진행 중';
    case 'SUSPENDED':
      return '갱신 중지';
    case 'CANCELLED':
      return '다음 갱신 중지됨';
    case 'EXPIRED':
      return '만료';
    default:
      return status;
  }
}
