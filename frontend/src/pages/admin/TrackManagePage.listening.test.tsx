import { act, cleanup, fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { createMemoryRouter, RouterProvider } from 'react-router-dom';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import type { AudioProcessing } from '@/api/tracks';

const mocks = vi.hoisted(() => ({
  list: vi.fn(),
  catalog: vi.fn(),
  change: vi.fn(),
  reconcile: vi.fn(),
  processing: vi.fn(),
}));
vi.mock('@/config/appMode', () => ({ isListeningMode: true }));
vi.mock('@/api/tracks', () => ({
  fetchAdminTracks: (...args: unknown[]) => mocks.list(...args),
  fetchListeningCatalog: (...args: unknown[]) => mocks.catalog(...args),
  changeListeningCatalog: (...args: unknown[]) => mocks.change(...args),
  reconcileListeningCatalog: (...args: unknown[]) => mocks.reconcile(...args),
  fetchAudioProcessing: (...args: unknown[]) => mocks.processing(...args),
}));
vi.mock('@/hooks/useAdminTrackThumbnail', () => ({ useAdminTrackThumbnail: () => null }));
vi.mock('@/store/authStore', () => ({
  getAuthSessionGeneration: () => 1,
  useAuthStore: Object.assign(
    (selector: (state: unknown) => unknown) =>
      selector({ user: { id: 1 }, accessToken: 'fixture' }),
    { getState: () => ({ user: { id: 1 }, accessToken: 'fixture' }) },
  ),
}));
vi.mock('@/store/playerStore', () => ({
  usePlayerStore: {
    getState: () => ({ currentTrack: null, queue: [], trackListContext: [] }),
    setState: vi.fn(),
  },
}));
import TrackManagePage from './TrackManagePage';

const readyAudio: AudioProcessing = {
  trackId: 13,
  state: 'READY',
  generation: 1,
  streamReady: true,
  retryAllowed: false,
  attemptCount: 1,
  errorCode: null,
  updatedAt: null,
};
const track = {
  id: 13,
  title: 'Client song',
  artistName: 'Client',
  duration: 120,
  bpm: 75,
  tonality: 'Gm',
  thumbnail: null,
  playCount: 0,
  likeCount: 0,
  downloadCount: 0,
  isActive: false,
  tags: [],
  createdAt: '2026-11-16T00:00:00Z',
  audioProcessing: readyAudio,
};
const pageInfo = { page: 1, size: 20, total: 1, start: 1, end: 1, prev: false, next: false };
const initial = { source: 'DATABASE', revision: 6, trackIds: [], digest: 'before' };
const published = { ...initial, revision: 7, trackIds: [13], digest: 'after' };
function renderPage() {
  const router = createMemoryRouter(
    [{ path: '/admin/track-manage', element: <TrackManagePage /> }],
    { initialEntries: ['/admin/track-manage'] },
  );
  render(<RouterProvider router={router} />);
  return router;
}
beforeEach(() => {
  sessionStorage.clear();
  mocks.list.mockReset().mockResolvedValue({ dataList: [track], pageInfo });
  mocks.catalog.mockReset().mockResolvedValue(initial);
  mocks.change.mockReset();
  mocks.reconcile.mockReset();
  mocks.processing.mockReset();
});
afterEach(() => {
  cleanup();
  vi.useRealTimers();
});

describe('single-click listening publication', () => {
  it('publishes an inactive READY target in one request without a modal or invented credit attestation, then refreshes active state', async () => {
    mocks.change.mockResolvedValue(published);
    renderPage();
    const publish = await screen.findByRole('button', { name: '공개' });
    expect(publish).toBeEnabled();
    expect(screen.getByText(/공개 버튼을 누르면 누구나 로그인 없이/)).toBeVisible();
    expect(screen.queryByRole('checkbox')).toBeNull();
    expect(screen.queryByRole('dialog')).toBeNull();
    expect(screen.queryByRole('button', { name: '삭제' })).toBeNull();
    mocks.list.mockResolvedValue({ dataList: [{ ...track, isActive: true }], pageInfo });
    fireEvent.click(publish);
    await screen.findByRole('button', { name: '공개해제' });
    expect(mocks.change).toHaveBeenCalledTimes(1);
    expect(mocks.change).toHaveBeenCalledWith(
      expect.objectContaining({
        expectedRevision: 6,
        trackIds: [13],
        creditsConfirmed: false,
        targetChange: { trackId: 13, publish: true },
      }),
    );
    expect(
      within(screen.getByRole('row', { name: /Client song/ })).getByText('활성'),
    ).toBeVisible();
    expect(mocks.list).toHaveBeenCalledTimes(2);
    expect(screen.getByText(/revision 7/)).not.toBeVisible();
    expect(screen.queryByRole('link', { name: '수정' })).toBeNull();
  });

  it.each([
    ['PENDING', true, '변환 완료 후 공개할 수 있습니다.'],
    ['PROCESSING', true, '변환 완료 후 공개할 수 있습니다.'],
    ['FAILED', true, '음원 변환 상태를 확인해 주세요.'],
    ['READY', false, '재생 파일 준비 후 공개할 수 있습니다.'],
  ] as const)(
    'blocks %s even when an old stream exists and explains the readiness condition',
    async (state, streamReady, message) => {
      mocks.list.mockResolvedValue({
        dataList: [{ ...track, audioProcessing: { ...readyAudio, state, streamReady } }],
        pageInfo,
      });
      renderPage();
      const publish = await screen.findByRole('button', { name: '공개' });
      expect(publish).toBeDisabled();
      expect(screen.getByText(message)).toBeVisible();
      fireEvent.click(publish);
      expect(mocks.change).not.toHaveBeenCalled();
    },
  );

  it('allows readiness polling to enable the button without a page reload', async () => {
    vi.useFakeTimers();
    mocks.list.mockResolvedValue({
      dataList: [{ ...track, audioProcessing: { ...readyAudio, state: 'PROCESSING' } }],
      pageInfo,
    });
    mocks.processing.mockResolvedValue(readyAudio);
    renderPage();
    await act(async () => undefined);
    expect(screen.getByRole('button', { name: '공개' })).toBeDisabled();
    await act(() => vi.advanceTimersByTimeAsync(3000));
    expect(screen.getByRole('button', { name: '공개' })).toBeEnabled();
    expect(mocks.change).not.toHaveBeenCalled();
  });

  it('unpublishes with one click even if the selected stream is no longer ready', async () => {
    mocks.catalog.mockResolvedValue(published);
    mocks.list.mockResolvedValue({
      dataList: [{ ...track, audioProcessing: { ...readyAudio, state: 'FAILED' } }],
      pageInfo,
    });
    mocks.change.mockResolvedValue({ ...initial, revision: 8 });
    renderPage();
    fireEvent.click(await screen.findByRole('button', { name: '공개해제' }));
    await waitFor(() => expect(mocks.list).toHaveBeenCalledTimes(2));
    expect(mocks.change).toHaveBeenCalledWith(
      expect.objectContaining({
        trackIds: [],
        targetChange: { trackId: 13, publish: false },
      }),
    );
    expect(screen.getByRole('link', { name: '수정' })).toBeVisible();
  });

  it('holds unknown results across UI clicks and refresh without retry or a success claim', async () => {
    mocks.change.mockRejectedValue(new Error('response lost'));
    renderPage();
    fireEvent.click(await screen.findByRole('button', { name: '공개' }));
    await screen.findByText('이전 변경 결과를 확인해야 합니다.');
    const publish = screen.getByRole('button', { name: '공개' });
    expect(publish).toBeDisabled();
    fireEvent.click(publish);
    fireEvent.click(screen.getByRole('button', { name: '목록 새로고침' }));
    await waitFor(() => expect(mocks.catalog).toHaveBeenCalledTimes(3));
    expect(mocks.change).toHaveBeenCalledTimes(1);
    expect(screen.queryByText('공개했습니다. 이제 로그인 없이 들을 수 있습니다.')).toBeNull();
    expect(sessionStorage.getItem('listening-catalog-hold:operator:1')).not.toBeNull();
  });

  it('does not repeat a pending click and refreshes the latest filter after the commit', async () => {
    let resolve!: (value: typeof published) => void;
    mocks.change.mockReturnValue(
      new Promise((done) => {
        resolve = done;
      }),
    );
    const router = renderPage();
    const button = await screen.findByRole('button', { name: '공개' });
    fireEvent.click(button);
    fireEvent.click(button);
    expect(mocks.change).toHaveBeenCalledTimes(1);
    await act(async () => {
      await router.navigate('/admin/track-manage?filter=active');
    });
    mocks.list.mockResolvedValue({ dataList: [{ ...track, isActive: true }], pageInfo });
    await act(async () => resolve(published));
    expect(mocks.list).toHaveBeenLastCalledWith(
      { page: 1, size: 20, is_active: true },
      expect.any(AbortSignal),
    );
    expect(mocks.change).toHaveBeenCalledTimes(1);
  });

  it('retries only the row refresh after a confirmed publication with failed list reload', async () => {
    mocks.change.mockResolvedValue(published);
    renderPage();
    const button = await screen.findByRole('button', { name: '공개' });
    mocks.list.mockRejectedValueOnce(new Error('list unavailable'));
    fireEvent.click(button);
    await screen.findByText('음원 목록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.');
    expect(screen.getByText('공개했습니다. 이제 로그인 없이 들을 수 있습니다.')).toBeVisible();
    mocks.list.mockResolvedValue({ dataList: [{ ...track, isActive: true }], pageInfo });
    fireEvent.click(screen.getByRole('button', { name: '목록 다시 시도' }));
    await screen.findByRole('button', { name: '공개해제' });
    expect(mocks.change).toHaveBeenCalledTimes(1);
  });
});
