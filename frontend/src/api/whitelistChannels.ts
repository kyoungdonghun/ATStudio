import client from '@/api/client';
import type { ApiResponse, WhitelistChannel } from '@/types';

/* ── Request type ── */

export interface WhitelistChannelRequest {
  channelUrl: string;
  channelName: string;
}

/* ── API functions ── */

/** POST /api/whitelist-channels -- register a YouTube channel */
export async function registerChannel(
  req: WhitelistChannelRequest,
): Promise<WhitelistChannel> {
  const { data } = await client.post<ApiResponse<WhitelistChannel>>(
    '/whitelist-channels',
    req,
  );
  return data.data;
}

/** GET /api/whitelist-channels -- my registered channels */
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

/** DELETE /api/whitelist-channels/{channelId} -- delete channel */
export async function deleteChannel(channelId: number): Promise<void> {
  await client.delete(`/whitelist-channels/${channelId}`);
}
