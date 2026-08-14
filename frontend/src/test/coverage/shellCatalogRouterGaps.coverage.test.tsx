import { act, fireEvent, render, renderHook, screen, waitFor } from '@testing-library/react';
import type { ReactElement } from 'react';
import {
  MemoryRouter,
  Outlet,
  Route,
  RouterProvider,
  Routes,
  createMemoryRouter,
} from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { PublicCapabilitiesResponse } from '@/api/auth';
import type { PageInfo, PagedResponse, TagItem, Track, TrackListItem, User } from '@/types';

const mocks = vi.hoisted(() => ({
  navigate: vi.fn(),
  fetchPublicCapabilities: vi.fn(),
  fetchTracks: vi.fn(),
  createTrack: vi.fn(),
  fetchTags: vi.fn(),
  fetchAvailableTags: vi.fn(),
  downloadTrack: vi.fn(),
  triggerBlobDownload: vi.fn(),
  fetchDownloadCount: vi.fn(),
  getApiErrorCode: vi.fn(),
  fetchMySubscription: vi.fn(),
  toast: vi.fn(),
  fileSizeOk: vi.fn(),
  validAudioExtension: vi.fn(),
}));

const states = vi.hoisted(() => ({
  auth: {
    authenticated: false,
    accessToken: null as string | null,
    user: null as User | null,
    role: 'GUEST' as 'GUEST' | 'USER' | 'ADMIN',
    isAuthenticated: vi.fn(),
    logout: vi.fn(),
  },
  theme: {
    theme: 'dark' as 'dark' | 'light',
    toggle: vi.fn(),
  },
  player: {
    currentTrack: null as Track | null,
    isPlaying: false,
    isStalled: false,
    playbackError: null as string | null,
    currentTime: 10,
    duration: 100,
    volume: 1,
    muted: false,
    shuffle: false,
    repeat: 'off' as 'off' | 'one' | 'all',
    trackListContext: [] as Track[],
    play: vi.fn(),
    pause: vi.fn(),
    resume: vi.fn(),
    next: vi.fn(),
    prev: vi.fn(),
    seek: vi.fn(),
    setVolume: vi.fn(),
    toggleMute: vi.fn(),
    toggleShuffle: vi.fn(),
    cycleRepeat: vi.fn(),
    setTrackListContext: vi.fn(),
  },
  likes: {
    loaded: true,
    likedIds: new Set<number>(),
    load: vi.fn(),
    toggle: vi.fn(),
  },
}));

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual<typeof import('react-router-dom')>('react-router-dom');
  return { ...actual, useNavigate: () => mocks.navigate };
});

vi.mock('@/api/auth', async () => {
  const actual = await vi.importActual<typeof import('@/api/auth')>('@/api/auth');
  return {
    ...actual,
    fetchPublicCapabilities: (...args: unknown[]) => mocks.fetchPublicCapabilities(...args),
  };
});

vi.mock('@/api/tracks', () => ({
  fetchTracks: (...args: unknown[]) => mocks.fetchTracks(...args),
  createTrack: (...args: unknown[]) => mocks.createTrack(...args),
}));

vi.mock('@/api/tags', () => ({
  fetchTags: (...args: unknown[]) => mocks.fetchTags(...args),
  fetchAvailableTags: (...args: unknown[]) => mocks.fetchAvailableTags(...args),
}));

vi.mock('@/api/downloads', () => ({
  createDownloadFallbackFileName: () => 'fallback-track.mp3',
  downloadTrack: (...args: unknown[]) => mocks.downloadTrack(...args),
  triggerBlobDownload: (...args: unknown[]) => mocks.triggerBlobDownload(...args),
  fetchDownloadCount: (...args: unknown[]) => mocks.fetchDownloadCount(...args),
}));

vi.mock('@/api/client', () => ({
  toUploadUrl: (path: string | null | undefined) => (path ? `/uploads/${path}` : null),
  getApiErrorCode: (...args: unknown[]) => mocks.getApiErrorCode(...args),
}));

vi.mock('@/api/userSubscriptions', () => ({
  fetchMySubscription: (...args: unknown[]) => mocks.fetchMySubscription(...args),
  isNoActiveSubscriptionError: (error: unknown) => {
    const shaped = error as { response?: { status?: number; data?: { errorCode?: string } } };
    return (
      shaped.response?.status === 403 &&
      shaped.response?.data?.errorCode === 'NO_ACTIVE_SUBSCRIPTION'
    );
  },
}));

vi.mock('@/utils/validation', async () => {
  const actual = await vi.importActual<typeof import('@/utils/validation')>('@/utils/validation');
  return {
    ...actual,
    isFileSizeOk: (file: File, maxSizeMb: number) => mocks.fileSizeOk(file, maxSizeMb),
    hasValidAudioExtension: (fileName: string) => mocks.validAudioExtension(fileName),
  };
});

vi.mock('@/store/authStore', () => ({
  useAuthStore: Object.assign(
    (selector: (state: typeof states.auth) => unknown) => selector(states.auth),
    { getState: () => states.auth },
  ),
}));

vi.mock('@/store/themeStore', () => ({
  useThemeStore: (selector: (state: typeof states.theme) => unknown) => selector(states.theme),
}));

vi.mock('@/store/playerStore', () => ({
  usePlayerStore: Object.assign(
    (selector: (state: typeof states.player) => unknown) => selector(states.player),
    { getState: () => states.player },
  ),
}));

vi.mock('@/store/likeStore', () => ({
  useLikeStore: (selector: (state: typeof states.likes) => unknown) => selector(states.likes),
}));

vi.mock('@/store/toastStore', () => ({
  useToastStore: Object.assign(
    (selector: (state: { show: typeof mocks.toast }) => unknown) => selector({ show: mocks.toast }),
    { getState: () => ({ show: mocks.toast }) },
  ),
}));

vi.mock('@/components/player/WaveformCanvas', () => ({
  default: ({
    peaks,
    progress,
    onSeek,
    height,
  }: {
    peaks: number[];
    progress: number;
    onSeek: (ratio: number) => void;
    height?: number;
  }) => (
    <button
      type="button"
      aria-label={height ? `waveform-seek-${height}` : 'waveform-seek'}
      data-peaks={peaks.join(',')}
      data-progress={progress}
      onClick={() => onSeek(0.25)}
    >
      waveform
    </button>
  ),
}));

vi.mock('@/components/player/HistoryModal', () => ({
  default: ({ open, onClose }: { open: boolean; onClose: () => void }) =>
    open ? (
      <button type="button" onClick={onClose}>
        close-history
      </button>
    ) : null,
}));

vi.mock('@/components/player/PlaylistDrawer', () => ({
  default: ({ open, onClose }: { open: boolean; onClose: () => void }) =>
    open ? (
      <button type="button" onClick={onClose}>
        close-queue
      </button>
    ) : null,
}));

vi.mock('@/components/playlist/AddToPlaylistModal', () => ({
  default: ({
    open,
    trackId,
    onClose,
    onSubscriptionRequired,
  }: {
    open: boolean;
    trackId: number | null;
    onClose: () => void;
    onSubscriptionRequired: () => void;
  }) =>
    open ? (
      <div>
        <span>{`playlist-modal-${trackId}`}</span>
        <button type="button" onClick={onClose}>
          close-playlist-modal
        </button>
        <button type="button" onClick={onSubscriptionRequired}>
          playlist-subscription-required
        </button>
      </div>
    ) : null,
}));

vi.mock('@/components/track/TrackRow', () => ({
  default: ({
    track,
    showAuthActions,
    onGuestAction,
    onPlay,
    onLike,
    onAddToPlaylist,
    onDownload,
  }: {
    track: TrackListItem;
    showAuthActions: boolean;
    onGuestAction: () => void;
    onPlay: (track: TrackListItem) => void;
    onLike: (track: TrackListItem) => void;
    onAddToPlaylist: (track: TrackListItem) => void;
    onDownload: (track: TrackListItem) => Promise<void>;
  }) => (
    <tr>
      <td>{track.title}</td>
      <td>
        <button type="button" onClick={() => onPlay(track)}>
          {`play-${track.id}`}
        </button>
        {showAuthActions ? (
          <>
            <button type="button" onClick={() => onLike(track)}>
              {`like-${track.id}`}
            </button>
            <button type="button" onClick={() => onAddToPlaylist(track)}>
              {`playlist-${track.id}`}
            </button>
            <button type="button" onClick={() => void onDownload(track)}>
              {`download-${track.id}`}
            </button>
          </>
        ) : (
          <button type="button" onClick={onGuestAction}>
            {`guest-${track.id}`}
          </button>
        )}
      </td>
    </tr>
  ),
}));

vi.mock('@/components/ui/FilterChip', () => ({
  default: ({
    label,
    active,
    onClick,
  }: {
    label: string;
    active: boolean;
    onClick: () => void;
  }) => (
    <button type="button" aria-pressed={active} onClick={onClick}>
      {label}
    </button>
  ),
}));

vi.mock('@/components/filter/TagFilterModal', () => ({
  default: () => null,
}));

vi.mock('@/components/ui/Pagination', () => ({
  default: ({ onPageChange }: { onPageChange: (page: number) => void }) => (
    <button type="button" onClick={() => onPageChange(3)}>
      page-3
    </button>
  ),
}));

vi.mock('@/layouts/MainLayout', () => ({ default: () => <Outlet /> }));
vi.mock('@/layouts/AdminLayout', () => ({ default: () => <Outlet /> }));

vi.mock('@/pages/public/HomePage', () => ({ default: () => <div>route-home</div> }));
vi.mock('@/pages/public/SubscriptionPlanPage', () => ({
  default: () => <div>route-subscriptions</div>,
}));
vi.mock('@/pages/auth/LoginPage', () => ({ default: () => <div>route-login</div> }));
vi.mock('@/pages/subscriber/ProfilePage', () => ({ default: () => <div>route-profile</div> }));
vi.mock('@/pages/subscriber/DownloadHistoryPage', () => ({
  default: () => <div>route-downloads</div>,
}));
vi.mock('@/pages/subscriber/CompanyCertStatusPage', () => ({
  default: () => <div>route-company-status</div>,
}));
vi.mock('@/pages/subscriber/QuestionListPage', () => ({
  default: () => <div>route-questions</div>,
}));
vi.mock('@/pages/admin/DashboardPage', () => ({ default: () => <div>route-dashboard</div> }));
vi.mock('@/pages/admin/QuestionManagePage', () => ({
  default: () => <div>route-admin-questions</div>,
}));
vi.mock('@/pages/admin/PaymentOperationsPage', () => ({
  default: () => <div>route-admin-payments</div>,
}));
vi.mock('@/pages/error/ServerErrorPage', () => ({ default: () => <div>route-error</div> }));
vi.mock('@/pages/error/NotFoundPage', () => ({ default: () => <div>route-not-found</div> }));

import { usePublicCapabilities } from '@/hooks/usePublicCapabilities';
import Header from '@/layouts/Header';
import PlayerBar from '@/layouts/PlayerBar';
import TrackUploadPage from '@/pages/creator/TrackUploadPage';
import TrackListPage from '@/pages/public/TrackListPage';
import { routes } from '@/router';

const capabilities: PublicCapabilitiesResponse = {
  passwordLoginEnabled: true,
  emailVerification: { enabled: true, deliveryMode: 'REMOTE_SMTP' },
  passwordReset: { enabled: true, deliveryMode: 'REMOTE_SMTP' },
  socialLogin: {
    google: { enabled: true, clientId: 'google-client', redirectUri: '/oauth/google' },
    kakao: { enabled: false, clientId: null, redirectUri: null },
    naver: { enabled: false, clientId: null, redirectUri: null },
  },
  testUsersEnabled: false,
};

const member: User = {
  id: 7,
  email: 'member@example.com',
  nickname: 'Member',
  role: 'USER',
  phonePersonal: '010-1234-5678',
  phoneCompany: null,
  job: 'EDITOR',
  companyName: null,
  userType: 'INDIVIDUAL',
  isVerified: true,
  createdAt: '2026-07-01T00:00:00Z',
};

const genreTags: TagItem[] = Array.from({ length: 7 }, (_, index) => ({
  id: index + 1,
  name: `genre-${index + 1}`,
  type: 'GENRE',
}));
const moodTags: TagItem[] = Array.from({ length: 7 }, (_, index) => ({
  id: index + 101,
  name: `mood-${index + 1}`,
  type: 'MOOD',
}));
const instrumentTags: TagItem[] = [
  { id: 201, name: 'Piano', type: 'INSTRUMENT' },
  { id: 202, name: 'Guitar', type: 'INSTRUMENT' },
];
const usageTags: TagItem[] = Array.from({ length: 7 }, (_, index) => ({
  id: index + 301,
  name: `usage-${index + 1}`,
  type: 'USAGE',
}));

const firstListTrack: TrackListItem = {
  id: 11,
  title: 'First catalog track',
  artistName: 'Creator',
  duration: 120,
  bpm: 110,
  tonality: 'C',
  thumbnail: null,
  playCount: 1,
  likeCount: 2,
  downloadCount: 3,
  waveformData: '[0.2,0.8]',
  tags: [genreTags[0], usageTags[0]],
  createdAt: '2026-07-01T00:00:00Z',
};

const secondListTrack: TrackListItem = {
  ...firstListTrack,
  id: 12,
  title: 'Fallback metadata track',
  artistName: null,
  duration: null,
  tags: [],
} as unknown as TrackListItem;

const currentTrack: Track = {
  ...firstListTrack,
  description: 'Player track',
  audioFile: 'track.mp3',
  thumbnail: 'track.png',
  isActive: true,
  updatedAt: '2026-07-02T00:00:00Z',
};

const pageInfo: PageInfo = {
  page: 1,
  size: 20,
  total: 2,
  start: 1,
  end: 1,
  prev: false,
  next: true,
};

const trackPage: PagedResponse<TrackListItem> = {
  dataList: [firstListTrack, secondListTrack],
  pageInfo,
};

function renderAt(element: ReactElement, route = '/', path = '*') {
  return render(
    <MemoryRouter
      initialEntries={[route]}
      future={{ v7_startTransition: true, v7_relativeSplatPath: true }}
    >
      <Routes>
        <Route path={path} element={element} />
      </Routes>
    </MemoryRouter>,
  );
}

function renderPlayer() {
  return renderAt(<PlayerBar />);
}

function renderCatalog(route = '/tracks?page=1') {
  return renderAt(<TrackListPage />, route, '/tracks');
}

function renderAppRoute(path: string) {
  const memoryRouter = createMemoryRouter(routes, { initialEntries: [path] });
  return render(<RouterProvider router={memoryRouter} future={{ v7_startTransition: true }} />);
}

function deferred<T>() {
  let resolve!: (value: T) => void;
  let reject!: (reason?: unknown) => void;
  const promise = new Promise<T>((resolvePromise, rejectPromise) => {
    resolve = resolvePromise;
    reject = rejectPromise;
  });
  return { promise, resolve, reject };
}

function resetMockFunctions() {
  for (const candidate of Object.values(mocks)) {
    if (typeof candidate === 'function' && 'mockReset' in candidate) {
      candidate.mockReset();
    }
  }
  for (const candidate of [
    ...Object.values(states.auth),
    ...Object.values(states.theme),
    ...Object.values(states.player),
    ...Object.values(states.likes),
  ]) {
    if (typeof candidate === 'function' && 'mockReset' in candidate) {
      candidate.mockReset();
    }
  }
}

beforeEach(() => {
  resetMockFunctions();
  localStorage.clear();

  states.auth.authenticated = false;
  states.auth.accessToken = null;
  states.auth.user = null;
  states.auth.role = 'GUEST';
  states.auth.isAuthenticated.mockImplementation(() => states.auth.authenticated);

  states.theme.theme = 'dark';

  states.player.currentTrack = currentTrack;
  states.player.isPlaying = false;
  states.player.isStalled = false;
  states.player.playbackError = null;
  states.player.currentTime = 10;
  states.player.duration = 100;
  states.player.volume = 1;
  states.player.muted = false;
  states.player.shuffle = false;
  states.player.repeat = 'off';
  states.player.trackListContext = [];

  states.likes.loaded = true;
  states.likes.likedIds = new Set<number>();
  states.likes.load.mockResolvedValue(undefined);
  states.likes.toggle.mockResolvedValue(undefined);

  mocks.fetchPublicCapabilities.mockResolvedValue(capabilities);
  mocks.fetchTracks.mockResolvedValue(trackPage);
  mocks.createTrack.mockResolvedValue(currentTrack);
  mocks.fetchTags.mockImplementation((type: string) => {
    if (type === 'GENRE') return Promise.resolve(genreTags);
    if (type === 'MOOD') return Promise.resolve(moodTags);
    if (type === 'INSTRUMENT') return Promise.resolve(instrumentTags);
    if (type === 'USAGE') return Promise.resolve(usageTags);
    return Promise.resolve([]);
  });
  mocks.fetchAvailableTags.mockResolvedValue([
    ...genreTags,
    ...moodTags,
    ...instrumentTags,
    ...usageTags,
  ]);
  mocks.downloadTrack.mockResolvedValue({
    blob: new Blob(['audio'], { type: 'audio/mpeg' }),
    fileName: 'server-track.mp3',
    contentType: 'audio/mpeg',
  });
  mocks.fetchDownloadCount.mockResolvedValue({ remaining: 4, dailyLimit: 5 });
  mocks.getApiErrorCode.mockResolvedValue(undefined);
  mocks.fetchMySubscription.mockResolvedValue({ id: 1 });
  mocks.fileSizeOk.mockReturnValue(true);
  mocks.validAudioExtension.mockReturnValue(true);
});

describe('public capability discovery', () => {
  it('publishes the configured login capabilities and ends loading', async () => {
    const { result } = renderHook(() => usePublicCapabilities());

    expect(result.current.loading).toBe(true);
    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(result.current.capabilities).toEqual(capabilities);
    expect(result.current.error).toBe('');
  });

  it('fails closed with a user-facing error and ignores completion after unmount', async () => {
    mocks.fetchPublicCapabilities.mockRejectedValueOnce(new Error('offline'));
    const failed = renderHook(() => usePublicCapabilities());
    await waitFor(() => expect(failed.result.current.loading).toBe(false));
    expect(failed.result.current.capabilities).toBeNull();
    expect(failed.result.current.error).not.toBe('');

    const pending = deferred<PublicCapabilitiesResponse>();
    mocks.fetchPublicCapabilities.mockReturnValueOnce(pending.promise);
    const abandoned = renderHook(() => usePublicCapabilities());
    abandoned.unmount();
    await act(async () => pending.resolve(capabilities));
    expect(mocks.fetchPublicCapabilities).toHaveBeenCalledTimes(2);
  });
});

describe('header navigation behavior', () => {
  it('opens the mobile menu, performs an encoded search, and toggles theme', () => {
    states.theme.theme = 'light';
    renderAt(<Header />, '/tracks');

    fireEvent.click(screen.getAllByRole('button', { name: '다크 모드로 전환' })[0]);
    expect(states.theme.toggle).toHaveBeenCalledTimes(1);

    fireEvent.click(screen.getByLabelText('메뉴 열기'));
    expect(screen.getByLabelText('메뉴 닫기')).toHaveAttribute('aria-expanded', 'true');

    const mobileSearch = screen.getByLabelText('모바일 곡 제목 및 용도 검색');
    fireEvent.change(mobileSearch, { target: { value: '  봄 shorts  ' } });
    fireEvent.submit(mobileSearch.closest('form')!);

    expect(mocks.navigate).toHaveBeenCalledWith(
      `/tracks?keyword=${encodeURIComponent('봄 shorts')}&page=1`,
    );
    expect(screen.getByLabelText('메뉴 열기')).toHaveAttribute('aria-expanded', 'false');
    expect(screen.queryByLabelText('모바일 곡 제목 및 용도 검색')).not.toBeInTheDocument();
    fireEvent.click(screen.getByLabelText('메뉴 열기'));
    expect(screen.getByLabelText('모바일 곡 제목 및 용도 검색')).toHaveValue('');
  });

  it('ignores blank search and closes the mobile overlay without navigation', () => {
    renderAt(<Header />);
    const desktopSearch = screen.getByLabelText('곡 제목 및 용도 검색');
    fireEvent.change(desktopSearch, { target: { value: '   ' } });
    fireEvent.submit(desktopSearch.closest('form')!);
    expect(mocks.navigate).not.toHaveBeenCalled();

    fireEvent.click(screen.getByLabelText('메뉴 열기'));
    fireEvent.click(document.querySelector('[class*="mobileOverlay"]')!);
    expect(screen.getByLabelText('메뉴 열기')).toBeInTheDocument();
  });

  it('shows member navigation and logs out from desktop and mobile actions', () => {
    states.auth.authenticated = true;
    states.auth.accessToken = 'token';
    states.auth.user = member;
    states.auth.role = 'USER';
    renderAt(<Header />, '/playlists');

    expect(screen.getAllByText('재생목록').length).toBeGreaterThan(0);
    fireEvent.click(screen.getByLabelText('계정 메뉴'));
    expect(screen.getAllByText('Member').length).toBeGreaterThan(0);

    const logoutButtons = screen.getAllByText('로그아웃');
    fireEvent.click(logoutButtons[0]);
    fireEvent.click(logoutButtons[1]);
    expect(states.auth.logout).toHaveBeenCalledTimes(2);
    expect(mocks.navigate).toHaveBeenNthCalledWith(1, '/', { replace: true });
    expect(mocks.navigate).toHaveBeenNthCalledWith(2, '/', { replace: true });
  });

  it('uses the reduced admin navigation and handles an authenticated identity without a profile', () => {
    states.auth.authenticated = true;
    states.auth.role = 'ADMIN';
    states.auth.user = { ...member, role: 'ADMIN', nickname: 'Admin' };
    const adminView = renderAt(<Header />, '/admin/dashboard');
    expect(screen.getAllByText('관리자').length).toBeGreaterThan(0);
    expect(screen.queryByText('구독')).not.toBeInTheDocument();
    adminView.unmount();

    states.auth.user = null;
    renderAt(<Header />);
    expect(screen.queryByRole('button', { name: '계정 메뉴' })).not.toBeInTheDocument();
    expect(screen.getAllByRole('button', { name: '로그아웃' }).length).toBeGreaterThan(0);
  });
});

describe('player transport, subscription, and recovery behavior', () => {
  it('seeks from waveform, keyboard, and mobile pointer while driving transport modes', () => {
    const view = renderPlayer();

    fireEvent.click(screen.getAllByRole('button', { name: 'waveform-seek' })[0]);
    expect(states.player.seek).toHaveBeenCalledWith(25);

    const seekSlider = screen.getByRole('slider', { name: '재생 위치' });
    for (const [key, expected] of [
      ['ArrowUp', 15],
      ['ArrowDown', 5],
      ['Home', 0],
      ['End', 100],
    ] as const) {
      fireEvent.keyDown(seekSlider, { key });
      expect(states.player.seek).toHaveBeenLastCalledWith(expected);
    }
    const callCount = states.player.seek.mock.calls.length;
    fireEvent.keyDown(seekSlider, { key: 'Escape' });
    expect(states.player.seek).toHaveBeenCalledTimes(callCount);

    const mobileSlider = screen.getByLabelText('모바일 재생 위치');
    vi.spyOn(mobileSlider, 'getBoundingClientRect').mockReturnValue({
      x: 10,
      y: 0,
      left: 10,
      right: 210,
      top: 0,
      bottom: 10,
      width: 200,
      height: 10,
      toJSON: () => ({}),
    });
    fireEvent.click(mobileSlider, { clientX: 310 });
    expect(states.player.seek).toHaveBeenLastCalledWith(100);

    fireEvent.click(screen.getAllByRole('button', { name: '재생' })[0]);
    expect(states.player.resume).toHaveBeenCalledTimes(1);
    fireEvent.click(screen.getAllByRole('button', { name: '이전 곡' })[0]);
    fireEvent.click(screen.getAllByRole('button', { name: '다음 곡' })[0]);
    expect(states.player.prev).toHaveBeenCalledTimes(1);
    expect(states.player.next).toHaveBeenCalledTimes(1);

    states.player.isPlaying = true;
    states.player.shuffle = true;
    states.player.repeat = 'one';
    view.rerender(
      <MemoryRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
        <Routes>
          <Route path="*" element={<PlayerBar />} />
        </Routes>
      </MemoryRouter>,
    );
    fireEvent.click(screen.getAllByRole('button', { name: '일시정지' })[0]);
    expect(states.player.pause).toHaveBeenCalledTimes(1);
    expect(screen.getAllByRole('button', { name: '셔플 사용 중' })[0]).toHaveAttribute(
      'aria-pressed',
      'true',
    );
    fireEvent.click(screen.getAllByRole('button', { name: '한 곡 반복 사용 중' })[0]);
    expect(states.player.cycleRepeat).toHaveBeenCalledTimes(1);
  });

  it('opens volume and drawers, closes them, and renders full empty state for administrators', () => {
    states.player.currentTrack = null;
    states.player.volume = 0.25;
    states.auth.authenticated = true;
    states.auth.role = 'ADMIN';
    states.auth.user = { ...member, role: 'ADMIN' };
    renderPlayer();

    expect(mocks.fetchMySubscription).not.toHaveBeenCalled();
    fireEvent.click(screen.getAllByRole('button', { name: '볼륨 설정 열기' })[0]);
    const volumeSliders = screen.getAllByRole('slider', { name: '볼륨' });
    fireEvent.change(volumeSliders[0], { target: { value: '0.4' } });
    expect(states.player.setVolume).toHaveBeenCalledWith(0.4);
    fireEvent.mouseDown(document.body);

    fireEvent.click(screen.getAllByRole('button', { name: '재생기록' })[0]);
    expect(screen.getByRole('button', { name: 'close-history' })).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'close-history' }));
    fireEvent.click(screen.getAllByRole('button', { name: '재생목록' })[0]);
    expect(screen.getByRole('button', { name: 'close-queue' })).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'close-queue' }));

    fireEvent.click(screen.getByLabelText('플레이어 상세 펼치기'));
    expect(screen.getByLabelText('플레이어 상세 접기')).toHaveAttribute('aria-expanded', 'true');
  });

  it('routes guests to login or subscription and protects like and playlist actions', () => {
    const view = renderPlayer();
    fireEvent.click(screen.getAllByRole('button', { name: '좋아요' })[0]);
    expect(mocks.toast).toHaveBeenCalledWith('warning', '로그인 후 이용 가능합니다.');
    expect(mocks.navigate).toHaveBeenCalledWith('/login?returnTo=%2F');

    fireEvent.click(screen.getByRole('button', { name: '재생목록에 추가' }));
    expect(mocks.navigate).toHaveBeenCalledWith('/login?returnTo=%2F');
    fireEvent.click(screen.getAllByRole('button', { name: `${currentTrack.title} 상세 보기` })[0]);
    expect(mocks.navigate).toHaveBeenCalledWith(`/tracks/${currentTrack.id}`);
    view.unmount();

    states.player.currentTrack = null;
    renderPlayer();
    fireEvent.click(screen.getAllByRole('button', { name: '구독하기' })[0]);
    expect(mocks.navigate).toHaveBeenCalledWith('/subscriptions');
  });

  it('enables member likes, playlists, and successful downloads after subscription verification', async () => {
    states.auth.authenticated = true;
    states.auth.accessToken = 'token';
    states.auth.user = member;
    states.auth.role = 'USER';
    states.likes.loaded = false;
    renderPlayer();

    expect(states.likes.load).toHaveBeenCalledTimes(1);
    const downloadButtons = await screen.findAllByTitle('음원 다운로드');
    fireEvent.click(screen.getAllByRole('button', { name: '좋아요' })[0]);
    expect(states.likes.toggle).toHaveBeenCalledWith(currentTrack.id);

    fireEvent.click(screen.getByRole('button', { name: '재생목록에 추가' }));
    expect(screen.getByText(`playlist-modal-${currentTrack.id}`)).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'playlist-subscription-required' }));
    expect(mocks.toast).toHaveBeenCalledWith('warning', '구독이 필요한 기능입니다.');
    expect(mocks.navigate).toHaveBeenCalledWith('/subscriptions');

    fireEvent.click(downloadButtons[0]);
    await waitFor(() =>
      expect(mocks.downloadTrack).toHaveBeenCalledWith(currentTrack.id, 'fallback-track.mp3'),
    );
    expect(mocks.triggerBlobDownload).toHaveBeenCalledWith(
      expect.objectContaining({ fileName: 'server-track.mp3' }),
    );
    expect(mocks.toast).toHaveBeenCalledWith('success', '다운로드가 완료되었습니다.');
  });

  it('classifies download limit payloads and generic download failures', async () => {
    states.auth.authenticated = true;
    states.auth.accessToken = 'token';
    states.auth.user = member;
    states.auth.role = 'USER';
    mocks.downloadTrack.mockRejectedValueOnce(new Error('limit'));
    mocks.getApiErrorCode.mockResolvedValueOnce('DOWNLOAD_LIMIT_EXCEEDED');
    renderPlayer();

    const firstDownload = (await screen.findAllByTitle('음원 다운로드'))[0];
    fireEvent.click(firstDownload);
    await waitFor(() =>
      expect(mocks.toast).toHaveBeenCalledWith(
        'warning',
        '금일 다운로드 횟수를 모두 사용했습니다.',
      ),
    );

    mocks.downloadTrack.mockRejectedValueOnce(new Error('network'));
    fireEvent.click(firstDownload);
    await waitFor(() =>
      expect(mocks.toast).toHaveBeenCalledWith('error', '다운로드에 실패했습니다.'),
    );
  });

  it('falls back from malformed waveform and duration, and exposes repeat and mute states', () => {
    states.player.currentTrack = {
      ...currentTrack,
      waveformData: '{bad-json',
      duration: 0,
      tags: undefined,
      thumbnail: null,
    } as unknown as Track;
    states.player.duration = 0;
    states.player.currentTime = Number.POSITIVE_INFINITY;
    states.player.repeat = 'all';
    states.player.muted = true;
    renderPlayer();
    fireEvent.click(screen.getByLabelText('플레이어 상세 펼치기'));

    expect(screen.getAllByRole('button', { name: 'waveform-seek' })[0]).toHaveAttribute(
      'data-peaks',
      '',
    );
    expect(screen.getAllByRole('button', { name: '전체 반복 사용 중' }).length).toBeGreaterThan(0);
    expect(screen.getAllByLabelText('음소거 해제').length).toBeGreaterThan(0);
    expect(screen.getAllByText('0:00').length).toBeGreaterThan(0);
  });
});

describe('catalog filters, actions, and pagination', () => {
  it('applies URL filters, toggles every filter family, clears search, and changes pages', async () => {
    const route =
      '/tracks?keyword=spring&genre=genre-1&mood=mood-1&instrument=Piano&usage=usage-1&bpm=60%20%E2%80%93%2079&sort=popular&page=2';
    renderCatalog(route);

    expect(await screen.findByText('First catalog track')).toBeInTheDocument();
    expect(mocks.fetchTracks).toHaveBeenCalledWith(
      expect.objectContaining({
        page: 2,
        sort: 'popular',
        keyword: 'spring',
        genre: ['genre-1'],
        mood: ['mood-1'],
        instrument: ['Piano'],
        usage: ['usage-1'],
        bpmMin: 60,
        bpmMax: 79,
      }),
      expect.any(AbortSignal),
    );

    for (const expand of screen.getAllByRole('button', { name: '▼ 펼치기' })) {
      fireEvent.click(expand);
    }
    expect(screen.getAllByRole('button', { name: '▲ 접기' })).toHaveLength(3);

    fireEvent.click(screen.getByRole('button', { name: 'genre-1' }));
    fireEvent.click(screen.getByRole('button', { name: 'mood-1' }));
    fireEvent.click(screen.getByRole('button', { name: 'Piano' }));
    fireEvent.click(screen.getByRole('button', { name: '#usage-1' }));
    fireEvent.click(screen.getByRole('button', { name: '60 \u2013 79' }));
    fireEvent.change(screen.getByRole('combobox'), { target: { value: 'downloads' } });
    fireEvent.click(screen.getByRole('button', { name: '검색 해제' }));
    fireEvent.click(await screen.findByRole('button', { name: 'page-3' }));

    await waitFor(() =>
      expect(mocks.fetchTracks).toHaveBeenLastCalledWith(
        expect.objectContaining({ page: 3 }),
        expect.any(AbortSignal),
      ),
    );
  }, 10_000);

  it('loads member likes and exercises play, like, playlist, and download outcomes', async () => {
    states.auth.authenticated = true;
    states.auth.accessToken = 'token';
    states.auth.user = member;
    states.auth.role = 'USER';
    states.likes.loaded = false;
    states.player.currentTrack = null;
    const view = renderCatalog();
    expect(await screen.findByText('First catalog track')).toBeInTheDocument();
    expect(states.likes.load).toHaveBeenCalledTimes(1);

    fireEvent.click(screen.getByRole('button', { name: 'play-11' }));
    expect(states.player.play).toHaveBeenCalledWith(
      expect.objectContaining({ id: 11, title: 'First catalog track' }),
    );

    states.player.currentTrack = currentTrack;
    states.player.isPlaying = false;
    view.rerender(
      <MemoryRouter
        initialEntries={['/tracks?page=1']}
        future={{ v7_startTransition: true, v7_relativeSplatPath: true }}
      >
        <Routes>
          <Route path="/tracks" element={<TrackListPage />} />
        </Routes>
      </MemoryRouter>,
    );
    fireEvent.click(screen.getByRole('button', { name: 'play-11' }));
    expect(states.player.resume).toHaveBeenCalledTimes(1);

    states.player.isPlaying = true;
    view.rerender(
      <MemoryRouter
        initialEntries={['/tracks?page=1']}
        future={{ v7_startTransition: true, v7_relativeSplatPath: true }}
      >
        <Routes>
          <Route path="/tracks" element={<TrackListPage />} />
        </Routes>
      </MemoryRouter>,
    );
    fireEvent.click(screen.getByRole('button', { name: 'play-11' }));
    expect(states.player.pause).toHaveBeenCalledTimes(1);

    fireEvent.click(screen.getByRole('button', { name: 'like-11' }));
    expect(states.likes.toggle).toHaveBeenCalledWith(11);
    fireEvent.click(screen.getByRole('button', { name: 'playlist-11' }));
    expect(screen.getByText('playlist-modal-11')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'playlist-subscription-required' }));
    expect(mocks.navigate).toHaveBeenCalledWith('/subscriptions');

    fireEvent.click(screen.getByRole('button', { name: 'download-11' }));
    await waitFor(() =>
      expect(mocks.toast).toHaveBeenCalledWith('success', '다운로드 완료! 오늘 남은 횟수: 4/5'),
    );

    mocks.fetchDownloadCount.mockRejectedValueOnce(new Error('count unavailable'));
    fireEvent.click(screen.getByRole('button', { name: 'download-11' }));
    await waitFor(() =>
      expect(mocks.toast).toHaveBeenCalledWith('success', '다운로드가 완료되었습니다.'),
    );

    for (const [code, expectedType, expectedMessage] of [
      ['NO_ACTIVE_SUBSCRIPTION', 'warning', '구독이 필요한 기능입니다.'],
      ['DOWNLOAD_LIMIT_EXCEEDED', 'warning', '금일 다운로드 횟수를 모두 사용했습니다.'],
      ['UNKNOWN', 'error', '다운로드에 실패했습니다.'],
    ] as const) {
      mocks.downloadTrack.mockRejectedValueOnce(new Error(code));
      mocks.getApiErrorCode.mockResolvedValueOnce(code);
      fireEvent.click(screen.getByRole('button', { name: 'download-11' }));
      await waitFor(() => expect(mocks.toast).toHaveBeenCalledWith(expectedType, expectedMessage));
    }
  });

  it('routes guest actions to login and tolerates supplementary tag failures', async () => {
    mocks.fetchTags.mockRejectedValueOnce(new Error('tags unavailable'));
    mocks.fetchAvailableTags.mockRejectedValueOnce(new Error('availability unavailable'));
    renderCatalog('/tracks?genre=genre-1&page=1');
    expect(await screen.findByText('First catalog track')).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: 'guest-11' }));
    expect(mocks.toast).toHaveBeenCalledWith('warning', '로그인이 필요한 기능입니다.');
    expect(mocks.navigate).toHaveBeenCalledWith('/login');
  });
});

describe('lazy route and guard behavior', () => {
  it.each([
    ['/', 'route-home'],
    ['/subscriptions', 'route-subscriptions'],
    ['/error', 'route-error'],
    ['/missing-screen', 'route-not-found'],
  ])('resolves the public lazy route %s', async (path, marker) => {
    renderAppRoute(path);
    expect(await screen.findByText(marker)).toBeInTheDocument();
  });

  it('redirects a guest from an authenticated lazy route to login', async () => {
    renderAppRoute('/profile');
    expect(await screen.findByText('route-login')).toBeInTheDocument();
    expect(mocks.toast).toHaveBeenCalledWith('warning', expect.any(String));
  });

  it('resolves user, subscriber, business, and admin lazy routes for matching capabilities', async () => {
    states.auth.authenticated = true;
    states.auth.accessToken = 'token';
    states.auth.user = member;
    states.auth.role = 'USER';
    const profile = renderAppRoute('/profile');
    expect(await screen.findByText('route-profile')).toBeInTheDocument();
    profile.unmount();

    const downloads = renderAppRoute('/downloads');
    expect(await screen.findByText('route-downloads')).toBeInTheDocument();
    downloads.unmount();

    states.auth.user = { ...member, userType: 'BUSINESS' };
    const company = renderAppRoute('/company-certification/status');
    expect(await screen.findByText('route-company-status')).toBeInTheDocument();
    company.unmount();

    states.auth.role = 'ADMIN';
    states.auth.user = { ...member, role: 'ADMIN', userType: 'BUSINESS' };
    const dashboard = renderAppRoute('/admin/dashboard');
    expect(await screen.findByText('route-dashboard')).toBeInTheDocument();
    dashboard.unmount();
  });

  it('redirects admin questions through the loader and keeps invalid local identity non-fatal', async () => {
    states.auth.authenticated = true;
    states.auth.role = 'ADMIN';
    states.auth.user = { ...member, role: 'ADMIN' };
    localStorage.setItem('user', JSON.stringify({ role: 'ADMIN' }));
    const redirected = renderAppRoute('/questions');
    expect(await screen.findByText('route-admin-questions')).toBeInTheDocument();
    redirected.unmount();

    states.auth.role = 'USER';
    states.auth.user = member;
    localStorage.setItem('user', '{invalid-json');
    renderAppRoute('/questions');
    expect(await screen.findByText('route-questions')).toBeInTheDocument();
  });
});

describe('track upload validation and recovery behavior', () => {
  it('rejects unsupported and oversized audio before creating upload entries', () => {
    const view = renderAt(<TrackUploadPage />);
    const audioInput = view.container.querySelector('input[type="file"][multiple]')!;

    mocks.validAudioExtension.mockReturnValueOnce(false);
    fireEvent.change(audioInput, {
      target: { files: [new File(['text'], 'notes.txt', { type: 'text/plain' })] },
    });
    expect(screen.getByText(/지원하지 않는 파일 형식입니다/)).toHaveTextContent('notes.txt');

    mocks.fileSizeOk.mockReturnValueOnce(false);
    fireEvent.change(audioInput, {
      target: { files: [new File(['audio'], 'large.mp3', { type: 'audio/mpeg' })] },
    });
    expect(screen.getByText(/오디오 파일은 .*MB 이하/)).toHaveTextContent('large.mp3');
    expect(screen.getByRole('button', { name: '업로드' })).toBeDisabled();
  });

  it('caps a batch at twenty tracks and refuses additional files', async () => {
    const view = renderAt(<TrackUploadPage />);
    const audioInput = view.container.querySelector('input[type="file"][multiple]')!;
    const files = Array.from(
      { length: 21 },
      (_, index) => new File(['audio'], `track-${index + 1}.mp3`, { type: 'audio/mpeg' }),
    );
    fireEvent.change(audioInput, { target: { files } });
    expect(await screen.findByRole('button', { name: '20곡 업로드' })).toBeEnabled();
    expect(screen.getByText(/1개 파일이 제외되었습니다/)).toBeInTheDocument();

    fireEvent.change(audioInput, {
      target: { files: [new File(['audio'], 'extra.mp3', { type: 'audio/mpeg' })] },
    });
    expect(screen.getByText(/최대 20곡까지 업로드/)).toBeInTheDocument();
  });

  it('validates title, BPM, and tonality and supports removal and cancel navigation', async () => {
    const view = renderAt(<TrackUploadPage />);
    const audioInput = view.container.querySelector('input[type="file"][multiple]')!;
    fireEvent.change(audioInput, {
      target: {
        files: [
          new File(['audio'], 'first.mp3', { type: 'audio/mpeg' }),
          new File(['audio'], 'second.mp3', { type: 'audio/mpeg' }),
        ],
      },
    });

    fireEvent.click(screen.getByRole('button', { name: 'second 제거' }));
    expect(screen.queryByDisplayValue('second')).not.toBeInTheDocument();
    const title = screen.getByPlaceholderText('음원 제목');
    fireEvent.change(title, { target: { value: '   ' } });
    fireEvent.click(screen.getByRole('button', { name: '업로드' }));
    expect(screen.getByText(/제목을 입력해주세요/)).toBeInTheDocument();

    fireEvent.change(title, { target: { value: 'Validated track' } });
    fireEvent.click(screen.getByRole('button', { name: '업로드' }));
    expect(screen.getByText(/BPM을 올바르게 입력해주세요/)).toBeInTheDocument();

    fireEvent.change(screen.getByPlaceholderText('BPM을 입력해주세요'), {
      target: { value: '120' },
    });
    fireEvent.click(screen.getByRole('button', { name: '업로드' }));
    expect(screen.getByText(/조성을 선택해주세요/)).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: '취소' }));
    expect(mocks.navigate).toHaveBeenCalledWith(-1);
  });

  it('submits rich metadata, preserves failed entries, and skips completed entries on retry', async () => {
    const view = renderAt(<TrackUploadPage />);
    const audioInput = view.container.querySelector('input[type="file"][multiple]')!;
    fireEvent.change(audioInput, {
      target: {
        files: [
          new File(['audio'], 'success.mp3', { type: 'audio/mpeg' }),
          new File(['audio'], 'retry.mp3', { type: 'audio/mpeg' }),
        ],
      },
    });

    const titleInputs = screen.getAllByPlaceholderText('음원 제목');
    const bpmInputs = screen.getAllByPlaceholderText('BPM을 입력해주세요');
    const tonalities = screen.getAllByRole('combobox');
    fireEvent.change(titleInputs[0], { target: { value: 'Success' } });
    fireEvent.change(bpmInputs[0], { target: { value: '110' } });
    fireEvent.change(tonalities[0], { target: { value: 'C' } });

    fireEvent.change(screen.getByPlaceholderText('음원에 대한 설명 (선택사항)'), {
      target: { value: '  launch description  ' },
    });
    const genreTag = await screen.findByRole('button', { name: 'genre-1' });
    fireEvent.click(genreTag);
    fireEvent.click(genreTag);
    fireEvent.click(screen.getByRole('button', { name: '#usage-1' }));

    const firstThumbnailInput = view.container.querySelector('input[type="file"]:not([multiple])')!;
    mocks.fileSizeOk.mockReturnValueOnce(false);
    fireEvent.change(firstThumbnailInput, {
      target: { files: [new File(['large'], 'large.png', { type: 'image/png' })] },
    });
    expect(screen.getByText('트랙 썸네일은 10MB 이하만 업로드할 수 있습니다.')).toBeInTheDocument();
    Object.defineProperty(URL, 'createObjectURL', {
      configurable: true,
      value: vi.fn(() => 'blob:track-thumbnail'),
    });
    Object.defineProperty(URL, 'revokeObjectURL', {
      configurable: true,
      value: vi.fn(),
    });
    fireEvent.change(firstThumbnailInput, {
      target: { files: [new File(['image'], 'cover.png', { type: 'image/png' })] },
    });
    expect(screen.getByText('cover.png')).toBeInTheDocument();
    expect(screen.getByText('이미지 크기를 확인하는 중입니다.')).toBeInTheDocument();
    const thumbnailPreview = screen.getByRole('img', { name: '선택한 트랙 썸네일 미리보기' });
    Object.defineProperties(thumbnailPreview, {
      naturalWidth: { configurable: true, value: 1200 },
      naturalHeight: { configurable: true, value: 1200 },
    });
    fireEvent.load(thumbnailPreview);
    await waitFor(() =>
      expect(screen.queryByText('이미지 크기를 확인하는 중입니다.')).not.toBeInTheDocument(),
    );

    const collapsedHeader = screen.getByText('retry').closest('[class*="trackHeader"]')!;
    fireEvent.click(collapsedHeader);
    fireEvent.change(screen.getByPlaceholderText('음원 제목'), {
      target: { value: 'Retry' },
    });
    fireEvent.change(screen.getByPlaceholderText('BPM을 입력해주세요'), {
      target: { value: '125' },
    });
    fireEvent.change(screen.getByRole('combobox'), { target: { value: 'D' } });

    mocks.createTrack
      .mockResolvedValueOnce(currentTrack)
      .mockRejectedValueOnce('provider failure')
      .mockResolvedValueOnce(currentTrack);
    fireEvent.click(screen.getByRole('button', { name: '2곡 업로드' }));
    expect(await screen.findByText('업로드 실패')).toBeInTheDocument();
    expect(screen.getByText(/일부 트랙 업로드에 실패했습니다/)).toBeInTheDocument();
    expect(mocks.createTrack).toHaveBeenCalledTimes(2);

    fireEvent.click(screen.getByRole('button', { name: '2곡 업로드' }));
    await waitFor(() => expect(mocks.navigate).toHaveBeenCalledWith('/admin/track-manage'));
    expect(mocks.createTrack).toHaveBeenCalledTimes(3);

    const firstForm = mocks.createTrack.mock.calls[0][0] as FormData;
    expect(firstForm.get('description')).toBe('launch description');
    expect(firstForm.get('thumbnail')).toBeInstanceOf(File);
    expect(firstForm.getAll('tagIds')).toEqual(['301']);
  });
});
