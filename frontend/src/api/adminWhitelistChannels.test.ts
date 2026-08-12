import { beforeEach, describe, expect, it, vi } from 'vitest';

const { getMock, postMock } = vi.hoisted(() => ({
  getMock: vi.fn(),
  postMock: vi.fn(),
}));

vi.mock('@/api/client', () => ({
  default: {
    get: getMock,
    post: postMock,
  },
}));

import {
  downloadAdminWhitelistExportBatch,
  exportAdminWhitelistChannels,
  fetchRecentAdminWhitelistExports,
} from '@/api/admin';

describe('admin whitelist exports', () => {
  beforeEach(() => {
    getMock.mockReset();
    postMock.mockReset();
  });

  it('posts the explicit recorded filter scope and returns the batch identity', async () => {
    const blob = new Blob(['csv']);
    postMock.mockResolvedValue({
      data: blob,
      headers: {
        'content-disposition': "attachment; filename*=UTF-8''whitelist.csv",
        'x-whitelist-export-batch-id': '77',
      },
    });

    const result = await exportAdminWhitelistChannels({
      status: 'PENDING',
      keyword: 'shorts',
    });

    expect(postMock).toHaveBeenCalledWith(
      '/admin/whitelist-channels/export',
      { status: 'PENDING', keyword: 'shorts' },
      { responseType: 'blob', skipAuthReplay: true },
    );
    expect(result).toEqual({ batchId: 77, blob, fileName: 'whitelist.csv' });
  });

  it.each([undefined, '', 'not-a-number', '1.5', '0', '-1'])(
    'rejects an export response with invalid batch metadata: %s',
    async (batchId) => {
      postMock.mockResolvedValue({
        data: new Blob(['csv']),
        headers: { 'x-whitelist-export-batch-id': batchId },
      });

      await expect(exportAdminWhitelistChannels({ status: 'PENDING' })).rejects.toThrow(
        'invalid batch ID',
      );
    },
  );

  it('downloads a stored immutable batch by ID', async () => {
    const blob = new Blob(['csv']);
    getMock.mockResolvedValue({
      data: blob,
      headers: {
        'content-disposition': 'attachment; filename="whitelist.csv"',
        'x-whitelist-export-batch-id': '77',
      },
    });

    const result = await downloadAdminWhitelistExportBatch(77);

    expect(getMock).toHaveBeenCalledWith('/admin/whitelist-channels/exports/77', {
      responseType: 'blob',
    });
    expect(result.batchId).toBe(77);
    expect(result.blob).toBe(blob);
  });

  it('gets recent summaries for the exact recorded scope without requesting bytes', async () => {
    const dataList = [
      {
        batchId: 77,
        fileName: 'whitelist.csv',
        itemCount: 3,
        status: 'PENDING',
        keyword: 'shorts',
        createdAt: '2026-08-13T12:00:00',
      },
    ];
    getMock.mockResolvedValue({ data: { dataList } });

    const result = await fetchRecentAdminWhitelistExports({
      status: 'PENDING',
      keyword: 'shorts',
    });

    expect(getMock).toHaveBeenCalledWith('/admin/whitelist-channels/exports/recent', {
      params: { status: 'PENDING', keyword: 'shorts' },
    });
    expect(result).toEqual(dataList);
  });

  it('falls back to the requested replay ID when CORS filters the batch header', async () => {
    getMock.mockResolvedValue({ data: new Blob(['csv']), headers: {} });

    const result = await downloadAdminWhitelistExportBatch(77);

    expect(result.batchId).toBe(77);
  });

  it('rejects a replay response whose valid batch header identifies another batch', async () => {
    getMock.mockResolvedValue({
      data: new Blob(['csv']),
      headers: { 'x-whitelist-export-batch-id': '78' },
    });

    await expect(downloadAdminWhitelistExportBatch(77)).rejects.toThrow('invalid batch ID');
  });
});
