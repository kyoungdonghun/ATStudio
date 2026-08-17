import { beforeEach, describe, expect, it, vi } from 'vitest';

const { postMock } = vi.hoisted(() => ({
  postMock: vi.fn(),
}));

vi.mock('@/api/client', () => ({
  default: {
    post: postMock,
  },
}));

import { logoutSession } from '@/api/auth';

describe('logoutSession', () => {
  beforeEach(() => {
    postMock.mockReset();
  });

  it('posts an authenticated bodyless logout request', async () => {
    postMock.mockResolvedValue({ status: 204 });

    await expect(logoutSession()).resolves.toBe('confirmed');

    expect(postMock).toHaveBeenCalledWith('/auth/logout');
  });

  it('treats every non-204 response as an unconfirmed logout', async () => {
    postMock.mockResolvedValue({ status: 200 });

    await expect(logoutSession()).resolves.toBe('unconfirmed');
  });

  it('treats a bare 401 response as an unconfirmed logout', async () => {
    postMock.mockRejectedValue(
      Object.assign(new Error('unauthorized'), {
        isAxiosError: true,
        response: { status: 401 },
      }),
    );

    await expect(logoutSession()).resolves.toBe('unconfirmed');
  });

  it('treats transient failures as an unconfirmed logout', async () => {
    const networkError = new Error('network unavailable');
    postMock.mockRejectedValue(networkError);

    await expect(logoutSession()).resolves.toBe('unconfirmed');
  });
});
