import { useEffect, useState } from 'react';
import { isListeningMode } from '@/config/appMode';
import { toUploadUrl } from '@/api/client';
import { fetchAdminTrackThumbnail } from '@/api/tracks';
import { useAuthStore } from '@/store/authStore';

/** Each mounted preview owns and releases its authenticated object URL. */
export function useAdminTrackThumbnail(trackId: number | null, path: string | null): string | null {
  const token = useAuthStore((state) => state.accessToken);
  const [preview, setPreview] = useState<{
    token: string;
    trackId: number;
    path: string;
    url: string;
  } | null>(null);
  useEffect(() => {
    if (!isListeningMode || trackId === null || !path || !token) return;
    setPreview(null);
    const controller = new AbortController();
    let objectURL: string | null = null;
    void fetchAdminTrackThumbnail(trackId, controller.signal)
      .then((blob) => {
        if (controller.signal.aborted) return;
        objectURL = URL.createObjectURL(blob);
        setPreview({ token, trackId, path, url: objectURL });
      })
      .catch(() => {
        // A failed protected read must not retry through publicly addressable storage.
        if (!controller.signal.aborted) setPreview(null);
      });
    return () => {
      controller.abort();
      if (objectURL) URL.revokeObjectURL(objectURL);
    };
  }, [trackId, path, token]);
  if (!isListeningMode) return toUploadUrl(path);
  return preview?.token === token && preview?.trackId === trackId && preview.path === path
    ? preview.url
    : null;
}
