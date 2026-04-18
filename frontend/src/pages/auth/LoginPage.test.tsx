import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import LoginPage from '@/pages/auth/LoginPage';
import type { PublicCapabilitiesResponse } from '@/api/auth';

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

function renderPage() {
  return render(
    <MemoryRouter>
      <LoginPage />
    </MemoryRouter>,
  );
}

describe('LoginPage', () => {
  beforeEach(() => {
    authState.login.mockReset();
    authState.isAuthenticated = () => false;
    usePublicCapabilitiesMock.mockReset();
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

    expect(screen.getByRole('button', { name: 'Google 로그인' })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Kakao 로그인' })).not.toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Naver 로그인' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: '비밀번호 찾기' })).toBeInTheDocument();
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

    expect(screen.getByText('이 환경에서는 소셜 로그인이 비활성화되어 있습니다.')).toBeInTheDocument();
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
});
