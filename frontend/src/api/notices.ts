import client from '@/api/client';
import { getBinaryDownload, type BinaryDownload } from '@/api/downloads';
import type { ApiResponse, Notice, PagedResponse } from '@/types';

interface NoticeListParams {
  page?: number;
  size?: number;
  sort?: 'latest' | 'views';
}

export async function fetchNotices(params: NoticeListParams = {}): Promise<PagedResponse<Notice>> {
  const { data } = await client.get<PagedResponse<Notice>>('/notices', {
    params,
  });
  return data;
}

export async function fetchNotice(noticeId: number, signal?: AbortSignal): Promise<Notice> {
  const { data } = await client.get<ApiResponse<Notice>>(`/notices/${noticeId}`, { signal });
  return data.data;
}

export interface NoticeAdminProjection {
  title: string;
  content: string;
  isPinned: boolean;
  attachments: Notice['attachments'];
}

export async function fetchAdminNotice(
  noticeId: number,
  signal?: AbortSignal,
): Promise<NoticeAdminProjection> {
  const { data } = await client.get<ApiResponse<NoticeAdminProjection>>(
    `/notices/${noticeId}/admin`,
    { signal },
  );
  return data.data;
}

/* ── Admin Notice CRUD ── */

interface NoticeCreateBody {
  title: string;
  content: string;
  isPinned: boolean;
  attachments?: File[];
}

interface NoticeUpdateBody {
  title: string;
  content: string;
  isPinned: boolean;
  deleteAttachmentIds?: number[];
  newAttachments?: File[];
}

export async function createNotice(body: NoticeCreateBody, signal?: AbortSignal): Promise<Notice> {
  const form = new FormData();
  form.append('title', body.title);
  form.append('content', body.content);
  form.append('isPinned', String(body.isPinned));
  if (body.attachments) {
    body.attachments.forEach((file) => form.append('attachments', file));
  }
  const { data } = await client.post<ApiResponse<Notice>>('/notices', form, {
    timeout: 60_000, // 1 minute for attachment upload
    signal,
  });
  return data.data;
}

export async function updateNotice(
  noticeId: number,
  body: NoticeUpdateBody,
  signal?: AbortSignal,
): Promise<Notice> {
  const form = new FormData();
  form.append('title', body.title);
  form.append('content', body.content);
  form.append('isPinned', String(body.isPinned));
  if (body.deleteAttachmentIds) {
    body.deleteAttachmentIds.forEach((id) => form.append('deleteAttachmentIds', String(id)));
  }
  if (body.newAttachments) {
    body.newAttachments.forEach((file) => form.append('newAttachments', file));
  }
  const { data } = await client.put<ApiResponse<Notice>>(`/notices/${noticeId}`, form, {
    timeout: 60_000,
    signal,
  });
  return data.data;
}

export async function deleteNotice(noticeId: number, signal?: AbortSignal): Promise<void> {
  await client.delete(`/notices/${noticeId}`, { signal });
}

/* ── Attachment download ── */

export async function downloadNoticeAttachment(
  noticeId: number,
  attachmentId: number,
  fallbackFileName: string,
  signal?: AbortSignal,
): Promise<BinaryDownload> {
  return getBinaryDownload(`/notices/${noticeId}/attachments/${attachmentId}`, fallbackFileName, {
    signal,
  });
}
