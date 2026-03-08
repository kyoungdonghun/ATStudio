import client from '@/api/client';
import type { Album, PagedResponse } from '@/types';

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
  createdAt: string;
}

/* ── List params ── */

export interface AlbumListParams {
  page?: number;
  size?: number;
  sort?: 'latest' | 'popular';
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
  const { data } = await client.get<AlbumDetail>(`/albums/${albumId}`);
  return data;
}

/** POST /api/albums -- create album (multipart/form-data) */
export async function createAlbum(formData: FormData): Promise<Album> {
  const { data } = await client.post<Album>('/albums', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return data;
}

/** PUT /api/albums/{id} -- update album (multipart/form-data) */
export async function updateAlbum(
  albumId: number,
  formData: FormData,
): Promise<Album> {
  const { data } = await client.put<Album>(`/albums/${albumId}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return data;
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
  const { data } = await client.post<AlbumDetail>(
    `/albums/${albumId}/tracks`,
    { trackId },
  );
  return data;
}

/** DELETE /api/albums/{id}/tracks/{trackId} -- remove track from album */
export async function removeTrackFromAlbum(
  albumId: number,
  trackId: number,
): Promise<void> {
  await client.delete(`/albums/${albumId}/tracks/${trackId}`);
}
