import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import EmailVerifyPage from '@/pages/auth/EmailVerifyPage';

const verifyEmailMock = vi.fn();

vi.mock('@/api/auth', () => ({
  verifyEmail: (...args: unknown[]) => verifyEmailMock(...args),
}));

describe('EmailVerifyPage', () => {
  beforeEach(() => {
    verifyEmailMock.mockReset();
  });

  it('hides an unsafe backend message behind the fixed invalid-token mapping', async () => {
    verifyEmailMock.mockRejectedValue({
      response: {
        status: 400,
        data: {
          errorCode: 'INVALID_TOKEN',
          message: 'private provider token diagnostics',
        },
      },
    });

    render(
      <MemoryRouter
        initialEntries={['/verify-email?token=unsafe-token']}
        future={{ v7_startTransition: true, v7_relativeSplatPath: true }}
      >
        <EmailVerifyPage />
      </MemoryRouter>,
    );

    expect(await screen.findByText('유효하지 않거나 만료된 인증 링크입니다.')).toBeInTheDocument();
    expect(screen.queryByText('private provider token diagnostics')).not.toBeInTheDocument();
  });
});
