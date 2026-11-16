import { afterEach, describe, expect, it, vi } from 'vitest';
import { reloadCurrentDocument } from '@/router/reloadCurrentDocument';

describe('current document reload', () => {
  afterEach(() => vi.unstubAllGlobals());

  it('uses a native reload without rewriting the URL or clearing authentication and work storage', () => {
    const location = {
      href: 'https://atmusic.co.kr/admin/tracks/12/edit?from=list#tags',
      reload: vi.fn(),
    };
    const localStorage = { clear: vi.fn(), removeItem: vi.fn() };
    const sessionStorage = { clear: vi.fn(), removeItem: vi.fn() };
    vi.stubGlobal('window', { location, localStorage, sessionStorage });

    reloadCurrentDocument();

    expect(location.reload).toHaveBeenCalledExactlyOnceWith();
    expect(location.href).toBe('https://atmusic.co.kr/admin/tracks/12/edit?from=list#tags');
    expect(localStorage.clear).not.toHaveBeenCalled();
    expect(localStorage.removeItem).not.toHaveBeenCalled();
    expect(sessionStorage.clear).not.toHaveBeenCalled();
    expect(sessionStorage.removeItem).not.toHaveBeenCalled();
  });
});
