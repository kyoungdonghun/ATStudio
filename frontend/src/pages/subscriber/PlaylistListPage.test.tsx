import { StrictMode } from 'react';
import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes, useLocation } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import PlaylistListPage from '@/pages/subscriber/PlaylistListPage';
import { useAuthStore } from '@/store/authStore';
import type { User } from '@/types';

const showToast = vi.fn();
const fetchMyPlaylistsMock = vi.fn();
const createPlaylistMock = vi.fn();
const fetchMySubscriptionMock = vi.fn();

vi.mock('@/store/toastStore', () => ({
  useToastStore: (selector: (state: { show: typeof showToast }) => unknown) =>
    selector({ show: showToast }),
}));

vi.mock('@/api/playlists', () => ({
  fetchMyPlaylists: (...args: unknown[]) => fetchMyPlaylistsMock(...args),
  createPlaylist: (...args: unknown[]) => createPlaylistMock(...args),
  deletePlaylist: vi.fn(),
}));

vi.mock('@/api/userSubscriptions', () => ({
  fetchMySubscription: (...args: unknown[]) => fetchMySubscriptionMock(...args),
}));

function LocationProbe() {
  const location = useLocation();
  return (
    <div data-testid="playlist-location">
      {location.pathname}:{JSON.stringify(location.state)}
    </div>
  );
}

function renderPage(
  initialEntry: string | { pathname: string; state: { openCreate: boolean } } = '/playlists',
  strict = false,
) {
  const page = (
    <MemoryRouter
      initialEntries={[initialEntry]}
      future={{ v7_startTransition: true, v7_relativeSplatPath: true }}
    >
      <LocationProbe />
      <Routes>
        <Route path="/playlists" element={<PlaylistListPage />} />
      </Routes>
    </MemoryRouter>
  );

  return render(strict ? <StrictMode>{page}</StrictMode> : page);
}

function deferred<T>() {
  let resolve!: (value: T) => void;
  let reject!: (reason?: unknown) => void;
  const promise = new Promise<T>((nextResolve, nextReject) => {
    resolve = nextResolve;
    reject = nextReject;
  });
  return { promise, resolve, reject };
}

describe('PlaylistListPage', () => {
  beforeEach(() => {
    showToast.mockReset();
    fetchMyPlaylistsMock.mockReset();
    createPlaylistMock.mockReset();
    fetchMySubscriptionMock.mockReset();
    useAuthStore.setState({
      user: { id: 1 } as User,
      accessToken: 'owner-one-token',
      role: 'USER',
    });
    fetchMyPlaylistsMock.mockResolvedValue({ dataList: [] });
    createPlaylistMock.mockResolvedValue(undefined);
    fetchMySubscriptionMock.mockResolvedValue({
      subscription: {
        maxPlaylists: 3,
      },
    });
  });

  it('shows an error state when playlist loading fails', async () => {
    fetchMyPlaylistsMock.mockRejectedValue(new Error('playlist service unavailable'));

    renderPage();

    await waitFor(() => {
      expect(screen.getByText('재생목록을 불러오지 못했습니다.')).toBeInTheDocument();
    });
  });

  it('fails closed while capacity is unavailable and recovers with one retry request', async () => {
    const retry = deferred<{ subscription: { maxPlaylists: number } }>();
    fetchMySubscriptionMock
      .mockRejectedValueOnce(new Error('subscription unavailable'))
      .mockReturnValueOnce(retry.promise);

    renderPage();

    expect(
      await screen.findByText('재생목록 생성 한도를 확인하지 못했습니다.'),
    ).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '새 재생목록' })).not.toBeInTheDocument();

    const retryButton = screen.getByRole('button', { name: '한도 다시 확인' });
    fireEvent.click(retryButton);
    fireEvent.click(retryButton);
    expect(fetchMySubscriptionMock).toHaveBeenCalledTimes(2);

    await act(async () => retry.resolve({ subscription: { maxPlaylists: 5 } }));

    expect(await screen.findByText(/최대 5개까지 만들 수 있어요/)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '새 재생목록' })).toBeInTheDocument();
  });

  it('clears prior owner capacity and ignores stale owner responses', async () => {
    const oldPlaylists = deferred<{
      dataList: Array<{ id: number; title: string; trackCount: number }>;
    }>();
    const oldCapacity = deferred<{ subscription: { maxPlaylists: number } }>();
    const currentPlaylists = deferred<{
      dataList: Array<{ id: number; title: string; trackCount: number }>;
    }>();
    const currentCapacity = deferred<{ subscription: { maxPlaylists: number } }>();
    fetchMyPlaylistsMock
      .mockReturnValueOnce(oldPlaylists.promise)
      .mockReturnValueOnce(currentPlaylists.promise);
    fetchMySubscriptionMock
      .mockReturnValueOnce(oldCapacity.promise)
      .mockReturnValueOnce(currentCapacity.promise);

    renderPage();
    await waitFor(() => expect(fetchMyPlaylistsMock).toHaveBeenCalledTimes(1));

    act(() => {
      useAuthStore.setState({
        user: { id: 2 } as User,
        accessToken: 'owner-two-token',
        role: 'USER',
      });
    });
    await waitFor(() => expect(fetchMyPlaylistsMock).toHaveBeenCalledTimes(2));

    await act(async () => {
      currentPlaylists.resolve({ dataList: [{ id: 2, title: '현재 목록', trackCount: 0 }] });
      currentCapacity.resolve({ subscription: { maxPlaylists: 9 } });
    });
    expect(await screen.findByText('현재 목록')).toBeInTheDocument();
    expect(screen.getByText('1 / 9개')).toBeInTheDocument();

    await act(async () => {
      oldPlaylists.resolve({ dataList: [{ id: 1, title: '이전 목록', trackCount: 0 }] });
      oldCapacity.resolve({ subscription: { maxPlaylists: 3 } });
    });
    expect(screen.queryByText('이전 목록')).not.toBeInTheDocument();
    expect(screen.getByText('1 / 9개')).toBeInTheDocument();
  });

  it('closes and resets create UI before a replacement owner capacity settles', async () => {
    const replacementPlaylists = deferred<{ dataList: [] }>();
    const replacementCapacity = deferred<{ subscription: { maxPlaylists: number } }>();
    const recoveredCapacity = deferred<{ subscription: { maxPlaylists: number } }>();
    fetchMyPlaylistsMock
      .mockReset()
      .mockResolvedValueOnce({ dataList: [] })
      .mockReturnValueOnce(replacementPlaylists.promise);
    fetchMySubscriptionMock
      .mockReset()
      .mockResolvedValueOnce({ subscription: { maxPlaylists: 3 } })
      .mockReturnValueOnce(replacementCapacity.promise)
      .mockReturnValueOnce(recoveredCapacity.promise);

    renderPage();

    fireEvent.click(await screen.findByRole('button', { name: '새 재생목록' }));
    fireEvent.change(screen.getByPlaceholderText('재생목록 이름'), {
      target: { value: '이전 사용자 초안' },
    });
    const staleCreateButton = screen.getByRole('button', { name: '만들기' });

    act(() => {
      useAuthStore.setState({
        user: { id: 2 } as User,
        accessToken: 'owner-two-token',
        role: 'USER',
      });
      fireEvent.click(staleCreateButton);
    });

    expect(createPlaylistMock).not.toHaveBeenCalled();
    expect(screen.queryByRole('dialog', { name: '새 재생목록 만들기' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '새 재생목록' })).not.toBeInTheDocument();

    await act(async () => replacementPlaylists.resolve({ dataList: [] }));
    await act(async () => replacementCapacity.reject(new Error('replacement capacity failed')));

    expect(
      await screen.findByText('재생목록 생성 한도를 확인하지 못했습니다.'),
    ).toBeInTheDocument();
    expect(createPlaylistMock).not.toHaveBeenCalled();
    fireEvent.click(screen.getByRole('button', { name: '한도 다시 확인' }));
    await act(async () => recoveredCapacity.resolve({ subscription: { maxPlaylists: 5 } }));

    fireEvent.click(await screen.findByRole('button', { name: '새 재생목록' }));
    expect(screen.getByPlaceholderText('재생목록 이름')).toHaveValue('');
  });

  it('aborts the retired StrictMode request owner', async () => {
    renderPage('/playlists', true);

    await waitFor(() => expect(fetchMyPlaylistsMock.mock.calls.length).toBeGreaterThanOrEqual(2));
    const firstSignal = fetchMyPlaylistsMock.mock.calls[0][0] as AbortSignal;
    const currentSignal = fetchMyPlaylistsMock.mock.calls[
      fetchMyPlaylistsMock.mock.calls.length - 1
    ]?.[0] as AbortSignal;
    expect(firstSignal.aborted).toBe(true);
    expect(currentSignal.aborted).toBe(false);
  });

  it('shows the current tier playlist limit in the header and notice', async () => {
    fetchMyPlaylistsMock.mockResolvedValue({
      dataList: [
        { id: 1, title: 'A', description: null, thumbnail: null, trackCount: 2, createdAt: '' },
        { id: 2, title: 'B', description: null, thumbnail: null, trackCount: 1, createdAt: '' },
        { id: 3, title: 'C', description: null, thumbnail: null, trackCount: 0, createdAt: '' },
      ],
    });
    fetchMySubscriptionMock.mockResolvedValue({
      subscription: {
        maxPlaylists: 5,
      },
    });

    renderPage();

    await waitFor(() => {
      expect(screen.getByText('3 / 5개')).toBeInTheDocument();
    });
    expect(screen.getByText('구독 플랜')).toBeInTheDocument();
    expect(screen.getByText(/최대 5개까지 만들 수 있어요/)).toBeInTheDocument();
  });

  it('opens the existing create modal from route state and clears it when cancelled', async () => {
    fetchMyPlaylistsMock.mockResolvedValue({ dataList: [] });

    renderPage({ pathname: '/playlists', state: { openCreate: true } });

    expect(await screen.findByRole('dialog', { name: '새 재생목록 만들기' })).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '취소' }));

    await waitFor(() => {
      expect(screen.queryByRole('dialog', { name: '새 재생목록 만들기' })).not.toBeInTheDocument();
      expect(screen.getByTestId('playlist-location')).toHaveTextContent('/playlists:null');
    });
  });

  it('consumes the create route request once when refresh races the transition', async () => {
    fetchMyPlaylistsMock.mockResolvedValue({ dataList: [] });

    renderPage({ pathname: '/playlists', state: { openCreate: true } });

    expect(await screen.findByRole('dialog', { name: '새 재생목록 만들기' })).toBeInTheDocument();
    fireEvent.change(screen.getByPlaceholderText('재생목록 이름'), {
      target: { value: '새 목록' },
    });
    fireEvent.click(screen.getByRole('button', { name: '만들기' }));

    await waitFor(() => {
      expect(createPlaylistMock).toHaveBeenCalledWith({
        title: '새 목록',
        description: undefined,
        thumbnail: undefined,
      });
      expect(fetchMyPlaylistsMock).toHaveBeenCalledTimes(2);
      expect(screen.queryByRole('dialog', { name: '새 재생목록 만들기' })).not.toBeInTheDocument();
      expect(screen.getByTestId('playlist-location')).toHaveTextContent('/playlists:null');
    });
  });
});
