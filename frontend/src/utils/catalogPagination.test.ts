import { describe, expect, it } from 'vitest';
import {
  PUBLIC_CATALOG_PAGE_SIZE,
  getCatalogTotalPages,
  normalizeCatalogPage,
} from '@/utils/catalogPagination';

describe('catalog pagination', () => {
  it.each([
    [null, 1],
    ['', 1],
    ['abc', 1],
    ['-1', 1],
    ['0', 1],
    ['1.5', 1],
    ['2', 2],
    ['9007199254740992', 1],
  ])('normalizes %s to a bounded 1-based page', (rawPage, expectedPage) => {
    expect(normalizeCatalogPage(rawPage)).toBe(expectedPage);
  });

  it('uses one public catalog page size and keeps an empty catalog on page one', () => {
    expect(PUBLIC_CATALOG_PAGE_SIZE).toBe(20);
    expect(getCatalogTotalPages(0, PUBLIC_CATALOG_PAGE_SIZE)).toBe(1);
    expect(getCatalogTotalPages(41, PUBLIC_CATALOG_PAGE_SIZE)).toBe(3);
  });
});
