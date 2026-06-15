import client from '@/api/client';
import type { ApiResponse, WhitelistChannel } from '@/types';

/* ── Request type ── */

export interface WhitelistChannelRequest {
  channelUrl: string;
  channelName: string;
  youtubeHandle?: string | null;
  youtubeChannelId?: string | null;
}

/* ── API functions ── */

/** POST /api/whitelist-channels -- save a YouTube channel draft */
export async function registerChannel(
  req: WhitelistChannelRequest,
): Promise<WhitelistChannel> {
  const { data } = await client.post<ApiResponse<WhitelistChannel>>(
    '/whitelist-channels',
    req,
  );
  return data.data;
}

/** GET /api/whitelist-channels -- my saved whitelist channels */
export async function fetchWhitelistChannels(): Promise<{ dataList: WhitelistChannel[] }> {
  const { data } = await client.get<{ dataList: WhitelistChannel[] }>(
    '/whitelist-channels',
  );
  return data;
}

/** PUT /api/whitelist-channels/{channelId} -- update channel */
export async function updateChannel(
  channelId: number,
  req: WhitelistChannelRequest,
): Promise<WhitelistChannel> {
  const { data } = await client.put<ApiResponse<WhitelistChannel>>(
    `/whitelist-channels/${channelId}`,
    req,
  );
  return data.data;
}

/** POST /api/whitelist-channels/{channelId}/request -- request whitelist registration */
export async function requestWhitelistRegistration(
  channelId: number,
): Promise<WhitelistChannel> {
  const { data } = await client.post<ApiResponse<WhitelistChannel>>(
    `/whitelist-channels/${channelId}/request`,
  );
  return data.data;
}

/** PUT /api/whitelist-channels/{channelId}/primary -- set primary channel */
export async function setPrimaryWhitelistChannel(
  channelId: number,
): Promise<WhitelistChannel> {
  const { data } = await client.put<ApiResponse<WhitelistChannel>>(
    `/whitelist-channels/${channelId}/primary`,
  );
  return data.data;
}

/** DELETE /api/whitelist-channels/{channelId} -- delete local draft or request external removal */
export async function deleteChannel(channelId: number): Promise<void> {
  await client.delete(`/whitelist-channels/${channelId}`);
}
