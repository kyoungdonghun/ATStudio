import client from '@/api/client';
import type { TagItem, TagType } from '@/types';

/** GET /api/tags -- tag list (backend returns raw array) */
export async function fetchTags(type?: string): Promise<TagItem[]> {
  const params: Record<string, string> = {};
  if (type) params.type = type;

  const { data } = await client.get<TagItem[]>('/tags', { params });
  return data;
}

/* ── Admin Tag CRUD ── */

interface TagCreateRequest {
  name: string;
  type: TagType;
}

interface TagUpdateRequest {
  name: string;
  type: TagType;
}

export async function createTag(body: TagCreateRequest): Promise<TagItem> {
  const { data } = await client.post<{ data: TagItem }>('/tags', body);
  return data.data;
}

export async function updateTag(
  tagId: number,
  body: TagUpdateRequest,
): Promise<TagItem> {
  const { data } = await client.put<{ data: TagItem }>(`/tags/${tagId}`, body);
  return data.data;
}

export async function deleteTag(tagId: number): Promise<void> {
  await client.delete(`/tags/${tagId}`);
}
