import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import PasswordResetPage from '@/pages/auth/PasswordResetPage';
import { requestPasswordReset, type PublicCapabilitiesResponse } from '@/api/auth';

const usePublicCapabilitiesMock = vi.fn();

vi.mock('@/api/auth', () => ({
  requestPasswordReset: vi.fn(),
  resetPassword: vi.fn(),
}));

vi.mock('@/hooks/usePublicCapabilities', () => ({
  usePublicCapabilities: () => usePublicCapabilitiesMock(),
}));

const requestPasswordResetMock = vi.mocked(requestPasswordReset);

function buildCapabilities(): PublicCapabilitiesResponse {
  return {
    passwordLoginEnabled: true,
    emailVerification: { enabled: true, deliveryMode: 'REMOTE_SMTP' },
    passwordReset: { enabled: true, deliveryMode: 'REMOTE_SMTP' },
    socialLogin: {
      google: { enabled: false, clientId: null, redirectUri: null },
      kakao: { enabled: false, clientId: null, redirectUri: null },
      naver: { enabled: false, clientId: null, redirectUri: null },
    },
    testUsersEnabled: false,
  };
}

function renderPage(initialEntry = '/password-reset') {
  return render(
    <MemoryRouter
      initialEntries={[initialEntry]}
      future={{ v7_startTransition: true, v7_relativeSplatPath: true }}
    >
      <PasswordResetPage />
    </MemoryRouter>,
  );
}

describe('PasswordResetPage', () => {
  beforeEach(() => {
    usePublicCapabilitiesMock.mockReset();
    requestPasswordResetMock.mockReset();
    usePublicCapabilitiesMock.mockReturnValue({
      capabilities: buildCapabilities(),
      loading: false,
      error: '',
      retry: vi.fn(),
      status: 'ready',
    });
  });

  it('shows a bounded unavailable state and one explicit retry on capability failure', () => {
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
    fireEvent.click(screen.getByRole('button', { name: '다시 시도' }));
    expect(retry).toHaveBeenCalledTimes(1);
    expect(requestPasswordResetMock).not.toHaveBeenCalled();
  });

  it('shows fixed rate-limit guidance without exposing arbitrary backend text', async () => {
    requestPasswordResetMock.mockRejectedValue({
      response: {
        status: 429,
        data: {
          errorCode: 'RATE_LIMIT_EXCEEDED',
          message: 'private backend detail',
        },
      },
    });

    renderPage();
    fireEvent.change(screen.getByLabelText('이메일'), {
      target: { value: 'unknown@example.com' },
    });
    fireEvent.click(screen.getByRole('button', { name: '재설정 링크 발송' }));

    expect(
      await screen.findByText('짧은 시간에 요청이 너무 많았습니다. 잠시 후 다시 시도해주세요.'),
    ).toBeInTheDocument();
    expect(screen.queryByText('private backend detail')).not.toBeInTheDocument();
  });

  it('keeps accepted forgot-password responses enumeration-safe', async () => {
    requestPasswordResetMock.mockResolvedValue();

    renderPage();
    fireEvent.change(screen.getByLabelText('이메일'), {
      target: { value: 'unknown@example.com' },
    });
    fireEvent.click(screen.getByRole('button', { name: '재설정 링크 발송' }));

    await waitFor(() => expect(requestPasswordResetMock).toHaveBeenCalledTimes(1));
    expect(screen.getByText('비밀번호 재설정 요청을 접수했습니다.')).toBeInTheDocument();
    expect(screen.queryByText(/가입되지 않은|존재하지 않는/)).not.toBeInTheDocument();
  });
});
