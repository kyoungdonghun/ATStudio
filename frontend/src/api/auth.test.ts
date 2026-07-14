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
    postMock.mockResolvedValue({});

    await logoutSession();

    expect(postMock).toHaveBeenCalledWith('/auth/logout');
  });

  it('treats a confirmed invalid session as logout success', async () => {
    postMock.mockRejectedValue(
      Object.assign(new Error('unauthorized'), {
        isAxiosError: true,
        response: { status: 401 },
      }),
    );

    await expect(logoutSession()).resolves.toBeUndefined();
  });

  it('keeps transient failures observable to the store', async () => {
    const networkError = new Error('network unavailable');
    postMock.mockRejectedValue(networkError);

    await expect(logoutSession()).rejects.toBe(networkError);
  });
});
