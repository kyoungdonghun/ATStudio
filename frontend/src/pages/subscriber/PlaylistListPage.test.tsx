import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes, useLocation } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import PlaylistListPage from '@/pages/subscriber/PlaylistListPage';

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
) {
  return render(
    <MemoryRouter
      initialEntries={[initialEntry]}
      future={{ v7_startTransition: true, v7_relativeSplatPath: true }}
    >
      <LocationProbe />
      <Routes>
        <Route path="/playlists" element={<PlaylistListPage />} />
      </Routes>
    </MemoryRouter>,
  );
}

describe('PlaylistListPage', () => {
  beforeEach(() => {
    showToast.mockReset();
    fetchMyPlaylistsMock.mockReset();
    createPlaylistMock.mockReset();
    fetchMySubscriptionMock.mockReset();
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
      expect(screen.getByText('playlist service unavailable')).toBeInTheDocument();
    });
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
