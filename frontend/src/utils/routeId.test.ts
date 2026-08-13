import { describe, expect, it } from 'vitest';
import { parsePositiveDecimalRouteID } from '@/utils/routeId';

describe('parsePositiveDecimalRouteID', () => {
  it.each([
    undefined,
    '',
    '0',
    '-1',
    '1.5',
    '1e3',
    '0x10',
    '+7',
    ' 7',
    '7 ',
    '7\n',
    '01',
    '9007199254740992',
    'abc',
  ])('rejects noncanonical route ID %s', (rawID) => {
    expect(parsePositiveDecimalRouteID(rawID)).toBeNull();
  });

  it.each([
    ['1', 1],
    ['7', 7],
    ['9007199254740991', Number.MAX_SAFE_INTEGER],
  ])('parses canonical route ID %s', (rawID, expectedID) => {
    expect(parsePositiveDecimalRouteID(rawID)).toBe(expectedID);
  });
});
