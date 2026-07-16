import { describe, expect, it } from 'vitest';
import { classifyLoadError, getApiErrorCode, getLoadErrorMessage } from '@/api/loadError';

describe('load error classification', () => {
  it.each([
    [{ code: 'ERR_CANCELED' }, 'cancelled'],
    [{ response: { status: 401 } }, 'unauthorized'],
    [{ response: { status: 403 } }, 'forbidden'],
    [{ response: { status: 404 } }, 'not-found'],
    [{ response: { status: 422 } }, 'validation'],
    [{ response: { status: 500 } }, 'server'],
    [{ code: 'ECONNABORTED' }, 'network'],
  ] as const)('classifies %o as %s', (error, expected) => {
    expect(classifyLoadError(error)).toBe(expected);
  });

  it('extracts a structured API error code without guessing from status', () => {
    expect(
      getApiErrorCode({
        response: { status: 403, data: { errorCode: 'NO_ACTIVE_SUBSCRIPTION' } },
      }),
    ).toBe('NO_ACTIVE_SUBSCRIPTION');
    expect(getApiErrorCode({ response: { status: 403 } })).toBeNull();
  });

  it('returns a Korean retry message for infrastructure failures', () => {
    expect(getLoadErrorMessage({ response: { status: 500 } }, '공지사항')).toContain('서버 오류');
    expect(getLoadErrorMessage({ code: 'ETIMEDOUT' }, '공지사항')).toContain('네트워크 연결');
  });
});
