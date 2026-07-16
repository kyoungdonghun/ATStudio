import client from '@/api/client';
import type { TagItem, TagType } from '@/types';

/** GET /api/tags -- tag list */
export async function fetchTags(type?: string): Promise<TagItem[]> {
  const params: Record<string, string> = {};
  if (type) params.type = type;

  const { data } = await client.get<{ dataList: TagItem[] }>('/tags', { params });
  return data.dataList;
}

/** GET /api/tags/available -- tags from tracks matching current filters */
export async function fetchAvailableTags(
  params: {
    genre?: string;
    mood?: string;
    usage?: string;
    bpmMin?: number;
    bpmMax?: number;
  },
  signal?: AbortSignal,
): Promise<TagItem[]> {
  const query: Record<string, string | number> = {};
  if (params.genre) query.genre = params.genre;
  if (params.mood) query.mood = params.mood;
  if (params.usage) query.usage = params.usage;
  if (params.bpmMin !== undefined) query.bpmMin = params.bpmMin;
  if (params.bpmMax !== undefined) query.bpmMax = params.bpmMax;
  const { data } = await client.get<{ dataList: TagItem[] }>('/tags/available', {
    params: query,
    signal,
  });
  return data.dataList;
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

export async function updateTag(tagId: number, body: TagUpdateRequest): Promise<TagItem> {
  const { data } = await client.put<{ data: TagItem }>(`/tags/${tagId}`, body);
  return data.data;
}

export async function deleteTag(tagId: number): Promise<void> {
  await client.delete(`/tags/${tagId}`);
}
