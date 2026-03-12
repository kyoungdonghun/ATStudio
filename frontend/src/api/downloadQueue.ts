import client from '@/api/client';

/* ── Response type (matches backend DTO) ── */

export interface QueueListItem {
  trackId: number;
  title: string;
  bpm: number;
  tonality: string;
  thumbnail: string | null;
  createdAt: string;
}

/* ── API functions ── */

/** POST /api/download-queue/{trackId} -- add track to queue */
export async function addToDownloadQueue(trackId: number): Promise<void> {
  await client.post(`/download-queue/${trackId}`);
}

/** GET /api/download-queue -- list queued tracks */
export async function fetchDownloadQueue(): Promise<{ dataList: QueueListItem[] }> {
  const { data } = await client.get<{ dataList: QueueListItem[] }>('/download-queue');
  return data;
}

/** DELETE /api/download-queue/{trackId} -- remove from queue */
export async function removeFromDownloadQueue(trackId: number): Promise<void> {
  await client.delete(`/download-queue/${trackId}`);
}
