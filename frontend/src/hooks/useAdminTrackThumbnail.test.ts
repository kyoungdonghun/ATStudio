import { act, renderHook, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
const state = vi.hoisted(() => ({ fetch: vi.fn(), token: 'token-A' as string | null }));
vi.mock('@/config/appMode', () => ({ isListeningMode: true }));
vi.mock('@/api/client', () => ({
  toUploadUrl: vi.fn(() => {
    throw new Error('public fallback');
  }),
}));
vi.mock('@/api/tracks', () => ({
  fetchAdminTrackThumbnail: (...args: unknown[]) => state.fetch(...args),
}));
vi.mock('@/store/authStore', () => ({
  useAuthStore: (selector: (s: { accessToken: string | null }) => unknown) =>
    selector({ accessToken: state.token }),
}));
import { useAdminTrackThumbnail } from './useAdminTrackThumbnail';

describe('protected listening cover lifecycle', () => {
  const create = vi.fn();
  const revoke = vi.fn();
  beforeEach(() => {
    state.fetch.mockReset();
    state.token = 'token-A';
    create.mockReset().mockReturnValue('blob:fixture');
    revoke.mockReset();
    Object.defineProperty(URL, 'createObjectURL', { configurable: true, value: create });
    Object.defineProperty(URL, 'revokeObjectURL', { configurable: true, value: revoke });
  });
  it('uses protected bytes and revokes the URL and aborts on unmount', async () => {
    state.fetch.mockResolvedValue(new Blob(['cover']));
    const { result, unmount } = renderHook(() =>
      useAdminTrackThumbnail(99, 'tracks/thumbnails/private.png'),
    );
    await waitFor(() => expect(result.current).toBe('blob:fixture'));
    const signal = state.fetch.mock.calls[0][1] as AbortSignal;
    expect(state.fetch.mock.calls[0][0]).toBe(99);
    unmount();
    expect(signal.aborted).toBe(true);
    expect(revoke).toHaveBeenCalledWith('blob:fixture');
  });
  it('discards an old track response after switching IDs', async () => {
    let oldResolve!: (b: Blob) => void;
    state.fetch
      .mockImplementationOnce(
        () =>
          new Promise<Blob>((r) => {
            oldResolve = r;
          }),
      )
      .mockResolvedValue(new Blob(['new']));
    const { result, rerender } = renderHook(({ id }) => useAdminTrackThumbnail(id, 'private.png'), {
      initialProps: { id: 99 },
    });
    rerender({ id: 100 });
    await waitFor(() => expect(result.current).toBe('blob:fixture'));
    await act(async () => oldResolve(new Blob(['old'])));
    expect(create).toHaveBeenCalledTimes(1);
  });
  it('revokes on session change and discards a pending response after logout', async () => {
    state.fetch.mockResolvedValueOnce(new Blob(['cover']));
    const { result, rerender } = renderHook(() => useAdminTrackThumbnail(99, 'private.png'));
    await waitFor(() => expect(result.current).toBe('blob:fixture'));
    let resolve!: (b: Blob) => void;
    state.fetch.mockImplementationOnce(
      () =>
        new Promise<Blob>((r) => {
          resolve = r;
        }),
    );
    state.token = 'token-B';
    rerender();
    expect(result.current).toBe(null);
    expect(revoke).toHaveBeenCalledWith('blob:fixture');
    state.token = null;
    rerender();
    await act(async () => resolve(new Blob(['late'])));
    expect(result.current).toBe(null);
    expect(create).toHaveBeenCalledTimes(1);
  });
  it('fails closed without a public URL fallback', async () => {
    state.fetch.mockRejectedValue(new Error('403'));
    const { result } = renderHook(() => useAdminTrackThumbnail(99, 'private.png'));
    await act(async () => {});
    expect(result.current).toBe(null);
    expect(create).not.toHaveBeenCalled();
  });
});
