import { useState } from 'react';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import TrackThumbnailField from './TrackThumbnailField';
import { emptyTrackThumbnailSelection, type TrackThumbnailSelection } from './trackThumbnail';

const createObjectURL = vi.fn<(file: Blob) => string>();
const revokeObjectURL = vi.fn<(url: string) => void>();

function Harness({ existingImageUrl = null }: { existingImageUrl?: string | null }) {
  const [selection, setSelection] = useState<TrackThumbnailSelection>(() =>
    emptyTrackThumbnailSelection(),
  );
  const blocked = selection.status === 'pending' || selection.status === 'invalid';

  return (
    <>
      <TrackThumbnailField
        value={selection}
        onChange={setSelection}
        existingImageUrl={existingImageUrl}
        existingFileName={existingImageUrl ? 'existing.jpg' : null}
      />
      <button type="button" disabled={blocked}>
        Submit
      </button>
    </>
  );
}

function selectThumbnail(file: File) {
  fireEvent.change(screen.getByLabelText('썸네일 이미지'), {
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

describe('TrackThumbnailField', () => {
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

  it('shows the JPEG/PNG, exact 1:1, 10MB, and recommended-size contract', () => {
    render(<Harness />);

    expect(screen.getByText('JPEG 또는 PNG, 1:1 필수, 10MB 이하')).toBeInTheDocument();
    expect(screen.getByText('2048x2048px 권장 (필수 아님)')).toBeInTheDocument();
    expect(screen.getByLabelText('썸네일 이미지')).toHaveAttribute(
      'accept',
      'image/jpeg,image/png',
    );
  });

  it('renders a stable square cover preview and blocks only while pending', async () => {
    createObjectURL.mockReturnValue('blob:square');
    const view = render(<Harness />);

    selectThumbnail(new File(['square'], 'square.png', { type: 'image/png' }));

    const image = await screen.findByAltText('선택한 트랙 썸네일 미리보기');
    const preview = screen.getByTestId('track-thumbnail-preview');
    expect(image).toHaveAttribute('src', 'blob:square');
    expect(preview).toHaveAttribute('data-preview-ratio', '1:1');
    expect(getComputedStyle(preview).aspectRatio).toBe('1 / 1');
    expect(getComputedStyle(image).objectFit).toBe('cover');
    expect(getComputedStyle(image).objectPosition).toBe('center');
    expect(screen.getByRole('button', { name: 'Submit' })).toBeDisabled();

    loadWithDimensions(image, 640, 640);
    expect(screen.queryByText('이미지 크기를 확인하는 중입니다.')).not.toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Submit' })).toBeEnabled();

    view.unmount();
    expect(revokeObjectURL).toHaveBeenCalledWith('blob:square');
  });

  it('keeps the selected cover preview but blocks a non-square image with a field error', async () => {
    createObjectURL.mockReturnValue('blob:wide');
    render(<Harness />);

    selectThumbnail(new File(['wide'], 'wide.jpg', { type: 'image/jpeg' }));
    const image = await screen.findByAltText('선택한 트랙 썸네일 미리보기');
    loadWithDimensions(image, 640, 480);

    expect(
      screen.getByText('트랙 썸네일은 가로와 세로 길이가 같은 1:1 이미지여야 합니다.'),
    ).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Submit' })).toBeDisabled();
    expect(screen.getByTestId('track-thumbnail-preview')).toBeInTheDocument();
  });

  it('ignores a stale load result and revokes replaced and unmounted object URLs', async () => {
    createObjectURL.mockReturnValueOnce('blob:first').mockReturnValueOnce('blob:second');
    const view = render(<Harness />);

    selectThumbnail(new File(['first'], 'first.png', { type: 'image/png' }));
    const staleImage = await screen.findByAltText('선택한 트랙 썸네일 미리보기');

    selectThumbnail(new File(['second'], 'second.png', { type: 'image/png' }));
    await waitFor(() =>
      expect(screen.getByAltText('선택한 트랙 썸네일 미리보기')).toHaveAttribute(
        'src',
        'blob:second',
      ),
    );
    expect(revokeObjectURL).toHaveBeenCalledWith('blob:first');

    loadWithDimensions(staleImage, 400, 400);
    expect(screen.getByRole('button', { name: 'Submit' })).toBeDisabled();

    const currentImage = screen.getByAltText('선택한 트랙 썸네일 미리보기');
    loadWithDimensions(currentImage, 800, 800);
    expect(screen.getByRole('button', { name: 'Submit' })).toBeEnabled();

    view.unmount();
    expect(revokeObjectURL).toHaveBeenCalledWith('blob:second');
  });

  it('rejects unsupported and oversized selections before creating an object URL', () => {
    render(<Harness />);

    selectThumbnail(new File(['gif'], 'cover.gif', { type: 'image/gif' }));
    expect(
      screen.getByText('트랙 썸네일은 JPEG 또는 PNG 파일만 업로드할 수 있습니다.'),
    ).toBeInTheDocument();
    expect(createObjectURL).not.toHaveBeenCalled();

    selectThumbnail(sizedFile('large.png', 'image/png', 10 * 1024 * 1024 + 1));
    expect(screen.getByText('트랙 썸네일은 10MB 이하만 업로드할 수 있습니다.')).toBeInTheDocument();
    expect(createObjectURL).not.toHaveBeenCalled();
  });

  it('warns only after a non-square existing image loads successfully', () => {
    const first = render(<Harness existingImageUrl="/uploads/tracks/thumbnail/wide.jpg" />);
    const existingImage = screen.getByAltText('현재 트랙 썸네일');
    expect(screen.queryByText(/새 정사각형 이미지로 교체를 권장/)).not.toBeInTheDocument();

    loadWithDimensions(existingImage, 564, 1404);
    expect(
      screen.getByText('현재 썸네일이 1:1이 아닙니다. 새 정사각형 이미지로 교체를 권장합니다.'),
    ).toBeInTheDocument();
    first.unmount();

    render(<Harness existingImageUrl="/uploads/tracks/thumbnail/unreadable.jpg" />);
    fireEvent.error(screen.getByAltText('현재 트랙 썸네일'));
    expect(screen.queryByText(/새 정사각형 이미지로 교체를 권장/)).not.toBeInTheDocument();
  });
});
