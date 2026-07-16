import { fireEvent, render, screen, within } from '@testing-library/react';
import { useState } from 'react';
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
});
