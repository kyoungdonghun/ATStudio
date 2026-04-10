import client from '@/api/client';
import type { ApiResponse, PagedResponse, TagItem } from '@/types';

/* ── Types ── */

export interface DownloadCount {
  todayDownloads: number;
  dailyLimit: number;
  remaining: number;
  nextResetAt: string;
}

/** GET /api/tracks/{trackId}/download -- download track file (subscribers only) */
export async function downloadTrack(trackId: number): Promise<Blob> {
  const { data } = await client.get(`/tracks/${trackId}/download`, {
    responseType: 'blob',
  });
  return data;
}

/** GET /api/utils/download-count -- today's download stats */
export async function fetchDownloadCount(): Promise<DownloadCount> {
  const { data } = await client.get<ApiResponse<DownloadCount>>(
    '/utils/download-count',
  );
  return data.data;
}

/* ── SR-79: 다운로드 기록 (download history) ── */

export interface DownloadHistoryItem {
  downloadId: number;
  trackId: number;
  title: string;
  artistName: string | null;
  thumbnail: string | null;
  bpm: number;
  tonality: string;
  duration: number;
  tags: TagItem[];
  downloadedAt: string;
}

export interface DownloadHistoryParams {
  page?: number;
  size?: number;
  keyword?: string;
  sort?: 'latest' | 'oldest';
}

/** GET /api/downloads/history -- paginated download history */
export async function fetchDownloadHistory(
  params: DownloadHistoryParams = {},
): Promise<PagedResponse<DownloadHistoryItem>> {
  const query: Record<string, string | number> = {};
  if (params.page !== undefined) query.page = params.page;
  if (params.size !== undefined) query.size = params.size;
  if (params.keyword) query.keyword = params.keyword;
  if (params.sort) query.sort = params.sort;
  const { data } = await client.get<PagedResponse<DownloadHistoryItem>>(
    '/downloads/history',
    { params: query },
  );
  return data;
}

/** GET /api/downloads/history/track-ids -- all matching track ids for bulk re-download */
export async function fetchDownloadHistoryTrackIds(
  keyword?: string,
): Promise<number[]> {
  const query: Record<string, string> = {};
  if (keyword) query.keyword = keyword;
  const { data } = await client.get<{ dataList: number[] }>(
    '/downloads/history/track-ids',
    { params: query },
  );
  return data.dataList ?? [];
}

/** Trigger browser file download from a Blob */
export function triggerBlobDownload(blob: Blob, filename: string) {
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(url);
}
