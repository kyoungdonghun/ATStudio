import { act, fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import type { ReactElement } from 'react';
import { createMemoryRouter, RouterProvider } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import LicenseManagePage from '@/pages/admin/LicenseManagePage';
import NoticeCreatePage from '@/pages/admin/NoticeCreatePage';
import NoticeEditPage from '@/pages/admin/NoticeEditPage';
import QuestionManagePage from '@/pages/admin/QuestionManagePage';
import SiteSettingsPage from '@/pages/admin/SiteSettingsPage';
import AdminSubscriptionManagePage from '@/pages/admin/SubscriptionManagePage';
import subscriptionStylesSource from '@/pages/admin/SubscriptionManagePage.module.css?raw';
import TagManagePage from '@/pages/admin/TagManagePage';
import TrackManagePage from '@/pages/admin/TrackManagePage';
import LicenseDetailPage from '@/pages/subscriber/LicenseDetailPage';
import LicenseListPage from '@/pages/subscriber/LicenseListPage';
import LikeListPage from '@/pages/subscriber/LikeListPage';
import PlayHistoryPage from '@/pages/subscriber/PlayHistoryPage';
import PlaylistDetailPage from '@/pages/subscriber/PlaylistDetailPage';
import PlaylistEditPage from '@/pages/subscriber/PlaylistEditPage';
import QuestionCreatePage from '@/pages/subscriber/QuestionCreatePage';
import QuestionDetailPage from '@/pages/subscriber/QuestionDetailPage';
import QuestionListPage from '@/pages/subscriber/QuestionListPage';

const mocks = vi.hoisted(() => ({
  fetchQuestions: vi.fn(),
  updateQuestionStatus: vi.fn(),
  createQuestion: vi.fn(),
  fetchQuestionDetail: vi.fn(),
  deleteQuestion: vi.fn(),
  downloadAttachment: vi.fn(),
  createAnswer: vi.fn(),
  fetchAdminTracks: vi.fn(),
  deleteTrack: vi.fn(),
  fetchAdminSubscriptionPlans: vi.fn(),
  getSetting: vi.fn(),
  updateSetting: vi.fn(),
  fetchTags: vi.fn(),
  createTag: vi.fn(),
  updateTag: vi.fn(),
  fetchTagDeletionImpact: vi.fn(),
  deleteTag: vi.fn(),
  createNotice: vi.fn(),
  fetchAdminNotice: vi.fn(),
  updateNotice: vi.fn(),
  deleteNotice: vi.fn(),
  fetchUserLicenses: vi.fn(),
  fetchMyLicenses: vi.fn(),
  fetchLicenseDetail: vi.fn(),
  fetchUserDetail: vi.fn(),
  fetchUsers: vi.fn(),
  fetchLikes: vi.fn(),
  removeLike: vi.fn(),
  fetchAlbumLikes: vi.fn(),
  removeAlbumLike: vi.fn(),
  downloadTrack: vi.fn(),
  triggerBlobDownload: vi.fn(),
  fetchPlaylistDetail: vi.fn(),
  updatePlaylist: vi.fn(),
  deletePlaylist: vi.fn(),
  removeTrackFromPlaylist: vi.fn(),
  reorderTracks: vi.fn(),
  loadPlayHistory: vi.fn(),
  hydratePlayHistory: vi.fn(),
  removePlayHistoryEntry: vi.fn(),
  clearPlayHistory: vi.fn(),
  toast: vi.fn(),
  playerPlay: vi.fn(),
  playerPause: vi.fn(),
  playerResume: vi.fn(),
  playerAddToQueue: vi.fn(),
  playerSetContext: vi.fn(),
  likeLoad: vi.fn(),
  likeToggle: vi.fn(),
  likeRemove: vi.fn(),
  albumLikeRemove: vi.fn(),
}));

const storeState = vi.hoisted(() => ({
  player: {
    currentTrack: null as { id: number } | null,
    isPlayerPlaying: false,
    isPlaying: false,
    play: mocks.playerPlay,
    pause: mocks.playerPause,
    resume: mocks.playerResume,
    addToQueue: mocks.playerAddToQueue,
    setTrackListContext: mocks.playerSetContext,
  },
  likes: {
    likedIds: new Set<number>(),
    load: mocks.likeLoad,
    toggle: mocks.likeToggle,
    remove: mocks.likeRemove,
  },
  albumLikes: {
    remove: mocks.albumLikeRemove,
  },
  auth: {
    role: 'ADMIN',
    user: {
      id: 7,
      email: 'admin@example.com',
      nickname: 'Admin',
      role: 'ADMIN',
      phonePersonal: null,
      phoneCompany: null,
      job: null,
      companyName: null,
      userType: 'INDIVIDUAL',
      isVerified: true,
      createdAt: '2026-01-01T00:00:00Z',
    },
  },
}));

vi.mock('@/api/questions', () => ({
  fetchQuestions: mocks.fetchQuestions,
  updateQuestionStatus: mocks.updateQuestionStatus,
  createQuestion: mocks.createQuestion,
  fetchQuestionDetail: mocks.fetchQuestionDetail,
  deleteQuestion: mocks.deleteQuestion,
  downloadAttachment: mocks.downloadAttachment,
  createAnswer: mocks.createAnswer,
}));

vi.mock('@/api/tracks', () => ({
  fetchAdminTracks: mocks.fetchAdminTracks,
  deleteTrack: mocks.deleteTrack,
}));

vi.mock('@/api/subscriptions', () => ({
  fetchAdminSubscriptionPlans: mocks.fetchAdminSubscriptionPlans,
}));

vi.mock('@/api/settings', () => ({
  getSetting: mocks.getSetting,
  updateSetting: mocks.updateSetting,
}));

vi.mock('@/api/tags', () => ({
  fetchTags: mocks.fetchTags,
  createTag: mocks.createTag,
  updateTag: mocks.updateTag,
  fetchTagDeletionImpact: mocks.fetchTagDeletionImpact,
  deleteTag: mocks.deleteTag,
}));

vi.mock('@/api/notices', () => ({
  createNotice: mocks.createNotice,
  fetchAdminNotice: mocks.fetchAdminNotice,
  updateNotice: mocks.updateNotice,
  deleteNotice: mocks.deleteNotice,
}));

vi.mock('@/api/licenses', () => ({
  fetchUserLicenses: mocks.fetchUserLicenses,
  fetchMyLicenses: mocks.fetchMyLicenses,
  fetchLicenseDetail: mocks.fetchLicenseDetail,
}));

vi.mock('@/api/admin', () => ({
  fetchUserDetail: mocks.fetchUserDetail,
  fetchUsers: mocks.fetchUsers,
}));

vi.mock('@/api/likes', () => ({
  fetchLikes: mocks.fetchLikes,
  removeLike: mocks.removeLike,
  fetchAlbumLikes: mocks.fetchAlbumLikes,
  removeAlbumLike: mocks.removeAlbumLike,
}));

vi.mock('@/api/downloads', () => ({
  downloadTrack: mocks.downloadTrack,
  triggerBlobDownload: mocks.triggerBlobDownload,
}));

vi.mock('@/api/playlists', () => ({
  fetchPlaylistDetail: mocks.fetchPlaylistDetail,
  updatePlaylist: mocks.updatePlaylist,
  deletePlaylist: mocks.deletePlaylist,
  removeTrackFromPlaylist: mocks.removeTrackFromPlaylist,
  reorderTracks: mocks.reorderTracks,
}));

vi.mock('@/api/client', () => ({
  default: {},
  toUploadUrl: (path: string | null | undefined) => (path ? `http://uploads.test${path}` : null),
}));

vi.mock('@/store/playerStore', () => ({
  usePlayerStore: (selector: (state: typeof storeState.player) => unknown) =>
    selector(storeState.player),
  loadPlayHistory: mocks.loadPlayHistory,
  hydratePlayHistory: mocks.hydratePlayHistory,
  removePlayHistoryEntry: mocks.removePlayHistoryEntry,
  clearPlayHistory: mocks.clearPlayHistory,
}));

vi.mock('@/store/likeStore', () => ({
  useLikeStore: () => storeState.likes,
}));

vi.mock('@/store/albumLikeStore', () => ({
  useAlbumLikeStore: () => storeState.albumLikes,
}));

vi.mock('@/store/toastStore', () => ({
  useToastStore: (selector: (state: { show: typeof mocks.toast }) => unknown) =>
    selector({ show: mocks.toast }),
}));

vi.mock('@/store/authStore', () => ({
  useAuthStore: (selector: (state: typeof storeState.auth) => unknown) => selector(storeState.auth),
}));

vi.mock('@/components/playlist/AddToPlaylistModal', () => ({
  default: ({
    open,
    trackId,
    onClose,
  }: {
    open: boolean;
    trackId: number | null;
    onClose: () => void;
  }) =>
    open ? (
      <button type="button" onClick={onClose}>
        close playlist picker {trackId}
      </button>
    ) : null,
}));

const firstPage = {
  page: 1,
  size: 20,
  total: 1,
  start: 1,
  end: 1,
  prev: false,
  next: false,
};

function deferred<T>() {
  let resolve!: (value: T) => void;
  let reject!: (reason?: unknown) => void;
  const promise = new Promise<T>((nextResolve, nextReject) => {
    resolve = nextResolve;
    reject = nextReject;
  });
  return { promise, resolve, reject };
}

function renderRoute(element: ReactElement, path: string, initialEntry = path) {
  const router = createMemoryRouter(
    [
      { path, element },
      { path: '*', element: <div data-testid="navigation-target">navigated</div> },
    ],
    { initialEntries: [initialEntry] },
  );
  render(<RouterProvider router={router} future={{ v7_startTransition: true }} />);
  return router;
}

function licenseItem() {
  return {
    id: 31,
    track: { id: 41, title: 'License Track', bpm: 120, tonality: 'C' },
    licenseCode: 'LICENSE-CODE-1234567890',
    issuedAt: '2026-07-01T00:00:00Z',
  };
}

function playlistDetail() {
  return {
    id: 3,
    title: 'Focus Mix',
    description: 'Work tracks',
    thumbnail: '/playlist.jpg',
    tracks: [
      {
        trackOrder: 1,
        trackId: 11,
        title: 'First Track',
        artistName: 'Artist',
        duration: 180,
        bpm: 110,
        tonality: 'C',
      },
      {
        trackOrder: 2,
        trackId: 12,
        title: 'Second Track',
        artistName: 'Artist',
        duration: 200,
        thumbnail: null,
        waveformData: '[0.1,0.9]',
        bpm: 120,
        tonality: 'D',
      },
    ],
    createdAt: '2026-07-01T00:00:00Z',
    updatedAt: '2026-07-02T00:00:00Z',
  };
}

beforeEach(() => {
  vi.resetAllMocks();
  storeState.player.currentTrack = null;
  storeState.player.isPlayerPlaying = false;
  storeState.player.isPlaying = false;
  storeState.likes.likedIds = new Set<number>();

  mocks.fetchQuestions.mockResolvedValue({ dataList: [], pageInfo: firstPage });
  mocks.fetchAdminTracks.mockResolvedValue({ dataList: [], pageInfo: firstPage });
  mocks.fetchAdminSubscriptionPlans.mockResolvedValue([]);
  mocks.getSetting.mockResolvedValue('Original guide');
  mocks.fetchTags.mockResolvedValue([]);
  mocks.fetchUserLicenses.mockResolvedValue({ dataList: [], pageInfo: firstPage });
  mocks.fetchUserDetail.mockResolvedValue({
    id: 7,
    email: 'member@example.com',
    nickname: 'Member',
    role: 'USER',
    phonePersonal: null,
    phoneCompany: null,
    job: null,
    companyName: null,
    userType: 'INDIVIDUAL',
    isVerified: true,
    createdAt: '2026-07-01T00:00:00Z',
  });
  mocks.fetchMyLicenses.mockResolvedValue({ dataList: [], pageInfo: firstPage });
  mocks.fetchLikes.mockResolvedValue({ dataList: [] });
  mocks.fetchAlbumLikes.mockResolvedValue({ dataList: [] });
  mocks.fetchPlaylistDetail.mockResolvedValue(playlistDetail());
  mocks.loadPlayHistory.mockReturnValue([]);
  mocks.hydratePlayHistory.mockResolvedValue([]);
  mocks.downloadTrack.mockResolvedValue(new Blob(['audio'], { type: 'audio/mpeg' }));
  mocks.createQuestion.mockResolvedValue({ id: 1 });
  mocks.createAnswer.mockResolvedValue({ id: 2 });
  mocks.createTag.mockResolvedValue({ id: 1, name: 'new', type: 'GENRE' });
  mocks.updateTag.mockResolvedValue({ id: 1, name: 'updated', type: 'GENRE' });
  mocks.fetchTagDeletionImpact.mockImplementation((tagId: number) =>
    Promise.resolve({ id: tagId, name: 'Old Tag', type: 'MOOD', trackAssociationCount: 0 }),
  );

  Object.defineProperty(navigator, 'clipboard', {
    configurable: true,
    value: { writeText: vi.fn().mockResolvedValue(undefined) },
  });
});

describe('admin page behavior coverage', () => {
  it('loads, edits, saves, and resets the company certification guide', async () => {
    mocks.getSetting
      .mockReset()
      .mockResolvedValueOnce('Original guide')
      .mockResolvedValueOnce('Canonical saved guide')
      .mockResolvedValueOnce('Reloaded guide');
    mocks.updateSetting.mockResolvedValue(undefined);
    renderRoute(<SiteSettingsPage />, '/admin/settings');

    const guide = await screen.findByDisplayValue('Original guide');
    fireEvent.change(guide, { target: { value: 'Updated guide' } });
    fireEvent.click(screen.getByRole('button', { name: '저장' }));

    await waitFor(() =>
      expect(mocks.updateSetting).toHaveBeenCalledWith('COMPANY_CERT_GUIDE', 'Updated guide'),
    );
    expect(await screen.findByDisplayValue('Canonical saved guide')).toBeInTheDocument();
    expect(mocks.toast).toHaveBeenCalledWith('success', '설정이 저장되었습니다.');

    fireEvent.click(screen.getByRole('button', { name: '초기화' }));
    expect(await screen.findByDisplayValue('Reloaded guide')).toBeInTheDocument();
  });

  it('preserves all eight mobile plan columns through horizontal scroll', async () => {
    Object.defineProperty(window, 'innerWidth', { configurable: true, value: 767 });
    mocks.fetchAdminSubscriptionPlans.mockResolvedValue([
      {
        id: 1,
        name: 'STANDARD',
        description: null,
        userType: 'INDIVIDUAL',
        priceMonthly: 9900,
        priceYearly: 99000,
        downloadPerDay: -1,
        maxWhitelistChannels: 1,
        maxPlaylists: 3,
        isActive: true,
      },
      {
        id: 2,
        name: 'STANDARD',
        description: null,
        userType: 'BUSINESS',
        priceMonthly: 0,
        priceYearly: 0,
        downloadPerDay: 5,
        maxWhitelistChannels: 0,
        maxPlaylists: -1,
        isActive: false,
      },
    ]);

    renderRoute(<AdminSubscriptionManagePage />, '/admin/subscriptions');

    expect(subscriptionStylesSource).toMatch(/\.tableWrap\s*{[^}]*overflow-x:\s*auto;/s);
    expect(subscriptionStylesSource).toMatch(/\.table\s*{[^}]*min-width:\s*\d+px;/s);
    expect(subscriptionStylesSource).not.toMatch(/nth-child\s*\(/);
    expect((await screen.findByRole('table')).querySelectorAll('th')).toHaveLength(8);
    const planRows = (await screen.findAllByText('STANDARD')).map((name) => name.closest('tr')!);
    expect(planRows).toHaveLength(2);
    expect(planRows[0].querySelectorAll('td')).toHaveLength(8);
    expect(planRows[1].querySelectorAll('td')).toHaveLength(8);
    expect(within(planRows[0]).getByText('개인')).toBeInTheDocument();
    expect(within(planRows[1]).getByText('기업')).toBeInTheDocument();
    expect(within(planRows[0]).getByText('3')).toBeInTheDocument();
    expect(within(planRows[1]).getByText('무제한')).toBeInTheDocument();
    expect(within(planRows[1]).getByText('비활성')).toBeInTheDocument();
    expect(mocks.fetchAdminSubscriptionPlans).toHaveBeenCalledTimes(1);
  });

  it('filters tags and creates a trimmed usage tag before reloading', async () => {
    mocks.fetchTags
      .mockResolvedValueOnce([
        { id: 1, name: 'Rock', type: 'GENRE' },
        { id: 2, name: 'Shorts', type: 'USAGE' },
      ])
      .mockResolvedValueOnce([{ id: 3, name: 'Tutorial', type: 'USAGE' }]);

    renderRoute(<TagManagePage />, '/admin/tags');

    expect(await screen.findByText('Rock')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: /USAGE/ }));
    expect(screen.queryByText('Rock')).not.toBeInTheDocument();
    expect(screen.getByText('#Shorts')).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: '+ New Tag' }));
    fireEvent.change(screen.getByPlaceholderText('Tag name'), {
      target: { value: '  Tutorial  ' },
    });
    fireEvent.change(within(screen.getByRole('dialog')).getByRole('combobox'), {
      target: { value: 'USAGE' },
    });
    fireEvent.click(screen.getByRole('button', { name: 'Create' }));

    await waitFor(() =>
      expect(mocks.createTag).toHaveBeenCalledWith({ name: 'Tutorial', type: 'USAGE' }),
    );
    expect(await screen.findByText('#Tutorial')).toBeInTheDocument();
  });

  it('creates a notice with a selected attachment and trimmed fields', async () => {
    const router = renderRoute(<NoticeCreatePage />, '/admin/notices/new');
    const file = new File(['notice'], 'notice.txt', { type: 'text/plain' });

    fireEvent.change(screen.getByLabelText('제목'), {
      target: { value: '  Service update  ' },
    });
    fireEvent.change(screen.getByLabelText('내용'), {
      target: { value: '  Maintenance window  ' },
    });
    fireEvent.click(screen.getByLabelText('상단 고정'));
    fireEvent.change(document.querySelector('input[type="file"]')!, {
      target: { files: [file] },
    });
    fireEvent.click(screen.getByRole('button', { name: '등록' }));

    await waitFor(() =>
      expect(mocks.createNotice).toHaveBeenCalledWith({
        title: 'Service update',
        content: 'Maintenance window',
        isPinned: true,
        attachments: [file],
      }),
    );
    await waitFor(() => expect(router.state.location.pathname).toBe('/notices'));
  });

  it('updates a notice while deleting an existing attachment and adding a new one', async () => {
    mocks.fetchAdminNotice.mockResolvedValue({
      id: 9,
      title: 'Old title',
      content: 'Old content',
      isPinned: false,
      viewCount: 0,
      attachments: [{ id: 4, originalName: 'old.pdf', fileSize: 100 }],
      createdAt: '2026-07-01T00:00:00Z',
      updatedAt: '2026-07-01T00:00:00Z',
    });
    const router = renderRoute(
      <NoticeEditPage />,
      '/admin/notices/:noticeId/edit',
      '/admin/notices/9/edit',
    );

    expect(await screen.findByDisplayValue('Old title')).toBeInTheDocument();
    fireEvent.change(screen.getByDisplayValue('Old title'), { target: { value: 'Updated title' } });
    fireEvent.click(within(screen.getByText('old.pdf').closest('li')!).getByRole('button'));

    const added = new File(['new'], 'new.txt', { type: 'text/plain' });
    fireEvent.change(document.querySelector('input[type="file"]')!, {
      target: { files: [added] },
    });
    fireEvent.click(screen.getByRole('button', { name: '저장' }));

    await waitFor(() =>
      expect(mocks.updateNotice).toHaveBeenCalledWith(9, {
        title: 'Updated title',
        content: 'Old content',
        isPinned: false,
        deleteAttachmentIds: [4],
        newAttachments: [added],
      }),
    );
    await waitFor(() => expect(router.state.location.pathname).toBe('/notices/9'));
  });

  it('searches for a user and loads that users license ledger', async () => {
    const user = {
      id: 7,
      email: 'member@example.com',
      nickname: 'Member',
      role: 'USER',
      phonePersonal: null,
      phoneCompany: null,
      job: null,
      companyName: null,
      userType: 'INDIVIDUAL',
      isVerified: true,
      createdAt: '2026-07-01T00:00:00Z',
    };
    mocks.fetchUsers.mockResolvedValue({ dataList: [user], pageInfo: firstPage });
    mocks.fetchUserLicenses.mockResolvedValue({ dataList: [licenseItem()], pageInfo: firstPage });

    renderRoute(<LicenseManagePage />, '/admin/licenses');
    const input = screen.getByRole('textbox');
    fireEvent.change(input, { target: { value: 'member@example.com' } });
    fireEvent.keyDown(input, { key: 'Enter' });

    expect(await screen.findByRole('button', { name: /Member/ })).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: /Member/ }));

    await waitFor(() =>
      expect(mocks.fetchUserLicenses).toHaveBeenCalledWith(7, 1, 20, expect.any(AbortSignal)),
    );
    expect(mocks.fetchUserDetail).toHaveBeenCalledWith(7, expect.any(AbortSignal));
    expect(await screen.findByText('License Track')).toBeInTheDocument();
    expect(screen.getByText('LICENSE-CODE-1234567890')).toBeInTheDocument();
  });

  it('deletes the selected admin track only after modal confirmation', async () => {
    const track = {
      id: 15,
      title: 'Admin Track',
      artistName: 'Artist',
      duration: 180,
      bpm: 128,
      tonality: 'C#',
      thumbnail: '/track.jpg',
      playCount: 22,
      likeCount: 3,
      downloadCount: 4,
      isActive: true,
      tags: [],
      createdAt: '2026-07-01T00:00:00Z',
    };
    mocks.fetchAdminTracks
      .mockResolvedValueOnce({ dataList: [track], pageInfo: firstPage })
      .mockResolvedValueOnce({ dataList: [], pageInfo: { ...firstPage, total: 0 } });

    renderRoute(<TrackManagePage />, '/admin/tracks');
    expect(await screen.findByText('Admin Track')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '삭제' }));

    const dialog = await screen.findByRole('dialog');
    expect(within(dialog).getByText('Admin Track')).toBeInTheDocument();
    fireEvent.click(within(dialog).getByRole('button', { name: '삭제' }));

    await waitFor(() => expect(mocks.deleteTrack).toHaveBeenCalledWith(15));
    await waitFor(() => expect(mocks.fetchAdminTracks).toHaveBeenCalledTimes(2));
  });

  it('offers only legal question transitions and applies the canonical response status', async () => {
    mocks.fetchQuestions.mockResolvedValue({
      dataList: [
        {
          id: 51,
          title: 'Open question',
          category: 'PAYMENT',
          isPublic: false,
          status: 'OPEN',
          createdAt: '2026-07-01T00:00:00Z',
        },
        {
          id: 52,
          title: 'In progress question',
          category: 'OTHER',
          isPublic: true,
          status: 'IN_PROGRESS',
          createdAt: '2026-07-01T00:00:00Z',
        },
        {
          id: 53,
          title: 'Resolved question',
          category: 'OTHER',
          isPublic: true,
          status: 'RESOLVED',
          createdAt: '2026-07-01T00:00:00Z',
        },
        {
          id: 54,
          title: 'Closed question',
          category: 'OTHER',
          isPublic: true,
          status: 'CLOSED',
          createdAt: '2026-07-01T00:00:00Z',
        },
      ],
      pageInfo: { ...firstPage, total: 4, end: 4 },
    });
    mocks.updateQuestionStatus.mockResolvedValue({
      id: 51,
      title: 'Open question',
      category: 'PAYMENT',
      isPublic: false,
      status: 'CLOSED',
      createdAt: '2026-07-01T00:00:00Z',
    });

    renderRoute(<QuestionManagePage />, '/admin/questions', '/admin/questions?category=PAYMENT');
    expect(await screen.findByText('Open question')).toBeInTheDocument();
    expect(mocks.fetchQuestions).toHaveBeenCalledWith(
      {
        page: 1,
        size: 20,
        mine: false,
        category: 'PAYMENT',
        status: undefined,
      },
      expect.any(AbortSignal),
    );

    const statusValues = (title: string) =>
      within(screen.getByText(title).closest('tr')!)
        .getAllByRole('option')
        .map((option) => (option as HTMLOptionElement).value);
    expect(statusValues('Open question')).toEqual(['OPEN', 'IN_PROGRESS', 'CLOSED']);
    expect(statusValues('In progress question')).toEqual(['IN_PROGRESS', 'RESOLVED', 'CLOSED']);
    expect(statusValues('Resolved question')).toEqual(['RESOLVED', 'CLOSED']);
    const closedSelect = within(screen.getByText('Closed question').closest('tr')!).getByRole(
      'combobox',
    );
    expect(statusValues('Closed question')).toEqual(['CLOSED']);
    expect(closedSelect).toBeDisabled();

    const openSelect = within(screen.getByText('Open question').closest('tr')!).getByRole(
      'combobox',
    );
    fireEvent.change(openSelect, { target: { value: 'IN_PROGRESS' } });
    await waitFor(() => expect(mocks.updateQuestionStatus).toHaveBeenCalledWith(51, 'IN_PROGRESS'));
    await waitFor(() => expect(openSelect).toHaveValue('CLOSED'));
  });

  it('refreshes backend membership and pageInfo when a canonical status leaves the active filter', async () => {
    const refreshedPageInfo = {
      ...firstPage,
      total: 21,
      end: 2,
      next: true,
    };
    mocks.fetchQuestions
      .mockResolvedValueOnce({
        dataList: [
          {
            id: 57,
            title: 'Open question leaving the filter',
            category: 'OTHER',
            isPublic: true,
            status: 'OPEN',
            createdAt: '2026-07-01T00:00:00Z',
          },
        ],
        pageInfo: firstPage,
      })
      .mockResolvedValueOnce({
        dataList: [
          {
            id: 58,
            title: 'Backend-refreshed open question',
            category: 'OTHER',
            isPublic: true,
            status: 'OPEN',
            createdAt: '2026-07-02T00:00:00Z',
          },
        ],
        pageInfo: refreshedPageInfo,
      });
    mocks.updateQuestionStatus.mockResolvedValue({
      id: 57,
      title: 'Open question leaving the filter',
      category: 'OTHER',
      isPublic: true,
      status: 'CLOSED',
      createdAt: '2026-07-01T00:00:00Z',
    });

    renderRoute(<QuestionManagePage />, '/admin/questions', '/admin/questions?status=OPEN');
    const row = (await screen.findByText('Open question leaving the filter')).closest('tr')!;

    fireEvent.change(within(row).getByRole('combobox'), { target: { value: 'CLOSED' } });

    expect(await screen.findByText('Backend-refreshed open question')).toBeInTheDocument();
    expect(screen.queryByText('Open question leaving the filter')).not.toBeInTheDocument();
    expect(screen.getByText('21건')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '2페이지' })).toBeInTheDocument();
    expect(mocks.fetchQuestions).toHaveBeenCalledTimes(2);
    expect(mocks.fetchQuestions).toHaveBeenLastCalledWith(
      {
        page: 1,
        size: 20,
        mine: false,
        category: undefined,
        status: 'OPEN',
      },
      expect.any(AbortSignal),
    );
  });

  it('keeps a canonical status locally when it still matches the active filter', async () => {
    mocks.fetchQuestions.mockResolvedValue({
      dataList: [
        {
          id: 59,
          title: 'Canonical matching-filter question',
          category: 'OTHER',
          isPublic: true,
          status: 'OPEN',
          createdAt: '2026-07-01T00:00:00Z',
        },
      ],
      pageInfo: firstPage,
    });
    mocks.updateQuestionStatus.mockResolvedValue({
      id: 59,
      title: 'Canonical matching-filter question',
      category: 'OTHER',
      isPublic: true,
      status: 'OPEN',
      createdAt: '2026-07-01T00:00:00Z',
    });

    renderRoute(<QuestionManagePage />, '/admin/questions', '/admin/questions?status=OPEN');
    const row = (await screen.findByText('Canonical matching-filter question')).closest('tr')!;
    const statusSelect = within(row).getByRole('combobox');

    fireEvent.change(statusSelect, { target: { value: 'CLOSED' } });

    await waitFor(() => expect(mocks.updateQuestionStatus).toHaveBeenCalledWith(59, 'CLOSED'));
    await waitFor(() => expect(statusSelect).toBeEnabled());
    expect(statusSelect).toHaveValue('OPEN');
    expect(mocks.fetchQuestions).toHaveBeenCalledTimes(1);
  });

  it('prevents conflicting question status mutations while one request is pending', async () => {
    const pending = deferred<{
      id: number;
      title: string;
      category: 'OTHER';
      isPublic: boolean;
      status: 'IN_PROGRESS';
      createdAt: string;
    }>();
    mocks.fetchQuestions.mockResolvedValue({
      dataList: [
        {
          id: 55,
          title: 'First pending question',
          category: 'OTHER',
          isPublic: true,
          status: 'OPEN',
          createdAt: '2026-07-01T00:00:00Z',
        },
        {
          id: 56,
          title: 'Second pending question',
          category: 'OTHER',
          isPublic: true,
          status: 'OPEN',
          createdAt: '2026-07-01T00:00:00Z',
        },
      ],
      pageInfo: { ...firstPage, total: 2, end: 2 },
    });
    mocks.updateQuestionStatus.mockReturnValueOnce(pending.promise);

    renderRoute(<QuestionManagePage />, '/admin/questions');
    expect(await screen.findByText('First pending question')).toBeInTheDocument();
    const firstSelect = within(screen.getByText('First pending question').closest('tr')!).getByRole(
      'combobox',
    );
    const secondSelect = within(
      screen.getByText('Second pending question').closest('tr')!,
    ).getByRole('combobox');

    fireEvent.change(firstSelect, { target: { value: 'IN_PROGRESS' } });

    expect(firstSelect).toBeDisabled();
    expect(secondSelect).toBeDisabled();
    fireEvent.change(secondSelect, { target: { value: 'CLOSED' } });
    expect(mocks.updateQuestionStatus).toHaveBeenCalledTimes(1);

    await act(async () =>
      pending.resolve({
        id: 55,
        title: 'First pending question',
        category: 'OTHER',
        isPublic: true,
        status: 'IN_PROGRESS',
        createdAt: '2026-07-01T00:00:00Z',
      }),
    );
    await waitFor(() => expect(firstSelect).toBeEnabled());
    expect(secondSelect).toBeEnabled();
  });

  it('keeps the newest question filter projection when an older list response finishes last', async () => {
    const olderProjection = deferred<{
      dataList: Array<{
        id: number;
        title: string;
        category: 'PAYMENT';
        isPublic: boolean;
        status: 'OPEN';
        createdAt: string;
      }>;
      pageInfo: typeof firstPage;
    }>();
    const newerProjection = deferred<{
      dataList: Array<{
        id: number;
        title: string;
        category: 'OTHER';
        isPublic: boolean;
        status: 'OPEN';
        createdAt: string;
      }>;
      pageInfo: typeof firstPage;
    }>();
    mocks.fetchQuestions
      .mockReturnValueOnce(olderProjection.promise)
      .mockReturnValueOnce(newerProjection.promise);

    renderRoute(<QuestionManagePage />, '/admin/questions');
    fireEvent.change(screen.getAllByRole('combobox')[0], { target: { value: 'OTHER' } });
    await waitFor(() => expect(mocks.fetchQuestions).toHaveBeenCalledTimes(2));

    await act(async () =>
      newerProjection.resolve({
        dataList: [
          {
            id: 62,
            title: 'Newest projection question',
            category: 'OTHER',
            isPublic: true,
            status: 'OPEN',
            createdAt: '2026-07-02T00:00:00Z',
          },
        ],
        pageInfo: firstPage,
      }),
    );
    expect(await screen.findByText('Newest projection question')).toBeInTheDocument();

    await act(async () =>
      olderProjection.resolve({
        dataList: [
          {
            id: 61,
            title: 'Older projection question',
            category: 'PAYMENT',
            isPublic: true,
            status: 'OPEN',
            createdAt: '2026-07-01T00:00:00Z',
          },
        ],
        pageInfo: firstPage,
      }),
    );
    expect(screen.getByText('Newest projection question')).toBeInTheDocument();
    expect(screen.queryByText('Older projection question')).not.toBeInTheDocument();
  });

  it('refreshes the current question projection after a detached status success', async () => {
    const pendingMutation = deferred<{
      id: number;
      title: string;
      category: 'PAYMENT';
      isPublic: boolean;
      status: 'IN_PROGRESS';
      createdAt: string;
    }>();
    mocks.fetchQuestions
      .mockResolvedValueOnce({
        dataList: [
          {
            id: 71,
            title: 'Initiating projection question',
            category: 'PAYMENT',
            isPublic: true,
            status: 'OPEN',
            createdAt: '2026-07-01T00:00:00Z',
          },
        ],
        pageInfo: firstPage,
      })
      .mockResolvedValueOnce({
        dataList: [
          {
            id: 72,
            title: 'Current projection before refresh',
            category: 'OTHER',
            isPublic: true,
            status: 'OPEN',
            createdAt: '2026-07-02T00:00:00Z',
          },
        ],
        pageInfo: firstPage,
      })
      .mockResolvedValueOnce({
        dataList: [
          {
            id: 73,
            title: 'Current projection after refresh',
            category: 'OTHER',
            isPublic: true,
            status: 'OPEN',
            createdAt: '2026-07-03T00:00:00Z',
          },
        ],
        pageInfo: firstPage,
      });
    mocks.updateQuestionStatus.mockReturnValueOnce(pendingMutation.promise);

    renderRoute(<QuestionManagePage />, '/admin/questions');
    const initiatingRow = (await screen.findByText('Initiating projection question')).closest(
      'tr',
    )!;
    fireEvent.change(within(initiatingRow).getByRole('combobox'), {
      target: { value: 'IN_PROGRESS' },
    });
    fireEvent.change(screen.getAllByRole('combobox')[0], { target: { value: 'OTHER' } });
    expect(await screen.findByText('Current projection before refresh')).toBeInTheDocument();

    await act(async () =>
      pendingMutation.resolve({
        id: 71,
        title: 'Initiating projection question',
        category: 'PAYMENT',
        isPublic: true,
        status: 'IN_PROGRESS',
        createdAt: '2026-07-01T00:00:00Z',
      }),
    );

    expect(await screen.findByText('Current projection after refresh')).toBeInTheDocument();
    expect(mocks.fetchQuestions).toHaveBeenCalledTimes(3);
    expect(screen.queryByText('Current projection before refresh')).not.toBeInTheDocument();
  });

  it('does not attach a detached status failure to the current question projection', async () => {
    const pendingMutation = deferred<never>();
    mocks.fetchQuestions
      .mockResolvedValueOnce({
        dataList: [
          {
            id: 81,
            title: 'Failure initiating question',
            category: 'PAYMENT',
            isPublic: true,
            status: 'OPEN',
            createdAt: '2026-07-01T00:00:00Z',
          },
        ],
        pageInfo: firstPage,
      })
      .mockResolvedValueOnce({
        dataList: [
          {
            id: 82,
            title: 'Current projection after failure',
            category: 'OTHER',
            isPublic: true,
            status: 'OPEN',
            createdAt: '2026-07-02T00:00:00Z',
          },
        ],
        pageInfo: firstPage,
      });
    mocks.updateQuestionStatus.mockReturnValueOnce(pendingMutation.promise);

    renderRoute(<QuestionManagePage />, '/admin/questions');
    const initiatingRow = (await screen.findByText('Failure initiating question')).closest('tr')!;
    fireEvent.change(within(initiatingRow).getByRole('combobox'), {
      target: { value: 'IN_PROGRESS' },
    });
    fireEvent.change(screen.getAllByRole('combobox')[0], { target: { value: 'OTHER' } });
    expect(await screen.findByText('Current projection after failure')).toBeInTheDocument();

    await act(async () => pendingMutation.reject(new Error('stale failure')));

    expect(screen.getByText('Current projection after failure')).toBeInTheDocument();
    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
  });

  it('reports settings load and save failures without claiming success', async () => {
    mocks.getSetting.mockRejectedValueOnce(new Error('offline'));
    renderRoute(<SiteSettingsPage />, '/admin/settings');
    expect(await screen.findByText('설정을 불러오는 데 실패했습니다.')).toBeInTheDocument();

    mocks.getSetting.mockResolvedValueOnce('Guide');
    mocks.updateSetting.mockRejectedValueOnce(new Error('write failed'));
    renderRoute(<SiteSettingsPage />, '/admin/settings-second');
    expect(await screen.findByDisplayValue('Guide')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '저장' }));
    await waitFor(() =>
      expect(mocks.toast).toHaveBeenCalledWith('error', '설정 저장에 실패했습니다.'),
    );
  });

  it('edits and deletes tags through their explicit confirmation paths', async () => {
    const tag = { id: 5, name: 'Old Tag', type: 'MOOD' };
    mocks.fetchTags.mockResolvedValue([tag]);
    renderRoute(<TagManagePage />, '/admin/tags');

    expect(await screen.findByText('Old Tag')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'Edit' }));
    fireEvent.change(screen.getByPlaceholderText('Tag name'), { target: { value: 'Calm' } });
    fireEvent.click(within(screen.getByRole('dialog')).getByRole('button', { name: 'Save' }));
    await waitFor(() =>
      expect(mocks.updateTag).toHaveBeenCalledWith(5, { name: 'Calm', type: 'MOOD' }),
    );

    fireEvent.click(screen.getByRole('button', { name: 'Delete' }));
    const dialog = await screen.findByRole('dialog');
    fireEvent.click(await within(dialog).findByRole('button', { name: 'Delete' }));
    await waitFor(() => expect(mocks.deleteTag).toHaveBeenCalledWith(5));
  });

  it('rejects excessive notice attachments and surfaces create failures', async () => {
    mocks.createNotice.mockRejectedValueOnce({ response: { status: 400 } });
    renderRoute(<NoticeCreatePage />, '/admin/notices/new');
    const input = document.querySelector('input[type="file"]')!;
    const files = Array.from(
      { length: 6 },
      (_, index) => new File(['x'], `notice-${index}.txt`, { type: 'text/plain' }),
    );
    fireEvent.change(input, { target: { files } });
    expect(screen.getByText(/첨부파일은 최대/)).toBeInTheDocument();

    fireEvent.change(screen.getByLabelText('제목'), { target: { value: 'Title' } });
    fireEvent.change(screen.getByLabelText('내용'), {
      target: { value: 'Content' },
    });
    fireEvent.click(screen.getByRole('button', { name: '등록' }));
    expect(await screen.findByText(/공지사항을 등록하지 못했습니다\./)).toBeInTheDocument();
  });

  it('deletes a loaded notice only after confirmation', async () => {
    mocks.fetchAdminNotice.mockResolvedValue({
      id: 10,
      title: 'Delete me',
      content: 'Content',
      isPinned: false,
      viewCount: 0,
      attachments: [],
      createdAt: '2026-07-01T00:00:00Z',
      updatedAt: '2026-07-01T00:00:00Z',
    });
    const router = renderRoute(
      <NoticeEditPage />,
      '/admin/notices/:noticeId/edit',
      '/admin/notices/10/edit',
    );
    expect(await screen.findByDisplayValue('Delete me')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '공지사항 삭제' }));
    fireEvent.click(within(screen.getByRole('dialog')).getByRole('button', { name: '삭제' }));
    await waitFor(() => expect(mocks.deleteNotice).toHaveBeenCalledWith(10));
    await waitFor(() => expect(router.state.location.pathname).toBe('/notices'));
  });

  it('applies track filters and search terms, then exposes a failed delete', async () => {
    const track = {
      id: 16,
      title: 'Searchable Track',
      artistName: 'Artist',
      duration: 180,
      bpm: 90,
      tonality: 'A',
      thumbnail: null,
      playCount: 0,
      likeCount: 0,
      downloadCount: 0,
      isActive: false,
      tags: [],
      createdAt: '2026-07-01T00:00:00Z',
    };
    mocks.fetchAdminTracks.mockResolvedValue({ dataList: [track], pageInfo: firstPage });
    mocks.deleteTrack.mockRejectedValueOnce(new Error('delete denied'));
    renderRoute(<TrackManagePage />, '/admin/tracks');
    expect(await screen.findByText('Searchable Track')).toBeInTheDocument();

    fireEvent.change(screen.getByRole('combobox'), { target: { value: 'inactive' } });
    await waitFor(() =>
      expect(mocks.fetchAdminTracks).toHaveBeenCalledWith(
        { page: 1, size: 20, is_active: false },
        expect.any(AbortSignal),
      ),
    );
    const search = screen.getByPlaceholderText('곡 제목 검색');
    fireEvent.change(search, { target: { value: ' Searchable ' } });
    fireEvent.keyDown(search, { key: 'Enter' });
    await waitFor(() =>
      expect(mocks.fetchAdminTracks).toHaveBeenCalledWith(
        {
          page: 1,
          size: 20,
          is_active: false,
          keyword: 'Searchable',
        },
        expect.any(AbortSignal),
      ),
    );

    fireEvent.click(screen.getByRole('button', { name: '삭제' }));
    fireEvent.click(within(screen.getByRole('dialog')).getByRole('button', { name: '삭제' }));
    expect(await screen.findByText(/음원을 삭제하지 못했습니다/)).toBeInTheDocument();
  });

  it('shows admin question and plan load failures as terminal page states', async () => {
    mocks.fetchQuestions.mockRejectedValueOnce(new Error('questions unavailable'));
    renderRoute(<QuestionManagePage />, '/admin/questions');
    expect(await screen.findByText('문의 목록을 불러오지 못했습니다.')).toBeInTheDocument();

    mocks.fetchAdminSubscriptionPlans.mockRejectedValueOnce(new Error('plans unavailable'));
    renderRoute(<AdminSubscriptionManagePage />, '/admin/subscriptions-second');
    expect(await screen.findByText('구독 플랜 목록을 불러올 수 없습니다.')).toBeInTheDocument();
  });

  it('keeps notice editing available after a save failure and handles load failure separately', async () => {
    mocks.fetchAdminNotice.mockResolvedValueOnce({
      id: 11,
      title: 'Retry notice',
      content: 'Retry content',
      isPinned: true,
      viewCount: 0,
      attachments: [],
      createdAt: '2026-07-01T00:00:00Z',
      updatedAt: '2026-07-01T00:00:00Z',
    });
    mocks.updateNotice.mockRejectedValueOnce({ response: { status: 400 } });
    renderRoute(<NoticeEditPage />, '/admin/notices/:noticeId/edit', '/admin/notices/11/edit');
    expect(await screen.findByDisplayValue('Retry notice')).toBeInTheDocument();
    fireEvent.change(screen.getByDisplayValue('Retry notice'), {
      target: { value: 'Retry again' },
    });
    fireEvent.click(screen.getByRole('button', { name: '저장' }));
    expect(await screen.findByText(/공지사항을 수정하지 못했습니다\./)).toBeInTheDocument();
    expect(screen.getByDisplayValue('Retry again')).toBeEnabled();

    mocks.fetchAdminNotice.mockRejectedValueOnce(new Error('load failed'));
    renderRoute(<NoticeEditPage />, '/admin/notices/:noticeId/edit', '/admin/notices/12/edit');
    expect(await screen.findByText('공지사항을 불러오지 못했습니다.')).toBeInTheDocument();
  });

  it('surfaces an admin question status update failure without applying the transition', async () => {
    mocks.fetchQuestions.mockResolvedValue({
      dataList: [
        {
          id: 52,
          title: 'Status retry question',
          category: 'OTHER',
          isPublic: true,
          status: 'OPEN',
          createdAt: '2026-07-01T00:00:00Z',
        },
      ],
      pageInfo: firstPage,
    });
    mocks.updateQuestionStatus.mockRejectedValueOnce(new Error('transition denied'));
    renderRoute(<QuestionManagePage />, '/admin/questions');
    expect(await screen.findByText('Status retry question')).toBeInTheDocument();
    fireEvent.change(screen.getAllByRole('combobox')[2], { target: { value: 'CLOSED' } });
    expect(await screen.findByText('상태 변경에 실패했습니다.')).toBeInTheDocument();
    expect(mocks.updateQuestionStatus).toHaveBeenCalledWith(52, 'CLOSED');
    expect(mocks.fetchQuestions).toHaveBeenCalledTimes(1);
    expect(screen.getByText('Status retry question')).toBeInTheDocument();
    expect(
      within(screen.getByText('Status retry question').closest('tr')!).getByRole('combobox'),
    ).toBeEnabled();
    expect(
      within(screen.getByText('Status retry question').closest('tr')!).getByRole('combobox'),
    ).toHaveValue('OPEN');
  });
});

describe('subscriber page behavior coverage', () => {
  it('deletes selected local play history entries and then clears all history', async () => {
    const entries = [
      {
        track: {
          id: 1,
          title: 'History One',
          artistName: 'Artist',
          duration: 180,
          thumbnail: null,
          waveformData: '[0.2,0.8]',
        },
        playedAt: '2026-07-01T00:00:00Z',
      },
      {
        track: {
          id: 2,
          title: 'History Two',
          artistName: 'Artist',
          duration: 200,
          thumbnail: '/two.jpg',
          waveformData: '[0.1,0.9]',
        },
        playedAt: '2026-07-02T00:00:00Z',
      },
    ];
    mocks.loadPlayHistory.mockReturnValueOnce(entries).mockReturnValueOnce([entries[1]]);
    mocks.hydratePlayHistory.mockResolvedValue(entries);

    renderRoute(<PlayHistoryPage />, '/history');
    expect(await screen.findByText('History One')).toBeInTheDocument();
    const checkboxes = screen.getAllByRole('checkbox');
    fireEvent.click(checkboxes[1]);
    fireEvent.click(screen.getByRole('button', { name: '선택 삭제 (1)' }));

    expect(mocks.removePlayHistoryEntry).toHaveBeenCalledWith(1);
    expect(await screen.findByText('History Two')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '전체 삭제' }));
    expect(mocks.clearPlayHistory).toHaveBeenCalledTimes(1);
    expect(screen.getByText('재생 기록이 없습니다.')).toBeInTheDocument();
  });

  it('downloads a licensed track and copies the full code from its detail modal', async () => {
    const item = licenseItem();
    mocks.fetchMyLicenses.mockResolvedValue({ dataList: [item], pageInfo: firstPage });

    renderRoute(<LicenseListPage />, '/licenses');
    expect(await screen.findByText('License Track')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '↓ 다운로드' }));

    await waitFor(() => expect(mocks.downloadTrack).toHaveBeenCalledWith(41));
    expect(mocks.triggerBlobDownload).toHaveBeenCalledWith(expect.any(Blob), 'License Track.mp3');

    fireEvent.click(screen.getByRole('button', { name: '상세' }));
    const dialog = screen.getByRole('dialog');
    expect(within(dialog).getByText('LICENSE-CODE-1234567890')).toBeInTheDocument();
    fireEvent.click(within(dialog).getByRole('button', { name: '코드 복사' }));
    await waitFor(() =>
      expect(navigator.clipboard.writeText).toHaveBeenCalledWith('LICENSE-CODE-1234567890'),
    );
  });

  it('renders a fetched license detail with optional track metadata', async () => {
    mocks.fetchLicenseDetail.mockResolvedValue({
      ...licenseItem(),
      user: { id: 7, nickname: 'Member' },
    });

    renderRoute(<LicenseDetailPage />, '/licenses/:licenseId', '/licenses/31');
    expect(await screen.findByRole('heading', { name: '라이선스 상세' })).toBeInTheDocument();
    expect(screen.getByText('License Track')).toBeInTheDocument();
    expect(screen.getByText('120')).toBeInTheDocument();
    expect(screen.getByText('Member')).toBeInTheDocument();
    expect(mocks.fetchLicenseDetail).toHaveBeenCalledWith(31, expect.any(AbortSignal));
  });

  it('validates and submits a private question with an attachment', async () => {
    const router = renderRoute(<QuestionCreatePage />, '/questions/new');
    fireEvent.click(screen.getByRole('button', { name: '등록' }));
    expect(screen.getByText('제목을 입력해 주세요.')).toBeInTheDocument();

    fireEvent.change(screen.getByLabelText('제목'), { target: { value: '  Download issue  ' } });
    fireEvent.change(screen.getByLabelText('카테고리'), { target: { value: 'DOWNLOAD' } });
    fireEvent.change(screen.getByLabelText('내용'), { target: { value: '  Cannot download  ' } });
    fireEvent.click(screen.getByRole('checkbox', { name: '공개 문의' }));
    const file = new File(['log'], 'error.log', { type: 'text/plain' });
    fireEvent.change(document.querySelector('input[type="file"]')!, {
      target: { files: [file] },
    });
    fireEvent.click(screen.getByRole('button', { name: '등록' }));

    await waitFor(() =>
      expect(mocks.createQuestion).toHaveBeenCalledWith({
        title: 'Download issue',
        content: 'Cannot download',
        category: 'DOWNLOAD',
        isPublic: false,
        attachments: [file],
      }),
    );
    await waitFor(() => expect(router.state.location.pathname).toBe('/questions'));
  });

  it('loads filtered personal questions and navigates from the selected row', async () => {
    mocks.fetchQuestions.mockResolvedValue({
      dataList: [
        {
          id: 61,
          title: 'My private question',
          category: 'COPYRIGHT',
          isPublic: false,
          status: 'IN_PROGRESS',
          createdAt: '2026-07-01T00:00:00Z',
        },
      ],
      pageInfo: firstPage,
    });
    const router = renderRoute(
      <QuestionListPage />,
      '/questions',
      '/questions?tab=mine&category=COPYRIGHT&status=IN_PROGRESS',
    );

    expect(await screen.findByText('My private question')).toBeInTheDocument();
    expect(mocks.fetchQuestions).toHaveBeenCalledWith(
      {
        page: 1,
        size: 20,
        mine: true,
        category: 'COPYRIGHT',
        status: 'IN_PROGRESS',
      },
      expect.any(AbortSignal),
    );
    fireEvent.click(screen.getByText('My private question').closest('tr')!);
    await waitFor(() => expect(router.state.location.pathname).toBe('/questions/61'));
  });

  it('downloads an attachment, posts an answer, and refreshes question detail', async () => {
    const attachmentBlob = new Blob(['attachment']);
    const detail = {
      id: 71,
      title: 'Question detail',
      content: 'Please help',
      category: 'OTHER',
      isPublic: true,
      status: 'OPEN',
      user: { id: 7, nickname: 'Admin' },
      attachments: [{ id: 8, originalName: 'evidence.txt', fileSize: 100 }],
      answers: [
        {
          id: 9,
          content: 'Existing answer',
          user: { id: 7, nickname: 'Admin', role: 'ADMIN' },
          createdAt: '2026-07-01T00:00:00Z',
        },
      ],
      createdAt: '2026-07-01T00:00:00Z',
    };
    mocks.fetchQuestionDetail.mockResolvedValue(detail);
    mocks.downloadAttachment.mockResolvedValue(attachmentBlob);

    renderRoute(<QuestionDetailPage />, '/questions/:questionId', '/questions/71');
    expect(await screen.findByText('Question detail')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'evidence.txt' }));
    expect(mocks.downloadAttachment).toHaveBeenCalledWith(71, 8, expect.any(AbortSignal));
    await waitFor(() =>
      expect(mocks.triggerBlobDownload).toHaveBeenCalledWith(attachmentBlob, 'evidence.txt'),
    );

    fireEvent.change(screen.getByPlaceholderText('답변 내용을 입력하세요'), {
      target: { value: '  New answer  ' },
    });
    fireEvent.click(screen.getByRole('button', { name: '답변 등록' }));
    await waitFor(() => expect(mocks.createAnswer).toHaveBeenCalledWith(71, 'New answer'));
    await waitFor(() => expect(mocks.fetchQuestionDetail).toHaveBeenCalledTimes(2));
  });

  it('plays, downloads, queues, and removes a track from a playlist detail', async () => {
    mocks.fetchPlaylistDetail
      .mockResolvedValueOnce(playlistDetail())
      .mockResolvedValueOnce({ ...playlistDetail(), tracks: [playlistDetail().tracks[1]] });

    renderRoute(<PlaylistDetailPage />, '/playlists/:playlistId', '/playlists/3');
    expect(await screen.findByText('Focus Mix')).toBeInTheDocument();
    const normalizedFirstTrack = {
      id: 11,
      title: 'First Track',
      artistName: 'Artist',
      duration: 180,
      thumbnail: null,
      waveformData: null,
      bpm: 110,
      tonality: 'C',
    };
    await waitFor(() =>
      expect(mocks.playerSetContext).toHaveBeenLastCalledWith([
        normalizedFirstTrack,
        expect.objectContaining({ id: 12 }),
      ]),
    );
    fireEvent.click(document.querySelector('button[aria-label="Play"]')!);
    expect(mocks.playerPlay).toHaveBeenCalledWith(normalizedFirstTrack);

    fireEvent.click(screen.getAllByTitle('다운로드')[0]);
    await waitFor(() => expect(mocks.downloadTrack).toHaveBeenCalledWith(11));

    fireEvent.click(screen.getByRole('button', { name: '전체 대기열 추가' }));
    expect(mocks.playerAddToQueue).toHaveBeenCalledTimes(2);
    expect(mocks.playerAddToQueue).toHaveBeenNthCalledWith(1, normalizedFirstTrack);

    fireEvent.click(screen.getAllByRole('button', { name: '삭제' })[0]);
    const dialog = await screen.findByRole('dialog');
    fireEvent.click(within(dialog).getByRole('button', { name: '삭제' }));
    await waitFor(() => expect(mocks.removeTrackFromPlaylist).toHaveBeenCalledWith(3, 11));
  });

  it('saves playlist metadata and reordered tracks after meaningful edits', async () => {
    mocks.fetchPlaylistDetail.mockResolvedValue(playlistDetail());
    const router = renderRoute(
      <PlaylistEditPage />,
      '/playlists/:playlistId/edit',
      '/playlists/3/edit',
    );

    expect(await screen.findByDisplayValue('Focus Mix')).toBeInTheDocument();
    fireEvent.change(screen.getByDisplayValue('Focus Mix'), { target: { value: ' Focus Mix 2 ' } });
    fireEvent.click(screen.getAllByRole('button', { name: 'Move down' })[0]);
    fireEvent.click(screen.getByRole('button', { name: '저장' }));

    await waitFor(() =>
      expect(mocks.updatePlaylist).toHaveBeenCalledWith(3, {
        title: 'Focus Mix 2',
        description: 'Work tracks',
        thumbnail: undefined,
      }),
    );
    expect(mocks.reorderTracks).toHaveBeenCalledWith(3, [
      { trackId: 12, trackOrder: 0 },
      { trackId: 11, trackOrder: 1 },
    ]);
    await waitFor(() => expect(router.state.location.pathname).toBe('/playlists/3'));
  });

  it('covers liked track and album actions across both tabs', async () => {
    mocks.fetchLikes.mockResolvedValue({
      dataList: [
        {
          trackId: 21,
          title: 'Liked Track',
          artistName: 'Liked Artist',
          duration: 95,
          bpm: 100,
          tonality: 'Am',
          createdAt: '2026-07-01T00:00:00Z',
        },
      ],
    });
    mocks.fetchAlbumLikes.mockResolvedValue({
      dataList: [
        {
          albumId: 22,
          title: 'Liked Album',
          description: null,
          thumbnailUrl: null,
          trackCount: 4,
          likeCount: 2,
          createdAt: '2026-07-01T00:00:00Z',
        },
      ],
    });

    renderRoute(<LikeListPage />, '/likes');
    expect(await screen.findByText('Liked Track')).toBeInTheDocument();
    const normalizedLikedTrack = {
      id: 21,
      title: 'Liked Track',
      artistName: 'Liked Artist',
      duration: 95,
      thumbnail: null,
      waveformData: null,
      bpm: 100,
      tonality: 'Am',
    };
    await waitFor(() =>
      expect(mocks.playerSetContext).toHaveBeenLastCalledWith([normalizedLikedTrack]),
    );
    fireEvent.click(document.querySelector('button[aria-label="Play"]')!);
    expect(mocks.playerPlay).toHaveBeenCalledWith(normalizedLikedTrack);

    fireEvent.click(screen.getByTitle('다운로드'));
    await waitFor(() => expect(mocks.downloadTrack).toHaveBeenCalledWith(21));
    fireEvent.click(screen.getByTitle('좋아요 해제'));
    await waitFor(() => expect(mocks.removeLike).toHaveBeenCalledWith(21));

    fireEvent.click(screen.getByRole('button', { name: '앨범' }));
    expect(await screen.findByText('Liked Album')).toBeInTheDocument();
    fireEvent.click(screen.getByTitle('좋아요 해제'));
    await waitFor(() => expect(mocks.removeAlbumLike).toHaveBeenCalledWith(22));
  });

  it('reports license list, detail, download, and clipboard failures', async () => {
    mocks.fetchMyLicenses.mockRejectedValueOnce(new Error('license list unavailable'));
    renderRoute(<LicenseListPage />, '/licenses');
    expect(await screen.findByText('라이선스를 불러오지 못했습니다.')).toBeInTheDocument();

    mocks.fetchLicenseDetail.mockRejectedValueOnce(new Error('license detail unavailable'));
    renderRoute(<LicenseDetailPage />, '/licenses/:licenseId', '/licenses/99');
    expect(await screen.findByText('라이선스 정보를 불러오지 못했습니다.')).toBeInTheDocument();

    mocks.fetchMyLicenses.mockResolvedValueOnce({ dataList: [licenseItem()], pageInfo: firstPage });
    mocks.downloadTrack.mockRejectedValueOnce(new Error('download blocked'));
    Object.defineProperty(navigator, 'clipboard', {
      configurable: true,
      value: { writeText: vi.fn().mockRejectedValue(new Error('clipboard blocked')) },
    });
    renderRoute(<LicenseListPage />, '/licenses-second');
    expect(await screen.findByText('License Track')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '↓ 다운로드' }));
    await waitFor(() =>
      expect(mocks.toast).toHaveBeenCalledWith(
        'error',
        '다운로드에 실패했습니다. 구독이 활성 상태인지 확인하세요.',
      ),
    );
    fireEvent.click(screen.getByRole('button', { name: '상세' }));
    fireEvent.click(within(screen.getByRole('dialog')).getByRole('button', { name: '코드 복사' }));
    await waitFor(() => expect(mocks.toast).toHaveBeenCalledWith('error', '복사에 실패했습니다.'));
  });

  it('surfaces question submission and answer errors and supports owner deletion', async () => {
    mocks.createQuestion.mockRejectedValueOnce(new Error('submit failed'));
    renderRoute(<QuestionCreatePage />, '/questions/new');
    fireEvent.change(screen.getByLabelText('제목'), { target: { value: 'Question' } });
    fireEvent.change(screen.getByLabelText('카테고리'), { target: { value: 'OTHER' } });
    fireEvent.change(screen.getByLabelText('내용'), { target: { value: 'Content' } });
    fireEvent.click(screen.getByRole('button', { name: '등록' }));
    expect(await screen.findByText('문의 등록에 실패했습니다.')).toBeInTheDocument();

    mocks.fetchQuestionDetail.mockResolvedValue({
      id: 72,
      title: 'Owned question',
      content: 'Content',
      category: 'OTHER',
      isPublic: true,
      status: 'OPEN',
      user: { id: 7, nickname: 'Admin' },
      attachments: null,
      answers: null,
      createdAt: '2026-07-01T00:00:00Z',
    });
    mocks.createAnswer.mockRejectedValueOnce(new Error('answer failed'));
    const router = renderRoute(<QuestionDetailPage />, '/questions/:questionId', '/questions/72');
    expect(await screen.findByText('Owned question')).toBeInTheDocument();
    fireEvent.change(screen.getByPlaceholderText('답변 내용을 입력하세요'), {
      target: { value: 'Answer' },
    });
    fireEvent.click(screen.getByRole('button', { name: '답변 등록' }));
    expect(await screen.findByText('답변 등록에 실패했습니다.')).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: '삭제' }));
    fireEvent.click(within(screen.getByRole('dialog')).getByRole('button', { name: '삭제' }));
    await waitFor(() => expect(mocks.deleteQuestion).toHaveBeenCalledWith(72));
    await waitFor(() => expect(router.state.location.pathname).toBe('/questions'));
  });

  it('covers playlist failure, empty, pause, resume, like, and download-error branches', async () => {
    storeState.player.currentTrack = { id: 11 };
    storeState.player.isPlaying = true;
    mocks.downloadTrack.mockRejectedValueOnce(new Error('blocked'));
    renderRoute(<PlaylistDetailPage />, '/playlists/:playlistId', '/playlists/3');
    expect(await screen.findByText('Focus Mix')).toBeInTheDocument();
    fireEvent.click(document.querySelector('button[aria-label="Pause"]')!);
    expect(mocks.playerPause).toHaveBeenCalledTimes(1);
    fireEvent.click(screen.getAllByTitle('좋아요')[0]);
    expect(mocks.likeToggle).toHaveBeenCalledWith(11);
    fireEvent.click(screen.getAllByTitle('다운로드')[0]);
    await waitFor(() =>
      expect(mocks.toast).toHaveBeenCalledWith('error', '다운로드에 실패했습니다.'),
    );

    mocks.fetchPlaylistDetail.mockRejectedValueOnce(new Error('playlist unavailable'));
    renderRoute(<PlaylistDetailPage />, '/playlists/:playlistId', '/playlists/4');
    expect(await screen.findByText('재생목록을 불러오지 못했습니다.')).toBeInTheDocument();

    mocks.fetchPlaylistDetail.mockResolvedValueOnce({ ...playlistDetail(), id: 5, tracks: [] });
    renderRoute(<PlaylistDetailPage />, '/playlists/:playlistId', '/playlists/5');
    expect(await screen.findByText(/아직 수록곡이 없습니다/)).toBeInTheDocument();
  });

  it('removes a playlist track and deletes the playlist through separate confirmations', async () => {
    mocks.fetchPlaylistDetail
      .mockResolvedValueOnce(playlistDetail())
      .mockResolvedValueOnce({ ...playlistDetail(), tracks: [playlistDetail().tracks[1]] });
    const router = renderRoute(
      <PlaylistEditPage />,
      '/playlists/:playlistId/edit',
      '/playlists/3/edit',
    );
    expect(await screen.findByText('First Track')).toBeInTheDocument();

    fireEvent.click(screen.getAllByTitle('삭제')[0]);
    fireEvent.click(within(screen.getByRole('dialog')).getByRole('button', { name: '삭제' }));
    await waitFor(() => expect(mocks.removeTrackFromPlaylist).toHaveBeenCalledWith(3, 11));

    fireEvent.click(screen.getByText('재생목록 삭제').closest('section')!.querySelector('button')!);
    fireEvent.click(within(screen.getByRole('dialog')).getByRole('button', { name: '삭제' }));
    await waitFor(() => expect(mocks.deletePlaylist).toHaveBeenCalledWith(3));
    await waitFor(() => expect(router.state.location.pathname).toBe('/playlists'));
  });

  it('renders independent track and album like loading failures', async () => {
    mocks.fetchLikes.mockRejectedValueOnce(new Error('track likes unavailable'));
    mocks.fetchAlbumLikes.mockRejectedValueOnce(new Error('album likes unavailable'));
    renderRoute(<LikeListPage />, '/likes');
    expect(await screen.findByText('좋아요 목록을 불러오지 못했습니다.')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '앨범' }));
    expect(await screen.findByText('좋아요 앨범을 불러오지 못했습니다.')).toBeInTheDocument();
  });

  it('walks through question validation, attachment removal, and filtered empty results', async () => {
    renderRoute(<QuestionCreatePage />, '/questions/new');
    fireEvent.change(screen.getByLabelText('제목'), { target: { value: 'Question' } });
    fireEvent.click(screen.getByRole('button', { name: '등록' }));
    expect(screen.getByText('내용을 입력해 주세요.')).toBeInTheDocument();
    fireEvent.change(screen.getByLabelText('내용'), { target: { value: 'Content' } });
    fireEvent.click(screen.getByRole('button', { name: '등록' }));
    expect(screen.getByText('카테고리를 선택해 주세요.')).toBeInTheDocument();

    const file = new File(['evidence'], 'evidence.txt', { type: 'text/plain' });
    fireEvent.change(document.querySelector('input[type="file"]')!, { target: { files: [file] } });
    expect(screen.getByText('evidence.txt')).toBeInTheDocument();
    fireEvent.click(within(screen.getByText('evidence.txt').closest('li')!).getByRole('button'));
    expect(screen.queryByText('evidence.txt')).not.toBeInTheDocument();

    mocks.fetchQuestions.mockResolvedValueOnce({
      dataList: [],
      pageInfo: { ...firstPage, total: 0 },
    });
    renderRoute(<QuestionListPage />, '/questions', '/questions?tab=mine');
    expect(await screen.findByText('등록된 문의가 없습니다.')).toBeInTheDocument();
    expect(mocks.fetchQuestions).toHaveBeenLastCalledWith(
      { page: 1, size: 20, mine: true },
      expect.any(AbortSignal),
    );
  });

  it('moves to the final history page, selects all visible rows, and returns to page one after deletion', async () => {
    const entries = Array.from({ length: 21 }, (_, index) => ({
      track: {
        id: index + 1,
        title: `History ${index + 1}`,
        artistName: 'Artist',
        duration: 180,
        thumbnail: null,
        waveformData: '[0.2,0.8]',
      },
      playedAt: `2026-07-${String((index % 9) + 1).padStart(2, '0')}T00:00:00Z`,
    }));
    mocks.loadPlayHistory.mockReturnValueOnce(entries).mockReturnValueOnce(entries.slice(0, 20));
    mocks.hydratePlayHistory.mockResolvedValue(entries);
    renderRoute(<PlayHistoryPage />, '/history');
    expect(await screen.findByText('History 1')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '2페이지' }));
    expect(screen.getByText('History 21')).toBeInTheDocument();
    fireEvent.click(screen.getAllByRole('checkbox')[0]);
    fireEvent.click(screen.getByRole('button', { name: '선택 삭제 (1)' }));
    expect(mocks.removePlayHistoryEntry).toHaveBeenCalledWith(21);
    expect(screen.getByText('History 1')).toBeInTheDocument();
  });

  it('shows playlist edit load and save failures while preserving user edits', async () => {
    mocks.fetchPlaylistDetail.mockRejectedValueOnce(new Error('load failed'));
    renderRoute(<PlaylistEditPage />, '/playlists/:playlistId/edit', '/playlists/8/edit');
    expect(await screen.findByText('재생목록을 불러오지 못했습니다.')).toBeInTheDocument();

    mocks.fetchPlaylistDetail.mockResolvedValueOnce(playlistDetail());
    mocks.updatePlaylist.mockRejectedValueOnce(new Error('save failed'));
    renderRoute(<PlaylistEditPage />, '/playlists/:playlistId/edit', '/playlists/9/edit');
    expect(await screen.findByDisplayValue('Focus Mix')).toBeInTheDocument();
    fireEvent.change(screen.getByDisplayValue('Focus Mix'), { target: { value: 'Unsaved Mix' } });
    fireEvent.click(screen.getByRole('button', { name: '저장' }));
    expect(await screen.findByText('저장에 실패했습니다.')).toBeInTheDocument();
    expect(screen.getByDisplayValue('Unsaved Mix')).toBeInTheDocument();
  });
});
