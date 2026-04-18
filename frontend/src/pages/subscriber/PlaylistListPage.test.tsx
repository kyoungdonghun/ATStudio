import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import PlaylistListPage from '@/pages/subscriber/PlaylistListPage';

const showToast = vi.fn();
const fetchMyPlaylistsMock = vi.fn();
const fetchMySubscriptionMock = vi.fn();

vi.mock('@/store/toastStore', () => ({
  useToastStore: (selector: (state: { show: typeof showToast }) => unknown) =>
    selector({ show: showToast }),
}));

vi.mock('@/api/playlists', () => ({
  fetchMyPlaylists: (...args: unknown[]) => fetchMyPlaylistsMock(...args),
  createPlaylist: vi.fn(),
  deletePlaylist: vi.fn(),
}));

vi.mock('@/api/userSubscriptions', () => ({
  fetchMySubscription: (...args: unknown[]) => fetchMySubscriptionMock(...args),
}));

function renderPage() {
  return render(
    <MemoryRouter initialEntries={['/playlists']}>
      <PlaylistListPage />
    </MemoryRouter>,
  );
}

describe('PlaylistListPage', () => {
  beforeEach(() => {
    showToast.mockReset();
    fetchMyPlaylistsMock.mockReset();
    fetchMySubscriptionMock.mockReset();
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
});
