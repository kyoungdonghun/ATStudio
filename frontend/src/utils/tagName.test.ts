import { describe, expect, it } from 'vitest';
import {
  formatTagNameForDisplay,
  getTagNameValidationError,
  isDuplicateTagName,
  normalizeTagName,
} from '@/utils/tagName';

describe('tagName', () => {
  it('trims and collapses Unicode space separators before NFC normalization', () => {
    expect(normalizeTagName('  \u1100\u1161\u2003\u00a0Beat  ')).toBe('가 Beat');
  });

  it.each([
    'K-Pop',
    'R&B',
    'Hip Hop/R&B',
    "Artist's Choice",
    'Children’s Music',
    'Electronic Dance Music (EDM)',
  ])('accepts approved punctuation in %s', (name) => {
    expect(getTagNameValidationError(name)).toBeNull();
  });

  it.each([
    '#Usage',
    'Smile😀',
    'Rock\tRoll',
    'Rock\nRoll',
    'Rock\n',
    'R.B',
    'R_B',
    'R+B',
    '[Live]',
  ])('rejects disallowed input %s', (name) => {
    expect(getTagNameValidationError(name)).not.toBeNull();
  });

  it('rejects blank, raw overflow, and final code-point overflow', () => {
    expect(getTagNameValidationError('  \u2003  ')).not.toBeNull();
    expect(getTagNameValidationError(`A${'\u2003'.repeat(199)}B`)).not.toBeNull();
    expect(getTagNameValidationError('가'.repeat(50))).toBeNull();
    expect(getTagNameValidationError('가'.repeat(51))).not.toBeNull();
  });

  it('uses canonical equality, excludes the edited tag, and does not fold case', () => {
    const tags = [{ id: 1, name: 'Hip Hop' }];

    expect(isDuplicateTagName(tags, '  Hip\u2003\u2003Hop  ')).toBe(true);
    expect(isDuplicateTagName(tags, '  Hip  Hop  ', 1)).toBe(false);
    expect(isDuplicateTagName(tags, 'hip hop')).toBe(false);
  });

  it('adds a hash only when displaying USAGE tags', () => {
    expect(formatTagNameForDisplay('Shorts', 'USAGE')).toBe('#Shorts');
    expect(formatTagNameForDisplay('Hip Hop', 'GENRE')).toBe('Hip Hop');
  });
});
