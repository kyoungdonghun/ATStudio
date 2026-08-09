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
    genre?: readonly string[];
    mood?: readonly string[];
    instrument?: readonly string[];
    usage?: readonly string[];
    bpmMin?: number;
    bpmMax?: number;
  },
  signal?: AbortSignal,
): Promise<TagItem[]> {
  const query = new URLSearchParams();
  params.genre?.forEach((value) => query.append('genre', value));
  params.mood?.forEach((value) => query.append('mood', value));
  params.instrument?.forEach((value) => query.append('instrument', value));
  params.usage?.forEach((value) => query.append('usage', value));
  if (params.bpmMin !== undefined) query.set('bpmMin', String(params.bpmMin));
  if (params.bpmMax !== undefined) query.set('bpmMax', String(params.bpmMax));
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
