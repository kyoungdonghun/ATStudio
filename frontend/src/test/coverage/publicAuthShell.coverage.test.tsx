import { fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { StrictMode, type ReactElement } from 'react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { Album, Notice, TagItem, Track, TrackListItem, User } from '@/types';

const mocks = vi.hoisted(() => ({
  navigate: vi.fn(),
  verifyEmail: vi.fn(),
  requestPasswordReset: vi.fn(),
  resetPassword: vi.fn(),
  loginRequest: vi.fn(),
  fetchMe: vi.fn(),
  register: vi.fn(),
  checkEmailAvailability: vi.fn(),
  checkNicknameAvailability: vi.fn(),
  checkPhoneAvailability: vi.fn(),
  fetchAlbums: vi.fn(),
  fetchAlbumDetail: vi.fn(),
  createAlbum: vi.fn(),
  updateAlbum: vi.fn(),
  deleteAlbum: vi.fn(),
  addTrackToAlbum: vi.fn(),
  removeTrackFromAlbum: vi.fn(),
  reorderAlbumTracks: vi.fn(),
  fetchTracks: vi.fn(),
  fetchTrackDetail: vi.fn(),
  fetchTrackDetailForAdmin: vi.fn(),
  createTrack: vi.fn(),
  updateTrack: vi.fn(),
  downloadTrack: vi.fn(),
  triggerBlobDownload: vi.fn(),
  fetchDownloadCount: vi.fn(),
  getApiErrorCode: vi.fn(),
  fetchTags: vi.fn(),
  fetchAvailableTags: vi.fn(),
  fetchNotice: vi.fn(),
  downloadNoticeAttachment: vi.fn(),
  toast: vi.fn(),
  fileSizeOk: vi.fn((_file: File, _maxSizeMb: number) => true),
  imageDimensionError: vi.fn((_file: File): Promise<string | null> => Promise.resolve(null)),
  validAudioExtension: vi.fn((_fileName: string) => true),
}));

const states = vi.hoisted(() => ({
  capabilities: {
    capabilities: {
      passwordLoginEnabled: true,
      emailVerification: { enabled: true, deliveryMode: 'REMOTE_SMTP' },
      passwordReset: { enabled: true, deliveryMode: 'REMOTE_SMTP' },
      socialLogin: {
        google: { enabled: false, clientId: null, redirectUri: null },
        kakao: { enabled: false, clientId: null, redirectUri: null },
        naver: { enabled: false, clientId: null, redirectUri: null },
      },
      testUsersEnabled: false,
    } as import('@/api/auth').PublicCapabilitiesResponse | null,
    loading: false,
    error: '',
    status: 'ready' as import('@/hooks/usePublicCapabilities').PublicCapabilitiesStatus,
    retry: vi.fn(),
  },
  auth: {
    user: null as User | null,
    accessToken: null as string | null,
    role: 'GUEST' as 'GUEST' | 'USER' | 'ADMIN',
    isAuthenticated: vi.fn(() => false),
    login: vi.fn(),
    logout: vi.fn(() => Promise.resolve(true)),
  },
  player: {
    currentTrack: null as Track | null,
    isPlaying: false,
    trackListContext: [] as Track[],
    play: vi.fn(),
    pause: vi.fn(),
    resume: vi.fn(),
    next: vi.fn(),
    prev: vi.fn(),
    playAll: vi.fn(),
    setTrackListContext: vi.fn(),
  },
  likes: {
    loaded: false,
    likedIds: new Set<number>(),
    load: vi.fn(() => Promise.resolve()),
    toggle: vi.fn(() => Promise.resolve()),
  },
  albumLikes: {
    loaded: false,
    likedAlbumIds: new Set<number>(),
    load: vi.fn(() => Promise.resolve()),
    toggle: vi.fn(() => Promise.resolve()),
  },
}));

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual<typeof import('react-router-dom')>('react-router-dom');
  return { ...actual, useNavigate: () => mocks.navigate };
});

vi.mock('@/api/auth', () => ({
  verifyEmail: (...args: unknown[]) => mocks.verifyEmail(...args),
  requestPasswordReset: (...args: unknown[]) => mocks.requestPasswordReset(...args),
  resetPassword: (...args: unknown[]) => mocks.resetPassword(...args),
  login: (...args: unknown[]) => mocks.loginRequest(...args),
  fetchMe: (...args: unknown[]) => mocks.fetchMe(...args),
  register: (...args: unknown[]) => mocks.register(...args),
  checkEmailAvailability: (...args: unknown[]) => mocks.checkEmailAvailability(...args),
  checkNicknameAvailability: (...args: unknown[]) => mocks.checkNicknameAvailability(...args),
  checkPhoneAvailability: (...args: unknown[]) => mocks.checkPhoneAvailability(...args),
}));

vi.mock('@/api/albums', () => ({
  fetchAlbums: (...args: unknown[]) => mocks.fetchAlbums(...args),
  fetchAlbumDetail: (...args: unknown[]) => mocks.fetchAlbumDetail(...args),
  createAlbum: (...args: unknown[]) => mocks.createAlbum(...args),
  updateAlbum: (...args: unknown[]) => mocks.updateAlbum(...args),
  deleteAlbum: (...args: unknown[]) => mocks.deleteAlbum(...args),
  addTrackToAlbum: (...args: unknown[]) => mocks.addTrackToAlbum(...args),
  removeTrackFromAlbum: (...args: unknown[]) => mocks.removeTrackFromAlbum(...args),
  reorderAlbumTracks: (...args: unknown[]) => mocks.reorderAlbumTracks(...args),
}));

vi.mock('@/api/tracks', () => ({
  fetchTracks: (...args: unknown[]) => mocks.fetchTracks(...args),
  fetchTrackDetail: (...args: unknown[]) => mocks.fetchTrackDetail(...args),
  fetchTrackDetailForAdmin: (...args: unknown[]) => mocks.fetchTrackDetailForAdmin(...args),
  createTrack: (...args: unknown[]) => mocks.createTrack(...args),
  updateTrack: (...args: unknown[]) => mocks.updateTrack(...args),
}));

vi.mock('@/api/downloads', () => ({
  createDownloadFallbackFileName: () => 'fallback-track.mp3',
  downloadTrack: (...args: unknown[]) => mocks.downloadTrack(...args),
  triggerBlobDownload: (...args: unknown[]) => mocks.triggerBlobDownload(...args),
  fetchDownloadCount: (...args: unknown[]) => mocks.fetchDownloadCount(...args),
}));

vi.mock('@/api/tags', () => ({
  fetchTags: (...args: unknown[]) => mocks.fetchTags(...args),
  fetchAvailableTags: (...args: unknown[]) => mocks.fetchAvailableTags(...args),
}));

vi.mock('@/api/notices', () => ({
  fetchNotice: (...args: unknown[]) => mocks.fetchNotice(...args),
  downloadNoticeAttachment: (...args: unknown[]) => mocks.downloadNoticeAttachment(...args),
}));

vi.mock('@/api/client', () => ({
  toUploadUrl: (path: string | null | undefined) => (path ? `/uploads/${path}` : null),
  getApiErrorCode: (...args: unknown[]) => mocks.getApiErrorCode(...args),
}));

vi.mock('@/hooks/usePublicCapabilities', () => ({
  usePublicCapabilities: () => states.capabilities,
}));

vi.mock('@/store/authStore', () => ({
  useAuthStore: (selector: (state: typeof states.auth) => unknown) => selector(states.auth),
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

vi.mock('@/store/albumLikeStore', () => ({
  useAlbumLikeStore: (selector: (state: typeof states.albumLikes) => unknown) =>
    selector(states.albumLikes),
}));

vi.mock('@/store/toastStore', () => ({
  useToastStore: (selector: (state: { show: typeof mocks.toast }) => unknown) =>
    selector({ show: mocks.toast }),
}));

vi.mock('@/utils/validation', async () => {
  const actual = await vi.importActual<typeof import('@/utils/validation')>('@/utils/validation');
  return {
    ...actual,
    isFileSizeOk: (file: File, maxSizeMb: number) => mocks.fileSizeOk(file, maxSizeMb),
    validateImageDimensions: (file: File) => mocks.imageDimensionError(file),
    hasValidAudioExtension: (fileName: string) => mocks.validAudioExtension(fileName),
  };
});

vi.mock('@/components/album/AlbumCard', () => ({
  default: ({
    album,
    onClick,
    onToggleLike,
  }: {
    album: Album;
    onClick: (album: Album) => void;
    onToggleLike?: (id: number) => void;
  }) => (
    <article>
      <button type="button" onClick={() => onClick(album)}>
        {`album-${album.title}`}
      </button>
      {onToggleLike ? (
        <button type="button" onClick={() => onToggleLike(album.id)}>
          {`like-album-${album.id}`}
        </button>
      ) : null}
    </article>
  ),
}));

vi.mock('@/components/ui/Pagination', () => ({
  default: ({ onPageChange }: { onPageChange: (page: number) => void }) => (
    <button type="button" onClick={() => onPageChange(3)}>
      page-3
    </button>
  ),
}));

vi.mock('@/components/playlist/AddToPlaylistModal', () => ({
  default: ({
    open,
    trackId,
    onSubscriptionRequired,
  }: {
    open: boolean;
    trackId: number | null;
    onSubscriptionRequired: () => void;
  }) =>
    open ? (
      <div>
        <span>{`playlist-modal-${trackId}`}</span>
        <button type="button" onClick={onSubscriptionRequired}>
          subscription-required
        </button>
      </div>
    ) : null,
}));

vi.mock('@/layouts/Header', () => ({ default: () => <div>header-shell</div> }));
vi.mock('@/layouts/PlayerBar', () => ({ default: () => <div>player-shell</div> }));
vi.mock('@/components/ui/ToastContainer', () => ({ default: () => <div>toast-shell</div> }));

import EmailVerifyPage from '@/pages/auth/EmailVerifyPage';
import LoginPage from '@/pages/auth/LoginPage';
import PasswordResetPage from '@/pages/auth/PasswordResetPage';
import SignupPage from '@/pages/auth/SignupPage';
import HomePage from '@/pages/public/HomePage';
import AlbumListPage from '@/pages/public/AlbumListPage';
import AlbumListImagePage from '@/pages/public/AlbumListImagePage';
import AlbumDetailPage from '@/pages/public/AlbumDetailPage';
import TrackDetailPage from '@/pages/public/TrackDetailPage';
import NoticeDetailPage from '@/pages/public/NoticeDetailPage';
import AlbumCreatePage from '@/pages/creator/AlbumCreatePage';
import AlbumManagePage from '@/pages/creator/AlbumManagePage';
import AlbumEditPage from '@/pages/creator/AlbumEditPage';
import TrackUploadPage from '@/pages/creator/TrackUploadPage';
import TrackEditPage from '@/pages/creator/TrackEditPage';
import NotFoundPage from '@/pages/error/NotFoundPage';
import ServerErrorPage from '@/pages/error/ServerErrorPage';
import MainLayout from '@/layouts/MainLayout';
import AdminLayout from '@/layouts/AdminLayout';

const pageInfo = { page: 1, size: 20, total: 1, start: 1, end: 1, prev: false, next: false };

const album: Album = {
  id: 11,
  title: 'Night Drive',
  description: 'City music',
  thumbnailUrl: 'album.jpg',
  trackCount: 2,
  likeCount: 7,
  createdAt: '2026-07-01T00:00:00',
};

const genre: TagItem = { id: 1, name: 'Lo-fi', type: 'GENRE' };
const mood: TagItem = { id: 2, name: 'Calm', type: 'MOOD' };
const instrument: TagItem = { id: 3, name: 'Piano', type: 'INSTRUMENT' };
const usage: TagItem = { id: 4, name: 'Shorts', type: 'USAGE' };

const listTrack: TrackListItem = {
  id: 21,
  title: 'Fresh Track',
  artistName: 'AT.M Creator',
  duration: 120,
  bpm: 110,
  tonality: 'C',
  thumbnail: 'track.jpg',
  playCount: 2,
  likeCount: 3,
  downloadCount: 4,
  waveformData: null,
  tags: [usage],
  createdAt: '2026-07-01T00:00:00',
};

const track: Track = {
  ...listTrack,
  description: 'Track description',
  audioFile: 'track.mp3',
  isActive: true,
  updatedAt: '2026-07-02T00:00:00',
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

function setDefaultApiResponses() {
  mocks.fetchAlbums.mockResolvedValue({ dataList: [album], pageInfo });
  mocks.fetchTracks.mockResolvedValue({ dataList: [listTrack], pageInfo });
  mocks.fetchTags.mockImplementation((type: string) => {
    if (type === 'GENRE') return Promise.resolve([genre]);
    if (type === 'MOOD') return Promise.resolve([mood]);
    if (type === 'INSTRUMENT') return Promise.resolve([instrument]);
    if (type === 'USAGE') return Promise.resolve([usage]);
    return Promise.resolve([genre, mood, instrument, usage]);
  });
  mocks.fetchAvailableTags.mockResolvedValue([genre, mood, instrument, usage]);
  mocks.fetchAlbumDetail.mockResolvedValue({
    id: album.id,
    title: album.title,
    description: album.description,
    thumbnailUrl: album.thumbnailUrl,
    likeCount: album.likeCount,
    createdAt: album.createdAt,
    tracks: [
      {
        trackId: 21,
        title: 'Fresh Track',
        artistName: 'Creator',
        duration: 120,
        order: 1,
      },
      {
        trackId: 22,
        title: 'Second Track',
        artistName: 'Creator',
        duration: 180,
        thumbnailUrl: 'b.jpg',
        waveformData: '[0.1,0.9]',
        order: 2,
      },
    ],
  });
  mocks.fetchTrackDetailForAdmin.mockResolvedValue({ ...track, audioFile: 'track.mp3' });
  mocks.fetchTrackDetail.mockResolvedValue({ ...track, tags: [usage, genre, mood] });
  mocks.downloadTrack.mockResolvedValue({
    blob: new Blob(['audio'], { type: 'audio/mpeg' }),
    fileName: 'server-track.mp3',
    contentType: 'audio/mpeg',
  });
  mocks.fetchDownloadCount.mockResolvedValue({ remaining: 4, dailyLimit: 5 });
  mocks.getApiErrorCode.mockResolvedValue(undefined);
  mocks.createAlbum.mockResolvedValue(album);
  mocks.updateAlbum.mockResolvedValue(album);
  mocks.deleteAlbum.mockResolvedValue(undefined);
  mocks.addTrackToAlbum.mockResolvedValue(undefined);
  mocks.removeTrackFromAlbum.mockResolvedValue(undefined);
  mocks.reorderAlbumTracks.mockResolvedValue(undefined);
  mocks.createTrack.mockResolvedValue({ ...track, audioFile: 'track.mp3' });
  mocks.updateTrack.mockResolvedValue({ ...track, audioFile: 'track.mp3' });
  mocks.verifyEmail.mockResolvedValue(undefined);
  mocks.requestPasswordReset.mockResolvedValue(undefined);
  mocks.resetPassword.mockResolvedValue(undefined);
  mocks.loginRequest.mockResolvedValue({
    accessToken: 'access-token',
    refreshToken: 'refresh-token',
    tokenType: 'Bearer',
    expiresIn: 900,
  });
  mocks.fetchMe.mockResolvedValue({
    id: 7,
    email: 'member@example.com',
    nickname: 'member_7',
    role: 'USER',
    phonePersonal: '010-1234-5678',
    phoneCompany: null,
    job: 'EDITOR',
    companyName: null,
    userType: 'INDIVIDUAL',
    isVerified: true,
    createdAt: '2026-07-01T00:00:00Z',
  });
  mocks.checkEmailAvailability.mockResolvedValue({ available: true });
  mocks.checkNicknameAvailability.mockResolvedValue({ available: true });
  mocks.checkPhoneAvailability.mockResolvedValue({ available: true });
  mocks.register.mockResolvedValue({
    id: 8,
    email: 'business@example.com',
    nickname: 'business_8',
    job: null,
    userType: 'BUSINESS',
    isVerified: false,
    createdAt: '2026-07-01T00:00:00Z',
  });
}

beforeEach(() => {
  vi.clearAllMocks();
  states.capabilities.capabilities = {
    passwordLoginEnabled: true,
    emailVerification: { enabled: true, deliveryMode: 'REMOTE_SMTP' },
    passwordReset: { enabled: true, deliveryMode: 'REMOTE_SMTP' },
    socialLogin: {
      google: { enabled: false, clientId: null, redirectUri: null },
      kakao: { enabled: false, clientId: null, redirectUri: null },
      naver: { enabled: false, clientId: null, redirectUri: null },
    },
    testUsersEnabled: false,
  };
  states.capabilities.loading = false;
  states.capabilities.error = '';
  states.capabilities.status = 'ready';
  states.auth.user = null;
  states.auth.accessToken = null;
  states.auth.role = 'GUEST';
  states.auth.isAuthenticated.mockReturnValue(false);
  states.player.currentTrack = null;
  states.player.isPlaying = false;
  states.player.trackListContext = [];
  states.likes.loaded = false;
  states.likes.likedIds = new Set();
  states.albumLikes.loaded = false;
  states.albumLikes.likedAlbumIds = new Set();
  mocks.fileSizeOk.mockReturnValue(true);
  mocks.imageDimensionError.mockResolvedValue(null);
  mocks.validAudioExtension.mockReturnValue(true);
  vi.stubGlobal('crypto', { randomUUID: vi.fn(() => 'track-entry-1') });
  Object.defineProperty(URL, 'createObjectURL', {
    configurable: true,
    value: vi.fn(() => 'blob:test'),
  });
  Object.defineProperty(URL, 'revokeObjectURL', { configurable: true, value: vi.fn() });
  setDefaultApiResponses();
});

describe('public authentication recovery', () => {
  it('handles missing links and verifies successfully once under StrictMode', async () => {
    const first = renderAt(<EmailVerifyPage />, '/verify-email');
    expect(mocks.verifyEmail).not.toHaveBeenCalled();
    expect(screen.getByRole('heading', { name: '이메일 인증' })).toBeInTheDocument();
    first.unmount();

    renderAt(
      <StrictMode>
        <EmailVerifyPage />
      </StrictMode>,
      '/verify-email?token=valid-token',
    );
    expect(await screen.findByRole('heading', { name: '인증 완료' })).toBeInTheDocument();
    expect(mocks.verifyEmail).toHaveBeenCalledTimes(1);
    expect(mocks.verifyEmail).toHaveBeenCalledWith('valid-token');

    renderAt(<div />).unmount();
  });

  it('maps email verification failure without exposing the server reason', async () => {
    mocks.verifyEmail.mockRejectedValue({
      response: {
        status: 400,
        data: { errorCode: 'INVALID_TOKEN', message: 'private verification detail' },
      },
    });
    renderAt(<EmailVerifyPage />, '/verify-email?token=expired');
    expect(await screen.findByText('유효하지 않거나 만료된 인증 링크입니다.')).toBeInTheDocument();
    expect(screen.queryByText('private verification detail')).not.toBeInTheDocument();
    expect(screen.getByRole('heading', { name: '인증 실패' })).toBeInTheDocument();
  });

  it('requests a reset link and presents the durable success state', async () => {
    renderAt(<PasswordResetPage />, '/password-reset');
    fireEvent.change(screen.getByPlaceholderText('your@email.com'), {
      target: { value: 'member@example.com' },
    });
    fireEvent.click(screen.getByRole('button', { name: '재설정 링크 발송' }));

    await waitFor(() => {
      expect(mocks.requestPasswordReset).toHaveBeenCalledWith({ email: 'member@example.com' });
    });
    expect(screen.getByRole('heading', { name: '요청 접수 완료' })).toBeInTheDocument();
  });

  it('covers password-reset availability, validation, failure, and token completion', async () => {
    states.capabilities.capabilities = {
      ...states.capabilities.capabilities!,
      passwordReset: { enabled: false, deliveryMode: 'UNCONFIGURED' },
    };
    const disabled = renderAt(<PasswordResetPage />, '/password-reset');
    expect(screen.getByText(/비활성화되어 있습니다/)).toBeInTheDocument();
    disabled.unmount();

    states.capabilities.capabilities = {
      ...states.capabilities.capabilities!,
      passwordReset: { enabled: true, deliveryMode: 'REMOTE_SMTP' },
    };
    mocks.resetPassword.mockRejectedValueOnce({
      response: {
        status: 400,
        data: { errorCode: 'INVALID_TOKEN', message: 'private reset token detail' },
      },
    });
    renderAt(<PasswordResetPage />, '/password-reset?token=reset-token');
    fireEvent.change(screen.getByPlaceholderText('8자 이상'), { target: { value: 'password123' } });
    fireEvent.change(screen.getByPlaceholderText('비밀번호 재입력'), {
      target: { value: 'different123' },
    });
    fireEvent.click(screen.getByRole('button', { name: '비밀번호 변경' }));
    expect(mocks.resetPassword).not.toHaveBeenCalled();
    expect(screen.getByText('비밀번호가 일치하지 않습니다.')).toBeInTheDocument();

    fireEvent.change(screen.getByPlaceholderText('비밀번호 재입력'), {
      target: { value: 'password123' },
    });
    fireEvent.click(screen.getByRole('button', { name: '비밀번호 변경' }));
    expect(
      await screen.findByText('유효하지 않거나 만료된 재설정 링크입니다.'),
    ).toBeInTheDocument();
    expect(screen.queryByText('private reset token detail')).not.toBeInTheDocument();

    mocks.resetPassword.mockResolvedValueOnce(undefined);
    fireEvent.click(screen.getByRole('button', { name: '비밀번호 변경' }));
    expect(await screen.findByRole('heading', { name: '비밀번호 변경 완료' })).toBeInTheDocument();
    expect(mocks.resetPassword).toHaveBeenLastCalledWith('reset-token', 'password123');
  });

  it('validates password login input and distinguishes code-bearing credentials from generic failures', async () => {
    renderAt(<LoginPage />, '/login');
    const submit = screen.getByRole('button', { name: '로그인' });
    const email = screen.getByLabelText('이메일');
    const password = screen.getByLabelText('비밀번호');

    fireEvent.click(submit);
    expect(screen.getByText('이메일을 입력해주세요.')).toBeInTheDocument();
    fireEvent.change(email, { target: { value: 'invalid-email' } });
    fireEvent.click(submit);
    expect(screen.getByText('올바른 이메일 형식을 입력해주세요.')).toBeInTheDocument();
    fireEvent.change(email, { target: { value: 'member@example.com' } });
    fireEvent.click(submit);
    expect(screen.getByText('비밀번호를 입력해주세요.')).toBeInTheDocument();
    fireEvent.change(password, { target: { value: 'short' } });
    fireEvent.click(submit);
    expect(screen.getByText(/비밀번호는 .*자 이상/)).toBeInTheDocument();

    fireEvent.change(password, { target: { value: 'password123' } });
    mocks.loginRequest.mockRejectedValueOnce({
      response: { status: 401, data: { errorCode: 'INVALID_CREDENTIALS' } },
    });
    fireEvent.click(submit);
    expect(
      await screen.findByText('이메일 또는 비밀번호가 일치하지 않습니다.'),
    ).toBeInTheDocument();

    mocks.loginRequest.mockRejectedValueOnce({ response: { status: 401 } });
    fireEvent.click(submit);
    expect(
      await screen.findByText('로그인에 실패했습니다. 잠시 후 다시 시도해주세요.'),
    ).toBeInTheDocument();
  });

  it('fails closed with an explicit capability retry and redirects an authenticated visitor', () => {
    states.capabilities.capabilities = null;
    states.capabilities.error = '설정 조회 실패.';
    states.capabilities.status = 'error';
    states.auth.user = {
      id: 7,
      email: 'member@example.com',
      nickname: 'member_7',
      role: 'USER',
      phonePersonal: '010-1234-5678',
      phoneCompany: null,
      job: 'EDITOR',
      companyName: null,
      userType: 'INDIVIDUAL',
      isVerified: true,
      createdAt: '2026-07-01T00:00:00Z',
    };
    states.auth.role = 'USER';
    states.auth.isAuthenticated.mockReturnValue(true);
    renderAt(<LoginPage />, '/login?returnTo=%2Ftracks');

    expect(mocks.navigate).toHaveBeenCalledWith('/tracks', { replace: true });
    expect(screen.getByText(/설정 조회 실패/)).toBeInTheDocument();
    expect(screen.queryByText(/QA 테스트 계정이 활성화/)).not.toBeInTheDocument();
    expect(screen.queryByLabelText('이메일')).not.toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '다시 시도' }));
    expect(states.capabilities.retry).toHaveBeenCalledTimes(1);
  });

  it('registers a business member and reports duplicate and provider failures', async () => {
    renderAt(<SignupPage />, '/signup');
    fireEvent.click(screen.getByRole('button', { name: '기업' }));
    fireEvent.change(screen.getByLabelText('닉네임'), { target: { value: 'business_8' } });
    fireEvent.change(screen.getByLabelText('이메일'), {
      target: { value: 'business@example.com' },
    });
    fireEvent.change(screen.getByLabelText('비밀번호'), {
      target: { value: 'password123' },
    });
    fireEvent.change(screen.getByLabelText('비밀번호 확인'), {
      target: { value: 'password123' },
    });
    fireEvent.change(screen.getByLabelText('연락처'), { target: { value: '01012345678' } });
    fireEvent.change(screen.getByLabelText('회사 연락처 (선택)'), {
      target: { value: '0212345678' },
    });

    fireEvent.click(screen.getByRole('button', { name: '가입하기' }));
    expect(screen.getByText('회사명을 입력해주세요.')).toBeInTheDocument();
    fireEvent.change(screen.getByLabelText('회사명'), { target: { value: '  AT.M Labs  ' } });
    fireEvent.click(screen.getByRole('checkbox', { name: '이용약관 동의 (필수)' }));
    fireEvent.click(screen.getByRole('checkbox', { name: '개인정보 처리방침 동의 (필수)' }));
    fireEvent.click(screen.getByRole('button', { name: '가입하기' }));
    await waitFor(() => expect(mocks.register).toHaveBeenCalledTimes(1));
    expect(mocks.register).toHaveBeenCalledWith({
      nickname: 'business_8',
      email: 'business@example.com',
      password: 'password123',
      phonePersonal: '010-1234-5678',
      phoneCompany: '02-1234-5678',
      job: null,
      companyName: 'AT.M Labs',
      userType: 'BUSINESS',
      termsAgreed: true,
      privacyAgreed: true,
      marketingAgreed: false,
    });
    expect(mocks.navigate).toHaveBeenCalledWith('/email-verify', { replace: true });

    mocks.checkEmailAvailability.mockResolvedValueOnce({ available: false });
    fireEvent.click(screen.getByRole('button', { name: '가입하기' }));
    expect(await screen.findByText('이미 사용 중인 이메일입니다.')).toBeInTheDocument();
    expect(mocks.register).toHaveBeenCalledTimes(1);

    mocks.checkNicknameAvailability.mockResolvedValueOnce({ available: false });
    fireEvent.click(screen.getByRole('button', { name: '가입하기' }));
    expect(await screen.findByText('이미 사용 중인 닉네임입니다.')).toBeInTheDocument();

    mocks.register.mockRejectedValueOnce({
      response: { status: 400, data: { message: '가입 정책에 맞지 않습니다.' } },
    });
    fireEvent.click(screen.getByRole('button', { name: '가입하기' }));
    expect(
      await screen.findByText('회원가입에 실패했습니다. 입력값을 확인하고 다시 시도해주세요.'),
    ).toBeInTheDocument();
    expect(screen.queryByText('가입 정책에 맞지 않습니다.')).not.toBeInTheDocument();
  });
});

describe('public discovery pages', () => {
  it('loads the home feed and follows album and tag exploration actions', async () => {
    mocks.fetchAlbums.mockResolvedValueOnce({ dataList: [album], pageInfo }).mockResolvedValueOnce({
      dataList: [{ ...album, id: 12, title: 'Popular Album' }],
      pageInfo,
    });
    renderAt(<HomePage />);

    fireEvent.click(await screen.findByRole('button', { name: 'album-Night Drive' }));
    expect(mocks.navigate).toHaveBeenCalledWith('/albums/11');
    expect(screen.getByRole('link', { name: /Fresh Track/ })).toHaveAttribute('href', '/tracks/21');

    fireEvent.click(await screen.findByRole('tab', { name: '장르' }));
    fireEvent.click(screen.getByRole('button', { name: 'Lo-fi' }));
    expect(screen.getByRole('link', { name: '선택한 태그로 탐색' })).toHaveAttribute(
      'href',
      '/tracks?genre=Lo-fi',
    );

    fireEvent.click(screen.getByRole('tab', { name: '분위기' }));
    fireEvent.click(screen.getByRole('button', { name: 'Calm' }));
    expect(screen.getByRole('link', { name: '선택한 태그로 탐색' })).toHaveAttribute(
      'href',
      '/tracks?mood=Calm',
    );
  });

  it('renders a home-feed failure without inventing fallback content', async () => {
    mocks.fetchAlbums.mockRejectedValue(new Error('catalog unavailable'));
    renderAt(<HomePage />);
    expect((await screen.findAllByText('catalog unavailable')).length).toBeGreaterThan(0);
    expect(screen.queryByRole('button', { name: 'album-Night Drive' })).not.toBeInTheDocument();
  });

  it('supports list album navigation, sorting, pagination, and authenticated likes', async () => {
    states.auth.isAuthenticated.mockReturnValue(true);
    renderAt(<AlbumListPage />, '/albums/list?page=2&sort=latest');

    const title = await screen.findByText('Night Drive');
    fireEvent.click(title.closest('tr')!);
    expect(mocks.navigate).toHaveBeenCalledWith('/albums/11');

    fireEvent.click(screen.getByRole('button', { name: '좋아요' }));
    expect(states.albumLikes.toggle).toHaveBeenCalledWith(11);
    expect(states.albumLikes.load).toHaveBeenCalled();

    fireEvent.change(screen.getByRole('combobox'), { target: { value: 'trackCount' } });
    fireEvent.click(await screen.findByRole('button', { name: 'page-3' }));
    await waitFor(() => {
      expect(mocks.fetchAlbums).toHaveBeenCalledWith(
        { page: 3, size: 20, sort: 'trackCount' },
        expect.any(AbortSignal),
      );
    });
  });

  it('supports the image album view and its empty and error branches', async () => {
    states.auth.isAuthenticated.mockReturnValue(true);
    const success = renderAt(<AlbumListImagePage />, '/albums');
    fireEvent.click(await screen.findByRole('button', { name: 'album-Night Drive' }));
    fireEvent.click(screen.getByRole('button', { name: 'like-album-11' }));
    expect(mocks.navigate).toHaveBeenCalledWith('/albums/11');
    expect(states.albumLikes.toggle).toHaveBeenCalledWith(11);
    success.unmount();

    mocks.fetchAlbums.mockResolvedValueOnce({ dataList: [], pageInfo });
    const empty = renderAt(<AlbumListImagePage />, '/albums');
    expect(await screen.findByText('앨범이 없습니다.')).toBeInTheDocument();
    empty.unmount();

    mocks.fetchAlbums.mockRejectedValueOnce(new Error('album failure'));
    renderAt(<AlbumListImagePage />, '/albums');
    expect(await screen.findByRole('alert')).toHaveTextContent(
      '앨범 목록 정보를 불러오지 못했습니다. 네트워크 연결을 확인하고 다시 시도해주세요.',
    );
    expect(screen.queryByText('album failure')).not.toBeInTheDocument();
  });

  it('runs album playback, likes, playlist gating, and unauthenticated redirect behavior', async () => {
    states.auth.isAuthenticated.mockReturnValue(true);
    const authenticated = renderAt(<AlbumDetailPage />, '/albums/11', '/albums/:albumId');
    expect(await screen.findByRole('heading', { name: 'Night Drive' })).toBeInTheDocument();
    const normalizedFirstTrack = {
      id: 21,
      title: 'Fresh Track',
      artistName: 'Creator',
      duration: 120,
      thumbnail: null,
      waveformData: null,
    };
    await waitFor(() =>
      expect(states.player.setTrackListContext).toHaveBeenLastCalledWith([
        normalizedFirstTrack,
        expect.objectContaining({
          id: 22,
          duration: 180,
          thumbnail: 'b.jpg',
          waveformData: '[0.1,0.9]',
        }),
      ]),
    );

    fireEvent.click(screen.getByRole('button', { name: /전체 재생/ }));
    fireEvent.click(authenticated.container.querySelector('button[aria-label="Play"]')!);
    fireEvent.click(screen.getAllByRole('button', { name: 'Like' })[0]);
    fireEvent.click(screen.getAllByRole('button', { name: 'Add to playlist' })[0]);
    expect(states.player.playAll).toHaveBeenCalledWith([
      normalizedFirstTrack,
      expect.objectContaining({ id: 22 }),
    ]);
    expect(states.player.play).toHaveBeenCalledWith(normalizedFirstTrack);
    expect(states.likes.toggle).toHaveBeenCalledWith(21);
    expect(screen.getByText('playlist-modal-21')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'subscription-required' }));
    expect(mocks.navigate).toHaveBeenCalledWith('/subscriptions');
    authenticated.unmount();

    states.auth.isAuthenticated.mockReturnValue(false);
    renderAt(<AlbumDetailPage />, '/albums/11', '/albums/:albumId');
    await screen.findByRole('heading', { name: 'Night Drive' });
    fireEvent.click(screen.getAllByTitle('Like')[0]);
    expect(mocks.toast).toHaveBeenCalledWith('warning', '로그인이 필요한 기능입니다.');
    expect(mocks.navigate).toHaveBeenCalledWith('/login');
  });

  it('shows album and notice load failures and downloads notice attachments', async () => {
    mocks.fetchAlbumDetail.mockRejectedValueOnce(new Error('missing album'));
    const missingAlbum = renderAt(<AlbumDetailPage />, '/albums/11', '/albums/:albumId');
    expect(await screen.findByRole('alert')).toHaveTextContent(
      '앨범 정보를 불러오지 못했습니다. 네트워크 연결을 확인하고 다시 시도해주세요.',
    );
    expect(screen.queryByText('missing album')).not.toBeInTheDocument();
    missingAlbum.unmount();

    const notice: Notice = {
      id: 31,
      title: 'Service notice',
      content: 'First line\n\nLast line',
      isPinned: true,
      viewCount: 1234,
      attachments: [
        { id: 1, originalName: 'tiny.txt', fileSize: 10 },
        { id: 2, originalName: 'guide.pdf', fileSize: 2048 },
        { id: 3, originalName: 'archive.zip', fileSize: 2 * 1024 * 1024 },
      ],
      createdAt: '2026-07-01T00:00:00',
      updatedAt: '2026-07-02T00:00:00',
    };
    mocks.fetchNotice.mockResolvedValueOnce(notice);
    mocks.downloadNoticeAttachment.mockResolvedValueOnce({
      blob: new Blob(['guide']),
      fileName: 'server-guide.pdf',
      contentType: 'application/pdf',
    });
    const detail = renderAt(<NoticeDetailPage />, '/notices/31', '/notices/:noticeId');
    expect(await screen.findByRole('heading', { name: 'Service notice' })).toBeInTheDocument();
    expect(screen.getByText('10 B')).toBeInTheDocument();
    expect(screen.getByText('2.0 KB')).toBeInTheDocument();
    expect(screen.getByText('2.0 MB')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'guide.pdf' }));
    expect(mocks.downloadNoticeAttachment).toHaveBeenCalledWith(
      31,
      2,
      expect.any(String),
      expect.any(AbortSignal),
    );
    await waitFor(() =>
      expect(mocks.triggerBlobDownload).toHaveBeenCalledWith(
        expect.objectContaining({ fileName: 'server-guide.pdf' }),
      ),
    );
    detail.unmount();

    mocks.fetchNotice.mockRejectedValueOnce(new Error('notice unavailable'));
    renderAt(<NoticeDetailPage />, '/notices/31', '/notices/:noticeId');
    expect(
      await screen.findByRole('heading', { name: '공지사항을 불러오지 못했습니다' }),
    ).toBeInTheDocument();
  });

  it('downloads a track, refreshes the quota, and exposes authenticated track actions', async () => {
    states.auth.isAuthenticated.mockReturnValue(true);
    renderAt(<TrackDetailPage />, '/tracks/21', '/tracks/:trackId');

    expect(await screen.findByRole('heading', { name: 'Fresh Track' })).toBeInTheDocument();
    expect(screen.getByText('#Shorts')).toBeInTheDocument();
    expect(screen.getByText('Lo-fi')).toBeInTheDocument();
    expect(screen.getByText('Calm')).toBeInTheDocument();
    expect(states.likes.load).toHaveBeenCalled();

    fireEvent.click(screen.getByRole('button', { name: /^▶\s+재생$/ }));
    expect(states.player.play).toHaveBeenCalledWith(expect.objectContaining({ id: 21 }));
    fireEvent.click(screen.getByRole('button', { name: /좋아요/ }));
    expect(states.likes.toggle).toHaveBeenCalledWith(21);

    fireEvent.click(screen.getByRole('button', { name: /재생목록에 추가/ }));
    expect(screen.getByText('playlist-modal-21')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'subscription-required' }));
    expect(mocks.toast).toHaveBeenCalledWith('warning', '구독이 필요한 기능입니다.');
    expect(mocks.navigate).toHaveBeenCalledWith('/subscriptions');

    fireEvent.click(screen.getByRole('button', { name: '다운로드' }));
    await waitFor(() => expect(mocks.downloadTrack).toHaveBeenCalledWith(21, 'fallback-track.mp3'));
    expect(mocks.triggerBlobDownload).toHaveBeenCalledWith(
      expect.objectContaining({ fileName: 'server-track.mp3' }),
    );
    expect(mocks.fetchDownloadCount).toHaveBeenCalled();
    expect(mocks.toast).toHaveBeenCalledWith('success', '다운로드 완료! 오늘 남은 횟수: 4/5');
  });

  it('keeps a successful download durable when the quota refresh fails', async () => {
    states.auth.isAuthenticated.mockReturnValue(true);
    mocks.fetchDownloadCount.mockRejectedValueOnce(new Error('quota unavailable'));
    renderAt(<TrackDetailPage />, '/tracks/21', '/tracks/:trackId');
    await screen.findByRole('heading', { name: 'Fresh Track' });

    fireEvent.click(screen.getByRole('button', { name: '다운로드' }));
    await waitFor(() => {
      expect(mocks.toast).toHaveBeenCalledWith('success', '다운로드가 완료되었습니다.');
    });
    expect(mocks.triggerBlobDownload).toHaveBeenCalled();
  });

  it('distinguishes subscription, quota, generic, and load failures on track detail', async () => {
    states.auth.isAuthenticated.mockReturnValue(true);
    mocks.downloadTrack.mockRejectedValueOnce(new Error('subscription required'));
    mocks.getApiErrorCode.mockResolvedValueOnce('NO_ACTIVE_SUBSCRIPTION');
    const subscriptionFailure = renderAt(<TrackDetailPage />, '/tracks/21', '/tracks/:trackId');
    await screen.findByRole('heading', { name: 'Fresh Track' });
    fireEvent.click(screen.getByRole('button', { name: '다운로드' }));
    await waitFor(() => expect(mocks.navigate).toHaveBeenCalledWith('/subscriptions'));
    expect(mocks.toast).toHaveBeenCalledWith('warning', '구독이 필요한 기능입니다.');
    subscriptionFailure.unmount();

    mocks.downloadTrack.mockRejectedValueOnce(new Error('limit reached'));
    mocks.getApiErrorCode.mockResolvedValueOnce('DOWNLOAD_LIMIT_EXCEEDED');
    const quotaFailure = renderAt(<TrackDetailPage />, '/tracks/21', '/tracks/:trackId');
    await screen.findByRole('heading', { name: 'Fresh Track' });
    fireEvent.click(screen.getByRole('button', { name: '다운로드' }));
    await waitFor(() => {
      expect(mocks.toast).toHaveBeenCalledWith(
        'warning',
        '금일 다운로드 횟수를 모두 사용했습니다.',
      );
    });
    quotaFailure.unmount();

    mocks.downloadTrack.mockRejectedValueOnce(new Error('network down'));
    mocks.getApiErrorCode.mockResolvedValueOnce(undefined);
    const genericFailure = renderAt(<TrackDetailPage />, '/tracks/21', '/tracks/:trackId');
    await screen.findByRole('heading', { name: 'Fresh Track' });
    fireEvent.click(screen.getByRole('button', { name: '다운로드' }));
    await waitFor(() => {
      expect(mocks.toast).toHaveBeenLastCalledWith('error', '다운로드에 실패했습니다.');
    });
    expect(screen.queryByText('다운로드에 실패했습니다.')).not.toBeInTheDocument();
    genericFailure.unmount();

    mocks.fetchTrackDetail.mockRejectedValueOnce(new Error('missing track'));
    renderAt(<TrackDetailPage />, '/tracks/404', '/tracks/:trackId');
    expect(await screen.findByRole('alert')).toHaveTextContent(
      '음원 정보를 불러오지 못했습니다. 네트워크 연결을 확인하고 다시 시도해주세요.',
    );
    expect(screen.queryByText('missing track')).not.toBeInTheDocument();
  });

  it('pauses and resumes the current track without restarting it', async () => {
    states.player.currentTrack = track;
    states.player.isPlaying = true;
    const playing = renderAt(<TrackDetailPage />, '/tracks/21', '/tracks/:trackId');
    await screen.findByRole('heading', { name: 'Fresh Track' });
    fireEvent.click(screen.getByRole('button', { name: /일시정지/ }));
    expect(states.player.pause).toHaveBeenCalled();
    expect(states.player.play).not.toHaveBeenCalled();
    playing.unmount();

    states.player.isPlaying = false;
    renderAt(<TrackDetailPage />, '/tracks/21', '/tracks/:trackId');
    await screen.findByRole('heading', { name: 'Fresh Track' });
    fireEvent.click(screen.getByRole('button', { name: /재생/ }));
    expect(states.player.resume).toHaveBeenCalled();
  });
});

describe('creator forms', () => {
  it('validates and creates an album with a verified thumbnail', async () => {
    const { container } = renderAt(<AlbumCreatePage />);
    fireEvent.click(screen.getByRole('button', { name: '만들기' }));
    expect(screen.getByText('앨범 제목을 입력해주세요.')).toBeInTheDocument();

    fireEvent.change(screen.getByPlaceholderText('앨범 제목'), { target: { value: '  Launch  ' } });
    fireEvent.change(screen.getByPlaceholderText('앨범에 대한 설명 (선택사항)'), {
      target: { value: '  Description  ' },
    });
    const image = new File(['image'], 'cover.png', { type: 'image/png' });
    fireEvent.change(container.querySelector('input[type="file"]')!, {
      target: { files: [image] },
    });
    const preview = await screen.findByAltText('선택한 앨범 썸네일 미리보기');
    expect(preview).toHaveAttribute('src', 'blob:test');
    Object.defineProperty(preview, 'naturalWidth', { configurable: true, value: 1200 });
    Object.defineProperty(preview, 'naturalHeight', { configurable: true, value: 800 });
    fireEvent.load(preview);

    fireEvent.click(screen.getByRole('button', { name: '만들기' }));
    await waitFor(() => expect(mocks.createAlbum).toHaveBeenCalledTimes(1));
    const payload = mocks.createAlbum.mock.calls[0][0] as FormData;
    expect(payload.get('title')).toBe('Launch');
    expect(payload.get('description')).toBe('Description');
    expect(payload.get('thumbnailFile')).toBe(image);
    expect(mocks.navigate).toHaveBeenCalledWith('/admin/albums');
  });

  it('surfaces album creation validation and provider failures', async () => {
    const { container } = renderAt(<AlbumCreatePage />);
    const image = new File(['image'], 'bad.gif', { type: 'image/gif' });
    fireEvent.change(container.querySelector('input[type="file"]')!, {
      target: { files: [image] },
    });
    expect(
      await screen.findByText('앨범 썸네일은 JPEG 또는 PNG 파일만 업로드할 수 있습니다.'),
    ).toBeInTheDocument();

    mocks.createAlbum.mockRejectedValueOnce(new Error('create failed'));
    fireEvent.change(screen.getByPlaceholderText('앨범 제목'), { target: { value: 'Valid' } });
    fireEvent.change(container.querySelector('input[type="file"]')!, {
      target: { files: [new File(['image'], 'valid.png', { type: 'image/png' })] },
    });
    const preview = await screen.findByAltText('선택한 앨범 썸네일 미리보기');
    Object.defineProperty(preview, 'naturalWidth', { configurable: true, value: 800 });
    Object.defineProperty(preview, 'naturalHeight', { configurable: true, value: 600 });
    fireEvent.load(preview);
    fireEvent.click(screen.getByRole('button', { name: '만들기' }));
    expect(await screen.findByText('create failed')).toBeInTheDocument();
  });

  it('creates, edits, navigates, and deletes albums from the manage screen', async () => {
    renderAt(<AlbumManagePage />);
    expect(await screen.findByText('Night Drive')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '음원 관리' }));
    expect(mocks.navigate).toHaveBeenCalledWith('/admin/albums/11/edit');

    fireEvent.click(screen.getByRole('button', { name: '+ 새 앨범' }));
    fireEvent.click(screen.getByRole('button', { name: '생성' }));
    expect(screen.getByText('제목을 입력해주세요.')).toBeInTheDocument();
    fireEvent.change(screen.getByPlaceholderText('앨범 제목'), { target: { value: 'New Album' } });
    fireEvent.click(screen.getByRole('button', { name: '생성' }));
    await waitFor(() => expect(mocks.createAlbum).toHaveBeenCalled());

    fireEvent.click(screen.getByRole('button', { name: '수정' }));
    await waitFor(() =>
      expect(mocks.fetchAlbumDetail).toHaveBeenCalledWith(11, expect.any(AbortSignal)),
    );
    fireEvent.change(screen.getByPlaceholderText('앨범 제목'), { target: { value: 'Edited' } });
    fireEvent.click(screen.getByRole('button', { name: '저장' }));
    await waitFor(() => expect(mocks.updateAlbum).toHaveBeenCalledWith(11, expect.any(FormData)));

    fireEvent.click(screen.getByRole('button', { name: '삭제' }));
    const dialog = screen.getByRole('dialog');
    fireEvent.click(within(dialog).getByRole('button', { name: '삭제' }));
    await waitFor(() => expect(mocks.deleteAlbum).toHaveBeenCalledWith(11));
  });

  it('loads an album editor and applies track search, ordering, removal, and save actions', async () => {
    renderAt(<AlbumEditPage />, '/admin/albums/11/edit', '/admin/albums/:albumId/edit');
    expect(await screen.findByRole('heading', { name: '앨범 수정' })).toBeInTheDocument();
    expect(screen.getByDisplayValue('Night Drive')).toBeInTheDocument();

    fireEvent.change(screen.getByPlaceholderText('트랙 제목 또는 Usage 태그 검색'), {
      target: { value: 'Fresh' },
    });
    fireEvent.click(screen.getByRole('button', { name: '검색' }));
    expect(await screen.findByText('Fresh Track')).toBeInTheDocument();

    fireEvent.click(screen.getAllByTitle('아래로')[0]);
    await waitFor(() => expect(mocks.reorderAlbumTracks).toHaveBeenCalled());
    fireEvent.click(screen.getAllByTitle('제거')[0]);
    await waitFor(() => expect(mocks.removeTrackFromAlbum).toHaveBeenCalledWith(11, 21));

    fireEvent.change(screen.getByDisplayValue('Night Drive'), {
      target: { value: 'Updated Album' },
    });
    fireEvent.click(screen.getByRole('button', { name: '저장' }));
    await waitFor(() => expect(mocks.updateAlbum).toHaveBeenCalledWith(11, expect.any(FormData)));
    expect(mocks.navigate).toHaveBeenCalledWith('/admin/albums');
  });

  it('adds search results and recovers from album track operation failures', async () => {
    const candidate = { ...listTrack, id: 23, title: 'Candidate Track' };
    mocks.fetchTracks.mockResolvedValueOnce({ dataList: [candidate], pageInfo });
    renderAt(<AlbumEditPage />, '/admin/albums/11/edit', '/admin/albums/:albumId/edit');
    await screen.findByRole('heading', { name: '앨범 수정' });

    fireEvent.change(screen.getByPlaceholderText('트랙 제목 또는 Usage 태그 검색'), {
      target: { value: 'Candidate' },
    });
    fireEvent.keyDown(screen.getByPlaceholderText('트랙 제목 또는 Usage 태그 검색'), {
      key: 'Enter',
    });
    fireEvent.click(await screen.findByRole('option', { name: /Candidate Track/ }));
    await waitFor(() => expect(mocks.addTrackToAlbum).toHaveBeenCalledWith(11, 23));
    expect(mocks.toast).toHaveBeenCalledWith('success', '트랙이 추가되었습니다.');

    mocks.reorderAlbumTracks.mockRejectedValueOnce(new Error('reorder failed'));
    fireEvent.click(screen.getAllByTitle('아래로')[0]);
    await waitFor(() => {
      expect(mocks.toast).toHaveBeenCalledWith('error', 'reorder failed');
    });

    mocks.removeTrackFromAlbum.mockRejectedValueOnce(new Error('remove failed'));
    fireEvent.click(screen.getAllByTitle('제거')[0]);
    await waitFor(() => {
      expect(mocks.toast).toHaveBeenCalledWith('error', 'remove failed');
    });
  });

  it('shows album edit load, search, validation, image, and update errors', async () => {
    mocks.fetchAlbumDetail.mockRejectedValueOnce(new Error('album load failed'));
    const loadFailure = renderAt(
      <AlbumEditPage />,
      '/admin/albums/11/edit',
      '/admin/albums/:albumId/edit',
    );
    expect(await screen.findByRole('alert', { name: '앨범 정보 불러오기 실패' })).toHaveTextContent(
      '앨범 정보를 불러오지 못했습니다. 잠시 후 다시 시도해주세요.',
    );
    expect(screen.queryByText('album load failed')).not.toBeInTheDocument();
    loadFailure.unmount();

    const editor = renderAt(
      <AlbumEditPage />,
      '/admin/albums/11/edit',
      '/admin/albums/:albumId/edit',
    );
    await screen.findByDisplayValue('Night Drive');
    const search = screen.getByPlaceholderText('트랙 제목 또는 Usage 태그 검색');
    mocks.fetchTracks.mockRejectedValueOnce(new Error('search failed'));
    fireEvent.change(search, { target: { value: 'Missing' } });
    fireEvent.click(screen.getByRole('button', { name: '검색' }));
    expect(await screen.findByRole('alert', { name: '트랙 검색 실패' })).toHaveTextContent(
      '트랙 검색 결과를 불러오지 못했습니다.',
    );

    fireEvent.change(screen.getByDisplayValue('Night Drive'), { target: { value: '   ' } });
    fireEvent.click(screen.getByRole('button', { name: '저장' }));
    expect(screen.getByText('앨범 제목을 입력해주세요.')).toBeInTheDocument();

    const fileInput = editor.container.querySelector('input[type="file"]')!;
    mocks.fileSizeOk.mockReturnValueOnce(false);
    fireEvent.change(fileInput, {
      target: { files: [new File(['large'], 'large.png', { type: 'image/png' })] },
    });
    expect(screen.getByText(/앨범 썸네일은 .*MB 이하/)).toBeInTheDocument();

    fireEvent.change(fileInput, {
      target: { files: [new File(['image'], 'wrong.png', { type: 'image/png' })] },
    });
    const preview = await screen.findByAltText('선택한 앨범 썸네일 미리보기');
    Object.defineProperty(preview, 'naturalWidth', { configurable: true, value: 4097 });
    Object.defineProperty(preview, 'naturalHeight', { configurable: true, value: 100 });
    fireEvent.load(preview);
    expect(
      await screen.findByText('앨범 썸네일은 가로와 세로가 각각 4096px 이하여야 합니다.'),
    ).toBeInTheDocument();

    fireEvent.change(screen.getAllByRole('textbox')[0], {
      target: { value: 'Updated Album' },
    });
    fireEvent.click(screen.getByRole('button', { name: '선택 지우기' }));
    mocks.updateAlbum.mockRejectedValueOnce(new Error('update failed'));
    fireEvent.click(screen.getByRole('button', { name: '저장' }));
    expect(await screen.findByText('update failed')).toBeInTheDocument();
  });

  it('uploads a validated track and keeps a failed item available for retry', async () => {
    const { container } = renderAt(<TrackUploadPage />);
    expect(screen.getByRole('button', { name: '업로드' })).toBeDisabled();

    const audio = new File(['audio'], 'launch.mp3', { type: 'audio/mpeg' });
    const audioInput = container.querySelector('input[type="file"][multiple]')!;
    fireEvent.change(audioInput, { target: { files: [audio] } });
    expect(await screen.findByDisplayValue('launch')).toBeInTheDocument();
    fireEvent.change(screen.getByPlaceholderText('BPM을 입력해주세요'), {
      target: { value: '120' },
    });
    fireEvent.change(screen.getByRole('combobox'), { target: { value: 'C' } });
    fireEvent.click(screen.getByRole('button', { name: 'Lo-fi' }));
    fireEvent.click(screen.getByRole('button', { name: '#Shorts' }));

    mocks.createTrack.mockRejectedValueOnce(new Error('upload failed'));
    fireEvent.click(screen.getByRole('button', { name: '업로드' }));
    expect(await screen.findByText('upload failed')).toBeInTheDocument();
    expect(screen.getByText(/일부 트랙 업로드에 실패했습니다/)).toBeInTheDocument();

    mocks.createTrack.mockResolvedValueOnce({ ...track, audioFile: 'track.mp3' });
    fireEvent.click(screen.getByRole('button', { name: '업로드' }));
    await waitFor(() => expect(mocks.navigate).toHaveBeenCalledWith('/admin/track-manage'));
  });

  it('loads and updates a track while exercising active, tag, and file branches', async () => {
    const { container } = renderAt(
      <TrackEditPage />,
      '/admin/tracks/21/edit',
      '/admin/tracks/:trackId/edit',
    );
    expect(await screen.findByRole('heading', { name: '음원 수정' })).toBeInTheDocument();
    expect(screen.getByDisplayValue('Fresh Track')).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: '음원 활성 상태' }));
    fireEvent.click(screen.getByRole('button', { name: 'Lo-fi' }));
    const fileInputs = container.querySelectorAll('input[type="file"]');
    fireEvent.change(fileInputs[0], {
      target: { files: [new File(['audio'], 'replacement.mp3', { type: 'audio/mpeg' })] },
    });
    fireEvent.change(fileInputs[1], {
      target: { files: [new File(['image'], 'replacement.png', { type: 'image/png' })] },
    });
    const replacementPreview = await screen.findByAltText('선택한 트랙 썸네일 미리보기');
    Object.defineProperty(replacementPreview, 'naturalWidth', { configurable: true, value: 800 });
    Object.defineProperty(replacementPreview, 'naturalHeight', { configurable: true, value: 800 });
    fireEvent.load(replacementPreview);
    fireEvent.click(screen.getByRole('button', { name: '저장' }));

    await waitFor(() => expect(mocks.updateTrack).toHaveBeenCalledWith(21, expect.any(FormData)));
    expect(mocks.navigate).toHaveBeenCalledWith('/admin/track-manage');
  });
});

describe('application shells and error routes', () => {
  it('routes global player shortcuts while preserving text-input editing', () => {
    states.player.currentTrack = track;
    states.player.trackListContext = [track, { ...track, id: 22, title: 'Next' }];
    states.player.isPlaying = true;
    render(
      <MemoryRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
        <Routes>
          <Route element={<MainLayout />}>
            <Route index element={<input aria-label="editor" />} />
          </Route>
        </Routes>
      </MemoryRouter>,
    );
    expect(screen.getByText('header-shell')).toBeInTheDocument();
    expect(screen.getByText('player-shell')).toBeInTheDocument();

    fireEvent.keyDown(window, { key: ' ' });
    fireEvent.keyDown(window, { key: 'ArrowDown' });
    fireEvent.keyDown(window, { key: 'ArrowUp' });
    expect(states.player.pause).toHaveBeenCalled();
    expect(states.player.next).toHaveBeenCalled();
    expect(states.player.prev).toHaveBeenCalled();

    fireEvent.keyDown(screen.getByRole('textbox', { name: 'editor' }), { key: ' ' });
    expect(states.player.pause).toHaveBeenCalledTimes(1);
  });

  it('starts the first context track and exercises the admin shell menu and logout', async () => {
    states.player.currentTrack = null;
    states.player.trackListContext = [track];
    const main = render(
      <MemoryRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
        <Routes>
          <Route element={<MainLayout />}>
            <Route index element={<div>main-content</div>} />
          </Route>
        </Routes>
      </MemoryRouter>,
    );
    fireEvent.keyDown(window, { key: 'ArrowDown' });
    expect(states.player.play).toHaveBeenCalledWith(track);
    main.unmount();

    states.auth.user = {
      id: 1,
      email: 'admin@example.com',
      nickname: 'Operator',
      role: 'ADMIN',
      phonePersonal: null,
      phoneCompany: null,
      job: 'EDITOR',
      companyName: null,
      userType: 'INDIVIDUAL',
      isVerified: true,
      createdAt: '2026-01-01T00:00:00',
    };
    const admin = render(
      <MemoryRouter
        initialEntries={['/admin/albums/11/edit']}
        future={{ v7_startTransition: true, v7_relativeSplatPath: true }}
      >
        <Routes>
          <Route element={<AdminLayout />}>
            <Route path="/admin/albums/:albumId/edit" element={<div>admin-content</div>} />
          </Route>
        </Routes>
      </MemoryRouter>,
    );
    const adminScreen = within(admin.container);
    expect(adminScreen.getByText('Operator')).toBeInTheDocument();
    expect(adminScreen.getByRole('link', { name: '앨범 관리' }).className).toContain(
      'navItemActive',
    );
    fireEvent.click(admin.container.querySelector('button[aria-label="Open menu"]')!);
    expect(admin.container.querySelector('button[aria-label="Close menu"]')).toBeInTheDocument();
    fireEvent.click(admin.container.querySelector('button[aria-label="Close menu"]')!);
    fireEvent.click(adminScreen.getByRole('button', { name: '로그아웃' }));
    expect(states.auth.logout).toHaveBeenCalled();
    expect(mocks.navigate).toHaveBeenCalledWith('/', { replace: true });
  });

  it('renders actionable not-found and server-error recovery links', () => {
    const notFound = renderAt(<NotFoundPage />);
    expect(screen.getByRole('link')).toHaveAttribute('href', '/');
    notFound.unmount();
    renderAt(<ServerErrorPage />);
    expect(screen.getByRole('link')).toHaveAttribute('href', '/');
  });
});
