import { describe, expect, it } from 'vitest';
import {
  isWhitelistChannelDeleteActionVisible,
  isWhitelistChannelEditable,
  isWhitelistChannelPrimaryEligible,
  requiresWhitelistReprocessingConfirmation,
} from './whitelistChannelPolicy';

describe('subscriber whitelist edit policy', () => {
  it.each(['REMOVAL_REQUESTED', 'CANCELLED'] as const)('keeps %s metadata immutable', (status) => {
    expect(isWhitelistChannelEditable(status)).toBe(false);
  });

  it('keeps an ordinary draft editable', () => {
    expect(isWhitelistChannelEditable('DRAFT')).toBe(true);
  });

  it.each(['REMOVAL_REQUESTED', 'CANCELLED'] as const)(
    'keeps %s ineligible for primary selection',
    (status) => {
      expect(isWhitelistChannelPrimaryEligible(status)).toBe(false);
    },
  );

  it.each([
    'DRAFT',
    'PENDING',
    'EXPORTED',
    'REGISTERED',
    'REVISION_REQUESTED',
    'REJECTED',
  ] as const)('keeps %s eligible for primary selection', (status) => {
    expect(isWhitelistChannelPrimaryEligible(status)).toBe(true);
  });

  it('hides the destructive-looking action only after removal is already requested', () => {
    expect(isWhitelistChannelDeleteActionVisible('REMOVAL_REQUESTED')).toBe(false);
    expect(isWhitelistChannelDeleteActionVisible('CANCELLED')).toBe(true);
  });

  it.each(['EXPORTED', 'REGISTERED', 'REVISION_REQUESTED'] as const)(
    'requires reprocessing confirmation for %s edits',
    (status) => {
      expect(requiresWhitelistReprocessingConfirmation(status)).toBe(true);
    },
  );
});
