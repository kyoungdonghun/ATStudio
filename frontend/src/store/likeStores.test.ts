import { beforeEach, describe, expect, it, vi } from 'vitest';

const { fetchLikes, addLike, removeLike, fetchAlbumLikes, addAlbumLike, removeAlbumLike } =
  vi.hoisted(() => ({
    fetchLikes: vi.fn(),
    addLike: vi.fn(),
    removeLike: vi.fn(),
    fetchAlbumLikes: vi.fn(),
    addAlbumLike: vi.fn(),
    removeAlbumLike: vi.fn(),
  }));

vi.mock('@/api/likes', () => ({
  fetchLikes,
  addLike,
  removeLike,
  fetchAlbumLikes,
  addAlbumLike,
  removeAlbumLike,
}));

import { useLikeStore } from '@/store/likeStore';
import { useAlbumLikeStore } from '@/store/albumLikeStore';

describe('optimistic like stores', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    useLikeStore.setState({ likedIds: new Set(), loaded: false });
    useAlbumLikeStore.setState({ likedAlbumIds: new Set(), loaded: false });
  });

  it('loads and mutates track likes through the server contract', async () => {
    fetchLikes.mockResolvedValue({ dataList: [{ trackId: 3 }, { trackId: 5 }] });
    addLike.mockResolvedValue(undefined);
    removeLike.mockResolvedValue(undefined);

    await useLikeStore.getState().load();
    expect(useLikeStore.getState().loaded).toBe(true);
    expect(useLikeStore.getState().isLiked(3)).toBe(true);
    await useLikeStore.getState().toggle(3);
    expect(removeLike).toHaveBeenCalledWith(3);
    expect(useLikeStore.getState().isLiked(3)).toBe(false);
    await useLikeStore.getState().toggle(8);
    expect(addLike).toHaveBeenCalledWith(8);
    expect(useLikeStore.getState().isLiked(8)).toBe(true);
    useLikeStore.getState().remove(5);
    expect(useLikeStore.getState().isLiked(5)).toBe(false);
  });

  it('reverts track likes when the optimistic server mutation fails', async () => {
    useLikeStore.setState({ likedIds: new Set([3]), loaded: true });
    removeLike.mockRejectedValue(new Error('offline'));
    addLike.mockRejectedValue(new Error('offline'));

    await useLikeStore.getState().toggle(3);
    expect(useLikeStore.getState().isLiked(3)).toBe(true);
    await useLikeStore.getState().toggle(8);
    expect(useLikeStore.getState().isLiked(8)).toBe(false);
  });

  it('keeps the existing track state when loading fails', async () => {
    useLikeStore.setState({ likedIds: new Set([9]), loaded: false });
    fetchLikes.mockRejectedValue(new Error('unauthorized'));
    await useLikeStore.getState().load();
    expect(useLikeStore.getState().loaded).toBe(false);
    expect(useLikeStore.getState().isLiked(9)).toBe(true);
  });

  it('loads, mutates, and removes album likes', async () => {
    fetchAlbumLikes.mockResolvedValue({ dataList: [{ albumId: 4 }] });
    addAlbumLike.mockResolvedValue(undefined);
    removeAlbumLike.mockResolvedValue(undefined);

    await useAlbumLikeStore.getState().load();
    expect(useAlbumLikeStore.getState().isLiked(4)).toBe(true);
    await useAlbumLikeStore.getState().toggle(4);
    expect(removeAlbumLike).toHaveBeenCalledWith(4);
    await useAlbumLikeStore.getState().toggle(7);
    expect(addAlbumLike).toHaveBeenCalledWith(7);
    useAlbumLikeStore.getState().remove(7);
    expect(useAlbumLikeStore.getState().isLiked(7)).toBe(false);
  });

  it('reverts album mutations and ignores a failed initial load', async () => {
    useAlbumLikeStore.setState({ likedAlbumIds: new Set([4]), loaded: false });
    fetchAlbumLikes.mockRejectedValue(new Error('unauthorized'));
    removeAlbumLike.mockRejectedValue(new Error('offline'));
    addAlbumLike.mockRejectedValue(new Error('offline'));

    await useAlbumLikeStore.getState().load();
    expect(useAlbumLikeStore.getState().loaded).toBe(false);
    await useAlbumLikeStore.getState().toggle(4);
    expect(useAlbumLikeStore.getState().isLiked(4)).toBe(true);
    await useAlbumLikeStore.getState().toggle(7);
    expect(useAlbumLikeStore.getState().isLiked(7)).toBe(false);
  });
});
