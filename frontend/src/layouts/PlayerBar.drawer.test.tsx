import { fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import PlayerBar from '@/layouts/PlayerBar';
import { useAuthStore } from '@/store/authStore';
import { useLikeStore } from '@/store/likeStore';
import { usePlayerStore } from '@/store/playerStore';
import type { PlayableTrack, User } from '@/types';

const api = vi.hoisted(() => ({
  fetchMyPlaylists: vi.fn(),
  fetchMySubscription: vi.fn(),
  fetchLikes: vi.fn(),
}));

vi.mock('@/api/playlists', () => ({
  fetchMyPlaylists: api.fetchMyPlaylists,
  fetchPlaylistDetail: vi.fn(),
  createPlaylist: vi.fn(),
  deletePlaylist: vi.fn(),
  removeTrackFromPlaylist: vi.fn(),
  reorderTracks: vi.fn(),
}));
vi.mock('@/api/userSubscriptions', () => ({
  fetchMySubscription: api.fetchMySubscription,
  isNoActiveSubscriptionError: () => false,
}));
vi.mock('@/api/likes', () => ({ fetchLikes: api.fetchLikes }));
vi.mock('@/api/client', () => ({
  getApiErrorCode: vi.fn(),
  toUploadUrl: (value: string | null) => value,
}));
vi.mock('@/components/player/WaveformCanvas', () => ({ default: () => null }));

const track: PlayableTrack = {
  id: 1,
  title: 'Drawer integration track',
  artistName: 'Artist',
  duration: 100,
  thumbnail: null,
  waveformData: null,
};

describe.each([false, true])('PlayerBar real drawer, loaded track: %s', (hasTrack) => {
  beforeEach(() => {
    vi.clearAllMocks();
    useAuthStore.setState({
      user: { id: 1 } as User,
      accessToken: 'drawer-test-token',
      role: 'USER',
    });
    useLikeStore.setState({ loaded: true, likedIds: new Set<number>() });
    usePlayerStore.setState({
      currentTrack: hasTrack ? track : null,
      isPlaying: false,
      queue: hasTrack ? [track] : [],
    });
    api.fetchMyPlaylists.mockResolvedValue({ dataList: [] });
    api.fetchMySubscription.mockResolvedValue({ subscription: { maxPlaylists: 3 } });
    api.fetchLikes.mockResolvedValue({ dataList: [] });
  });

  it.each(['desktop', 'mobile'])('uses the visible tab for %s actions', async (layout) => {
    const view = render(
      <MemoryRouter>
        <PlayerBar />
      </MemoryRouter>,
    );
    const desktopLikes = await screen.findByRole('button', { name: '좋아요 목록 열기' });
    if (layout === 'mobile') {
      fireEvent.click(
        view.container.querySelector('button[aria-label="플레이어 상세 펼치기"]') as HTMLElement,
      );
    }
    const mobilePanel = view.container.querySelector('#player-mobile-expanded-controls');
    const likesAction =
      layout === 'mobile'
        ? (mobilePanel!.querySelector('button[aria-label="좋아요 목록 열기"]') as HTMLElement)
        : desktopLikes;
    const playlistsAction =
      layout === 'mobile'
        ? within(mobilePanel as HTMLElement).getByText('재생목록', { selector: 'button' })
        : screen.getByRole('button', { name: '재생목록' });
    const drawer = () => screen.getByRole('dialog', { name: '재생목록 및 좋아요' });

    fireEvent.click(likesAction);
    expect(within(drawer()).getByRole('button', { name: '좋아요' })).toHaveAttribute(
      'aria-pressed',
      'true',
    );
    fireEvent.click(within(drawer()).getByRole('button', { name: '재생목록' }));
    fireEvent.click(likesAction);
    expect(within(drawer()).getByRole('button', { name: '좋아요' })).toHaveAttribute(
      'aria-pressed',
      'true',
    );
    fireEvent.click(likesAction);
    expect(screen.queryByRole('dialog', { name: '재생목록 및 좋아요' })).not.toBeInTheDocument();

    fireEvent.click(playlistsAction);
    fireEvent.click(within(drawer()).getByRole('button', { name: '좋아요' }));
    fireEvent.click(playlistsAction);
    expect(within(drawer()).getByRole('button', { name: '재생목록' })).toHaveAttribute(
      'aria-pressed',
      'true',
    );
    fireEvent.click(playlistsAction);
    expect(screen.queryByRole('dialog', { name: '재생목록 및 좋아요' })).not.toBeInTheDocument();
    await waitFor(() => expect(api.fetchLikes).toHaveBeenCalled());
  });
});
