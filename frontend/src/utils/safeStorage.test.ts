import { afterEach, describe, expect, it, vi } from 'vitest';

import { safeSessionStorage, safeStorage } from '@/utils/safeStorage';

describe('safe browser storage wrappers', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('reads, writes, removes, and clears local storage', () => {
    expect(safeStorage.setItem('key', 'value')).toBe(true);
    expect(safeStorage.getItem('key')).toBe('value');
    safeStorage.removeItem('key');
    expect(safeStorage.getItem('key')).toBeNull();
    localStorage.setItem('other', 'value');
    safeStorage.clear();
    expect(localStorage.length).toBe(0);
  });

  it('turns local storage access failures into safe results', () => {
    vi.spyOn(Storage.prototype, 'getItem').mockImplementation(() => {
      throw new DOMException('blocked');
    });
    expect(safeStorage.getItem('key')).toBeNull();
    vi.restoreAllMocks();
    vi.spyOn(Storage.prototype, 'setItem').mockImplementation(() => {
      throw new DOMException('quota');
    });
    expect(safeStorage.setItem('key', 'value')).toBe(false);
    vi.restoreAllMocks();
    vi.spyOn(Storage.prototype, 'removeItem').mockImplementation(() => {
      throw new DOMException('blocked');
    });
    expect(() => safeStorage.removeItem('key')).not.toThrow();
    vi.restoreAllMocks();
    vi.spyOn(Storage.prototype, 'clear').mockImplementation(() => {
      throw new DOMException('blocked');
    });
    expect(() => safeStorage.clear()).not.toThrow();
  });

  it('reads, writes, and removes session storage', () => {
    expect(safeSessionStorage.setItem('key', 'value')).toBe(true);
    expect(safeSessionStorage.getItem('key')).toBe('value');
    safeSessionStorage.removeItem('key');
    expect(safeSessionStorage.getItem('key')).toBeNull();
  });

  it('turns session storage failures into safe results', () => {
    vi.spyOn(Storage.prototype, 'getItem').mockImplementation(() => {
      throw new DOMException('blocked');
    });
    expect(safeSessionStorage.getItem('key')).toBeNull();
    vi.restoreAllMocks();
    vi.spyOn(Storage.prototype, 'setItem').mockImplementation(() => {
      throw new DOMException('quota');
    });
    expect(safeSessionStorage.setItem('key', 'value')).toBe(false);
    vi.restoreAllMocks();
    vi.spyOn(Storage.prototype, 'removeItem').mockImplementation(() => {
      throw new DOMException('blocked');
    });
    expect(() => safeSessionStorage.removeItem('key')).not.toThrow();
  });
});
