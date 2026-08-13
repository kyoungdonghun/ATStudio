import { act, fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import WhitelistChannelPage from '@/pages/subscriber/WhitelistChannelPage';

const fetchWhitelistChannels = vi.fn();
const fetchMySubscription = vi.fn();
const registerChannel = vi.fn();
const updateChannel = vi.fn();
const deleteChannel = vi.fn();
const requestWhitelistRegistration = vi.fn();
const setPrimaryWhitelistChannel = vi.fn();
const mutationMocks = [
  registerChannel,
  updateChannel,
  deleteChannel,
  requestWhitelistRegistration,
  setPrimaryWhitelistChannel,
] as const;

function expectNoMutationCalls() {
  for (const mock of mutationMocks) {
    expect(mock).not.toHaveBeenCalled();
  }
}

function expectOnlyMutationCall(
  expectedMock: (typeof mutationMocks)[number],
  ...expectedArguments: unknown[]
) {
  for (const mock of mutationMocks) {
    if (mock === expectedMock) {
      expect(mock).toHaveBeenCalledTimes(1);
      expect(mock).toHaveBeenCalledWith(...expectedArguments);
    } else {
      expect(mock).not.toHaveBeenCalled();
    }
  }
}

vi.mock('@/api/whitelistChannels', () => ({
  deleteChannel: (...args: unknown[]) => deleteChannel(...args),
  fetchWhitelistChannels: (...args: unknown[]) => fetchWhitelistChannels(...args),
  registerChannel: (...args: unknown[]) => registerChannel(...args),
  requestWhitelistRegistration: (...args: unknown[]) => requestWhitelistRegistration(...args),
  setPrimaryWhitelistChannel: (...args: unknown[]) => setPrimaryWhitelistChannel(...args),
  updateChannel: (...args: unknown[]) => updateChannel(...args),
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

const actionMatrix = [
  {
    status: 'DRAFT',
    actions: ['대표 설정', '등록 요청', '수정', '삭제'],
  },
  {
    status: 'PENDING',
    actions: ['대표 설정', '수정', '삭제'],
  },
  {
    status: 'EXPORTED',
    actions: ['대표 설정', '수정', '해제 요청'],
  },
  {
    status: 'REGISTERED',
    actions: ['대표 설정', '수정', '해제 요청'],
  },
  {
    status: 'REVISION_REQUESTED',
    actions: ['대표 설정', '수정 후 재요청', '수정', '삭제'],
  },
  {
    status: 'REJECTED',
    actions: ['대표 설정', '등록 요청', '수정', '삭제'],
  },
  {
    status: 'CANCELLED',
    actions: ['삭제'],
  },
  {
    status: 'REMOVAL_REQUESTED',
    actions: [],
  },
] as const;

type ChannelStatus = (typeof actionMatrix)[number]['status'];

function channelWithStatus(status: ChannelStatus) {
  return { ...channel, status, primary: false };
}

function mockLoadedChannel(status: ChannelStatus) {
  fetchWhitelistChannels.mockResolvedValue({ dataList: [channelWithStatus(status)] });
  fetchMySubscription.mockResolvedValue(subscription);
}

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
    registerChannel.mockReset();
    updateChannel.mockReset();
    deleteChannel.mockReset();
    requestWhitelistRegistration.mockReset();
    setPrimaryWhitelistChannel.mockReset();
    registerChannel.mockResolvedValue(channel);
    updateChannel.mockResolvedValue(channel);
    deleteChannel.mockResolvedValue(undefined);
    requestWhitelistRegistration.mockResolvedValue(channel);
    setPrimaryWhitelistChannel.mockResolvedValue(channel);
  });

  it.each(actionMatrix)(
    'renders the exact subscriber actions for $status',
    async ({ status, actions }) => {
      mockLoadedChannel(status);

      render(<WhitelistChannelPage />);

      const card = (await screen.findByText(channel.channelName)).closest('article');
      expect(card).not.toBeNull();
      const cardQueries = within(card as HTMLElement);
      const allActionLabels = [
        '대표 설정',
        '등록 요청',
        '수정 후 재요청',
        '수정',
        '삭제',
        '해제 요청',
      ];
      for (const label of allActionLabels) {
        const expected = actions.includes(label as never);
        expect(cardQueries.queryByRole('button', { name: label }) !== null).toBe(expected);
      }
      if (status === 'REMOVAL_REQUESTED') {
        expect(cardQueries.getByText('해제 요청 처리 중')).toBeInTheDocument();
      }
      expectNoMutationCalls();
    },
  );

  it.each(actionMatrix)(
    'proves the primary API call contract for $status',
    async ({ status, actions }) => {
      mockLoadedChannel(status);
      render(<WhitelistChannelPage />);
      const card = (await screen.findByText(channel.channelName)).closest('article');
      const primaryButton = within(card as HTMLElement).queryByRole('button', {
        name: '대표 설정',
      });

      if (!actions.includes('대표 설정' as never)) {
        expect(primaryButton).not.toBeInTheDocument();
        expectNoMutationCalls();
        return;
      }

      fireEvent.click(primaryButton as HTMLElement);

      await waitFor(() => expectOnlyMutationCall(setPrimaryWhitelistChannel, channel.id));
    },
  );

  it.each(actionMatrix)(
    'proves the registration-request API call contract for $status',
    async ({ status, actions }) => {
      mockLoadedChannel(status);
      render(<WhitelistChannelPage />);
      const card = (await screen.findByText(channel.channelName)).closest('article');
      const label = status === 'REVISION_REQUESTED' ? '수정 후 재요청' : '등록 요청';
      const requestButton = within(card as HTMLElement).queryByRole('button', { name: label });

      if (!actions.includes(label as never)) {
        expect(requestButton).not.toBeInTheDocument();
        expectNoMutationCalls();
        return;
      }

      fireEvent.click(requestButton as HTMLElement);

      await waitFor(() => expectOnlyMutationCall(requestWhitelistRegistration, channel.id));
    },
  );

  it.each(actionMatrix)(
    'proves the edit API call contract for $status',
    async ({ status, actions }) => {
      mockLoadedChannel(status);
      render(<WhitelistChannelPage />);
      const card = (await screen.findByText(channel.channelName)).closest('article');
      const editButton = within(card as HTMLElement).queryByRole('button', { name: '수정' });

      if (!actions.includes('수정' as never)) {
        expect(editButton).not.toBeInTheDocument();
        expectNoMutationCalls();
        return;
      }

      fireEvent.click(editButton as HTMLElement);
      const saveButton = screen.getByRole('button', { name: '수정 저장' });
      const requiresConfirmation = ['EXPORTED', 'REGISTERED', 'REVISION_REQUESTED'].includes(
        status,
      );

      if (requiresConfirmation) {
        expect(screen.getByText(/저장하면.*등록 요청.*관리자 재처리/)).toBeInTheDocument();
        fireEvent.click(saveButton);
        expectNoMutationCalls();
        const dialog = screen.getByRole('dialog', { name: '채널 재처리 확인' });
        const confirm = within(dialog).getByRole('button', { name: '저장 후 재처리 요청' });
        fireEvent.click(confirm);
        fireEvent.click(confirm);
      } else {
        fireEvent.click(saveButton);
      }

      await waitFor(() =>
        expectOnlyMutationCall(updateChannel, channel.id, {
          channelName: channel.channelName,
          channelUrl: channel.channelUrl,
          youtubeHandle: channel.youtubeHandle,
          youtubeChannelId: channel.youtubeChannelId,
        }),
      );
    },
  );

  it.each(actionMatrix)(
    'proves the delete or removal API call contract for $status',
    async ({ status, actions }) => {
      mockLoadedChannel(status);
      render(<WhitelistChannelPage />);
      const card = (await screen.findByText(channel.channelName)).closest('article');
      const removalFlow = actions.includes('해제 요청' as never);
      const immediateDelete = actions.includes('삭제' as never);

      if (!removalFlow && !immediateDelete) {
        expect(within(card as HTMLElement).getByText('해제 요청 처리 중')).toBeInTheDocument();
        expectNoMutationCalls();
        return;
      }

      const actionLabel = removalFlow ? '해제 요청' : '삭제';
      fireEvent.click(within(card as HTMLElement).getByRole('button', { name: actionLabel }));
      expectNoMutationCalls();
      const dialog = screen.getByRole('dialog', {
        name: removalFlow ? '등록 해제 요청' : '채널 삭제',
      });
      const confirm = within(dialog).getByRole('button', { name: actionLabel });
      fireEvent.click(confirm);
      fireEvent.click(confirm);

      await waitFor(() => expectOnlyMutationCall(deleteChannel, channel.id));
    },
  );

  it('normalizes whitespace and accepts a valid 255-character YouTube subdomain URL', async () => {
    const url = 'https://studio.youtube.com/'.padEnd(255, 'a');
    fetchWhitelistChannels.mockResolvedValue({ dataList: [] });
    fetchMySubscription.mockResolvedValue(subscription);
    render(<WhitelistChannelPage />);

    fireEvent.change(await screen.findByLabelText('채널명'), { target: { value: '  새 채널  ' } });
    const urlInput = screen.getByLabelText('채널 링크');
    expect(urlInput).toHaveAttribute('maxlength', '255');
    fireEvent.change(urlInput, { target: { value: `  ${url}  ` } });
    fireEvent.click(screen.getByRole('button', { name: '채널 저장' }));

    await waitFor(() => expect(registerChannel).toHaveBeenCalledTimes(1));
    expect(registerChannel).toHaveBeenCalledWith({
      channelName: '새 채널',
      channelUrl: url,
      youtubeHandle: null,
      youtubeChannelId: null,
    });
  });

  it('sends the exact canonical YouTube URL returned by validation', async () => {
    fetchWhitelistChannels.mockResolvedValue({ dataList: [] });
    fetchMySubscription.mockResolvedValue(subscription);
    render(<WhitelistChannelPage />);

    fireEvent.change(await screen.findByLabelText('채널명'), { target: { value: '새 채널' } });
    fireEvent.change(screen.getByLabelText('채널 링크'), {
      target: { value: 'HTTPS://WWW.YOUTUBE.COM:443/@atm' },
    });
    fireEvent.click(screen.getByRole('button', { name: '채널 저장' }));

    await waitFor(() => expect(registerChannel).toHaveBeenCalledTimes(1));
    expect(registerChannel).toHaveBeenCalledWith({
      channelName: '새 채널',
      channelUrl: 'https://www.youtube.com/@atm',
      youtubeHandle: null,
      youtubeChannelId: null,
    });
  });

  it.each([
    'http://youtube.com/@atm',
    'https://user@youtube.com/@atm',
    'https://vimeo.com/@atm',
    'https://youtube.com.evil.test/@atm',
    'https://youtube.com:8443/@atm',
    '//youtube.com/@atm',
    'https:youtube.com/@atm',
    'https:\\youtube.com\\@atm',
  ])('blocks an invalid backend-rejected URL before API invocation: %s', async (url) => {
    fetchWhitelistChannels.mockResolvedValue({ dataList: [] });
    fetchMySubscription.mockResolvedValue(subscription);
    render(<WhitelistChannelPage />);

    fireEvent.change(await screen.findByLabelText('채널명'), { target: { value: '새 채널' } });
    fireEvent.change(screen.getByLabelText('채널 링크'), { target: { value: url } });
    fireEvent.click(screen.getByRole('button', { name: '채널 저장' }));

    expect(await screen.findByText(/YouTube HTTPS 채널 링크/)).toBeInTheDocument();
    expect(registerChannel).not.toHaveBeenCalled();
  });

  it('blocks a 256-character URL before API invocation', async () => {
    const url = 'https://youtube.com/'.padEnd(256, 'a');
    fetchWhitelistChannels.mockResolvedValue({ dataList: [] });
    fetchMySubscription.mockResolvedValue(subscription);
    render(<WhitelistChannelPage />);

    fireEvent.change(await screen.findByLabelText('채널명'), { target: { value: '새 채널' } });
    fireEvent.change(screen.getByLabelText('채널 링크'), { target: { value: url } });
    fireEvent.click(screen.getByRole('button', { name: '채널 저장' }));

    expect(await screen.findByText(/255자/)).toBeInTheDocument();
    expect(registerChannel).not.toHaveBeenCalled();
  });

  it('blocks canonical URL growth beyond 255 characters before API invocation', async () => {
    const url = `https://youtube.com/${'가'.repeat(27)}`;
    expect(url.length).toBeLessThanOrEqual(255);
    expect(new URL(url).href.length).toBeGreaterThan(255);
    fetchWhitelistChannels.mockResolvedValue({ dataList: [] });
    fetchMySubscription.mockResolvedValue(subscription);
    render(<WhitelistChannelPage />);

    fireEvent.change(await screen.findByLabelText('채널명'), { target: { value: '새 채널' } });
    fireEvent.change(screen.getByLabelText('채널 링크'), { target: { value: url } });
    fireEvent.click(screen.getByRole('button', { name: '채널 저장' }));

    expect(await screen.findByText(/255자/)).toBeInTheDocument();
    expect(registerChannel).not.toHaveBeenCalled();
    expect(updateChannel).not.toHaveBeenCalled();
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
    requestWhitelistRegistration.mockImplementationOnce(() => firstMutation.promise);
    setPrimaryWhitelistChannel.mockImplementationOnce(() => secondMutation.promise);

    render(<WhitelistChannelPage />);
    const firstCard = (await screen.findByText(channel.channelName)).closest('article');
    const secondCard = screen.getByText(secondChannel.channelName).closest('article');
    expect(firstCard).not.toBeNull();
    expect(secondCard).not.toBeNull();

    fireEvent.click(within(firstCard as HTMLElement).getByRole('button', { name: '등록 요청' }));
    fireEvent.click(within(secondCard as HTMLElement).getByRole('button', { name: '대표 설정' }));
    await waitFor(() => expect(requestWhitelistRegistration).toHaveBeenCalledTimes(1));
    await waitFor(() => expect(setPrimaryWhitelistChannel).toHaveBeenCalledTimes(1));

    await act(async () => firstMutation.resolve());
    await waitFor(() => expect(fetchWhitelistChannels).toHaveBeenCalledTimes(2));
    await act(async () => secondMutation.resolve());
    expect(fetchWhitelistChannels).toHaveBeenCalledTimes(2);

    await act(async () => firstRefresh.resolve({ dataList: [intermediateChannel] }));

    expect(await screen.findByText('Final server state')).toBeInTheDocument();
    expect(screen.queryByText('Intermediate state')).not.toBeInTheDocument();
    expect(fetchWhitelistChannels).toHaveBeenCalledTimes(3);
  });

  it('confirms channel deletion once and keeps the dialog pending', async () => {
    const mutation = deferred<void>();
    fetchWhitelistChannels.mockResolvedValue({ dataList: [channel] });
    fetchMySubscription.mockResolvedValue(subscription);
    deleteChannel.mockReturnValueOnce(mutation.promise);

    render(<WhitelistChannelPage />);
    const card = (await screen.findByText(channel.channelName)).closest('article');
    expect(card).not.toBeNull();
    fireEvent.click(within(card as HTMLElement).getByRole('button', { name: '삭제' }));

    expect(deleteChannel).not.toHaveBeenCalled();
    const dialog = screen.getByRole('dialog', { name: '채널 삭제' });
    const confirmButton = within(dialog).getByRole('button', { name: '삭제' });
    fireEvent.click(confirmButton);
    fireEvent.click(confirmButton);

    expect(deleteChannel).toHaveBeenCalledTimes(1);
    expect(deleteChannel).toHaveBeenCalledWith(channel.id);
    expect(confirmButton).toBeDisabled();

    await act(async () => mutation.resolve());
    await waitFor(() => expect(fetchWhitelistChannels).toHaveBeenCalledTimes(2));
  });
});
