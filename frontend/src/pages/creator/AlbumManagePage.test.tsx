import { act, fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { MemoryRouter, Route, Routes, useLocation } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { AlbumDetail } from '@/api/albums';
import type { Album, PageInfo } from '@/types';
import AlbumManagePage from './AlbumManagePage';

const mocks = vi.hoisted(() => ({
  fetchAlbums: vi.fn(),
  fetchAlbumDetail: vi.fn(),
  createAlbum: vi.fn(),
  updateAlbum: vi.fn(),
  deleteAlbum: vi.fn(),
}));

vi.mock('@/api/albums', () => ({
  fetchAlbums: (...args: unknown[]) => mocks.fetchAlbums(...args),
  fetchAlbumDetail: (...args: unknown[]) => mocks.fetchAlbumDetail(...args),
  createAlbum: (...args: unknown[]) => mocks.createAlbum(...args),
  updateAlbum: (...args: unknown[]) => mocks.updateAlbum(...args),
  deleteAlbum: (...args: unknown[]) => mocks.deleteAlbum(...args),
}));

vi.mock('@/api/client', () => ({
  toUploadUrl: (path: string | null | undefined) => path,
}));

const albums: Album[] = [
  {
    id: 11,
    title: 'First Album',
    description: null,
    thumbnailUrl: null,
    trackCount: 1,
    likeCount: 0,
    createdAt: '2026-08-01T00:00:00Z',
  },
  {
    id: 12,
    title: 'Second Album',
    description: null,
    thumbnailUrl: null,
    trackCount: 0,
    likeCount: 0,
    createdAt: '2026-08-02T00:00:00Z',
  },
];

function pageInfo(page: number, total: number): PageInfo {
  const totalPages = Math.max(1, Math.ceil(total / 20));
  return {
    page,
    size: 20,
    total,
    start: total === 0 ? 0 : Math.floor((page - 1) / 10) * 10 + 1,
    end: total === 0 ? 0 : Math.min(totalPages, Math.floor((page - 1) / 10) * 10 + 10),
    prev: page > 1,
    next: page < totalPages,
  };
}

function albumDetail(album: Album, description: string): AlbumDetail {
  return {
    id: album.id,
    title: album.title,
    description,
    thumbnailUrl: album.thumbnailUrl,
    tracks: [],
    likeCount: album.likeCount,
    createdAt: album.createdAt,
  };
}

function deferred<T>() {
  let resolve!: (value: T) => void;
  let reject!: (reason?: unknown) => void;
  const promise = new Promise<T>((res, rej) => {
    resolve = res;
    reject = rej;
  });
  return { promise, resolve, reject };
}

function LocationProbe() {
  const location = useLocation();
  return <span data-testid="location">{`${location.pathname}${location.search}`}</span>;
}

function renderPage(path = '/admin/albums') {
  return render(
    <MemoryRouter initialEntries={[path]}>
      <Routes>
        <Route
          path="/admin/albums"
          element={
            <>
              <AlbumManagePage />
              <LocationProbe />
            </>
          }
        />
      </Routes>
    </MemoryRouter>,
  );
}

describe('AlbumManagePage', () => {
  beforeEach(() => {
    vi.resetAllMocks();
    mocks.fetchAlbums.mockResolvedValue({ dataList: albums, pageInfo: pageInfo(1, 2) });
    mocks.fetchAlbumDetail.mockResolvedValue(albumDetail(albums[0], 'First description'));
    mocks.createAlbum.mockResolvedValue(albums[0]);
    mocks.updateAlbum.mockResolvedValue(albums[0]);
    mocks.deleteAlbum.mockResolvedValue(undefined);
  });

  it('normalizes invalid and beyond-last pages without request loops', async () => {
    const invalid = renderPage('/admin/albums?page=invalid');
    await waitFor(() => expect(screen.getByTestId('location')).toHaveTextContent('?page=1'));
    await waitFor(() =>
      expect(mocks.fetchAlbums).toHaveBeenCalledWith(
        { page: 1, size: 20 },
        expect.any(AbortSignal),
      ),
    );
    invalid.unmount();

    mocks.fetchAlbums.mockReset();
    mocks.fetchAlbums
      .mockResolvedValueOnce({ dataList: [], pageInfo: pageInfo(7, 21) })
      .mockResolvedValueOnce({ dataList: [albums[1]], pageInfo: pageInfo(2, 21) });

    renderPage('/admin/albums?page=7');
    await waitFor(() => expect(screen.getByTestId('location')).toHaveTextContent('?page=2'));
    expect(await screen.findByText('Second Album')).toBeInTheDocument();
    expect(mocks.fetchAlbums).toHaveBeenCalledTimes(2);
    expect(mocks.fetchAlbums.mock.calls[0][0]).toEqual({ page: 7, size: 20 });
    expect(mocks.fetchAlbums.mock.calls[1][0]).toEqual({ page: 2, size: 20 });
  });

  it('exposes every management page through bounded URL navigation', async () => {
    mocks.fetchAlbums
      .mockResolvedValueOnce({ dataList: [albums[0]], pageInfo: pageInfo(1, 41) })
      .mockResolvedValueOnce({ dataList: [albums[1]], pageInfo: pageInfo(2, 41) });

    renderPage();
    expect(await screen.findByText('First Album')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '2페이지' }));

    await waitFor(() => expect(screen.getByTestId('location')).toHaveTextContent('?page=2'));
    expect(await screen.findByText('Second Album')).toBeInTheDocument();
    expect(mocks.fetchAlbums).toHaveBeenLastCalledWith(
      { page: 2, size: 20 },
      expect.any(AbortSignal),
    );
  });

  it('shows bounded list failure recovery and retries only on command', async () => {
    mocks.fetchAlbums
      .mockRejectedValueOnce(new Error('network'))
      .mockResolvedValueOnce({ dataList: albums, pageInfo: pageInfo(1, 2) });

    renderPage();
    expect(
      await screen.findByRole('alert', { name: '앨범 목록 불러오기 실패' }),
    ).toBeInTheDocument();
    expect(mocks.fetchAlbums).toHaveBeenCalledTimes(1);

    fireEvent.click(screen.getByRole('button', { name: '앨범 목록 다시 시도' }));
    expect(await screen.findByText('First Album')).toBeInTheDocument();
    expect(mocks.fetchAlbums).toHaveBeenCalledTimes(2);
  });

  it('keeps modal detail owned by the active Album across close and target switch', async () => {
    const first = deferred<AlbumDetail>();
    const second = deferred<AlbumDetail>();
    mocks.fetchAlbumDetail
      .mockReset()
      .mockReturnValueOnce(first.promise)
      .mockReturnValueOnce(second.promise);

    renderPage();
    expect(await screen.findByText('First Album')).toBeInTheDocument();
    const editButtons = screen.getAllByRole('button', { name: '수정' });
    fireEvent.click(editButtons[0]);

    let dialog = screen.getByRole('dialog', { name: '앨범 수정' });
    expect(within(dialog).getByText('앨범 정보를 불러오는 중...')).toBeInTheDocument();
    expect(within(dialog).getByPlaceholderText('앨범 제목')).toBeDisabled();
    fireEvent.click(within(dialog).getByRole('button', { name: '닫기' }));

    fireEvent.click(screen.getAllByRole('button', { name: '수정' })[1]);
    dialog = screen.getByRole('dialog', { name: '앨범 수정' });
    expect(within(dialog).getByPlaceholderText('앨범 제목')).toHaveValue('');

    await act(async () => first.resolve(albumDetail(albums[0], 'Stale first description')));
    expect(within(dialog).getByPlaceholderText('앨범 제목')).toHaveValue('');

    await act(async () => second.resolve(albumDetail(albums[1], 'Second description')));
    expect(within(dialog).getByPlaceholderText('앨범 제목')).toHaveValue('Second Album');
    expect(within(dialog).getByPlaceholderText('앨범에 대한 설명 (선택사항)')).toHaveValue(
      'Second description',
    );
  });

  it('keeps failed modal detail non-submittable and retries the same target', async () => {
    mocks.fetchAlbumDetail
      .mockRejectedValueOnce(new Error('network'))
      .mockResolvedValueOnce(albumDetail(albums[0], 'Recovered description'));

    renderPage();
    expect(await screen.findByText('First Album')).toBeInTheDocument();
    fireEvent.click(screen.getAllByRole('button', { name: '수정' })[0]);

    const dialog = screen.getByRole('dialog', { name: '앨범 수정' });
    expect(
      await within(dialog).findByRole('alert', { name: '앨범 정보 불러오기 실패' }),
    ).toBeInTheDocument();
    expect(within(dialog).getByRole('button', { name: '저장' })).toBeDisabled();
    fireEvent.click(within(dialog).getByRole('button', { name: '앨범 정보 다시 시도' }));

    await waitFor(() => expect(mocks.fetchAlbumDetail).toHaveBeenCalledTimes(2));
    expect(within(dialog).getByPlaceholderText('앨범 제목')).toHaveValue('First Album');
  });

  it('sends blank description as an explicit edit clear', async () => {
    renderPage();
    expect(await screen.findByText('First Album')).toBeInTheDocument();
    fireEvent.click(screen.getAllByRole('button', { name: '수정' })[0]);

    const dialog = screen.getByRole('dialog', { name: '앨범 수정' });
    const description = await within(dialog).findByPlaceholderText('앨범에 대한 설명 (선택사항)');
    expect(description).toHaveValue('First description');
    fireEvent.change(description, { target: { value: '   ' } });
    fireEvent.click(within(dialog).getByRole('button', { name: '저장' }));

    await waitFor(() => expect(mocks.updateAlbum).toHaveBeenCalledTimes(1));
    const payload = mocks.updateAlbum.mock.calls[0][1] as FormData;
    expect(payload.get('description')).toBe('');
  });

  it('uses the shared thumbnail pending fence inside the create modal', async () => {
    Object.defineProperty(URL, 'createObjectURL', {
      configurable: true,
      value: vi.fn(() => 'blob:manage'),
    });
    Object.defineProperty(URL, 'revokeObjectURL', {
      configurable: true,
      value: vi.fn(),
    });

    renderPage();
    expect(await screen.findByText('First Album')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '+ 새 앨범' }));
    const dialog = screen.getByRole('dialog', { name: '새 앨범' });
    fireEvent.change(within(dialog).getByLabelText('앨범 썸네일 이미지'), {
      target: { files: [new File(['image'], 'cover.png', { type: 'image/png' })] },
    });

    expect(within(dialog).getByRole('button', { name: '생성' })).toBeDisabled();
    const preview = await within(dialog).findByAltText('선택한 앨범 썸네일 미리보기');
    Object.defineProperty(preview, 'naturalWidth', { configurable: true, value: 640 });
    Object.defineProperty(preview, 'naturalHeight', { configurable: true, value: 480 });
    fireEvent.load(preview);
    expect(within(dialog).getByRole('button', { name: '생성' })).toBeEnabled();
  });
});
