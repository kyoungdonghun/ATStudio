import { useRef, useEffect, useCallback } from 'react';
import styles from './WaveformCanvas.module.css';

interface Props {
  peaks: number[]; // 200 floats 0.0–1.0
  progress: number; // 0–1
  onSeek: (ratio: number) => void;
  height?: number; // px, default 48
}

export default function WaveformCanvas({ peaks, progress, onSeek, height = 48 }: Props) {
  const canvasRef = useRef<HTMLCanvasElement>(null);

  const draw = useCallback(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const computed = getComputedStyle(canvas);
    const accent = computed.getPropertyValue('--accent').trim() || '#c6a75e';
    const dim = computed.getPropertyValue('--border2').trim() || 'rgba(150,120,60,0.3)';

    const dpr = window.devicePixelRatio || 1;
    const w = canvas.offsetWidth;
    const h = canvas.offsetHeight;
    if (w === 0 || h === 0) return;

    canvas.width = w * dpr;
    canvas.height = h * dpr;
    ctx.scale(dpr, dpr);
    ctx.clearRect(0, 0, w, h);

    if (peaks.length === 0) {
      // flat line fallback (no waveform data)
      ctx.strokeStyle = dim;
      ctx.lineWidth = 1;
      ctx.beginPath();
      ctx.moveTo(0, h / 2);
      ctx.lineTo(w, h / 2);
      ctx.stroke();
      return;
    }

    const count = peaks.length;
    const barW = w / count;
    const gap = Math.max(0.5, barW * 0.2);
    const barActualW = Math.max(1, barW - gap);
    const progressX = progress * w;

    for (let i = 0; i < count; i++) {
      const x = i * barW + gap / 2;
      const barH = Math.max(2, peaks[i] * h * 0.85);
      const y = (h - barH) / 2;

      ctx.fillStyle = x < progressX ? accent : dim;
      ctx.beginPath();
      if (barActualW >= 2) {
        ctx.roundRect(x, y, barActualW, barH, Math.min(2, barActualW / 2));
      } else {
        ctx.rect(x, y, barActualW, barH);
      }
      ctx.fill();
    }

    // play head — thin vertical line at current position
    if (progress > 0 && progress < 1) {
      ctx.strokeStyle = accent;
      ctx.lineWidth = 1.5;
      ctx.beginPath();
      ctx.moveTo(progressX, 2);
      ctx.lineTo(progressX, h - 2);
      ctx.stroke();
    }
  }, [peaks, progress]);

  useEffect(() => {
    draw();
  }, [draw]);

  // redraw on container resize (e.g. window resize)
  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ro = new ResizeObserver(() => draw());
    ro.observe(canvas);
    return () => ro.disconnect();
  }, [draw]);

  const handleClick = useCallback(
    (e: React.MouseEvent<HTMLCanvasElement>) => {
      const canvas = canvasRef.current;
      if (!canvas) return;
      const rect = canvas.getBoundingClientRect();
      const ratio = Math.max(0, Math.min(1, (e.clientX - rect.left) / rect.width));
      onSeek(ratio);
    },
    [onSeek],
  );

  return (
    <canvas
      ref={canvasRef}
      className={styles.canvas}
      style={{ height: `${height}px` }}
      onClick={handleClick}
      aria-label="Waveform — click to seek"
    />
  );
}
