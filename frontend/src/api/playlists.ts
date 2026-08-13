import client from '@/api/client';
import type { ApiResponse, Playlist } from '@/types';

/* ── Detail types ── */

export interface PlaylistTrack {
  trackOrder: number;
  trackId: number;
  title: string;
  artistName: string;
  duration: number;
  thumbnail?: string | null;
  waveformData?: string | null;
  bpm: number;
  tonality: string;
}

export interface PlaylistDetail {
  id: number;
  title: string;
  description: string | null;
  thumbnail: string | null;
  tracks: PlaylistTrack[];
  createdAt: string;
  updatedAt: string;
}

export interface PlaylistCreateRequest {
  title: string;
  description?: string;
  thumbnail?: File;
}

/* ── API functions ── */

/** GET /api/playlists -- my playlists (no pagination, limit per subscription tier) */
export async function fetchMyPlaylists(signal?: AbortSignal): Promise<{ dataList: Playlist[] }> {
  const { data } = await client.get<{ dataList: Playlist[] }>('/playlists', { signal });
  return data;
}

/** GET /api/playlists/{playlistId} -- playlist detail with tracks */
export async function fetchPlaylistDetail(
  playlistId: number,
  signal?: AbortSignal,
): Promise<PlaylistDetail> {
  const { data } = await client.get<ApiResponse<PlaylistDetail>>(`/playlists/${playlistId}`, {
    signal,
  });
  return data.data;
}

/** POST /api/playlists -- create a new playlist (multipart/form-data) */
export async function createPlaylist(req: PlaylistCreateRequest): Promise<Playlist> {
  const formData = new FormData();
  formData.append('title', req.title);
  if (req.description) formData.append('description', req.description);
  if (req.thumbnail) formData.append('thumbnail', req.thumbnail);
  const { data } = await client.post<ApiResponse<Playlist>>('/playlists', formData, {
    timeout: 60_000, // 1 minute for image upload
  });
  return data.data;
}

/** PUT /api/playlists/{playlistId} -- update playlist (multipart/form-data) */
export async function updatePlaylist(
  playlistId: number,
  req: PlaylistCreateRequest,
): Promise<void> {
  const formData = new FormData();
  formData.append('title', req.title);
  if (req.description) formData.append('description', req.description);
  if (req.thumbnail) formData.append('thumbnail', req.thumbnail);
  await client.put(`/playlists/${playlistId}`, formData, {
    timeout: 60_000,
  });
}

/** DELETE /api/playlists/{playlistId} -- delete playlist */
export async function deletePlaylist(playlistId: number): Promise<void> {
  await client.delete(`/playlists/${playlistId}`);
}

/** POST /api/playlists/{playlistId}/tracks -- add track */
export async function addTrackToPlaylist(playlistId: number, trackId: number): Promise<void> {
  await client.post(`/playlists/${playlistId}/tracks`, { trackId });
}

/** PUT /api/playlists/{playlistId}/tracks -- reorder tracks */
export async function reorderTracks(
  playlistId: number,
  tracks: { trackId: number; trackOrder: number }[],
): Promise<void> {
  await client.put(`/playlists/${playlistId}/tracks`, { tracks });
}

/** DELETE /api/playlists/{playlistId}/tracks/{trackId} -- remove track */
export async function removeTrackFromPlaylist(playlistId: number, trackId: number): Promise<void> {
  await client.delete(`/playlists/${playlistId}/tracks/${trackId}`);
}
