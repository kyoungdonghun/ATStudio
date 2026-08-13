import { act, renderHook, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { fetchPublicCapabilities, type PublicCapabilitiesResponse } from '@/api/auth';
import { usePublicCapabilities } from '@/hooks/usePublicCapabilities';

vi.mock('@/api/auth', () => ({
  fetchPublicCapabilities: vi.fn(),
}));

const fetchPublicCapabilitiesMock = vi.mocked(fetchPublicCapabilities);

function buildCapabilities(passwordLoginEnabled: boolean): PublicCapabilitiesResponse {
  return {
    passwordLoginEnabled,
    emailVerification: { enabled: passwordLoginEnabled, deliveryMode: 'REMOTE_SMTP' },
    passwordReset: { enabled: passwordLoginEnabled, deliveryMode: 'REMOTE_SMTP' },
    socialLogin: {
      google: { enabled: false, clientId: null, redirectUri: null },
      kakao: { enabled: false, clientId: null, redirectUri: null },
      naver: { enabled: false, clientId: null, redirectUri: null },
    },
    testUsersEnabled: false,
  };
}

function deferred<T>() {
  let resolve!: (value: T) => void;
  let reject!: (reason?: unknown) => void;
  const promise = new Promise<T>((resolvePromise, rejectPromise) => {
    resolve = resolvePromise;
    reject = rejectPromise;
  });
  return { promise, resolve, reject };
}

describe('usePublicCapabilities', () => {
  beforeEach(() => {
    fetchPublicCapabilitiesMock.mockReset();
  });

  it('keeps loading, ready, and failure states distinct and retries only on request', async () => {
    fetchPublicCapabilitiesMock
      .mockRejectedValueOnce(new Error('offline'))
      .mockResolvedValueOnce(buildCapabilities(true));

    const { result } = renderHook(() => usePublicCapabilities());

    expect(result.current.loading).toBe(true);
    expect(result.current.capabilities).toBeNull();

    await waitFor(() => expect(result.current.status).toBe('error'));
    expect(result.current.capabilities).toBeNull();
    expect(fetchPublicCapabilitiesMock).toHaveBeenCalledTimes(1);

    act(() => {
      void result.current.retry();
    });

    expect(result.current.status).toBe('loading');
    expect(result.current.capabilities).toBeNull();
    await waitFor(() => expect(result.current.status).toBe('ready'));
    expect(result.current.capabilities?.passwordLoginEnabled).toBe(true);
    expect(fetchPublicCapabilitiesMock).toHaveBeenCalledTimes(2);
  });

  it('ignores an older response after an explicit retry owns the latest request', async () => {
    const first = deferred<PublicCapabilitiesResponse>();
    const second = deferred<PublicCapabilitiesResponse>();
    fetchPublicCapabilitiesMock
      .mockReturnValueOnce(first.promise)
      .mockReturnValueOnce(second.promise);

    const { result } = renderHook(() => usePublicCapabilities());

    act(() => {
      void result.current.retry();
    });
    await act(async () => {
      second.resolve(buildCapabilities(false));
      await second.promise;
    });

    expect(result.current.status).toBe('ready');
    expect(result.current.capabilities?.passwordLoginEnabled).toBe(false);

    await act(async () => {
      first.resolve(buildCapabilities(true));
      await first.promise;
    });

    expect(result.current.capabilities?.passwordLoginEnabled).toBe(false);
    expect(fetchPublicCapabilitiesMock).toHaveBeenCalledTimes(2);
  });
});
