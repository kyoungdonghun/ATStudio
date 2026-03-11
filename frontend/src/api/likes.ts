import client from '@/api/client';
import type { LikeItem } from '@/types';

/** GET /api/likes — 좋아요 목록 */
export async function fetchLikes(): Promise<{ dataList: LikeItem[] }> {
  const { data } = await client.get<{ dataList: LikeItem[] }>('/likes');
  return data;
}

/** POST /api/likes/{trackId} — 좋아요 추가 */
export async function addLike(trackId: number): Promise<void> {
  await client.post(`/likes/${trackId}`);
}

/** DELETE /api/likes/{trackId} — 좋아요 해제 */
export async function removeLike(trackId: number): Promise<void> {
  await client.delete(`/likes/${trackId}`);
}
