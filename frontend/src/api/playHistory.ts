import client from '@/api/client';
import type { PlayHistory, PageInfo } from '@/types';

/** POST /api/play-histories — 재생 기록 저장 */
export async function recordPlay(trackId: number): Promise<void> {
  await client.post('/play-histories', { trackId });
}

/** GET /api/play-histories — 재생 기록 조회 */
export async function fetchPlayHistory(params?: {
  page?: number;
  size?: number;
}): Promise<{ dataList: PlayHistory[]; pageInfo: PageInfo }> {
  const { data } = await client.get<{
    dataList: PlayHistory[];
    pageInfo: PageInfo;
  }>('/play-histories', { params });
  return data;
}

/** DELETE /api/play-histories — 재생 기록 삭제 (빈 배열이면 전체 삭제) */
export async function deletePlayHistory(
  historyIds: number[],
): Promise<void> {
  await client.delete('/play-histories', { data: { historyIds } });
}
