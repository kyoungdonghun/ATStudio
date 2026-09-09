import { beforeEach, describe, expect, it, vi } from 'vitest';
import { fetchAudioProcessing, retryAudioProcessing } from './tracks';

const mocks = vi.hoisted(() => ({ get: vi.fn(), post: vi.fn() }));
vi.mock('@/api/client', () => ({ default: mocks }));

describe('admin audio processing API', () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it('unwraps GET ResponseDTO.data and passes cancellation', async () => {
    const result = { trackId: 5, state: 'PENDING' };
    mocks.get.mockResolvedValue({ data: { data: result } });
    const controller = new AbortController();
    expect(await fetchAudioProcessing(5, controller.signal)).toBe(result);
    expect(mocks.get).toHaveBeenCalledWith('/tracks/admin/5/audio-processing', {
      signal: controller.signal,
    });
  });

  it('sends only the observed generation and never auth-replays the retry mutation', async () => {
    const result = { trackId: 5, state: 'PENDING', generation: 7 };
    mocks.post.mockResolvedValue({ status: 202, data: { data: result } });
    const controller = new AbortController();
    expect(await retryAudioProcessing(5, 6, controller.signal)).toBe(result);
    expect(mocks.post).toHaveBeenCalledWith(
      '/tracks/admin/5/audio-processing/retry',
      { generation: 6 },
      { signal: controller.signal, skipAuthReplay: true },
    );
  });
});
