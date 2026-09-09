import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import TrackUploadPage from './TrackUploadPage';

const mocks = vi.hoisted(() => ({
  createTrack: vi.fn(),
  fetchTags: vi.fn(),
  fetchAudioProcessing: vi.fn(),
  retryAudioProcessing: vi.fn(),
}));

vi.mock('@/api/tracks', () => ({
  createTrack: (...args: unknown[]) => mocks.createTrack(...args),
  fetchAudioProcessing: (...args: unknown[]) => mocks.fetchAudioProcessing(...args),
  retryAudioProcessing: (...args: unknown[]) => mocks.retryAudioProcessing(...args),
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
    mocks.fetchAudioProcessing.mockReset();
    mocks.retryAudioProcessing.mockReset();
    mocks.createTrack.mockReset().mockResolvedValue(undefined);
    mocks.fetchTags.mockReset().mockResolvedValue([]);
    Object.defineProperty(navigator, 'userAgent', { configurable: true, value: 'Desktop' });
    Object.defineProperty(navigator, 'platform', { configurable: true, value: 'Win32' });
    Object.defineProperty(navigator, 'maxTouchPoints', { configurable: true, value: 0 });
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

  it('accepts a sized 100MiB WAV and rejects one byte more before creating a row', async () => {
    render(
      <MemoryRouter>
        <TrackUploadPage />
      </MemoryRouter>,
    );
    const audioInput = screen.getByLabelText('오디오 파일 선택', { selector: 'input' });
    const over = new File(['x'], 'over.wav', { type: 'audio/wav' });
    Object.defineProperty(over, 'size', { value: 104857601 });
    fireEvent.change(audioInput, { target: { files: [over] } });
    expect(screen.getByRole('alert')).toHaveTextContent('100MB 이하');
    expect(screen.queryByDisplayValue('over')).not.toBeInTheDocument();
    const exact = new File(['x'], 'exact.wav', { type: 'audio/wav' });
    Object.defineProperty(exact, 'size', { value: 104857600 });
    fireEvent.change(audioInput, { target: { files: [exact] } });
    expect(await screen.findByDisplayValue('exact')).toBeInTheDocument();
    expect(mocks.createTrack).not.toHaveBeenCalled();
  });

  it('separates upload acceptance from conversion readiness and blocks duplicate submits', async () => {
    let resolve!: (value: unknown) => void;
    mocks.createTrack.mockReturnValue(
      new Promise((done) => {
        resolve = done;
      }),
    );
    render(
      <MemoryRouter>
        <TrackUploadPage />
      </MemoryRouter>,
    );
    fireEvent.change(screen.getByLabelText('오디오 파일 선택', { selector: 'input' }), {
      target: { files: [new File(['x'], 'queued.wav', { type: 'audio/wav' })] },
    });
    fireEvent.change(await screen.findByLabelText('BPM'), { target: { value: '120' } });
    fireEvent.change(screen.getByRole('combobox'), { target: { value: 'C' } });
    const form = screen.getByRole('button', { name: '업로드' }).closest('form')!;
    fireEvent.submit(form);
    fireEvent.submit(form);
    expect(screen.getByRole('button', { name: 'queued 제거' })).toBeDisabled();
    expect(mocks.createTrack).toHaveBeenCalledTimes(1);
    await act(async () =>
      resolve({
        id: 21,
        audioProcessing: {
          trackId: 21,
          state: 'PENDING',
          generation: 1,
          streamReady: false,
          retryAllowed: false,
          attemptCount: 0,
          errorCode: null,
          updatedAt: null,
        },
      }),
    );
    expect(screen.getByText('업로드 접수 완료')).toBeInTheDocument();
    expect(screen.getByText('변환 대기')).toBeInTheDocument();
    expect(screen.queryByText('재생 준비 완료')).not.toBeInTheDocument();
    expect(screen.getByRole('button', { name: '업로드' })).toBeDisabled();
    expect(screen.getByRole('button', { name: '음원 관리로 이동' })).toBeEnabled();
    fireEvent.submit(form);
    expect(mocks.createTrack).toHaveBeenCalledTimes(1);
  });

  it('blocks the multi-row submit for pending or invalid covers and submits a valid square file', async () => {
    const view = render(
      <MemoryRouter>
        <TrackUploadPage />
      </MemoryRouter>,
    );
    const audioInput = view.container.querySelector('input[type="file"][multiple]');
    const audioFile = new File(['audio'], 'launch.mp3', { type: 'audio/mpeg' });
    fireEvent.change(audioInput!, { target: { files: [audioFile] } });

    expect(await screen.findByDisplayValue('launch')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /^1 launch/ })).toHaveAttribute(
      'aria-expanded',
      'true',
    );
    expect(screen.getByLabelText('제목')).toHaveValue('launch');
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

  it('accepts only MP3 and WAV and clears a rejected selection for same-file retry', async () => {
    const view = render(
      <MemoryRouter>
        <TrackUploadPage />
      </MemoryRouter>,
    );
    const audioInput = view.container.querySelector<HTMLInputElement>(
      'input[type="file"][multiple]',
    );
    expect(audioInput).not.toBeNull();
    expect(audioInput).toHaveAttribute('accept', '.mp3,.wav,audio/mpeg,audio/wav,audio/x-wav');

    const rejected = new File(['audio'], 'unsupported.m4a', { type: 'audio/mp4' });
    fireEvent.change(audioInput!, { target: { files: [rejected] } });
    expect(await screen.findByText(/MP3, WAV만 업로드 가능/)).toBeInTheDocument();
    expect(audioInput).toHaveValue('');
    expect(screen.queryByDisplayValue('unsupported')).not.toBeInTheDocument();

    const accepted = new File(['audio'], 'accepted.wav', { type: 'audio/wav' });
    fireEvent.change(audioInput!, { target: { files: [accepted] } });
    expect(await screen.findByDisplayValue('accepted')).toBeInTheDocument();
  });

  it('keeps tag failure recoverable without dispatching an upload', async () => {
    mocks.fetchTags
      .mockRejectedValueOnce(new Error('tag service unavailable'))
      .mockResolvedValue([]);
    render(
      <MemoryRouter>
        <TrackUploadPage />
      </MemoryRouter>,
    );

    expect(await screen.findByRole('alert')).toHaveTextContent('태그를 불러오지 못했습니다.');
    fireEvent.click(screen.getByRole('button', { name: '다시 시도' }));
    await waitFor(() => expect(mocks.fetchTags).toHaveBeenCalledTimes(8));
    expect(mocks.createTrack).not.toHaveBeenCalled();
  });

  it('omits the native audio hint on iOS while still rejecting and resetting M4A', async () => {
    Object.defineProperty(navigator, 'userAgent', { configurable: true, value: 'iPhone' });
    const view = render(
      <MemoryRouter>
        <TrackUploadPage />
      </MemoryRouter>,
    );
    const audioInput = view.container.querySelector<HTMLInputElement>(
      'input[type="file"][multiple]',
    );
    expect(audioInput).not.toBeNull();
    expect(audioInput).not.toHaveAttribute('accept');

    const rejected = new File(['audio'], 'unsupported.m4a', { type: 'audio/mp4' });
    fireEvent.change(audioInput!, { target: { files: [rejected] } });

    expect(await screen.findByText(/MP3, WAV만 업로드 가능/)).toBeInTheDocument();
    expect(audioInput).toHaveValue('');
    expect(screen.queryByDisplayValue('unsupported')).not.toBeInTheDocument();
  });
});
