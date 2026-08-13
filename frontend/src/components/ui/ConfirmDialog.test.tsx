import { fireEvent, render, screen, within } from '@testing-library/react';
import { useState } from 'react';
import { describe, expect, it, vi } from 'vitest';
import ConfirmDialog from '@/components/ui/ConfirmDialog';

describe('ConfirmDialog', () => {
  it('allows only one confirmation while the action is pending', () => {
    const onConfirm = vi.fn();
    const onCancel = vi.fn();

    function Harness() {
      const [busy, setBusy] = useState(false);
      return (
        <ConfirmDialog
          open
          title="Confirm operation"
          message="This action changes persisted state."
          confirmLabel="Continue"
          busy={busy}
          onConfirm={() => {
            onConfirm();
            setBusy(true);
          }}
          onCancel={onCancel}
        />
      );
    }

    render(<Harness />);
    const dialog = screen.getByRole('dialog', { name: 'Confirm operation' });
    const confirmButton = screen.getByRole('button', { name: 'Continue' });
    fireEvent.click(confirmButton);
    fireEvent.click(confirmButton);
    fireEvent.keyDown(document, { key: 'Escape' });
    fireEvent.click(dialog.parentElement!);
    fireEvent.click(within(dialog).getByRole('button', { name: '닫기' }));
    fireEvent.click(within(dialog).getByRole('button', { name: '취소' }));

    expect(onConfirm).toHaveBeenCalledTimes(1);
    expect(onCancel).not.toHaveBeenCalled();
    expect(confirmButton).toBeDisabled();
    expect(dialog).toHaveAttribute('aria-busy', 'true');
    expect(within(dialog).getByRole('button', { name: '닫기' })).toBeDisabled();
  });

  it('supports normal cancellation before submission', () => {
    const onCancel = vi.fn();
    render(
      <ConfirmDialog
        open
        title="Confirm operation"
        message="This action changes persisted state."
        onConfirm={vi.fn()}
        onCancel={onCancel}
      />,
    );

    const dialog = screen.getByRole('dialog', { name: 'Confirm operation' });
    fireEvent.click(dialog.parentElement!);

    expect(onCancel).toHaveBeenCalledTimes(1);
  });

  it('requires the exact trimmed typed confirmation when configured', () => {
    const onConfirm = vi.fn();

    function Harness() {
      const [value, setValue] = useState('');
      return (
        <ConfirmDialog
          open
          title="Typed operation"
          message="This action changes persisted state."
          confirmLabel="Execute"
          typedConfirmation={{
            label: 'Confirmation phrase',
            requiredText: '권한 보정 실행',
            value,
            onChange: setValue,
          }}
          onConfirm={onConfirm}
          onCancel={vi.fn()}
        />
      );
    }

    render(<Harness />);
    const input = screen.getByLabelText('Confirmation phrase');
    const executeButton = screen.getByRole('button', { name: 'Execute' });
    expect(executeButton).toBeDisabled();

    fireEvent.change(input, { target: { value: '권한 보정' } });
    fireEvent.click(executeButton);
    expect(onConfirm).not.toHaveBeenCalled();

    fireEvent.change(input, { target: { value: '  권한 보정 실행  ' } });
    expect(executeButton).toBeEnabled();
    fireEvent.click(executeButton);
    expect(onConfirm).toHaveBeenCalledTimes(1);
  });
});
