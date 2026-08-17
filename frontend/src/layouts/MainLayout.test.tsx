import { fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import MainLayout from '@/layouts/MainLayout';

const player = vi.hoisted(() => ({
  getState: vi.fn(),
  next: vi.fn(),
  pause: vi.fn(),
  play: vi.fn(),
  prev: vi.fn(),
  resume: vi.fn(),
}));

vi.mock('@/store/playerStore', () => ({
  usePlayerStore: { getState: player.getState },
}));

vi.mock('@/layouts/Header', () => ({ default: () => null }));
vi.mock('@/layouts/PlayerBar', () => ({ default: () => null }));
vi.mock('@/components/ui/ToastContainer', () => ({ default: () => null }));

function renderMainLayout() {
  render(
    <MemoryRouter>
      <Routes>
        <Route element={<MainLayout />}>
          <Route
            index
            element={
              <>
                <div data-testid="ordinary-target">일반 콘텐츠</div>
                <button data-testid="button-target">버튼</button>
                <a href="/tracks" data-testid="link-target">
                  링크
                </a>
                <input data-testid="input-target" />
                <textarea data-testid="textarea-target" />
                <select data-testid="select-target" aria-label="선택">
                  <option>항목</option>
                </select>
                <div data-testid="editable-target" contentEditable suppressContentEditableWarning>
                  편집 영역
                </div>
                <div role="slider" tabIndex={0} data-testid="control-role-target" />
                <div role="menu">
                  <span data-testid="composite-role-target">메뉴 항목</span>
                </div>
                <div role="dialog">
                  <span data-testid="dialog-target">대화상자 내용</span>
                </div>
                <div tabIndex={0} data-testid="tabbable-target">
                  사용자 정의 컨트롤
                </div>
              </>
            }
          />
        </Route>
      </Routes>
    </MemoryRouter>,
  );
}

describe('MainLayout playback shortcut safety', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    player.getState.mockReturnValue({
      currentTrack: { id: 1 },
      isPlaying: true,
      next: player.next,
      pause: player.pause,
      play: player.play,
      prev: player.prev,
      resume: player.resume,
      trackListContext: [{ id: 1 }, { id: 2 }],
    });
  });

  it.each([
    'button-target',
    'link-target',
    'input-target',
    'textarea-target',
    'select-target',
    'editable-target',
    'control-role-target',
    'composite-role-target',
    'dialog-target',
    'tabbable-target',
  ])('ignores playback keys from %s', (targetId) => {
    renderMainLayout();

    fireEvent.keyDown(screen.getByTestId(targetId), { key: ' ' });
    fireEvent.keyDown(screen.getByTestId(targetId), { key: 'ArrowDown' });
    fireEvent.keyDown(screen.getByTestId(targetId), { key: 'ArrowUp' });

    expect(player.getState).not.toHaveBeenCalled();
  });

  it('ignores default-prevented and modified events from ordinary content', () => {
    renderMainLayout();
    const target = screen.getByTestId('ordinary-target');
    const preventedEvent = new KeyboardEvent('keydown', {
      bubbles: true,
      cancelable: true,
      key: ' ',
    });
    preventedEvent.preventDefault();

    target.dispatchEvent(preventedEvent);
    fireEvent.keyDown(target, { key: ' ', altKey: true });
    fireEvent.keyDown(target, { key: ' ', ctrlKey: true });
    fireEvent.keyDown(target, { key: 'ArrowDown', metaKey: true });
    fireEvent.keyDown(target, { key: 'ArrowUp', shiftKey: true });

    expect(player.getState).not.toHaveBeenCalled();
  });

  it('preserves play, pause, and list navigation from ordinary content', () => {
    renderMainLayout();
    const target = screen.getByTestId('ordinary-target');

    fireEvent.keyDown(target, { key: ' ' });
    fireEvent.keyDown(target, { key: 'ArrowDown' });
    fireEvent.keyDown(target, { key: 'ArrowUp' });

    expect(player.pause).toHaveBeenCalledTimes(1);
    expect(player.next).toHaveBeenCalledTimes(1);
    expect(player.prev).toHaveBeenCalledTimes(1);
    expect(player.resume).not.toHaveBeenCalled();
  });

  it('preserves ArrowDown starting the first track when none is current', () => {
    const firstTrack = { id: 1 };
    player.getState.mockReturnValue({
      currentTrack: null,
      isPlaying: false,
      next: player.next,
      pause: player.pause,
      play: player.play,
      prev: player.prev,
      resume: player.resume,
      trackListContext: [firstTrack],
    });
    renderMainLayout();

    fireEvent.keyDown(screen.getByTestId('ordinary-target'), { key: 'ArrowDown' });

    expect(player.play).toHaveBeenCalledWith(firstTrack);
    expect(player.next).not.toHaveBeenCalled();
  });
});
