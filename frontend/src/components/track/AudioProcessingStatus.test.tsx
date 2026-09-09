import { act, cleanup, fireEvent, render, screen } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import type { AudioProcessing } from '@/api/tracks';
import AudioProcessingStatus from './AudioProcessingStatus';

const mocks = vi.hoisted(() => ({ fetch: vi.fn(), retry: vi.fn() }));
vi.mock('@/api/tracks', () => ({
  fetchAudioProcessing: (...args: unknown[]) => mocks.fetch(...args),
  retryAudioProcessing: (...args: unknown[]) => mocks.retry(...args),
}));

const pending: AudioProcessing = {
  trackId: 21,
  state: 'PENDING',
  generation: 2,
  streamReady: false,
  retryAllowed: false,
  attemptCount: 0,
  errorCode: null,
  updatedAt: null,
};

function deferred<T>() {
  let resolve!: (value: T) => void;
  const promise = new Promise<T>((done) => {
    resolve = done;
  });
  return { promise, resolve };
}

describe('AudioProcessingStatus lifecycle', () => {
  beforeEach(() => {
    vi.useFakeTimers();
    mocks.fetch.mockReset().mockResolvedValue(pending);
    mocks.retry.mockReset();
    Object.defineProperty(document, 'hidden', { configurable: true, value: false });
  });

  afterEach(() => {
    cleanup();
    vi.useRealTimers();
    vi.restoreAllMocks();
    Object.defineProperty(document, 'hidden', { configurable: true, value: false });
  });

  it('does not request or invent a failure for null and historical API projections', async () => {
    const view = render(<AudioProcessingStatus />);
    expect(view.container).toBeEmptyDOMElement();
    view.rerender(<AudioProcessingStatus value={null} />);
    await act(() => vi.advanceTimersByTimeAsync(10000));
    expect(mocks.fetch).not.toHaveBeenCalled();
    expect(mocks.retry).not.toHaveBeenCalled();
  });

  it('polls processing only and stops at READY without publishing', async () => {
    const onChange = vi.fn();
    mocks.fetch
      .mockResolvedValueOnce({ ...pending, state: 'PROCESSING' })
      .mockResolvedValueOnce({ ...pending, state: 'READY', streamReady: true });
    render(<AudioProcessingStatus value={pending} onChange={onChange} />);
    expect(screen.getByRole('status')).toHaveTextContent('변환 대기');
    expect(mocks.fetch).not.toHaveBeenCalled();
    await act(() => vi.advanceTimersByTimeAsync(3000));
    expect(screen.getByRole('status')).toHaveTextContent('변환 중');
    await act(() => vi.advanceTimersByTimeAsync(3000));
    expect(screen.getByRole('status')).toHaveTextContent('재생 준비 완료');
    await act(() => vi.advanceTimersByTimeAsync(30000));
    expect(mocks.fetch).toHaveBeenCalledTimes(2);
    expect(onChange).toHaveBeenLastCalledWith(expect.objectContaining({ streamReady: true }));
    expect(mocks.retry).not.toHaveBeenCalled();
  });

  it('keeps replacement playback visible and retries only once with the observed generation', async () => {
    const failure = {
      ...pending,
      state: 'FAILED' as const,
      streamReady: true,
      retryAllowed: true,
      errorCode: 'private-path',
    };
    const retry = deferred<AudioProcessing>();
    mocks.retry.mockReturnValue(retry.promise);
    render(<AudioProcessingStatus value={failure} />);
    expect(screen.getByText('기존 재생 파일 유지')).toBeInTheDocument();
    expect(screen.queryByText('private-path')).not.toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '음원 변환 재시도' }));
    fireEvent.click(screen.getByRole('button', { name: '음원 변환 재시도' }));
    expect(mocks.retry).toHaveBeenCalledTimes(1);
    expect(mocks.retry).toHaveBeenCalledWith(21, 2, expect.any(AbortSignal));
    await act(async () => retry.resolve({ ...pending, generation: 3, streamReady: true }));
    expect(screen.getByRole('status')).toHaveTextContent('변환 대기');
    expect(screen.queryByRole('button', { name: '음원 변환 재시도' })).not.toBeInTheDocument();
  });

  it.each([
    ['FFMPEG_NOT_FOUND', '운영 담당자에게 설치 및 실행 설정 확인을 요청해 주세요.'],
    ['AUDIO_TRANSCODE_TIMEOUT', '서버 부하를 확인한 뒤 다시 시도해 주세요.'],
    ['AUDIO_TRANSCODE_FAILED', '원본 WAV 파일이 정상 재생되는지 확인한 뒤 다시 시도해 주세요.'],
    ['AUDIO_OUTPUT_INVALID', '다시 시도하고, 반복되면 운영 담당자에게 문의해 주세요.'],
    ['AUDIO_OUTPUT_TOO_LARGE', '원본 길이와 샘플레이트를 확인해 주세요.'],
    [
      'AUDIO_INPUT_UNAVAILABLE',
      '운영 담당자에게 원본 파일과 저장소 접근 상태 확인을 요청해 주세요.',
    ],
    ['AUDIO_STORAGE_FAILED', '운영 담당자에게 저장 공간과 접근 권한 확인을 요청해 주세요.'],
    ['AUDIO_PROCESSING_INTERRUPTED', '서버 상태를 확인한 뒤 다시 시도해 주세요.'],
    ['AUDIO_FORMAT_UNSUPPORTED', 'WAV 파일 형식을 확인해 주세요.'],
    ['AUDIO_PROCESS_TERMINATION_FAILED', '운영 담당자에게 프로세스 상태 확인을 요청해 주세요.'],
  ])('shows safe actionable guidance for the known failure %s', (errorCode, action) => {
    render(<AudioProcessingStatus value={{ ...pending, state: 'FAILED', errorCode }} />);
    expect(screen.getByText(action, { exact: false })).toBeInTheDocument();
    expect(screen.queryByText(errorCode, { exact: false })).not.toBeInTheDocument();
  });

  it.each([null, 'C:\\private\\audio.wav: native stderr', '__proto__', 'constructor'])(
    'uses generic guidance without exposing unknown failure data %s',
    (errorCode) => {
      render(<AudioProcessingStatus value={{ ...pending, state: 'FAILED', errorCode }} />);
      expect(
        screen.getByText('음원 변환을 완료하지 못했습니다. 원본 파일과 서버 상태를 확인해 주세요.'),
      ).toBeInTheDocument();
      if (errorCode)
        expect(screen.queryByText(errorCode, { exact: false })).not.toBeInTheDocument();
    },
  );

  it('shows a polled failure reason and removes it once a retry is accepted', async () => {
    mocks.fetch.mockResolvedValue({
      ...pending,
      state: 'FAILED',
      retryAllowed: true,
      errorCode: 'AUDIO_TRANSCODE_TIMEOUT',
    });
    mocks.retry.mockResolvedValue({ ...pending, generation: 3 });
    render(<AudioProcessingStatus value={pending} />);
    await act(() => vi.advanceTimersByTimeAsync(3000));
    expect(screen.getByText(/음원 변환 제한 시간을 초과했습니다/)).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '음원 변환 재시도' }));
    await act(async () => undefined);
    expect(screen.queryByText(/음원 변환 제한 시간을 초과했습니다/)).not.toBeInTheDocument();
    expect(screen.getByRole('status')).toHaveTextContent('변환 대기');
  });

  it('does not show a stale failure reason for a non-failed state', () => {
    render(<AudioProcessingStatus value={{ ...pending, errorCode: 'AUDIO_TRANSCODE_TIMEOUT' }} />);
    expect(screen.queryByText(/음원 변환 제한 시간을 초과했습니다/)).not.toBeInTheDocument();
  });

  it.each(['AUDIO_PROCESSING_CONFLICT', 'NETWORK'])(
    'requires a read before retrying an uncertain %s failure',
    async (errorCode) => {
      const failure = { ...pending, state: 'FAILED' as const, retryAllowed: true };
      mocks.retry.mockRejectedValue({
        response: { data: { errorCode, message: 'private diagnostic' } },
      });
      mocks.fetch.mockResolvedValue({ ...failure, generation: 3 });
      render(<AudioProcessingStatus value={failure} />);
      fireEvent.click(screen.getByRole('button', { name: '음원 변환 재시도' }));
      await act(async () => undefined);
      expect(screen.getByRole('alert')).not.toHaveTextContent('private diagnostic');
      expect(screen.queryByRole('button', { name: '음원 변환 재시도' })).not.toBeInTheDocument();
      await act(() => vi.advanceTimersByTimeAsync(30000));
      expect(mocks.fetch).not.toHaveBeenCalled();
      fireEvent.click(screen.getByRole('button', { name: '음원 처리 상태 새로고침' }));
      await act(async () => undefined);
      fireEvent.click(screen.getByRole('button', { name: '음원 변환 재시도' }));
      expect(mocks.retry).toHaveBeenLastCalledWith(21, 3, expect.any(AbortSignal));
      await act(async () => undefined);
    },
  );

  it('aborts stale row requests, ignores late results and cleans up its listener on unmount', async () => {
    const stale = deferred<AudioProcessing>();
    mocks.fetch.mockReturnValueOnce(stale.promise);
    const onChange = vi.fn();
    const removeListener = vi.spyOn(document, 'removeEventListener');
    const view = render(<AudioProcessingStatus value={pending} onChange={onChange} />);
    await act(() => vi.advanceTimersByTimeAsync(3000));
    const signal = mocks.fetch.mock.calls[0][1] as AbortSignal;
    view.rerender(
      <AudioProcessingStatus value={{ ...pending, trackId: 22 }} onChange={onChange} />,
    );
    expect(signal.aborted).toBe(true);
    await act(async () => stale.resolve({ ...pending, state: 'READY', streamReady: true }));
    expect(onChange).not.toHaveBeenCalled();
    expect(screen.getByRole('status')).toHaveTextContent('변환 대기');
    view.unmount();
    await act(() => vi.advanceTimersByTimeAsync(30000));
    expect(mocks.fetch).toHaveBeenCalledTimes(1);
    expect(removeListener).toHaveBeenCalledWith('visibilitychange', expect.any(Function));
  });

  it('rejects a response from an older backend generation and stops polling on read error', async () => {
    mocks.fetch.mockResolvedValue({ ...pending, generation: 1, state: 'READY', streamReady: true });
    render(<AudioProcessingStatus value={pending} />);
    await act(() => vi.advanceTimersByTimeAsync(3000));
    expect(screen.getByRole('status')).toHaveTextContent('변환 대기');
    expect(screen.getByRole('alert')).toHaveTextContent('처리 상태를 불러오지 못했습니다.');
    await act(() => vi.advanceTimersByTimeAsync(30000));
    expect(mocks.fetch).toHaveBeenCalledTimes(1);
  });

  it('pauses hidden tabs, aborts in-flight reads, and resumes only one timer', async () => {
    const stale = deferred<AudioProcessing>();
    mocks.fetch.mockReturnValueOnce(stale.promise);
    render(<AudioProcessingStatus value={pending} />);
    await act(() => vi.advanceTimersByTimeAsync(3000));
    const signal = mocks.fetch.mock.calls[0][1] as AbortSignal;
    Object.defineProperty(document, 'hidden', { configurable: true, value: true });
    fireEvent(document, new Event('visibilitychange'));
    expect(signal.aborted).toBe(true);
    await act(() => vi.advanceTimersByTimeAsync(30000));
    expect(mocks.fetch).toHaveBeenCalledTimes(1);
    Object.defineProperty(document, 'hidden', { configurable: true, value: false });
    fireEvent(document, new Event('visibilitychange'));
    fireEvent(document, new Event('visibilitychange'));
    await act(() => vi.advanceTimersByTimeAsync(3000));
    expect(mocks.fetch).toHaveBeenCalledTimes(2);
    await act(async () => stale.resolve({ ...pending, state: 'READY', streamReady: true }));
    expect(screen.getByRole('status')).toHaveTextContent('변환 대기');
  });

  it('bounds automatic refresh at 60 reads and offers a manual status check', async () => {
    render(<AudioProcessingStatus value={pending} />);
    await act(() => vi.advanceTimersByTimeAsync(3000 * 61));
    expect(mocks.fetch).toHaveBeenCalledTimes(60);
    expect(screen.getByText('자동 상태 확인 일시 중지')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '음원 처리 상태 새로고침' }));
    await act(async () => undefined);
    expect(mocks.fetch).toHaveBeenCalledTimes(61);
  });

  it('requires refresh after an interrupted retry even when disabled and re-enabled', async () => {
    const failure = { ...pending, state: 'FAILED' as const, retryAllowed: true };
    const stale = deferred<AudioProcessing>();
    mocks.retry.mockReturnValueOnce(stale.promise);
    const view = render(<AudioProcessingStatus value={failure} />);
    fireEvent.click(screen.getByRole('button', { name: '음원 변환 재시도' }));
    const signal = mocks.retry.mock.calls[0][2] as AbortSignal;
    view.rerender(<AudioProcessingStatus value={failure} disabled />);
    expect(signal.aborted).toBe(true);
    view.rerender(<AudioProcessingStatus value={failure} />);
    expect(screen.queryByRole('button', { name: '음원 변환 재시도' })).not.toBeInTheDocument();
    expect(screen.getByRole('button', { name: '음원 처리 상태 새로고침' })).toBeEnabled();
    await act(async () => stale.resolve(pending));
    expect(screen.getByRole('status')).toHaveTextContent('변환 실패');
  });
});
