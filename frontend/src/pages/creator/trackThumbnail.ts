export type TrackThumbnailStatus = 'empty' | 'pending' | 'valid' | 'invalid';

export interface TrackThumbnailSelection {
  file: File | null;
  status: TrackThumbnailStatus;
  error: string | null;
}

export function emptyTrackThumbnailSelection(): TrackThumbnailSelection {
  return { file: null, status: 'empty', error: null };
}
