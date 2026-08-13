import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { AdminTrackDetail } from '@/api/tracks';
import TrackEditPage from './TrackEditPage';

const mocks = vi.hoisted(() => ({
  fetchTrackDetailForAdmin: vi.fn(),
  updateTrack: vi.fn(),
  fetchTags: vi.fn(),
}));

vi.mock('@/api/tracks', () => ({
  fetchTrackDetailForAdmin: (...args: unknown[]) => mocks.fetchTrackDetailForAdmin(...args),
  updateTrack: (...args: unknown[]) => mocks.updateTrack(...args),
}));

vi.mock('@/api/tags', () => ({
  fetchTags: (...args: unknown[]) => mocks.fetchTags(...args),
}));

function track(thumbnail: string | null, tags: AdminTrackDetail['tags'] = []): AdminTrackDetail {
  return {
    id: 21,
    title: 'Fresh Track',
    artistName: 'Creator',
    duration: 120,
    bpm: 110,
    tonality: 'C',
    description: 'Description',
    audioFile: 'tracks/audio/fresh.mp3',
    thumbnail,
    waveformData: '[0.100]',
    isActive: true,
    playCount: 1,
    likeCount: 2,
    downloadCount: 3,
    tags,
    createdAt: '2026-08-08T00:00:00',
    updatedAt: '2026-08-08T00:00:00',
  };
}

function renderPage(initialEntry = '/admin/tracks/21/edit') {
  return render(
    <MemoryRouter
      initialEntries={[initialEntry]}
      future={{ v7_startTransition: true, v7_relativeSplatPath: true }}
    >
      <Routes>
        <Route path="/admin/tracks/:trackId/edit" element={<TrackEditPage />} />
        <Route path="/admin/track-manage" element={<div>Track management</div>} />
      </Routes>
    </MemoryRouter>,
  );
}

function loadWithDimensions(image: HTMLElement, width: number, height: number) {
  Object.defineProperty(image, 'naturalWidth', { configurable: true, value: width });
  Object.defineProperty(image, 'naturalHeight', { configurable: true, value: height });
  fireEvent.load(image);
}

describe('TrackEditPage thumbnail contract', () => {
  beforeEach(() => {
    mocks.fetchTrackDetailForAdmin.mockReset();
    mocks.updateTrack.mockReset();
    mocks.fetchTags.mockReset().mockResolvedValue([]);
    Object.defineProperty(URL, 'createObjectURL', {
      configurable: true,
      value: vi.fn().mockReturnValueOnce('blob:wide').mockReturnValueOnce('blob:square'),
    });
    Object.defineProperty(URL, 'revokeObjectURL', {
      configurable: true,
      value: vi.fn(),
    });
  });

  it('renders and warns for an existing non-square cover without replacing it on save', async () => {
    const existing = track('tracks/thumbnail/legacy-wide.jpg');
    mocks.fetchTrackDetailForAdmin.mockResolvedValue(existing);
    mocks.updateTrack.mockResolvedValue(existing);
    renderPage();

    const image = await screen.findByAltText('현재 트랙 썸네일');
    expect(image).toHaveAttribute('src', '/uploads/tracks/thumbnail/legacy-wide.jpg');
    expect(screen.getByText('JPEG 또는 PNG, 1:1 필수, 10MB 이하')).toBeInTheDocument();
    expect(screen.getByText('2048x2048px 권장 (필수 아님)')).toBeInTheDocument();
    expect(screen.getByLabelText('썸네일 이미지')).toHaveAttribute(
      'accept',
      'image/jpeg,image/png',
    );

    loadWithDimensions(image, 564, 1404);
    expect(
      screen.getByText('현재 썸네일이 1:1이 아닙니다. 새 정사각형 이미지로 교체를 권장합니다.'),
    ).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '저장' })).toBeEnabled();

    fireEvent.click(screen.getByRole('button', { name: '저장' }));
    await waitFor(() => expect(mocks.updateTrack).toHaveBeenCalledTimes(1));
    const formData = mocks.updateTrack.mock.calls[0][1] as FormData;
    expect(formData.has('thumbnail')).toBe(false);
  });

  it('does not label an existing cover as non-square when it fails to load', async () => {
    mocks.fetchTrackDetailForAdmin.mockResolvedValue(track('tracks/thumbnail/unreadable.jpg'));
    renderPage();

    const image = await screen.findByAltText('현재 트랙 썸네일');
    fireEvent.error(image);

    expect(screen.queryByText(/새 정사각형 이미지로 교체를 권장/)).not.toBeInTheDocument();
    expect(screen.getByRole('button', { name: '저장' })).toBeEnabled();
  });

  it('blocks replacement submission while pending or invalid and enables an exact square cover', async () => {
    const existing = track(null);
    mocks.fetchTrackDetailForAdmin.mockResolvedValue(existing);
    mocks.updateTrack.mockResolvedValue(existing);
    renderPage();

    const thumbnailInput = await screen.findByLabelText('썸네일 이미지');
    const wideFile = new File(['wide'], 'wide.png', { type: 'image/png' });
    fireEvent.change(thumbnailInput, { target: { files: [wideFile] } });
    expect(screen.getByRole('button', { name: '저장' })).toBeDisabled();

    const widePreview = await screen.findByAltText('선택한 트랙 썸네일 미리보기');
    loadWithDimensions(widePreview, 900, 600);
    expect(
      screen.getByText('트랙 썸네일은 가로와 세로 길이가 같은 1:1 이미지여야 합니다.'),
    ).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '저장' })).toBeDisabled();

    const squareFile = new File(['square'], 'square.jpg', { type: 'image/jpeg' });
    fireEvent.change(thumbnailInput, { target: { files: [squareFile] } });
    expect(screen.getByRole('button', { name: '저장' })).toBeDisabled();
    const squarePreview = await screen.findByAltText('선택한 트랙 썸네일 미리보기');
    await waitFor(() => expect(squarePreview).toHaveAttribute('src', 'blob:square'));
    loadWithDimensions(squarePreview, 2048, 2048);
    expect(screen.getByRole('button', { name: '저장' })).toBeEnabled();

    fireEvent.click(screen.getByRole('button', { name: '저장' }));
    await waitFor(() => expect(mocks.updateTrack).toHaveBeenCalledTimes(1));
    const formData = mocks.updateTrack.mock.calls[0][1] as FormData;
    expect(formData.get('thumbnail')).toBe(squareFile);
  });

  it('rejects a malformed route ID without making Track or Tag API calls', async () => {
    renderPage('/admin/tracks/not-a-number/edit');

    expect(await screen.findByRole('alert')).toHaveTextContent('올바른 음원 주소가 아닙니다.');
    expect(screen.getByRole('button', { name: '음원 관리로 이동' })).toBeInTheDocument();
    expect(mocks.fetchTrackDetailForAdmin).not.toHaveBeenCalled();
    expect(mocks.fetchTags).not.toHaveBeenCalled();
    expect(mocks.updateTrack).not.toHaveBeenCalled();
  });

  it('sends explicit empty Tag replacement intent and keeps description clearable', async () => {
    const existingTag = { id: 31, name: 'Rock', type: 'GENRE' as const };
    const existing = track(null, [existingTag]);
    mocks.fetchTrackDetailForAdmin.mockResolvedValue(existing);
    mocks.fetchTags.mockImplementation((type: string) =>
      Promise.resolve(type === 'GENRE' ? [existingTag] : []),
    );
    mocks.updateTrack.mockResolvedValue(existing);
    renderPage();

    fireEvent.click(await screen.findByRole('button', { name: 'Rock' }));
    fireEvent.change(screen.getByDisplayValue('Description'), { target: { value: '   ' } });
    fireEvent.click(screen.getByRole('button', { name: '저장' }));

    await waitFor(() => expect(mocks.updateTrack).toHaveBeenCalledWith(21, expect.any(FormData)));
    const formData = mocks.updateTrack.mock.calls[0][1] as FormData;
    expect(formData.get('replaceTags')).toBe('true');
    expect(formData.getAll('tagIds')).toEqual([]);
    expect(formData.get('description')).toBe('');
  });

  it('validates nonblank title, integer in-range BPM, and nonblank tonality before construction', async () => {
    const existing = track(null);
    mocks.fetchTrackDetailForAdmin.mockResolvedValue(existing);
    mocks.updateTrack.mockResolvedValue(existing);
    renderPage();

    const titleInput = await screen.findByDisplayValue('Fresh Track');
    const bpmInput = screen.getByDisplayValue('110');
    const tonalityInput = screen.getByRole('combobox');
    const saveButton = screen.getByRole('button', { name: '저장' });
    const form = saveButton.closest('form');
    expect(form).not.toBeNull();

    fireEvent.change(titleInput, { target: { value: '   ' } });
    fireEvent.submit(form!);
    expect(screen.getByText('제목을 입력해 주세요.')).toBeInTheDocument();
    expect(mocks.updateTrack).not.toHaveBeenCalled();

    fireEvent.change(titleInput, { target: { value: 'Fresh Track' } });
    fireEvent.change(bpmInput, { target: { value: '1000' } });
    fireEvent.submit(form!);
    expect(screen.getByText('BPM은 1부터 999 사이의 정수여야 합니다.')).toBeInTheDocument();
    expect(mocks.updateTrack).not.toHaveBeenCalled();

    fireEvent.change(bpmInput, { target: { value: '120' } });
    fireEvent.change(tonalityInput, { target: { value: '' } });
    fireEvent.submit(form!);
    expect(screen.getByText('조성을 선택해 주세요.')).toBeInTheDocument();
    expect(mocks.updateTrack).not.toHaveBeenCalled();
  });

  it('accepts a WAV replacement, rejects M4A, and clears rejected file selection', async () => {
    mocks.fetchTrackDetailForAdmin.mockResolvedValue(track(null));
    renderPage();

    await screen.findByDisplayValue('Fresh Track');
    const audioInput = document.querySelector<HTMLInputElement>('input[type="file"]');
    expect(audioInput).not.toBeNull();
    expect(audioInput).toHaveAttribute('accept', '.mp3,.wav,audio/mpeg,audio/wav,audio/x-wav');

    const rejected = new File(['audio'], 'replacement.m4a', { type: 'audio/mp4' });
    fireEvent.change(audioInput!, { target: { files: [rejected] } });
    expect(screen.getByText(/MP3, WAV만 업로드할 수 있습니다\.$/)).toBeInTheDocument();
    expect(audioInput).toHaveValue('');

    const accepted = new File(['audio'], 'replacement.wav', { type: 'audio/wav' });
    fireEvent.change(audioInput!, { target: { files: [accepted] } });
    expect(screen.queryByText(/MP3, WAV만 업로드할 수 있습니다\.$/)).not.toBeInTheDocument();
    expect(screen.getByText('replacement.wav')).toBeInTheDocument();
  });
});
