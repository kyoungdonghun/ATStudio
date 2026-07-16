import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import WhitelistChannelManagePage from '@/pages/admin/WhitelistChannelManagePage';
import type { AdminWhitelistChannel } from '@/api/admin';

const mocks = vi.hoisted(() => ({
  fetchChannels: vi.fn(),
}));

vi.mock('@/api/admin', () => ({
  fetchAdminWhitelistChannels: (...args: unknown[]) => mocks.fetchChannels(...args),
  updateAdminWhitelistChannelStatus: vi.fn(),
  exportAdminWhitelistChannels: vi.fn(),
  downloadAdminWhitelistExportBatch: vi.fn(),
}));

const unsafeChannel: AdminWhitelistChannel = {
  id: 1,
  userId: 2,
  userEmail: 'channel@example.com',
  userNickname: 'channel-owner',
  channelUrl: 'javascript:alert(document.domain)',
  channelName: 'Retained unsafe channel',
  youtubeHandle: '@unsafe',
  youtubeChannelId: null,
  status: 'PENDING',
  primary: false,
  adminNote: null,
  processedByEmail: null,
  planName: 'STANDARD',
  billingCycle: 'MONTHLY',
  requestedAt: '2026-07-16T00:00:00',
  exportedAt: null,
  processedAt: null,
  removalRequestedAt: null,
  createdAt: '2026-07-16T00:00:00',
};

const pageInfo = (total: number) => ({
  page: 1,
  size: 20,
  total,
  start: total > 0 ? 1 : 0,
  end: Math.min(total, 20),
  prev: false,
  next: total > 20,
});

function channelPage(channel: AdminWhitelistChannel, total = 1) {
  return { dataList: [channel], pageInfo: pageInfo(total) };
}

function deferred<T>() {
  let resolve!: (value: T) => void;
  let reject!: (reason?: unknown) => void;
  const promise = new Promise<T>((resolvePromise, rejectPromise) => {
    resolve = resolvePromise;
    reject = rejectPromise;
  });
  return { promise, reject, resolve };
}

describe('WhitelistChannelManagePage persisted URL defense', () => {
  beforeEach(() => {
    mocks.fetchChannels.mockReset();
  });

  it('renders an unsafe retained channel URL as text instead of an anchor', async () => {
    mocks.fetchChannels.mockResolvedValue(channelPage(unsafeChannel));

    render(<WhitelistChannelManagePage />);

    const renderedUrl = await screen.findByText(unsafeChannel.channelUrl);
    expect(renderedUrl.tagName).toBe('SPAN');
    expect(screen.queryByRole('link', { name: unsafeChannel.channelUrl })).not.toBeInTheDocument();
  });

  it('keeps rows, pagination, and editable status from the latest list request', async () => {
    mocks.fetchChannels.mockResolvedValueOnce(channelPage(unsafeChannel));
    render(<WhitelistChannelManagePage />);
    await screen.findByText(unsafeChannel.channelName);

    const stale = deferred<ReturnType<typeof channelPage>>();
    const latest = deferred<ReturnType<typeof channelPage>>();
    mocks.fetchChannels
      .mockImplementationOnce(() => stale.promise)
      .mockImplementationOnce(() => latest.promise);

    const statusFilter = screen.getAllByRole('combobox')[0];
    fireEvent.change(statusFilter, { target: { value: 'EXPORTED' } });
    await waitFor(() => expect(mocks.fetchChannels).toHaveBeenCalledTimes(2));
    fireEvent.change(statusFilter, { target: { value: 'REGISTERED' } });
    await waitFor(() => expect(mocks.fetchChannels).toHaveBeenCalledTimes(3));

    const latestChannel = {
      ...unsafeChannel,
      id: 3,
      channelName: 'Latest channel',
      channelUrl: 'https://www.youtube.com/@latest',
      status: 'REGISTERED' as const,
    };
    await act(async () => latest.resolve(channelPage(latestChannel)));
    expect(await screen.findByText('Latest channel')).toBeInTheDocument();

    const staleChannel = {
      ...unsafeChannel,
      id: 2,
      channelName: 'Stale channel',
      status: 'EXPORTED' as const,
    };
    await act(async () => stale.resolve(channelPage(staleChannel, 21)));

    expect(screen.queryByText('Stale channel')).not.toBeInTheDocument();
    expect(screen.getAllByRole('combobox')[1]).toHaveValue('REGISTERED');
    expect(screen.queryByRole('button', { name: '2' })).not.toBeInTheDocument();
  });

  it('ignores stale failure and finally while the latest request is pending', async () => {
    mocks.fetchChannels.mockResolvedValueOnce(channelPage(unsafeChannel));
    const { container } = render(<WhitelistChannelManagePage />);
    await screen.findByText(unsafeChannel.channelName);

    const stale = deferred<ReturnType<typeof channelPage>>();
    const latest = deferred<ReturnType<typeof channelPage>>();
    mocks.fetchChannels
      .mockImplementationOnce(() => stale.promise)
      .mockImplementationOnce(() => latest.promise);

    const statusFilter = screen.getAllByRole('combobox')[0];
    fireEvent.change(statusFilter, { target: { value: 'EXPORTED' } });
    await waitFor(() => expect(mocks.fetchChannels).toHaveBeenCalledTimes(2));
    fireEvent.change(statusFilter, { target: { value: 'REGISTERED' } });
    await waitFor(() => expect(mocks.fetchChannels).toHaveBeenCalledTimes(3));

    await act(async () => stale.reject(new Error('stale failure')));
    expect(screen.getByText('Loading...')).toBeInTheDocument();
    expect(container.querySelector('[class*="error"]')).toBeNull();

    await act(async () => latest.resolve(channelPage(unsafeChannel)));
    expect(await screen.findByText(unsafeChannel.channelName)).toBeInTheDocument();
  });
});
