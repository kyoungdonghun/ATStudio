import client from '@/api/client';

/* ── Legacy queue API (pre-SR-79) ── */
/* Retained for backend compatibility. Subscriber-facing download history uses /api/downloads/history. */

export interface QueueListItem {
  trackId: number;
  title: string;
  bpm: number;
  tonality: string;
  thumbnail: string | null;
  createdAt: string;
}

/* ── API functions ── */

/** @deprecated Legacy queue API. Prefer direct download + download history flow. */
export async function addToDownloadQueue(trackId: number): Promise<void> {
  await client.post(`/download-queue/${trackId}`);
}

/** @deprecated Legacy queue API. Prefer /api/downloads/history for subscriber-facing history. */
export async function fetchDownloadQueue(): Promise<{ dataList: QueueListItem[] }> {
  const { data } = await client.get<{ dataList: QueueListItem[] }>('/download-queue');
  return data;
}

/** @deprecated Legacy queue API. Prefer direct download + download history flow. */
export async function removeFromDownloadQueue(trackId: number): Promise<void> {
  await client.delete(`/download-queue/${trackId}`);
}
