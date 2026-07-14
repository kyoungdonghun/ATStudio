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

function prepareCallbackStorage() {
  sessionStorage.setItem('oauth_state', 'expected-state');
  sessionStorage.setItem('oauth_code_verifier', 'pkce-verifier');
}

function renderPage(strict = false) {
  const routes = (
    <MemoryRouter
      initialEntries={[
        '/social-login/google?code=authorization-code&state=expected-state',
      ]}
    >
      <Routes>
        <Route path="/social-login/:provider" element={<SocialLoginPage />} />
        <Route path="/" element={<p>home</p>} />
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
    expect(socialLoginMock).toHaveBeenCalledWith(
      'google',
      'authorization-code',
      'pkce-verifier',
    );
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

  it('best-effort revokes and always clears a staged session when fetchMe fails', async () => {
    prepareCallbackStorage();
    useLikeStore.setState({ likedIds: new Set([1]), loaded: true });
    useAlbumLikeStore.setState({ likedAlbumIds: new Set([2]), loaded: true });
    usePlayerStore.setState({ isPlaying: true });
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
      await screen.findByText('사용자 정보를 불러오지 못했습니다.'),
    ).toBeInTheDocument();
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
    expect(usePlayerStore.getState().isPlaying).toBe(false);
  });
});
