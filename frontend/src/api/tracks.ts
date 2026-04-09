import client from '@/api/client';
import type { ApiResponse, PagedResponse, TagItem, TrackListItem } from '@/types';

/* ── Local detail types (not in shared types) ── */

export interface TrackDetail {
  id: number;
  title: string;
  artistName: string;
  duration: number;
  bpm: number;
  tonality: string;
  description: string | null;
  audioFile: string;
  thumbnail: string | null;
  isActive: boolean;
  playCount: number;
  likeCount: number;
  downloadCount: number;
  tags: TagItem[];
  createdAt: string;
  updatedAt: string;
}

/* ── List params ── */

export interface TrackListParams {
  page?: number;
  size?: number;
  keyword?: string;
  genre?: string;
  mood?: string;
  instrument?: string;
  bpmMin?: number;
  bpmMax?: number;
  tonality?: string;
  sort?: 'latest' | 'popular' | 'likes' | 'downloads';
}

/* ── API functions ── */

/** GET /api/tracks -- public track list with filters & pagination */
export async function fetchTracks(
  params: TrackListParams = {},
): Promise<PagedResponse<TrackListItem>> {
  const query: Record<string, string | number> = {};

  if (params.page !== undefined) query.page = params.page;
  if (params.size !== undefined) query.size = params.size;
  if (params.keyword) query.keyword = params.keyword;
  if (params.genre) query.genre = params.genre;
  if (params.mood) query.mood = params.mood;
  if (params.instrument) query.instrument = params.instrument;
  if (params.bpmMin !== undefined) query.bpmMin = params.bpmMin;
  if (params.bpmMax !== undefined) query.bpmMax = params.bpmMax;
  if (params.tonality) query.tonality = params.tonality;
  if (params.sort) query.sort = params.sort;

  const { data } = await client.get<PagedResponse<TrackListItem>>('/tracks', {
    params: query,
  });
  return data;
}

/** GET /api/tracks/{trackId} -- track detail */
export async function fetchTrackDetail(trackId: number): Promise<TrackDetail> {
  const { data } = await client.get<ApiResponse<TrackDetail>>(`/tracks/${trackId}`);
  return data.data;
}

/** GET /api/tracks/admin/{trackId} -- admin track detail (includes inactive) */
export async function fetchTrackDetailForAdmin(trackId: number): Promise<TrackDetail> {
  const { data } = await client.get<ApiResponse<TrackDetail>>(`/tracks/admin/${trackId}`);
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
): Promise<PagedResponse<AdminTrackListItem>> {
  const query: Record<string, string | number | boolean> = {};

  if (params.page !== undefined) query.page = params.page;
  if (params.size !== undefined) query.size = params.size;
  if (params.is_active !== undefined) query.is_active = params.is_active;
  if (params.keyword) query.keyword = params.keyword;

  const { data } = await client.get<PagedResponse<AdminTrackListItem>>(
    '/tracks/admin',
    { params: query },
  );
  return data;
}

/** POST /api/tracks -- create track (multipart/form-data) */
export async function createTrack(formData: FormData): Promise<TrackDetail> {
  const { data } = await client.post<ApiResponse<TrackDetail>>('/tracks', formData, {
    timeout: 300_000, // 5 minutes for file upload
  });
  return data.data;
}

/** PUT /api/tracks/{trackId} -- update track (multipart/form-data) */
export async function updateTrack(
  trackId: number,
  formData: FormData,
): Promise<TrackDetail> {
  const { data } = await client.put<ApiResponse<TrackDetail>>(
    `/tracks/${trackId}`,
    formData,
    { timeout: 300_000 },
  );
  return data.data;
}

/** DELETE /api/tracks/{trackId} -- soft delete */
export async function deleteTrack(trackId: number): Promise<void> {
  await client.delete(`/tracks/${trackId}`);
}
