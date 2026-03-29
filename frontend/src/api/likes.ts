import client from '@/api/client';
import type { LikeItem, AlbumLikeItem } from '@/types';

/** GET /api/likes — 트랙 좋아요 목록 */
export async function fetchLikes(): Promise<{ dataList: LikeItem[] }> {
  const { data } = await client.get<{ dataList: LikeItem[] }>('/likes');
  return data;
}

/** POST /api/likes/{trackId} — 트랙 좋아요 추가 */
export async function addLike(trackId: number): Promise<void> {
  await client.post(`/likes/${trackId}`);
}

/** DELETE /api/likes/{trackId} — 트랙 좋아요 해제 */
export async function removeLike(trackId: number): Promise<void> {
  await client.delete(`/likes/${trackId}`);
}

// ── Album Likes ──────────────────────────────────────────────────────────

/** GET /api/likes/albums — 앨범 좋아요 목록 */
export async function fetchAlbumLikes(): Promise<{ dataList: AlbumLikeItem[] }> {
  const { data } = await client.get<{ dataList: AlbumLikeItem[] }>('/likes/albums');
  return data;
}

/** POST /api/likes/albums/{albumId} — 앨범 좋아요 추가 */
export async function addAlbumLike(albumId: number): Promise<void> {
  await client.post(`/likes/albums/${albumId}`);
}

/** DELETE /api/likes/albums/{albumId} — 앨범 좋아요 해제 */
export async function removeAlbumLike(albumId: number): Promise<void> {
  await client.delete(`/likes/albums/${albumId}`);
}
