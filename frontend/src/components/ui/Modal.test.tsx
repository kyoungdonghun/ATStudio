import { fireEvent, render, screen, within } from '@testing-library/react';
import { useRef, useState } from 'react';
import { describe, expect, it } from 'vitest';
import Modal from '@/components/ui/Modal';

function ModalHarness() {
  const [open, setOpen] = useState(false);

  return (
    <>
      <button type="button" onClick={() => setOpen(true)}>
        모달 열기
      </button>
      <Modal open={open} onClose={() => setOpen(false)} title="테스트 모달">
        <button type="button">첫 번째 작업</button>
        <button type="button">마지막 작업</button>
      </Modal>
    </>
  );
}

function NestedModalHarness() {
  const [parentOpen, setParentOpen] = useState(false);
  const [childOpen, setChildOpen] = useState(false);

  return (
    <>
      <button type="button" onClick={() => setParentOpen(true)}>
        부모 모달 열기
      </button>
      <Modal open={parentOpen} onClose={() => setParentOpen(false)} title="부모 모달">
        <button type="button" onClick={() => setChildOpen(true)}>
          자식 모달 열기
        </button>
        <Modal open={childOpen} onClose={() => setChildOpen(false)} title="자식 모달">
          <button type="button">자식 작업</button>
        </Modal>
      </Modal>
    </>
  );
}

function BusyModalHarness() {
  const [open, setOpen] = useState(true);
  const [busy, setBusy] = useState(true);

  return (
    <>
      <button type="button" onClick={() => setBusy(false)}>
        작업 완료
      </button>
      <Modal open={open} onClose={() => setOpen(false)} title="처리 모달" busy={busy}>
        <button type="button">내부 작업</button>
      </Modal>
    </>
  );
}

type InvalidOpenerMode = 'removed' | 'disabled' | 'aria-disabled';
type InvalidNestedTargetMode = InvalidOpenerMode | 'hidden' | 'inert';

function ExplicitFallbackHarness({ mode }: { mode: InvalidOpenerMode }) {
  const [open, setOpen] = useState(false);
  const [openerInvalid, setOpenerInvalid] = useState(false);
  const fallbackRef = useRef<HTMLButtonElement>(null);

  return (
    <>
      <button type="button" ref={fallbackRef}>
        Explicit fallback
      </button>
      {(mode !== 'removed' || !openerInvalid) && (
        <button
          type="button"
          disabled={mode === 'disabled' && openerInvalid}
          aria-disabled={mode === 'aria-disabled' && openerInvalid ? 'true' : undefined}
          onClick={() => setOpen(true)}
        >
          Open fallback modal
        </button>
      )}
      <Modal
        open={open}
        onClose={() => setOpen(false)}
        title="Fallback modal"
        focusFallbackRef={fallbackRef}
      >
        <button type="button" onClick={() => setOpenerInvalid(true)}>
          Invalidate opener
        </button>
      </Modal>
    </>
  );
}

function MainFallbackHarness({ includeHeading }: { includeHeading: boolean }) {
  const [open, setOpen] = useState(false);
  const [targetsAvailable, setTargetsAvailable] = useState(true);
  const fallbackRef = useRef<HTMLButtonElement>(null);

  return (
    <main aria-label="Primary content">
      {includeHeading && <h1>Stable page heading</h1>}
      {targetsAvailable && (
        <>
          <button type="button" ref={fallbackRef}>
            Disappearing fallback
          </button>
          <button type="button" onClick={() => setOpen(true)}>
            Open main fallback modal
          </button>
        </>
      )}
      <Modal
        open={open}
        onClose={() => setOpen(false)}
        title="Main fallback modal"
        focusFallbackRef={fallbackRef}
      >
        <button type="button" onClick={() => setTargetsAvailable(false)}>
          Remove focus targets
        </button>
      </Modal>
    </main>
  );
}

function NestedInvalidTargetHarness({
  mode,
  parentBusy = false,
}: {
  mode: InvalidNestedTargetMode;
  parentBusy?: boolean;
}) {
  const [parentOpen, setParentOpen] = useState(false);
  const [childOpen, setChildOpen] = useState(false);
  const [targetsInvalid, setTargetsInvalid] = useState(false);
  const childFallbackRef = useRef<HTMLButtonElement>(null);
  const targetProps = {
    disabled: mode === 'disabled' && targetsInvalid,
    'aria-disabled': mode === 'aria-disabled' && targetsInvalid ? ('true' as const) : undefined,
    hidden: mode === 'hidden' && targetsInvalid,
    ...(mode === 'inert' && targetsInvalid ? { inert: '' } : {}),
  };

  return (
    <main>
      <h1>Page behind parent</h1>
      <button type="button" onClick={() => setParentOpen(true)}>
        Open parent
      </button>
      <Modal
        open={parentOpen}
        onClose={() => setParentOpen(false)}
        title="Surviving parent"
        busy={parentBusy}
      >
        <button type="button">Parent action</button>
        {(mode !== 'removed' || !targetsInvalid) && (
          <button type="button" ref={childFallbackRef} {...targetProps}>
            Child fallback
          </button>
        )}
        {(mode !== 'removed' || !targetsInvalid) && (
          <button type="button" onClick={() => setChildOpen(true)} {...targetProps}>
            Open child
          </button>
        )}
        <Modal
          open={childOpen}
          onClose={() => setChildOpen(false)}
          title="Closing child"
          focusFallbackRef={childFallbackRef}
        >
          <button type="button" onClick={() => setTargetsInvalid(true)}>
            Invalidate child targets
          </button>
        </Modal>
      </Modal>
    </main>
  );
}

describe('Modal focus behavior', () => {
  it('traps focus and restores it to the opener after Escape closes the modal', () => {
    render(<ModalHarness />);
    const opener = screen.getByRole('button', { name: '모달 열기' });

    opener.focus();
    fireEvent.click(opener);

    const dialog = screen.getByRole('dialog', { name: '테스트 모달' });
    expect(dialog).toHaveFocus();

    fireEvent.keyDown(document, { key: 'Tab' });
    expect(screen.getByRole('button', { name: '닫기' })).toHaveFocus();

    fireEvent.keyDown(document, { key: 'Tab', shiftKey: true });
    expect(screen.getByRole('button', { name: '마지막 작업' })).toHaveFocus();

    fireEvent.keyDown(document, { key: 'Tab' });
    expect(screen.getByRole('button', { name: '닫기' })).toHaveFocus();

    fireEvent.keyDown(document, { key: 'Escape' });
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    expect(opener).toHaveFocus();
  });

  it('restores focus through nested modal close order', () => {
    render(<NestedModalHarness />);
    const parentOpener = screen.getByRole('button', { name: '부모 모달 열기' });
    parentOpener.focus();
    fireEvent.click(parentOpener);

    const childOpener = screen.getByRole('button', { name: '자식 모달 열기' });
    childOpener.focus();
    fireEvent.click(childOpener);

    fireEvent.keyDown(document, { key: 'Escape' });
    expect(screen.queryByRole('dialog', { name: '자식 모달' })).not.toBeInTheDocument();
    expect(screen.getByRole('dialog', { name: '부모 모달' })).toBeInTheDocument();
    expect(childOpener).toHaveFocus();

    const parentDialog = screen.getByRole('dialog', { name: '부모 모달' });
    fireEvent.click(within(parentDialog).getByRole('button', { name: '닫기' }));
    expect(parentOpener).toHaveFocus();
  });

  it.each<[InvalidNestedTargetMode, boolean]>([
    ['removed', false],
    ['disabled', false],
    ['aria-disabled', false],
    ['hidden', false],
    ['inert', false],
    ['removed', true],
  ])(
    'restores an invalid %s child target inside the surviving parent (busy: %s)',
    (mode, parentBusy) => {
      render(<NestedInvalidTargetHarness mode={mode} parentBusy={parentBusy} />);
      fireEvent.click(screen.getByRole('button', { name: 'Open parent' }));
      const childOpener = screen.getByRole('button', { name: 'Open child' });
      childOpener.focus();
      fireEvent.click(childOpener);
      fireEvent.click(screen.getByRole('button', { name: 'Invalidate child targets' }));

      fireEvent.keyDown(document, { key: 'Escape' });

      expect(screen.queryByRole('dialog', { name: 'Closing child' })).not.toBeInTheDocument();
      const parentDialog = screen.getByRole('dialog', { name: 'Surviving parent' });
      expect(parentDialog).toContainElement(document.activeElement as HTMLElement);
      expect(screen.getByRole('heading', { name: 'Page behind parent' })).not.toHaveFocus();
    },
  );

  it('restores focus when an open modal unmounts', () => {
    const opener = document.createElement('button');
    opener.textContent = '외부 열기 버튼';
    document.body.appendChild(opener);
    opener.focus();

    const { unmount } = render(
      <Modal open onClose={() => {}} title="언마운트 모달">
        <button type="button">작업</button>
      </Modal>,
    );
    expect(screen.getByRole('dialog', { name: '언마운트 모달' })).toHaveFocus();

    unmount();

    expect(opener).toHaveFocus();
    opener.remove();
  });

  it.each<InvalidOpenerMode>(['removed', 'disabled', 'aria-disabled'])(
    'uses the explicit fallback when the opener becomes %s',
    (mode) => {
      render(<ExplicitFallbackHarness mode={mode} />);
      const opener = screen.getByRole('button', { name: 'Open fallback modal' });
      const fallback = screen.getByRole('button', { name: 'Explicit fallback' });

      opener.focus();
      fireEvent.click(opener);
      fireEvent.click(screen.getByRole('button', { name: 'Invalidate opener' }));
      fireEvent.keyDown(document, { key: 'Escape' });

      expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
      expect(fallback).toHaveFocus();
    },
  );

  it('falls back to the current main heading without leaving a tabindex behind', () => {
    render(<MainFallbackHarness includeHeading />);
    const opener = screen.getByRole('button', { name: 'Open main fallback modal' });

    opener.focus();
    fireEvent.click(opener);
    fireEvent.click(screen.getByRole('button', { name: 'Remove focus targets' }));
    fireEvent.keyDown(document, { key: 'Escape' });

    const heading = screen.getByRole('heading', { name: 'Stable page heading', level: 1 });
    expect(heading).toHaveFocus();
    expect(heading).not.toHaveAttribute('tabindex');
  });

  it('falls back to the current main region when no stable heading exists', () => {
    render(<MainFallbackHarness includeHeading={false} />);
    const opener = screen.getByRole('button', { name: 'Open main fallback modal' });

    opener.focus();
    fireEvent.click(opener);
    fireEvent.click(screen.getByRole('button', { name: 'Remove focus targets' }));
    fireEvent.keyDown(document, { key: 'Escape' });

    const main = screen.getByRole('main', { name: 'Primary content' });
    expect(main).toHaveFocus();
    expect(main).not.toHaveAttribute('tabindex');
  });

  it('exposes one busy contract for close, Escape, backdrop, focus, and recovery', () => {
    render(<BusyModalHarness />);
    const dialog = screen.getByRole('dialog', { name: '처리 모달' });
    const close = within(dialog).getByRole('button', { name: '닫기' });

    expect(dialog).toHaveAttribute('aria-busy', 'true');
    expect(close).toBeDisabled();
    fireEvent.click(close);
    expect(dialog).toBeVisible();
    dialog.focus();
    fireEvent.keyDown(document, { key: 'Tab' });
    expect(within(dialog).getByRole('button', { name: '내부 작업' })).toHaveFocus();
    fireEvent.keyDown(document, { key: 'Escape' });
    fireEvent.click(dialog.parentElement!);
    expect(screen.getByRole('dialog', { name: '처리 모달' })).toBeVisible();

    fireEvent.click(screen.getByRole('button', { name: '작업 완료' }));
    expect(dialog).not.toHaveAttribute('aria-busy');
    expect(close).toBeEnabled();
    fireEvent.keyDown(document, { key: 'Escape' });
    expect(screen.queryByRole('dialog', { name: '처리 모달' })).not.toBeInTheDocument();
  });
});
