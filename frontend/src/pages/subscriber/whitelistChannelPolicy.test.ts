import { describe, expect, it } from 'vitest';
import { isWhitelistChannelEditable } from './whitelistChannelPolicy';

describe('subscriber whitelist edit policy', () => {
  it.each(['REMOVAL_REQUESTED', 'CANCELLED'] as const)('keeps %s metadata immutable', (status) => {
    expect(isWhitelistChannelEditable(status)).toBe(false);
  });

  it('keeps an ordinary draft editable', () => {
    expect(isWhitelistChannelEditable('DRAFT')).toBe(true);
  });
});
