import '@testing-library/jest-dom/vitest';
import { cleanup } from '@testing-library/react';
import { afterEach, vi } from 'vitest';

class MockAudio {
  src = '';
  currentTime = 0;
  duration = 0;
  volume = 1;
  muted = false;

  play() {
    return Promise.resolve();
  }

  pause() {}

  addEventListener() {}

  removeEventListener() {}
}

vi.stubGlobal('Audio', MockAudio);
vi.stubGlobal('scrollTo', vi.fn());

afterEach(() => {
  cleanup();
  localStorage.clear();
  sessionStorage.clear();
});
