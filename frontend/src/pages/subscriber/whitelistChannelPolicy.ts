import type { WhitelistChannelStatus } from '@/types';

const USER_IMMUTABLE_STATUSES = new Set<WhitelistChannelStatus>(['REMOVAL_REQUESTED', 'CANCELLED']);

export function isWhitelistChannelEditable(status: WhitelistChannelStatus): boolean {
  return !USER_IMMUTABLE_STATUSES.has(status);
}
