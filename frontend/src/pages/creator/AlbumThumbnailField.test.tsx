import { useState } from 'react';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import AlbumThumbnailField from './AlbumThumbnailField';
import {
  emptyAlbumThumbnailSelection,
  isAlbumThumbnailBlocked,
  type AlbumThumbnailSelection,
} from './albumThumbnail';

const createObjectURL = vi.fn<(file: Blob) => string>();
const revokeObjectURL = vi.fn<(url: string) => void>();

function Harness() {
  const [selection, setSelection] = useState<AlbumThumbnailSelection>(emptyAlbumThumbnailSelection);
  return (
    <>
      <AlbumThumbnailField value={selection} onChange={setSelection} />
      <button type="button" disabled={isAlbumThumbnailBlocked(selection)}>
        Submit
      </button>
      <output>{selection.status}</output>
    </>
  );
}

function selectThumbnail(file: File) {
  fireEvent.change(screen.getByLabelText('앨범 썸네일 이미지'), {
    target: { files: [file] },
  });
}

function loadWithDimensions(image: HTMLElement, width: number, height: number) {
  Object.defineProperty(image, 'naturalWidth', { configurable: true, value: width });
  Object.defineProperty(image, 'naturalHeight', { configurable: true, value: height });
  fireEvent.load(image);
}

function sizedFile(name: string, type: string, size: number): File {
  const file = new File(['image'], name, { type });
  Object.defineProperty(file, 'size', { configurable: true, value: size });
  return file;
}

describe('AlbumThumbnailField', () => {
  beforeEach(() => {
    createObjectURL.mockReset();
    revokeObjectURL.mockReset();
    Object.defineProperty(URL, 'createObjectURL', {
      configurable: true,
      value: createObjectURL,
    });
    Object.defineProperty(URL, 'revokeObjectURL', {
      configurable: true,
      value: revokeObjectURL,
    });
  });

  it('exposes the backend JPEG/PNG, size, and decoded-dimension contract', () => {
    render(<Harness />);

    expect(screen.getByLabelText('앨범 썸네일 이미지')).toHaveAttribute(
      'accept',
      'image/jpeg,image/png',
    );
    expect(screen.getByText('JPEG 또는 PNG, 10MB 이하, 최대 4096x4096px')).toBeInTheDocument();
  });

  it.each([
    { label: 'JPEG', type: 'image/jpeg' },
    { label: 'PNG', type: 'image/png' },
  ])('lets an extensionless valid $label selection reach browser decode', async ({ type }) => {
    createObjectURL.mockReturnValue(`blob:${type}`);
    render(<Harness />);
    const file = new File(['image'], 'cover', { type });

    selectThumbnail(file);

    expect(createObjectURL).toHaveBeenCalledWith(file);
    const preview = await screen.findByAltText('선택한 앨범 썸네일 미리보기');
    loadWithDimensions(preview, 800, 600);
    expect(screen.getByText('valid')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Submit' })).toBeEnabled();
  });

  it('rejects an incompatible supplied MIME before decode', () => {
    render(<Harness />);

    selectThumbnail(new File(['image'], 'cover', { type: 'image/gif' }));

    expect(createObjectURL).not.toHaveBeenCalled();
    expect(screen.getByRole('alert')).toHaveTextContent(
      '앨범 썸네일은 JPEG 또는 PNG 파일만 업로드할 수 있습니다.',
    );
    expect(screen.getByText('invalid')).toBeInTheDocument();
  });

  it('routes corrupt compatible data through decode and rejects the decode failure', async () => {
    createObjectURL.mockReturnValue('blob:corrupt');
    render(<Harness />);
    const file = new File(['not-an-image'], 'cover', { type: 'image/png' });

    selectThumbnail(file);

    expect(createObjectURL).toHaveBeenCalledWith(file);
    const preview = await screen.findByAltText('선택한 앨범 썸네일 미리보기');
    fireEvent.error(preview);
    expect(screen.getByRole('alert')).toHaveTextContent(
      '이미지 파일을 읽을 수 없습니다. JPEG 또는 PNG 파일인지 확인해주세요.',
    );
    expect(screen.getByText('invalid')).toBeInTheDocument();
  });

  it('blocks while pending, accepts a valid non-square image, and revokes on unmount', async () => {
    createObjectURL.mockReturnValue('blob:valid');
    const view = render(<Harness />);
    const file = new File(['image'], 'cover.png', { type: 'image/png' });

    selectThumbnail(file);
    expect(screen.getByRole('button', { name: 'Submit' })).toBeDisabled();
    expect(screen.getByText('이미지 크기를 확인하는 중입니다.')).toBeInTheDocument();

    const preview = await screen.findByAltText('선택한 앨범 썸네일 미리보기');
    loadWithDimensions(preview, 1200, 800);
    expect(screen.getByRole('button', { name: 'Submit' })).toBeEnabled();
    expect(screen.getByText('valid')).toBeInTheDocument();

    view.unmount();
    expect(revokeObjectURL).toHaveBeenCalledWith('blob:valid');
  });

  it('ignores stale completion and revokes replacement, rejection, and clear lifecycles', async () => {
    createObjectURL
      .mockReturnValueOnce('blob:first')
      .mockReturnValueOnce('blob:second')
      .mockReturnValueOnce('blob:clear');
    render(<Harness />);
    const firstFile = new File(['first'], 'first.png', { type: 'image/png' });
    const secondFile = new File(['second'], 'second.jpg', { type: 'image/jpeg' });

    selectThumbnail(firstFile);
    const stalePreview = await screen.findByAltText('선택한 앨범 썸네일 미리보기');
    selectThumbnail(secondFile);
    await waitFor(() =>
      expect(screen.getByAltText('선택한 앨범 썸네일 미리보기')).toHaveAttribute(
        'src',
        'blob:second',
      ),
    );
    expect(revokeObjectURL).toHaveBeenCalledWith('blob:first');

    loadWithDimensions(stalePreview, 800, 800);
    expect(screen.getByRole('button', { name: 'Submit' })).toBeDisabled();
    loadWithDimensions(screen.getByAltText('선택한 앨범 썸네일 미리보기'), 800, 600);
    expect(screen.getByRole('button', { name: 'Submit' })).toBeEnabled();

    selectThumbnail(new File(['gif'], 'bad.gif', { type: 'image/gif' }));
    expect(revokeObjectURL).toHaveBeenCalledWith('blob:second');
    expect(screen.getByRole('alert')).toHaveTextContent(
      '앨범 썸네일은 JPEG 또는 PNG 파일만 업로드할 수 있습니다.',
    );

    selectThumbnail(new File(['clear'], 'clear.png', { type: 'image/png' }));
    loadWithDimensions(await screen.findByAltText('선택한 앨범 썸네일 미리보기'), 800, 600);
    fireEvent.click(screen.getByRole('button', { name: '선택 지우기' }));
    expect(screen.getByText('empty')).toBeInTheDocument();
    expect(revokeObjectURL).toHaveBeenCalledWith('blob:clear');
  });

  it('rejects oversized files and decoded images beyond the server bounds', async () => {
    createObjectURL.mockReturnValue('blob:large-dimensions');
    render(<Harness />);

    selectThumbnail(sizedFile('large.png', 'image/png', 10 * 1024 * 1024 + 1));
    expect(screen.getByRole('alert')).toHaveTextContent(
      '앨범 썸네일은 10MB 이하만 업로드할 수 있습니다.',
    );
    expect(createObjectURL).not.toHaveBeenCalled();

    selectThumbnail(new File(['image'], 'large.png', { type: 'image/png' }));
    const preview = await screen.findByAltText('선택한 앨범 썸네일 미리보기');
    loadWithDimensions(preview, 4097, 100);
    expect(screen.getByRole('alert')).toHaveTextContent(
      '앨범 썸네일은 가로와 세로가 각각 4096px 이하여야 합니다.',
    );
    expect(screen.getByRole('button', { name: 'Submit' })).toBeDisabled();
  });

  it('allows the same file to be retried after a decoded-image rejection', async () => {
    createObjectURL.mockReturnValueOnce('blob:retry-1').mockReturnValueOnce('blob:retry-2');
    render(<Harness />);
    const file = new File(['image'], 'retry.png', { type: 'image/png' });

    selectThumbnail(file);
    loadWithDimensions(await screen.findByAltText('선택한 앨범 썸네일 미리보기'), 5000, 100);
    expect(screen.getByRole('alert')).toBeInTheDocument();

    selectThumbnail(file);
    await waitFor(() =>
      expect(screen.getByAltText('선택한 앨범 썸네일 미리보기')).toHaveAttribute(
        'src',
        'blob:retry-2',
      ),
    );
    loadWithDimensions(screen.getByAltText('선택한 앨범 썸네일 미리보기'), 1000, 500);
    expect(screen.getByRole('button', { name: 'Submit' })).toBeEnabled();
  });
});
