import { create } from 'zustand';
import { fetchLikes, addLike, removeLike } from '@/api/likes';

interface LikeState {
  likedIds: Set<number>;
  loaded: boolean;
  load: () => Promise<void>;
  toggle: (trackId: number) => Promise<void>;
  remove: (trackId: number) => void;
  isLiked: (trackId: number) => boolean;
}

export const useLikeStore = create<LikeState>((set, get) => ({
  likedIds: new Set(),
  loaded: false,

  load: async () => {
    try {
      const res = await fetchLikes();
      const ids = new Set((res.dataList ?? []).map((it) => it.trackId));
      set({ likedIds: ids, loaded: true });
    } catch {
      /* ignore — user may not be logged in */
    }
  },

  toggle: async (trackId: number) => {
    const { likedIds } = get();
    const wasLiked = likedIds.has(trackId);

    // Optimistic update
    const next = new Set(likedIds);
    if (wasLiked) {
      next.delete(trackId);
    } else {
      next.add(trackId);
    }
    set({ likedIds: next });

    try {
      if (wasLiked) {
        await removeLike(trackId);
      } else {
        await addLike(trackId);
      }
    } catch {
      // Revert on error
      const revert = new Set(get().likedIds);
      if (wasLiked) {
        revert.add(trackId);
      } else {
        revert.delete(trackId);
      }
      set({ likedIds: revert });
    }
  },

  remove: (trackId: number) => {
    const next = new Set(get().likedIds);
    next.delete(trackId);
    set({ likedIds: next });
  },

  isLiked: (trackId: number) => get().likedIds.has(trackId),
}));
