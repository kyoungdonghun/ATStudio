import client from '@/api/client';
import type { Album, ApiResponse, PagedResponse } from '@/types';

/* ── Detail types ── */

export interface AlbumTrack {
  trackId: number;
  title: string;
  artistName: string;
  thumbnailUrl: string | null;
  order: number;
}

export interface AlbumDetail {
  id: number;
  title: string;
  description: string | null;
  thumbnailUrl: string | null;
  tracks: AlbumTrack[];
  likeCount: number;
  createdAt: string;
}

/* ── List params ── */

export interface AlbumListParams {
  page?: number;
  size?: number;
  sort?: 'latest' | 'popular' | 'trackCount';
}

/* ── API functions ── */

/** GET /api/albums -- public album list */
export async function fetchAlbums(
  params: AlbumListParams = {},
): Promise<PagedResponse<Album>> {
  const query: Record<string, string | number> = {};

  if (params.page !== undefined) query.page = params.page;
  if (params.size !== undefined) query.size = params.size;
  if (params.sort) query.sort = params.sort;

  const { data } = await client.get<PagedResponse<Album>>('/albums', {
    params: query,
  });
  return data;
}

/** GET /api/albums/{id} -- album detail */
export async function fetchAlbumDetail(albumId: number): Promise<AlbumDetail> {
  const { data } = await client.get<ApiResponse<AlbumDetail>>(`/albums/${albumId}`);
  return data.data;
}

/** POST /api/albums -- create album (multipart/form-data) */
export async function createAlbum(formData: FormData): Promise<Album> {
  const { data } = await client.post<ApiResponse<Album>>('/albums', formData, {
    timeout: 60_000, // 1 minute for image upload
  });
  return data.data;
}

/** PUT /api/albums/{id} -- update album (multipart/form-data) */
export async function updateAlbum(
  albumId: number,
  formData: FormData,
): Promise<Album> {
  const { data } = await client.put<ApiResponse<Album>>(`/albums/${albumId}`, formData, {
    timeout: 60_000,
  });
  return data.data;
}

/** DELETE /api/albums/{id} -- delete album */
export async function deleteAlbum(albumId: number): Promise<void> {
  await client.delete(`/albums/${albumId}`);
}

/** POST /api/albums/{id}/tracks -- add track to album */
export async function addTrackToAlbum(
  albumId: number,
  trackId: number,
): Promise<AlbumDetail> {
  const { data } = await client.post<ApiResponse<AlbumDetail>>(
    `/albums/${albumId}/tracks`,
    { trackId },
  );
  return data.data;
}

/** PUT /api/albums/{id}/tracks -- reorder tracks in album */
export async function reorderAlbumTracks(
  albumId: number,
  trackOrders: { trackId: number; order: number }[],
): Promise<AlbumDetail> {
  const { data } = await client.put<ApiResponse<AlbumDetail>>(
    `/albums/${albumId}/tracks`,
    { trackOrders },
  );
  return data.data;
}

/** DELETE /api/albums/{id}/tracks/{trackId} -- remove track from album */
export async function removeTrackFromAlbum(
  albumId: number,
  trackId: number,
): Promise<void> {
  await client.delete(`/albums/${albumId}/tracks/${trackId}`);
}
