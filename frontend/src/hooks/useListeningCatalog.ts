import { useCallback, useEffect, useRef, useState } from 'react';
import { isListeningMode } from '@/config/appMode';
import {
  changeListeningCatalog,
  reconcileListeningCatalog,
  fetchListeningCatalog,
  type ListeningCatalog,
  type ListeningCatalogChange,
} from '@/api/tracks';
import { getAuthSessionGeneration, useAuthStore } from '@/store/authStore';
import { usePlayerStore } from '@/store/playerStore';

interface Owner {
  actor: number | null;
  token: string | null;
  generation: number;
}
interface Attempt {
  version: 1 | 2;
  attemptId: string;
  actor: number;
  before: ListeningCatalog;
  proposal: ListeningCatalogChange;
  startedAt: string;
}

// Only these deliberately safe backend messages may be shown; never echo arbitrary response text.
const PUBLICATION_REJECTIONS = new Set([
  '음원을 찾을 수 없습니다.',
  '음원 변환이 완료되지 않았습니다. 변환 상태를 확인해 주세요.',
  '음원 변경 처리가 진행 중입니다. 완료 후 다시 시도해 주세요.',
  '비활성 음원입니다. 목록의 공개 버튼으로 다시 시도해 주세요.',
  '선택한 한 곡의 공개 상태만 변경할 수 있습니다.',
]);
function rejectionMessage(error: unknown, status: number): string {
  if (status === 401) return '로그인이 만료되었습니다. 다시 로그인한 뒤 공개 상태를 확인해 주세요.';
  if (status === 403)
    return '공개 상태를 변경할 관리자 권한이 없습니다. 로그인 계정을 확인해 주세요.';
  const message = (error as { response?: { data?: { message?: unknown } } })?.response?.data
    ?.message;
  if (typeof message === 'string' && PUBLICATION_REJECTIONS.has(message)) return message;
  if (status === 409)
    return '다른 변경과 겹쳐 적용하지 않았습니다. 최신 목록을 확인한 뒤 다시 선택해 주세요.';
  return '공개 요청을 적용하지 못했습니다. 목록을 새로고침한 뒤 음원 상태를 확인해 주세요.';
}

function validAttemptAction(attempt: Attempt): boolean {
  const target = attempt.proposal.targetChange;
  if (attempt.version === 1) return attempt.proposal.creditsConfirmed === true && target == null;
  if (
    attempt.version !== 2 ||
    attempt.proposal.creditsConfirmed !== false ||
    !target ||
    !Number.isSafeInteger(target.trackId) ||
    target.trackId <= 0 ||
    typeof target.publish !== 'boolean'
  )
    return false;
  const expectedIDs = target.publish
    ? [...new Set([...attempt.before.trackIds, target.trackId])].sort((a, b) => a - b)
    : attempt.before.trackIds.filter((id) => id !== target.trackId).sort((a, b) => a - b);
  return (
    JSON.stringify(expectedIDs) ===
    JSON.stringify([...attempt.proposal.trackIds].sort((a, b) => a - b))
  );
}
function owner(): Owner {
  const auth = useAuthStore.getState();
  return {
    actor: auth.user?.id ?? null,
    token: auth.accessToken,
    generation: getAuthSessionGeneration(),
  };
}
function owns(expected: Owner): boolean {
  const current = owner();
  return (
    current.actor === expected.actor &&
    current.token === expected.token &&
    current.generation === expected.generation
  );
}
function readActive(key: string): { raw: string | null; blocked: boolean } {
  try {
    const raw = sessionStorage.getItem(key);
    return { raw, blocked: raw !== null };
  } catch {
    return { raw: null, blocked: true };
  }
}
function validView(value: ListeningCatalog): boolean {
  return (
    value !== null &&
    typeof value === 'object' &&
    ['DATABASE', 'STATIC'].includes(value.source) &&
    Number.isSafeInteger(value.revision) &&
    value.revision >= 0 &&
    Array.isArray(value.trackIds) &&
    value.trackIds.length <= 1000 &&
    value.trackIds.every((id) => Number.isSafeInteger(id) && id > 0) &&
    new Set(value.trackIds).size === value.trackIds.length &&
    typeof value.digest === 'string' &&
    value.digest.length > 0
  );
}

function applyCatalogToPlayer(catalog: ListeningCatalog) {
  const player = usePlayerStore.getState();
  const allowed = new Set(catalog.trackIds);
  if (player.currentTrack && !allowed.has(player.currentTrack.id)) player.pause();
  usePlayerStore.setState({
    queue: player.queue.filter((track) => allowed.has(track.id)),
    trackListContext: player.trackListContext.filter((track) => allowed.has(track.id)),
    ...(player.currentTrack && !allowed.has(player.currentTrack.id)
      ? { currentTrack: null, currentTime: 0, duration: 0 }
      : {}),
  });
}

export function useListeningCatalog() {
  const operatorID = useAuthStore((s) => s.user?.id ?? null);
  const token = useAuthStore((s) => s.accessToken);
  const authGeneration = getAuthSessionGeneration();
  const holdKey = `listening-catalog-hold:operator:${operatorID}`;
  const [loaded, setLoaded] = useState<{ value: ListeningCatalog; owner: Owner } | null>(null);
  const [loading, setLoading] = useState(isListeningMode);
  const [pending, setPending] = useState(false);
  const [message, setMessage] = useState('');
  const [, rerender] = useState(0);
  const busy = useRef<string | null>(null);
  const sequence = useRef(0);
  const mounted = useRef(false);
  const active = readActive(holdKey);
  const hold = active.blocked;
  const snapshot = loaded && owns(loaded.owner) ? loaded.value : null;

  const reload = useCallback(
    async (preserveMessage = false) => {
      if (!isListeningMode) return;
      const initiatingOwner = owner();
      const activeAtStart = readActive(holdKey).raw;
      const request = ++sequence.current;
      const current = () =>
        mounted.current &&
        request === sequence.current &&
        owns(initiatingOwner) &&
        readActive(holdKey).raw === activeAtStart;
      setLoading(true);
      setLoaded(null);
      try {
        const result = await fetchListeningCatalog();
        if (!validView(result)) throw new Error('Invalid catalog response');
        if (current()) setLoaded({ value: result, owner: initiatingOwner });
      } catch {
        if (current() && !preserveMessage)
          setMessage(
            '공개 목록을 확인할 수 없습니다. 변경은 중지됩니다. 운영자에게 초기 설정·상태 확인을 요청하세요.',
          );
      } finally {
        if (current()) setLoading(false);
      }
    },
    [holdKey],
  );

  useEffect(() => {
    mounted.current = true;
    setMessage('');
    setPending(false);
    void reload();
    return () => {
      mounted.current = false;
      sequence.current += 1;
    };
  }, [reload, operatorID, token, authGeneration]);

  async function change(trackID: number, publish: boolean): Promise<boolean> {
    const initiatingOwner = owner();
    if (
      !isListeningMode ||
      busy.current ||
      loading ||
      readActive(holdKey).blocked ||
      !snapshot ||
      !loaded ||
      !owns(loaded.owner) ||
      snapshot.source !== 'DATABASE' ||
      initiatingOwner.actor === null ||
      !initiatingOwner.token ||
      initiatingOwner.actor !== operatorID ||
      !Number.isSafeInteger(trackID) ||
      trackID <= 0 ||
      typeof publish !== 'boolean' ||
      snapshot.trackIds.includes(trackID) === publish
    )
      return false;
    const ids = publish
      ? [...new Set([...snapshot.trackIds, trackID])].sort((a, b) => a - b)
      : snapshot.trackIds.filter((id) => id !== trackID);
    const attemptId = crypto.randomUUID();
    const attempt: Attempt = {
      version: 2,
      attemptId,
      actor: initiatingOwner.actor,
      before: { ...snapshot, trackIds: [...snapshot.trackIds] },
      proposal: {
        attemptId,
        expectedRevision: snapshot.revision,
        trackIds: ids,
        reason: `관리자 목록에서 단일 곡 ${publish ? '공개' : '공개해제'}: ID ${trackID}`,
        creditsConfirmed: false,
        targetChange: { trackId: trackID, publish },
      },
      startedAt: new Date().toISOString(),
    };
    const raw = JSON.stringify(attempt);
    const archiveKey = `listening-catalog-attempt:${attempt.attemptId}`;
    // No await between the active-record check and journal writes. Archives are never deleted or overwritten.
    try {
      if (readActive(holdKey).blocked || sessionStorage.getItem(archiveKey) !== null) return false;
      sessionStorage.setItem(archiveKey, raw);
      sessionStorage.setItem(holdKey, raw);
    } catch {
      setMessage('변경 기록을 보존할 수 없어 요청하지 않았습니다. 기록을 확인하세요.');
      rerender((v) => v + 1);
      return false;
    }
    busy.current = attempt.attemptId;
    setPending(true);
    setMessage('');
    rerender((v) => v + 1);
    const current = () =>
      mounted.current &&
      owns(initiatingOwner) &&
      busy.current === attempt.attemptId &&
      readActive(holdKey).raw === raw;
    const settle = (
      disposition: 'CONFIRMED_RESPONSE' | 'REJECTED_RESPONSE' | 'UNKNOWN',
      details: unknown,
    ): boolean => {
      if (!current()) return false;
      try {
        const outcomeKey = `${archiveKey}:outcome`;
        if (sessionStorage.getItem(outcomeKey) !== null) return false;
        sessionStorage.setItem(
          outcomeKey,
          JSON.stringify({
            attemptId: attempt.attemptId,
            actor: attempt.actor,
            disposition,
            observedAt: new Date().toISOString(),
            details,
          }),
        );
        // The exact active record and in-memory auth session are checked again before clearing only our pointer.
        if (disposition !== 'UNKNOWN' && current()) sessionStorage.removeItem(holdKey);
        rerender((v) => v + 1);
        return true;
      } catch {
        setMessage('응답 기록을 보존할 수 없어 HOLD를 유지합니다.');
        return false;
      }
    };
    try {
      const result = await changeListeningCatalog(attempt.proposal);
      if (
        !validView(result) ||
        result.source !== 'DATABASE' ||
        result.revision !== attempt.before.revision + 1 ||
        JSON.stringify([...result.trackIds].sort((a, b) => a - b)) !== JSON.stringify(ids)
      )
        throw new Error('Unattributable response');
      if (!settle('CONFIRMED_RESPONSE', result)) return false;
      setLoaded({ value: result, owner: initiatingOwner });
      setMessage(
        publish
          ? '공개했습니다. 이제 로그인 없이 들을 수 있습니다.'
          : '공개를 해제했습니다. 이제 음원 정보를 수정할 수 있습니다.',
      );
      applyCatalogToPlayer(result);
      return true;
    } catch (error) {
      if (!current()) return false;
      const status = (error as { response?: { status?: number } })?.response?.status;
      if ([400, 401, 403, 409].includes(status ?? 0)) {
        if (!settle('REJECTED_RESPONSE', { status })) return false;
        setMessage(rejectionMessage(error, status!));
      } else {
        settle('UNKNOWN', { status: status ?? null });
        setMessage(
          '변경 결과가 불명확합니다(HOLD). 현재 목록 조회는 이 요청의 성공 증거가 아닙니다. 요청을 반복하지 마세요.',
        );
      }
      await reload(true);
      return false;
    } finally {
      if (busy.current === attempt.attemptId) busy.current = null;
      if (mounted.current && owns(initiatingOwner)) {
        setPending(false);
        rerender((v) => v + 1);
      }
    }
  }

  async function reconcile() {
    const initiatingOwner = owner();
    const raw = readActive(holdKey).raw;
    if (busy.current || !raw || initiatingOwner.actor !== operatorID || !initiatingOwner.token)
      return;
    let attempt: Attempt;
    try {
      attempt = JSON.parse(raw) as Attempt;
      if (
        ![1, 2].includes(attempt.version) ||
        attempt.actor !== operatorID ||
        !/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/.test(attempt.attemptId) ||
        !validView(attempt.before) ||
        attempt.before.source !== 'DATABASE' ||
        attempt.proposal.attemptId !== attempt.attemptId ||
        attempt.proposal.expectedRevision !== attempt.before.revision ||
        !Array.isArray(attempt.proposal.trackIds) ||
        attempt.proposal.trackIds.length > 1000 ||
        !attempt.proposal.trackIds.every((id) => Number.isSafeInteger(id) && id > 0) ||
        new Set(attempt.proposal.trackIds).size !== attempt.proposal.trackIds.length ||
        typeof attempt.proposal.reason !== 'string' ||
        !attempt.proposal.reason.trim() ||
        attempt.proposal.reason.length > 500 ||
        !validAttemptAction(attempt) ||
        sessionStorage.getItem(`listening-catalog-attempt:${attempt.attemptId}`) !== raw
      )
        throw new Error('Invalid attempt');
    } catch {
      setMessage(
        '시도 기록이 없거나 일치하지 않아 HOLD를 유지합니다. 별도 승인된 운영 복구가 필요합니다.',
      );
      return;
    }
    busy.current = attempt.attemptId;
    setPending(true);
    const current = () =>
      mounted.current &&
      owns(initiatingOwner) &&
      busy.current === attempt.attemptId &&
      readActive(holdKey).raw === raw;
    const sameIDs = (a: number[], b: number[]) =>
      JSON.stringify([...a].sort((x, y) => x - y)) === JSON.stringify([...b].sort((x, y) => x - y));
    const sameView = (a: ListeningCatalog, b: ListeningCatalog) =>
      validView(a) &&
      validView(b) &&
      a.source === b.source &&
      a.revision === b.revision &&
      a.digest === b.digest &&
      sameIDs(a.trackIds, b.trackIds);
    try {
      const response = await reconcileListeningCatalog({
        attemptId: attempt.attemptId,
        expectedRevision: attempt.before.revision,
        beforeTrackIds: attempt.before.trackIds,
        beforeDigest: attempt.before.digest,
        trackIds: attempt.proposal.trackIds,
        reason: attempt.proposal.reason,
        ...(attempt.version === 2 ? { targetChange: attempt.proposal.targetChange } : {}),
      });
      if (!current()) return;
      const receipt = response?.receipt;
      if (
        response?.status !== 'COMMITTED' ||
        !receipt ||
        !Number.isSafeInteger(receipt.auditId) ||
        receipt.auditId <= 0 ||
        receipt.actorId !== attempt.actor ||
        receipt.attemptId !== attempt.attemptId ||
        receipt.reason !== attempt.proposal.reason ||
        (attempt.version === 2
          ? receipt.targetChange?.trackId !== attempt.proposal.targetChange?.trackId ||
            receipt.targetChange?.publish !== attempt.proposal.targetChange?.publish
          : receipt.targetChange != null) ||
        !sameView(receipt.before, attempt.before) ||
        !validView(receipt.after) ||
        receipt.after.source !== 'DATABASE' ||
        receipt.after.revision !== attempt.before.revision + 1 ||
        !sameIDs(receipt.after.trackIds, attempt.proposal.trackIds) ||
        typeof receipt.recordedAt !== 'string' ||
        !Number.isFinite(Date.parse(receipt.recordedAt))
      ) {
        setMessage(
          '일치하는 커밋 감사 증거를 확인하지 못했습니다. HOLD를 유지하며 요청을 반복하지 않습니다.',
        );
        return;
      }
      const receiptKey = `listening-catalog-attempt:${attempt.attemptId}:reconciliation:${crypto.randomUUID()}`;
      if (sessionStorage.getItem(receiptKey) !== null)
        throw new Error('Receipt identity collision');
      sessionStorage.setItem(
        receiptKey,
        JSON.stringify({
          attemptId: attempt.attemptId,
          actor: attempt.actor,
          receipt,
          observedAt: new Date().toISOString(),
        }),
      );
      const fresh = await fetchListeningCatalog();
      if (!current()) return;
      if (
        !validView(fresh) ||
        fresh.source !== 'DATABASE' ||
        fresh.revision < receipt.after.revision ||
        (fresh.revision === receipt.after.revision && !sameView(fresh, receipt.after))
      ) {
        setMessage(
          '현재 목록의 revision 또는 digest가 감사와 모순되거나 확인할 수 없어 HOLD를 유지합니다. 별도 운영 조정이 필요합니다.',
        );
        return;
      }
      // This clears only the matching active pointer; immutable attempt, unknown outcome and receipt remain.
      sessionStorage.removeItem(holdKey);
      setLoaded({ value: fresh, owner: initiatingOwner });
      applyCatalogToPlayer(fresh);
      setLoading(false);
      setMessage(
        '서버 감사에서 이 시도의 과거 커밋을 확인했습니다. 현재 목록은 후속 변경을 포함할 수 있습니다. 기록은 보존되며 이전 요청을 재전송하거나 되돌리지 않습니다. 최신 목록에서 새 변경을 검토하세요.',
      );
      rerender((v) => v + 1);
    } catch {
      if (current())
        setMessage('서버 감사 대조를 완료하지 못해 HOLD를 유지합니다. 요청을 반복하지 마세요.');
    } finally {
      if (busy.current === attempt.attemptId) busy.current = null;
      if (mounted.current && owns(initiatingOwner)) {
        setPending(false);
        rerender((v) => v + 1);
      }
    }
  }

  function exportJournal() {
    const capturedOwner = owner();
    const captured = readActive(holdKey).raw;
    if (capturedOwner.actor !== operatorID || !captured || !owns(capturedOwner)) return;
    const url = URL.createObjectURL(new Blob([captured], { type: 'application/json' }));
    const link = document.createElement('a');
    link.href = url;
    link.download = 'listening-catalog-attempt.json';
    link.click();
    setTimeout(() => URL.revokeObjectURL(url), 0);
  }
  return {
    snapshot,
    loading,
    pending,
    hold,
    message,
    reload,
    change,
    exportJournal,
    reconcile,
    journalText: active.raw,
    ownerIdentity: `${operatorID}:${authGeneration}`,
  };
}
export type ListeningCatalogState = ReturnType<typeof useListeningCatalog>;
