export function getFiniteMediaDuration(duration: number, fallback = 0): number {
  if (Number.isFinite(duration) && duration > 0) return duration;
  return Number.isFinite(fallback) && fallback > 0 ? fallback : 0;
}

export function clampPlaybackTime(time: number, duration: number): number {
  if (!Number.isFinite(time)) return 0;

  const nonNegativeTime = Math.max(0, time);
  const finiteDuration = getFiniteMediaDuration(duration);
  return finiteDuration > 0 ? Math.min(nonNegativeTime, finiteDuration) : nonNegativeTime;
}
