import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes, useLocation } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import LoginPage from '@/pages/auth/LoginPage';
import { fetchMe, login as loginRequest } from '@/api/auth';
import type { MeResponse, PublicCapabilitiesResponse } from '@/api/auth';

const authState = {
  login: vi.fn(),
  isAuthenticated: () => false,
};

const usePublicCapabilitiesMock = vi.fn();

vi.mock('@/store/authStore', () => ({
  useAuthStore: (selector: (state: typeof authState) => unknown) => selector(authState),
}));

vi.mock('@/api/auth', () => ({
  login: vi.fn(),
  fetchMe: vi.fn(),
}));

const loginRequestMock = vi.mocked(loginRequest);
const fetchMeMock = vi.mocked(fetchMe);

vi.mock('@/hooks/usePublicCapabilities', () => ({
  usePublicCapabilities: () => usePublicCapabilitiesMock(),
}));

function buildCapabilities(
  overrides: Partial<PublicCapabilitiesResponse> = {},
): PublicCapabilitiesResponse {
  return {
    passwordLoginEnabled: true,
    emailVerification: {
      enabled: true,
      deliveryMode: 'REMOTE_SMTP',
    },
    passwordReset: {
      enabled: true,
      deliveryMode: 'REMOTE_SMTP',
    },
    socialLogin: {
      google: {
        enabled: true,
        clientId: 'google-client',
        redirectUri: 'https://app.atstudio.com/social-login/google',
      },
      kakao: {
        enabled: false,
        clientId: null,
        redirectUri: null,
      },
      naver: {
        enabled: false,
        clientId: null,
        redirectUri: null,
      },
    },
    testUsersEnabled: false,
    ...overrides,
  };
}

function DestinationProbe() {
  const location = useLocation();
  return <div>Destination: {`${location.pathname}${location.search}`}</div>;
}

function renderPage(initialEntry = '/login') {
  return render(
    <MemoryRouter
      initialEntries={[initialEntry]}
      future={{ v7_startTransition: true, v7_relativeSplatPath: true }}
    >
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/" element={<DestinationProbe />} />
        <Route path="/profile" element={<DestinationProbe />} />
        <Route path="/admin/dashboard" element={<DestinationProbe />} />
        <Route path="/subscriptions/checkout" element={<DestinationProbe />} />
        <Route path="/company-certification/status" element={<DestinationProbe />} />
      </Routes>
    </MemoryRouter>,
  );
}

const me: MeResponse = {
  id: 1,
  nickname: 'tester',
  email: 'tester@example.com',
  phonePersonal: '010-1234-5678',
  phoneCompany: null,
  job: 'EDITOR',
  companyName: null,
  userType: 'INDIVIDUAL',
  role: 'USER',
  isVerified: true,
  createdAt: '2026-07-16T00:00:00Z',
};

async function submitPasswordLogin() {
  fireEvent.change(screen.getByLabelText('이메일'), {
    target: { value: 'tester@example.com' },
  });
  fireEvent.change(screen.getByLabelText('비밀번호'), {
    target: { value: 'password123' },
  });
  fireEvent.click(screen.getByRole('button', { name: '로그인' }));
  await waitFor(() => expect(loginRequestMock).toHaveBeenCalledTimes(1));
}

describe('LoginPage', () => {
  beforeEach(() => {
    authState.login.mockReset();
    authState.isAuthenticated = () => false;
    usePublicCapabilitiesMock.mockReset();
    loginRequestMock.mockReset();
    fetchMeMock.mockReset();
  });

  it('renders only providers enabled by server capabilities', () => {
    usePublicCapabilitiesMock.mockReturnValue({
      capabilities: buildCapabilities({
        socialLogin: {
          google: {
            enabled: true,
            clientId: 'google-client',
            redirectUri: 'https://app.atstudio.com/social-login/google',
          },
          kakao: {
            enabled: false,
            clientId: null,
            redirectUri: null,
          },
          naver: {
            enabled: true,
            clientId: 'naver-client',
            redirectUri: 'https://app.atstudio.com/social-login/naver',
          },
        },
      }),
      loading: false,
      error: '',
    });

    renderPage();

    expect(screen.getByRole('heading', { name: 'AT.M' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Google 로그인' })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Kakao 로그인' })).not.toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Naver 로그인' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: '비밀번호 찾기' })).toBeInTheDocument();
  });

  it('advertises no auth capability when discovery fails and retries once on request', () => {
    const retry = vi.fn();
    usePublicCapabilitiesMock.mockReturnValue({
      capabilities: null,
      loading: false,
      error: '로그인 환경 설정을 불러오지 못했습니다.',
      retry,
      status: 'error',
    });

    renderPage();

    expect(screen.queryByRole('button', { name: '로그인' })).not.toBeInTheDocument();
    expect(screen.queryByRole('link', { name: '회원가입' })).not.toBeInTheDocument();
    expect(screen.queryByRole('link', { name: '비밀번호 찾기' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /Google|Kakao|Naver/ })).not.toBeInTheDocument();
    expect(screen.queryByText(/QA 테스트 계정/)).not.toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: '다시 시도' }));
    expect(retry).toHaveBeenCalledTimes(1);
  });

  it('shows disabled-state guidance when social and password reset flows are unavailable', () => {
    usePublicCapabilitiesMock.mockReturnValue({
      capabilities: buildCapabilities({
        passwordReset: {
          enabled: false,
          deliveryMode: 'UNCONFIGURED',
        },
        socialLogin: {
          google: {
            enabled: false,
            clientId: null,
            redirectUri: null,
          },
          kakao: {
            enabled: false,
            clientId: null,
            redirectUri: null,
          },
          naver: {
            enabled: false,
            clientId: null,
            redirectUri: null,
          },
        },
      }),
      loading: false,
      error: '',
    });

    renderPage();

    expect(
      screen.getByText('이 환경에서는 소셜 로그인이 비활성화되어 있습니다.'),
    ).toBeInTheDocument();
    expect(screen.getByText('비밀번호 찾기 비활성화')).toBeInTheDocument();
  });
  it('disables password login and signup when the server turns off local auth', () => {
    usePublicCapabilitiesMock.mockReturnValue({
      capabilities: buildCapabilities({
        passwordLoginEnabled: false,
        passwordReset: {
          enabled: false,
          deliveryMode: 'UNCONFIGURED',
        },
      }),
      loading: false,
      error: '',
    });

    renderPage();

    expect(
      screen.getByText('현재 이 환경에서는 이메일 로그인과 회원가입이 비활성화되어 있습니다.'),
    ).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '로그인' })).toBeDisabled();
    expect(screen.getByText('회원가입 비활성화')).toBeInTheDocument();
  });

  it('shows QA bootstrap guidance when test users are enabled', () => {
    usePublicCapabilitiesMock.mockReturnValue({
      capabilities: buildCapabilities({
        testUsersEnabled: true,
      }),
      loading: false,
      error: '',
    });

    renderPage();

    expect(
      screen.getByText(
        '이 환경에서는 QA 테스트 계정이 활성화되어 있습니다. 계정은 운영자가 별도로 제공합니다.',
      ),
    ).toBeInTheDocument();
  });

  it('returns once to a safe internal pathname and query after password login', async () => {
    usePublicCapabilitiesMock.mockReturnValue({
      capabilities: buildCapabilities(),
      loading: false,
      error: '',
    });
    loginRequestMock.mockResolvedValue({
      accessToken: 'access-token',
      refreshToken: 'refresh-token',
      tokenType: 'Bearer',
      expiresIn: 900,
    });
    fetchMeMock.mockResolvedValue(me);

    renderPage('/login?returnTo=%2Fprofile%3Ftab%3Dedit');
    await submitPasswordLogin();

    expect(await screen.findByText('Destination: /profile?tab=edit')).toBeInTheDocument();
    expect(authState.login).toHaveBeenCalledWith('access-token', me, 'refresh-token');
  });

  it('stays logged out when durable session persistence fails', async () => {
    usePublicCapabilitiesMock.mockReturnValue({
      capabilities: buildCapabilities(),
      loading: false,
      error: '',
    });
    loginRequestMock.mockResolvedValue({
      accessToken: 'access-token',
      refreshToken: 'refresh-token',
      tokenType: 'Bearer',
      expiresIn: 900,
    });
    fetchMeMock.mockResolvedValue(me);
    authState.login.mockImplementationOnce(() => {
      throw new Error('storage unavailable');
    });

    renderPage('/login?returnTo=%2Fprofile');
    await submitPasswordLogin();

    expect(
      await screen.findByText('로그인에 실패했습니다. 잠시 후 다시 시도해주세요.'),
    ).toBeInTheDocument();
    expect(screen.queryByText('Destination: /profile')).not.toBeInTheDocument();
  });

  it.each([
    'https://evil.example/steal',
    '//evil.example/steal',
    '/social-login/google',
    '/profile%zz',
  ])('falls back to the existing home for unsafe return target %s', async (returnTo) => {
    usePublicCapabilitiesMock.mockReturnValue({
      capabilities: buildCapabilities(),
      loading: false,
      error: '',
    });
    loginRequestMock.mockResolvedValue({
      accessToken: 'access-token',
      refreshToken: 'refresh-token',
      tokenType: 'Bearer',
      expiresIn: 900,
    });
    fetchMeMock.mockResolvedValue(me);

    renderPage(`/login?returnTo=${encodeURIComponent(returnTo)}`);
    await submitPasswordLogin();

    expect(await screen.findByText('Destination: /')).toBeInTheDocument();
  });

  it.each([
    ['ADMIN destination for USER', '/admin/dashboard', me, '/'],
    [
      'USER-only payment destination for ADMIN',
      '/subscriptions/checkout',
      { ...me, role: 'ADMIN' as const },
      '/',
    ],
    ['BUSINESS destination for INDIVIDUAL', '/company-certification/status', me, '/'],
    [
      'ADMIN destination for ADMIN',
      '/admin/dashboard',
      { ...me, role: 'ADMIN' as const },
      '/admin/dashboard',
    ],
    [
      'BUSINESS destination for BUSINESS USER',
      '/company-certification/status',
      { ...me, userType: 'BUSINESS' as const },
      '/company-certification/status',
    ],
  ])('applies post-login access policy for %s', async (_, returnTo, identity, expected) => {
    usePublicCapabilitiesMock.mockReturnValue({
      capabilities: buildCapabilities(),
      loading: false,
      error: '',
    });
    loginRequestMock.mockResolvedValue({
      accessToken: 'access-token',
      refreshToken: 'refresh-token',
      tokenType: 'Bearer',
      expiresIn: 900,
    });
    fetchMeMock.mockResolvedValue(identity);

    renderPage(`/login?returnTo=${encodeURIComponent(returnTo)}`);
    await submitPasswordLogin();

    expect(await screen.findByText(`Destination: ${expected}`)).toBeInTheDocument();
  });
});
