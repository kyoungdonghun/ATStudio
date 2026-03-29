import { create } from 'zustand';
import { fetchAlbumLikes, addAlbumLike, removeAlbumLike } from '@/api/likes';

interface AlbumLikeState {
  likedAlbumIds: Set<number>;
  loaded: boolean;
  load: () => Promise<void>;
  toggle: (albumId: number) => Promise<void>;
  remove: (albumId: number) => void;
  isLiked: (albumId: number) => boolean;
}

export const useAlbumLikeStore = create<AlbumLikeState>((set, get) => ({
  likedAlbumIds: new Set(),
  loaded: false,

  load: async () => {
    try {
      const res = await fetchAlbumLikes();
      const ids = new Set((res.dataList ?? []).map((it) => it.albumId));
      set({ likedAlbumIds: ids, loaded: true });
    } catch {
      /* ignore — user may not be logged in */
    }
  },

  toggle: async (albumId: number) => {
    const { likedAlbumIds } = get();
    const wasLiked = likedAlbumIds.has(albumId);

    // Optimistic update
    const next = new Set(likedAlbumIds);
    if (wasLiked) {
      next.delete(albumId);
    } else {
      next.add(albumId);
    }
    set({ likedAlbumIds: next });

    try {
      if (wasLiked) {
        await removeAlbumLike(albumId);
      } else {
        await addAlbumLike(albumId);
      }
    } catch {
      // Revert on error
      const revert = new Set(get().likedAlbumIds);
      if (wasLiked) {
        revert.add(albumId);
      } else {
        revert.delete(albumId);
      }
      set({ likedAlbumIds: revert });
    }
  },

  remove: (albumId: number) => {
    const next = new Set(get().likedAlbumIds);
    next.delete(albumId);
    set({ likedAlbumIds: next });
  },

  isLiked: (albumId: number) => get().likedAlbumIds.has(albumId),
}));
