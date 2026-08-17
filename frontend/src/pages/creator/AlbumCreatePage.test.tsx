import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import AlbumCreatePage from './AlbumCreatePage';

const mocks = vi.hoisted(() => ({
  createAlbum: vi.fn(),
  navigate: vi.fn(),
}));

vi.mock('@/api/albums', () => ({
  createAlbum: (...args: unknown[]) => mocks.createAlbum(...args),
}));

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual<typeof import('react-router-dom')>('react-router-dom');
  return { ...actual, useNavigate: () => mocks.navigate };
});

function renderPage() {
  return render(
    <MemoryRouter>
      <AlbumCreatePage />
    </MemoryRouter>,
  );
}

describe('AlbumCreatePage', () => {
  beforeEach(() => {
    vi.resetAllMocks();
    mocks.createAlbum.mockResolvedValue({});
    Object.defineProperty(URL, 'createObjectURL', {
      configurable: true,
      value: vi.fn(() => 'blob:create'),
    });
    Object.defineProperty(URL, 'revokeObjectURL', {
      configurable: true,
      value: vi.fn(),
    });
  });

  it('blocks submission until shared thumbnail validation owns the current selection', async () => {
    const view = renderPage();
    fireEvent.change(screen.getByPlaceholderText('앨범 제목'), { target: { value: 'Launch' } });
    const file = new File(['image'], 'cover.png', { type: 'image/png' });
    fireEvent.change(screen.getByLabelText('앨범 썸네일 이미지'), { target: { files: [file] } });

    const submit = screen.getByRole('button', { name: '만들기' });
    expect(submit).toBeDisabled();
    const preview = await screen.findByAltText('선택한 앨범 썸네일 미리보기');
    Object.defineProperty(preview, 'naturalWidth', { configurable: true, value: 1200 });
    Object.defineProperty(preview, 'naturalHeight', { configurable: true, value: 800 });
    fireEvent.load(preview);
    expect(submit).toBeEnabled();

    fireEvent.click(submit);
    await waitFor(() => expect(mocks.createAlbum).toHaveBeenCalledTimes(1));
    const payload = mocks.createAlbum.mock.calls[0][0] as FormData;
    expect(payload.get('thumbnailFile')).toBe(file);

    view.unmount();
    expect(URL.revokeObjectURL).toHaveBeenCalledWith('blob:create');
  });
});
