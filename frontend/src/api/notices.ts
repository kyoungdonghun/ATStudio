import client from '@/api/client';
import type { ApiResponse, Notice, PagedResponse } from '@/types';

interface NoticeListParams {
  page?: number;
  size?: number;
}

export async function fetchNotices(
  params: NoticeListParams = {},
): Promise<PagedResponse<Notice>> {
  const { data } = await client.get<PagedResponse<Notice>>('/notices', {
    params,
  });
  return data;
}

export async function fetchNotice(noticeId: number): Promise<Notice> {
  const { data } = await client.get<ApiResponse<Notice>>(`/notices/${noticeId}`);
  return data.data;
}

/* ── Admin Notice CRUD ── */

interface NoticeCreateRequest {
  title: string;
  content: string;
  isPinned: boolean;
}

interface NoticeUpdateRequest {
  title: string;
  content: string;
  isPinned: boolean;
}

export async function createNotice(body: NoticeCreateRequest): Promise<Notice> {
  const { data } = await client.post<ApiResponse<Notice>>('/notices', body);
  return data.data;
}

export async function updateNotice(
  noticeId: number,
  body: NoticeUpdateRequest,
): Promise<Notice> {
  const { data } = await client.put<ApiResponse<Notice>>(`/notices/${noticeId}`, body);
  return data.data;
}

export async function deleteNotice(noticeId: number): Promise<void> {
  await client.delete(`/notices/${noticeId}`);
}
