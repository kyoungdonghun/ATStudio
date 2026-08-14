import client from '@/api/client';
import type { ApiResponse, PagedResponse, TagItem } from '@/types';
import type { AxiosRequestConfig } from 'axios';

/* ── Types ── */

export interface DownloadCount {
  todayDownloads: number;
  dailyLimit: number;
  remaining: number;
  nextResetAt: string;
}

export interface BinaryDownload {
  blob: Blob;
  fileName: string;
  contentType: string;
}

type BinaryResponse = {
  data: unknown;
  headers?: unknown;
};

const UNICODE_OTHER_CHARACTERS = /\p{C}/u;
const INVALID_FILENAME_CHARACTERS = /[\\/:*?"<>|]/g;
const MEDIA_TYPE_PATTERN = /^[!#$%&'*+\-.^_`|~0-9A-Za-z]+\/[!#$%&'*+\-.^_`|~0-9A-Za-z]+$/;
const CONTENT_TYPE_EXTENSIONS: Record<string, string> = {
  'application/pdf': 'pdf',
  'audio/aac': 'aac',
  'audio/mpeg': 'mp3',
  'audio/mp4': 'm4a',
  'audio/ogg': 'ogg',
  'audio/wav': 'wav',
  'audio/x-wav': 'wav',
  'image/jpeg': 'jpg',
  'image/png': 'png',
};

function readHeader(headers: unknown, name: string): string | null {
  if (!headers || typeof headers !== 'object') return null;

  if ('get' in headers && typeof headers.get === 'function') {
    const value = headers.get(name);
    if (typeof value === 'string') return value;
  }

  for (const [key, value] of Object.entries(headers)) {
    if (key.toLowerCase() !== name.toLowerCase()) continue;
    if (typeof value === 'string') return value;
    if (Array.isArray(value)) return value.find((item) => typeof item === 'string') ?? null;
  }
  return null;
}

function normalizeContentType(value: string | null): string | null {
  if (!value || UNICODE_OTHER_CHARACTERS.test(value)) return null;
  const mediaType = value.split(';', 1)[0]?.trim().toLowerCase();
  return mediaType && MEDIA_TYPE_PATTERN.test(mediaType) ? mediaType : null;
}

function sanitizeFileName(value: string | null | undefined): string | null {
  if (!value) return null;
  const trimmed = value.trim();
  if (
    !trimmed ||
    UNICODE_OTHER_CHARACTERS.test(trimmed) ||
    /(?:^|[\\/])\.\.?($|[\\/])/.test(trimmed) ||
    /^\.+$/.test(trimmed)
  ) {
    return null;
  }

  const sanitized = trimmed.replace(INVALID_FILENAME_CHARACTERS, '').trim();
  return sanitized && !/^\.+$/.test(sanitized) ? sanitized.slice(0, 200) : null;
}

function decodeRfc5987FileName(value: string): string | null {
  const match = /^([^']*)'[^']*'(.*)$/.exec(value);
  if (!match || match[1].toLowerCase() !== 'utf-8') return null;
  try {
    return decodeURIComponent(match[2]);
  } catch {
    return null;
  }
}

function readResponseFileName(disposition: string | null): string | null {
  if (!disposition || UNICODE_OTHER_CHARACTERS.test(disposition)) return null;

  const encoded = /(?:^|;)\s*filename\*\s*=\s*(?:"([^"]*)"|([^;]*))/i.exec(disposition);
  if (encoded) {
    const decoded = decodeRfc5987FileName((encoded[1] ?? encoded[2] ?? '').trim());
    const safeName = sanitizeFileName(decoded);
    if (safeName) return safeName;
  }

  const basic = /(?:^|;)\s*filename\s*=\s*(?:"([^"]*)"|([^;]*))/i.exec(disposition);
  return sanitizeFileName((basic?.[1] ?? basic?.[2] ?? '').trim());
}

function applyContentTypeExtension(fileName: string, contentType: string): string {
  const extension = CONTENT_TYPE_EXTENSIONS[contentType];
  if (!extension) return fileName;
  if (fileName.toLowerCase().endsWith(`.${extension}`)) return fileName;
  const withoutExtension = fileName.replace(/\.[A-Za-z0-9]{1,10}$/, '');
  return `${withoutExtension}.${extension}`;
}

export function createDownloadFallbackFileName(
  resource: string,
  stableId: string | number,
  metadata?: string | null,
  defaultExtension?: string,
): string {
  const safeResource =
    sanitizeFileName(resource)?.replace(/\.[A-Za-z0-9]{1,10}$/, '') || 'download';
  const safeId = sanitizeFileName(String(stableId)) || 'item';
  const safeMetadata = sanitizeFileName(metadata)?.replace(/\.[A-Za-z0-9]{1,10}$/, '');
  const extension = defaultExtension?.replace(/[^A-Za-z0-9]/g, '').toLowerCase();
  const baseName = [safeResource, safeId, safeMetadata].filter(Boolean).join('-').slice(0, 190);
  return extension ? `${baseName}.${extension}` : baseName;
}

export function normalizeBinaryDownload(
  response: BinaryResponse,
  fallbackFileName: string,
): BinaryDownload {
  if (!(response.data instanceof Blob)) {
    throw new Error('DOWNLOAD_INVALID_BODY');
  }
  if (response.data.size === 0) {
    throw new Error('DOWNLOAD_EMPTY_BODY');
  }

  const contentType =
    normalizeContentType(readHeader(response.headers, 'content-type')) ??
    normalizeContentType(response.data.type) ??
    'application/octet-stream';
  const responseFileName = readResponseFileName(
    readHeader(response.headers, 'content-disposition'),
  );
  const safeFallback = sanitizeFileName(fallbackFileName) || 'download';

  return {
    blob: response.data,
    fileName: responseFileName ?? applyContentTypeExtension(safeFallback, contentType),
    contentType,
  };
}

export async function getBinaryDownload(
  url: string,
  fallbackFileName: string,
  requestConfig: AxiosRequestConfig = {},
): Promise<BinaryDownload> {
  const response = await client.get<Blob>(url, { ...requestConfig, responseType: 'blob' });
  return normalizeBinaryDownload(response, fallbackFileName);
}

/** GET /api/tracks/{trackId}/download -- download track file (subscribers only) */
export function downloadTrack(
  trackId: number,
  fallbackFileName: string,
  signal?: AbortSignal,
): Promise<BinaryDownload> {
  return getBinaryDownload(`/tracks/${trackId}/download`, fallbackFileName, { signal });
}

/** GET /api/utils/download-count -- today's download stats */
export async function fetchDownloadCount(signal?: AbortSignal): Promise<DownloadCount> {
  const { data } = await client.get<ApiResponse<DownloadCount>>('/utils/download-count', {
    signal,
  });
  return data.data;
}

/* ── SR-79: 다운로드 기록 (download history) ── */

export interface DownloadHistoryItem {
  downloadId: number;
  trackId: number;
  title: string;
  artistName: string;
  thumbnail: string | null;
  bpm: number;
  tonality: string;
  duration: number;
  waveformData: string | null;
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
  signal?: AbortSignal,
): Promise<PagedResponse<DownloadHistoryItem>> {
  const query: Record<string, string | number> = {};
  if (params.page !== undefined) query.page = params.page;
  if (params.size !== undefined) query.size = params.size;
  if (params.keyword) query.keyword = params.keyword;
  if (params.sort) query.sort = params.sort;
  const { data } = await client.get<PagedResponse<DownloadHistoryItem>>('/downloads/history', {
    params: query,
    signal,
  });
  return data;
}

/** GET /api/downloads/history/track-ids -- all matching track ids for bulk re-download */
export async function fetchDownloadHistoryTrackIds(
  keyword?: string,
  signal?: AbortSignal,
): Promise<number[]> {
  const query: Record<string, string> = {};
  if (keyword) query.keyword = keyword;
  const { data } = await client.get<{ dataList: number[] }>('/downloads/history/track-ids', {
    params: query,
    signal,
  });
  return data.dataList ?? [];
}

/** Trigger a browser download from a validated binary response. */
export function triggerBlobDownload(download: BinaryDownload) {
  const url = URL.createObjectURL(download.blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = download.fileName;
  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(url);
}
