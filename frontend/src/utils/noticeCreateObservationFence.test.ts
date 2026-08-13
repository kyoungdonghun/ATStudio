import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import {
  clearNoticeCreateObservation,
  isNoticeCreateObservationRequired,
  requireNoticeCreateObservation,
} from '@/utils/noticeCreateObservationFence';

describe('Notice create observation fence', () => {
  beforeEach(() => {
    sessionStorage.clear();
    clearNoticeCreateObservation();
  });

  afterEach(() => {
    vi.restoreAllMocks();
    sessionStorage.clear();
    clearNoticeCreateObservation();
  });

  it('keeps the memory fence after remove-only failure and clears it on a later success', () => {
    requireNoticeCreateObservation();
    expect(isNoticeCreateObservationRequired()).toBe(true);

    const removeItem = vi.spyOn(Storage.prototype, 'removeItem').mockImplementation(function (
      this: Storage,
    ) {
      if (this === sessionStorage) throw new DOMException('remove blocked');
    });

    clearNoticeCreateObservation();
    sessionStorage.clear();
    expect(isNoticeCreateObservationRequired()).toBe(true);

    removeItem.mockRestore();
    clearNoticeCreateObservation();
    expect(isNoticeCreateObservationRequired()).toBe(false);
  });
});
