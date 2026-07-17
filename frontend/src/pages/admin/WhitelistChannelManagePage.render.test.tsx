import { act, fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import WhitelistChannelManagePage from '@/pages/admin/WhitelistChannelManagePage';
import type { AdminWhitelistChannel } from '@/api/admin';

const mocks = vi.hoisted(() => ({
  fetchChannels: vi.fn(),
  updateChannelStatus: vi.fn(),
  exportChannels: vi.fn(),
}));

vi.mock('@/api/admin', () => ({
  fetchAdminWhitelistChannels: (...args: unknown[]) => mocks.fetchChannels(...args),
  updateAdminWhitelistChannelStatus: (...args: unknown[]) => mocks.updateChannelStatus(...args),
  exportAdminWhitelistChannels: (...args: unknown[]) => mocks.exportChannels(...args),
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
    mocks.updateChannelStatus.mockReset();
    mocks.exportChannels.mockReset();
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

  it('confirms a status change once and keeps the dialog pending until completion', async () => {
    const mutation = deferred<void>();
    mocks.fetchChannels.mockResolvedValue(channelPage(unsafeChannel));
    mocks.updateChannelStatus.mockReturnValueOnce(mutation.promise);

    render(<WhitelistChannelManagePage />);
    await screen.findByText(unsafeChannel.channelName);

    fireEvent.change(screen.getAllByRole('combobox')[1], { target: { value: 'REGISTERED' } });
    fireEvent.click(screen.getByRole('button', { name: '저장' }));

    expect(mocks.updateChannelStatus).not.toHaveBeenCalled();
    const dialog = screen.getByRole('dialog', { name: '채널 상태 변경' });
    const confirmButton = within(dialog).getByRole('button', { name: '변경' });
    fireEvent.click(confirmButton);
    fireEvent.click(confirmButton);

    expect(mocks.updateChannelStatus).toHaveBeenCalledTimes(1);
    expect(mocks.updateChannelStatus).toHaveBeenCalledWith(unsafeChannel.id, {
      status: 'REGISTERED',
      adminNote: undefined,
    });
    expect(confirmButton).toBeDisabled();

    await act(async () => mutation.resolve());
    await waitFor(() => expect(mocks.fetchChannels).toHaveBeenCalledTimes(2));
  });

  it('cancels CSV export without calling the API', async () => {
    mocks.fetchChannels.mockResolvedValue(channelPage(unsafeChannel));

    render(<WhitelistChannelManagePage />);
    await screen.findByText(unsafeChannel.channelName);
    fireEvent.click(screen.getByRole('button', { name: 'CSV 내보내기' }));

    const dialog = screen.getByRole('dialog', { name: '화이트리스트 CSV 내보내기' });
    fireEvent.click(within(dialog).getByRole('button', { name: '취소' }));

    expect(mocks.exportChannels).not.toHaveBeenCalled();
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
  });
});
