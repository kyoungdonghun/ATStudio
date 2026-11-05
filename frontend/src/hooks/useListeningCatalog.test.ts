import { createElement } from 'react';
import { act, render, renderHook, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
const mocks = vi.hoisted(() => ({
  get: vi.fn(),
  put: vi.fn(),
  reconcile: vi.fn(),
  id: 1,
  token: 'token-A',
  generation: 1,
  pause: vi.fn(),
  setPlayer: vi.fn(),
}));
vi.mock('@/config/appMode', () => ({ isListeningMode: true }));
vi.mock('@/api/tracks', () => ({
  fetchListeningCatalog: (...a: unknown[]) => mocks.get(...a),
  reconcileListeningCatalog: (...a: unknown[]) => mocks.reconcile(...a),
  changeListeningCatalog: (...a: unknown[]) => mocks.put(...a),
}));
vi.mock('@/store/authStore', () => ({
  getAuthSessionGeneration: () => mocks.generation,
  useAuthStore: Object.assign(
    (s: (v: { user: { id: number }; accessToken: string }) => unknown) =>
      s({ user: { id: mocks.id }, accessToken: mocks.token }),
    { getState: () => ({ user: { id: mocks.id }, accessToken: mocks.token }) },
  ),
}));
vi.mock('@/store/playerStore', () => ({
  usePlayerStore: {
    getState: () => ({
      currentTrack: { id: 11 },
      queue: [{ id: 11 }, { id: 99 }],
      trackListContext: [{ id: 11 }],
      pause: mocks.pause,
    }),
    setState: mocks.setPlayer,
  },
}));
import { useListeningCatalog } from './useListeningCatalog';
import { ListeningCatalogStatus } from '@/components/track/ListeningCatalogControls';
const snapshot = {
  source: 'DATABASE' as const,
  revision: 4,
  trackIds: [11],
  digest: 'before-digest',
};
const key = 'listening-catalog-hold:operator:1';
const result = { ...snapshot, revision: 5, trackIds: [11, 99] };
beforeEach(() => {
  mocks.id = 1;
  mocks.token = 'token-A';
  mocks.generation = 1;
  mocks.get.mockReset().mockResolvedValue(snapshot);
  mocks.put.mockReset();
  mocks.reconcile.mockReset();
  mocks.pause.mockReset();
  mocks.setPlayer.mockReset();
  sessionStorage.clear();
});
async function ready() {
  const h = renderHook(() => useListeningCatalog());
  await waitFor(() => expect(h.result.current.loading).toBe(false));
  return h;
}
function deferred<T>() {
  let resolve!: (value: T) => void;
  const promise = new Promise<T>((r) => {
    resolve = r;
  });
  return { promise, resolve };
}
describe('publication attempt ownership and HOLD', () => {
  it('does not write on load or missing reason/credit confirmation', async () => {
    const h = await ready();
    await act(async () => {
      await h.result.current.change(99, true, '', true);
      await h.result.current.change(99, true, 'reason', false);
    });
    expect(mocks.put).not.toHaveBeenCalled();
  });
  it('keeps immutable proposal and outcome after confirmed response, prunes playback', async () => {
    mocks.put.mockResolvedValue({ ...snapshot, revision: 5, trackIds: [] });
    const h = await ready();
    await act(async () => {
      expect(await h.result.current.change(11, false, ' reviewed ', true)).toBe(true);
    });
    expect(mocks.put).toHaveBeenCalledWith({
      attemptId: expect.any(String),
      expectedRevision: 4,
      trackIds: [],
      reason: 'reviewed',
      creditsConfirmed: true,
    });
    expect(mocks.pause).toHaveBeenCalled();
    expect(h.result.current.hold).toBe(false);
    const archive = Object.keys(sessionStorage).find(
      (k) => k.startsWith('listening-catalog-attempt:') && !k.endsWith(':outcome'),
    )!;
    expect(JSON.parse(sessionStorage.getItem(archive)!)).toMatchObject({
      actor: 1,
      before: snapshot,
      proposal: { reason: 'reviewed', trackIds: [] },
    });
    expect(sessionStorage.getItem(archive + ':outcome')).toContain('CONFIRMED_RESPONSE');
  });
  it('refreshes conflict without replay and preserves the rejected attempt', async () => {
    mocks.put.mockRejectedValue({ response: { status: 409 } });
    const h = await ready();
    mocks.get.mockResolvedValue({ ...snapshot, revision: 5 });
    await act(async () => {
      await h.result.current.change(99, true, 'reason', true);
    });
    expect(mocks.put).toHaveBeenCalledTimes(1);
    expect(h.result.current.snapshot?.revision).toBe(5);
    expect(h.result.current.hold).toBe(false);
    expect(Object.values(sessionStorage).join()).toContain('REJECTED_RESPONSE');
  });
  it('review repro: unknown retains unique actor/before/proposal/reason/credit identity across remount', async () => {
    mocks.put.mockRejectedValue(new Error('lost'));
    const h = await ready();
    mocks.get.mockResolvedValue(result);
    await act(async () => {
      await h.result.current.change(99, true, 'exact-audit-reason-unique', true);
    });
    const raw = sessionStorage.getItem(key)!;
    const journal = JSON.parse(raw);
    expect(journal).toMatchObject({
      version: 1,
      actor: 1,
      before: snapshot,
      proposal: {
        expectedRevision: 4,
        trackIds: [11, 99],
        reason: 'exact-audit-reason-unique',
        creditsConfirmed: true,
      },
    });
    expect(journal.attemptId).toMatch(/^[0-9a-f-]{36}$/);
    expect(journal.startedAt).toBeTruthy();
    expect(raw).not.toContain('token-A');
    expect(sessionStorage.getItem('listening-catalog-attempt:' + journal.attemptId)).toBe(raw);
    h.unmount();
    const next = await ready();
    await act(async () => {
      await next.result.current.change(99, true, 'again', true);
      await next.result.current.reload();
    });
    expect(mocks.put).toHaveBeenCalledTimes(1);
    expect(next.result.current.hold).toBe(true);
    expect(sessionStorage.getItem(key)).toBe(raw);
  });
  it('review repro: no checkbox/reference release survives across attempts because release is absent', async () => {
    sessionStorage.setItem(key, 'attempt-A');
    const h = await ready();
    const view = render(createElement(ListeningCatalogStatus, { catalog: h.result.current }));
    expect(screen.queryByRole('button', { name: '감사 확인 후 HOLD 해제' })).toBeNull();
    expect(screen.queryByRole('checkbox')).toBeNull();
    sessionStorage.setItem(key, 'attempt-B');
    h.rerender();
    view.rerender(createElement(ListeningCatalogStatus, { catalog: h.result.current }));
    expect(screen.queryByRole('button', { name: '감사 확인 후 HOLD 해제' })).toBeNull();
    expect('releaseHold' in h.result.current).toBe(false);
    expect(sessionStorage.getItem(key)).toBe('attempt-B');
  });
  it('review repro: late success cannot delete or overwrite a successor attempt', async () => {
    const d = deferred<typeof result>();
    mocks.put.mockReturnValue(d.promise);
    const h = await ready();
    let pending!: Promise<boolean>;
    act(() => {
      pending = h.result.current.change(99, true, 'attempt A', true);
    });
    const original = sessionStorage.getItem(key)!;
    const successor = JSON.stringify({
      attemptId: 'attempt-B',
      actor: 1,
      before: { revision: 5 },
      proposal: { trackIds: [100] },
    });
    sessionStorage.setItem(key, successor);
    await act(async () => {
      d.resolve(result);
      expect(await pending).toBe(false);
    });
    expect(sessionStorage.getItem(key)).toBe(successor);
    expect(
      sessionStorage.getItem('listening-catalog-attempt:' + JSON.parse(original).attemptId),
    ).toBe(original);
    expect(
      sessionStorage.getItem(
        'listening-catalog-attempt:' + JSON.parse(original).attemptId + ':outcome',
      ),
    ).toBeNull();
    expect(mocks.setPlayer).not.toHaveBeenCalled();
  });
  it('same-user replacement token rejects late response before any rerender', async () => {
    const d = deferred<typeof result>();
    mocks.put.mockReturnValue(d.promise);
    const h = await ready();
    let pending!: Promise<boolean>;
    act(() => {
      pending = h.result.current.change(99, true, 'reason', true);
    });
    const raw = sessionStorage.getItem(key);
    mocks.token = 'token-B';
    mocks.generation = 2;
    await act(async () => {
      d.resolve(result);
      await pending;
    });
    expect(sessionStorage.getItem(key)).toBe(raw);
    expect(mocks.setPlayer).not.toHaveBeenCalled();
  });
  it('same token but newer session generation also rejects late response', async () => {
    const d = deferred<typeof result>();
    mocks.put.mockReturnValue(d.promise);
    const h = await ready();
    let pending!: Promise<boolean>;
    act(() => {
      pending = h.result.current.change(99, true, 'reason', true);
    });
    const raw = sessionStorage.getItem(key);
    mocks.generation = 2;
    await act(async () => {
      d.resolve(result);
      await pending;
    });
    expect(sessionStorage.getItem(key)).toBe(raw);
  });
  it('actor switch cannot settle the old actor attempt or contaminate the new snapshot', async () => {
    const d = deferred<typeof result>();
    mocks.put.mockReturnValue(d.promise);
    const h = await ready();
    let pending!: Promise<boolean>;
    act(() => {
      pending = h.result.current.change(99, true, 'reason', true);
    });
    const raw = sessionStorage.getItem(key);
    mocks.id = 2;
    mocks.token = 'second-actor';
    mocks.generation = 2;
    mocks.get.mockResolvedValue({ ...snapshot, revision: 8 });
    h.rerender();
    await waitFor(() => expect(h.result.current.snapshot?.revision).toBe(8));
    await act(async () => {
      d.resolve(result);
      await pending;
    });
    expect(sessionStorage.getItem(key)).toBe(raw);
    expect(h.result.current.snapshot?.revision).toBe(8);
    expect(h.result.current.hold).toBe(false);
  });
  it('late GET cannot install a snapshot after a newer active attempt appears', async () => {
    const h = await ready();
    const d = deferred<typeof snapshot>();
    mocks.get.mockReturnValue(d.promise);
    let read!: Promise<void>;
    act(() => {
      read = h.result.current.reload();
    });
    sessionStorage.setItem(key, 'successor-attempt');
    await act(async () => {
      d.resolve(snapshot);
      await read;
    });
    expect(h.result.current.snapshot).toBeNull();
    expect(sessionStorage.getItem(key)).toBe('successor-attempt');
  });
  it('STATIC and unavailable GET are read only without seed', async () => {
    mocks.get.mockResolvedValue({ ...snapshot, source: 'STATIC' });
    const h = await ready();
    await act(async () => {
      await h.result.current.change(99, true, 'reason', true);
    });
    expect(mocks.put).not.toHaveBeenCalled();
    mocks.get.mockRejectedValue({ response: { status: 503 } });
    await act(async () => {
      await h.result.current.reload();
      await h.result.current.change(99, true, 'reason', true);
    });
    expect(h.result.current.snapshot).toBeNull();
    expect(mocks.put).not.toHaveBeenCalled();
  });
  it('blocks duplicate dispatch while pending', async () => {
    const d = deferred<typeof result>();
    mocks.put.mockReturnValue(d.promise);
    const h = await ready();
    let pending!: Promise<boolean>;
    act(() => {
      pending = h.result.current.change(99, true, 'reason', true);
    });
    await act(async () => {
      await h.result.current.change(99, true, 'reason', true);
    });
    expect(mocks.put).toHaveBeenCalledTimes(1);
    await act(async () => {
      d.resolve(result);
      await pending;
    });
  });
  it('unattributable 200 response remains HOLD with original journal', async () => {
    mocks.put.mockResolvedValue({ ...result, revision: 19 });
    const h = await ready();
    await act(async () => {
      await h.result.current.change(99, true, 'reason', true);
    });
    expect(h.result.current.hold).toBe(true);
    expect(Object.values(sessionStorage).join()).toContain('UNKNOWN');
  });
  it.each(['archive', 'active'])(
    'blocks PUT when %s journal persistence fails',
    async (failedWrite) => {
      const h = await ready();
      const original = Storage.prototype.setItem;
      const spy = vi.spyOn(Storage.prototype, 'setItem').mockImplementation(function (
        this: Storage,
        key: string,
        value: string,
      ) {
        if (
          (failedWrite === 'archive' && key.startsWith('listening-catalog-attempt:')) ||
          (failedWrite === 'active' && key.startsWith('listening-catalog-hold:'))
        )
          throw new DOMException('Quota exceeded', 'QuotaExceededError');
        original.call(this, key, value);
      });
      try {
        await act(async () => {
          expect(await h.result.current.change(99, true, 'reason', true)).toBe(false);
        });
        expect(mocks.put).not.toHaveBeenCalled();
      } finally {
        spy.mockRestore();
      }
    },
  );
  it('corrupt active record fails closed and survives GET unchanged', async () => {
    sessionStorage.setItem(key, '{broken');
    const h = await ready();
    await act(async () => {
      await h.result.current.change(99, true, 'reason', true);
      await h.result.current.reload();
    });
    expect(mocks.put).not.toHaveBeenCalled();
    expect(h.result.current.hold).toBe(true);
    expect(sessionStorage.getItem(key)).toBe('{broken');
  });
  async function unknownAttempt() {
    mocks.put.mockRejectedValue(new Error('lost'));
    const h = await ready();
    await act(async () => {
      await h.result.current.change(99, true, 'exact-reason', true);
    });
    const raw = sessionStorage.getItem(key)!;
    const attempt = JSON.parse(raw);
    const receipt = {
      auditId: 9,
      actorId: 1,
      attemptId: attempt.attemptId,
      reason: 'exact-reason',
      before: snapshot,
      after: result,
      recordedAt: '2026-11-05T14:00:00Z',
    };
    return { h, raw, attempt, receipt };
  }
  it('exact committed receipt plus fresh matching GET clears only active pointer and preserves every journal', async () => {
    const { h, raw, attempt, receipt } = await unknownAttempt();
    mocks.reconcile.mockResolvedValue({ status: 'COMMITTED', receipt });
    mocks.get.mockResolvedValue(result);
    await act(async () => {
      await h.result.current.reconcile();
    });
    expect(h.result.current.hold).toBe(false);
    expect(mocks.put).toHaveBeenCalledTimes(1);
    expect(sessionStorage.getItem('listening-catalog-attempt:' + attempt.attemptId)).toBe(raw);
    expect(Object.values(sessionStorage).join()).toContain('UNKNOWN');
    expect(Object.values(sessionStorage).join()).toContain('"auditId":9');
    expect(mocks.reconcile).toHaveBeenCalledWith({
      attemptId: attempt.attemptId,
      expectedRevision: 4,
      beforeTrackIds: [11],
      beforeDigest: 'before-digest',
      trackIds: [11, 99],
      reason: 'exact-reason',
    });
  });
  it.each([
    'UNKNOWN',
    'actor',
    'attempt',
    'before',
    'proposal',
    'reason',
    'shape',
    'current',
    'error',
  ])('keeps HOLD when audit reconciliation has %s mismatch', async (mode) => {
    const { h, raw, receipt } = await unknownAttempt();
    const changed = JSON.parse(JSON.stringify(receipt));
    if (mode === 'actor') changed.actorId = 2;
    if (mode === 'attempt') changed.attemptId = 'another';
    if (mode === 'before') changed.before.digest = 'another';
    if (mode === 'proposal') changed.after.trackIds = [11, 100];
    if (mode === 'reason') changed.reason = 'another';
    mocks.reconcile.mockResolvedValue(
      mode === 'UNKNOWN'
        ? { status: 'UNKNOWN', receipt: null }
        : mode === 'shape'
          ? {}
          : { status: 'COMMITTED', receipt: changed },
    );
    if (mode === 'error') mocks.reconcile.mockRejectedValue(new Error('network'));
    mocks.get.mockResolvedValue(mode === 'current' ? { ...result, revision: 4 } : result);
    await act(async () => {
      await h.result.current.reconcile();
    });
    expect(h.result.current.hold).toBe(true);
    expect(sessionStorage.getItem(key)).toBe(raw);
    expect(mocks.put).toHaveBeenCalledTimes(1);
  });
  it('late reconcile receipt cannot clear successor attempt', async () => {
    const { h, receipt } = await unknownAttempt();
    const d = deferred<unknown>();
    mocks.reconcile.mockReturnValue(d.promise);
    let pending!: Promise<void>;
    act(() => {
      pending = h.result.current.reconcile();
    });
    sessionStorage.setItem(key, 'successor');
    await act(async () => {
      d.resolve({ status: 'COMMITTED', receipt });
      await pending;
    });
    expect(sessionStorage.getItem(key)).toBe('successor');
  });
  it('session replacement during final reconcile GET preserves HOLD', async () => {
    const { h, raw, receipt } = await unknownAttempt();
    mocks.reconcile.mockResolvedValue({ status: 'COMMITTED', receipt });
    const d = deferred<typeof result>();
    mocks.get.mockReturnValue(d.promise);
    let pending!: Promise<void>;
    act(() => {
      pending = h.result.current.reconcile();
    });
    await waitFor(() => expect(mocks.get.mock.calls.length).toBe(3));
    mocks.generation += 1;
    mocks.token = 'replacement';
    await act(async () => {
      d.resolve(result);
      await pending;
    });
    expect(sessionStorage.getItem(key)).toBe(raw);
  });
  it('historical committed receipt accepts newer current revision without restoring old desired IDs', async () => {
    const { h, receipt } = await unknownAttempt();
    mocks.reconcile.mockResolvedValue({ status: 'COMMITTED', receipt });
    mocks.get.mockResolvedValue({ ...result, revision: 8, trackIds: [11], digest: 'newer-digest' });
    await act(async () => {
      await h.result.current.reconcile();
    });
    expect(h.result.current.hold).toBe(false);
    expect(h.result.current.snapshot?.revision).toBe(8);
    expect(h.result.current.snapshot?.trackIds).toEqual([11]);
    expect(mocks.put).toHaveBeenCalledTimes(1);
    expect(mocks.setPlayer).toHaveBeenLastCalledWith(
      expect.objectContaining({ queue: [{ id: 11 }] }),
    );
  });
  it('same revision but contradictory digest cannot clear HOLD', async () => {
    const { h, raw, receipt } = await unknownAttempt();
    mocks.reconcile.mockResolvedValue({ status: 'COMMITTED', receipt });
    mocks.get.mockResolvedValue({ ...result, digest: 'contradiction' });
    await act(async () => {
      await h.result.current.reconcile();
    });
    expect(sessionStorage.getItem(key)).toBe(raw);
  });
});
