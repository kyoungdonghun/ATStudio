import type { WhitelistChannelStatus } from '@/types';

const USER_IMMUTABLE_STATUSES = new Set<WhitelistChannelStatus>(['REMOVAL_REQUESTED', 'CANCELLED']);
const PRIMARY_INELIGIBLE_STATUSES = new Set<WhitelistChannelStatus>([
  'REMOVAL_REQUESTED',
  'CANCELLED',
]);
const REPROCESSING_EDIT_STATUSES = new Set<WhitelistChannelStatus>([
  'EXPORTED',
  'REGISTERED',
  'REVISION_REQUESTED',
]);

export function isWhitelistChannelEditable(status: WhitelistChannelStatus): boolean {
  return !USER_IMMUTABLE_STATUSES.has(status);
}

export function isWhitelistChannelPrimaryEligible(status: WhitelistChannelStatus): boolean {
  return !PRIMARY_INELIGIBLE_STATUSES.has(status);
}

export function isWhitelistChannelDeleteActionVisible(status: WhitelistChannelStatus): boolean {
  return status !== 'REMOVAL_REQUESTED';
}

export function requiresWhitelistReprocessingConfirmation(status: WhitelistChannelStatus): boolean {
  return REPROCESSING_EDIT_STATUSES.has(status);
}
