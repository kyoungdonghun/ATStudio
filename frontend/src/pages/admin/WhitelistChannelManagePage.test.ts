import { describe, expect, it } from 'vitest';
import { ADMIN_STATUS_TRANSITIONS } from './whitelistStatusTransitions';

describe('admin whitelist status transitions', () => {
  it('allows only removal completion from REMOVAL_REQUESTED', () => {
    expect(ADMIN_STATUS_TRANSITIONS.REMOVAL_REQUESTED).toEqual(['CANCELLED']);
  });

  it('keeps CANCELLED terminal', () => {
    expect(ADMIN_STATUS_TRANSITIONS.CANCELLED).toEqual([]);
  });
});
