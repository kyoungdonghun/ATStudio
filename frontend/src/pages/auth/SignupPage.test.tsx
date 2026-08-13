import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import SignupPage from '@/pages/auth/SignupPage';
import {
  checkEmailAvailability,
  checkNicknameAvailability,
  checkPhoneAvailability,
  register,
  type PublicCapabilitiesResponse,
} from '@/api/auth';

const authState = {
  isAuthenticated: () => false,
};

const usePublicCapabilitiesMock = vi.fn();

vi.mock('@/store/authStore', () => ({
  useAuthStore: (selector: (state: typeof authState) => unknown) => selector(authState),
}));

vi.mock('@/api/auth', () => ({
  register: vi.fn(),
  checkEmailAvailability: vi.fn(),
  checkNicknameAvailability: vi.fn(),
  checkPhoneAvailability: vi.fn(),
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
    testUsersEnabled: false,
    ...overrides,
  };
}

function renderPage() {
  return render(
    <MemoryRouter>
      <SignupPage />
    </MemoryRouter>,
  );
}

describe('SignupPage', () => {
  beforeEach(() => {
    authState.isAuthenticated = () => false;
    usePublicCapabilitiesMock.mockReset();
    usePublicCapabilitiesMock.mockReturnValue({
      capabilities: buildCapabilities(),
      loading: false,
      error: '',
    });
    vi.mocked(checkEmailAvailability).mockResolvedValue({ available: true });
    vi.mocked(checkNicknameAvailability).mockResolvedValue({ available: true });
    vi.mocked(checkPhoneAvailability).mockResolvedValue({ available: true });
    vi.mocked(register).mockResolvedValue({
      id: 1,
      nickname: 'tester',
      email: 'tester@example.com',
      job: 'EDITOR',
      userType: 'INDIVIDUAL',
      isVerified: false,
      createdAt: '2026-04-18T00:00:00',
    });
  });

  it('disables signup when email verification mail is unavailable', () => {
    usePublicCapabilitiesMock.mockReturnValue({
      capabilities: buildCapabilities({
        emailVerification: {
          enabled: false,
          deliveryMode: 'UNCONFIGURED',
        },
      }),
      loading: false,
      error: '',
    });

    renderPage();

    expect(screen.getByText('AT.M에 가입하고 음악을 시작하세요')).toBeInTheDocument();
    expect(
      screen.getByText(
        '현재 이 환경에서는 이메일 인증 메일이 비활성화되어 있습니다. 가입 전에 운영자에게 메일 설정을 요청해주세요.',
      ),
    ).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '가입하기' })).toBeDisabled();
    expect(
      screen.queryByText('* 외부 테스트 환경에서는 이메일 인증이 자동으로 건너뛰어집니다.'),
    ).not.toBeInTheDocument();
  });

  it('renders no signup form when capability discovery fails and supports explicit retry', () => {
    const retry = vi.fn();
    usePublicCapabilitiesMock.mockReturnValue({
      capabilities: null,
      loading: false,
      error: '로그인 환경 설정을 불러오지 못했습니다.',
      retry,
      status: 'error',
    });

    renderPage();

    expect(screen.queryByLabelText('이메일')).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '가입하기' })).not.toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '다시 시도' }));
    expect(retry).toHaveBeenCalledTimes(1);
  });

  it('shows local mail guidance instead of the old auto-skip claim', () => {
    usePublicCapabilitiesMock.mockReturnValue({
      capabilities: buildCapabilities({
        emailVerification: {
          enabled: true,
          deliveryMode: 'LOCAL_SMTP',
        },
      }),
      loading: false,
      error: '',
    });

    renderPage();

    expect(
      screen.getByText(
        '현재 이 환경에서는 로컬 메일 수신 환경(MailHog 등)에서만 인증 링크를 확인할 수 있습니다.',
      ),
    ).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '가입하기' })).toBeEnabled();
    expect(
      screen.queryByText('* 외부 테스트 환경에서는 이메일 인증이 자동으로 건너뛰어집니다.'),
    ).not.toBeInTheDocument();
  });
  it('disables signup when the server turns off password login entirely', () => {
    usePublicCapabilitiesMock.mockReturnValue({
      capabilities: buildCapabilities({
        passwordLoginEnabled: false,
        emailVerification: {
          enabled: false,
          deliveryMode: 'REMOTE_SMTP',
        },
      }),
      loading: false,
      error: '',
    });

    renderPage();

    expect(
      screen.getByText(
        '현재 이 환경에서는 이메일 로그인과 회원가입이 비활성화되어 있습니다. 운영자에게 소셜 로그인 또는 테스트 계정을 확인해주세요.',
      ),
    ).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '가입하기' })).toBeDisabled();
  });

  it('blocks submit when the phone number is already registered', async () => {
    vi.mocked(checkPhoneAvailability).mockResolvedValue({ available: false });

    renderPage();

    fireEvent.change(screen.getByLabelText('닉네임'), { target: { value: 'tester_01' } });
    fireEvent.change(screen.getByLabelText('이메일'), { target: { value: 'tester@example.com' } });
    fireEvent.change(screen.getByLabelText('비밀번호'), { target: { value: 'password123' } });
    fireEvent.change(screen.getByLabelText('비밀번호 확인'), {
      target: { value: 'password123' },
    });
    fireEvent.change(screen.getByLabelText('연락처'), {
      target: { value: '010-1234-5678' },
    });
    fireEvent.change(screen.getByLabelText('직업'), { target: { value: 'EDITOR' } });
    fireEvent.click(screen.getByRole('button', { name: '가입하기' }));

    await waitFor(() => {
      expect(checkPhoneAvailability).toHaveBeenCalledWith('010-1234-5678');
    });
    expect(screen.getByText('이미 등록된 전화번호입니다.')).toBeInTheDocument();
    expect(register).not.toHaveBeenCalled();
  });
});
