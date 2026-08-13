import { useEffect, useMemo, useRef, useState } from 'react';
import {
  approveAdminSubscriptionCorrection,
  createAdminSubscriptionCorrection,
  executeAdminSubscriptionCorrection,
  fetchAdminSubscriptionCorrection,
  fetchOpenAdminSubscriptionCorrection,
  previewAdminSubscriptionCorrection,
  type AdminSubscriptionCorrection,
  type AdminSubscriptionCorrectionPreview,
  type AdminSubscriptionCorrectionRequest,
  type BillingAgreementStatus,
  type MySubscription,
  type SubscriptionBillingCycle,
  type UserSubscriptionStatus,
} from '@/api/userSubscriptions';
import type { SubscriptionPlan } from '@/api/subscriptions';
import { classifyLoadError } from '@/api/loadError';
import Button from '@/components/ui/Button';
import ConfirmDialog from '@/components/ui/ConfirmDialog';
import Modal from '@/components/ui/Modal';
import { formatDate } from '@/utils/format';
import styles from './UserSubscriptionManagePage.module.css';

const STATUS_LABELS: Record<UserSubscriptionStatus, string> = {
  ACTIVE: '활성',
  CANCELLED: '취소됨',
  EXPIRED: '만료',
};

const BILLING_LABELS: Record<SubscriptionBillingCycle, string> = {
  MONTHLY: '월간',
  YEARLY: '연간',
};

const AGREEMENT_LABELS: Record<BillingAgreementStatus, string> = {
  READY: '준비',
  ACTIVE: '활성',
  SUSPENDED: '중지',
  CANCELLED: '취소됨',
  EXPIRED: '만료',
};

const CORRECTION_STATUS_LABELS: Record<AdminSubscriptionCorrection['status'], string> = {
  REQUESTED: '요청됨',
  APPROVED: '승인됨',
  PROCESSING: '처리 중',
  SUCCEEDED: '성공',
  FAILED: '실패',
  CANCELLED: '취소됨',
};

const PREVIEW_REASON_LABELS: Record<string, string> = {
  'A nonblank operator reason is required.': '운영 사유를 입력해야 합니다.',
  'The operator reason must not exceed 500 characters.': '운영 사유는 500자 이하여야 합니다.',
  'The target subscription plan is inactive.': '비활성 플랜은 보정 대상으로 선택할 수 없습니다.',
  "The target subscription plan does not match the user's type.":
    '사용자 유형과 맞지 않는 플랜입니다.',
  'An expired subscription must expire today or earlier.':
    '만료 상태의 만료일은 오늘 또는 이전이어야 합니다.',
  'An active or cancelled subscription must expire today or later.':
    '활성 또는 취소됨 상태의 만료일은 오늘 또는 이후여야 합니다.',
  'A payment order can still receive a provider outcome.':
    '결제사업자 결과를 아직 받을 수 있는 진행 중 주문이 있어 보정할 수 없습니다.',
  'The requested correction does not change local subscription state.':
    '현재 로컬 구독 상태와 달라지는 항목이 없습니다.',
};

type WorkflowBusy = 'open' | 'preview' | 'request' | 'approve' | 'execute' | 'status' | null;
type ConfirmationStage = 'approve' | 'execute' | null;
type OpenLookupState = 'idle' | 'loading' | 'ready' | 'error';
type MutationStage = 'request' | 'approve' | 'execute';
type PendingOwner = MutationStage | 'status';
type MutationFailureKind = 'cancelled' | 'definite' | 'ambiguous';

const CORRECTION_EXECUTION_CONFIRM_TEXT = '권한 보정 실행';

interface UnknownOutcome {
  stage: MutationStage;
  correctionId: number | null;
}

interface AxiosLikeMutationError {
  response?: {
    status?: number;
    data?: {
      message?: unknown;
    };
  };
}

interface CorrectionDraft {
  targetSubscriptionId: string;
  targetBillingCycle: SubscriptionBillingCycle;
  targetStatus: UserSubscriptionStatus;
  targetExpiresAt: string;
  clearPendingChange: boolean;
  cancelBillingAgreement: boolean;
  reasonNote: string;
}

interface UserSubscriptionCorrectionModalProps {
  target: MySubscription | null;
  plans: SubscriptionPlan[];
  planError: string | null;
  onClose: () => void;
  onSucceeded: (correction: AdminSubscriptionCorrection) => Promise<void>;
  onMutationOwnershipChange: (owned: boolean) => void;
}

function toDateInput(value: string): string {
  return value.slice(0, 10);
}

function isValidDateInput(value: string): boolean {
  const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(value);
  if (!match) return false;
  const year = Number(match[1]);
  const month = Number(match[2]);
  const day = Number(match[3]);
  const date = new Date(Date.UTC(year, month - 1, day));
  return (
    date.getUTCFullYear() === year && date.getUTCMonth() === month - 1 && date.getUTCDate() === day
  );
}

function initialDraft(target: MySubscription): CorrectionDraft {
  return {
    targetSubscriptionId: target.subscription.isActive ? String(target.subscription.id) : '',
    targetBillingCycle: target.billingCycle,
    targetStatus: target.status,
    targetExpiresAt: toDateInput(target.expiresAt),
    clearPendingChange: target.pendingSubscriptionId !== null,
    cancelBillingAgreement: false,
    reasonNote: '',
  };
}

function correctionDraft(correction: AdminSubscriptionCorrection): CorrectionDraft {
  return {
    targetSubscriptionId: String(correction.targetSubscriptionId),
    targetBillingCycle: correction.targetBillingCycle,
    targetStatus: correction.targetStatus,
    targetExpiresAt: toDateInput(correction.targetExpiresAt),
    clearPendingChange: correction.clearPendingChange,
    cancelBillingAgreement: correction.cancelBillingAgreement,
    reasonNote: correction.reasonNote,
  };
}

function correctionTargetAgreementStatus(
  correction: AdminSubscriptionCorrection,
): BillingAgreementStatus | null {
  const current = correction.beforeBillingAgreementStatus;
  if (
    correction.cancelBillingAgreement &&
    current !== null &&
    current !== 'CANCELLED' &&
    current !== 'EXPIRED'
  ) {
    return 'CANCELLED';
  }
  return correction.afterBillingAgreementStatus ?? current;
}

function correctionAsPreview(
  correction: AdminSubscriptionCorrection,
): AdminSubscriptionCorrectionPreview {
  return {
    userSubscriptionId: correction.userSubscriptionId,
    userId: correction.userId,
    userNickname: correction.userNickname,
    currentSubscriptionId: correction.beforeSubscriptionId,
    currentPlanName: correction.beforePlanName,
    currentBillingCycle: correction.beforeBillingCycle,
    currentStatus: correction.beforeStatus,
    currentExpiresAt: correction.beforeExpiresAt,
    currentPendingSubscriptionId: correction.beforePendingSubscriptionId,
    currentPendingPlanName: correction.beforePendingPlanName,
    currentPendingBillingCycle: correction.beforePendingBillingCycle,
    targetSubscriptionId: correction.targetSubscriptionId,
    targetPlanName: correction.targetPlanName,
    targetBillingCycle: correction.targetBillingCycle,
    targetStatus: correction.targetStatus,
    targetExpiresAt: correction.targetExpiresAt,
    clearPendingChange: correction.clearPendingChange,
    cancelBillingAgreement: correction.cancelBillingAgreement,
    currentBillingAgreementStatus: correction.beforeBillingAgreementStatus,
    targetBillingAgreementStatus: correctionTargetAgreementStatus(correction),
    externalPaymentExecuted: false,
    executable: true,
    reason: null,
  };
}

function isOpenCorrection(correction: AdminSubscriptionCorrection): boolean {
  return (
    correction.status === 'REQUESTED' ||
    correction.status === 'APPROVED' ||
    correction.status === 'PROCESSING'
  );
}

function classifyMutationFailure(error: unknown): MutationFailureKind {
  if (classifyLoadError(error) === 'cancelled') return 'cancelled';
  const status = (error as AxiosLikeMutationError)?.response?.status;
  if (typeof status === 'number' && status >= 400 && status < 500) return 'definite';
  return 'ambiguous';
}

function definiteMutationErrorMessage(error: unknown, stage: MutationStage): string {
  const message = (error as AxiosLikeMutationError)?.response?.data?.message;
  if (typeof message === 'string' && message.trim()) return message.trim();
  if (stage === 'request') return '권한 보정 요청을 생성하지 못했습니다.';
  if (stage === 'approve') return '권한 보정 요청을 승인하지 못했습니다.';
  return '권한 보정을 실행하지 못했습니다.';
}

function agreementLabel(status: BillingAgreementStatus | null): string {
  return status ? AGREEMENT_LABELS[status] : '로컬 약정 없음';
}

function previewReason(reason: string | null): string {
  if (!reason) return '서버 검증에서 실행할 수 없는 보정으로 판단했습니다.';
  return PREVIEW_REASON_LABELS[reason] ?? '서버 검증에서 실행할 수 없는 보정으로 판단했습니다.';
}

function pendingLabel(preview: AdminSubscriptionCorrectionPreview): string {
  if (!preview.currentPendingSubscriptionId) return '대기 변경 없음';
  const planName =
    preview.currentPendingPlanName ?? `플랜 #${preview.currentPendingSubscriptionId}`;
  const cycle = preview.currentPendingBillingCycle
    ? ` · ${BILLING_LABELS[preview.currentPendingBillingCycle]}`
    : '';
  return `${planName}${cycle}`;
}

function targetPendingLabel(preview: AdminSubscriptionCorrectionPreview): string {
  if (!preview.currentPendingSubscriptionId) return '대기 변경 없음';
  return preview.clearPendingChange ? '대기 변경 제거' : '기존 대기 변경 유지';
}

function workflowStage(
  correction: AdminSubscriptionCorrection | null,
  hasPreview: boolean,
): number {
  if (!correction) return hasPreview ? 1 : 0;
  if (correction.status === 'REQUESTED') return 2;
  if (correction.status === 'APPROVED' || correction.status === 'PROCESSING') return 3;
  if (correction.status === 'SUCCEEDED') return 4;
  return 3;
}

export default function UserSubscriptionCorrectionModal({
  target,
  plans,
  planError,
  onClose,
  onSucceeded,
  onMutationOwnershipChange,
}: UserSubscriptionCorrectionModalProps) {
  const [draft, setDraft] = useState<CorrectionDraft | null>(null);
  const [preview, setPreview] = useState<AdminSubscriptionCorrectionPreview | null>(null);
  const [correction, setCorrection] = useState<AdminSubscriptionCorrection | null>(null);
  const [approvalNote, setApprovalNote] = useState('');
  const [executionNote, setExecutionNote] = useState('');
  const [busy, setBusy] = useState<WorkflowBusy>(null);
  const [error, setError] = useState<string | null>(null);
  const [statusMessage, setStatusMessage] = useState<string | null>(null);
  const [unknownOutcome, setUnknownOutcome] = useState<UnknownOutcome | null>(null);
  const [confirmation, setConfirmation] = useState<ConfirmationStage>(null);
  const [executionConfirmationText, setExecutionConfirmationText] = useState('');
  const [openLookupState, setOpenLookupState] = useState<OpenLookupState>('idle');
  const [openLookupAttempt, setOpenLookupAttempt] = useState(0);
  const requestGenerationRef = useRef(0);
  const requestControllerRef = useRef<AbortController | null>(null);
  const pendingMutationRef = useRef<PendingOwner | null>(null);
  const targetOwnershipRef = useRef(false);
  const unknownOutcomeRef = useRef<UnknownOutcome | null>(null);

  const eligiblePlans = useMemo(() => {
    if (!target) return [];
    const byId = new Map<number, SubscriptionPlan>();
    if (target.subscription.isActive) byId.set(target.subscription.id, target.subscription);
    for (const plan of plans) {
      if (plan.isActive && plan.userType === target.subscription.userType) byId.set(plan.id, plan);
    }
    return [...byId.values()];
  }, [plans, target]);

  useEffect(() => {
    requestControllerRef.current?.abort();
    const controller = new AbortController();
    const generation = ++requestGenerationRef.current;
    const isCurrent = () =>
      requestGenerationRef.current === generation && !controller.signal.aborted;
    requestControllerRef.current = controller;
    setDraft(target ? initialDraft(target) : null);
    setPreview(null);
    setCorrection(null);
    setApprovalNote('');
    setExecutionNote('');
    setBusy(null);
    setError(null);
    setStatusMessage(null);
    unknownOutcomeRef.current = null;
    setUnknownOutcome(null);
    setConfirmation(null);
    setExecutionConfirmationText('');
    setOpenLookupState(target ? 'loading' : 'idle');

    if (!target) {
      requestControllerRef.current = null;
      controller.abort();
      return;
    }

    setBusy('open');
    void fetchOpenAdminSubscriptionCorrection(target.id, controller.signal)
      .then((result) => {
        if (!isCurrent()) return;
        if (result && (result.userSubscriptionId !== target.id || !isOpenCorrection(result))) {
          setOpenLookupState('error');
          setError(
            '진행 중 요청 조회 결과가 올바르지 않아 새 보정을 차단했습니다. 다시 조회하세요.',
          );
          return;
        }
        if (result) {
          setDraft(correctionDraft(result));
          setPreview(correctionAsPreview(result));
          setCorrection(result);
          setApprovalNote(result.approvalNote ?? '');
          setExecutionNote(result.executionNote ?? '');
        }
        setOpenLookupState('ready');
      })
      .catch((lookupError: unknown) => {
        if (isCurrent() && classifyLoadError(lookupError) !== 'cancelled') {
          setOpenLookupState('error');
          setError('진행 중 권한 보정 요청을 확인하지 못했습니다. 새 요청은 차단되었습니다.');
        }
      })
      .finally(() => {
        if (isCurrent()) setBusy(null);
      });

    return () => controller.abort();
  }, [openLookupAttempt, target]);

  useEffect(
    () => () => {
      requestControllerRef.current?.abort();
      requestGenerationRef.current += 1;
    },
    [],
  );

  function beginRequest() {
    requestControllerRef.current?.abort();
    const controller = new AbortController();
    const generation = ++requestGenerationRef.current;
    requestControllerRef.current = controller;
    return { controller, generation };
  }

  function isCurrentRequest(generation: number, controller: AbortController): boolean {
    return requestGenerationRef.current === generation && !controller.signal.aborted;
  }

  function claimPendingOwner(owner: PendingOwner): boolean {
    if (pendingMutationRef.current) return false;
    pendingMutationRef.current = owner;
    if (!targetOwnershipRef.current) {
      targetOwnershipRef.current = true;
      onMutationOwnershipChange(true);
    }
    return true;
  }

  function releasePendingOwner(owner: PendingOwner) {
    if (pendingMutationRef.current !== owner) return;
    pendingMutationRef.current = null;
    if (unknownOutcomeRef.current) return;
    targetOwnershipRef.current = false;
    onMutationOwnershipChange(false);
  }

  function invalidatePreview(patch: Partial<CorrectionDraft>) {
    requestControllerRef.current?.abort();
    requestControllerRef.current = null;
    requestGenerationRef.current += 1;
    setBusy(null);
    setPreview(null);
    setError(null);
    setStatusMessage(null);
    setDraft((current) => (current ? { ...current, ...patch } : current));
  }

  function closeWorkflow() {
    if (
      pendingMutationRef.current ||
      unknownOutcomeRef.current ||
      (busy && busy !== 'open' && busy !== 'preview')
    ) {
      return;
    }
    requestControllerRef.current?.abort();
    requestControllerRef.current = null;
    requestGenerationRef.current += 1;
    setConfirmation(null);
    setExecutionConfirmationText('');
    onClose();
  }

  function retryOpenLookup() {
    requestControllerRef.current?.abort();
    requestGenerationRef.current += 1;
    setOpenLookupAttempt((current) => current + 1);
  }

  const validationError = (() => {
    if (!draft) return null;
    if (!draft.targetSubscriptionId)
      return '활성 상태이며 사용자 유형과 맞는 목표 플랜을 선택하세요.';
    if (!draft.targetExpiresAt) return '목표 만료일을 입력하세요.';
    if (!isValidDateInput(draft.targetExpiresAt)) return '목표 만료일 형식을 확인하세요.';
    if (!draft.reasonNote.trim()) return '운영 사유를 입력하세요.';
    if (draft.reasonNote.trim().length > 500) return '운영 사유는 500자 이하여야 합니다.';
    return null;
  })();

  function buildRequest(): AdminSubscriptionCorrectionRequest | null {
    if (!target || !draft || validationError) return null;
    return {
      userSubscriptionId: target.id,
      targetSubscriptionId: Number(draft.targetSubscriptionId),
      targetBillingCycle: draft.targetBillingCycle,
      targetStatus: draft.targetStatus,
      targetExpiresAt: draft.targetExpiresAt,
      clearPendingChange: draft.clearPendingChange,
      cancelBillingAgreement: draft.cancelBillingAgreement,
      reasonNote: draft.reasonNote.trim(),
    };
  }

  function restoreCorrectionSnapshot(result: AdminSubscriptionCorrection) {
    setDraft(correctionDraft(result));
    setPreview(correctionAsPreview(result));
    setCorrection(result);
    setApprovalNote((current) => result.approvalNote ?? current);
    setExecutionNote((current) => result.executionNote ?? current);
  }

  async function readMutationOutcome(
    outcome: UnknownOutcome,
    signal: AbortSignal,
  ): Promise<AdminSubscriptionCorrection | null> {
    if (!target) throw new Error('The correction target is no longer open.');
    if (outcome.stage === 'request') {
      const result = await fetchOpenAdminSubscriptionCorrection(target.id, signal);
      if (result && (result.userSubscriptionId !== target.id || !isOpenCorrection(result))) {
        throw new Error('The open correction response does not match the target.');
      }
      return result;
    }
    if (outcome.correctionId === null) {
      throw new Error('A correction ID is required to reconcile this mutation.');
    }
    const result = await fetchAdminSubscriptionCorrection(outcome.correctionId, signal);
    if (result.id !== outcome.correctionId || result.userSubscriptionId !== target.id) {
      throw new Error('The correction detail response does not match the target.');
    }
    return result;
  }

  function reconciledStatusMessage(
    outcome: UnknownOutcome,
    result: AdminSubscriptionCorrection,
  ): string {
    const statusLabel = CORRECTION_STATUS_LABELS[result.status];
    if (outcome.stage === 'approve' && result.status === 'REQUESTED') {
      return `서버 상태를 동기화했습니다. 요청 #${result.id}은 요청됨 상태이며 승인은 반영되지 않았습니다.`;
    }
    if (outcome.stage === 'execute' && result.status === 'APPROVED') {
      return `서버 상태를 동기화했습니다. 요청 #${result.id}은 승인됨 상태이며 실행은 반영되지 않았습니다.`;
    }
    return `서버 상태를 동기화했습니다. 요청 #${result.id}의 현재 단계는 ${statusLabel}입니다.`;
  }

  async function applyReconciledOutcome(
    outcome: UnknownOutcome,
    result: AdminSubscriptionCorrection,
  ) {
    unknownOutcomeRef.current = null;
    setUnknownOutcome(null);
    setError(null);
    setStatusMessage(reconciledStatusMessage(outcome, result));
    restoreCorrectionSnapshot(result);
    if (result.status === 'SUCCEEDED') {
      try {
        await onSucceeded(result);
      } catch {
        setError('보정 실행은 완료되었지만 구독 목록을 새로고침하지 못했습니다.');
      }
    } else if (result.status === 'FAILED') {
      setError('보정 실행에 실패했습니다. 실패 기록과 현재 구독 상태를 확인하세요.');
    }
  }

  function markOutcomeUnknown(outcome: UnknownOutcome) {
    const actionLabel =
      outcome.stage === 'request' ? '요청 생성' : outcome.stage === 'approve' ? '승인' : '실행';
    unknownOutcomeRef.current = outcome;
    setUnknownOutcome(outcome);
    setStatusMessage(null);
    setError(
      `${actionLabel} 응답과 서버 상태를 모두 확인하지 못해 결과를 알 수 없습니다. 중복 ${actionLabel}을 차단했습니다.`,
    );
  }

  async function reconcileMutation(
    outcome: UnknownOutcome,
    controller: AbortController,
    generation: number,
  ) {
    try {
      const result = await readMutationOutcome(outcome, controller.signal);
      if (!isCurrentRequest(generation, controller)) return;
      if (result === null) {
        markOutcomeUnknown(outcome);
        return;
      }
      await applyReconciledOutcome(outcome, result);
    } catch (reconciliationError: unknown) {
      if (
        isCurrentRequest(generation, controller) &&
        classifyLoadError(reconciliationError) !== 'cancelled'
      ) {
        markOutcomeUnknown(outcome);
      }
    }
  }

  async function handleMutationFailure(
    failure: unknown,
    outcome: UnknownOutcome,
    controller: AbortController,
    generation: number,
  ) {
    if (!isCurrentRequest(generation, controller)) return;
    const failureKind = classifyMutationFailure(failure);
    if (failureKind === 'cancelled') return;
    if (failureKind === 'definite') {
      setError(definiteMutationErrorMessage(failure, outcome.stage));
      return;
    }
    await reconcileMutation(outcome, controller, generation);
  }

  async function handleStatusRetry() {
    if (!unknownOutcome || !claimPendingOwner('status')) return;
    const outcome = unknownOutcome;
    const { controller, generation } = beginRequest();
    setBusy('status');
    try {
      await reconcileMutation(outcome, controller, generation);
    } finally {
      releasePendingOwner('status');
      if (isCurrentRequest(generation, controller)) setBusy(null);
    }
  }

  async function handlePreview() {
    if (openLookupState !== 'ready') {
      setError('진행 중 요청 조회를 완료한 뒤 미리보기를 실행하세요.');
      return;
    }
    const request = buildRequest();
    if (!request) {
      setError(validationError ?? '보정 입력값을 확인하세요.');
      return;
    }
    setDraft((current) => (current ? { ...current, reasonNote: request.reasonNote } : current));
    const { controller, generation } = beginRequest();
    setBusy('preview');
    setError(null);
    setStatusMessage(null);
    try {
      const result = await previewAdminSubscriptionCorrection(request, controller.signal);
      if (!isCurrentRequest(generation, controller)) return;
      setPreview(result);
    } catch (requestError: unknown) {
      if (
        isCurrentRequest(generation, controller) &&
        classifyLoadError(requestError) !== 'cancelled'
      ) {
        setError('보정 미리보기를 불러오지 못했습니다. 목록은 변경되지 않았습니다.');
      }
    } finally {
      if (isCurrentRequest(generation, controller)) setBusy(null);
    }
  }

  async function handleCreateRequest() {
    if (unknownOutcome || pendingMutationRef.current) return;
    if (openLookupState !== 'ready') {
      setError('진행 중 요청 조회를 완료한 뒤 새 요청을 생성하세요.');
      return;
    }
    const request = buildRequest();
    if (!request || !preview?.executable || preview.externalPaymentExecuted) {
      setError('외부 결제 실행이 없는 실행 가능한 미리보기를 먼저 확인하세요.');
      return;
    }
    if (!claimPendingOwner('request')) return;
    const { controller, generation } = beginRequest();
    setBusy('request');
    setError(null);
    setStatusMessage(null);
    try {
      const result = await createAdminSubscriptionCorrection(request, controller.signal);
      if (!isCurrentRequest(generation, controller)) return;
      restoreCorrectionSnapshot(result);
    } catch (requestError: unknown) {
      await handleMutationFailure(
        requestError,
        { stage: 'request', correctionId: null },
        controller,
        generation,
      );
    } finally {
      releasePendingOwner('request');
      if (isCurrentRequest(generation, controller)) setBusy(null);
    }
  }

  async function handleApprove() {
    if (
      !correction ||
      correction.status !== 'REQUESTED' ||
      unknownOutcome ||
      pendingMutationRef.current
    ) {
      return;
    }
    if (!claimPendingOwner('approve')) return;
    const correctionId = correction.id;
    const { controller, generation } = beginRequest();
    setBusy('approve');
    setError(null);
    setStatusMessage(null);
    try {
      const result = await approveAdminSubscriptionCorrection(
        correctionId,
        { note: approvalNote.trim() },
        controller.signal,
      );
      if (!isCurrentRequest(generation, controller)) return;
      restoreCorrectionSnapshot(result);
    } catch (requestError: unknown) {
      await handleMutationFailure(
        requestError,
        { stage: 'approve', correctionId },
        controller,
        generation,
      );
    } finally {
      releasePendingOwner('approve');
      if (isCurrentRequest(generation, controller)) {
        setBusy(null);
        setConfirmation(null);
        setExecutionConfirmationText('');
      }
    }
  }

  async function handleExecute() {
    if (
      !correction ||
      correction.status !== 'APPROVED' ||
      unknownOutcome ||
      pendingMutationRef.current ||
      executionConfirmationText.trim() !== CORRECTION_EXECUTION_CONFIRM_TEXT
    ) {
      return;
    }
    if (!claimPendingOwner('execute')) return;
    const correctionId = correction.id;
    const { controller, generation } = beginRequest();
    setBusy('execute');
    setError(null);
    setStatusMessage(null);
    try {
      const result = await executeAdminSubscriptionCorrection(
        correctionId,
        { note: executionNote.trim() },
        controller.signal,
      );
      if (!isCurrentRequest(generation, controller)) return;
      restoreCorrectionSnapshot(result);
      if (result.status === 'SUCCEEDED') {
        await onSucceeded(result);
      } else if (result.status === 'FAILED') {
        setError('보정 실행에 실패했습니다. 실패 기록과 현재 구독 상태를 확인하세요.');
      } else {
        setError(
          `보정 실행 결과가 ${CORRECTION_STATUS_LABELS[result.status]} 상태입니다. 감사 기록을 확인하세요.`,
        );
      }
    } catch (requestError: unknown) {
      await handleMutationFailure(
        requestError,
        { stage: 'execute', correctionId },
        controller,
        generation,
      );
    } finally {
      releasePendingOwner('execute');
      if (isCurrentRequest(generation, controller)) {
        setBusy(null);
        setConfirmation(null);
        setExecutionConfirmationText('');
      }
    }
  }

  function confirmStage() {
    if (confirmation === 'approve') void handleApprove();
    if (
      confirmation === 'execute' &&
      executionConfirmationText.trim() === CORRECTION_EXECUTION_CONFIRM_TEXT
    ) {
      void handleExecute();
    }
  }

  function openConfirmation(stage: Exclude<ConfirmationStage, null>) {
    if (unknownOutcome || busy) return;
    if (stage === 'approve') setApprovalNote((current) => current.trim());
    if (stage === 'execute') {
      setExecutionNote((current) => current.trim());
      setExecutionConfirmationText('');
    }
    setConfirmation(stage);
  }

  if (!target || !draft) return null;

  const stage = workflowStage(correction, preview !== null);
  const openLookupReady = openLookupState === 'ready';
  const locked = correction !== null || busy === 'request' || !openLookupReady || !!unknownOutcome;
  const hasPendingChange = correction
    ? correction.beforePendingSubscriptionId !== null
    : target.pendingSubscriptionId !== null;
  const restoredTargetPlanMissing = Boolean(
    correction && !eligiblePlans.some((plan) => plan.id === correction.targetSubscriptionId),
  );
  const dateHelp = '날짜와 상태의 업무 규칙은 서버 미리보기에서 확인합니다.';
  const canRequest = Boolean(
    openLookupReady && !unknownOutcome && preview?.executable && !preview.externalPaymentExecuted,
  );
  const workflowCloseBlocked =
    unknownOutcome !== null || (busy !== null && busy !== 'open' && busy !== 'preview');

  return (
    <>
      <Modal open onClose={closeWorkflow} title="사용자 구독 권한 보정" busy={workflowCloseBlocked}>
        <div className={styles.workflowBody}>
          <div className={styles.targetSummary}>
            <strong>{target.userNickname ?? `사용자 #${target.userId}`}</strong>
            <span>구독 #{target.id}</span>
          </div>

          <ol className={styles.stageList} aria-label="권한 보정 단계">
            {['미리보기', '요청 생성', '승인', '실행'].map((label, index) => (
              <li
                key={label}
                className={
                  index < stage ? styles.stageComplete : index === stage ? styles.stageActive : ''
                }
                aria-current={index === stage ? 'step' : undefined}
              >
                <span>{index + 1}</span>
                {label}
              </li>
            ))}
          </ol>

          <p className={styles.operatorNotice}>
            같은 관리자 계정이 요청 생성, 승인, 실행을 순서대로 진행하는 단일 운영자 절차입니다. 2인
            승인을 의미하지 않습니다.
          </p>

          <div className={styles.boundaryNotice} role="note">
            <strong>로컬 구독 권한만 변경합니다.</strong>
            <span>
              외부 Toss 결제·환불, 결제사업자 빌링키 삭제, 이메일 발송은 실행하지 않습니다.
            </span>
          </div>

          {openLookupState === 'loading' ? (
            <div className={styles.lookupGate} role="status">
              진행 중 권한 보정 요청을 확인하고 있습니다. 조회가 끝날 때까지 새 요청은 차단됩니다.
            </div>
          ) : null}

          {openLookupState === 'error' ? (
            <div className={styles.lookupGateError} role="alert">
              <span>{error}</span>
              <Button size="sm" variant="outline" onClick={retryOpenLookup}>
                진행 중 요청 다시 조회
              </Button>
            </div>
          ) : null}

          {statusMessage ? (
            <div className={styles.lookupGate} role="status">
              {statusMessage}
            </div>
          ) : null}

          {unknownOutcome ? (
            <div className={styles.lookupGateError} role="alert">
              <span>{error}</span>
              <Button
                size="sm"
                variant="outline"
                loading={busy === 'status'}
                disabled={busy !== null}
                onClick={() => void handleStatusRetry()}
              >
                상태 다시 확인
              </Button>
            </div>
          ) : null}

          {openLookupReady && correction && isOpenCorrection(correction) ? (
            <div className={styles.resumeNotice} role="status">
              <strong>진행 중 요청 #{correction.id}을 이어서 처리합니다.</strong>
              <span>현재 단계: {CORRECTION_STATUS_LABELS[correction.status]}</span>
            </div>
          ) : null}

          <div className={styles.formGrid}>
            <div className={styles.modalField}>
              <label className={styles.modalLabel} htmlFor="correction-plan">
                목표 활성 플랜
              </label>
              <select
                id="correction-plan"
                className={styles.modalSelect}
                value={draft.targetSubscriptionId}
                disabled={locked || busy !== null}
                onChange={(event) =>
                  invalidatePreview({ targetSubscriptionId: event.target.value })
                }
              >
                <option value="">선택하세요</option>
                {eligiblePlans.map((plan) => (
                  <option key={plan.id} value={plan.id}>
                    {plan.name}
                  </option>
                ))}
                {restoredTargetPlanMissing && correction ? (
                  <option value={correction.targetSubscriptionId}>
                    {correction.targetPlanName}
                  </option>
                ) : null}
              </select>
              {planError ? <span className={styles.fieldError}>{planError}</span> : null}
            </div>

            <div className={styles.modalField}>
              <label className={styles.modalLabel} htmlFor="correction-cycle">
                목표 결제 주기
              </label>
              <select
                id="correction-cycle"
                className={styles.modalSelect}
                value={draft.targetBillingCycle}
                disabled={locked || busy !== null}
                onChange={(event) =>
                  invalidatePreview({
                    targetBillingCycle: event.target.value as SubscriptionBillingCycle,
                  })
                }
              >
                <option value="MONTHLY">월간</option>
                <option value="YEARLY">연간</option>
              </select>
            </div>

            <div className={styles.modalField}>
              <label className={styles.modalLabel} htmlFor="correction-status">
                목표 상태
              </label>
              <select
                id="correction-status"
                className={styles.modalSelect}
                value={draft.targetStatus}
                disabled={locked || busy !== null}
                onChange={(event) =>
                  invalidatePreview({ targetStatus: event.target.value as UserSubscriptionStatus })
                }
              >
                <option value="ACTIVE">활성</option>
                <option value="CANCELLED">취소됨</option>
                <option value="EXPIRED">만료</option>
              </select>
            </div>

            <div className={styles.modalField}>
              <label className={styles.modalLabel} htmlFor="correction-expires">
                목표 만료일
              </label>
              <input
                id="correction-expires"
                type="date"
                className={styles.modalInput}
                value={draft.targetExpiresAt}
                required
                disabled={locked || busy !== null}
                onChange={(event) => invalidatePreview({ targetExpiresAt: event.target.value })}
              />
              <span className={styles.fieldHelp}>{dateHelp}</span>
            </div>
          </div>

          <div className={styles.checkFields}>
            <label className={styles.checkField}>
              <input
                type="checkbox"
                checked={draft.clearPendingChange}
                disabled={locked || busy !== null || !hasPendingChange}
                onChange={(event) =>
                  invalidatePreview({ clearPendingChange: event.target.checked })
                }
              />
              <span>
                <strong>대기 중인 플랜·주기 변경 제거</strong>
                <small>
                  {hasPendingChange
                    ? '선택하지 않으면 기존 예약 변경을 유지합니다.'
                    : '현재 대기 중인 변경이 없습니다.'}
                </small>
              </span>
            </label>
            <label className={styles.checkField}>
              <input
                type="checkbox"
                checked={draft.cancelBillingAgreement}
                disabled={locked || busy !== null}
                onChange={(event) =>
                  invalidatePreview({ cancelBillingAgreement: event.target.checked })
                }
              />
              <span>
                <strong>로컬 자동 갱신 약정 취소</strong>
                <small>외부 결제사업자의 빌링키는 삭제하지 않습니다.</small>
              </span>
            </label>
          </div>

          <div className={styles.modalField}>
            <label className={styles.modalLabel} htmlFor="correction-reason">
              운영 사유 (필수)
            </label>
            <textarea
              id="correction-reason"
              className={styles.modalTextarea}
              value={draft.reasonNote}
              maxLength={500}
              disabled={locked || busy !== null}
              placeholder="지원 티켓 또는 보정 근거를 입력하세요."
              onChange={(event) => invalidatePreview({ reasonNote: event.target.value })}
            />
            <span className={styles.characterCount}>{draft.reasonNote.length}/500</span>
          </div>

          {openLookupReady && !correction && validationError && !preview ? (
            <div className={styles.inlineHint}>{validationError}</div>
          ) : null}

          {preview ? (
            <section className={styles.previewSection} aria-label="권한 보정 미리보기">
              <h3>현재 상태 → 목표 상태</h3>
              <dl className={styles.comparisonList}>
                <div>
                  <dt>플랜</dt>
                  <dd>{preview.currentPlanName}</dd>
                  <dd>{preview.targetPlanName}</dd>
                </div>
                <div>
                  <dt>주기</dt>
                  <dd>{BILLING_LABELS[preview.currentBillingCycle]}</dd>
                  <dd>{BILLING_LABELS[preview.targetBillingCycle]}</dd>
                </div>
                <div>
                  <dt>상태</dt>
                  <dd>{STATUS_LABELS[preview.currentStatus]}</dd>
                  <dd>{STATUS_LABELS[preview.targetStatus]}</dd>
                </div>
                <div>
                  <dt>만료일</dt>
                  <dd>{formatDate(preview.currentExpiresAt)}</dd>
                  <dd>{formatDate(preview.targetExpiresAt)}</dd>
                </div>
                <div>
                  <dt>대기 변경</dt>
                  <dd>{pendingLabel(preview)}</dd>
                  <dd>{targetPendingLabel(preview)}</dd>
                </div>
                <div>
                  <dt>로컬 자동 갱신 약정</dt>
                  <dd>{agreementLabel(preview.currentBillingAgreementStatus)}</dd>
                  <dd>{agreementLabel(preview.targetBillingAgreementStatus)}</dd>
                </div>
              </dl>
              <div className={styles.persistedText}>
                <strong>저장할 운영 사유</strong>
                <code>{JSON.stringify(draft.reasonNote)}</code>
              </div>
              <div
                className={
                  preview.externalPaymentExecuted
                    ? styles.externalExecutionWarning
                    : styles.externalExecutionSafe
                }
                role="status"
              >
                {preview.externalPaymentExecuted
                  ? '외부 결제가 실행된 것으로 보고되어 요청 생성을 차단했습니다.'
                  : '외부 결제 실행 없음: Toss 결제·환불·빌링키 삭제·이메일 발송을 수행하지 않습니다.'}
              </div>
              {!preview.executable ? (
                <div className={styles.previewRejected} role="alert">
                  <strong>요청할 수 없습니다.</strong>
                  <span>{previewReason(preview.reason)}</span>
                </div>
              ) : null}
            </section>
          ) : null}

          {correction?.status === 'REQUESTED' ? (
            <div className={styles.stageAction}>
              <p>
                요청 #{correction.id}이 생성되었습니다. 승인 전에는 구독 상태가 바뀌지 않습니다.
              </p>
              <label className={styles.modalLabel} htmlFor="approval-note">
                승인 메모 (선택)
              </label>
              <textarea
                id="approval-note"
                className={styles.modalTextarea}
                value={approvalNote}
                maxLength={500}
                disabled={busy !== null || unknownOutcome !== null}
                onChange={(event) => setApprovalNote(event.target.value)}
              />
              <Button
                size="sm"
                disabled={busy !== null || unknownOutcome !== null}
                onClick={() => openConfirmation('approve')}
              >
                승인 단계로 이동
              </Button>
            </div>
          ) : null}

          {correction?.status === 'APPROVED' ? (
            <div className={styles.stageAction}>
              <p>
                요청 #{correction.id}이 승인되었습니다.
                {correction.approvalNote ? ` 승인 메모: ${correction.approvalNote}` : ''}
              </p>
              <label className={styles.modalLabel} htmlFor="execution-note">
                실행 메모 (선택)
              </label>
              <textarea
                id="execution-note"
                className={styles.modalTextarea}
                value={executionNote}
                maxLength={500}
                disabled={busy !== null || unknownOutcome !== null}
                onChange={(event) => setExecutionNote(event.target.value)}
              />
              <Button
                variant="danger"
                size="sm"
                disabled={busy !== null || unknownOutcome !== null}
                onClick={() => openConfirmation('execute')}
              >
                실행 확인
              </Button>
            </div>
          ) : null}

          {correction?.status === 'PROCESSING' ? (
            <div className={styles.stageAction} role="status">
              요청 #{correction.id}의 로컬 보정을 처리하고 있습니다. 중복 실행하지 마세요.
            </div>
          ) : null}

          {correction?.status === 'SUCCEEDED' ? (
            <div className={styles.workflowSuccess} role="status">
              <strong>권한 보정 실행 완료</strong>
              <span>
                요청 #{correction.id}의 로컬 구독 상태가 반영되었습니다. 외부 결제 작업은 실행되지
                않았습니다.
              </span>
            </div>
          ) : null}

          {correction && (correction.status === 'FAILED' || correction.status === 'CANCELLED') ? (
            <div className={styles.previewRejected} role="alert">
              <strong>{correction.status === 'FAILED' ? '실행 실패' : '요청 취소됨'}</strong>
              <span>
                {correction.failureMessage
                  ? '감사 기록의 실패 상세를 확인하세요.'
                  : '현재 구독 상태와 감사 기록을 확인하세요.'}
              </span>
            </div>
          ) : null}

          {error && openLookupState !== 'error' && !unknownOutcome ? (
            <div className={styles.workflowError} role="alert">
              {error}
            </div>
          ) : null}
        </div>

        <div className={styles.modalActions}>
          <Button variant="ghost" size="sm" disabled={workflowCloseBlocked} onClick={closeWorkflow}>
            닫기
          </Button>
          {!correction ? (
            <>
              <Button
                variant="outline"
                size="sm"
                loading={busy === 'preview'}
                disabled={
                  !openLookupReady ||
                  busy !== null ||
                  unknownOutcome !== null ||
                  Boolean(validationError)
                }
                onClick={() => void handlePreview()}
              >
                미리보기
              </Button>
              <Button
                size="sm"
                loading={busy === 'request'}
                disabled={busy !== null || !canRequest}
                onClick={() => void handleCreateRequest()}
              >
                요청 생성
              </Button>
            </>
          ) : null}
        </div>
      </Modal>

      <ConfirmDialog
        open={confirmation !== null}
        title={confirmation === 'execute' ? '권한 보정 실행 확인' : '권한 보정 승인 확인'}
        message={
          confirmation === 'execute'
            ? `실행하면 로컬 구독 권한과 선택한 대기 변경·자동 갱신 약정 상태가 즉시 바뀝니다. Toss 결제·환불·빌링키 삭제·이메일 발송은 실행되지 않으며, 되돌리려면 새 보정 요청이 필요합니다. 저장할 실행 메모: ${JSON.stringify(executionNote)}`
            : `요청을 승인 상태로 전환합니다. 아직 구독 데이터는 변경되지 않으며, 같은 운영자가 다음 실행 단계를 계속합니다. 저장할 승인 메모: ${JSON.stringify(approvalNote)}`
        }
        confirmLabel={confirmation === 'execute' ? '권한 보정 실행' : '승인 확정'}
        confirmVariant={confirmation === 'execute' ? 'danger' : 'primary'}
        busy={busy === 'approve' || busy === 'execute'}
        typedConfirmation={
          confirmation === 'execute'
            ? {
                label: '실행 확인 문구',
                requiredText: CORRECTION_EXECUTION_CONFIRM_TEXT,
                value: executionConfirmationText,
                hint: `"${CORRECTION_EXECUTION_CONFIRM_TEXT}"을 정확히 입력하세요.`,
                onChange: setExecutionConfirmationText,
              }
            : undefined
        }
        onConfirm={confirmStage}
        onCancel={() => {
          if (!busy) {
            setConfirmation(null);
            setExecutionConfirmationText('');
          }
        }}
      />
    </>
  );
}
