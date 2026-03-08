import client from '@/api/client';
import type { Playlist } from '@/types';

/* ── Detail types ── */

export interface PlaylistTrack {
  trackOrder: number;
  trackId: number;
  title: string;
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
}

/* ── API functions ── */

/** GET /api/playlists -- my playlists (no pagination, max 3) */
export async function fetchMyPlaylists(): Promise<{ dataList: Playlist[] }> {
  const { data } = await client.get<{ dataList: Playlist[] }>('/playlists');
  return data;
}

/** GET /api/playlists/{playlistId} -- playlist detail with tracks */
export async function fetchPlaylistDetail(
  playlistId: number,
): Promise<PlaylistDetail> {
  const { data } = await client.get<PlaylistDetail>(
    `/playlists/${playlistId}`,
  );
  return data;
}

/** POST /api/playlists -- create a new playlist */
export async function createPlaylist(
  req: PlaylistCreateRequest,
): Promise<Playlist> {
  const { data } = await client.post<Playlist>('/playlists', req);
  return data;
}

/** PUT /api/playlists/{playlistId} -- update playlist */
export async function updatePlaylist(
  playlistId: number,
  req: PlaylistCreateRequest,
): Promise<void> {
  await client.put(`/playlists/${playlistId}`, req);
}

/** DELETE /api/playlists/{playlistId} -- delete playlist */
export async function deletePlaylist(playlistId: number): Promise<void> {
  await client.delete(`/playlists/${playlistId}`);
}

/** POST /api/playlists/{playlistId}/tracks -- add track */
export async function addTrackToPlaylist(
  playlistId: number,
  trackId: number,
): Promise<void> {
  await client.post(`/playlists/${playlistId}/tracks`, { trackId });
}

/** DELETE /api/playlists/{playlistId}/tracks/{trackId} -- remove track */
export async function removeTrackFromPlaylist(
  playlistId: number,
  trackId: number,
): Promise<void> {
  await client.delete(`/playlists/${playlistId}/tracks/${trackId}`);
}
