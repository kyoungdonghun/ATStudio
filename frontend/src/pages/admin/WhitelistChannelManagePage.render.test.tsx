import { act, fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import WhitelistChannelManagePage from '@/pages/admin/WhitelistChannelManagePage';
import type { AdminWhitelistChannel } from '@/api/admin';

const mocks = vi.hoisted(() => ({
  fetchChannels: vi.fn(),
  fetchRecentExports: vi.fn(),
  updateChannelStatus: vi.fn(),
  exportChannels: vi.fn(),
  downloadBatch: vi.fn(),
}));

vi.mock('@/api/admin', () => ({
  fetchAdminWhitelistChannels: (...args: unknown[]) => mocks.fetchChannels(...args),
  fetchRecentAdminWhitelistExports: (...args: unknown[]) => mocks.fetchRecentExports(...args),
  updateAdminWhitelistChannelStatus: (...args: unknown[]) => mocks.updateChannelStatus(...args),
  exportAdminWhitelistChannels: (...args: unknown[]) => mocks.exportChannels(...args),
  downloadAdminWhitelistExportBatch: (...args: unknown[]) => mocks.downloadBatch(...args),
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
    mocks.fetchRecentExports.mockReset();
    mocks.updateChannelStatus.mockReset();
    mocks.exportChannels.mockReset();
    mocks.downloadBatch.mockReset();
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

  it('clears rows, pagination, and edits when the latest list request fails', async () => {
    mocks.fetchChannels.mockResolvedValueOnce(channelPage(unsafeChannel, 21));
    render(<WhitelistChannelManagePage />);
    await screen.findByText(unsafeChannel.channelName);
    expect(screen.getByRole('button', { name: '2페이지' })).toBeInTheDocument();

    const latest = deferred<ReturnType<typeof channelPage>>();
    mocks.fetchChannels.mockReturnValueOnce(latest.promise);
    fireEvent.change(screen.getAllByRole('combobox')[0], { target: { value: 'EXPORTED' } });
    await waitFor(() => expect(mocks.fetchChannels).toHaveBeenCalledTimes(2));
    await act(async () => latest.reject(new Error('latest failure')));

    expect(
      await screen.findByText('화이트리스트 채널 목록을 불러오지 못했습니다.'),
    ).toBeInTheDocument();
    expect(screen.queryByText(unsafeChannel.channelName)).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '저장' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '2페이지' })).not.toBeInTheDocument();
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

  it('accepts exactly 500 admin-note characters and sends the exact status payload', async () => {
    const note = '가'.repeat(500);
    mocks.fetchChannels.mockResolvedValue(channelPage(unsafeChannel));
    mocks.updateChannelStatus.mockResolvedValue(undefined);

    render(<WhitelistChannelManagePage />);
    await screen.findByText(unsafeChannel.channelName);

    fireEvent.change(screen.getAllByRole('combobox')[1], { target: { value: 'REGISTERED' } });
    const noteInput = screen.getByPlaceholderText('운영자 메모');
    expect(noteInput).toHaveAttribute('maxlength', '500');
    fireEvent.change(noteInput, { target: { value: note } });
    expect(screen.getByText('운영자 메모 500/500')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '저장' }));
    fireEvent.click(within(screen.getByRole('dialog')).getByRole('button', { name: '변경' }));

    await waitFor(() => expect(mocks.updateChannelStatus).toHaveBeenCalledTimes(1));
    expect(mocks.updateChannelStatus).toHaveBeenCalledWith(unsafeChannel.id, {
      status: 'REGISTERED',
      adminNote: note,
    });
  });

  it('blocks a 501-character admin note locally before confirmation or API invocation', async () => {
    mocks.fetchChannels.mockResolvedValue(channelPage(unsafeChannel));

    render(<WhitelistChannelManagePage />);
    await screen.findByText(unsafeChannel.channelName);

    fireEvent.change(screen.getAllByRole('combobox')[1], { target: { value: 'REGISTERED' } });
    fireEvent.change(screen.getByPlaceholderText('운영자 메모'), {
      target: { value: '가'.repeat(501) },
    });
    fireEvent.click(screen.getByRole('button', { name: '저장' }));

    expect(
      screen.getByText('운영자 메모는 최대 500자까지 입력할 수 있습니다.'),
    ).toBeInTheDocument();
    expect(screen.queryByRole('dialog', { name: '채널 상태 변경' })).not.toBeInTheDocument();
    expect(mocks.updateChannelStatus).not.toHaveBeenCalled();
  });

  it('cancels CSV export without calling the API', async () => {
    mocks.fetchChannels.mockResolvedValue(channelPage(unsafeChannel));

    render(<WhitelistChannelManagePage />);
    await screen.findByText(unsafeChannel.channelName);
    fireEvent.click(screen.getByRole('button', { name: 'CSV 내보내기' }));

    const dialog = screen.getByRole('dialog', { name: '화이트리스트 CSV 내보내기' });
    expect(dialog).toHaveTextContent('상태: 등록 요청(PENDING)');
    expect(dialog).toHaveTextContent('모두 외부 처리 중(EXPORTED)으로 전환');
    fireEvent.click(within(dialog).getByRole('button', { name: '취소' }));

    expect(mocks.exportChannels).not.toHaveBeenCalled();
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
  });

  it('builds ALL plus keyword confirmation from applied filters and discloses PENDING mutation', async () => {
    mocks.fetchChannels.mockResolvedValue(channelPage(unsafeChannel));

    render(<WhitelistChannelManagePage />);
    await screen.findByText(unsafeChannel.channelName);

    fireEvent.change(screen.getAllByRole('combobox')[0], { target: { value: '' } });
    const searchInput = screen
      .getAllByRole('textbox')
      .find((input) => input.getAttribute('inputmode') !== 'numeric')!;
    fireEvent.change(searchInput, { target: { value: '  applied scope  ' } });
    fireEvent.click(screen.getByRole('button', { name: '검색' }));
    await waitFor(() =>
      expect(mocks.fetchChannels).toHaveBeenLastCalledWith(
        expect.objectContaining({ status: undefined, keyword: 'applied scope' }),
      ),
    );
    fireEvent.change(searchInput, { target: { value: 'draft scope' } });
    fireEvent.click(screen.getByRole('button', { name: 'CSV 내보내기' }));

    const dialog = screen.getByRole('dialog', { name: '화이트리스트 CSV 내보내기' });
    expect(dialog).toHaveTextContent('상태: 전체');
    expect(dialog).toHaveTextContent('검색어: "applied scope"');
    expect(dialog).toHaveTextContent('등록 요청(PENDING) 채널은 외부 처리 중(EXPORTED)으로 전환');
    expect(dialog).toHaveTextContent('다른 상태는 변경되지 않습니다');
    expect(dialog).not.toHaveTextContent('draft scope');
  });

  it('states explicit non-PENDING scope and its no-status-change effect', async () => {
    mocks.fetchChannels.mockResolvedValue(channelPage({ ...unsafeChannel, status: 'REGISTERED' }));

    render(<WhitelistChannelManagePage />);
    await screen.findByText(unsafeChannel.channelName);
    fireEvent.change(screen.getAllByRole('combobox')[0], { target: { value: 'REGISTERED' } });
    await waitFor(() =>
      expect(mocks.fetchChannels).toHaveBeenLastCalledWith(
        expect.objectContaining({ status: 'REGISTERED' }),
      ),
    );
    fireEvent.click(screen.getByRole('button', { name: 'CSV 내보내기' }));

    const dialog = screen.getByRole('dialog', { name: '화이트리스트 CSV 내보내기' });
    expect(dialog).toHaveTextContent('상태: 등록 완료(REGISTERED)');
    expect(dialog).toHaveTextContent('검색어: 적용 안 함');
    expect(dialog).toHaveTextContent('채널 상태는 변경되지 않습니다');
  });

  it('recovers an ambiguous export with one exact-scope GET and explicit batch replay only', async () => {
    mocks.fetchChannels.mockResolvedValue(channelPage(unsafeChannel));
    mocks.exportChannels.mockRejectedValueOnce(new Error('response lost'));
    mocks.fetchRecentExports.mockResolvedValueOnce([
      {
        batchId: 77,
        fileName: 'whitelist-channels.csv',
        itemCount: 3,
        status: 'PENDING',
        keyword: null,
        createdAt: '2026-08-13T12:00:00',
      },
    ]);
    mocks.downloadBatch.mockResolvedValueOnce({
      batchId: 77,
      blob: new Blob(['csv']),
      fileName: 'whitelist-channels.csv',
    });

    render(<WhitelistChannelManagePage />);
    await screen.findByText(unsafeChannel.channelName);
    fireEvent.click(screen.getByRole('button', { name: 'CSV 내보내기' }));
    fireEvent.click(within(screen.getByRole('dialog')).getByRole('button', { name: '내보내기' }));

    expect(await screen.findByText(/내보내기 결과를 확인할 수 없습니다/)).toBeInTheDocument();
    expect(mocks.exportChannels).toHaveBeenCalledTimes(1);
    expect(mocks.fetchRecentExports).toHaveBeenCalledTimes(1);
    expect(mocks.fetchRecentExports).toHaveBeenCalledWith({
      status: 'PENDING',
      keyword: undefined,
    });
    expect(screen.queryByText(unsafeChannel.channelName)).not.toBeInTheDocument();
    expect(screen.getByText('whitelist-channels.csv')).toBeInTheDocument();
    expect(screen.getByText('3건')).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: 'Batch 77 다시 받기' }));
    await waitFor(() => expect(mocks.downloadBatch).toHaveBeenCalledWith(77));
    expect(mocks.exportChannels).toHaveBeenCalledTimes(1);
  });

  it('treats a definitive 4xx export rejection as a normal failure without recovery claims', async () => {
    mocks.fetchChannels.mockResolvedValue(channelPage(unsafeChannel));
    mocks.exportChannels.mockRejectedValueOnce({ response: { status: 400 } });

    render(<WhitelistChannelManagePage />);
    await screen.findByText(unsafeChannel.channelName);
    fireEvent.click(screen.getByRole('button', { name: 'CSV 내보내기' }));
    fireEvent.click(within(screen.getByRole('dialog')).getByRole('button', { name: '내보내기' }));

    expect(await screen.findByText('CSV export에 실패했습니다.')).toBeInTheDocument();
    expect(mocks.fetchRecentExports).not.toHaveBeenCalled();
    expect(screen.queryByText(/결과를 확인할 수 없습니다/)).not.toBeInTheDocument();
  });
});
