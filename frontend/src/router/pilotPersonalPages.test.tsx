import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { afterEach, expect, it, vi } from 'vitest';
import type { User } from '@/types';
import { useAuthStore } from '@/store/authStore';
import { startPilotSession } from '@/store/pilotSession';
import { usePlayerStore } from '@/store/playerStore';
import { useToastStore } from '@/store/toastStore';
import { safeStorage } from '@/utils/safeStorage';
import LoginPage from '@/pages/auth/LoginPage';
import Header from '@/layouts/Header';
import TrackDetailPage from '@/pages/public/TrackDetailPage';
import LicenseDetailPage from '@/pages/subscriber/LicenseDetailPage';
import LicenseListPage from '@/pages/subscriber/LicenseListPage';
import DownloadHistoryPage from '@/pages/subscriber/DownloadHistoryPage';
import client from '@/api/client';

vi.mock('@/config/pilotMode', () => ({ isPilotMode: true }));

const originalAdapter = client.defaults.adapter;
afterEach(() => {
  client.defaults.adapter = originalAdapter;
  vi.restoreAllMocks();
  useAuthStore.getState().clearSession();
});

it('releases the login button and reports a session storage failure to the participant', async () => {
  const originalPath = window.location.pathname;
  window.history.replaceState({}, '', '/login');
  try {
    client.defaults.adapter = async (config) => {
      let data;
      if (config.url === '/utils/public-capabilities')
        data = {
          passwordLoginEnabled: true,
          passwordReset: { enabled: false },
          socialLogin: {
            google: { enabled: false },
            kakao: { enabled: false },
            naver: { enabled: false },
          },
        };
      else if (config.url === '/auth/login') data = { accessToken: 'A', refreshToken: 'R-A' };
      else if (config.url === '/users/me') data = { id: 11, role: 'USER' };
      else throw new Error('Unexpected request ' + config.url);
      return { config, status: 200, statusText: 'OK', headers: {}, data: { data } };
    };
    vi.spyOn(safeStorage, 'setItem').mockReturnValue(false);
    render(
      <MemoryRouter initialEntries={['/login']}>
        <LoginPage />
      </MemoryRouter>,
    );
    const button = await screen.findByRole('button', { name: '로그인' });
    await waitFor(() => expect(button).toBeEnabled());
    fireEvent.change(screen.getByLabelText('이메일'), { target: { value: 'pilot@example.test' } });
    fireEvent.change(screen.getByLabelText('비밀번호'), {
      target: { value: 'local-pilot-fixture' },
    });
    fireEvent.click(button);
    await waitFor(() =>
      expect(screen.getByRole('alert')).toHaveTextContent('로그인에 실패했습니다.'),
    );
    expect(button).toBeEnabled();
    expect(useAuthStore.getState().user).toBeNull();
  } finally {
    window.history.replaceState({}, '', originalPath);
  }
});

it('keeps a public track read anonymous and performs an authenticated individual download', async () => {
  const requests: [string, unknown][] = [];
  const click = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => {});
  const blob = new Blob(['synthetic-audio'], { type: 'audio/wav' });
  const createUrl = vi.spyOn(URL, 'createObjectURL').mockReturnValue('blob:local-test');
  vi.spyOn(URL, 'revokeObjectURL').mockImplementation(() => {});
  client.defaults.adapter = async (config) => {
    const path = config.url ?? '';
    requests.push([path, config.headers.get('Authorization')]);
    let data;
    if (path === '/tracks/7')
      data = {
        data: {
          id: 7,
          title: '공개 시험곡',
          artistName: '시험',
          bpm: 90,
          tonality: 'C',
          duration: 3,
          description: '',
          playCount: 0,
          likeCount: 0,
          downloadCount: 0,
          tags: [],
          createdAt: '2026-11-21T12:00:00',
        },
      };
    else if (path === '/tracks/7/download') data = blob;
    else if (path === '/utils/download-count') data = { data: { remaining: 4, dailyLimit: 5 } };
    else throw new Error('Unexpected request ' + path);
    return { config, status: 200, statusText: 'OK', headers: {}, data };
  };
  render(
    <MemoryRouter initialEntries={['/tracks/7']}>
      <Routes>
        <Route path="/tracks/:trackId" element={<TrackDetailPage />} />
      </Routes>
    </MemoryRouter>,
  );
  expect(await screen.findByRole('heading', { name: '공개 시험곡' })).toBeInTheDocument();
  expect(screen.queryByRole('button', { name: '다운로드' })).not.toBeInTheDocument();
  act(() => useAuthStore.getState().login('user-A', { id: 11, role: 'USER' } as User));
  fireEvent.click(screen.getByRole('button', { name: '다운로드' }));
  await waitFor(() => expect(click).toHaveBeenCalledTimes(1));
  expect(createUrl).toHaveBeenCalledWith(blob);
  expect(requests).toEqual([
    ['/tracks/7', undefined],
    ['/tracks/7/download', 'Bearer user-A'],
    ['/utils/download-count', 'Bearer user-A'],
  ]);
  expect(screen.queryByRole('button', { name: /재생목록에 추가/ })).not.toBeInTheDocument();
});

it('switches personal history and license owners without offering bulk or sales actions', async () => {
  const pageInfo = { page: 1, size: 20, total: 1, start: 1, end: 1, prev: false, next: false };
  const requests: string[] = [];
  client.defaults.adapter = async (config) => {
    const owner =
      config.headers.get('Authorization') === 'Bearer user-A'
        ? 'A'
        : config.headers.get('Authorization') === 'Bearer user-B'
          ? 'B'
          : null;
    if (!owner) throw new Error('Expected current user authentication');
    const id = owner === 'A' ? 11 : 22;
    const license = {
      id,
      track: { id, title: '곡 ' + owner },
      licenseCode: 'trial-code-' + owner,
      issuedAt: '2026-11-21T12:00:00',
      user: { id, nickname: '지인 ' + owner },
    };
    const item = {
      downloadId: id,
      trackId: id,
      title: license.track.title,
      artistName: '시험',
      bpm: 90,
      tonality: 'C',
      duration: 3,
      thumbnail: null,
      waveformData: null,
      tags: [],
      downloadedAt: license.issuedAt,
    };
    const path = config.url;
    requests.push(owner + ' ' + path);
    let data;
    if (path === '/licenses/me') data = { dataList: [license], pageInfo };
    else if (path === '/licenses/' + id) data = { data: license };
    else if (path === '/downloads/history') data = { dataList: [item], pageInfo };
    else if (path === '/utils/download-count')
      data = { data: { todayDownloads: 1, dailyLimit: 5, remaining: 4 } };
    else throw new Error('Unknown fixture request ' + path);
    return { config, status: 200, statusText: 'OK', headers: {}, data };
  };
  function login(id: number, owner: string) {
    useAuthStore
      .getState()
      .login('user-' + owner, { id, role: 'USER', nickname: '지인 ' + owner } as User);
  }
  login(11, 'A');
  render(
    <MemoryRouter initialEntries={['/downloads']}>
      <Header />
      <Routes>
        <Route path="/downloads" element={<DownloadHistoryPage />} />
        <Route path="/licenses" element={<LicenseListPage />} />
        <Route path="/licenses/:licenseId" element={<LicenseDetailPage />} />
      </Routes>
    </MemoryRouter>,
  );
  expect(await screen.findByText('곡 A')).toBeInTheDocument();
  expect(screen.queryByRole('button', { name: 'Play 곡 A' })).not.toBeInTheDocument();
  expect(usePlayerStore.getState().trackListContext).toHaveLength(0);
  expect(screen.queryByRole('checkbox')).not.toBeInTheDocument();
  expect(screen.queryByRole('link', { name: '구독' })).not.toBeInTheDocument();
  fireEvent.click(screen.getByRole('link', { name: '내 라이선스' }));
  fireEvent.click(await screen.findByRole('link', { name: '상세' }));
  expect(await screen.findByText('trial-code-A')).toBeInTheDocument();
  act(() => {
    startPilotSession({ id: 22, role: 'USER', nickname: '지인 B' } as User, 'user-B', 'refresh-B');
    window.dispatchEvent(new StorageEvent('storage', { key: 'atstudio.pilot.session' }));
  });
  expect(screen.queryByText('trial-code-A')).not.toBeInTheDocument();
  await waitFor(() => expect(requests).toContain('B /licenses/11'));
  fireEvent.click(screen.getByRole('link', { name: '내 다운로드' }));
  expect(await screen.findByText('곡 B')).toBeInTheDocument();
  expect(screen.queryByText('곡 A')).not.toBeInTheDocument();
  fireEvent.click(screen.getByRole('link', { name: '내 라이선스' }));
  fireEvent.click(await screen.findByRole('link', { name: '상세' }));
  expect(await screen.findByText('trial-code-B')).toBeInTheDocument();
  expect(requests).not.toContain('B /downloads/history/track-ids');
});

it.each([true, false])(
  'discards a pending public-page file and feedback after account replacement (success=%s)',
  async (success) => {
    useAuthStore.getState().login('user-A', { id: 11, role: 'USER' } as User);
    useToastStore.setState({ toasts: [] });
    const click = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => {});
    let settle!: () => void;
    const pending = new Promise<void>((resolve) => {
      settle = resolve;
    });
    const requests: string[] = [];
    client.defaults.adapter = async (config) => {
      requests.push(config.url!);
      const reply = { config, status: 200, statusText: 'OK', headers: {} };
      if (config.url === '/tracks/7')
        return {
          ...reply,
          data: {
            data: {
              id: 7,
              title: '공개 시험곡',
              artistName: '시험',
              bpm: 90,
              tonality: 'C',
              duration: 3,
              description: '',
              playCount: 0,
              likeCount: 0,
              downloadCount: 0,
              tags: [],
              createdAt: '2026-11-21T12:00:00',
            },
          },
        };
      if (config.url !== '/tracks/7/download') throw new Error('Unexpected follow-up request');
      await pending;
      if (!success) throw new Error('Interrupted file transfer');
      return { ...reply, data: new Blob(['synthetic']) };
    };
    render(
      <MemoryRouter initialEntries={['/tracks/7']}>
        <Routes>
          <Route path="/tracks/:trackId" element={<TrackDetailPage />} />
        </Routes>
      </MemoryRouter>,
    );
    fireEvent.click(await screen.findByRole('button', { name: '다운로드' }));
    await waitFor(() => expect(requests).toContain('/tracks/7/download'));
    // Another tab changes localStorage before this tab receives a storage event.
    await act(async () => {
      startPilotSession({ id: 22, role: 'USER' } as User, 'user-B', 'refresh-B');
      settle();
      await pending;
    });
    expect(click).not.toHaveBeenCalled();
    expect(useToastStore.getState().toasts).toEqual([]);
    expect(requests).not.toContain('/utils/download-count');
    expect(useAuthStore.getState().user?.id).toBe(22);
  },
);
