import { fireEvent, render, screen } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import WaveformCanvas from '@/components/player/WaveformCanvas';

const context = {
  scale: vi.fn(),
  clearRect: vi.fn(),
  beginPath: vi.fn(),
  moveTo: vi.fn(),
  lineTo: vi.fn(),
  stroke: vi.fn(),
  rect: vi.fn(),
  roundRect: vi.fn(),
  fill: vi.fn(),
  fillStyle: '',
  strokeStyle: '',
  lineWidth: 0,
};

describe('WaveformCanvas', () => {
  beforeEach(() => {
    Object.values(context).forEach((value) => {
      if (typeof value === 'function' && 'mockClear' in value) value.mockClear();
    });
    vi.stubGlobal(
      'ResizeObserver',
      class {
        observe = vi.fn();
        disconnect = vi.fn();
      },
    );
    vi.spyOn(HTMLCanvasElement.prototype, 'getContext').mockReturnValue(
      context as unknown as CanvasRenderingContext2D,
    );
    Object.defineProperty(HTMLCanvasElement.prototype, 'offsetWidth', {
      configurable: true,
      get: () => 200,
    });
    Object.defineProperty(HTMLCanvasElement.prototype, 'offsetHeight', {
      configurable: true,
      get: () => 48,
    });
  });

  it('draws bars, progress color, and the playhead', () => {
    render(<WaveformCanvas peaks={[0.2, 0.8, 0.5]} progress={0.5} onSeek={vi.fn()} />);
    expect(context.clearRect).toHaveBeenCalledWith(0, 0, 200, 48);
    expect(context.roundRect).toHaveBeenCalledTimes(3);
    expect(context.fill).toHaveBeenCalledTimes(3);
    expect(context.moveTo).toHaveBeenCalledWith(100, 2);
    expect(context.lineTo).toHaveBeenCalledWith(100, 46);
  });

  it('draws a fallback line for missing waveform data', () => {
    render(<WaveformCanvas peaks={[]} progress={0} onSeek={vi.fn()} height={36} />);
    expect(context.moveTo).toHaveBeenCalledWith(0, 24);
    expect(context.lineTo).toHaveBeenCalledWith(200, 24);
    expect(screen.getByLabelText(/Waveform/)).toHaveStyle({ height: '36px' });
  });

  it('converts click positions into clamped seek ratios', () => {
    const onSeek = vi.fn();
    render(<WaveformCanvas peaks={[1]} progress={0} onSeek={onSeek} />);
    const canvas = screen.getByLabelText(/Waveform/);
    vi.spyOn(canvas, 'getBoundingClientRect').mockReturnValue({
      left: 50,
      right: 250,
      width: 200,
      top: 0,
      bottom: 48,
      height: 48,
      x: 50,
      y: 0,
      toJSON: () => ({}),
    });
    fireEvent.click(canvas, { clientX: 150 });
    fireEvent.click(canvas, { clientX: 20 });
    fireEvent.click(canvas, { clientX: 300 });
    expect(onSeek.mock.calls.map(([ratio]) => ratio)).toEqual([0.5, 0, 1]);
  });
});
