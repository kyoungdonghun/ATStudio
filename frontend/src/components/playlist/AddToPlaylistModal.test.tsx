import { render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import AddToPlaylistModal from '@/components/playlist/AddToPlaylistModal';

const fetchMyPlaylistsMock = vi.fn();

vi.mock('@/api/playlists', () => ({
  fetchMyPlaylists: (...args: unknown[]) => fetchMyPlaylistsMock(...args),
  addTrackToPlaylist: vi.fn(),
}));

vi.mock('@/api/client', () => ({
  isSubscriptionRequired: (err: unknown) =>
    (err as { response?: { data?: { errorCode?: string } } })?.response?.data?.errorCode ===
    'NO_ACTIVE_SUBSCRIPTION',
}));

describe('AddToPlaylistModal', () => {
  beforeEach(() => {
    fetchMyPlaylistsMock.mockReset();
  });

  it('shows a real load error instead of the empty-state copy on generic API failures', async () => {
    fetchMyPlaylistsMock.mockRejectedValue(new Error('server unavailable'));

    render(
      <AddToPlaylistModal
        open
        trackId={1}
        onClose={vi.fn()}
      />,
    );

    await waitFor(() => {
      expect(screen.getByText('재생목록을 불러오지 못했습니다.')).toBeInTheDocument();
    });
    expect(screen.queryByText('재생목록이 없습니다.')).not.toBeInTheDocument();
  });
});
