import { beforeEach, describe, expect, expectTypeOf, it, vi } from 'vitest';

vi.mock('@/api/client', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}));

import client from '@/api/client';
import * as albums from '@/api/albums';
import * as downloads from '@/api/downloads';
import * as licenses from '@/api/licenses';
import * as likes from '@/api/likes';
import * as notices from '@/api/notices';
import * as payments from '@/api/payments';
import * as playlists from '@/api/playlists';
import * as questions from '@/api/questions';
import * as settings from '@/api/settings';
import * as subscriptions from '@/api/subscriptions';
import * as tags from '@/api/tags';
import * as tracks from '@/api/tracks';
import * as userSubscriptions from '@/api/userSubscriptions';
import * as whitelistChannels from '@/api/whitelistChannels';
import * as companyCerts from '@/api/companyCerts';

const mockedClient = vi.mocked(client);
const payload = { id: 7, title: 'Result' };
const paged = { dataList: [payload], pageInfo: { currentPage: 1, totalPages: 1 } };
const BACKEND_PAYMENT_ORDER_STATUSES = [
  'READY',
  'IN_PROGRESS',
  'PROCESSING',
  'PROVIDER_SUCCEEDED',
  'PENDING_PROVIDER_CONFIRMATION',
  'DONE',
  'FAILED',
  'CANCELLED',
  'EXPIRED',
] as const;

function apiResponse<T>(data: T) {
  return { data: { data } };
}

describe('domain API contracts', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('uses the album list/detail/mutation contracts', async () => {
    mockedClient.get
      .mockResolvedValueOnce({ data: paged })
      .mockResolvedValueOnce(apiResponse(payload));
    mockedClient.post
      .mockResolvedValueOnce(apiResponse(payload))
      .mockResolvedValueOnce(apiResponse(payload));
    mockedClient.put
      .mockResolvedValueOnce(apiResponse(payload))
      .mockResolvedValueOnce(apiResponse(payload));
    mockedClient.delete.mockResolvedValue({});

    await expect(albums.fetchAlbums({ page: 2, size: 10, sort: 'popular' })).resolves.toEqual(
      paged,
    );
    expect(mockedClient.get).toHaveBeenNthCalledWith(1, '/albums', {
      params: { page: 2, size: 10, sort: 'popular' },
    });
    await expect(albums.fetchAlbumDetail(7)).resolves.toEqual(payload);

    const form = new FormData();
    form.append('title', 'Album');
    await expect(albums.createAlbum(form)).resolves.toEqual(payload);
    expect(mockedClient.post).toHaveBeenNthCalledWith(1, '/albums', form, { timeout: 60_000 });
    await expect(albums.updateAlbum(7, form)).resolves.toEqual(payload);
    expect(mockedClient.put).toHaveBeenNthCalledWith(1, '/albums/7', form, { timeout: 60_000 });

    await albums.addTrackToAlbum(7, 11);
    expect(mockedClient.post).toHaveBeenNthCalledWith(2, '/albums/7/tracks', { trackId: 11 });
    await albums.reorderAlbumTracks(7, [{ trackId: 11, order: 0 }]);
    expect(mockedClient.put).toHaveBeenNthCalledWith(2, '/albums/7/tracks', {
      trackOrders: [{ trackId: 11, order: 0 }],
    });
    await albums.removeTrackFromAlbum(7, 11);
    await albums.deleteAlbum(7);
    expect(mockedClient.delete).toHaveBeenCalledWith('/albums/7/tracks/11');
    expect(mockedClient.delete).toHaveBeenCalledWith('/albums/7');
  });

  it('filters download history params and downloads blobs safely', async () => {
    const abortController = new AbortController();
    const blob = new Blob(['audio']);
    mockedClient.get
      .mockResolvedValueOnce({ data: blob })
      .mockResolvedValueOnce(apiResponse({ remaining: 3 }))
      .mockResolvedValueOnce({ data: paged })
      .mockResolvedValueOnce({ data: { dataList: [9, 10] } })
      .mockResolvedValueOnce({ data: {} });

    await expect(downloads.downloadTrack(9, abortController.signal)).resolves.toBe(blob);
    expect(mockedClient.get).toHaveBeenNthCalledWith(1, '/tracks/9/download', {
      responseType: 'blob',
      signal: abortController.signal,
    });
    await downloads.fetchDownloadCount(abortController.signal);
    await downloads.fetchDownloadHistory(
      { page: 3, size: 5, keyword: 'spring', sort: 'oldest' },
      abortController.signal,
    );
    expect(mockedClient.get).toHaveBeenNthCalledWith(3, '/downloads/history', {
      params: { page: 3, size: 5, keyword: 'spring', sort: 'oldest' },
      signal: abortController.signal,
    });
    await expect(
      downloads.fetchDownloadHistoryTrackIds('spring', abortController.signal),
    ).resolves.toEqual([9, 10]);
    expect(mockedClient.get).toHaveBeenNthCalledWith(4, '/downloads/history/track-ids', {
      params: { keyword: 'spring' },
      signal: abortController.signal,
    });
    await expect(downloads.fetchDownloadHistoryTrackIds()).resolves.toEqual([]);

    const click = vi.fn();
    const remove = vi.fn();
    const anchor = { href: '', download: '', click, remove };
    vi.spyOn(document, 'createElement').mockReturnValueOnce(anchor as unknown as HTMLAnchorElement);
    vi.spyOn(document.body, 'appendChild').mockImplementationOnce((node) => node);
    vi.spyOn(URL, 'createObjectURL').mockReturnValueOnce('blob:test');
    vi.spyOn(URL, 'revokeObjectURL').mockImplementationOnce(() => undefined);
    downloads.triggerBlobDownload(blob, 'track.mp3');
    expect(anchor).toMatchObject({ href: 'blob:test', download: 'track.mp3' });
    expect(click).toHaveBeenCalledOnce();
    expect(remove).toHaveBeenCalledOnce();
    expect(URL.revokeObjectURL).toHaveBeenCalledWith('blob:test');
  });

  it('uses license and like endpoint contracts', async () => {
    const controller = new AbortController();
    mockedClient.get
      .mockResolvedValueOnce({ data: paged })
      .mockResolvedValueOnce({ data: paged })
      .mockResolvedValueOnce(apiResponse(payload))
      .mockResolvedValueOnce({ data: { dataList: [payload] } })
      .mockResolvedValueOnce({ data: { dataList: [payload] } });
    mockedClient.post.mockResolvedValue({});
    mockedClient.delete.mockResolvedValue({});

    await licenses.fetchUserLicenses(3, 2, 15, controller.signal);
    expect(mockedClient.get).toHaveBeenNthCalledWith(1, '/users/3/licenses', {
      params: { page: 2, size: 15 },
      signal: controller.signal,
    });
    await licenses.fetchMyLicenses(1, 20, controller.signal);
    expect(mockedClient.get).toHaveBeenNthCalledWith(2, '/licenses/me', {
      params: { page: 1, size: 20 },
      signal: controller.signal,
    });
    await expect(licenses.fetchLicenseDetail(7, controller.signal)).resolves.toEqual(payload);
    expect(mockedClient.get).toHaveBeenNthCalledWith(3, '/licenses/7', {
      signal: controller.signal,
    });

    await expect(likes.fetchLikes(controller.signal)).resolves.toEqual({ dataList: [payload] });
    expect(mockedClient.get).toHaveBeenNthCalledWith(4, '/likes', {
      signal: controller.signal,
    });
    await likes.addLike(7);
    await likes.removeLike(7);
    await expect(likes.fetchAlbumLikes(controller.signal)).resolves.toEqual({
      dataList: [payload],
    });
    expect(mockedClient.get).toHaveBeenNthCalledWith(5, '/likes/albums', {
      signal: controller.signal,
    });
    await likes.addAlbumLike(8);
    await likes.removeAlbumLike(8);
    expect(mockedClient.post).toHaveBeenCalledWith('/likes/7');
    expect(mockedClient.delete).toHaveBeenCalledWith('/likes/7');
    expect(mockedClient.post).toHaveBeenCalledWith('/likes/albums/8');
    expect(mockedClient.delete).toHaveBeenCalledWith('/likes/albums/8');
  });

  it('builds notice multipart bodies and performs attachment downloads', async () => {
    const blob = new Blob(['notice']);
    mockedClient.get
      .mockResolvedValueOnce({ data: paged })
      .mockResolvedValueOnce(apiResponse(payload))
      .mockResolvedValueOnce({ data: blob });
    mockedClient.post.mockResolvedValueOnce(apiResponse(payload));
    mockedClient.put.mockResolvedValueOnce(apiResponse(payload));
    mockedClient.delete.mockResolvedValue({});

    await notices.fetchNotices({ page: 2, size: 5, sort: 'views' });
    await expect(notices.fetchNotice(7)).resolves.toEqual(payload);
    const attachment = new File(['first'], 'first.txt');
    await notices.createNotice({
      title: 'Notice',
      content: 'Body',
      isPinned: true,
      attachments: [attachment],
    });
    const createForm = mockedClient.post.mock.calls[0]?.[1] as FormData;
    expect(Array.from(createForm.entries())).toEqual([
      ['title', 'Notice'],
      ['content', 'Body'],
      ['isPinned', 'true'],
      ['attachments', attachment],
    ]);

    const replacement = new File(['second'], 'second.txt');
    await notices.updateNotice(7, {
      title: 'Updated',
      content: 'New body',
      isPinned: false,
      deleteAttachmentIds: [4],
      newAttachments: [replacement],
    });
    const updateForm = mockedClient.put.mock.calls[0]?.[1] as FormData;
    expect(Array.from(updateForm.entries())).toEqual([
      ['title', 'Updated'],
      ['content', 'New body'],
      ['isPinned', 'false'],
      ['deleteAttachmentIds', '4'],
      ['newAttachments', replacement],
    ]);
    await notices.deleteNotice(7);

    const click = vi.fn();
    const remove = vi.fn();
    const anchor = { href: '', download: '', click, remove };
    vi.spyOn(document, 'createElement').mockReturnValueOnce(anchor as unknown as HTMLAnchorElement);
    vi.spyOn(document.body, 'appendChild').mockImplementationOnce((node) => node);
    vi.spyOn(URL, 'createObjectURL').mockReturnValueOnce('blob:notice');
    vi.spyOn(URL, 'revokeObjectURL').mockImplementationOnce(() => undefined);
    await notices.downloadNoticeAttachment(7, 4, 'notice.txt');
    expect(anchor).toMatchObject({ href: 'blob:notice', download: 'notice.txt' });
  });

  it('uses recurring payment and subscription contracts', async () => {
    mockedClient.post
      .mockResolvedValueOnce(apiResponse({ orderId: 'order-1' }))
      .mockResolvedValueOnce(apiResponse({ orderStatus: 'DONE' }))
      .mockResolvedValueOnce(apiResponse({ status: 'ACTIVE' }));
    mockedClient.get
      .mockResolvedValueOnce(apiResponse({ provider: 'TOSS' }))
      .mockResolvedValueOnce(apiResponse({ orderStatus: 'DONE' }))
      .mockResolvedValueOnce(apiResponse({ orderStatus: 'DONE' }))
      .mockResolvedValueOnce(apiResponse({ id: 1 }))
      .mockResolvedValueOnce({ data: paged })
      .mockResolvedValueOnce(apiResponse({ changeType: 'UPGRADE' }));
    mockedClient.put
      .mockResolvedValueOnce(apiResponse({ changeType: 'UPGRADE' }))
      .mockResolvedValueOnce(apiResponse({ id: 1 }));
    mockedClient.delete.mockResolvedValue({});

    const prepare = {
      subscriptionId: 2,
      billingCycle: 'MONTHLY' as const,
      purpose: 'SUBSCRIBE' as const,
    };
    const idempotencyKey = '11111111-1111-4111-8111-111111111111';
    await payments.prepareBillingAgreement(prepare, idempotencyKey);
    expect(mockedClient.post).toHaveBeenNthCalledWith(
      1,
      '/payments/billing-agreements/prepare',
      prepare,
      { headers: { 'Idempotency-Key': idempotencyKey } },
    );
    expect(prepare).not.toHaveProperty('idempotencyKey');
    const confirm = { orderId: 'order-1', authKey: 'auth', customerKey: 'customer', amount: 9900 };
    await payments.confirmBillingAgreement(confirm);
    await payments.fetchMyBillingAgreement();
    await payments.fetchPaymentCommandOutcome('order/with space');
    await payments.fetchSubscriptionUpgradeOutcome(2, 'YEARLY');
    expect(mockedClient.get).toHaveBeenCalledWith('/payments/orders/order%2Fwith%20space/outcome');
    expect(mockedClient.get).toHaveBeenCalledWith('/payments/subscription-upgrades/outcome', {
      params: { subscriptionId: 2, billingCycle: 'YEARLY' },
    });

    const controller = new AbortController();
    await userSubscriptions.fetchMySubscription(controller.signal);
    await userSubscriptions.changeMySubscription({
      subscriptionId: prepare.subscriptionId,
      billingCycle: prepare.billingCycle,
    });
    await userSubscriptions.cancelMySubscription();
    await userSubscriptions.reactivateMySubscription();
    await userSubscriptions.fetchAdminUserSubscriptions(2, 30, controller.signal);
    await userSubscriptions.fetchSubscriptionChangePreview(2, 'YEARLY');
    expect(mockedClient.get).toHaveBeenCalledWith('/utils/subscription-change-preview', {
      params: { subscriptionId: 2, billingCycle: 'YEARLY' },
    });
  });

  it('uses the administrator subscription correction workflow contracts', async () => {
    const controller = new AbortController();
    const request: userSubscriptions.AdminSubscriptionCorrectionRequest = {
      userSubscriptionId: 71,
      targetSubscriptionId: 2,
      targetBillingCycle: 'YEARLY',
      targetStatus: 'CANCELLED',
      targetExpiresAt: '2026-09-01',
      clearPendingChange: true,
      cancelBillingAgreement: true,
      reasonNote: '지원 티켓 ATS-71',
    };
    const correction = { id: 91, status: 'REQUESTED' };

    mockedClient.post
      .mockResolvedValueOnce(apiResponse({ executable: true, externalPaymentExecuted: false }))
      .mockResolvedValueOnce(apiResponse(correction))
      .mockResolvedValueOnce(apiResponse({ ...correction, status: 'APPROVED' }))
      .mockResolvedValueOnce(apiResponse({ ...correction, status: 'SUCCEEDED' }));
    mockedClient.get
      .mockResolvedValueOnce({ data: paged })
      .mockResolvedValueOnce(apiResponse(correction));

    await userSubscriptions.previewAdminSubscriptionCorrection(request, controller.signal);
    await userSubscriptions.fetchAdminSubscriptionCorrections(2, 30, controller.signal);
    await userSubscriptions.fetchAdminSubscriptionCorrection(91, controller.signal);
    await userSubscriptions.createAdminSubscriptionCorrection(request, controller.signal);
    await userSubscriptions.approveAdminSubscriptionCorrection(
      91,
      { note: '승인 메모' },
      controller.signal,
    );
    await userSubscriptions.executeAdminSubscriptionCorrection(
      91,
      { note: '실행 메모' },
      controller.signal,
    );

    expect(mockedClient.post).toHaveBeenNthCalledWith(
      1,
      '/admin/user-subscription-corrections/preview',
      request,
      { signal: controller.signal },
    );
    expect(mockedClient.get).toHaveBeenNthCalledWith(1, '/admin/user-subscription-corrections', {
      params: { page: 2, size: 30 },
      signal: controller.signal,
    });
    expect(mockedClient.get).toHaveBeenNthCalledWith(2, '/admin/user-subscription-corrections/91', {
      signal: controller.signal,
    });
    expect(mockedClient.post).toHaveBeenNthCalledWith(
      2,
      '/admin/user-subscription-corrections',
      request,
      { signal: controller.signal },
    );
    expect(mockedClient.post).toHaveBeenNthCalledWith(
      3,
      '/admin/user-subscription-corrections/91/approve',
      { note: '승인 메모' },
      { signal: controller.signal },
    );
    expect(mockedClient.post).toHaveBeenNthCalledWith(
      4,
      '/admin/user-subscription-corrections/91/execute',
      { note: '실행 메모' },
      { signal: controller.signal },
    );
  });

  it('returns the open correction and maps an Axios 204 response to null', async () => {
    const controller = new AbortController();
    const correction = { id: 91, status: 'REQUESTED' };
    mockedClient.get
      .mockResolvedValueOnce({ status: 200, ...apiResponse(correction) })
      .mockResolvedValueOnce({ status: 204, data: '' });

    await expect(
      userSubscriptions.fetchOpenAdminSubscriptionCorrection(71, controller.signal),
    ).resolves.toEqual(correction);
    await expect(
      userSubscriptions.fetchOpenAdminSubscriptionCorrection(72, controller.signal),
    ).resolves.toBeNull();
    expect(mockedClient.get).toHaveBeenNthCalledWith(
      1,
      '/admin/user-subscription-corrections/open',
      { params: { userSubscriptionId: 71 }, signal: controller.signal },
    );
    expect(mockedClient.get).toHaveBeenNthCalledWith(
      2,
      '/admin/user-subscription-corrections/open',
      { params: { userSubscriptionId: 72 }, signal: controller.signal },
    );
  });

  it('matches every backend PaymentOrderStatus value', () => {
    type BackendPaymentOrderStatus = (typeof BACKEND_PAYMENT_ORDER_STATUSES)[number];

    expect(payments.PAYMENT_ORDER_STATUSES).toEqual(BACKEND_PAYMENT_ORDER_STATUSES);
    expectTypeOf<payments.PaymentOrderStatus>().toEqualTypeOf<BackendPaymentOrderStatus>();
  });

  it('identifies only the documented no-active-subscription error', () => {
    expect(
      userSubscriptions.isNoActiveSubscriptionError({
        response: { status: 403, data: { errorCode: 'NO_ACTIVE_SUBSCRIPTION' } },
      }),
    ).toBe(true);
    expect(
      userSubscriptions.isNoActiveSubscriptionError({
        response: { status: 403, data: { errorCode: 'FORBIDDEN' } },
      }),
    ).toBe(false);
    expect(userSubscriptions.isNoActiveSubscriptionError(new Error('network'))).toBe(false);
  });

  it('builds playlist multipart requests and track mutations', async () => {
    const controller = new AbortController();
    mockedClient.get
      .mockResolvedValueOnce({ data: { dataList: [payload] } })
      .mockResolvedValueOnce(apiResponse(payload));
    mockedClient.post.mockResolvedValueOnce(apiResponse(payload)).mockResolvedValue({});
    mockedClient.put.mockResolvedValue({});
    mockedClient.delete.mockResolvedValue({});

    await playlists.fetchMyPlaylists(controller.signal);
    expect(mockedClient.get).toHaveBeenNthCalledWith(1, '/playlists', {
      signal: controller.signal,
    });
    await expect(playlists.fetchPlaylistDetail(4, controller.signal)).resolves.toEqual(payload);
    expect(mockedClient.get).toHaveBeenNthCalledWith(2, '/playlists/4', {
      signal: controller.signal,
    });
    const thumbnail = new File(['image'], 'cover.png');
    await playlists.createPlaylist({ title: 'Mix', description: 'Focus', thumbnail });
    const createForm = mockedClient.post.mock.calls[0]?.[1] as FormData;
    expect(Array.from(createForm.entries())).toEqual([
      ['title', 'Mix'],
      ['description', 'Focus'],
      ['thumbnail', thumbnail],
    ]);
    await playlists.updatePlaylist(4, { title: 'Mix 2' });
    await playlists.addTrackToPlaylist(4, 9);
    await playlists.reorderTracks(4, [{ trackId: 9, trackOrder: 0 }]);
    await playlists.removeTrackFromPlaylist(4, 9);
    await playlists.deletePlaylist(4);
    expect(mockedClient.post).toHaveBeenCalledWith('/playlists/4/tracks', { trackId: 9 });
    expect(mockedClient.put).toHaveBeenCalledWith('/playlists/4/tracks', {
      tracks: [{ trackId: 9, trackOrder: 0 }],
    });
  });

  it('uses question CRUD, answer, status, and attachment contracts', async () => {
    const controller = new AbortController();
    const blob = new Blob(['question']);
    mockedClient.get
      .mockResolvedValueOnce({ data: paged })
      .mockResolvedValueOnce(apiResponse(payload))
      .mockResolvedValueOnce({ data: blob });
    mockedClient.post
      .mockResolvedValueOnce(apiResponse(payload))
      .mockResolvedValueOnce(apiResponse({ id: 3, content: 'Answer' }));
    mockedClient.put.mockResolvedValueOnce(apiResponse(payload));
    mockedClient.delete.mockResolvedValue({});

    const params = { page: 2, size: 10, category: 'OTHER' as const, mine: true };
    await questions.fetchQuestions(params, controller.signal);
    expect(mockedClient.get).toHaveBeenNthCalledWith(1, '/questions', {
      params,
      signal: controller.signal,
    });
    await questions.fetchQuestionDetail(2, controller.signal);
    expect(mockedClient.get).toHaveBeenNthCalledWith(2, '/questions/2', {
      signal: controller.signal,
    });
    const file = new File(['question'], 'question.txt');
    await questions.createQuestion({
      title: 'Help',
      content: 'Please help',
      category: 'OTHER',
      isPublic: false,
      attachments: [file],
    });
    const form = mockedClient.post.mock.calls[0]?.[1] as FormData;
    expect(Array.from(form.entries())).toEqual([
      ['title', 'Help'],
      ['content', 'Please help'],
      ['category', 'OTHER'],
      ['isPublic', 'false'],
      ['attachments', file],
    ]);
    await questions.updateQuestionStatus(2, 'RESOLVED');
    await questions.createAnswer(2, 'Answer');
    await questions.deleteQuestion(2);

    const click = vi.fn();
    const remove = vi.fn();
    vi.spyOn(document, 'createElement').mockReturnValueOnce({
      href: '',
      download: '',
      click,
      remove,
    } as unknown as HTMLAnchorElement);
    vi.spyOn(document.body, 'appendChild').mockImplementationOnce((node) => node);
    vi.spyOn(URL, 'createObjectURL').mockReturnValueOnce('blob:question');
    vi.spyOn(URL, 'revokeObjectURL').mockImplementationOnce(() => undefined);
    await questions.downloadAttachment(2, 5, 'question.txt');
    expect(click).toHaveBeenCalledOnce();
  });

  it('uses settings, plan, tag, and track query contracts', async () => {
    const controller = new AbortController();
    mockedClient.get
      .mockResolvedValueOnce(apiResponse({ key: 'downloads', value: '20' }))
      .mockResolvedValueOnce({ data: { dataList: [payload] } })
      .mockResolvedValueOnce({ data: { dataList: [payload] } })
      .mockResolvedValueOnce({ data: { dataList: [payload] } })
      .mockResolvedValueOnce({ data: { dataList: [payload] } })
      .mockResolvedValueOnce({ data: paged })
      .mockResolvedValueOnce(apiResponse(payload))
      .mockResolvedValueOnce(apiResponse(payload))
      .mockResolvedValueOnce({ data: paged });
    mockedClient.post
      .mockResolvedValueOnce({ data: { data: payload } })
      .mockResolvedValueOnce(apiResponse(payload));
    mockedClient.put
      .mockResolvedValueOnce({})
      .mockResolvedValueOnce({ data: { data: payload } })
      .mockResolvedValueOnce(apiResponse(payload));
    mockedClient.delete.mockResolvedValue({});

    await expect(settings.getSetting('downloads')).resolves.toBe('20');
    await settings.updateSetting('downloads', '30');
    await expect(subscriptions.fetchSubscriptionPlans('PERSONAL')).resolves.toEqual([payload]);
    await subscriptions.fetchAdminSubscriptionPlans(controller.signal);

    await tags.fetchTags('GENRE');
    expect(mockedClient.get).toHaveBeenNthCalledWith(4, '/tags', { params: { type: 'GENRE' } });
    await tags.fetchAvailableTags(
      {
        genre: ['K-Pop', '한글 장르'],
        mood: ['bright'],
        instrument: ['guitar, synth'],
        usage: ['#shorts'],
        bpmMin: 80,
        bpmMax: 120,
      },
      controller.signal,
    );
    expect(mockedClient.get).toHaveBeenNthCalledWith(5, '/tags/available', {
      params: expect.any(URLSearchParams),
      signal: controller.signal,
    });
    const availableTagQuery = mockedClient.get.mock.calls[4]?.[1] as {
      params: URLSearchParams;
    };
    expect(availableTagQuery.params.toString()).toBe(
      'genre=K-Pop&genre=%ED%95%9C%EA%B8%80+%EC%9E%A5%EB%A5%B4&mood=bright&instrument=guitar%2C+synth&usage=%23shorts&bpmMin=80&bpmMax=120',
    );
    await tags.createTag({ name: 'Rock', type: 'GENRE' });
    await tags.updateTag(7, { name: 'Indie', type: 'GENRE' });
    await tags.deleteTag(7);

    const trackFilters = {
      page: 2,
      size: 12,
      keyword: 'spring',
      genre: ['K-Pop', '한글 장르'],
      mood: ['bright'],
      instrument: ['guitar, synth'],
      usage: ['#shorts'],
      bpmMin: 80,
      bpmMax: 120,
      tonality: 'C',
      sort: 'popular' as const,
    };
    await tracks.fetchTracks(trackFilters, controller.signal);
    expect(mockedClient.get).toHaveBeenNthCalledWith(6, '/tracks', {
      params: expect.any(URLSearchParams),
      signal: controller.signal,
    });
    const trackQuery = mockedClient.get.mock.calls[5]?.[1] as { params: URLSearchParams };
    expect(trackQuery.params.toString()).toBe(
      'page=2&size=12&keyword=spring&genre=K-Pop&genre=%ED%95%9C%EA%B8%80+%EC%9E%A5%EB%A5%B4&mood=bright&instrument=guitar%2C+synth&usage=%23shorts&bpmMin=80&bpmMax=120&tonality=C&sort=popular',
    );
    await tracks.fetchTrackDetail(7, controller.signal);
    await tracks.fetchTrackDetailForAdmin(7);
    await tracks.fetchAdminTracks({ page: 2, size: 20, is_active: false, keyword: 'old' });
    const form = new FormData();
    await tracks.createTrack(form);
    await tracks.updateTrack(7, form);
    await tracks.deleteTrack(7);
    expect(mockedClient.post).toHaveBeenLastCalledWith('/tracks', form, { timeout: 300_000 });
    expect(mockedClient.put).toHaveBeenLastCalledWith('/tracks/7', form, { timeout: 300_000 });
  });

  it('uses whitelist channel lifecycle contracts', async () => {
    mockedClient.post.mockResolvedValue(apiResponse(payload));
    mockedClient.get.mockResolvedValue({ data: { dataList: [payload] } });
    mockedClient.put.mockResolvedValue(apiResponse(payload));
    mockedClient.delete.mockResolvedValue({});
    const request = {
      channelUrl: 'https://youtube.com/@atm',
      channelName: 'AT.M',
      youtubeHandle: '@atm',
      youtubeChannelId: 'UC123',
    };

    await whitelistChannels.registerChannel(request);
    await whitelistChannels.fetchWhitelistChannels();
    await whitelistChannels.updateChannel(4, request);
    await whitelistChannels.requestWhitelistRegistration(4);
    await whitelistChannels.setPrimaryWhitelistChannel(4);
    await whitelistChannels.deleteChannel(4);
    expect(mockedClient.post).toHaveBeenCalledWith('/whitelist-channels', request);
    expect(mockedClient.put).toHaveBeenCalledWith('/whitelist-channels/4/primary');
    expect(mockedClient.delete).toHaveBeenCalledWith('/whitelist-channels/4');
  });

  it('hydrates multiple playable tracks with one bounded batch request', async () => {
    const playable = {
      id: 7,
      title: 'Track',
      artistName: 'Artist',
      duration: 180,
      thumbnail: null,
      waveformData: '[0.2,0.8]',
    };
    mockedClient.post.mockResolvedValue({ data: { dataList: [playable] } });

    await expect(tracks.fetchPlayableTracks([7, 8, 7])).resolves.toEqual([playable]);
    expect(mockedClient.post).toHaveBeenCalledTimes(1);
    expect(mockedClient.post).toHaveBeenCalledWith('/tracks/batch', { ids: [7, 8, 7] });
  });

  it('builds company certification multipart requests and maps errors', async () => {
    mockedClient.post.mockResolvedValue(apiResponse(payload));
    mockedClient.get.mockResolvedValue(apiResponse(payload));
    const first = new File(['first'], 'business.pdf');
    const second = new File(['second'], 'identity.pdf');

    await companyCerts.applyCompanyCert([first, second]);
    const applyForm = mockedClient.post.mock.calls[0]?.[1] as FormData;
    expect(applyForm.getAll('documents')).toEqual([first, second]);
    await companyCerts.resubmitCompanyCert([second]);
    await companyCerts.fetchMyCompanyCert();
    expect(companyCerts.getCompanyCertErrorStatus(null)).toBeUndefined();
    expect(companyCerts.getCompanyCertErrorStatus({ response: { status: 422 } })).toBe(422);
    expect(
      companyCerts.getCompanyCertErrorMessage(
        { response: { status: 500, data: { message: ' Provider error ' } } },
        'Fallback',
      ),
    ).toBe('Provider error');
    expect(
      companyCerts.getCompanyCertErrorMessage({ response: { status: 422 } }, 'Fallback'),
    ).not.toBe('Fallback');
    expect(companyCerts.getCompanyCertErrorMessage(new Error('offline'), 'Fallback')).toBe(
      'Fallback',
    );
  });
});
