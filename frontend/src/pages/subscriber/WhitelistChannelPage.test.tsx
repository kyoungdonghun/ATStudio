import { act, fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import WhitelistChannelPage from '@/pages/subscriber/WhitelistChannelPage';

const fetchWhitelistChannels = vi.fn();
const fetchMySubscription = vi.fn();
const deleteChannel = vi.fn();

vi.mock('@/api/whitelistChannels', () => ({
  deleteChannel: (...args: unknown[]) => deleteChannel(...args),
  fetchWhitelistChannels: (...args: unknown[]) => fetchWhitelistChannels(...args),
  registerChannel: vi.fn(),
  requestWhitelistRegistration: vi.fn(),
  setPrimaryWhitelistChannel: vi.fn(),
  updateChannel: vi.fn(),
}));

vi.mock('@/api/userSubscriptions', async () => {
  const actual =
    await vi.importActual<typeof import('@/api/userSubscriptions')>('@/api/userSubscriptions');
  return {
    ...actual,
    fetchMySubscription: (...args: unknown[]) => fetchMySubscription(...args),
  };
});

const channel = {
  id: 1,
  channelUrl: 'https://www.youtube.com/@atm',
  channelName: 'AT.M Shorts',
  youtubeHandle: '@atm',
  youtubeChannelId: 'UC1234567890123456789012',
  status: 'DRAFT',
  primary: true,
  adminNote: null,
  requestedAt: null,
  exportedAt: null,
  processedAt: null,
  removalRequestedAt: null,
  createdAt: '2026-07-16T09:00:00',
};

const subscription = {
  id: 1,
  subscription: { maxWhitelistChannels: 3 },
};

function deferred<T>() {
  let resolve!: (value: T) => void;
  const promise = new Promise<T>((resolvePromise) => {
    resolve = resolvePromise;
  });
  return { promise, resolve };
}

describe('WhitelistChannelPage load states', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    fetchWhitelistChannels.mockReset();
    fetchMySubscription.mockReset();
    deleteChannel.mockReset();
  });

  it('retries one failed channel load and preserves the whitelist data state', async () => {
    fetchWhitelistChannels
      .mockRejectedValueOnce({ response: { status: 500 } })
      .mockResolvedValueOnce({ dataList: [channel] });
    fetchMySubscription.mockResolvedValue(subscription);

    render(<WhitelistChannelPage />);

    expect(await screen.findByRole('alert')).toHaveTextContent('서버 오류');
    const retryButton = screen.getByRole('button', { name: '다시 시도' });
    fireEvent.click(retryButton);
    fireEvent.click(retryButton);

    expect(retryButton).toBeDisabled();
    expect(fetchWhitelistChannels).toHaveBeenCalledTimes(2);
    await waitFor(() => {
      expect(screen.getByText('AT.M Shorts')).toBeInTheDocument();
    });
    expect(screen.getByText('0/3')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('예: @your_channel')).toBeInTheDocument();
    expect(
      screen.getByPlaceholderText('예: https://www.youtube.com/@your_channel'),
    ).toBeInTheDocument();
  });

  it('keeps approved no-subscription separate from a legitimate empty channel list', async () => {
    fetchWhitelistChannels.mockResolvedValue({ dataList: [] });
    fetchMySubscription.mockRejectedValue({
      response: {
        status: 403,
        data: { errorCode: 'NO_ACTIVE_SUBSCRIPTION' },
      },
    });

    render(<WhitelistChannelPage />);

    expect(await screen.findByText('저장된 채널이 없습니다.')).toBeInTheDocument();
    expect(screen.getByText('구독 필요')).toBeInTheDocument();
    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
  });

  it('does not reduce a subscription-service failure to no subscription', async () => {
    fetchWhitelistChannels.mockResolvedValue({ dataList: [] });
    fetchMySubscription.mockRejectedValue({ response: { status: 500 } });

    render(<WhitelistChannelPage />);

    expect(await screen.findByRole('alert')).toHaveTextContent('서버 오류');
    expect(screen.queryByText('구독 필요')).not.toBeInTheDocument();
    expect(screen.queryByText('저장된 채널이 없습니다.')).not.toBeInTheDocument();
  });

  it('renders an unsafe persisted channel URL as text instead of a link', async () => {
    const unsafeUrl = 'javascript://youtube.com/%0Aalert(1)';
    fetchWhitelistChannels.mockResolvedValue({
      dataList: [{ ...channel, channelUrl: unsafeUrl }],
    });
    fetchMySubscription.mockResolvedValue(subscription);

    render(<WhitelistChannelPage />);

    expect(await screen.findByText(unsafeUrl)).toBeInTheDocument();
    expect(screen.queryByRole('link', { name: unsafeUrl })).not.toBeInTheDocument();
  });

  it('queues a final refresh when overlapping mutations finish during an active refresh', async () => {
    const secondChannel = {
      ...channel,
      id: 2,
      channelName: 'Second channel',
      channelUrl: 'https://www.youtube.com/@second',
      primary: false,
    };
    const intermediateChannel = { ...channel, channelName: 'Intermediate state' };
    const finalChannel = { ...channel, channelName: 'Final server state' };
    const firstRefresh = deferred<{ dataList: (typeof channel)[] }>();
    const firstMutation = deferred<void>();
    const secondMutation = deferred<void>();

    fetchWhitelistChannels
      .mockResolvedValueOnce({ dataList: [channel, secondChannel] })
      .mockImplementationOnce(() => firstRefresh.promise)
      .mockResolvedValueOnce({ dataList: [finalChannel] });
    fetchMySubscription.mockResolvedValue(subscription);
    deleteChannel
      .mockImplementationOnce(() => firstMutation.promise)
      .mockImplementationOnce(() => secondMutation.promise);
    vi.spyOn(window, 'confirm').mockReturnValue(true);

    render(<WhitelistChannelPage />);
    const firstCard = (await screen.findByText(channel.channelName)).closest('article');
    const secondCard = screen.getByText(secondChannel.channelName).closest('article');
    expect(firstCard).not.toBeNull();
    expect(secondCard).not.toBeNull();

    const firstButtons = within(firstCard as HTMLElement).getAllByRole('button');
    const secondButtons = within(secondCard as HTMLElement).getAllByRole('button');
    fireEvent.click(firstButtons[firstButtons.length - 1]);
    fireEvent.click(secondButtons[secondButtons.length - 1]);
    await waitFor(() => expect(deleteChannel).toHaveBeenCalledTimes(2));

    await act(async () => firstMutation.resolve());
    await waitFor(() => expect(fetchWhitelistChannels).toHaveBeenCalledTimes(2));
    await act(async () => secondMutation.resolve());
    expect(fetchWhitelistChannels).toHaveBeenCalledTimes(2);

    await act(async () => firstRefresh.resolve({ dataList: [intermediateChannel] }));

    expect(await screen.findByText('Final server state')).toBeInTheDocument();
    expect(screen.queryByText('Intermediate state')).not.toBeInTheDocument();
    expect(fetchWhitelistChannels).toHaveBeenCalledTimes(3);
  });
});
