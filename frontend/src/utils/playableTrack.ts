import type { PlayableTrack, TagItem } from '@/types';

export interface PlayableTrackSource {
  id?: number;
  trackId?: number;
  title: string;
  artistName: string;
  duration: number;
  thumbnail?: string | null;
  thumbnailUrl?: string | null;
  waveformData?: string | null;
  bpm?: number;
  tonality?: string;
  tags?: TagItem[];
}

export function toPlayableTrack(source: PlayableTrackSource): PlayableTrack {
  const id = source.id ?? source.trackId;
  if (!Number.isSafeInteger(id) || Number(id) < 1) {
    throw new TypeError('PlayableTrack requires a positive integer id');
  }

  return {
    id: Number(id),
    title: source.title,
    artistName: source.artistName,
    duration: source.duration,
    thumbnail: source.thumbnail ?? source.thumbnailUrl ?? null,
    waveformData: source.waveformData ?? null,
    ...(source.bpm !== undefined ? { bpm: source.bpm } : {}),
    ...(source.tonality !== undefined ? { tonality: source.tonality } : {}),
    ...(source.tags !== undefined ? { tags: source.tags } : {}),
  };
}
