import client from '@/api/client';
import type { ApiResponse } from '@/types';

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
