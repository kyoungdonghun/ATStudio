import client from '@/api/client';

/** GET /api/tracks/{trackId}/download -- download track file (subscribers only) */
export async function downloadTrack(trackId: number): Promise<Blob> {
  const { data } = await client.get(`/tracks/${trackId}/download`, {
    responseType: 'blob',
  });
  return data;
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
