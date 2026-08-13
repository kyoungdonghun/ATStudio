import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('@/api/client', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}));

import client from '@/api/client';
import {
  createNotice,
  deleteNotice,
  downloadNoticeAttachment,
  fetchAdminNotice,
  fetchNotice,
  updateNotice,
} from '@/api/notices';

const mockedClient = vi.mocked(client);

describe('Notice API ownership contracts', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('separates the counting public read from the non-counting ADMIN edit read', async () => {
    const controller = new AbortController();
    const publicNotice = { id: 7, title: 'Public' };
    const adminNotice = { title: 'Edit', content: 'Body', isPinned: false, attachments: [] };
    mockedClient.get
      .mockResolvedValueOnce({ data: { data: publicNotice } })
      .mockResolvedValueOnce({ data: { data: adminNotice } });

    await expect(fetchNotice(7, controller.signal)).resolves.toEqual(publicNotice);
    await expect(fetchAdminNotice(7, controller.signal)).resolves.toEqual(adminNotice);

    expect(mockedClient.get).toHaveBeenNthCalledWith(1, '/notices/7', {
      signal: controller.signal,
    });
    expect(mockedClient.get).toHaveBeenNthCalledWith(2, '/notices/7/admin', {
      signal: controller.signal,
    });
  });

  it('returns attachment bytes without invoking a browser download side effect', async () => {
    const controller = new AbortController();
    const blob = new Blob(['notice']);
    mockedClient.get.mockResolvedValueOnce({ data: blob });

    await expect(downloadNoticeAttachment(7, 4, controller.signal)).resolves.toBe(blob);
    expect(mockedClient.get).toHaveBeenCalledWith('/notices/7/attachments/4', {
      responseType: 'blob',
      signal: controller.signal,
    });
  });

  it('passes abort ownership through every ADMIN mutation', async () => {
    const controller = new AbortController();
    const response = { id: 7, title: 'Notice' };
    mockedClient.post.mockResolvedValueOnce({ data: { data: response } });
    mockedClient.put.mockResolvedValueOnce({ data: { data: response } });
    mockedClient.delete.mockResolvedValueOnce({});

    await createNotice({ title: 'Notice', content: 'Body', isPinned: false }, controller.signal);
    await updateNotice(7, { title: 'Updated', content: 'Body', isPinned: true }, controller.signal);
    await deleteNotice(7, controller.signal);

    expect(mockedClient.post).toHaveBeenCalledWith('/notices', expect.any(FormData), {
      timeout: 60_000,
      signal: controller.signal,
    });
    expect(mockedClient.put).toHaveBeenCalledWith('/notices/7', expect.any(FormData), {
      timeout: 60_000,
      signal: controller.signal,
    });
    expect(mockedClient.delete).toHaveBeenCalledWith('/notices/7', {
      signal: controller.signal,
    });
  });
});
