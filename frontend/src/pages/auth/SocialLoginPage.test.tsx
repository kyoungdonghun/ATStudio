import { StrictMode } from 'react';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { MeResponse } from '@/api/auth';
import SocialLoginPage from '@/pages/auth/SocialLoginPage';
import { useAlbumLikeStore } from '@/store/albumLikeStore';
import { useAuthStore } from '@/store/authStore';
import { useLikeStore } from '@/store/likeStore';
import { usePlayerStore } from '@/store/playerStore';
import type { PlayableTrack } from '@/types';
import { createOAuthAttempt } from '@/utils/oauthAttempt';

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
});
