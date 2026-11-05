import { useCallback, useEffect, useRef, useState } from 'react';
import { isListeningMode } from '@/config/appMode';
import {
  changeListeningCatalog,
  reconcileListeningCatalog,
  fetchListeningCatalog,
  type ListeningCatalog,
} from '@/api/tracks';
import { getAuthSessionGeneration, useAuthStore } from '@/store/authStore';
import { usePlayerStore } from '@/store/playerStore';

interface Owner {
  actor: number | null;
  token: string | null;
  generation: number;
}
interface Attempt {
  version: 1;
  attemptId: string;
  actor: number;
  before: ListeningCatalog;
  proposal: {
    attemptId: string;
    expectedRevision: number;
    trackIds: number[];
    reason: string;
    creditsConfirmed: true;
  };
  startedAt: string;
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

  const reload = useCallback(async () => {
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
      if (current())
        setMessage(
          '공개 목록을 확인할 수 없습니다. 변경은 중지됩니다. 운영자에게 초기 설정·상태 확인을 요청하세요.',
        );
    } finally {
      if (current()) setLoading(false);
    }
  }, [holdKey]);

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

  async function change(
    trackID: number,
    publish: boolean,
    reason: string,
    creditsConfirmed: boolean,
  ): Promise<boolean> {
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
      !reason.trim() ||
      reason.trim().length > 500 ||
      !creditsConfirmed
    )
      return false;
    const ids = publish
      ? [...new Set([...snapshot.trackIds, trackID])].sort((a, b) => a - b)
      : snapshot.trackIds.filter((id) => id !== trackID);
    const attemptId = crypto.randomUUID();
    const attempt: Attempt = {
      version: 1,
      attemptId,
      actor: initiatingOwner.actor,
      before: { ...snapshot, trackIds: [...snapshot.trackIds] },
      proposal: {
        attemptId,
        expectedRevision: snapshot.revision,
        trackIds: ids,
        reason: reason.trim(),
        creditsConfirmed: true,
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
      setMessage('이 시도의 공개 목록 변경 응답을 확인했습니다.');
      applyCatalogToPlayer(result);
      return true;
    } catch (error) {
      if (!current()) return false;
      const status = (error as { response?: { status?: number } })?.response?.status;
      if ([400, 401, 403, 409].includes(status ?? 0)) {
        if (!settle('REJECTED_RESPONSE', { status })) return false;
        setMessage(
          status === 409
            ? '변경이 거부되었습니다. 최신 목록에서 새 변경을 검토하세요.'
            : '변경이 거부되었습니다. 활성 상태·변환 완료·권리와 접근 권한을 확인하세요.',
        );
      } else {
        settle('UNKNOWN', { status: status ?? null });
        setMessage(
          '변경 결과가 불명확합니다(HOLD). 현재 목록 조회는 이 요청의 성공 증거가 아닙니다. 요청을 반복하지 마세요.',
        );
      }
      await reload();
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
        attempt.version !== 1 ||
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
        attempt.proposal.creditsConfirmed !== true ||
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
