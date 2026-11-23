import { create } from 'zustand';
import { fetchLikes, addLike, removeLike } from '@/api/likes';
import { isPilotMode } from '@/config/pilotMode';
import { getAuthSessionGeneration, isAuthSessionCurrent } from './authStore';
import { useToastStore } from './toastStore';

interface LikeState {
  likedIds: Set<number>;
  loaded: boolean;
  load: () => Promise<void>;
  toggle: (trackId: number) => Promise<void>;
  remove: (trackId: number) => void;
  isLiked: (trackId: number) => boolean;
}

let loadFlight: { session: number; promise: Promise<void> } | null = null;
const pendingToggles = new Map<number, number>();

export const useLikeStore = create<LikeState>((set, get) => ({
  likedIds: new Set(),
  loaded: false,

  load: () => {
    const session = getAuthSessionGeneration();
    if (loadFlight?.session === session) return loadFlight.promise;
    const promise = (async () => {
      try {
        const res = await fetchLikes();
        if (!isAuthSessionCurrent(session)) return;
        const ids = new Set((res.dataList ?? []).map((it) => it.trackId));
        set({ likedIds: ids, loaded: true });
      } catch {
        /* The next visit or action can retry the read. */
      } finally {
        if (loadFlight?.session === session) loadFlight = null;
      }
    })();
    loadFlight = { session, promise };
    return promise;
  },

  toggle: async (trackId: number) => {
    const session = getAuthSessionGeneration();
    if (!isAuthSessionCurrent(session) || pendingToggles.get(trackId) === session) return;
    pendingToggles.set(trackId, session);
    if (isPilotMode && !get().loaded) {
      await get().load();
      if (!isAuthSessionCurrent(session) || !get().loaded) {
        if (pendingToggles.get(trackId) === session) pendingToggles.delete(trackId);
        if (isAuthSessionCurrent(session))
          useToastStore.getState().show('error', '좋아요 목록을 불러오지 못했습니다.');
        return;
      }
    }
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
      if (!isAuthSessionCurrent(session)) return;
      // Revert on error
      const revert = new Set(get().likedIds);
      if (wasLiked) {
        revert.add(trackId);
      } else {
        revert.delete(trackId);
      }
      set({ likedIds: revert });
      if (isPilotMode) useToastStore.getState().show('error', '좋아요를 변경하지 못했습니다.');
    } finally {
      if (pendingToggles.get(trackId) === session) pendingToggles.delete(trackId);
    }
  },

  remove: (trackId: number) => {
    const next = new Set(get().likedIds);
    next.delete(trackId);
    set({ likedIds: next });
  },

  isLiked: (trackId: number) => get().likedIds.has(trackId),
}));
