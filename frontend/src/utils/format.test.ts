import { describe, expect, it } from 'vitest';

import { formatDate, formatDateTime, formatNumber, formatPrice } from '@/utils/format';

describe('display formatting helpers', () => {
  it('formats numbers using the Korean locale grouping contract', () => {
    expect(formatNumber(0)).toBe('0');
    expect(formatNumber(1234567)).toContain('1,234,567');
    expect(formatPrice(29900)).toBe('₩29,900');
  });

  it('formats a calendar date and timestamp from ISO values', () => {
    expect(formatDate('2026-07-17T12:34:56+09:00')).toMatch(/2026/);
    expect(formatDateTime('2026-07-17T12:34:56+09:00')).toMatch(/2026/);
    expect(formatDate('')).toBe('-');
    expect(formatDateTime('')).toBe('-');
  });
});
