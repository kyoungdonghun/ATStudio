import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import SocialCompleteProfilePage from '@/pages/auth/SocialCompleteProfilePage';
import type { CheckAvailabilityResponse, MeResponse } from '@/api/auth';
import { storeOAuthProfileReturnTarget } from '@/utils/oauthAttempt';
import { useAuthStore } from '@/store/authStore';

const fetchMeMock = vi.fn();
const checkNicknameAvailabilityMock = vi.fn();
const checkPhoneAvailabilityMock = vi.fn();
const clientPutMock = vi.fn();

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

function buildIncompleteProfile(overrides: Partial<MeResponse> = {}): MeResponse {
  return buildProfile({ phonePersonal: null, job: null, ...overrides });
}

function deferred<T>() {
  let resolve!: (value: T) => void;
  const promise = new Promise<T>((resolvePromise) => {
    resolve = resolvePromise;
  });
  return { promise, resolve };
}

function renderPage() {
  return render(
    <MemoryRouter
      initialEntries={['/complete-profile']}
      future={{ v7_startTransition: true, v7_relativeSplatPath: true }}
    >
      <Routes>
        <Route path="/complete-profile" element={<SocialCompleteProfilePage />} />
        <Route path="/tracks/7" element={<p>stored destination</p>} />
        <Route path="/profile" element={<p>profile destination</p>} />
        <Route path="/" element={<p>home destination</p>} />
      </Routes>
    </MemoryRouter>,
  );
}

describe('SocialCompleteProfilePage', () => {
  beforeEach(() => {
    fetchMeMock.mockReset();
    checkNicknameAvailabilityMock.mockReset();
    checkPhoneAvailabilityMock.mockReset();
    clientPutMock.mockReset();
    localStorage.clear();
    sessionStorage.clear();
    useAuthStore.getState().clearSession();
    useAuthStore.getState().login('access-token', buildIncompleteProfile(), 'refresh-token');

    fetchMeMock.mockResolvedValue(buildIncompleteProfile());

    checkNicknameAvailabilityMock.mockResolvedValue({
      available: true,
    } satisfies CheckAvailabilityResponse);
    checkPhoneAvailabilityMock.mockResolvedValue({
      available: true,
    } satisfies CheckAvailabilityResponse);
  });

  it('submits the individual complete-profile payload with job', async () => {
    fetchMeMock
      .mockResolvedValueOnce(buildIncompleteProfile())
      .mockResolvedValueOnce(buildProfile({ nickname: 'creator02', job: 'ARTIST' }));
    clientPutMock.mockResolvedValue({ data: {} });

    renderPage();

    fireEvent.change(await screen.findByLabelText('닉네임'), { target: { value: 'creator02' } });
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

    await waitFor(() => {
      expect(useAuthStore.getState().user).toEqual(
        expect.objectContaining({
          nickname: 'creator02',
          job: 'ARTIST',
        }),
      );
    });
  });

  it('submits the business complete-profile payload with companyName and no job', async () => {
    fetchMeMock.mockResolvedValueOnce(buildIncompleteProfile()).mockResolvedValueOnce(
      buildProfile({
        nickname: 'bizcreator',
        phoneCompany: '02-1234-5678',
        job: null,
        companyName: 'ATStudio Biz',
        userType: 'BUSINESS',
      }),
    );
    clientPutMock.mockResolvedValue({ data: {} });

    renderPage();

    fireEvent.click(await screen.findByRole('button', { name: '기업' }));
    expect(screen.queryByLabelText('직업')).not.toBeInTheDocument();

    fireEvent.change(screen.getByLabelText('닉네임'), { target: { value: 'bizcreator' } });
    fireEvent.change(screen.getByLabelText('연락처'), { target: { value: '01012345678' } });
    fireEvent.change(screen.getByLabelText('회사 연락처 (선택)'), {
      target: { value: '0212345678' },
    });
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

    await waitFor(() => {
      expect(useAuthStore.getState().user).toEqual(
        expect.objectContaining({
          companyName: 'ATStudio Biz',
          userType: 'BUSINESS',
        }),
      );
    });
  });

  it('blocks business submit when companyName is blank', async () => {
    renderPage();

    fireEvent.click(await screen.findByRole('button', { name: '기업' }));
    fireEvent.change(screen.getByLabelText('닉네임'), { target: { value: 'bizcreator' } });
    fireEvent.change(screen.getByLabelText('연락처'), { target: { value: '01012345678' } });
    fireEvent.click(screen.getByRole('button', { name: '완료' }));

    expect(await screen.findByText('기업 회원은 회사명을 입력해주세요.')).toBeInTheDocument();
    expect(clientPutMock).not.toHaveBeenCalled();
  });

  it('consumes the stored OAuth return target after profile completion', async () => {
    storeOAuthProfileReturnTarget('expected-state-1234', '/tracks/7', 1);
    fetchMeMock
      .mockResolvedValueOnce(buildIncompleteProfile())
      .mockResolvedValueOnce(buildProfile({ nickname: 'creator02', job: 'ARTIST' }));
    clientPutMock.mockResolvedValue({ data: {} });

    renderPage();
    fireEvent.change(await screen.findByLabelText('닉네임'), { target: { value: 'creator02' } });
    fireEvent.change(screen.getByLabelText('연락처'), { target: { value: '01012345678' } });
    fireEvent.change(screen.getByLabelText('직업'), { target: { value: 'ARTIST' } });
    fireEvent.click(screen.getByRole('button', { name: '완료' }));

    expect(await screen.findByText('stored destination')).toBeInTheDocument();
    expect(sessionStorage.getItem('oauth_profile_return')).toBeNull();
  });

  it('deletes but does not use a continuation bound to another account', async () => {
    storeOAuthProfileReturnTarget('expected-state-1234', '/tracks/7', 2);
    fetchMeMock
      .mockResolvedValueOnce(buildIncompleteProfile())
      .mockResolvedValueOnce(buildProfile({ nickname: 'creator02', job: 'ARTIST' }));
    clientPutMock.mockResolvedValue({ data: {} });

    renderPage();
    fireEvent.change(await screen.findByLabelText('닉네임'), { target: { value: 'creator02' } });
    fireEvent.change(screen.getByLabelText('연락처'), { target: { value: '01012345678' } });
    fireEvent.change(screen.getByLabelText('직업'), { target: { value: 'ARTIST' } });
    fireEvent.click(screen.getByRole('button', { name: '완료' }));

    expect(await screen.findByText('home destination')).toBeInTheDocument();
    expect(screen.queryByText('stored destination')).not.toBeInTheDocument();
    expect(sessionStorage.getItem('oauth_profile_return')).toBeNull();
  });

  it('fences the whole submit while delayed availability validation is pending', async () => {
    const nicknameCheck = deferred<CheckAvailabilityResponse>();
    fetchMeMock
      .mockResolvedValueOnce(buildIncompleteProfile())
      .mockResolvedValueOnce(buildProfile({ nickname: 'creator02', job: 'ARTIST' }));
    checkNicknameAvailabilityMock.mockReturnValueOnce(nicknameCheck.promise);
    clientPutMock.mockResolvedValue({ data: {} });

    renderPage();
    fireEvent.change(await screen.findByLabelText('닉네임'), { target: { value: 'creator02' } });
    fireEvent.change(screen.getByLabelText('연락처'), { target: { value: '01012345678' } });
    fireEvent.change(screen.getByLabelText('직업'), { target: { value: 'ARTIST' } });
    const form = screen.getByRole('button', { name: '완료' }).closest('form');

    fireEvent.submit(form!);
    fireEvent.submit(form!);

    expect(checkNicknameAvailabilityMock).toHaveBeenCalledTimes(1);
    expect(screen.getByLabelText('닉네임')).toBeDisabled();
    expect(screen.getByLabelText('연락처')).toBeDisabled();
    expect(screen.getByLabelText('직업')).toBeDisabled();
    expect(screen.getByRole('button', { name: '개인' })).toBeDisabled();
    expect(screen.getByRole('button', { name: '기업' })).toBeDisabled();

    nicknameCheck.resolve({ available: true });
    await waitFor(() => expect(clientPutMock).toHaveBeenCalledTimes(1));
    expect(checkPhoneAvailabilityMock).toHaveBeenCalledTimes(1);
  });

  it('redirects a known complete profile before rendering or mutating the form', async () => {
    fetchMeMock.mockResolvedValue(buildProfile());

    renderPage();

    expect(await screen.findByText('profile destination')).toBeInTheDocument();
    expect(screen.queryByLabelText('닉네임')).not.toBeInTheDocument();
    expect(clientPutMock).not.toHaveBeenCalled();
  });

  it('keeps identity failure non-mutating and retries only on explicit request', async () => {
    fetchMeMock
      .mockRejectedValueOnce(new Error('offline'))
      .mockResolvedValueOnce(buildIncompleteProfile());

    renderPage();

    expect(await screen.findByText(/프로필 상태 정보를 불러오지 못했습니다/)).toBeInTheDocument();
    expect(screen.queryByLabelText('닉네임')).not.toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '다시 시도' }));

    expect(await screen.findByLabelText('닉네임')).toBeInTheDocument();
    expect(fetchMeMock).toHaveBeenCalledTimes(2);
    expect(clientPutMock).not.toHaveBeenCalled();
  });

  it('does not restore or navigate after the session is cleared during a deferred refresh', async () => {
    const refresh = deferred<MeResponse>();
    storeOAuthProfileReturnTarget('expected-state-1234', '/tracks/7', 1);
    fetchMeMock
      .mockResolvedValueOnce(buildIncompleteProfile())
      .mockReturnValueOnce(refresh.promise);
    clientPutMock.mockResolvedValue({ data: {} });

    const view = renderPage();
    fireEvent.change(await screen.findByLabelText('닉네임'), { target: { value: 'creator02' } });
    fireEvent.change(screen.getByLabelText('연락처'), { target: { value: '01012345678' } });
    fireEvent.change(screen.getByLabelText('직업'), { target: { value: 'ARTIST' } });
    fireEvent.click(screen.getByRole('button', { name: '완료' }));

    await waitFor(() => expect(fetchMeMock).toHaveBeenCalledTimes(2));
    useAuthStore.getState().clearSession();
    view.unmount();

    await act(async () => {
      refresh.resolve(buildProfile({ nickname: 'creator02', job: 'ARTIST' }));
      await refresh.promise;
    });

    expect(useAuthStore.getState()).toEqual(
      expect.objectContaining({ accessToken: null, user: null, role: 'GUEST' }),
    );
    expect(localStorage.getItem('accessToken')).toBeNull();
    expect(localStorage.getItem('refreshToken')).toBeNull();
    expect(localStorage.getItem('user')).toBeNull();
    expect(sessionStorage.getItem('oauth_profile_return')).not.toBeNull();
  });
});
