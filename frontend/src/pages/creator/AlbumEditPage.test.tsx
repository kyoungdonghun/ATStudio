import { act, fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { MemoryRouter, Route, Routes, useNavigate } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { AlbumDetail } from '@/api/albums';
import type { TrackListItem } from '@/types';
import AlbumEditPage from './AlbumEditPage';

const mocks = vi.hoisted(() => ({
  fetchAlbumDetail: vi.fn(),
  updateAlbum: vi.fn(),
  addTrackToAlbum: vi.fn(),
  removeTrackFromAlbum: vi.fn(),
  reorderAlbumTracks: vi.fn(),
  fetchTracks: vi.fn(),
  toast: vi.fn(),
}));

vi.mock('@/api/albums', () => ({
  fetchAlbumDetail: (...args: unknown[]) => mocks.fetchAlbumDetail(...args),
  updateAlbum: (...args: unknown[]) => mocks.updateAlbum(...args),
  addTrackToAlbum: (...args: unknown[]) => mocks.addTrackToAlbum(...args),
  removeTrackFromAlbum: (...args: unknown[]) => mocks.removeTrackFromAlbum(...args),
  reorderAlbumTracks: (...args: unknown[]) => mocks.reorderAlbumTracks(...args),
}));

vi.mock('@/api/tracks', () => ({
  fetchTracks: (...args: unknown[]) => mocks.fetchTracks(...args),
}));

vi.mock('@/api/client', () => ({
  toUploadUrl: (path: string | null | undefined) => path,
}));

vi.mock('@/store/toastStore', () => ({
  useToastStore: (selector: (state: { show: typeof mocks.toast }) => unknown) =>
    selector({ show: mocks.toast }),
}));

const initialAlbum: AlbumDetail = {
  id: 11,
  title: 'Night Drive',
  description: 'City music',
  thumbnailUrl: null,
  likeCount: 0,
  createdAt: '2026-08-01T00:00:00Z',
  tracks: [
    {
      trackId: 21,
      title: 'First Track',
      artistName: 'Creator',
      duration: 120,
      order: 0,
    },
    {
      trackId: 22,
      title: 'Second Track',
      artistName: 'Creator',
      duration: 130,
      order: 1,
    },
    {
      trackId: 23,
      title: 'Third Track',
      artistName: 'Creator',
      duration: 140,
      order: 2,
    },
  ],
};

const route12Album: AlbumDetail = {
  ...initialAlbum,
  id: 12,
  title: 'Album Twelve',
  description: 'Current route owner',
  tracks: [
    {
      trackId: 121,
      title: 'Album Twelve Track',
      artistName: 'Creator',
      duration: 150,
      order: 0,
    },
  ],
};

function albumWithOrder(trackIds: number[]): AlbumDetail {
  const tracksById = new Map(initialAlbum.tracks.map((track) => [track.trackId, track]));
  return {
    ...initialAlbum,
    tracks: trackIds.map((trackId, order) => ({ ...tracksById.get(trackId)!, order })),
  };
}

function searchTrack(id: number, title: string): TrackListItem {
  return {
    id,
    title,
    artistName: 'Creator',
    duration: 30,
    bpm: 120,
    tonality: 'C',
    thumbnail: null,
    waveformData: null,
    playCount: 0,
    likeCount: 0,
    downloadCount: 0,
    tags: [],
    createdAt: '2026-08-01T00:00:00Z',
  };
}

function albumWithAddedTrack(trackId: number, title: string): AlbumDetail {
  return {
    ...initialAlbum,
    tracks: [
      ...initialAlbum.tracks,
      {
        trackId,
        title,
        artistName: 'Creator',
        duration: 30,
        order: initialAlbum.tracks.length,
      },
    ],
  };
}

function RouteSwitchControl() {
  const navigate = useNavigate();
  return (
    <button type="button" onClick={() => navigate('/admin/albums/12/edit')}>
      Open Album 12
    </button>
  );
}

function renderPage(path = '/admin/albums/11/edit', withRouteSwitch = false) {
  return render(
    <MemoryRouter initialEntries={[path]}>
      {withRouteSwitch ? <RouteSwitchControl /> : null}
      <Routes>
        <Route path="/admin/albums/:albumId/edit" element={<AlbumEditPage />} />
        <Route path="/admin/albums/edit" element={<AlbumEditPage />} />
      </Routes>
    </MemoryRouter>,
  );
}

function trackTitles() {
  const section = screen.getByRole('heading', { name: '앨범 트랙' }).parentElement!;
  return within(section)
    .getAllByRole('listitem')
    .map((item) => within(item).getByText(/Track$/).textContent);
}

describe('AlbumEditPage track reorder', () => {
  beforeEach(() => {
    vi.resetAllMocks();
    mocks.fetchAlbumDetail.mockResolvedValue(initialAlbum);
    mocks.updateAlbum.mockResolvedValue(initialAlbum);
    mocks.addTrackToAlbum.mockResolvedValue(initialAlbum);
    mocks.removeTrackFromAlbum.mockResolvedValue(undefined);
    mocks.reorderAlbumTracks.mockResolvedValue(undefined);
    mocks.fetchTracks.mockResolvedValue({
      dataList: [],
      pageInfo: { page: 1, size: 10, total: 0, start: 0, end: 0, prev: false, next: false },
    });
  });

  it('rejects malformed, missing, non-positive, and unsafe route IDs without protected requests', async () => {
    for (const path of [
      '/admin/albums/edit',
      '/admin/albums/not-a-number/edit',
      '/admin/albums/0/edit',
      '/admin/albums/-1/edit',
      '/admin/albums/9007199254740992/edit',
    ]) {
      const view = renderPage(path);

      expect(
        await screen.findByRole('heading', { name: '앨범을 열 수 없습니다.' }),
      ).toBeInTheDocument();
      expect(screen.getByRole('link', { name: '앨범 관리로 이동' })).toHaveAttribute(
        'href',
        '/admin/albums',
      );

      view.unmount();
    }

    expect(mocks.fetchAlbumDetail).not.toHaveBeenCalled();
    expect(mocks.updateAlbum).not.toHaveBeenCalled();
    expect(mocks.addTrackToAlbum).not.toHaveBeenCalled();
    expect(mocks.removeTrackFromAlbum).not.toHaveBeenCalled();
    expect(mocks.reorderAlbumTracks).not.toHaveBeenCalled();
  });

  it('retires a pending add when the canonical Album route owner switches', async () => {
    let resolveAdd!: (value: AlbumDetail) => void;
    const pendingAdd = new Promise<AlbumDetail>((resolve) => {
      resolveAdd = resolve;
    });
    const pendingRemove = new Promise<void>(() => undefined);
    mocks.addTrackToAlbum.mockReturnValueOnce(pendingAdd);
    mocks.removeTrackFromAlbum.mockReturnValueOnce(pendingRemove);
    mocks.fetchAlbumDetail.mockImplementation((requestedAlbumId: number) =>
      Promise.resolve(requestedAlbumId === 12 ? route12Album : initialAlbum),
    );
    mocks.fetchTracks.mockResolvedValue({
      dataList: [searchTrack(42, 'Route Safe Track')],
      pageInfo: { page: 1, size: 10, total: 1, start: 1, end: 1, prev: false, next: false },
    });

    renderPage('/admin/albums/11/edit', true);
    expect(await screen.findByText('First Track')).toBeInTheDocument();
    const searchInput = screen.getByRole('combobox', { name: '앨범에 추가할 트랙 검색' });
    fireEvent.change(searchInput, { target: { value: 'route' } });
    fireEvent.click(screen.getByRole('button', { name: '검색' }));
    fireEvent.click(await screen.findByRole('option', { name: /Route Safe Track/ }));
    await waitFor(() => expect(mocks.addTrackToAlbum).toHaveBeenCalledWith(11, 42));

    fireEvent.click(screen.getByRole('button', { name: 'Open Album 12' }));
    expect(await screen.findByDisplayValue('Album Twelve')).toBeInTheDocument();
    expect(screen.getByText('Album Twelve Track')).toBeInTheDocument();

    await act(async () => {
      resolveAdd(albumWithAddedTrack(42, 'Route Safe Track'));
      await pendingAdd;
    });

    expect(mocks.fetchAlbumDetail.mock.calls.map(([requestedAlbumId]) => requestedAlbumId)).toEqual(
      [11, 12],
    );
    expect(mocks.toast).not.toHaveBeenCalled();
    expect(screen.getByText('Album Twelve Track')).toBeInTheDocument();
    expect(screen.queryByText('First Track')).not.toBeInTheDocument();

    const currentSearchInput = screen.getByRole('combobox', {
      name: '앨범에 추가할 트랙 검색',
    });
    fireEvent.change(currentSearchInput, { target: { value: 'route' } });
    fireEvent.click(screen.getByRole('button', { name: '검색' }));
    expect(await screen.findByRole('option', { name: /Route Safe Track/ })).toBeInTheDocument();

    fireEvent.click(screen.getByTitle('제거'));
    expect(mocks.removeTrackFromAlbum).toHaveBeenCalledWith(12, 121);
    expect(mocks.removeTrackFromAlbum).not.toHaveBeenCalledWith(12, 21);
  });

  it('retires a pending add without follow-up reads or feedback after unmount', async () => {
    let resolveAdd!: (value: AlbumDetail) => void;
    const pendingAdd = new Promise<AlbumDetail>((resolve) => {
      resolveAdd = resolve;
    });
    mocks.addTrackToAlbum.mockReturnValueOnce(pendingAdd);
    mocks.fetchTracks.mockResolvedValue({
      dataList: [searchTrack(42, 'Unmounted Track')],
      pageInfo: { page: 1, size: 10, total: 1, start: 1, end: 1, prev: false, next: false },
    });

    const view = renderPage();
    expect(await screen.findByText('First Track')).toBeInTheDocument();
    const searchInput = screen.getByRole('combobox', { name: '앨범에 추가할 트랙 검색' });
    fireEvent.change(searchInput, { target: { value: 'unmount' } });
    fireEvent.click(screen.getByRole('button', { name: '검색' }));
    fireEvent.click(await screen.findByRole('option', { name: /Unmounted Track/ }));
    await waitFor(() => expect(mocks.addTrackToAlbum).toHaveBeenCalledWith(11, 42));

    view.unmount();
    await act(async () => {
      resolveAdd(albumWithAddedTrack(42, 'Unmounted Track'));
      await pendingAdd;
    });

    expect(mocks.fetchAlbumDetail).toHaveBeenCalledTimes(1);
    expect(mocks.toast).not.toHaveBeenCalled();
  });

  it('uses latest-request search ownership and supports keyboard combobox selection', async () => {
    let resolveFirst!: (value: unknown) => void;
    let resolveSecond!: (value: unknown) => void;
    const first = new Promise((resolve) => {
      resolveFirst = resolve;
    });
    const second = new Promise((resolve) => {
      resolveSecond = resolve;
    });
    mocks.fetchTracks.mockReset().mockReturnValueOnce(first).mockReturnValueOnce(second);
    mocks.fetchAlbumDetail.mockResolvedValue(initialAlbum);

    renderPage();
    expect(await screen.findByText('First Track')).toBeInTheDocument();

    const searchInput = screen.getByRole('combobox', { name: '앨범에 추가할 트랙 검색' });
    expect(searchInput).toHaveAttribute('placeholder', '트랙 제목 또는 Usage 태그 검색');

    fireEvent.change(searchInput, { target: { value: 'first' } });
    fireEvent.click(screen.getByRole('button', { name: '검색' }));
    fireEvent.change(searchInput, { target: { value: 'second' } });
    fireEvent.click(screen.getByRole('button', { name: '검색' }));

    await act(async () => {
      resolveSecond({
        dataList: [
          {
            id: 42,
            title: 'Latest Result',
            artistName: 'Creator',
            duration: 30,
            bpm: 120,
            tonality: 'C',
            thumbnail: null,
            waveformData: null,
            playCount: 0,
            likeCount: 0,
            downloadCount: 0,
            tags: [],
            createdAt: '2026-08-01T00:00:00Z',
          },
        ],
        pageInfo: { page: 1, size: 10, total: 1, start: 1, end: 1, prev: false, next: false },
      });
    });
    expect(await screen.findByRole('listbox', { name: '트랙 검색 결과' })).toBeInTheDocument();

    await act(async () => {
      resolveFirst({
        dataList: [],
        pageInfo: { page: 1, size: 10, total: 0, start: 0, end: 0, prev: false, next: false },
      });
    });
    expect(screen.getByText('Latest Result')).toBeInTheDocument();

    fireEvent.keyDown(searchInput, { key: 'ArrowDown' });
    const option = screen.getByRole('option', { name: /Latest Result/ });
    expect(searchInput).toHaveAttribute('aria-activedescendant', option.id);
    fireEvent.keyDown(searchInput, { key: 'Enter' });

    await waitFor(() => expect(mocks.addTrackToAlbum).toHaveBeenCalledWith(11, 42));
    fireEvent.keyDown(searchInput, { key: 'Escape' });
    expect(screen.queryByRole('listbox', { name: '트랙 검색 결과' })).not.toBeInTheDocument();
  });

  it('moves the active option with Home and End before Enter selects it', async () => {
    mocks.fetchTracks.mockResolvedValue({
      dataList: [
        searchTrack(41, 'First Result'),
        searchTrack(42, 'Middle Result'),
        searchTrack(43, 'Last Result'),
      ],
      pageInfo: { page: 1, size: 10, total: 3, start: 1, end: 3, prev: false, next: false },
    });
    mocks.fetchAlbumDetail
      .mockResolvedValueOnce(initialAlbum)
      .mockResolvedValueOnce(albumWithAddedTrack(43, 'Last Result'));

    renderPage();
    expect(await screen.findByText('First Track')).toBeInTheDocument();
    const searchInput = screen.getByRole('combobox', { name: '앨범에 추가할 트랙 검색' });
    fireEvent.change(searchInput, { target: { value: 'result' } });
    fireEvent.click(screen.getByRole('button', { name: '검색' }));
    const options = await screen.findAllByRole('option');

    fireEvent.keyDown(searchInput, { key: 'ArrowDown' });
    fireEvent.keyDown(searchInput, { key: 'ArrowDown' });
    expect(searchInput).toHaveAttribute('aria-activedescendant', options[1].id);
    expect(options[1]).toHaveAttribute('aria-selected', 'true');

    fireEvent.keyDown(searchInput, { key: 'Home' });
    expect(searchInput).toHaveAttribute('aria-activedescendant', options[0].id);
    expect(options[0]).toHaveAttribute('aria-selected', 'true');

    fireEvent.keyDown(searchInput, { key: 'End' });
    expect(searchInput).toHaveAttribute('aria-activedescendant', options[2].id);
    expect(options[2]).toHaveAttribute('aria-selected', 'true');

    fireEvent.keyDown(searchInput, { key: 'Enter' });
    await waitFor(() => expect(mocks.addTrackToAlbum).toHaveBeenCalledWith(11, 43));
    expect(mocks.addTrackToAlbum).toHaveBeenCalledTimes(1);
    await waitFor(() =>
      expect(screen.queryByRole('listbox', { name: '트랙 검색 결과' })).not.toBeInTheDocument(),
    );
  });

  it('dismisses with Escape and outside focus while preserving pointer option selection', async () => {
    mocks.fetchTracks.mockResolvedValue({
      dataList: [searchTrack(41, 'First Result'), searchTrack(42, 'Pointer Result')],
      pageInfo: { page: 1, size: 10, total: 2, start: 1, end: 2, prev: false, next: false },
    });

    renderPage();
    expect(await screen.findByText('First Track')).toBeInTheDocument();
    const searchInput = screen.getByRole('combobox', { name: '앨범에 추가할 트랙 검색' });
    fireEvent.change(searchInput, { target: { value: 'result' } });
    fireEvent.click(screen.getByRole('button', { name: '검색' }));
    expect(await screen.findByRole('listbox', { name: '트랙 검색 결과' })).toBeInTheDocument();

    fireEvent.keyDown(searchInput, { key: 'ArrowDown' });
    fireEvent.keyDown(searchInput, { key: 'Escape' });
    expect(searchInput).toHaveAttribute('aria-expanded', 'false');
    expect(searchInput).not.toHaveAttribute('aria-activedescendant');

    fireEvent.click(screen.getByRole('button', { name: '검색' }));
    expect(await screen.findByRole('listbox', { name: '트랙 검색 결과' })).toBeInTheDocument();
    fireEvent.focus(searchInput);
    fireEvent.blur(searchInput, {
      relatedTarget: screen.getByDisplayValue('Night Drive'),
    });
    expect(searchInput).toHaveAttribute('aria-expanded', 'false');
    expect(screen.queryByRole('listbox', { name: '트랙 검색 결과' })).not.toBeInTheDocument();

    fireEvent.focus(searchInput);
    fireEvent.click(screen.getByRole('button', { name: '검색' }));
    const pointerOption = await screen.findByRole('option', { name: /Pointer Result/ });
    fireEvent.mouseDown(pointerOption);
    fireEvent.click(pointerOption);

    await waitFor(() => expect(mocks.addTrackToAlbum).toHaveBeenCalledWith(11, 42));
    expect(mocks.addTrackToAlbum).toHaveBeenCalledTimes(1);
  });

  it('excludes current Album members from Track search results', async () => {
    mocks.fetchTracks.mockResolvedValue({
      dataList: [searchTrack(21, 'First Track'), searchTrack(42, 'New Track')],
      pageInfo: { page: 1, size: 10, total: 2, start: 1, end: 2, prev: false, next: false },
    });

    renderPage();
    expect(await screen.findByText('First Track')).toBeInTheDocument();
    const searchInput = screen.getByRole('combobox', { name: '앨범에 추가할 트랙 검색' });
    fireEvent.change(searchInput, { target: { value: 'track' } });
    fireEvent.click(screen.getByRole('button', { name: '검색' }));

    expect(await screen.findByRole('option', { name: /New Track/ })).toBeInTheDocument();
    expect(screen.queryByRole('option', { name: /First Track/ })).not.toBeInTheDocument();
  });

  it('fences a committed add from duplicate search after its authoritative refresh fails', async () => {
    mocks.fetchTracks.mockResolvedValue({
      dataList: [searchTrack(42, 'New Track')],
      pageInfo: { page: 1, size: 10, total: 1, start: 1, end: 1, prev: false, next: false },
    });
    mocks.fetchAlbumDetail
      .mockResolvedValueOnce(initialAlbum)
      .mockRejectedValueOnce(new Error('refresh failed'));

    renderPage();
    expect(await screen.findByText('First Track')).toBeInTheDocument();
    const searchInput = screen.getByRole('combobox', { name: '앨범에 추가할 트랙 검색' });
    fireEvent.change(searchInput, { target: { value: 'new' } });
    fireEvent.click(screen.getByRole('button', { name: '검색' }));
    fireEvent.click(await screen.findByRole('option', { name: /New Track/ }));

    expect(
      await screen.findByText('변경은 완료되었지만 최신 트랙 목록을 불러오지 못했습니다.'),
    ).toBeInTheDocument();
    expect(mocks.addTrackToAlbum).toHaveBeenCalledTimes(1);

    fireEvent.change(searchInput, { target: { value: 'new' } });
    fireEvent.click(screen.getByRole('button', { name: '검색' }));

    expect(await screen.findByText('검색 결과가 없습니다.')).toBeInTheDocument();
    expect(screen.queryByRole('option', { name: /New Track/ })).not.toBeInTheDocument();
    expect(mocks.addTrackToAlbum).toHaveBeenCalledTimes(1);
  });

  it('exposes search error and retries only the last keyword', async () => {
    mocks.fetchTracks
      .mockReset()
      .mockRejectedValueOnce(new Error('network'))
      .mockResolvedValueOnce({
        dataList: [],
        pageInfo: { page: 1, size: 10, total: 0, start: 0, end: 0, prev: false, next: false },
      });

    renderPage();
    expect(await screen.findByText('First Track')).toBeInTheDocument();
    const searchInput = screen.getByRole('combobox', { name: '앨범에 추가할 트랙 검색' });
    fireEvent.change(searchInput, { target: { value: 'usage' } });
    fireEvent.click(screen.getByRole('button', { name: '검색' }));

    expect(await screen.findByRole('alert', { name: '트랙 검색 실패' })).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '검색 다시 시도' }));

    await waitFor(() => expect(mocks.fetchTracks).toHaveBeenCalledTimes(2));
    expect(mocks.fetchTracks.mock.calls[1][0]).toEqual({ keyword: 'usage', size: 10 });
    expect(await screen.findByText('검색 결과가 없습니다.')).toBeInTheDocument();
  });

  it('blocks Album save while thumbnail validation is pending', async () => {
    const createObjectURL = vi.spyOn(URL, 'createObjectURL').mockReturnValue('blob:album');
    const revokeObjectURL = vi.spyOn(URL, 'revokeObjectURL').mockImplementation(() => undefined);

    const view = renderPage();
    expect(await screen.findByText('First Track')).toBeInTheDocument();
    const file = new File(['image'], 'cover.png', { type: 'image/png' });
    fireEvent.change(screen.getByLabelText('앨범 썸네일 이미지'), { target: { files: [file] } });

    const saveButton = screen.getByRole('button', { name: '저장' });
    expect(saveButton).toBeDisabled();
    const preview = await screen.findByAltText('선택한 앨범 썸네일 미리보기');
    Object.defineProperty(preview, 'naturalWidth', { configurable: true, value: 800 });
    Object.defineProperty(preview, 'naturalHeight', { configurable: true, value: 600 });
    fireEvent.load(preview);
    expect(saveButton).toBeEnabled();

    view.unmount();
    expect(createObjectURL).toHaveBeenCalledWith(file);
    expect(revokeObjectURL).toHaveBeenCalledWith('blob:album');
  });

  it('keeps title validation inline and sends blank description as an explicit clear', async () => {
    renderPage();
    expect(await screen.findByText('First Track')).toBeInTheDocument();

    fireEvent.change(screen.getByDisplayValue('Night Drive'), { target: { value: '   ' } });
    fireEvent.change(screen.getByDisplayValue('City music'), { target: { value: '   ' } });
    fireEvent.click(screen.getByRole('button', { name: '저장' }));

    expect(screen.getByText('앨범 제목을 입력해주세요.')).toBeInTheDocument();
    expect(
      screen.queryByRole('heading', { name: '앨범 정보를 불러오지 못했습니다.' }),
    ).not.toBeInTheDocument();
    expect(mocks.updateAlbum).not.toHaveBeenCalled();

    fireEvent.change(screen.getAllByRole('textbox')[0], {
      target: { value: 'Updated Album' },
    });
    fireEvent.click(screen.getByRole('button', { name: '저장' }));

    await waitFor(() => expect(mocks.updateAlbum).toHaveBeenCalledTimes(1));
    const payload = mocks.updateAlbum.mock.calls[0][1] as FormData;
    expect(payload.get('description')).toBe('');
  });

  it('sends every member once with zero-based contiguous orders and adopts one authoritative refetch', async () => {
    let resolveReorder!: () => void;
    const pendingReorder = new Promise<void>((resolve) => {
      resolveReorder = resolve;
    });
    mocks.reorderAlbumTracks.mockReturnValueOnce(pendingReorder);
    mocks.fetchAlbumDetail
      .mockResolvedValueOnce(initialAlbum)
      .mockResolvedValueOnce(albumWithOrder([23, 22, 21]));

    renderPage();
    expect(await screen.findByText('First Track')).toBeInTheDocument();

    fireEvent.click(screen.getAllByTitle('아래로')[0]);

    expect(mocks.reorderAlbumTracks).toHaveBeenCalledWith(11, [
      { trackId: 22, order: 0 },
      { trackId: 21, order: 1 },
      { trackId: 23, order: 2 },
    ]);
    expect(trackTitles()).toEqual(['Second Track', 'First Track', 'Third Track']);
    expect(mocks.fetchAlbumDetail).toHaveBeenCalledTimes(1);
    expect(screen.getAllByTitle('아래로')[0]).toBeDisabled();

    fireEvent.click(screen.getAllByTitle('아래로')[0]);
    expect(mocks.reorderAlbumTracks).toHaveBeenCalledTimes(1);

    await act(async () => resolveReorder());

    await waitFor(() => expect(mocks.fetchAlbumDetail).toHaveBeenCalledTimes(2));
    await waitFor(() =>
      expect(trackTitles()).toEqual(['Third Track', 'Second Track', 'First Track']),
    );
    expect(mocks.reorderAlbumTracks).toHaveBeenCalledTimes(1);
  });

  it('refetches once and adopts the authoritative order after success', async () => {
    mocks.fetchAlbumDetail
      .mockResolvedValueOnce(initialAlbum)
      .mockResolvedValueOnce(albumWithOrder([23, 22, 21]));

    renderPage();
    expect(await screen.findByText('First Track')).toBeInTheDocument();

    const firstDown = screen
      .getAllByRole('button')
      .find((button) => button.textContent === '\u25BC');
    expect(firstDown).toBeDefined();
    fireEvent.click(firstDown!);

    expect(trackTitles()).toEqual(['Second Track', 'First Track', 'Third Track']);
    expect(mocks.reorderAlbumTracks).toHaveBeenCalledTimes(1);
    await waitFor(() => expect(mocks.fetchAlbumDetail).toHaveBeenCalledTimes(2));
    await waitFor(() =>
      expect(trackTitles()).toEqual(['Third Track', 'Second Track', 'First Track']),
    );
  });

  it('does not request a reorder for top-up or bottom-down boundaries', async () => {
    renderPage();
    expect(await screen.findByText('First Track')).toBeInTheDocument();

    const upButtons = screen.getAllByTitle('위로');
    const downButtons = screen.getAllByTitle('아래로');
    expect(upButtons[0]).toBeDisabled();
    expect(downButtons[downButtons.length - 1]).toBeDisabled();

    fireEvent.click(upButtons[0]);
    fireEvent.click(downButtons[downButtons.length - 1]);

    expect(mocks.reorderAlbumTracks).not.toHaveBeenCalled();
    expect(mocks.fetchAlbumDetail).toHaveBeenCalledTimes(1);
  });

  it('reports rejection without retry and adopts one authoritative recovery refetch', async () => {
    mocks.reorderAlbumTracks.mockRejectedValueOnce(new Error('reorder failed'));
    mocks.fetchAlbumDetail
      .mockResolvedValueOnce(initialAlbum)
      .mockResolvedValueOnce(albumWithOrder([21, 23, 22]));

    renderPage();
    expect(await screen.findByText('First Track')).toBeInTheDocument();

    fireEvent.click(screen.getAllByTitle('아래로')[0]);

    await waitFor(() => expect(mocks.toast).toHaveBeenCalledWith('error', 'reorder failed'));
    await waitFor(() => expect(mocks.fetchAlbumDetail).toHaveBeenCalledTimes(2));
    expect(trackTitles()).toEqual(['First Track', 'Third Track', 'Second Track']);
    expect(mocks.reorderAlbumTracks).toHaveBeenCalledTimes(1);
  });

  it('keeps rejected-reorder provenance neutral when recovery and retry reads both fail', async () => {
    mocks.reorderAlbumTracks.mockRejectedValueOnce(new Error('reorder failed'));
    mocks.fetchAlbumDetail
      .mockResolvedValueOnce(initialAlbum)
      .mockRejectedValueOnce(new Error('recovery failed'))
      .mockRejectedValueOnce(new Error('retry failed'));

    renderPage();
    expect(await screen.findByText('First Track')).toBeInTheDocument();
    fireEvent.click(screen.getAllByTitle('아래로')[0]);

    const neutralMessage = '변경 결과와 최신 트랙 목록을 확인하지 못했습니다.';
    expect(await screen.findByText(neutralMessage)).toBeInTheDocument();
    expect(mocks.reorderAlbumTracks).toHaveBeenCalledTimes(1);

    fireEvent.click(screen.getByRole('button', { name: '트랙 목록 다시 불러오기' }));
    await waitFor(() => expect(mocks.fetchAlbumDetail).toHaveBeenCalledTimes(3));

    expect(screen.getByText(neutralMessage)).toBeInTheDocument();
    expect(
      screen.queryByText('변경은 완료되었지만 최신 트랙 목록을 불러오지 못했습니다.'),
    ).not.toBeInTheDocument();
    expect(mocks.reorderAlbumTracks).toHaveBeenCalledTimes(1);
  });

  it('clears neutral rejected-reorder recovery after a successful read-only retry', async () => {
    mocks.reorderAlbumTracks.mockRejectedValueOnce(new Error('reorder failed'));
    mocks.fetchAlbumDetail
      .mockResolvedValueOnce(initialAlbum)
      .mockRejectedValueOnce(new Error('recovery failed'))
      .mockResolvedValueOnce(albumWithOrder([21, 23, 22]));

    renderPage();
    expect(await screen.findByText('First Track')).toBeInTheDocument();
    fireEvent.click(screen.getAllByTitle('아래로')[0]);

    expect(
      await screen.findByText('변경 결과와 최신 트랙 목록을 확인하지 못했습니다.'),
    ).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '트랙 목록 다시 불러오기' }));

    await waitFor(() => expect(mocks.fetchAlbumDetail).toHaveBeenCalledTimes(3));
    await waitFor(() =>
      expect(
        screen.queryByRole('alert', { name: '앨범 트랙 새로고침 실패' }),
      ).not.toBeInTheDocument(),
    );
    expect(trackTitles()).toEqual(['First Track', 'Third Track', 'Second Track']);
    expect(mocks.reorderAlbumTracks).toHaveBeenCalledTimes(1);
  });

  it.each([
    {
      name: 'add',
      start: async () => {
        const searchInput = screen.getByRole('combobox', { name: '앨범에 추가할 트랙 검색' });
        fireEvent.change(searchInput, { target: { value: 'new' } });
        fireEvent.click(screen.getByRole('button', { name: '검색' }));
        const option = await screen.findByRole('option', { name: /New Track/ });
        fireEvent.click(option);
      },
      mutation: () => mocks.addTrackToAlbum,
    },
    {
      name: 'remove',
      start: async () => {
        fireEvent.click(screen.getAllByTitle('제거')[0]);
      },
      mutation: () => mocks.removeTrackFromAlbum,
    },
    {
      name: 'reorder',
      start: async () => {
        fireEvent.click(screen.getAllByTitle('아래로')[0]);
      },
      mutation: () => mocks.reorderAlbumTracks,
    },
  ])(
    'represents a committed $name with failed refresh and retries only the membership read',
    async ({ start, mutation }) => {
      mocks.fetchTracks.mockResolvedValue({
        dataList: [
          {
            id: 42,
            title: 'New Track',
            artistName: 'Creator',
            duration: 30,
            bpm: 120,
            tonality: 'C',
            thumbnail: null,
            waveformData: null,
            playCount: 0,
            likeCount: 0,
            downloadCount: 0,
            tags: [],
            createdAt: '2026-08-01T00:00:00Z',
          },
        ],
        pageInfo: { page: 1, size: 10, total: 1, start: 1, end: 1, prev: false, next: false },
      });
      mocks.fetchAlbumDetail
        .mockResolvedValueOnce(initialAlbum)
        .mockRejectedValueOnce(new Error('refresh failed'))
        .mockResolvedValueOnce(albumWithOrder([21, 22, 23]));

      renderPage();
      expect(await screen.findByText('First Track')).toBeInTheDocument();
      await start();

      expect(
        await screen.findByRole('alert', { name: '앨범 트랙 새로고침 실패' }),
      ).toBeInTheDocument();
      expect(mutation()).toHaveBeenCalledTimes(1);

      fireEvent.click(screen.getByRole('button', { name: '트랙 목록 다시 불러오기' }));
      await waitFor(() => expect(mocks.fetchAlbumDetail).toHaveBeenCalledTimes(3));
      expect(mutation()).toHaveBeenCalledTimes(1);
      await waitFor(() =>
        expect(
          screen.queryByRole('alert', { name: '앨범 트랙 새로고침 실패' }),
        ).not.toBeInTheDocument(),
      );
    },
  );
});
