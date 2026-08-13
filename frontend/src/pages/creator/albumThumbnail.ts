export type AlbumThumbnailStatus = 'empty' | 'pending' | 'valid' | 'invalid';

export interface AlbumThumbnailSelection {
  file: File | null;
  status: AlbumThumbnailStatus;
  error: string | null;
  selectionId: number;
}

export function emptyAlbumThumbnailSelection(): AlbumThumbnailSelection {
  return { file: null, status: 'empty', error: null, selectionId: 0 };
}

export function isAlbumThumbnailBlocked(selection: AlbumThumbnailSelection): boolean {
  return selection.status === 'pending' || selection.status === 'invalid';
}
