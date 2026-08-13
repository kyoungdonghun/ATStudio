import client from '@/api/client';
import type { ApiResponse, PagedResponse, PlayableTrack, TagItem, TrackListItem } from '@/types';

/* ── Local detail types (not in shared types) ── */

export interface TrackDetail {
  id: number;
  title: string;
  artistName: string;
  duration: number;
  bpm: number;
  tonality: string;
  description: string | null;
  audioFile: string | null;
  thumbnail: string | null;
  waveformData: string | null;
  isActive: boolean;
  playCount: number;
  likeCount: number;
  downloadCount: number;
  tags: TagItem[];
  createdAt: string;
  updatedAt: string;
}

export interface AdminTrackDetail extends TrackDetail {
  audioFile: string;
}

/* ── List params ── */

export interface TrackListParams {
  page?: number;
  size?: number;
  keyword?: string;
  genre?: readonly string[];
  mood?: readonly string[];
  instrument?: readonly string[];
  usage?: readonly string[];
  bpmMin?: number;
  bpmMax?: number;
  tonality?: string;
  sort?: 'latest' | 'popular' | 'likes' | 'downloads';
}

/* ── API functions ── */

/** GET /api/tracks -- public track list with filters & pagination */
export async function fetchTracks(
  params: TrackListParams = {},
  signal?: AbortSignal,
): Promise<PagedResponse<TrackListItem>> {
  const query = new URLSearchParams();

  if (params.page !== undefined) query.set('page', String(params.page));
  if (params.size !== undefined) query.set('size', String(params.size));
  if (params.keyword) query.set('keyword', params.keyword);
  params.genre?.forEach((value) => query.append('genre', value));
  params.mood?.forEach((value) => query.append('mood', value));
  params.instrument?.forEach((value) => query.append('instrument', value));
  params.usage?.forEach((value) => query.append('usage', value));
  if (params.bpmMin !== undefined) query.set('bpmMin', String(params.bpmMin));
  if (params.bpmMax !== undefined) query.set('bpmMax', String(params.bpmMax));
  if (params.tonality) query.set('tonality', params.tonality);
  if (params.sort) query.set('sort', params.sort);

  const { data } = await client.get<PagedResponse<TrackListItem>>('/tracks', {
    params: query,
    signal,
  });
  return data;
}

/** GET /api/tracks/{trackId} -- track detail */
export async function fetchTrackDetail(
  trackId: number,
  signal?: AbortSignal,
): Promise<TrackDetail> {
  const { data } = await client.get<ApiResponse<TrackDetail>>(`/tracks/${trackId}`, { signal });
  return data.data;
}

/** POST /api/tracks/batch -- bounded public hydration for persisted player records. */
export async function fetchPlayableTracks(ids: number[]): Promise<PlayableTrack[]> {
  const { data } = await client.post<{ dataList: PlayableTrack[] }>('/tracks/batch', { ids });
  return data.dataList ?? [];
}

/** GET /api/tracks/admin/{trackId} -- admin track detail (includes inactive) */
export async function fetchTrackDetailForAdmin(trackId: number): Promise<AdminTrackDetail> {
  const { data } = await client.get<ApiResponse<AdminTrackDetail>>(`/tracks/admin/${trackId}`);
  return data.data;
}

/* ── Admin track list item (includes isActive) ── */

export interface AdminTrackListItem {
  id: number;
  title: string;
  artistName: string;
  duration: number;
  bpm: number;
  tonality: string;
  thumbnail: string | null;
  playCount: number;
  likeCount: number;
  downloadCount: number;
  isActive: boolean;
  tags: TagItem[];
  createdAt: string;
}

export interface AdminTrackListParams {
  page?: number;
  size?: number;
  is_active?: boolean;
  keyword?: string;
}

/** GET /api/tracks/admin -- admin-only full track list */
export async function fetchAdminTracks(
  params: AdminTrackListParams = {},
  signal?: AbortSignal,
): Promise<PagedResponse<AdminTrackListItem>> {
  const query: Record<string, string | number | boolean> = {};

  if (params.page !== undefined) query.page = params.page;
  if (params.size !== undefined) query.size = params.size;
  if (params.is_active !== undefined) query.is_active = params.is_active;
  if (params.keyword) query.keyword = params.keyword;

  const { data } = await client.get<PagedResponse<AdminTrackListItem>>('/tracks/admin', {
    params: query,
    signal,
  });
  return data;
}

/** POST /api/tracks -- create track (multipart/form-data) */
export async function createTrack(formData: FormData): Promise<AdminTrackDetail> {
  const { data } = await client.post<ApiResponse<AdminTrackDetail>>('/tracks', formData, {
    timeout: 300_000, // 5 minutes for file upload
  });
  return data.data;
}

/** PUT /api/tracks/{trackId} -- update track (multipart/form-data) */
export async function updateTrack(trackId: number, formData: FormData): Promise<AdminTrackDetail> {
  const { data } = await client.put<ApiResponse<AdminTrackDetail>>(`/tracks/${trackId}`, formData, {
    timeout: 300_000,
  });
  return data.data;
}

/** DELETE /api/tracks/{trackId} -- soft delete */
export async function deleteTrack(trackId: number): Promise<void> {
  await client.delete(`/tracks/${trackId}`);
}
