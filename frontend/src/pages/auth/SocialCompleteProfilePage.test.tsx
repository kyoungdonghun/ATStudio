import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import SocialCompleteProfilePage from '@/pages/auth/SocialCompleteProfilePage';
import type { CheckAvailabilityResponse, MeResponse } from '@/api/auth';

const authState = {
  login: vi.fn(),
  accessToken: 'access-token',
};

const fetchMeMock = vi.fn();
const checkNicknameAvailabilityMock = vi.fn();
const checkPhoneAvailabilityMock = vi.fn();
const clientPutMock = vi.fn();

vi.mock('@/store/authStore', () => ({
  useAuthStore: (selector: (state: typeof authState) => unknown) => selector(authState),
}));

vi.mock('@/api/auth', () => ({
  fetchMe: (...args: unknown[]) => fetchMeMock(...args),
  checkNicknameAvailability: (...args: unknown[]) => checkNicknameAvailabilityMock(...args),
  checkPhoneAvailability: (...args: unknown[]) => checkPhoneAvailabilityMock(...args),
}));

vi.mock('@/api/client', () => ({
  default: {
    put: (...args: unknown[]) => clientPutMock(...args),
  },
}));

function buildProfile(overrides: Partial<MeResponse> = {}): MeResponse {
  return {
    id: 1,
    nickname: 'creator01',
    email: 'creator@example.com',
    phonePersonal: '010-1234-5678',
    phoneCompany: null,
    job: 'EDITOR',
    companyName: null,
    userType: 'INDIVIDUAL',
    role: 'USER',
    isVerified: true,
    createdAt: '2026-04-18T00:00:00',
    ...overrides,
  };
}

function renderPage() {
  return render(
    <MemoryRouter>
      <SocialCompleteProfilePage />
    </MemoryRouter>,
  );
}

describe('SocialCompleteProfilePage', () => {
  beforeEach(() => {
    authState.login.mockReset();
    fetchMeMock.mockReset();
    checkNicknameAvailabilityMock.mockReset();
    checkPhoneAvailabilityMock.mockReset();
    clientPutMock.mockReset();

    checkNicknameAvailabilityMock.mockResolvedValue({ available: true } satisfies CheckAvailabilityResponse);
    checkPhoneAvailabilityMock.mockResolvedValue({ available: true } satisfies CheckAvailabilityResponse);
  });

  it('submits the individual complete-profile payload with job', async () => {
    fetchMeMock.mockResolvedValue(buildProfile({ nickname: 'creator02', job: 'ARTIST' }));
    clientPutMock.mockResolvedValue({ data: {} });

    renderPage();

    fireEvent.change(screen.getByLabelText('닉네임'), { target: { value: 'creator02' } });
    fireEvent.change(screen.getByLabelText('연락처'), { target: { value: '01012345678' } });
    fireEvent.change(screen.getByLabelText('직업'), { target: { value: 'ARTIST' } });
    fireEvent.click(screen.getByRole('button', { name: '완료' }));

    await waitFor(() => {
      expect(checkNicknameAvailabilityMock).toHaveBeenCalledWith('creator02');
      expect(checkPhoneAvailabilityMock).toHaveBeenCalledWith('010-1234-5678');
      expect(clientPutMock).toHaveBeenCalledWith('/users/me/complete-profile', {
        nickname: 'creator02',
        phonePersonal: '010-1234-5678',
        phoneCompany: null,
        job: 'ARTIST',
        companyName: null,
        userType: 'INDIVIDUAL',
      });
    });

    expect(authState.login).toHaveBeenCalledWith('access-token', expect.objectContaining({
      nickname: 'creator02',
      job: 'ARTIST',
    }));
  });

  it('submits the business complete-profile payload with companyName and no job', async () => {
    fetchMeMock.mockResolvedValue(buildProfile({
      nickname: 'bizcreator',
      phoneCompany: '02-1234-5678',
      job: null,
      companyName: 'ATStudio Biz',
      userType: 'BUSINESS',
    }));
    clientPutMock.mockResolvedValue({ data: {} });

    renderPage();

    fireEvent.click(screen.getByRole('button', { name: '기업' }));
    expect(screen.queryByLabelText('직업')).not.toBeInTheDocument();

    fireEvent.change(screen.getByLabelText('닉네임'), { target: { value: 'bizcreator' } });
    fireEvent.change(screen.getByLabelText('연락처'), { target: { value: '01012345678' } });
    fireEvent.change(screen.getByLabelText('회사 연락처 (선택)'), { target: { value: '0212345678' } });
    fireEvent.change(screen.getByLabelText('회사명'), { target: { value: 'ATStudio Biz' } });
    fireEvent.click(screen.getByRole('button', { name: '완료' }));

    await waitFor(() => {
      expect(clientPutMock).toHaveBeenCalledWith('/users/me/complete-profile', {
        nickname: 'bizcreator',
        phonePersonal: '010-1234-5678',
        phoneCompany: '02-1234-5678',
        job: null,
        companyName: 'ATStudio Biz',
        userType: 'BUSINESS',
      });
    });

    expect(authState.login).toHaveBeenCalledWith('access-token', expect.objectContaining({
      companyName: 'ATStudio Biz',
      userType: 'BUSINESS',
    }));
  });

  it('blocks business submit when companyName is blank', async () => {
    renderPage();

    fireEvent.click(screen.getByRole('button', { name: '기업' }));
    fireEvent.change(screen.getByLabelText('닉네임'), { target: { value: 'bizcreator' } });
    fireEvent.change(screen.getByLabelText('연락처'), { target: { value: '01012345678' } });
    fireEvent.click(screen.getByRole('button', { name: '완료' }));

    expect(await screen.findByText('기업 회원은 회사명을 입력해주세요.')).toBeInTheDocument();
    expect(clientPutMock).not.toHaveBeenCalled();
  });
});
