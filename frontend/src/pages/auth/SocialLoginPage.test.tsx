import { StrictMode } from 'react';
import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { Link, MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { LoginResponse, MeResponse } from '@/api/auth';
import SocialLoginPage from '@/pages/auth/SocialLoginPage';
import { useAlbumLikeStore } from '@/store/albumLikeStore';
import { useAuthStore } from '@/store/authStore';
import { useLikeStore } from '@/store/likeStore';
import { usePlayerStore } from '@/store/playerStore';
import type { PlayableTrack } from '@/types';
import { createOAuthAttempt } from '@/utils/oauthAttempt';
import { safeStorage } from '@/utils/safeStorage';

const { fetchMeMock, logoutSessionMock, socialLoginMock } = vi.hoisted(() => ({
  fetchMeMock: vi.fn(),
  logoutSessionMock: vi.fn(),
  socialLoginMock: vi.fn(),
}));

vi.mock('@/api/auth', () => ({
  fetchMe: (...args: unknown[]) => fetchMeMock(...args),
  logoutSession: (...args: unknown[]) => logoutSessionMock(...args),
  socialLogin: (...args: unknown[]) => socialLoginMock(...args),
}));

const profile: MeResponse = {
  id: 1,
  nickname: 'social-user',
  email: 'social@example.com',
  phonePersonal: null,
  phoneCompany: null,
  job: 'EDITOR',
  companyName: null,
  userType: 'INDIVIDUAL',
  role: 'USER',
  isVerified: true,
  createdAt: '2026-07-14T00:00:00Z',
};

const publicTrack: PlayableTrack = {
  id: 4,
  title: 'Public track',
  artistName: 'Artist',
  duration: 7,
  bpm: 92,
  tonality: 'C',
  thumbnail: null,
  waveformData: null,
  tags: [],
};

function prepareCallbackStorage() {
  createOAuthAttempt('expected-state-1234', 'pkce-verifier', '/');
}

function renderPage(strict = false) {
  const routes = (
    <MemoryRouter
      initialEntries={['/social-login/google?code=authorization-code&state=expected-state-1234']}
    >
      <Link to="/social-login/google?code=replacement-code&state=replacement-state-1234">
        replacement callback
      </Link>
      <Routes>
        <Route path="/social-login/:provider" element={<SocialLoginPage />} />
        <Route path="/" element={<p>home</p>} />
        <Route path="/profile" element={<p>profile destination</p>} />
        <Route path="/admin/dashboard" element={<p>admin destination</p>} />
        <Route path="/complete-profile" element={<p>complete profile</p>} />
      </Routes>
    </MemoryRouter>
  );

  return render(strict ? <StrictMode>{routes}</StrictMode> : routes);
}

describe('SocialLoginPage', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    fetchMeMock.mockReset();
    logoutSessionMock.mockReset();
    socialLoginMock.mockReset();
    localStorage.clear();
    sessionStorage.clear();
    useAuthStore.getState().clearSession();
  });

  it('shows the AT.M brand while processing the callback', () => {
    prepareCallbackStorage();
    socialLoginMock.mockReturnValue(new Promise(() => {}));

    renderPage();

    expect(screen.getByRole('heading', { name: 'AT.M' })).toBeInTheDocument();
  });

  it('stages issued tokens before fetchMe and exchanges once in Strict Mode', async () => {
    const requestOrder: string[] = [];
    prepareCallbackStorage();
    socialLoginMock.mockImplementation(async () => {
      requestOrder.push('exchange');
      return {
        accessToken: 'issued-access-token',
        refreshToken: 'issued-refresh-token',
        tokenType: 'Bearer',
        expiresIn: 3600,
        isProfileComplete: true,
      };
    });
    fetchMeMock.mockImplementation(async (accessToken: string) => {
      requestOrder.push('fetchMe');
      expect(accessToken).toBe('issued-access-token');
      expect(localStorage.getItem('accessToken')).toBe('issued-access-token');
      expect(localStorage.getItem('refreshToken')).toBe('issued-refresh-token');
      expect(useAuthStore.getState()).toMatchObject({
        accessToken: 'issued-access-token',
        user: null,
        role: 'GUEST',
      });
      return profile;
    });

    renderPage(true);

    expect(await screen.findByText('home')).toBeInTheDocument();
    expect(requestOrder).toEqual(['exchange', 'fetchMe']);
    expect(socialLoginMock).toHaveBeenCalledTimes(1);
    expect(socialLoginMock).toHaveBeenCalledWith('google', 'authorization-code', 'pkce-verifier');
    expect(useAuthStore.getState()).toMatchObject({
      accessToken: 'issued-access-token',
      user: profile,
      role: 'USER',
    });
  });

  it('commits the user before routing an incomplete profile', async () => {
    prepareCallbackStorage();
    socialLoginMock.mockResolvedValue({
      accessToken: 'issued-access-token',
      refreshToken: 'issued-refresh-token',
      tokenType: 'Bearer',
      expiresIn: 3600,
      isProfileComplete: false,
    });
    fetchMeMock.mockResolvedValue(profile);

    renderPage();

    expect(await screen.findByText('complete profile')).toBeInTheDocument();
    expect(useAuthStore.getState()).toMatchObject({
      accessToken: 'issued-access-token',
      user: profile,
      role: 'USER',
    });
  });

  it('clears stale continuation and proceeds without one when profile storage fails', async () => {
    prepareCallbackStorage();
    sessionStorage.setItem(
      'oauth_profile_return',
      JSON.stringify({
        attemptId: 'stale-state-1234567',
        userId: 99,
        returnTarget: '/admin/dashboard',
        createdAt: Date.now(),
      }),
    );
    const originalSetItem = Storage.prototype.setItem;
    vi.spyOn(Storage.prototype, 'setItem').mockImplementation(function (this: Storage, key, value) {
      if (this === sessionStorage && key === 'oauth_profile_return') {
        throw new Error('storage unavailable');
      }
      return originalSetItem.call(this, key, value);
    });
    socialLoginMock.mockResolvedValue({
      accessToken: 'issued-access-token',
      refreshToken: 'issued-refresh-token',
      tokenType: 'Bearer',
      expiresIn: 3600,
      isProfileComplete: false,
    });
    fetchMeMock.mockResolvedValue(profile);

    renderPage();

    expect(await screen.findByText('complete profile')).toBeInTheDocument();
    expect(sessionStorage.getItem('oauth_profile_return')).toBeNull();
  });

  it('returns a complete social login to the stored safe internal target', async () => {
    createOAuthAttempt('expected-state-1234', 'pkce-verifier', '/profile?tab=edit');
    socialLoginMock.mockResolvedValue({
      accessToken: 'issued-access-token',
      refreshToken: 'issued-refresh-token',
      tokenType: 'Bearer',
      expiresIn: 3600,
      isProfileComplete: true,
    });
    fetchMeMock.mockResolvedValue(profile);

    renderPage();

    expect(await screen.findByText('profile destination')).toBeInTheDocument();
    expect(sessionStorage.getItem('oauth_attempt:expected-state-1234')).toBeNull();
  });

  it('rejects a stored target that does not match the authenticated role', async () => {
    createOAuthAttempt('expected-state-1234', 'pkce-verifier', '/admin/dashboard');
    socialLoginMock.mockResolvedValue({
      accessToken: 'issued-access-token',
      refreshToken: 'issued-refresh-token',
      tokenType: 'Bearer',
      expiresIn: 3600,
      isProfileComplete: true,
    });
    fetchMeMock.mockResolvedValue(profile);

    renderPage();

    expect(await screen.findByText('home')).toBeInTheDocument();
    expect(screen.queryByText('admin destination')).not.toBeInTheDocument();
  });

  it('rejects a missing or already consumed callback attempt before provider exchange', async () => {
    renderPage();

    expect(
      await screen.findByText('보안 검증에 실패했습니다. 다시 로그인해주세요.'),
    ).toBeInTheDocument();
    expect(socialLoginMock).not.toHaveBeenCalled();
  });

  it('best-effort revokes and always clears a staged session when fetchMe fails', async () => {
    prepareCallbackStorage();
    useLikeStore.setState({ likedIds: new Set([1]), loaded: true });
    useAlbumLikeStore.setState({ likedAlbumIds: new Set([2]), loaded: true });
    usePlayerStore.setState({
      currentTrack: publicTrack,
      isPlaying: false,
      currentTime: 0.7,
      duration: publicTrack.duration,
      queue: [publicTrack],
    });
    socialLoginMock.mockResolvedValue({
      accessToken: 'issued-access-token',
      refreshToken: 'issued-refresh-token',
      tokenType: 'Bearer',
      expiresIn: 3600,
      isProfileComplete: true,
    });
    fetchMeMock.mockRejectedValue({
      response: { data: { message: '사용자 정보를 불러오지 못했습니다.' } },
    });
    logoutSessionMock.mockImplementation(async () => {
      expect(localStorage.getItem('accessToken')).toBe('issued-access-token');
      expect(useAuthStore.getState().accessToken).toBe('issued-access-token');
      throw new Error('network unavailable');
    });

    renderPage();

    expect(
      await screen.findByText('소셜 로그인에 실패했습니다. 다시 시도해주세요.'),
    ).toBeInTheDocument();
    expect(screen.queryByText('사용자 정보를 불러오지 못했습니다.')).not.toBeInTheDocument();
    expect(logoutSessionMock).toHaveBeenCalledTimes(1);
    expect(localStorage.getItem('accessToken')).toBeNull();
    expect(localStorage.getItem('refreshToken')).toBeNull();
    expect(localStorage.getItem('user')).toBeNull();
    expect(useAuthStore.getState()).toMatchObject({
      accessToken: null,
      user: null,
      role: 'GUEST',
    });
    expect(useLikeStore.getState()).toMatchObject({ loaded: false });
    expect(useLikeStore.getState().likedIds.size).toBe(0);
    expect(useAlbumLikeStore.getState()).toMatchObject({ loaded: false });
    expect(useAlbumLikeStore.getState().likedAlbumIds.size).toBe(0);
    expect(usePlayerStore.getState()).toMatchObject({
      currentTrack: publicTrack,
      isPlaying: false,
      currentTime: 0.7,
      duration: publicTrack.duration,
      queue: [publicTrack],
    });
  });

  it.each(
    (['exchange', 'profile'] as const).flatMap((phase) =>
      (['replacement', 'same-user', 'logout', 'unmount'] as const).flatMap((change) =>
        (['success', 'failure'] as const).map((result) => ({ phase, change, result })),
      ),
    ),
  )('ignores stale $phase $result after $change (WI010-F1)', async ({ phase, change, result }) => {
    const exchange = deferred<SocialTokens>();
    const currentUser = deferred<MeResponse>();
    prepareCallbackStorage();
    useAuthStore.getState().login(issuedTokens.accessToken, profile, issuedTokens.refreshToken);
    socialLoginMock.mockReturnValue(
      phase === 'exchange' ? exchange.promise : Promise.resolve(issuedTokens),
    );
    fetchMeMock.mockReturnValue(currentUser.promise);
    const view = renderPage();
    await waitFor(() => expect(socialLoginMock).toHaveBeenCalledTimes(1));
    if (phase === 'profile') await waitFor(() => expect(fetchMeMock).toHaveBeenCalledTimes(1));

    act(() => {
      if (change === 'unmount') view.unmount();
      else if (change === 'logout') useAuthStore.getState().clearSession();
      else {
        useAuthStore
          .getState()
          .login(
            issuedTokens.accessToken,
            change === 'same-user' ? profile : { ...profile, id: 2 },
            issuedTokens.refreshToken,
          );
      }
    });
    sessionStorage.setItem('oauth_profile_return', 'new-session-continuation');
    const expectedState = useAuthStore.getState();
    const expectedStorage = { ...localStorage };
    const expectedContinuation = { ...sessionStorage };

    await act(async () => {
      if (phase === 'exchange') {
        if (result === 'success') exchange.resolve(issuedTokens);
        else exchange.reject(new Error('old exchange failed'));
      } else if (result === 'success') currentUser.resolve(profile);
      else currentUser.reject(new Error('old profile failed'));
    });

    expect(useAuthStore.getState()).toBe(expectedState);
    expect({ ...localStorage }).toEqual(expectedStorage);
    expect({ ...sessionStorage }).toEqual(expectedContinuation);
    expect(logoutSessionMock).not.toHaveBeenCalled();
    expect(fetchMeMock).toHaveBeenCalledTimes(phase === 'exchange' ? 0 : 1);
    expect(screen.queryByText('home')).not.toBeInTheDocument();
    expect(screen.queryByText('complete profile')).not.toBeInTheDocument();
    expect(
      screen.queryByText('소셜 로그인에 실패했습니다. 다시 시도해주세요.'),
    ).not.toBeInTheDocument();
    expect(
      screen.queryByText('네트워크 연결을 확인하고 소셜 로그인을 다시 시도해주세요.'),
    ).not.toBeInTheDocument();
  });

  it.each(['exchange', 'profile'] as const)(
    'ignores an older %s after a new callback attempt on the same mounted page',
    async (phase) => {
      const oldExchange = deferred<SocialTokens>();
      const newExchange = deferred<SocialTokens>();
      const oldProfile = deferred<MeResponse>();
      prepareCallbackStorage();
      createOAuthAttempt('replacement-state-1234', 'replacement-verifier', '/profile');
      socialLoginMock
        .mockReturnValueOnce(
          phase === 'exchange' ? oldExchange.promise : Promise.resolve(issuedTokens),
        )
        .mockReturnValueOnce(newExchange.promise);
      if (phase === 'profile') fetchMeMock.mockReturnValueOnce(oldProfile.promise);
      fetchMeMock.mockResolvedValue(profile);
      renderPage(true);
      await waitFor(() => expect(socialLoginMock).toHaveBeenCalledTimes(1));
      if (phase === 'profile') await waitFor(() => expect(fetchMeMock).toHaveBeenCalledTimes(1));

      fireEvent.click(screen.getByRole('link', { name: 'replacement callback' }));
      await waitFor(() => expect(socialLoginMock).toHaveBeenCalledTimes(2));
      const expectedState = useAuthStore.getState();
      await act(async () => {
        if (phase === 'exchange') oldExchange.resolve(issuedTokens);
        else oldProfile.resolve(profile);
      });
      expect(useAuthStore.getState()).toBe(expectedState);
      expect(screen.queryByText('complete profile')).not.toBeInTheDocument();
      expect(logoutSessionMock).not.toHaveBeenCalled();
      await act(async () => {
        newExchange.resolve({ ...issuedTokens, isProfileComplete: true });
      });

      expect(await screen.findByText('profile destination')).toBeInTheDocument();
      expect(socialLoginMock).toHaveBeenCalledTimes(2);
      expect(fetchMeMock).toHaveBeenCalledTimes(phase === 'exchange' ? 1 : 2);
      expect(useAuthStore.getState().user).toEqual(profile);
    },
  );

  it('does not report the old failure after staged-session logout yields to a new login', async () => {
    const logout = deferred<'confirmed'>();
    prepareCallbackStorage();
    socialLoginMock.mockResolvedValue(issuedTokens);
    fetchMeMock.mockRejectedValue(new Error('old profile failed'));
    logoutSessionMock.mockReturnValue(logout.promise);
    renderPage();
    await waitFor(() => expect(logoutSessionMock).toHaveBeenCalledTimes(1));
    act(() => {
      useAuthStore.getState().login('new-access', { ...profile, id: 2 }, 'new-refresh');
    });
    const expectedState = useAuthStore.getState();
    await act(async () => {
      logout.resolve('confirmed');
    });

    expect(useAuthStore.getState()).toBe(expectedState);
    expect(
      screen.queryByText('소셜 로그인에 실패했습니다. 다시 시도해주세요.'),
    ).not.toBeInTheDocument();
    expect(
      screen.queryByText('네트워크 연결을 확인하고 소셜 로그인을 다시 시도해주세요.'),
    ).not.toBeInTheDocument();
  });

  it.each(['stage', 'commit'] as const)(
    'does not adopt a newer generation triggered synchronously during its own %s',
    async (phase) => {
      prepareCallbackStorage();
      socialLoginMock.mockResolvedValue(issuedTokens);
      fetchMeMock.mockResolvedValue(profile);
      const unsubscribe = useAuthStore.subscribe((state) => {
        if (
          state.accessToken === issuedTokens.accessToken &&
          (phase === 'stage' ? state.user === null : state.user?.id === profile.id)
        ) {
          useAuthStore
            .getState()
            .login('replacement-access', { ...profile, id: 2 }, 'replacement-refresh');
        }
      });
      try {
        renderPage(true);
        await waitFor(() => expect(useAuthStore.getState().user?.id).toBe(2));
        expect(useAuthStore.getState().accessToken).toBe('replacement-access');
        expect(fetchMeMock).toHaveBeenCalledTimes(phase === 'stage' ? 0 : 1);
        expect(sessionStorage.getItem('oauth_profile_return')).toBeNull();
        expect(screen.queryByText('complete profile')).not.toBeInTheDocument();
        expect(logoutSessionMock).not.toHaveBeenCalled();
      } finally {
        unsubscribe();
      }
    },
  );

  it.each(['replacement', 'same-user', 'logout'] as const)(
    'preserves %s during the internal staging clear notification (WI010-F1-R1)',
    async (change) => {
      const exchange = deferred<SocialTokens>();
      prepareCallbackStorage();
      useAuthStore.getState().login('initial-access', profile, 'initial-refresh');
      socialLoginMock.mockReturnValue(exchange.promise);
      fetchMeMock.mockResolvedValue(profile);
      let armed = true;
      let expectedState = useAuthStore.getState();
      let expectedStorage = { ...localStorage };
      const unsubscribe = useAuthStore.subscribe((state) => {
        if (!armed || state.accessToken !== null || state.user !== null) return;
        armed = false;
        if (change === 'logout') useAuthStore.getState().clearSession();
        else {
          useAuthStore
            .getState()
            .login(
              'replacement-access',
              change === 'same-user' ? profile : { ...profile, id: 2 },
              'replacement-refresh',
            );
        }
        sessionStorage.setItem('oauth_profile_return', 'replacement-continuation');
        expectedState = useAuthStore.getState();
        expectedStorage = { ...localStorage };
      });
      try {
        renderPage(true);
        await act(async () => {
          exchange.resolve(issuedTokens);
        });

        expect(armed).toBe(false);
        expect(useAuthStore.getState()).toBe(expectedState);
        expect({ ...localStorage }).toEqual(expectedStorage);
        expect(sessionStorage.getItem('oauth_profile_return')).toBe('replacement-continuation');
        expect(socialLoginMock).toHaveBeenCalledTimes(1);
        expect(fetchMeMock).not.toHaveBeenCalled();
        expect(logoutSessionMock).not.toHaveBeenCalled();
        expect(screen.getByRole('heading', { name: 'AT.M' })).toBeInTheDocument();
        expect(screen.queryByText('home')).not.toBeInTheDocument();
        expect(screen.queryByText('profile destination')).not.toBeInTheDocument();
        expect(screen.queryByText('complete profile')).not.toBeInTheDocument();
      } finally {
        unsubscribe();
      }
    },
  );

  it.each([true, false])(
    'completes a deferred StrictMode callback once with profileComplete=%s',
    async (isProfileComplete) => {
      const exchange = deferred<SocialTokens>();
      const currentUser = deferred<MeResponse>();
      createOAuthAttempt('expected-state-1234', 'pkce-verifier', '/profile?tab=edit');
      socialLoginMock.mockReturnValue(exchange.promise);
      fetchMeMock.mockReturnValue(currentUser.promise);
      renderPage(true);
      expect(socialLoginMock).toHaveBeenCalledTimes(1);
      await act(async () => {
        exchange.resolve({ ...issuedTokens, isProfileComplete });
      });
      expect(fetchMeMock).toHaveBeenCalledTimes(1);
      expect(useAuthStore.getState()).toMatchObject({
        accessToken: issuedTokens.accessToken,
        user: null,
      });
      await act(async () => {
        currentUser.resolve(profile);
      });

      expect(
        await screen.findByText(isProfileComplete ? 'profile destination' : 'complete profile'),
      ).toBeInTheDocument();
      expect(socialLoginMock).toHaveBeenCalledTimes(1);
      expect(useAuthStore.getState().user).toEqual(profile);
      expect(logoutSessionMock).not.toHaveBeenCalled();
      expect(sessionStorage.getItem('oauth_attempt:expected-state-1234')).toBeNull();
      if (isProfileComplete) expect(sessionStorage.getItem('oauth_profile_return')).toBeNull();
      else {
        const continuation = JSON.parse(sessionStorage.getItem('oauth_profile_return') ?? 'null');
        expect(continuation).toMatchObject({
          attemptId: 'expected-state-1234',
          userId: profile.id,
          returnTarget: '/profile?tab=edit',
        });
      }
    },
  );

  it.each(['stage', 'commit'] as const)(
    'keeps the existing error guidance when owned %s persistence fails',
    async (phase) => {
      prepareCallbackStorage();
      socialLoginMock.mockResolvedValue(issuedTokens);
      fetchMeMock.mockResolvedValue(profile);
      const setItem = safeStorage.setItem;
      vi.spyOn(safeStorage, 'setItem').mockImplementation((key, value) => {
        if (key === (phase === 'stage' ? 'accessToken' : 'user')) return false;
        return setItem(key, value);
      });
      renderPage();

      expect(
        await screen.findByText('네트워크 연결을 확인하고 소셜 로그인을 다시 시도해주세요.'),
      ).toBeInTheDocument();
      expect(useAuthStore.getState().accessToken).toBeNull();
      expect(logoutSessionMock).not.toHaveBeenCalled();
      expect(fetchMeMock).toHaveBeenCalledTimes(phase === 'stage' ? 0 : 1);
    },
  );
});

type SocialTokens = LoginResponse & { isProfileComplete: boolean };

const issuedTokens: SocialTokens = {
  accessToken: 'issued-access-token',
  refreshToken: 'issued-refresh-token',
  tokenType: 'Bearer',
  expiresIn: 3600,
  isProfileComplete: false,
};

function deferred<T>() {
  let resolve!: (value: T) => void;
  let reject!: (reason: unknown) => void;
  const promise = new Promise<T>((resolvePromise, rejectPromise) => {
    resolve = resolvePromise;
    reject = rejectPromise;
  });
  return { promise, resolve, reject };
}
