/** Admin payment operations view */
import { useCallback, useEffect, useRef, useState } from 'react';
import {
  approveAdminPaymentEntitlementCorrection,
  approveAdminPaymentRefund,
  createAdminPaymentEntitlementCorrection,
  createAdminPaymentRefund,
  executeAdminPaymentEntitlementCorrection,
  executeAdminPaymentRefund,
  fetchAdminBillingAgreements,
  fetchAdminPaymentEntitlementCorrection,
  fetchAdminPaymentEntitlementCorrections,
  fetchAdminPaymentOperationAuditLogs,
  fetchAdminPaymentOrders,
  fetchAdminPaymentReceipts,
  fetchAdminPaymentReconciliationIncidents,
  fetchAdminPaymentRefund,
  fetchAdminPaymentRefundPreview,
  fetchAdminPaymentRefunds,
  fetchAdminPaymentSettlements,
  fetchAdminSubscriptionPayments,
  ignoreAdminPaymentSettlement,
  importAdminPaymentSettlements,
  previewAdminPaymentEntitlementCorrection,
  reconcileAdminPaymentSettlements,
  recoverAdminPaymentSettlementImportAttempt,
  updateAdminPaymentReconciliationIncidentStatus,
  type AdminBillingAgreement,
  type AdminPaymentEntitlementCorrection,
  type AdminPaymentEntitlementCorrectionBillingCycle,
  type AdminPaymentEntitlementCorrectionPreview,
  type AdminPaymentEntitlementCorrectionStatus,
  type AdminPaymentEntitlementCorrectionSubscriptionStatus,
  type AdminPaymentOperationAuditLog,
  type AdminPaymentOrder,
  type AdminPaymentReceipt,
  type AdminPaymentReconciliationIncident,
  type AdminPaymentReconciliationIncidentStatus,
  type AdminPaymentRefund,
  type AdminPaymentRefundPreview,
  type AdminPaymentRefundReasonCode,
  type AdminPaymentRefundStatus,
  type AdminPaymentSettlement,
  type AdminPaymentSettlementImportAttempt,
  type AdminPaymentSettlementImportResult,
  type AdminPaymentSettlementSource,
  type AdminPaymentSettlementStatus,
  type AdminSubscriptionPayment,
} from '@/api/admin';
import { fetchAdminSubscriptionPlans, type SubscriptionPlan } from '@/api/subscriptions';
import ConfirmDialog from '@/components/ui/ConfirmDialog';
import Pagination from '@/components/ui/Pagination';
import { useToastStore } from '@/store/toastStore';
import type { PageInfo } from '@/types';
import { formatDate, formatDateTime, formatPrice } from '@/utils/format';
import { getSafeReceiptUrl } from '@/utils/safeReceiptUrl';
import {
  clearSettlementImportAttempt,
  CorruptSettlementImportAttemptError,
  createNewSettlementImportAttempt,
  getSettlementImportAttempt,
  PendingSettlementImportAttemptError,
} from '@/utils/settlementImportAttempt';
import styles from './PaymentOperationsPage.module.css';

type TabKey =
  | 'orders'
  | 'agreements'
  | 'payments'
  | 'incidents'
  | 'receipts'
  | 'audits'
  | 'settlements'
  | 'refunds'
  | 'corrections';

const INCIDENT_STATUSES: AdminPaymentReconciliationIncidentStatus[] = [
  'OPEN',
  'ACKNOWLEDGED',
  'RESOLVED',
  'IGNORED',
];

const REFUND_REASONS: AdminPaymentRefundReasonCode[] = [
  'CUSTOMER_REQUEST',
  'DUPLICATE_PAYMENT',
  'PAYMENT_ERROR',
  'SERVICE_ISSUE',
  'ADMIN_ADJUSTMENT',
  'OTHER',
];

const BILLING_CYCLES: AdminPaymentEntitlementCorrectionBillingCycle[] = ['MONTHLY', 'YEARLY'];

const SUBSCRIPTION_STATUSES: AdminPaymentEntitlementCorrectionSubscriptionStatus[] = [
  'ACTIVE',
  'CANCELLED',
  'EXPIRED',
];

const SETTLEMENT_STATUSES: AdminPaymentSettlementStatus[] = [
  'MATCHED',
  'MISMATCHED',
  'LOCAL_PAYMENT_NOT_FOUND',
  'PROVIDER_SETTLEMENT_NOT_FOUND',
  'IMPORTED',
  'IGNORED',
];

const SETTLEMENT_SOURCES: AdminPaymentSettlementSource[] = [
  'CSV_MANUAL',
  'SYSTEM_RECONCILIATION',
  'TOSS_API',
];

const REFUND_EXECUTION_CONFIRM_TEXT = '환불 실행';
const CORRECTION_EXECUTION_CONFIRM_TEXT = '권한 보정 실행';
const SETTLEMENT_IMPORT_ATTEMPT_CORRUPT_MESSAGE =
  '저장된 정산 import 복구 정보가 손상되어 새 import를 시작할 수 없습니다. 브라우저 세션을 지운 후 다시 시도해주세요.';
const SETTLEMENT_IMPORT_ATTEMPT_PENDING_MESSAGE =
  '이전 정산 import 결과를 먼저 복구한 후 새 import를 시작해주세요.';

interface SettlementImportAttemptState {
  key: string | null;
  corrupt: boolean;
}

function readSettlementImportAttemptState(): SettlementImportAttemptState {
  try {
    return { key: getSettlementImportAttempt(), corrupt: false };
  } catch (error) {
    if (error instanceof CorruptSettlementImportAttemptError) {
      return { key: null, corrupt: true };
    }
    throw error;
  }
}

interface IncidentEdit {
  status: AdminPaymentReconciliationIncidentStatus;
  note: string;
}

interface CorrectionForm {
  paymentRefundId: string;
  targetSubscriptionId: string;
  targetBillingCycle: AdminPaymentEntitlementCorrectionBillingCycle;
  targetStatus: AdminPaymentEntitlementCorrectionSubscriptionStatus;
  targetExpiresAt: string;
  clearPendingChange: boolean;
  cancelBillingAgreement: boolean;
  reasonNote: string;
}

interface PaymentViewRequest {
  tab: TabKey;
  page: number;
  incidentStatus: AdminPaymentReconciliationIncidentStatus | '';
  settlementStatus: AdminPaymentSettlementStatus | '';
  settlementSource: AdminPaymentSettlementSource | '';
  settlementBaseDateFrom: string;
  settlementBaseDateTo: string;
}

interface ConfirmationState {
  title: string;
  message: string;
  confirmLabel: string;
  confirmVariant?: 'primary' | 'danger';
  action: () => Promise<void>;
}

type RecoveryOutcome = 'COMMITTED' | 'FAILED' | 'RELOAD_FAILED' | 'UNKNOWN';

interface RefundRecoveryIntent {
  readonly domain: 'refund';
  readonly durableId: number;
  readonly generation: number;
  readonly page: number;
  readonly viewRequestGeneration: number;
  readonly viewRequestKey: string;
  outcome: RecoveryOutcome;
  detail: AdminPaymentRefund;
}

interface CorrectionRecoveryIntent {
  readonly domain: 'correction';
  readonly durableId: number;
  readonly paymentRefundId: number;
  readonly generation: number;
  readonly page: number;
  readonly viewRequestGeneration: number;
  readonly viewRequestKey: string;
  outcome: RecoveryOutcome;
  detail: AdminPaymentEntitlementCorrection;
}

function isAmbiguousOutcome(outcome: RecoveryOutcome): boolean {
  return outcome === 'UNKNOWN' || outcome === 'RELOAD_FAILED';
}

function refundOutcome(status: AdminPaymentRefundStatus): RecoveryOutcome {
  if (status === 'SUCCEEDED') return 'COMMITTED';
  if (status === 'FAILED' || status === 'CANCELLED') return 'FAILED';
  return 'UNKNOWN';
}

function correctionOutcome(status: AdminPaymentEntitlementCorrectionStatus): RecoveryOutcome {
  if (status === 'SUCCEEDED') return 'COMMITTED';
  if (status === 'FAILED' || status === 'CANCELLED') return 'FAILED';
  return 'UNKNOWN';
}

function refundExecutionFeedback(outcome: RecoveryOutcome) {
  if (outcome === 'COMMITTED') {
    return { type: 'success' as const, message: '환불 실행 결과가 확인되었습니다.' };
  }
  if (outcome === 'FAILED') {
    return { type: 'error' as const, message: '환불 실행이 최종 실패 상태로 확인되었습니다.' };
  }
  if (outcome === 'UNKNOWN') {
    return {
      type: 'error' as const,
      message: '환불 후속 상태가 아직 확정되지 않았습니다. 상태를 다시 확인해주세요.',
    };
  }
  return {
    type: 'error' as const,
    message: '환불은 성공했지만 후속 정보를 새로고침하지 못했습니다.',
  };
}

function correctionExecutionFeedback(outcome: RecoveryOutcome) {
  if (outcome === 'COMMITTED') {
    return { type: 'success' as const, message: '권한 보정 결과가 확인되었습니다.' };
  }
  if (outcome === 'FAILED') {
    return { type: 'error' as const, message: '권한 보정이 최종 실패 상태로 확인되었습니다.' };
  }
  if (outcome === 'UNKNOWN') {
    return {
      type: 'error' as const,
      message: '권한 보정 후속 상태가 아직 확정되지 않았습니다. 상태를 다시 확인해주세요.',
    };
  }
  return {
    type: 'error' as const,
    message: '권한 보정은 성공했지만 후속 정보를 새로고침하지 못했습니다.',
  };
}

export default function PaymentOperationsPage() {
  const [tab, setTab] = useState<TabKey>('orders');
  const [page, setPage] = useState(1);
  const [pageInfo, setPageInfo] = useState<PageInfo | null>(null);
  const [orders, setOrders] = useState<AdminPaymentOrder[]>([]);
  const [agreements, setAgreements] = useState<AdminBillingAgreement[]>([]);
  const [payments, setPayments] = useState<AdminSubscriptionPayment[]>([]);
  const [incidents, setIncidents] = useState<AdminPaymentReconciliationIncident[]>([]);
  const [receipts, setReceipts] = useState<AdminPaymentReceipt[]>([]);
  const [audits, setAudits] = useState<AdminPaymentOperationAuditLog[]>([]);
  const [settlements, setSettlements] = useState<AdminPaymentSettlement[]>([]);
  const [refunds, setRefunds] = useState<AdminPaymentRefund[]>([]);
  const [corrections, setCorrections] = useState<AdminPaymentEntitlementCorrection[]>([]);
  const [plans, setPlans] = useState<SubscriptionPlan[]>([]);
  const [incidentStatusFilter, setIncidentStatusFilter] = useState<
    AdminPaymentReconciliationIncidentStatus | ''
  >('OPEN');
  const [settlementStatusFilter, setSettlementStatusFilter] = useState<
    AdminPaymentSettlementStatus | ''
  >('');
  const [settlementSourceFilter, setSettlementSourceFilter] = useState<
    AdminPaymentSettlementSource | ''
  >('');
  const [settlementBaseDateFrom, setSettlementBaseDateFrom] = useState('');
  const [settlementBaseDateTo, setSettlementBaseDateTo] = useState('');
  const [settlementFile, setSettlementFile] = useState<File | null>(null);
  const [settlementFileInputKey, setSettlementFileInputKey] = useState(0);
  const [settlementNote, setSettlementNote] = useState('');
  const [settlementBusy, setSettlementBusy] = useState<string | null>(null);
  const [settlementImportResult, setSettlementImportResult] =
    useState<AdminPaymentSettlementImportResult | null>(null);
  const [settlementImportAttemptState] = useState(readSettlementImportAttemptState);
  const [settlementImportOperationKey, setSettlementImportOperationKey] = useState<string | null>(
    () => settlementImportAttemptState.key,
  );
  const [settlementImportAttemptCorrupt] = useState(() => settlementImportAttemptState.corrupt);
  const [settlementRecoveryAttempt, setSettlementRecoveryAttempt] =
    useState<AdminPaymentSettlementImportAttempt | null>(null);
  const [settlementIgnoreNotes, setSettlementIgnoreNotes] = useState<Record<number, string>>({});
  const [incidentEdits, setIncidentEdits] = useState<Record<number, IncidentEdit>>({});
  const [updatingIncidentId, setUpdatingIncidentId] = useState<number | null>(null);
  const [refundPaymentId, setRefundPaymentId] = useState('');
  const [refundPreview, setRefundPreview] = useState<AdminPaymentRefundPreview | null>(null);
  const [refundAmount, setRefundAmount] = useState('');
  const [refundReasonCode, setRefundReasonCode] =
    useState<AdminPaymentRefundReasonCode>('CUSTOMER_REQUEST');
  const [refundReasonNote, setRefundReasonNote] = useState('');
  const [refundActionNotes, setRefundActionNotes] = useState<Record<number, string>>({});
  const [refundBusy, setRefundBusy] = useState<string | null>(null);
  const [correctionForm, setCorrectionForm] = useState<CorrectionForm>({
    paymentRefundId: '',
    targetSubscriptionId: '',
    targetBillingCycle: 'MONTHLY',
    targetStatus: 'EXPIRED',
    targetExpiresAt: todayInputValue(),
    clearPendingChange: true,
    cancelBillingAgreement: true,
    reasonNote: '',
  });
  const [correctionPreview, setCorrectionPreview] =
    useState<AdminPaymentEntitlementCorrectionPreview | null>(null);
  const [correctionActionNotes, setCorrectionActionNotes] = useState<Record<number, string>>({});
  const [correctionBusy, setCorrectionBusy] = useState<string | null>(null);
  const [refundRecoveries, setRefundRecoveries] = useState<Record<number, RefundRecoveryIntent>>(
    {},
  );
  const [correctionRecoveries, setCorrectionRecoveries] = useState<
    Record<number, CorrectionRecoveryIntent>
  >({});
  const [refundExecutePending, setRefundExecutePending] = useState<Record<number, boolean>>({});
  const [correctionExecutePending, setCorrectionExecutePending] = useState<Record<number, boolean>>(
    {},
  );
  const [recoveryReads, setRecoveryReads] = useState<Record<string, boolean>>({});
  const [confirmation, setConfirmation] = useState<ConfirmationState | null>(null);
  const [confirmationBusy, setConfirmationBusy] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const showToast = useToastStore((s) => s.show);
  const paymentRequestGenerationRef = useRef(0);
  const paymentRequestControllerRef = useRef<AbortController | null>(null);
  const refundRecoveryRef = useRef<Record<number, RefundRecoveryIntent>>({});
  const correctionRecoveryRef = useRef<Record<number, CorrectionRecoveryIntent>>({});
  const refundGenerationRef = useRef(0);
  const correctionGenerationRef = useRef(0);
  const refundReadGenerationRef = useRef(new Map<number, number>());
  const correctionReadGenerationRef = useRef(new Map<number, number>());
  const refundExecutePendingRef = useRef(new Set<number>());
  const correctionExecutePendingRef = useRef(new Set<number>());
  const recoveryReadPendingRef = useRef(new Set<string>());
  const paymentViewRequest: PaymentViewRequest = {
    tab,
    page,
    incidentStatus: incidentStatusFilter,
    settlementStatus: settlementStatusFilter,
    settlementSource: settlementSourceFilter,
    settlementBaseDateFrom,
    settlementBaseDateTo,
  };
  const paymentViewRequestKey = JSON.stringify(paymentViewRequest);
  const latestPaymentViewRequestRef = useRef(paymentViewRequest);
  const latestPaymentViewRequestKeyRef = useRef(paymentViewRequestKey);
  latestPaymentViewRequestRef.current = paymentViewRequest;
  latestPaymentViewRequestKeyRef.current = paymentViewRequestKey;

  function setRefundRecovery(intent: RefundRecoveryIntent) {
    refundRecoveryRef.current = { ...refundRecoveryRef.current, [intent.durableId]: intent };
    setRefundRecoveries(refundRecoveryRef.current);
  }

  function setCorrectionRecovery(intent: CorrectionRecoveryIntent) {
    correctionRecoveryRef.current = {
      ...correctionRecoveryRef.current,
      [intent.durableId]: intent,
    };
    setCorrectionRecoveries(correctionRecoveryRef.current);
  }

  function clearRefundRecovery(intent: RefundRecoveryIntent) {
    if (!isCurrentRefundIntent(intent)) return;
    const next = { ...refundRecoveryRef.current };
    delete next[intent.durableId];
    refundRecoveryRef.current = next;
    setRefundRecoveries(next);
  }

  function clearCorrectionRecovery(intent: CorrectionRecoveryIntent) {
    if (!isCurrentCorrectionIntent(intent)) return;
    const next = { ...correctionRecoveryRef.current };
    delete next[intent.durableId];
    correctionRecoveryRef.current = next;
    setCorrectionRecoveries(next);
  }

  function isCurrentRefundIntent(intent: RefundRecoveryIntent): boolean {
    return refundRecoveryRef.current[intent.durableId]?.generation === intent.generation;
  }

  function isCurrentCorrectionIntent(intent: CorrectionRecoveryIntent): boolean {
    return correctionRecoveryRef.current[intent.durableId]?.generation === intent.generation;
  }

  function ownsRecoveryView(intent: RefundRecoveryIntent | CorrectionRecoveryIntent): boolean {
    return (
      paymentRequestGenerationRef.current === intent.viewRequestGeneration &&
      latestPaymentViewRequestKeyRef.current === intent.viewRequestKey
    );
  }

  function isRefundMutationBlocked(refundId: number): boolean {
    const ownIntent = refundRecoveryRef.current[refundId];
    if (ownIntent && isAmbiguousOutcome(ownIntent.outcome)) return true;
    return hasAmbiguousCorrectionIntent(refundId);
  }

  function hasAmbiguousCorrectionIntent(paymentRefundId: number): boolean {
    return Object.values(correctionRecoveryRef.current).some(
      (intent) => intent.paymentRefundId === paymentRefundId && isAmbiguousOutcome(intent.outcome),
    );
  }

  function isCorrectionMutationBlocked(correction: AdminPaymentEntitlementCorrection): boolean {
    const ownIntent = correctionRecoveryRef.current[correction.id];
    if (ownIntent && isAmbiguousOutcome(ownIntent.outcome)) return true;
    if (hasAmbiguousCorrectionIntent(correction.paymentRefundId)) return true;
    const refundIntent = refundRecoveryRef.current[correction.paymentRefundId];
    return Boolean(refundIntent && isAmbiguousOutcome(refundIntent.outcome));
  }

  function setRecoveryReadPending(key: string, pending: boolean) {
    if (pending) recoveryReadPendingRef.current.add(key);
    else recoveryReadPendingRef.current.delete(key);
    setRecoveryReads((current) => ({ ...current, [key]: pending }));
  }

  function updateRefundExecutePending(refundId: number, pending: boolean) {
    if (pending) refundExecutePendingRef.current.add(refundId);
    else refundExecutePendingRef.current.delete(refundId);
    setRefundExecutePending((current) => {
      const next = { ...current };
      if (pending) next[refundId] = true;
      else delete next[refundId];
      return next;
    });
  }

  function updateCorrectionExecutePending(correctionId: number, pending: boolean) {
    if (pending) correctionExecutePendingRef.current.add(correctionId);
    else correctionExecutePendingRef.current.delete(correctionId);
    setCorrectionExecutePending((current) => {
      const next = { ...current };
      if (pending) next[correctionId] = true;
      else delete next[correctionId];
      return next;
    });
  }

  const loadData = useCallback(async () => {
    paymentRequestControllerRef.current?.abort();
    const controller = new AbortController();
    paymentRequestControllerRef.current = controller;
    const requestGeneration = ++paymentRequestGenerationRef.current;
    const request = latestPaymentViewRequestRef.current;
    const requestKey = latestPaymentViewRequestKeyRef.current;
    const isCurrentRequest = () =>
      requestGeneration === paymentRequestGenerationRef.current &&
      requestKey === latestPaymentViewRequestKeyRef.current &&
      !controller.signal.aborted;

    setLoading(true);
    setError(null);
    try {
      if (request.tab === 'orders') {
        const result = await fetchAdminPaymentOrders(request.page, 20, controller.signal);
        if (!isCurrentRequest()) return false;
        setOrders(result.dataList);
        setPageInfo(result.pageInfo);
      } else if (request.tab === 'agreements') {
        const result = await fetchAdminBillingAgreements(request.page, 20, controller.signal);
        if (!isCurrentRequest()) return false;
        setAgreements(result.dataList);
        setPageInfo(result.pageInfo);
      } else if (request.tab === 'payments') {
        const result = await fetchAdminSubscriptionPayments(request.page, 20, controller.signal);
        if (!isCurrentRequest()) return false;
        setPayments(result.dataList);
        setPageInfo(result.pageInfo);
      } else if (request.tab === 'incidents') {
        const result = await fetchAdminPaymentReconciliationIncidents(
          request.page,
          20,
          request.incidentStatus || undefined,
          controller.signal,
        );
        if (!isCurrentRequest()) return false;
        setIncidents(result.dataList);
        setIncidentEdits(buildIncidentEdits(result.dataList));
        setPageInfo(result.pageInfo);
      } else if (request.tab === 'receipts') {
        const result = await fetchAdminPaymentReceipts(request.page, 20, controller.signal);
        if (!isCurrentRequest()) return false;
        setReceipts(result.dataList);
        setPageInfo(result.pageInfo);
      } else if (request.tab === 'audits') {
        const result = await fetchAdminPaymentOperationAuditLogs(
          request.page,
          20,
          controller.signal,
        );
        if (!isCurrentRequest()) return false;
        setAudits(result.dataList);
        setPageInfo(result.pageInfo);
      } else if (request.tab === 'settlements') {
        const result = await fetchAdminPaymentSettlements(
          request.page,
          20,
          {
            status: request.settlementStatus || undefined,
            source: request.settlementSource || undefined,
            baseDateFrom: request.settlementBaseDateFrom || undefined,
            baseDateTo: request.settlementBaseDateTo || undefined,
          },
          controller.signal,
        );
        if (!isCurrentRequest()) return false;
        setSettlements(result.dataList);
        setPageInfo(result.pageInfo);
      } else if (request.tab === 'refunds') {
        const result = await fetchAdminPaymentRefunds(request.page, 20, controller.signal);
        if (!isCurrentRequest()) return false;
        let nextRecoveries = refundRecoveryRef.current;
        let recoveriesChanged = false;
        for (const refund of result.dataList) {
          if (
            !nextRecoveries[refund.id] &&
            ['PROCESSING', 'PENDING_PROVIDER_CONFIRMATION'].includes(refund.status)
          ) {
            if (!recoveriesChanged) nextRecoveries = { ...nextRecoveries };
            recoveriesChanged = true;
            nextRecoveries[refund.id] = {
              domain: 'refund',
              durableId: refund.id,
              generation: ++refundGenerationRef.current,
              page: request.page,
              viewRequestGeneration: requestGeneration,
              viewRequestKey: requestKey,
              outcome: 'UNKNOWN',
              detail: refund,
            };
          }
        }
        if (recoveriesChanged) {
          refundRecoveryRef.current = nextRecoveries;
          setRefundRecoveries(nextRecoveries);
        }
        setRefunds(result.dataList.map((refund) => nextRecoveries[refund.id]?.detail ?? refund));
        setPageInfo(result.pageInfo);
      } else {
        const result = await fetchAdminPaymentEntitlementCorrections(
          request.page,
          20,
          controller.signal,
        );
        if (!isCurrentRequest()) return false;
        let nextRecoveries = correctionRecoveryRef.current;
        let recoveriesChanged = false;
        for (const correction of result.dataList) {
          if (!nextRecoveries[correction.id] && correction.status === 'PROCESSING') {
            if (!recoveriesChanged) nextRecoveries = { ...nextRecoveries };
            recoveriesChanged = true;
            nextRecoveries[correction.id] = {
              domain: 'correction',
              durableId: correction.id,
              paymentRefundId: correction.paymentRefundId,
              generation: ++correctionGenerationRef.current,
              page: request.page,
              viewRequestGeneration: requestGeneration,
              viewRequestKey: requestKey,
              outcome: 'UNKNOWN',
              detail: correction,
            };
          }
        }
        if (recoveriesChanged) {
          correctionRecoveryRef.current = nextRecoveries;
          setCorrectionRecoveries(nextRecoveries);
        }
        setCorrections(
          result.dataList.map((correction) => nextRecoveries[correction.id]?.detail ?? correction),
        );
        setPageInfo(result.pageInfo);
      }
      return true;
    } catch {
      if (!isCurrentRequest()) {
        return false;
      }
      setError('결제 정보를 불러오지 못했습니다.');
      setPageInfo(null);
      return false;
    } finally {
      if (isCurrentRequest()) {
        setLoading(false);
        paymentRequestControllerRef.current = null;
      }
    }
  }, []);

  useEffect(() => {
    void loadData();
    return () => {
      paymentRequestControllerRef.current?.abort();
      paymentRequestControllerRef.current = null;
      paymentRequestGenerationRef.current += 1;
    };
  }, [
    incidentStatusFilter,
    loadData,
    page,
    settlementBaseDateFrom,
    settlementBaseDateTo,
    settlementSourceFilter,
    settlementStatusFilter,
    tab,
  ]);

  useEffect(() => {
    if (tab !== 'corrections' || plans.length > 0) {
      return;
    }
    fetchAdminSubscriptionPlans()
      .then((items) => {
        setPlans(items.filter((plan) => plan.isActive));
      })
      .catch(() => {
        showToast('error', '구독 플랜 목록을 불러오지 못했습니다.');
      });
  }, [plans.length, showToast, tab]);

  function changeTab(next: TabKey) {
    setTab(next);
    setPage(1);
    setError(null);
  }

  function changeIncidentFilter(next: AdminPaymentReconciliationIncidentStatus | '') {
    setIncidentStatusFilter(next);
    setPage(1);
  }

  function changeSettlementStatusFilter(next: AdminPaymentSettlementStatus | '') {
    setSettlementStatusFilter(next);
    setPage(1);
  }

  function changeSettlementSourceFilter(next: AdminPaymentSettlementSource | '') {
    setSettlementSourceFilter(next);
    setPage(1);
  }

  function changeSettlementDateFilter(type: 'from' | 'to', value: string) {
    if (type === 'from') {
      setSettlementBaseDateFrom(value);
    } else {
      setSettlementBaseDateTo(value);
    }
    setPage(1);
  }

  function changeIncidentEdit(incidentId: number, patch: Partial<IncidentEdit>) {
    setIncidentEdits((prev) => ({
      ...prev,
      [incidentId]: {
        status: prev[incidentId]?.status ?? 'OPEN',
        note: prev[incidentId]?.note ?? '',
        ...patch,
      },
    }));
  }

  function changeRefundActionNote(refundId: number, note: string) {
    setRefundActionNotes((prev) => ({ ...prev, [refundId]: note }));
  }

  function changeCorrectionForm(patch: Partial<CorrectionForm>) {
    setCorrectionForm((prev) => ({ ...prev, ...patch }));
    setCorrectionPreview(null);
  }

  function changeCorrectionActionNote(correctionId: number, note: string) {
    setCorrectionActionNotes((prev) => ({ ...prev, [correctionId]: note }));
  }

  function changeSettlementIgnoreNote(settlementId: number, note: string) {
    setSettlementIgnoreNotes((prev) => ({ ...prev, [settlementId]: note }));
  }

  function requestConfirmation(next: ConfirmationState) {
    setConfirmation((current) => current ?? next);
  }

  async function confirmOperation() {
    const current = confirmation;
    if (!current || confirmationBusy) return;

    setConfirmationBusy(true);
    try {
      await current.action();
      setConfirmation(null);
    } finally {
      setConfirmationBusy(false);
    }
  }

  function cancelConfirmation() {
    if (!confirmationBusy) setConfirmation(null);
  }

  function importSettlementFile() {
    if (!settlementFile) {
      showToast('error', '정산 CSV 파일을 선택해주세요.');
      return;
    }
    if (settlementImportAttemptCorrupt) {
      showToast('error', SETTLEMENT_IMPORT_ATTEMPT_CORRUPT_MESSAGE);
      return;
    }
    if (settlementImportOperationKey) {
      showToast('warning', SETTLEMENT_IMPORT_ATTEMPT_PENDING_MESSAGE);
      return;
    }
    const file = settlementFile;
    const note = settlementNote.trim() || undefined;
    requestConfirmation({
      title: '정산 파일 가져오기',
      message: '정산 파일을 import하고 내부 결제/환불 원장과 대조합니다.',
      confirmLabel: '가져오기',
      action: async () => {
        let operationKey: string;
        try {
          operationKey = createNewSettlementImportAttempt();
        } catch (error) {
          if (error instanceof PendingSettlementImportAttemptError) {
            setSettlementImportOperationKey(error.idempotencyKey);
            showToast('warning', SETTLEMENT_IMPORT_ATTEMPT_PENDING_MESSAGE);
            return;
          }
          showToast('error', '정산 import 요청 키를 생성하지 못했습니다.');
          return;
        }
        setSettlementImportOperationKey(operationKey);
        setSettlementRecoveryAttempt(null);
        setSettlementBusy('import');
        setSettlementImportResult(null);
        try {
          const result = await importAdminPaymentSettlements(file, operationKey, note);
          clearSettlementImportAttempt(operationKey);
          setSettlementImportOperationKey(null);
          setSettlementImportResult(result);
          const reloaded = await loadData();
          if (!reloaded) {
            showToast('error', '정산 import 결과를 받았지만 목록을 다시 불러오지 못했습니다.');
            return;
          }
          if (result.failedRows > 0) {
            showToast(
              'warning',
              `정산 import가 부분 완료되었습니다. 실패 ${result.failedRows}건을 확인해주세요.`,
            );
            return;
          }
          setSettlementFile(null);
          setSettlementFileInputKey((current) => current + 1);
          showToast('success', '정산 파일 import가 완료되었습니다.');
        } catch {
          await recoverSettlementImportOutcome(operationKey, true);
        } finally {
          setSettlementBusy(null);
        }
      },
    });
  }

  async function recoverSettlementImportOutcome(operationKey: string, afterPost: boolean) {
    try {
      const attempt = await recoverAdminPaymentSettlementImportAttempt(operationKey);
      setSettlementRecoveryAttempt(attempt);
      if (attempt.state === 'PROCESSING') {
        showToast('warning', '정산 import가 처리 중입니다. 같은 키로 결과를 다시 확인해주세요.');
        return;
      }

      clearSettlementImportAttempt(operationKey);
      setSettlementImportOperationKey(null);
      if (attempt.state === 'FAILED') {
        showToast(
          'error',
          '정산 import 시도가 실패했습니다. 새 작업은 명시적으로 다시 시작해주세요.',
        );
        return;
      }

      const reloaded = await loadData();
      if (!reloaded) {
        showToast('error', '완료된 정산 import 결과는 복구했지만 목록을 다시 불러오지 못했습니다.');
        return;
      }
      if (attempt.failedRows > 0) {
        showToast(
          'warning',
          `정산 import 완료 결과를 복구했습니다. 실패 ${attempt.failedRows}건의 행 오류 상세는 응답에만 제공됩니다.`,
        );
        return;
      }
      setSettlementFile(null);
      setSettlementFileInputKey((current) => current + 1);
      showToast('success', '정산 import 완료 결과를 복구했습니다.');
    } catch {
      showToast(
        'error',
        afterPost
          ? '정산 import 결과가 불확실합니다. 동일 키로 읽기 복구를 다시 실행해주세요.'
          : '정산 import 결과를 복구하지 못했습니다.',
      );
    }
  }

  async function recoverPendingSettlementImport() {
    if (settlementImportAttemptCorrupt) {
      showToast('error', SETTLEMENT_IMPORT_ATTEMPT_CORRUPT_MESSAGE);
      return;
    }
    if (!settlementImportOperationKey || settlementBusy) return;
    setSettlementBusy('recover');
    try {
      await recoverSettlementImportOutcome(settlementImportOperationKey, false);
    } finally {
      setSettlementBusy(null);
    }
  }

  function reconcileSettlementGaps() {
    const request = {
      baseDateFrom: settlementBaseDateFrom || undefined,
      baseDateTo: settlementBaseDateTo || undefined,
    };
    requestConfirmation({
      title: '정산 누락 후보 확인',
      message: '선택한 기간의 로컬 결제 중 정산 근거가 없는 항목을 확인합니다.',
      confirmLabel: '확인',
      action: async () => {
        setSettlementBusy('reconcile');
        try {
          const result = await reconcileAdminPaymentSettlements(request);
          setSettlementImportResult(result);
          showToast('success', '정산 누락 후보 확인이 완료되었습니다.');
          await loadData();
        } catch {
          showToast('error', '정산 누락 후보 확인에 실패했습니다.');
        } finally {
          setSettlementBusy(null);
        }
      },
    });
  }

  function ignoreSettlement(settlement: AdminPaymentSettlement) {
    const note = settlementIgnoreNotes[settlement.id]?.trim();
    if (!note) {
      showToast('error', 'ignore 처리 메모를 입력해주세요.');
      return;
    }
    requestConfirmation({
      title: '정산 항목 제외',
      message: `정산 row #${settlement.id}을 IGNORE 처리합니다.`,
      confirmLabel: 'IGNORE',
      confirmVariant: 'danger',
      action: async () => {
        setSettlementBusy(`ignore-${settlement.id}`);
        try {
          await ignoreAdminPaymentSettlement(settlement.id, note);
          showToast('success', '정산 row가 IGNORE 처리되었습니다.');
          await loadData();
        } catch {
          showToast('error', '정산 row를 IGNORE 처리하지 못했습니다.');
        } finally {
          setSettlementBusy(null);
        }
      },
    });
  }

  async function saveIncident(incident: AdminPaymentReconciliationIncident) {
    const edit = incidentEdits[incident.id] ?? {
      status: incident.status,
      note: incident.resolutionNote ?? '',
    };
    setUpdatingIncidentId(incident.id);
    setError(null);
    try {
      await updateAdminPaymentReconciliationIncidentStatus(incident.id, {
        status: edit.status,
        note: edit.note.trim() || undefined,
      });
      showToast('success', 'Incident 상태가 저장되었습니다.');
      await loadData();
    } catch {
      showToast('error', 'Incident 상태를 저장하지 못했습니다.');
    } finally {
      setUpdatingIncidentId(null);
    }
  }

  async function previewRefund(paymentIdOverride?: number) {
    const paymentId = paymentIdOverride ?? Number(refundPaymentId);
    if (!Number.isFinite(paymentId) || paymentId <= 0) {
      showToast('error', '결제내역 ID를 입력해주세요.');
      return;
    }
    setRefundBusy('preview');
    setRefundPreview(null);
    try {
      const result = await fetchAdminPaymentRefundPreview(paymentId);
      setRefundPreview(result);
      setRefundPaymentId(String(result.subscriptionPaymentId));
      setRefundAmount(String(result.refundableAmount));
      changeTab('refunds');
      showToast(result.refundable ? 'success' : 'error', '환불 가능 금액을 확인했습니다.');
    } catch {
      setRefundPreview(null);
      showToast('error', '환불 미리보기를 불러오지 못했습니다.');
    } finally {
      setRefundBusy(null);
    }
  }

  function requestRefund() {
    const paymentId = Number(refundPaymentId);
    const amount = Number(refundAmount);
    if (
      !refundPreview?.refundable ||
      !Number.isSafeInteger(paymentId) ||
      paymentId !== refundPreview.subscriptionPaymentId ||
      !Number.isFinite(amount) ||
      amount <= 0
    ) {
      showToast('error', '환불 가능한 결제와 금액을 먼저 확인해주세요.');
      return;
    }
    const request = {
      subscriptionPaymentId: refundPreview.subscriptionPaymentId,
      amount,
      reasonCode: refundReasonCode,
      reasonNote: refundReasonNote.trim() || undefined,
    };
    requestConfirmation({
      title: '환불 요청 생성',
      message: '환불 요청 원장을 생성합니다. 아직 Toss 환불은 실행되지 않습니다.',
      confirmLabel: '요청 생성',
      confirmVariant: 'danger',
      action: async () => {
        setRefundBusy('create');
        try {
          await createAdminPaymentRefund(request);
          showToast('success', '환불 요청이 생성되었습니다.');
          setRefundPreview(null);
          setRefundReasonNote('');
          await loadData();
        } catch {
          showToast('error', '환불 요청을 생성하지 못했습니다.');
        } finally {
          setRefundBusy(null);
        }
      },
    });
  }

  function changeRefundPaymentId(value: string) {
    setRefundPaymentId(value);
    setRefundPreview(null);
  }

  function approveRefund(refund: AdminPaymentRefund) {
    if (isRefundMutationBlocked(refund.id)) return;
    const note = refundActionNotes[refund.id]?.trim() || undefined;
    requestConfirmation({
      title: '환불 요청 승인',
      message: `환불 #${refund.id} 요청을 승인합니다.`,
      confirmLabel: '승인',
      confirmVariant: 'danger',
      action: async () => {
        setRefundBusy(`approve-${refund.id}`);
        try {
          await approveAdminPaymentRefund(refund.id, note);
          showToast('success', '환불 요청이 승인되었습니다.');
          await loadData();
        } catch {
          showToast('error', '환불 요청을 승인하지 못했습니다.');
        } finally {
          setRefundBusy(null);
        }
      },
    });
  }

  async function refreshRefundStatus(
    intent: RefundRecoveryIntent,
    readFailureOutcome: 'UNKNOWN' | 'RELOAD_FAILED',
    refreshList: boolean,
    unlockPreExecution = false,
  ) {
    const key = `refund-${intent.durableId}`;
    if (recoveryReadPendingRef.current.has(key)) return;
    const readGeneration = (refundReadGenerationRef.current.get(intent.durableId) ?? 0) + 1;
    refundReadGenerationRef.current.set(intent.durableId, readGeneration);
    setRecoveryReadPending(key, true);
    try {
      const detail = await fetchAdminPaymentRefund(intent.durableId);
      if (
        !isCurrentRefundIntent(intent) ||
        refundReadGenerationRef.current.get(intent.durableId) !== readGeneration
      ) {
        return;
      }
      if (detail.id !== intent.durableId) {
        setRefundRecovery({ ...intent, outcome: readFailureOutcome });
        return;
      }
      const outcome = refundOutcome(detail.status);
      if (
        unlockPreExecution &&
        intent.outcome === 'UNKNOWN' &&
        ['REQUESTED', 'APPROVED'].includes(detail.status)
      ) {
        setRefunds((current) => current.map((item) => (item.id === detail.id ? detail : item)));
        clearRefundRecovery(intent);
        return;
      }
      const nextIntent = { ...intent, outcome, detail };
      setRefundRecovery(nextIntent);
      setRefunds((current) => current.map((item) => (item.id === detail.id ? detail : item)));
      if (outcome !== 'COMMITTED' || !refreshList) return;

      try {
        const result = await fetchAdminPaymentRefunds(intent.page, 20);
        if (!isCurrentRefundIntent(nextIntent) || !ownsRecoveryView(nextIntent)) return;
        setRefunds(result.dataList.map((item) => (item.id === detail.id ? detail : item)));
        setPageInfo(result.pageInfo);
      } catch {
        if (isCurrentRefundIntent(nextIntent) && ownsRecoveryView(nextIntent)) {
          setRefundRecovery({ ...nextIntent, outcome: 'RELOAD_FAILED' });
        }
      }
    } catch {
      if (isCurrentRefundIntent(intent)) {
        setRefundRecovery({ ...intent, outcome: readFailureOutcome });
      }
    } finally {
      if (refundReadGenerationRef.current.get(intent.durableId) === readGeneration) {
        setRecoveryReadPending(key, false);
      }
    }
  }

  function checkRefundStatus(refundId: number) {
    if (refundExecutePendingRef.current.has(refundId)) return;
    const intent = refundRecoveryRef.current[refundId];
    if (!intent || !isAmbiguousOutcome(intent.outcome)) return;
    const readFailureOutcome = intent.outcome === 'RELOAD_FAILED' ? 'RELOAD_FAILED' : 'UNKNOWN';
    void refreshRefundStatus(intent, readFailureOutcome, false, true);
  }

  async function executeRefund(refund: AdminPaymentRefund) {
    if (isRefundMutationBlocked(refund.id) || refundExecutePendingRef.current.has(refund.id))
      return;
    if (
      !confirmTypedAction(
        `Toss provider 환불을 실행합니다. 환불 #${refund.id}의 idempotency key를 재사용합니다. 이 작업은 결제 취소 API를 호출합니다.`,
        REFUND_EXECUTION_CONFIRM_TEXT,
      )
    ) {
      return;
    }
    updateRefundExecutePending(refund.id, true);
    const intent: RefundRecoveryIntent = {
      domain: 'refund',
      durableId: refund.id,
      generation: ++refundGenerationRef.current,
      page,
      viewRequestGeneration: paymentRequestGenerationRef.current,
      viewRequestKey: latestPaymentViewRequestKeyRef.current,
      outcome: 'UNKNOWN',
      detail: refund,
    };
    setRefundRecovery(intent);
    setRefundBusy(`execute-${refund.id}`);
    const releaseExecuteLock = () => {
      updateRefundExecutePending(refund.id, false);
      setRefundBusy(null);
    };
    let preflight: AdminPaymentRefund;
    try {
      preflight = await fetchAdminPaymentRefund(refund.id);
    } catch {
      if (isCurrentRefundIntent(intent)) {
        setRefundRecovery({ ...intent, outcome: 'UNKNOWN' });
        showToast('error', '환불 실행 전 최신 상태를 확인하지 못했습니다.');
      }
      releaseExecuteLock();
      return;
    }
    if (!isCurrentRefundIntent(intent)) {
      releaseExecuteLock();
      return;
    }
    if (preflight.id !== refund.id) {
      setRefundRecovery({ ...intent, outcome: 'UNKNOWN' });
      showToast('error', '환불 실행 전 최신 상태를 확인하지 못했습니다.');
      releaseExecuteLock();
      return;
    }
    const preflightOutcome = refundOutcome(preflight.status);
    const preflightIntent = { ...intent, outcome: preflightOutcome, detail: preflight };
    setRefundRecovery(preflightIntent);
    setRefunds((current) => current.map((item) => (item.id === preflight.id ? preflight : item)));
    if (preflight.status !== 'APPROVED') {
      showToast(
        preflightOutcome === 'COMMITTED' ? 'success' : 'error',
        preflightOutcome === 'COMMITTED'
          ? '환불이 이미 완료된 상태입니다.'
          : preflightOutcome === 'FAILED'
            ? '환불이 최종 실패 상태입니다.'
            : '환불 실행 가능 상태를 확인할 수 없습니다.',
      );
      releaseExecuteLock();
      return;
    }
    try {
      const response = await executeAdminPaymentRefund(
        refund.id,
        refundActionNotes[refund.id]?.trim() || undefined,
      );
      if (!isCurrentRefundIntent(preflightIntent)) return;
      setRefunds((current) => current.map((item) => (item.id === response.id ? response : item)));
      const outcome = refundOutcome(response.status);
      const responseIntent = { ...preflightIntent, outcome, detail: response };
      setRefundRecovery(responseIntent);
      if (outcome === 'COMMITTED') {
        await refreshRefundStatus(responseIntent, 'RELOAD_FAILED', true);
        const current = refundRecoveryRef.current[refund.id];
        const feedback = refundExecutionFeedback(current?.outcome ?? 'RELOAD_FAILED');
        showToast(feedback.type, feedback.message);
      } else if (outcome === 'FAILED') {
        showToast('error', '환불 실행이 최종 실패 상태로 확인되었습니다.');
      } else {
        showToast('error', '환불 처리 결과를 확정할 수 없습니다. 상태를 다시 확인해주세요.');
      }
    } catch {
      await refreshRefundStatus(preflightIntent, 'UNKNOWN', false);
      const current = refundRecoveryRef.current[refund.id];
      showToast(
        current?.outcome === 'COMMITTED' ? 'success' : 'error',
        current?.outcome === 'COMMITTED'
          ? '응답은 유실됐지만 환불 완료 상태를 확인했습니다.'
          : current?.outcome === 'FAILED'
            ? '환불 실행이 최종 실패 상태로 확인되었습니다.'
            : '환불 처리 결과를 확정할 수 없습니다. 상태를 다시 확인해주세요.',
      );
    } finally {
      releaseExecuteLock();
    }
  }

  async function previewCorrection() {
    const request = correctionRequest();
    if (!request) {
      return;
    }
    setCorrectionBusy('preview');
    try {
      const result = await previewAdminPaymentEntitlementCorrection(request);
      setCorrectionPreview(result);
      showToast(result.executable ? 'success' : 'error', '권한 보정 미리보기를 확인했습니다.');
    } catch {
      showToast('error', '권한 보정 미리보기를 불러오지 못했습니다.');
    } finally {
      setCorrectionBusy(null);
    }
  }

  function requestCorrection() {
    const request = correctionRequest();
    if (!request || !correctionPreview?.executable) {
      showToast('error', '실행 가능한 권한 보정 미리보기를 먼저 확인해주세요.');
      return;
    }
    const refundIntent = refundRecoveryRef.current[request.paymentRefundId];
    if (
      (refundIntent && isAmbiguousOutcome(refundIntent.outcome)) ||
      hasAmbiguousCorrectionIntent(request.paymentRefundId)
    ) {
      return;
    }
    requestConfirmation({
      title: '권한 보정 요청 생성',
      message: '권한 보정 요청 원장을 생성합니다. 아직 구독 상태는 변경되지 않습니다.',
      confirmLabel: '요청 생성',
      confirmVariant: 'danger',
      action: async () => {
        setCorrectionBusy('create');
        try {
          await createAdminPaymentEntitlementCorrection(request);
          showToast('success', '권한 보정 요청이 생성되었습니다.');
          setCorrectionPreview(null);
          await loadData();
        } catch {
          showToast('error', '권한 보정 요청을 생성하지 못했습니다.');
        } finally {
          setCorrectionBusy(null);
        }
      },
    });
  }

  function approveCorrection(correction: AdminPaymentEntitlementCorrection) {
    if (isCorrectionMutationBlocked(correction)) return;
    const note = correctionActionNotes[correction.id]?.trim() || undefined;
    requestConfirmation({
      title: '권한 보정 요청 승인',
      message: `권한 보정 #${correction.id} 요청을 승인합니다.`,
      confirmLabel: '승인',
      confirmVariant: 'danger',
      action: async () => {
        setCorrectionBusy(`approve-${correction.id}`);
        try {
          await approveAdminPaymentEntitlementCorrection(correction.id, note);
          showToast('success', '권한 보정 요청이 승인되었습니다.');
          await loadData();
        } catch {
          showToast('error', '권한 보정 요청을 승인하지 못했습니다.');
        } finally {
          setCorrectionBusy(null);
        }
      },
    });
  }

  async function refreshCorrectionStatus(
    intent: CorrectionRecoveryIntent,
    readFailureOutcome: 'UNKNOWN' | 'RELOAD_FAILED',
    refreshList: boolean,
    unlockPreExecution = false,
  ) {
    const key = `correction-${intent.durableId}`;
    if (recoveryReadPendingRef.current.has(key)) return;
    const readGeneration = (correctionReadGenerationRef.current.get(intent.durableId) ?? 0) + 1;
    correctionReadGenerationRef.current.set(intent.durableId, readGeneration);
    setRecoveryReadPending(key, true);
    try {
      const detail = await fetchAdminPaymentEntitlementCorrection(intent.durableId);
      if (
        !isCurrentCorrectionIntent(intent) ||
        correctionReadGenerationRef.current.get(intent.durableId) !== readGeneration
      ) {
        return;
      }
      if (detail.id !== intent.durableId) {
        setCorrectionRecovery({ ...intent, outcome: readFailureOutcome });
        return;
      }
      const outcome = correctionOutcome(detail.status);
      if (
        unlockPreExecution &&
        intent.outcome === 'UNKNOWN' &&
        ['REQUESTED', 'APPROVED'].includes(detail.status)
      ) {
        setCorrections((current) => current.map((item) => (item.id === detail.id ? detail : item)));
        clearCorrectionRecovery(intent);
        return;
      }
      const nextIntent = { ...intent, outcome, detail };
      setCorrectionRecovery(nextIntent);
      setCorrections((current) => current.map((item) => (item.id === detail.id ? detail : item)));
      if (outcome !== 'COMMITTED' || !refreshList) return;

      try {
        const result = await fetchAdminPaymentEntitlementCorrections(intent.page, 20);
        if (!isCurrentCorrectionIntent(nextIntent) || !ownsRecoveryView(nextIntent)) return;
        setCorrections(result.dataList.map((item) => (item.id === detail.id ? detail : item)));
        setPageInfo(result.pageInfo);
      } catch {
        if (isCurrentCorrectionIntent(nextIntent) && ownsRecoveryView(nextIntent)) {
          setCorrectionRecovery({ ...nextIntent, outcome: 'RELOAD_FAILED' });
        }
      }
    } catch {
      if (isCurrentCorrectionIntent(intent)) {
        setCorrectionRecovery({ ...intent, outcome: readFailureOutcome });
      }
    } finally {
      if (correctionReadGenerationRef.current.get(intent.durableId) === readGeneration) {
        setRecoveryReadPending(key, false);
      }
    }
  }

  function checkCorrectionStatus(correctionId: number) {
    if (correctionExecutePendingRef.current.has(correctionId)) return;
    const intent = correctionRecoveryRef.current[correctionId];
    if (!intent || !isAmbiguousOutcome(intent.outcome)) return;
    const readFailureOutcome = intent.outcome === 'RELOAD_FAILED' ? 'RELOAD_FAILED' : 'UNKNOWN';
    void refreshCorrectionStatus(intent, readFailureOutcome, false, true);
  }

  async function executeCorrection(correction: AdminPaymentEntitlementCorrection) {
    if (
      isCorrectionMutationBlocked(correction) ||
      correctionExecutePendingRef.current.has(correction.id)
    ) {
      return;
    }
    if (
      !confirmTypedAction(
        `권한 보정 #${correction.id}을 실행합니다. 이 작업은 로컬 구독 상태를 변경하며, provider 환불과는 별도입니다.`,
        CORRECTION_EXECUTION_CONFIRM_TEXT,
      )
    ) {
      return;
    }
    updateCorrectionExecutePending(correction.id, true);
    const intent: CorrectionRecoveryIntent = {
      domain: 'correction',
      durableId: correction.id,
      paymentRefundId: correction.paymentRefundId,
      generation: ++correctionGenerationRef.current,
      page,
      viewRequestGeneration: paymentRequestGenerationRef.current,
      viewRequestKey: latestPaymentViewRequestKeyRef.current,
      outcome: 'UNKNOWN',
      detail: correction,
    };
    setCorrectionRecovery(intent);
    setCorrectionBusy(`execute-${correction.id}`);
    const releaseExecuteLock = () => {
      updateCorrectionExecutePending(correction.id, false);
      setCorrectionBusy(null);
    };
    let preflight: AdminPaymentEntitlementCorrection;
    try {
      preflight = await fetchAdminPaymentEntitlementCorrection(correction.id);
    } catch {
      if (isCurrentCorrectionIntent(intent)) {
        setCorrectionRecovery({ ...intent, outcome: 'UNKNOWN' });
        showToast('error', '권한 보정 실행 전 최신 상태를 확인하지 못했습니다.');
      }
      releaseExecuteLock();
      return;
    }
    if (!isCurrentCorrectionIntent(intent)) {
      releaseExecuteLock();
      return;
    }
    if (preflight.id !== correction.id) {
      setCorrectionRecovery({ ...intent, outcome: 'UNKNOWN' });
      showToast('error', '권한 보정 실행 전 최신 상태를 확인하지 못했습니다.');
      releaseExecuteLock();
      return;
    }
    const preflightOutcome = correctionOutcome(preflight.status);
    const preflightIntent = { ...intent, outcome: preflightOutcome, detail: preflight };
    setCorrectionRecovery(preflightIntent);
    setCorrections((current) =>
      current.map((item) => (item.id === preflight.id ? preflight : item)),
    );
    if (preflight.status !== 'APPROVED') {
      showToast(
        preflightOutcome === 'COMMITTED' ? 'success' : 'error',
        preflightOutcome === 'COMMITTED'
          ? '권한 보정이 이미 완료된 상태입니다.'
          : preflightOutcome === 'FAILED'
            ? '권한 보정이 최종 실패 상태입니다.'
            : '권한 보정 실행 가능 상태를 확인할 수 없습니다.',
      );
      releaseExecuteLock();
      return;
    }
    try {
      const response = await executeAdminPaymentEntitlementCorrection(
        correction.id,
        correctionActionNotes[correction.id]?.trim() || undefined,
      );
      if (!isCurrentCorrectionIntent(preflightIntent)) return;
      setCorrections((current) =>
        current.map((item) => (item.id === response.id ? response : item)),
      );
      const outcome = correctionOutcome(response.status);
      const responseIntent = { ...preflightIntent, outcome, detail: response };
      setCorrectionRecovery(responseIntent);
      if (outcome === 'COMMITTED') {
        await refreshCorrectionStatus(responseIntent, 'RELOAD_FAILED', true);
        const current = correctionRecoveryRef.current[correction.id];
        const feedback = correctionExecutionFeedback(current?.outcome ?? 'RELOAD_FAILED');
        showToast(feedback.type, feedback.message);
      } else if (outcome === 'FAILED') {
        showToast('error', '권한 보정이 최종 실패 상태로 확인되었습니다.');
      } else {
        showToast('error', '권한 보정 결과를 확정할 수 없습니다. 상태를 다시 확인해주세요.');
      }
    } catch {
      await refreshCorrectionStatus(preflightIntent, 'UNKNOWN', false);
      const current = correctionRecoveryRef.current[correction.id];
      showToast(
        current?.outcome === 'COMMITTED' ? 'success' : 'error',
        current?.outcome === 'COMMITTED'
          ? '응답은 유실됐지만 권한 보정 완료 상태를 확인했습니다.'
          : current?.outcome === 'FAILED'
            ? '권한 보정이 최종 실패 상태로 확인되었습니다.'
            : '권한 보정 결과를 확정할 수 없습니다. 상태를 다시 확인해주세요.',
      );
    } finally {
      releaseExecuteLock();
    }
  }

  function correctionRequest() {
    const paymentRefundId = Number(correctionForm.paymentRefundId);
    const targetSubscriptionId = Number(correctionForm.targetSubscriptionId);
    if (!Number.isFinite(paymentRefundId) || paymentRefundId <= 0) {
      showToast('error', '성공한 환불 ID를 입력해주세요.');
      return null;
    }
    if (!Number.isFinite(targetSubscriptionId) || targetSubscriptionId <= 0) {
      showToast('error', '대상 플랜을 선택하거나 ID를 입력해주세요.');
      return null;
    }
    if (!correctionForm.targetExpiresAt) {
      showToast('error', '목표 만료일을 입력해주세요.');
      return null;
    }
    return {
      paymentRefundId,
      targetSubscriptionId,
      targetBillingCycle: correctionForm.targetBillingCycle,
      targetStatus: correctionForm.targetStatus,
      targetExpiresAt: correctionForm.targetExpiresAt,
      clearPendingChange: correctionForm.clearPendingChange,
      cancelBillingAgreement: correctionForm.cancelBillingAgreement,
      reasonNote: correctionForm.reasonNote.trim() || undefined,
    };
  }

  return (
    <div className={styles.page}>
      <h1 className={styles.title}>{'결제 운영'}</h1>

      <div className={styles.tabs}>
        <TabButton active={tab === 'orders'} onClick={() => changeTab('orders')}>
          {'주문'}
        </TabButton>
        <TabButton active={tab === 'agreements'} onClick={() => changeTab('agreements')}>
          {'자동결제'}
        </TabButton>
        <TabButton active={tab === 'payments'} onClick={() => changeTab('payments')}>
          {'결제내역'}
        </TabButton>
        <TabButton active={tab === 'incidents'} onClick={() => changeTab('incidents')}>
          {'대사 Incident'}
        </TabButton>
        <TabButton active={tab === 'receipts'} onClick={() => changeTab('receipts')}>
          {'영수증'}
        </TabButton>
        <TabButton active={tab === 'audits'} onClick={() => changeTab('audits')}>
          {'감사로그'}
        </TabButton>
        <TabButton active={tab === 'settlements'} onClick={() => changeTab('settlements')}>
          {'정산'}
        </TabButton>
        <TabButton active={tab === 'refunds'} onClick={() => changeTab('refunds')}>
          {'환불'}
        </TabButton>
        <TabButton active={tab === 'corrections'} onClick={() => changeTab('corrections')}>
          {'권한 보정'}
        </TabButton>
      </div>

      {tab === 'incidents' && (
        <div className={styles.filterBar}>
          <span className={styles.filterLabel}>{'상태'}</span>
          <select
            className={styles.filterSelect}
            value={incidentStatusFilter}
            onChange={(e) =>
              changeIncidentFilter(e.target.value as AdminPaymentReconciliationIncidentStatus | '')
            }
          >
            <option value="">{'전체'}</option>
            {INCIDENT_STATUSES.map((status) => (
              <option key={status} value={status}>
                {status}
              </option>
            ))}
          </select>
        </div>
      )}

      {tab === 'settlements' && (
        <>
          <div className={styles.filterBar}>
            <span className={styles.filterLabel}>{'상태'}</span>
            <select
              className={styles.filterSelect}
              value={settlementStatusFilter}
              onChange={(e) =>
                changeSettlementStatusFilter(e.target.value as AdminPaymentSettlementStatus | '')
              }
            >
              <option value="">{'전체'}</option>
              {SETTLEMENT_STATUSES.map((status) => (
                <option key={status} value={status}>
                  {status}
                </option>
              ))}
            </select>
            <span className={styles.filterLabel}>{'source'}</span>
            <select
              className={styles.filterSelect}
              value={settlementSourceFilter}
              onChange={(e) =>
                changeSettlementSourceFilter(e.target.value as AdminPaymentSettlementSource | '')
              }
            >
              <option value="">{'전체'}</option>
              {SETTLEMENT_SOURCES.map((source) => (
                <option key={source} value={source}>
                  {source}
                </option>
              ))}
            </select>
            <input
              className={styles.textInput}
              type="date"
              value={settlementBaseDateFrom}
              onChange={(e) => changeSettlementDateFilter('from', e.target.value)}
            />
            <input
              className={styles.textInput}
              type="date"
              value={settlementBaseDateTo}
              onChange={(e) => changeSettlementDateFilter('to', e.target.value)}
            />
          </div>
          <SettlementOperationPanel
            busy={settlementBusy}
            file={settlementFile}
            fileInputKey={settlementFileInputKey}
            note={settlementNote}
            recoveryAttempt={settlementRecoveryAttempt}
            recoveryAvailable={settlementImportOperationKey !== null}
            result={settlementImportResult}
            onFileChange={setSettlementFile}
            onImport={() => void importSettlementFile()}
            onNoteChange={setSettlementNote}
            onReconcile={() => void reconcileSettlementGaps()}
            onRecover={() => void recoverPendingSettlementImport()}
          />
        </>
      )}

      {tab === 'refunds' && (
        <RefundOperationPanel
          amount={refundAmount}
          busy={refundBusy}
          paymentId={refundPaymentId}
          preview={refundPreview}
          reasonCode={refundReasonCode}
          reasonNote={refundReasonNote}
          onAmountChange={setRefundAmount}
          onPaymentIdChange={changeRefundPaymentId}
          onPreview={() => void previewRefund()}
          onReasonCodeChange={setRefundReasonCode}
          onReasonNoteChange={setRefundReasonNote}
          onRequest={() => void requestRefund()}
        />
      )}

      {tab === 'corrections' && (
        <EntitlementCorrectionPanel
          busy={correctionBusy}
          form={correctionForm}
          mutationBlocked={Boolean(
            (refundRecoveries[Number(correctionForm.paymentRefundId)] &&
              isAmbiguousOutcome(
                refundRecoveries[Number(correctionForm.paymentRefundId)].outcome,
              )) ||
            hasAmbiguousCorrectionIntent(Number(correctionForm.paymentRefundId)),
          )}
          plans={plans}
          preview={correctionPreview}
          onChange={changeCorrectionForm}
          onPreview={() => void previewCorrection()}
          onRequest={() => void requestCorrection()}
        />
      )}

      {loading && <div className={styles.loading}>{'불러오는 중...'}</div>}
      {error && <div className={styles.error}>{error}</div>}
      {!loading && !error && tab === 'orders' && <OrderTable orders={orders} />}
      {!loading && !error && tab === 'agreements' && <AgreementTable agreements={agreements} />}
      {!loading && !error && tab === 'payments' && (
        <PaymentTable
          payments={payments}
          onRefundSelect={(payment) => void previewRefund(payment.id)}
        />
      )}
      {!loading && !error && tab === 'incidents' && (
        <IncidentTable
          incidents={incidents}
          edits={incidentEdits}
          updatingIncidentId={updatingIncidentId}
          onEditChange={changeIncidentEdit}
          onSave={saveIncident}
        />
      )}
      {!loading && !error && tab === 'receipts' && <ReceiptTable receipts={receipts} />}
      {!loading && !error && tab === 'audits' && <AuditTable audits={audits} />}
      {!loading && !error && tab === 'settlements' && (
        <SettlementTable
          busy={settlementBusy}
          ignoreNotes={settlementIgnoreNotes}
          settlements={settlements}
          onIgnore={(settlement) => void ignoreSettlement(settlement)}
          onNoteChange={changeSettlementIgnoreNote}
        />
      )}
      {!loading && !error && tab === 'refunds' && (
        <RefundTable
          actionNotes={refundActionNotes}
          busy={refundBusy}
          executePending={refundExecutePending}
          isMutationBlocked={isRefundMutationBlocked}
          readPending={recoveryReads}
          recoveries={refundRecoveries}
          refunds={refunds}
          onApprove={(refund) => void approveRefund(refund)}
          onCheckStatus={checkRefundStatus}
          onExecute={(refund) => void executeRefund(refund)}
          onNoteChange={changeRefundActionNote}
          onPrepareCorrection={(refund) => {
            changeTab('corrections');
            changeCorrectionForm({ paymentRefundId: String(refund.id) });
          }}
        />
      )}
      {!loading && !error && tab === 'corrections' && (
        <EntitlementCorrectionTable
          actionNotes={correctionActionNotes}
          busy={correctionBusy}
          corrections={corrections}
          executePending={correctionExecutePending}
          isMutationBlocked={isCorrectionMutationBlocked}
          readPending={recoveryReads}
          recoveries={correctionRecoveries}
          onApprove={(correction) => void approveCorrection(correction)}
          onCheckStatus={checkCorrectionStatus}
          onExecute={(correction) => void executeCorrection(correction)}
          onNoteChange={changeCorrectionActionNote}
        />
      )}

      {pageInfo && pageInfo.total > pageInfo.size && (
        <Pagination pageInfo={pageInfo} currentPage={page} onPageChange={setPage} />
      )}

      <ConfirmDialog
        open={confirmation !== null}
        title={confirmation?.title ?? ''}
        message={confirmation?.message ?? ''}
        confirmLabel={confirmation?.confirmLabel}
        confirmVariant={confirmation?.confirmVariant}
        busy={confirmationBusy}
        onConfirm={() => void confirmOperation()}
        onCancel={cancelConfirmation}
      />
    </div>
  );
}

function TabButton({
  active,
  children,
  onClick,
}: {
  active: boolean;
  children: string;
  onClick: () => void;
}) {
  return (
    <button className={active ? styles.tabActive : styles.tab} onClick={onClick} type="button">
      {children}
    </button>
  );
}

function buildIncidentEdits(
  incidents: AdminPaymentReconciliationIncident[],
): Record<number, IncidentEdit> {
  return incidents.reduce<Record<number, IncidentEdit>>((acc, incident) => {
    acc[incident.id] = {
      status: incident.status,
      note: incident.resolutionNote ?? '',
    };
    return acc;
  }, {});
}

function SettlementOperationPanel({
  busy,
  file,
  fileInputKey,
  note,
  recoveryAttempt,
  recoveryAvailable,
  result,
  onFileChange,
  onImport,
  onNoteChange,
  onReconcile,
  onRecover,
}: {
  busy: string | null;
  file: File | null;
  fileInputKey: number;
  note: string;
  recoveryAttempt: AdminPaymentSettlementImportAttempt | null;
  recoveryAvailable: boolean;
  result: AdminPaymentSettlementImportResult | null;
  onFileChange: (file: File | null) => void;
  onImport: () => void;
  onNoteChange: (note: string) => void;
  onReconcile: () => void;
  onRecover: () => void;
}) {
  return (
    <section className={styles.operationPanel}>
      <div className={styles.panelHeader}>
        <div>
          <h2>{'정산 import'}</h2>
          <p>{'CSV source adapter로 정산 근거를 저장하고 내부 결제/환불 원장과 대조합니다.'}</p>
        </div>
        <span className={styles.panelBadge}>{'CSV_MANUAL'}</span>
      </div>
      <div className={styles.formGrid}>
        <label className={styles.fieldWide}>
          <span>{'정산 CSV'}</span>
          <input
            accept=".csv,text/csv"
            className={styles.fileInput}
            key={fileInputKey}
            type="file"
            onChange={(e) => onFileChange(e.target.files?.[0] ?? null)}
          />
          <strong className={styles.fileName}>{file?.name ?? '선택된 파일 없음'}</strong>
        </label>
        <label className={styles.fieldWide}>
          <span>
            {'운영 메모'}
            <small className={styles.securityHint} id="settlement-operator-note-security-hint">
              {'개인정보, 인증정보, 결제 키 등 민감정보를 입력하지 마세요.'}
            </small>
          </span>
          <textarea
            aria-describedby="settlement-operator-note-security-hint"
            className={styles.noteInput}
            maxLength={500}
            value={note}
            onChange={(e) => onNoteChange(e.target.value)}
            placeholder="정산 import 근거"
          />
        </label>
      </div>
      {result && (
        <div className={styles.previewBox}>
          <PreviewItem label="batch" value={result.importBatchKey} />
          <PreviewItem label="rows" value={String(result.totalRows)} />
          <PreviewItem label="imported" value={String(result.importedRows)} />
          <PreviewItem label="duplicates" value={String(result.skippedDuplicateRows)} />
          <PreviewItem label="failed" value={String(result.failedRows)} />
          <PreviewItem label="statuses" value={formatStatusCounts(result.statusCounts)} />
        </div>
      )}
      {result && (result.failedRows > 0 || result.errors.length > 0) && (
        <div className={styles.errorList} role="status">
          {result.failedRows > 0 && (
            <strong>{`부분 완료: ${result.failedRows}개 row를 수정한 뒤 다시 import해주세요.`}</strong>
          )}
          {result.errors.map((error) => (
            <div key={`${error.rowNumber}-${error.message}`}>
              {`row ${error.rowNumber}: ${error.message}`}
            </div>
          ))}
        </div>
      )}
      {recoveryAttempt && (
        <div className={styles.previewBox} role="status">
          <PreviewItem label="attempt" value={String(recoveryAttempt.attemptId)} />
          <PreviewItem label="state" value={recoveryAttempt.state} />
          <PreviewItem label="rows" value={String(recoveryAttempt.totalRows)} />
          <PreviewItem label="imported" value={String(recoveryAttempt.importedRows)} />
          <PreviewItem label="duplicates" value={String(recoveryAttempt.skippedDuplicateRows)} />
          <PreviewItem label="failed" value={String(recoveryAttempt.failedRows)} />
        </div>
      )}
      <div className={styles.actionBar}>
        <button
          className={styles.saveBtn}
          disabled={!file || busy === 'import'}
          onClick={onImport}
          type="button"
        >
          {busy === 'import' ? 'import 중' : '정산 import'}
        </button>
        {recoveryAvailable && (
          <button
            className={styles.secondaryBtn}
            disabled={busy !== null}
            onClick={onRecover}
            type="button"
          >
            {busy === 'recover' ? '복구 중' : 'import 결과 복구'}
          </button>
        )}
        <button
          className={styles.secondaryBtn}
          disabled={busy === 'reconcile'}
          onClick={onReconcile}
          type="button"
        >
          {busy === 'reconcile' ? '확인 중' : '누락 후보 확인'}
        </button>
      </div>
    </section>
  );
}

function RefundOperationPanel({
  amount,
  busy,
  paymentId,
  preview,
  reasonCode,
  reasonNote,
  onAmountChange,
  onPaymentIdChange,
  onPreview,
  onReasonCodeChange,
  onReasonNoteChange,
  onRequest,
}: {
  amount: string;
  busy: string | null;
  paymentId: string;
  preview: AdminPaymentRefundPreview | null;
  reasonCode: AdminPaymentRefundReasonCode;
  reasonNote: string;
  onAmountChange: (value: string) => void;
  onPaymentIdChange: (value: string) => void;
  onPreview: () => void;
  onReasonCodeChange: (value: AdminPaymentRefundReasonCode) => void;
  onReasonNoteChange: (value: string) => void;
  onRequest: () => void;
}) {
  return (
    <section className={styles.operationPanel}>
      <div className={styles.panelHeader}>
        <div>
          <h2>{'환불 요청'}</h2>
          <p>{'환불은 요청 원장 생성 후 승인, provider 실행을 별도로 진행합니다.'}</p>
        </div>
        <span className={styles.panelBadge}>{'ADMIN ONLY'}</span>
      </div>
      <div className={styles.formGrid}>
        <label className={styles.field}>
          <span>{'결제내역 ID'}</span>
          <input
            className={styles.textInput}
            inputMode="numeric"
            value={paymentId}
            onChange={(e) => onPaymentIdChange(e.target.value)}
            placeholder="subscriptionPaymentId"
          />
        </label>
        <label className={styles.field}>
          <span>{'환불 금액'}</span>
          <input
            className={styles.textInput}
            inputMode="numeric"
            value={amount}
            onChange={(e) => onAmountChange(e.target.value)}
            placeholder="0"
          />
        </label>
        <label className={styles.field}>
          <span>{'사유'}</span>
          <select
            className={styles.filterSelect}
            value={reasonCode}
            onChange={(e) => onReasonCodeChange(e.target.value as AdminPaymentRefundReasonCode)}
          >
            {REFUND_REASONS.map((reason) => (
              <option key={reason} value={reason}>
                {reason}
              </option>
            ))}
          </select>
        </label>
        <label className={styles.fieldWide}>
          <span>{'운영 메모'}</span>
          <textarea
            className={styles.noteInput}
            maxLength={500}
            value={reasonNote}
            onChange={(e) => onReasonNoteChange(e.target.value)}
            placeholder="고객 문의/incident/승인 근거"
          />
        </label>
      </div>
      {preview && (
        <div className={styles.previewBox}>
          <PreviewItem label="사용자" value={`${preview.userNickname} (#${preview.userId})`} />
          <PreviewItem label="주문번호" value={preview.orderId ?? '-'} />
          <PreviewItem label="원 결제액" value={formatPrice(preview.originalAmount)} />
          <PreviewItem
            label="예약/환불액"
            value={formatPrice(preview.alreadyRefundedOrReservedAmount)}
          />
          <PreviewItem label="환불 가능액" value={formatPrice(preview.refundableAmount)} />
          <PreviewItem
            label="상태"
            value={preview.refundable ? '환불 가능' : (preview.reason ?? '불가')}
          />
        </div>
      )}
      <div className={styles.actionBar}>
        <button
          className={styles.secondaryBtn}
          disabled={busy === 'preview'}
          onClick={onPreview}
          type="button"
        >
          {busy === 'preview' ? '확인 중' : '환불 미리보기'}
        </button>
        <button
          className={styles.saveBtn}
          disabled={!preview?.refundable || busy === 'create'}
          onClick={onRequest}
          type="button"
        >
          {busy === 'create' ? '요청 중' : '환불 요청 생성'}
        </button>
      </div>
    </section>
  );
}

function EntitlementCorrectionPanel({
  busy,
  form,
  mutationBlocked,
  plans,
  preview,
  onChange,
  onPreview,
  onRequest,
}: {
  busy: string | null;
  form: CorrectionForm;
  mutationBlocked: boolean;
  plans: SubscriptionPlan[];
  preview: AdminPaymentEntitlementCorrectionPreview | null;
  onChange: (patch: Partial<CorrectionForm>) => void;
  onPreview: () => void;
  onRequest: () => void;
}) {
  return (
    <section className={styles.operationPanel}>
      <div className={styles.panelHeader}>
        <div>
          <h2>{'권한 보정 요청'}</h2>
          <p>
            {'성공한 환불 이후에만 명시한 목표 구독 상태로 로컬 권한을 보정합니다.'}
            {' 일반 사용자 구독 관리와 달리 환불 근거가 있는 보정 작업만 다룹니다.'}
          </p>
        </div>
        <span className={styles.panelBadge}>{'LOCAL ACCESS'}</span>
      </div>
      <div className={styles.formGrid}>
        <label className={styles.field}>
          <span>{'환불 ID'}</span>
          <input
            className={styles.textInput}
            inputMode="numeric"
            value={form.paymentRefundId}
            onChange={(e) => onChange({ paymentRefundId: e.target.value })}
            placeholder="succeeded refund id"
          />
        </label>
        <label className={styles.field}>
          <span>{'대상 플랜'}</span>
          <select
            className={styles.filterSelect}
            value={form.targetSubscriptionId}
            onChange={(e) => onChange({ targetSubscriptionId: e.target.value })}
          >
            <option value="">{'선택'}</option>
            {plans.map((plan) => (
              <option key={plan.id} value={plan.id}>
                {`${plan.name} / ${plan.userType}`}
              </option>
            ))}
          </select>
        </label>
        <label className={styles.field}>
          <span>{'목표 주기'}</span>
          <select
            className={styles.filterSelect}
            value={form.targetBillingCycle}
            onChange={(e) =>
              onChange({
                targetBillingCycle: e.target.value as AdminPaymentEntitlementCorrectionBillingCycle,
              })
            }
          >
            {BILLING_CYCLES.map((cycle) => (
              <option key={cycle} value={cycle}>
                {cycle}
              </option>
            ))}
          </select>
        </label>
        <label className={styles.field}>
          <span>{'목표 상태'}</span>
          <select
            className={styles.filterSelect}
            value={form.targetStatus}
            onChange={(e) =>
              onChange({
                targetStatus: e.target.value as AdminPaymentEntitlementCorrectionSubscriptionStatus,
              })
            }
          >
            {SUBSCRIPTION_STATUSES.map((status) => (
              <option key={status} value={status}>
                {status}
              </option>
            ))}
          </select>
        </label>
        <label className={styles.field}>
          <span>{'목표 만료일'}</span>
          <input
            className={styles.textInput}
            type="date"
            value={form.targetExpiresAt}
            onChange={(e) => onChange({ targetExpiresAt: e.target.value })}
          />
        </label>
        <label className={styles.checkField}>
          <input
            checked={form.clearPendingChange}
            type="checkbox"
            onChange={(e) => onChange({ clearPendingChange: e.target.checked })}
          />
          <span>{'예약 변경 제거'}</span>
        </label>
        <label className={styles.checkField}>
          <input
            checked={form.cancelBillingAgreement}
            type="checkbox"
            onChange={(e) => onChange({ cancelBillingAgreement: e.target.checked })}
          />
          <span>{'로컬 자동결제 취소'}</span>
        </label>
        <label className={styles.fieldWide}>
          <span>{'운영 메모'}</span>
          <textarea
            className={styles.noteInput}
            maxLength={500}
            value={form.reasonNote}
            onChange={(e) => onChange({ reasonNote: e.target.value })}
            placeholder="환불 후 권한 보정 근거"
          />
        </label>
      </div>
      {preview && (
        <div className={styles.previewBox}>
          <PreviewItem label="사용자" value={`${preview.userNickname} (#${preview.userId})`} />
          <PreviewItem
            label="현재"
            value={`${preview.currentPlanName} / ${preview.currentBillingCycle} / ${preview.currentStatus}`}
          />
          <PreviewItem
            label="목표"
            value={`${preview.targetPlanName} / ${preview.targetBillingCycle} / ${preview.targetStatus}`}
          />
          <PreviewItem label="만료일" value={formatDate(preview.targetExpiresAt)} />
          <PreviewItem
            label="자동결제"
            value={`${preview.currentBillingAgreementStatus ?? '-'} -> ${
              preview.targetBillingAgreementStatus ?? '-'
            }`}
          />
          <PreviewItem
            label="상태"
            value={preview.executable ? '실행 가능' : (preview.reason ?? '불가')}
          />
        </div>
      )}
      <div className={styles.actionBar}>
        <button
          className={styles.secondaryBtn}
          disabled={busy === 'preview'}
          onClick={onPreview}
          type="button"
        >
          {busy === 'preview' ? '확인 중' : '권한 보정 미리보기'}
        </button>
        <button
          className={styles.saveBtn}
          disabled={!preview?.executable || busy === 'create' || mutationBlocked}
          onClick={onRequest}
          type="button"
        >
          {busy === 'create' ? '요청 중' : '권한 보정 요청 생성'}
        </button>
      </div>
    </section>
  );
}

function PreviewItem({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

function OrderTable({ orders }: { orders: AdminPaymentOrder[] }) {
  return (
    <div className={styles.tableWrap}>
      <table className={styles.table}>
        <thead>
          <tr>
            <th>{'주문번호'}</th>
            <th>{'사용자'}</th>
            <th>{'목적'}</th>
            <th>{'PG'}</th>
            <th>{'상태'}</th>
            <th>{'플랜'}</th>
            <th>{'금액'}</th>
            <th>{'실패'}</th>
            <th>{'생성일'}</th>
          </tr>
        </thead>
        <tbody>
          {orders.length === 0 && <EmptyRow colSpan={9} />}
          {orders.map((order) => (
            <tr key={order.id}>
              <td>{order.orderId}</td>
              <td>{order.userNickname}</td>
              <td>{order.purpose}</td>
              <td>{order.provider}</td>
              <td>
                <span className={statusClass(order.status)}>{order.status}</span>
              </td>
              <td>{order.subscriptionName}</td>
              <td>{formatPrice(order.amount)}</td>
              <td>{order.failureCode ?? '-'}</td>
              <td>{formatDateTime(order.createdAt)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function AgreementTable({ agreements }: { agreements: AdminBillingAgreement[] }) {
  return (
    <div className={styles.tableWrap}>
      <table className={styles.table}>
        <thead>
          <tr>
            <th>ID</th>
            <th>{'사용자'}</th>
            <th>{'PG'}</th>
            <th>{'상태'}</th>
            <th>{'수단'}</th>
            <th>{'다음 결제일'}</th>
            <th>{'실패 횟수'}</th>
            <th>{'해지일'}</th>
          </tr>
        </thead>
        <tbody>
          {agreements.length === 0 && <EmptyRow colSpan={8} />}
          {agreements.map((agreement) => (
            <tr key={agreement.id}>
              <td>{agreement.id}</td>
              <td>{agreement.userNickname}</td>
              <td>{agreement.provider}</td>
              <td>
                <span className={statusClass(agreement.status)}>{agreement.status}</span>
              </td>
              <td>
                {agreement.maskedMethod
                  ? `${agreement.payMethod ?? 'CARD'} ${agreement.maskedMethod}`
                  : (agreement.payMethod ?? '-')}
              </td>
              <td>{formatDate(agreement.nextBillingAt)}</td>
              <td>{agreement.failureCount}</td>
              <td>{formatDateTime(agreement.cancelledAt)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function PaymentTable({
  payments,
  onRefundSelect,
}: {
  payments: AdminSubscriptionPayment[];
  onRefundSelect: (payment: AdminSubscriptionPayment) => void;
}) {
  return (
    <div className={styles.tableWrap}>
      <table className={styles.table}>
        <thead>
          <tr>
            <th>ID</th>
            <th>{'사용자'}</th>
            <th>{'주문번호'}</th>
            <th>{'플랜'}</th>
            <th>{'주기'}</th>
            <th>{'PG'}</th>
            <th>{'상태'}</th>
            <th>{'금액'}</th>
            <th>{'생성일'}</th>
            <th>{'운영'}</th>
          </tr>
        </thead>
        <tbody>
          {payments.length === 0 && <EmptyRow colSpan={10} />}
          {payments.map((payment) => (
            <tr key={payment.id}>
              <td>{payment.id}</td>
              <td>{payment.userNickname}</td>
              <td>{payment.orderId ?? '-'}</td>
              <td>{payment.subscriptionName}</td>
              <td>{payment.billingCycle}</td>
              <td>{payment.provider ?? '-'}</td>
              <td>
                <span className={statusClass(payment.paymentStatus)}>{payment.paymentStatus}</span>
              </td>
              <td>{formatPrice(payment.amount)}</td>
              <td>{formatDateTime(payment.createdAt)}</td>
              <td>
                <button
                  className={styles.compactBtn}
                  disabled={payment.paymentStatus !== 'DONE'}
                  onClick={() => onRefundSelect(payment)}
                  type="button"
                >
                  {'환불'}
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function ReceiptTable({ receipts }: { receipts: AdminPaymentReceipt[] }) {
  return (
    <div className={styles.tableWrap}>
      <table className={styles.table}>
        <thead>
          <tr>
            <th>ID</th>
            <th>{'사용자'}</th>
            <th>{'주문번호'}</th>
            <th>{'결제내역'}</th>
            <th>{'유형'}</th>
            <th>{'상태'}</th>
            <th>{'지원 참조'}</th>
            <th>{'영수증'}</th>
            <th>{'발급일'}</th>
            <th>{'생성일'}</th>
          </tr>
        </thead>
        <tbody>
          {receipts.length === 0 && <EmptyRow colSpan={10} />}
          {receipts.map((receipt) => {
            const safeReceiptUrl = getSafeReceiptUrl(receipt.receiptUrl);
            return (
              <tr key={receipt.id}>
                <td>{receipt.id}</td>
                <td>{receipt.userNickname}</td>
                <td>{receipt.orderId}</td>
                <td>{receipt.subscriptionPaymentId}</td>
                <td>{receipt.type}</td>
                <td>
                  <span className={statusClass(receipt.status)}>{receipt.status}</span>
                </td>
                <td>{receipt.providerReference ?? '-'}</td>
                <td>
                  {safeReceiptUrl ? (
                    <a href={safeReceiptUrl} rel="noreferrer" target="_blank">
                      {'열기'}
                    </a>
                  ) : (
                    (receipt.receiptReference ?? (receipt.receiptUrl ? '링크 확인 필요' : '-'))
                  )}
                </td>
                <td>{formatDateTime(receipt.issuedAt)}</td>
                <td>{formatDateTime(receipt.createdAt)}</td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}

function AuditTable({ audits }: { audits: AdminPaymentOperationAuditLog[] }) {
  return (
    <div className={styles.tableWrap}>
      <table className={styles.table}>
        <thead>
          <tr>
            <th>ID</th>
            <th>{'Action'}</th>
            <th>{'Target'}</th>
            <th>{'Actor'}</th>
            <th>{'사용자'}</th>
            <th>{'주문번호'}</th>
            <th>{'상태 변경'}</th>
            <th>{'사유'}</th>
            <th>{'메모'}</th>
            <th>{'생성일'}</th>
          </tr>
        </thead>
        <tbody>
          {audits.length === 0 && <EmptyRow colSpan={10} />}
          {audits.map((audit) => (
            <tr key={audit.id}>
              <td>{audit.id}</td>
              <td>
                <div className={styles.issueType}>{audit.action}</div>
              </td>
              <td>
                <div>{audit.targetType}</div>
                <div className={styles.subtle}>{audit.targetId ?? '-'}</div>
              </td>
              <td>{audit.actorEmail ?? audit.actorUserId ?? '-'}</td>
              <td>{audit.targetUserNickname ?? audit.targetUserId ?? '-'}</td>
              <td>{audit.orderId ?? '-'}</td>
              <td>
                <div>{audit.beforeStatus ?? '-'}</div>
                <div className={styles.subtle}>{audit.afterStatus ?? '-'}</div>
              </td>
              <td>{audit.reasonCode ?? '-'}</td>
              <td className={styles.wrapCell}>{audit.note ?? '-'}</td>
              <td>{formatDateTime(audit.createdAt)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function SettlementTable({
  busy,
  ignoreNotes,
  settlements,
  onIgnore,
  onNoteChange,
}: {
  busy: string | null;
  ignoreNotes: Record<number, string>;
  settlements: AdminPaymentSettlement[];
  onIgnore: (settlement: AdminPaymentSettlement) => void;
  onNoteChange: (settlementId: number, note: string) => void;
}) {
  return (
    <div className={styles.tableWrap}>
      <table className={styles.table}>
        <thead>
          <tr>
            <th>ID</th>
            <th>{'상태'}</th>
            <th>{'주문번호'}</th>
            <th>{'사용자'}</th>
            <th>{'Source'}</th>
            <th>{'금액'}</th>
            <th>{'정산일'}</th>
            <th>{'Local'}</th>
            <th>{'사유'}</th>
            <th>{'처리'}</th>
          </tr>
        </thead>
        <tbody>
          {settlements.length === 0 && <EmptyRow colSpan={10} />}
          {settlements.map((settlement) => (
            <tr key={settlement.id}>
              <td>{settlement.id}</td>
              <td>
                <span className={statusClass(settlement.status)}>{settlement.status}</span>
              </td>
              <td>
                <div>{settlement.orderId}</div>
                <div className={styles.subtle}>{settlement.providerReference ?? '-'}</div>
              </td>
              <td>{settlement.userNickname ?? settlement.userId ?? '-'}</td>
              <td>
                <div>{settlement.source}</div>
                <div className={styles.subtle}>{settlement.provider}</div>
                <div className={styles.subtle}>
                  {settlement.sourceFileName
                    ? `${settlement.sourceFileName}:${settlement.sourceRowNumber ?? '-'}`
                    : '-'}
                </div>
              </td>
              <td>
                <div>{`gross ${formatPrice(settlement.grossAmount)}`}</div>
                <div
                  className={styles.subtle}
                >{`refund ${formatPrice(settlement.refundAmount)}`}</div>
                <div className={styles.subtle}>{`fee ${formatPrice(settlement.feeAmount)}`}</div>
                <div
                  className={styles.subtle}
                >{`net ${formatPrice(settlement.netSettlementAmount)}`}</div>
              </td>
              <td>
                <div>{formatDate(settlement.settlementBaseDate)}</div>
                <div className={styles.subtle}>{formatDate(settlement.settlementPayoutDate)}</div>
              </td>
              <td>
                <div>{settlement.paymentOrderId ? `order #${settlement.paymentOrderId}` : '-'}</div>
                <div className={styles.subtle}>
                  {settlement.subscriptionPaymentId
                    ? `payment #${settlement.subscriptionPaymentId}`
                    : '-'}
                </div>
              </td>
              <td className={styles.wrapCell}>{settlement.mismatchReason ?? '-'}</td>
              <td>
                <div className={styles.operationActions}>
                  <textarea
                    className={styles.noteInput}
                    maxLength={500}
                    value={ignoreNotes[settlement.id] ?? ''}
                    onChange={(e) => onNoteChange(settlement.id, e.target.value)}
                    placeholder="IGNORE 메모"
                  />
                  <button
                    className={styles.compactBtn}
                    disabled={settlement.status === 'IGNORED' || busy === `ignore-${settlement.id}`}
                    onClick={() => onIgnore(settlement)}
                    type="button"
                  >
                    {'IGNORE'}
                  </button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function RefundTable({
  actionNotes,
  busy,
  executePending,
  isMutationBlocked,
  readPending,
  recoveries,
  refunds,
  onApprove,
  onCheckStatus,
  onExecute,
  onNoteChange,
  onPrepareCorrection,
}: {
  actionNotes: Record<number, string>;
  busy: string | null;
  executePending: Record<number, boolean>;
  isMutationBlocked: (refundId: number) => boolean;
  readPending: Record<string, boolean>;
  recoveries: Record<number, RefundRecoveryIntent>;
  refunds: AdminPaymentRefund[];
  onApprove: (refund: AdminPaymentRefund) => void;
  onCheckStatus: (refundId: number) => void;
  onExecute: (refund: AdminPaymentRefund) => void;
  onNoteChange: (refundId: number, note: string) => void;
  onPrepareCorrection: (refund: AdminPaymentRefund) => void;
}) {
  return (
    <div className={styles.tableWrap}>
      <table className={styles.table}>
        <thead>
          <tr>
            <th>ID</th>
            <th>{'사용자'}</th>
            <th>{'주문번호'}</th>
            <th>{'결제내역'}</th>
            <th>{'상태'}</th>
            <th>{'금액'}</th>
            <th>{'사유'}</th>
            <th>{'Provider'}</th>
            <th>{'일시'}</th>
            <th>{'처리'}</th>
          </tr>
        </thead>
        <tbody>
          {refunds.length === 0 && <EmptyRow colSpan={10} />}
          {refunds.map((refund) => (
            <tr key={refund.id}>
              <td>{refund.id}</td>
              <td>{refund.userNickname}</td>
              <td>{refund.orderId}</td>
              <td>{refund.subscriptionPaymentId}</td>
              <td>
                <span className={statusClass(refund.status)}>{refund.status}</span>
                {refund.failureCode && <div className={styles.subtle}>{refund.failureCode}</div>}
                {recoveries[refund.id] && (
                  <div className={styles.subtle} data-testid={`refund-recovery-${refund.id}`}>
                    {recoveries[refund.id].outcome}
                  </div>
                )}
                {recoveries[refund.id] && isAmbiguousOutcome(recoveries[refund.id].outcome) && (
                  <button
                    className={styles.compactBtn}
                    disabled={
                      readPending[`refund-${refund.id}`] || executePending[refund.id] === true
                    }
                    onClick={() => onCheckStatus(refund.id)}
                    type="button"
                  >
                    {'상태 다시 확인'}
                  </button>
                )}
              </td>
              <td>{formatPrice(refund.amount)}</td>
              <td>
                <div>{refund.reasonCode}</div>
                <div className={styles.subtle}>{refund.reasonNote ?? '-'}</div>
              </td>
              <td>
                <div>{refund.provider}</div>
                <div className={styles.subtle}>{refund.providerRefundReference ?? '-'}</div>
              </td>
              <td>
                <div>{formatDateTime(refund.createdAt)}</div>
                <div className={styles.subtle}>{formatDateTime(refund.executedAt)}</div>
              </td>
              <td>
                <div className={styles.operationActions}>
                  <textarea
                    className={styles.noteInput}
                    maxLength={500}
                    value={actionNotes[refund.id] ?? ''}
                    onChange={(e) => onNoteChange(refund.id, e.target.value)}
                    placeholder="승인/실행 메모"
                  />
                  <div className={styles.buttonRow}>
                    <button
                      className={styles.compactBtn}
                      disabled={
                        refund.status !== 'REQUESTED' ||
                        busy === `approve-${refund.id}` ||
                        isMutationBlocked(refund.id)
                      }
                      onClick={() => onApprove(refund)}
                      type="button"
                    >
                      {'승인'}
                    </button>
                    <button
                      className={styles.compactDangerBtn}
                      disabled={
                        refund.status !== 'APPROVED' ||
                        busy === `execute-${refund.id}` ||
                        isMutationBlocked(refund.id)
                      }
                      title={`실행하려면 '${REFUND_EXECUTION_CONFIRM_TEXT}' 입력이 필요합니다.`}
                      onClick={() => onExecute(refund)}
                      type="button"
                    >
                      {'실행'}
                    </button>
                    <button
                      className={styles.compactBtn}
                      disabled={refund.status !== 'SUCCEEDED' || isMutationBlocked(refund.id)}
                      onClick={() => onPrepareCorrection(refund)}
                      type="button"
                    >
                      {'권한 보정'}
                    </button>
                  </div>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function EntitlementCorrectionTable({
  actionNotes,
  busy,
  corrections,
  executePending,
  isMutationBlocked,
  readPending,
  recoveries,
  onApprove,
  onCheckStatus,
  onExecute,
  onNoteChange,
}: {
  actionNotes: Record<number, string>;
  busy: string | null;
  corrections: AdminPaymentEntitlementCorrection[];
  executePending: Record<number, boolean>;
  isMutationBlocked: (correction: AdminPaymentEntitlementCorrection) => boolean;
  readPending: Record<string, boolean>;
  recoveries: Record<number, CorrectionRecoveryIntent>;
  onApprove: (correction: AdminPaymentEntitlementCorrection) => void;
  onCheckStatus: (correctionId: number) => void;
  onExecute: (correction: AdminPaymentEntitlementCorrection) => void;
  onNoteChange: (correctionId: number, note: string) => void;
}) {
  return (
    <div className={styles.tableWrap}>
      <table className={styles.table}>
        <thead>
          <tr>
            <th>ID</th>
            <th>{'환불'}</th>
            <th>{'사용자'}</th>
            <th>{'상태'}</th>
            <th>{'Before'}</th>
            <th>{'Target'}</th>
            <th>{'예약/자동결제'}</th>
            <th>{'일시'}</th>
            <th>{'처리'}</th>
          </tr>
        </thead>
        <tbody>
          {corrections.length === 0 && <EmptyRow colSpan={9} />}
          {corrections.map((correction) => (
            <tr key={correction.id}>
              <td>{correction.id}</td>
              <td>{correction.paymentRefundId}</td>
              <td>{correction.userNickname}</td>
              <td>
                <span className={statusClass(correction.status)}>{correction.status}</span>
                {correction.failureCode && (
                  <div className={styles.subtle}>{correction.failureCode}</div>
                )}
                {recoveries[correction.id] && (
                  <div
                    className={styles.subtle}
                    data-testid={`correction-recovery-${correction.id}`}
                  >
                    {recoveries[correction.id].outcome}
                  </div>
                )}
                {recoveries[correction.id] &&
                  isAmbiguousOutcome(recoveries[correction.id].outcome) && (
                    <button
                      className={styles.compactBtn}
                      disabled={
                        readPending[`correction-${correction.id}`] ||
                        executePending[correction.id] === true
                      }
                      onClick={() => onCheckStatus(correction.id)}
                      type="button"
                    >
                      {'상태 다시 확인'}
                    </button>
                  )}
              </td>
              <td>
                <div>{correction.beforePlanName}</div>
                <div className={styles.subtle}>
                  {`${correction.beforeBillingCycle} / ${correction.beforeStatus}`}
                </div>
                <div className={styles.subtle}>{formatDate(correction.beforeExpiresAt)}</div>
              </td>
              <td>
                <div>{correction.targetPlanName}</div>
                <div className={styles.subtle}>
                  {`${correction.targetBillingCycle} / ${correction.targetStatus}`}
                </div>
                <div className={styles.subtle}>{formatDate(correction.targetExpiresAt)}</div>
              </td>
              <td>
                <div>{correction.clearPendingChange ? '예약 제거' : '예약 유지'}</div>
                <div className={styles.subtle}>
                  {correction.cancelBillingAgreement ? '로컬 자동결제 취소' : '자동결제 유지'}
                </div>
              </td>
              <td>
                <div>{formatDateTime(correction.createdAt)}</div>
                <div className={styles.subtle}>{formatDateTime(correction.executedAt)}</div>
              </td>
              <td>
                <div className={styles.operationActions}>
                  <textarea
                    className={styles.noteInput}
                    maxLength={500}
                    value={actionNotes[correction.id] ?? ''}
                    onChange={(e) => onNoteChange(correction.id, e.target.value)}
                    placeholder="승인/실행 메모"
                  />
                  <div className={styles.buttonRow}>
                    <button
                      className={styles.compactBtn}
                      disabled={
                        correction.status !== 'REQUESTED' ||
                        busy === `approve-${correction.id}` ||
                        isMutationBlocked(correction)
                      }
                      onClick={() => onApprove(correction)}
                      type="button"
                    >
                      {'승인'}
                    </button>
                    <button
                      className={styles.compactDangerBtn}
                      disabled={
                        correction.status !== 'APPROVED' ||
                        busy === `execute-${correction.id}` ||
                        isMutationBlocked(correction)
                      }
                      title={`실행하려면 '${CORRECTION_EXECUTION_CONFIRM_TEXT}' 입력이 필요합니다.`}
                      onClick={() => onExecute(correction)}
                      type="button"
                    >
                      {'실행'}
                    </button>
                  </div>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function IncidentTable({
  incidents,
  edits,
  updatingIncidentId,
  onEditChange,
  onSave,
}: {
  incidents: AdminPaymentReconciliationIncident[];
  edits: Record<number, IncidentEdit>;
  updatingIncidentId: number | null;
  onEditChange: (incidentId: number, patch: Partial<IncidentEdit>) => void;
  onSave: (incident: AdminPaymentReconciliationIncident) => void;
}) {
  return (
    <div className={styles.tableWrap}>
      <table className={styles.table}>
        <thead>
          <tr>
            <th>{'심각도'}</th>
            <th>{'유형'}</th>
            <th>{'상태'}</th>
            <th>{'주문번호'}</th>
            <th>{'사용자'}</th>
            <th>{'PG/목적'}</th>
            <th>{'로컬'}</th>
            <th>{'Provider'}</th>
            <th>{'금액'}</th>
            <th>{'발생'}</th>
            <th>{'탐지'}</th>
            <th>{'처리'}</th>
          </tr>
        </thead>
        <tbody>
          {incidents.length === 0 && <EmptyRow colSpan={12} />}
          {incidents.map((incident) => {
            const edit = edits[incident.id] ?? {
              status: incident.status,
              note: incident.resolutionNote ?? '',
            };
            const saving = updatingIncidentId === incident.id;
            return (
              <tr key={incident.id}>
                <td>
                  <span className={severityClass(incident.severity)}>{incident.severity}</span>
                </td>
                <td>
                  <div className={styles.issueType}>{incident.issueType}</div>
                  <div className={styles.subtle}>ID {incident.id}</div>
                </td>
                <td>
                  <span className={statusClass(incident.status)}>{incident.status}</span>
                </td>
                <td>{incident.orderId ?? '-'}</td>
                <td>{incident.userNickname ?? incident.userId ?? '-'}</td>
                <td>
                  <div>{incident.provider ?? '-'}</div>
                  <div className={styles.subtle}>{incident.purpose ?? '-'}</div>
                </td>
                <td>{incident.localStatus ?? '-'}</td>
                <td>
                  <div>{incident.providerStatus ?? '-'}</div>
                  <div className={styles.subtle}>{incident.providerReference ?? '-'}</div>
                </td>
                <td>
                  <div>{formatNullablePrice(incident.localAmount)}</div>
                  <div className={styles.subtle}>
                    {formatNullablePrice(incident.providerAmount)}
                  </div>
                </td>
                <td>{incident.occurrenceCount}</td>
                <td>
                  <div>{formatDateTime(incident.lastDetectedAt)}</div>
                  <div className={styles.subtle}>{formatDateTime(incident.notifiedAt)}</div>
                </td>
                <td>
                  <div className={styles.incidentActions}>
                    <select
                      className={styles.statusSelect}
                      value={edit.status}
                      onChange={(e) =>
                        onEditChange(incident.id, {
                          status: e.target.value as AdminPaymentReconciliationIncidentStatus,
                        })
                      }
                    >
                      {INCIDENT_STATUSES.map((status) => (
                        <option key={status} value={status}>
                          {status}
                        </option>
                      ))}
                    </select>
                    <textarea
                      className={styles.noteInput}
                      value={edit.note}
                      maxLength={500}
                      onChange={(e) => onEditChange(incident.id, { note: e.target.value })}
                      placeholder="처리 메모"
                    />
                    <button
                      className={styles.saveBtn}
                      type="button"
                      disabled={saving}
                      onClick={() => onSave(incident)}
                    >
                      {saving ? '저장 중' : '저장'}
                    </button>
                  </div>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}

function EmptyRow({ colSpan }: { colSpan: number }) {
  return (
    <tr>
      <td colSpan={colSpan} className={styles.empty}>
        {'조회된 결제 정보가 없습니다.'}
      </td>
    </tr>
  );
}

function statusClass(status: string): string {
  const stateClass = styles[`status${status}`] ?? '';
  return `${styles.statusBadge} ${stateClass}`;
}

function severityClass(severity: string): string {
  const stateClass = styles[`severity${severity}`] ?? '';
  return `${styles.statusBadge} ${stateClass}`;
}

function formatNullablePrice(value: number | null): string {
  return value == null ? '-' : formatPrice(value);
}

function formatStatusCounts(counts: Record<string, number>): string {
  const entries = Object.entries(counts);
  if (entries.length === 0) {
    return '-';
  }
  return entries.map(([key, value]) => `${key}:${value}`).join(', ');
}

function todayInputValue(): string {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function confirmTypedAction(message: string, expectedText: string): boolean {
  const input = window.prompt(`${message}\n\n계속하려면 '${expectedText}'를 정확히 입력하세요.`);
  return input === expectedText;
}
