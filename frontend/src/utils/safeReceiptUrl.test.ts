import { describe, expect, it } from 'vitest';
import { getSafeReceiptUrl } from './safeReceiptUrl';

describe('getSafeReceiptUrl', () => {
  it.each([
    ['https://receipts.example.com/r/1', 'https://receipts.example.com/r/1'],
    ['HTTPS://RECEIPTS.EXAMPLE.COM/r/1', 'https://receipts.example.com/r/1'],
    ['https://receipts.example.com:443/r/1', 'https://receipts.example.com/r/1'],
  ])('accepts and normalizes provider-neutral HTTPS receipt URLs: %s', (value, expected) => {
    expect(getSafeReceiptUrl(value)).toBe(expected);
  });

  it.each([
    'javascript:alert(1)',
    'data:text/html,test',
    'file:///tmp/receipt',
    'ftp://receipts.example.com/r/1',
    '//receipts.example.com/r/1',
    'https://user:password@receipts.example.com/r/1',
    'https://receipts.example.com:8443/r/1',
    'not a url',
  ])('rejects unsafe receipt URLs: %s', (value) => {
    expect(getSafeReceiptUrl(value)).toBeNull();
  });
});
