import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, useLocation, useNavigate } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import ProfilePage from '@/pages/subscriber/ProfilePage';
import type { MeResponse, CheckAvailabilityResponse } from '@/api/auth';
import { useAuthStore } from '@/store/authStore';

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
  isNoActiveSubscriptionError: (error: unknown) => {
    const response = (error as { response?: { status?: number; data?: { errorCode?: string } } })
      ?.response;
    return response?.status === 403 && response.data?.errorCode === 'NO_ACTIVE_SUBSCRIPTION';
  },
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

function RouterProbe() {
  const location = useLocation();
  const navigate = useNavigate();

  return (
    <>
      <div data-testid="location">{`${location.pathname}${location.search}`}</div>
      <button type="button" onClick={() => navigate(-1)}>
        history back
      </button>
      <button type="button" onClick={() => navigate(1)}>
        history forward
      </button>
    </>
  );
}

function renderPage({
  initialEntries = ['/profile?tab=edit'],
  initialIndex,
}: {
  initialEntries?: string[];
  initialIndex?: number;
} = {}) {
  return render(
    <MemoryRouter initialEntries={initialEntries} initialIndex={initialIndex}>
      <RouterProbe />
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
    localStorage.clear();
    const initialUser = buildProfile();
    localStorage.setItem('accessToken', 'access-token');
    localStorage.setItem('user', JSON.stringify(initialUser));
    useAuthStore.setState({
      accessToken: 'access-token',
      user: initialUser,
      role: initialUser.role,
    });

    checkNicknameAvailabilityMock.mockResolvedValue({
      available: true,
    } satisfies CheckAvailabilityResponse);
    checkPhoneAvailabilityMock.mockResolvedValue({
      available: true,
    } satisfies CheckAvailabilityResponse);
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

    const nicknameInput = await screen.findByLabelText('닉네임');
    await waitFor(() => {
      expect(nicknameInput).toHaveValue('creator01');
      expect(screen.getByLabelText('직업')).toHaveValue('EDITOR');
      expect(screen.getByLabelText('연락처')).toHaveValue('010-1111-2222');
    });

    fireEvent.change(nicknameInput, { target: { value: 'creator02' } });
    fireEvent.change(screen.getByLabelText('직업'), { target: { value: 'ARTIST' } });
    const saveButton = screen.getByRole('button', { name: '저장' });
    await waitFor(() => expect(saveButton).toBeEnabled());
    fireEvent.click(saveButton);

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
    expect(useAuthStore.getState().user?.nickname).toBe('creator02');
    expect(JSON.parse(localStorage.getItem('user') ?? 'null').nickname).toBe('creator02');
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
    checkPhoneAvailabilityMock.mockResolvedValue({
      available: false,
    } satisfies CheckAvailabilityResponse);

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

  it('keeps profile state unchanged and hides arbitrary backend detail when save fails', async () => {
    const initialProfile = buildProfile();
    fetchMeMock.mockResolvedValue(initialProfile);
    clientPutMock.mockRejectedValue({
      response: { status: 500, data: { message: 'private server detail' } },
    });

    renderPage();

    const nicknameInput = await screen.findByLabelText('닉네임');
    fireEvent.change(nicknameInput, { target: { value: 'creator02' } });
    fireEvent.click(screen.getByRole('button', { name: '저장' }));

    expect(
      await screen.findByText(
        '프로필 저장 중 서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.',
      ),
    ).toBeInTheDocument();
    expect(screen.queryByText('private server detail')).not.toBeInTheDocument();
    expect(useAuthStore.getState().user).toEqual(initialProfile);
    expect(JSON.parse(localStorage.getItem('user') ?? 'null')).toEqual(initialProfile);
    expect(nicknameInput).toHaveValue('creator02');
  });

  it('normalizes an unsupported tab to account without rendering a blank panel', async () => {
    fetchMeMock.mockResolvedValue(buildProfile());

    renderPage({ initialEntries: ['/profile?tab=unsupported'] });

    expect(await screen.findByText('creator@example.com')).toBeInTheDocument();
    await waitFor(() => {
      expect(screen.getByTestId('location')).toHaveTextContent('/profile?tab=account');
    });
  });

  it.each([
    ['likes', '/likes'],
    ['downloads', '/downloads'],
    ['playlists', '/playlists'],
    ['history', '/play-history'],
    ['licenses', '/licenses'],
  ])('redirects the legacy %s query tab to its activity route', async (tab, route) => {
    fetchMeMock.mockResolvedValue(buildProfile());

    renderPage({ initialEntries: [`/profile?tab=${tab}`] });

    expect(await screen.findByText('creator@example.com')).toBeInTheDocument();
    await waitFor(() => {
      expect(screen.getByTestId('location')).toHaveTextContent(route);
    });
  });

  it('keeps activity query redirects canonical through browser back and forward', async () => {
    fetchMeMock.mockResolvedValue(buildProfile());

    renderPage({
      initialEntries: ['/profile?tab=account', '/profile?tab=likes'],
      initialIndex: 1,
    });

    await waitFor(() => {
      expect(screen.getByTestId('location')).toHaveTextContent('/likes');
    });
    expect(await screen.findByText('creator@example.com')).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: 'history back' }));
    await waitFor(() => {
      expect(screen.getByTestId('location')).toHaveTextContent('/profile?tab=account');
    });

    fireEvent.click(screen.getByRole('button', { name: 'history forward' }));
    await waitFor(() => {
      expect(screen.getByTestId('location')).toHaveTextContent('/likes');
    });
    expect(screen.getByText('creator@example.com')).toBeInTheDocument();
  });

  it('keeps valid content through browser back and forward after invalid-tab normalization', async () => {
    fetchMeMock.mockResolvedValue(buildProfile());

    renderPage({
      initialEntries: ['/profile?tab=subscription', '/profile?tab=unsupported'],
      initialIndex: 1,
    });

    expect(await screen.findByText('creator@example.com')).toBeInTheDocument();
    await waitFor(() => {
      expect(screen.getByTestId('location')).toHaveTextContent('/profile?tab=account');
    });

    fireEvent.click(screen.getByRole('button', { name: 'history back' }));
    await waitFor(() => {
      expect(screen.getByTestId('location')).toHaveTextContent('/profile?tab=subscription');
    });
    expect(screen.getByText('구독 정보')).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: 'history forward' }));
    await waitFor(() => {
      expect(screen.getByTestId('location')).toHaveTextContent('/profile?tab=account');
    });
    expect(screen.getByText('creator@example.com')).toBeInTheDocument();
  });

  it('separates retryable subscription failure from authoritative absence', async () => {
    fetchMeMock.mockResolvedValue(buildProfile());
    fetchMySubscriptionMock.mockRejectedValueOnce(new Error('offline')).mockResolvedValueOnce({
      id: 7,
      subscription: { name: 'STANDARD' },
      billingCycle: 'MONTHLY',
      status: 'ACTIVE',
      startedAt: '2026-08-01T00:00:00',
      expiresAt: '2026-09-01T00:00:00',
      pendingSubscriptionId: null,
      pendingBillingCycle: null,
    });

    renderPage({ initialEntries: ['/profile?tab=subscription'] });

    expect(await screen.findByText(/구독 정보를 불러오지 못했습니다/)).toBeInTheDocument();
    expect(screen.queryByText('현재 구독 중인 플랜이 없습니다.')).not.toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '다시 시도' }));

    expect(await screen.findByText('스탠다드')).toBeInTheDocument();
    expect(fetchMySubscriptionMock).toHaveBeenCalledTimes(2);
  });

  it('renders no subscription only for the authoritative no-active response', async () => {
    fetchMeMock.mockResolvedValue(buildProfile());
    fetchMySubscriptionMock.mockRejectedValue({
      response: { status: 403, data: { errorCode: 'NO_ACTIVE_SUBSCRIPTION' } },
    });

    renderPage({ initialEntries: ['/profile?tab=subscription'] });

    expect(await screen.findByText('현재 구독 중인 플랜이 없습니다.')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '다시 시도' })).not.toBeInTheDocument();
  });

  it('maps password-update rejection to bounded guidance', async () => {
    fetchMeMock.mockResolvedValue(buildProfile());
    clientPutMock.mockRejectedValue({
      response: {
        status: 401,
        data: { errorCode: 'INVALID_CREDENTIALS', message: 'private credential detail' },
      },
    });

    const { container } = renderPage({ initialEntries: ['/profile?tab=password'] });
    await screen.findByText('현재 비밀번호');
    const passwordInputs = container.querySelectorAll<HTMLInputElement>('input[type="password"]');
    fireEvent.change(passwordInputs[0], { target: { value: 'current-password' } });
    fireEvent.change(passwordInputs[1], { target: { value: 'new-password' } });
    fireEvent.change(passwordInputs[2], { target: { value: 'new-password' } });
    const passwordButtons = screen.getAllByRole('button', { name: '비밀번호 변경' });
    fireEvent.click(passwordButtons[passwordButtons.length - 1]);

    expect(await screen.findByText('현재 비밀번호가 올바르지 않습니다.')).toBeInTheDocument();
    expect(screen.queryByText('private credential detail')).not.toBeInTheDocument();
  });
});
