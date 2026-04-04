import client from '@/api/client';
import type { ApiResponse } from '@/types';

/* ── Public: read a setting by key ── */

export async function getSetting(key: string): Promise<string> {
  const { data } = await client.get<ApiResponse<{ key: string; value: string }>>(`/settings/${key}`);
  return data.data.value;
}

/* ── Admin: update a setting by key ── */

export async function updateSetting(key: string, value: string): Promise<void> {
  await client.put<ApiResponse<void>>(`/admin/settings/${key}`, { value });
}
