import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import TrackUploadPage from './TrackUploadPage';

const mocks = vi.hoisted(() => ({
  createTrack: vi.fn(),
  fetchTags: vi.fn(),
}));

vi.mock('@/api/tracks', () => ({
  createTrack: (...args: unknown[]) => mocks.createTrack(...args),
}));

vi.mock('@/api/tags', () => ({
  fetchTags: (...args: unknown[]) => mocks.fetchTags(...args),
}));

function loadWithDimensions(image: HTMLElement, width: number, height: number) {
  Object.defineProperty(image, 'naturalWidth', { configurable: true, value: width });
  Object.defineProperty(image, 'naturalHeight', { configurable: true, value: height });
  fireEvent.load(image);
}

describe('TrackUploadPage thumbnail contract', () => {
  beforeEach(() => {
    mocks.createTrack.mockReset().mockResolvedValue(undefined);
    mocks.fetchTags.mockReset().mockResolvedValue([]);
    vi.stubGlobal('crypto', { randomUUID: vi.fn(() => 'track-entry-1') });
    Object.defineProperty(URL, 'createObjectURL', {
      configurable: true,
      value: vi.fn().mockReturnValueOnce('blob:wide').mockReturnValueOnce('blob:square'),
    });
    Object.defineProperty(URL, 'revokeObjectURL', {
      configurable: true,
      value: vi.fn(),
    });
  });

  it('blocks the multi-row submit for pending or invalid covers and submits a valid square file', async () => {
    const view = render(
      <MemoryRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
        <TrackUploadPage />
      </MemoryRouter>,
    );
    const audioInput = view.container.querySelector('input[type="file"][multiple]');
    const audioFile = new File(['audio'], 'launch.mp3', { type: 'audio/mpeg' });
    fireEvent.change(audioInput!, { target: { files: [audioFile] } });

    expect(await screen.findByDisplayValue('launch')).toBeInTheDocument();
    expect(screen.getByText('JPEG 또는 PNG, 1:1 필수, 10MB 이하')).toBeInTheDocument();
    expect(screen.getByText('2048x2048px 권장 (필수 아님)')).toBeInTheDocument();
    const thumbnailInput = screen.getByLabelText('썸네일 이미지');
    expect(thumbnailInput).toHaveAttribute('accept', 'image/jpeg,image/png');

    const wideFile = new File(['wide'], 'wide.png', { type: 'image/png' });
    fireEvent.change(thumbnailInput, { target: { files: [wideFile] } });
    expect(screen.getByRole('button', { name: '업로드' })).toBeDisabled();
    const widePreview = await screen.findByAltText('선택한 트랙 썸네일 미리보기');
    loadWithDimensions(widePreview, 800, 600);
    expect(
      screen.getByText('트랙 썸네일은 가로와 세로 길이가 같은 1:1 이미지여야 합니다.'),
    ).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '업로드' })).toBeDisabled();

    const squareFile = new File(['square'], 'square.jpg', { type: 'image/jpeg' });
    fireEvent.change(thumbnailInput, { target: { files: [squareFile] } });
    expect(screen.getByRole('button', { name: '업로드' })).toBeDisabled();
    const squarePreview = await screen.findByAltText('선택한 트랙 썸네일 미리보기');
    await waitFor(() => expect(squarePreview).toHaveAttribute('src', 'blob:square'));
    loadWithDimensions(squarePreview, 1200, 1200);
    expect(screen.getByRole('button', { name: '업로드' })).toBeEnabled();

    fireEvent.change(screen.getByPlaceholderText('BPM을 입력해주세요'), {
      target: { value: '120' },
    });
    fireEvent.change(screen.getByRole('combobox'), { target: { value: 'C' } });
    fireEvent.click(screen.getByRole('button', { name: '업로드' }));

    await waitFor(() => expect(mocks.createTrack).toHaveBeenCalledTimes(1));
    const formData = mocks.createTrack.mock.calls[0][0] as FormData;
    expect(formData.get('audioFile')).toBe(audioFile);
    expect(formData.get('thumbnail')).toBe(squareFile);
  });
});
