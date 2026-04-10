/**
 * Safe storage wrappers — tolerate iOS Safari private mode, Safari ITP,
 * disabled cookies/storage, and quota exceeded errors.
 *
 * Direct localStorage / sessionStorage access can throw on access,
 * not just on setItem. Always use these wrappers.
 */

export const safeStorage = {
  getItem(key: string): string | null {
    try {
      return localStorage.getItem(key);
    } catch {
      return null;
    }
  },
  setItem(key: string, value: string): boolean {
    try {
      localStorage.setItem(key, value);
      return true;
    } catch {
      return false;
    }
  },
  removeItem(key: string): void {
    try {
      localStorage.removeItem(key);
    } catch {
      // ignore
    }
  },
  clear(): void {
    try {
      localStorage.clear();
    } catch {
      // ignore
    }
  },
};

export const safeSessionStorage = {
  getItem(key: string): string | null {
    try {
      return sessionStorage.getItem(key);
    } catch {
      return null;
    }
  },
  setItem(key: string, value: string): boolean {
    try {
      sessionStorage.setItem(key, value);
      return true;
    } catch {
      return false;
    }
  },
  removeItem(key: string): void {
    try {
      sessionStorage.removeItem(key);
    } catch {
      // ignore
    }
  },
};
