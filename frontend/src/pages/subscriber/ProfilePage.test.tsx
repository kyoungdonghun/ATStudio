import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import ProfilePage from '@/pages/subscriber/ProfilePage';
import type { MeResponse, CheckAvailabilityResponse } from '@/api/auth';

const fetchMeMock = vi.fn();
const checkNicknameAvailabilityMock = vi.fn();
const checkPhoneAvailabilityMock = vi.fn();
const fetchMySubscriptionMock = vi.fn();
const clientPutMock = vi.fn();

vi.mock('@/api/auth', () => ({
  fetchMe: (...args: unknown[]) => fetchMeMock(...args),
  checkNicknameAvailability: (...args: unknown[]) => checkNicknameAvailabilityMock(...args),
  checkPhoneAvailability: (...args: unknown[]) => checkPhoneAvailabilityMock(...args),
}));

vi.mock('@/api/userSubscriptions', () => ({
  fetchMySubscription: (...args: unknown[]) => fetchMySubscriptionMock(...args),
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
    phonePersonal: '010-1111-2222',
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
    <MemoryRouter initialEntries={['/profile?tab=edit']}>
      <ProfilePage />
    </MemoryRouter>,
  );
}

describe('ProfilePage', () => {
  beforeEach(() => {
    fetchMeMock.mockReset();
    checkNicknameAvailabilityMock.mockReset();
    checkPhoneAvailabilityMock.mockReset();
    fetchMySubscriptionMock.mockReset();
    clientPutMock.mockReset();

    checkNicknameAvailabilityMock.mockResolvedValue({ available: true } satisfies CheckAvailabilityResponse);
    checkPhoneAvailabilityMock.mockResolvedValue({ available: true } satisfies CheckAvailabilityResponse);
    fetchMySubscriptionMock.mockRejectedValue(new Error('no subscription'));
  });

  it('submits the individual profile payload with nickname and job updates', async () => {
    fetchMeMock.mockResolvedValue(buildProfile());
    clientPutMock.mockResolvedValue({
      data: {
        data: buildProfile({ nickname: 'creator02', job: 'ARTIST' }),
      },
    });

    renderPage();

    expect(await screen.findByLabelText('닉네임')).toBeInTheDocument();

    fireEvent.change(screen.getByLabelText('닉네임'), { target: { value: 'creator02' } });
    fireEvent.change(screen.getByLabelText('직업'), { target: { value: 'ARTIST' } });
    fireEvent.click(screen.getByRole('button', { name: '저장' }));

    await waitFor(() => {
      expect(checkNicknameAvailabilityMock).toHaveBeenCalledWith('creator02');
      expect(clientPutMock).toHaveBeenCalledWith('/users/me', {
        nickname: 'creator02',
        phonePersonal: '010-1111-2222',
        phoneCompany: null,
        job: 'ARTIST',
        companyName: null,
      });
    });

    expect(screen.getByText('프로필이 저장되었습니다.')).toBeInTheDocument();
    expect(checkPhoneAvailabilityMock).not.toHaveBeenCalled();
  });

  it('preserves company fields for business members when saving a nickname change', async () => {
    fetchMeMock.mockResolvedValue(
      buildProfile({
        nickname: 'bizcreator',
        phonePersonal: '010-1234-5678',
        phoneCompany: '02-9876-5432',
        job: null,
        companyName: 'ATStudio Biz',
        userType: 'BUSINESS',
      }),
    );
    clientPutMock.mockResolvedValue({
      data: {
        data: buildProfile({
          nickname: 'bizcreator2',
          phonePersonal: '010-1234-5678',
          phoneCompany: '02-9876-5432',
          job: null,
          companyName: 'ATStudio Biz',
          userType: 'BUSINESS',
        }),
      },
    });

    renderPage();

    expect(await screen.findByLabelText('회사명')).toBeInTheDocument();
    expect(screen.queryByLabelText('직업')).not.toBeInTheDocument();

    fireEvent.change(screen.getByLabelText('닉네임'), { target: { value: 'bizcreator2' } });
    fireEvent.click(screen.getByRole('button', { name: '저장' }));

    await waitFor(() => {
      expect(clientPutMock).toHaveBeenCalledWith('/users/me', {
        nickname: 'bizcreator2',
        phonePersonal: '010-1234-5678',
        phoneCompany: '02-9876-5432',
        job: null,
        companyName: 'ATStudio Biz',
      });
    });
  });

  it('blocks business profile save when company name is blank', async () => {
    fetchMeMock.mockResolvedValue(
      buildProfile({
        nickname: 'bizcreator',
        phonePersonal: '010-1234-5678',
        phoneCompany: '02-9876-5432',
        job: null,
        companyName: 'ATStudio Biz',
        userType: 'BUSINESS',
      }),
    );

    renderPage();

    const companyNameInput = await screen.findByLabelText('회사명');
    fireEvent.change(companyNameInput, { target: { value: '' } });
    fireEvent.click(screen.getByRole('button', { name: '저장' }));

    expect(await screen.findByText('기업 회원은 회사명을 입력해주세요.')).toBeInTheDocument();
    expect(clientPutMock).not.toHaveBeenCalled();
  });

  it('shows a phone-duplicate error before submit when the personal phone changes', async () => {
    fetchMeMock.mockResolvedValue(buildProfile());
    checkPhoneAvailabilityMock.mockResolvedValue({ available: false } satisfies CheckAvailabilityResponse);

    renderPage();

    expect(await screen.findByLabelText('연락처')).toBeInTheDocument();
    fireEvent.change(screen.getByLabelText('닉네임'), { target: { value: 'creator02' } });
    fireEvent.change(screen.getByLabelText('연락처'), { target: { value: '01099998888' } });
    fireEvent.click(screen.getByRole('button', { name: '저장' }));

    await waitFor(() => {
      expect(checkPhoneAvailabilityMock).toHaveBeenCalledWith('010-9999-8888');
    });
    expect(screen.getByText('이미 등록된 전화번호입니다.')).toBeInTheDocument();
    expect(clientPutMock).not.toHaveBeenCalled();
  });
});
