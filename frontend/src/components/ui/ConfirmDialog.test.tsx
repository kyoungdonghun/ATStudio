import { fireEvent, render, screen } from '@testing-library/react';
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
    const confirmButton = screen.getByRole('button', { name: 'Continue' });
    fireEvent.click(confirmButton);
    fireEvent.click(confirmButton);
    fireEvent.keyDown(document, { key: 'Escape' });

    expect(onConfirm).toHaveBeenCalledTimes(1);
    expect(onCancel).not.toHaveBeenCalled();
    expect(confirmButton).toBeDisabled();
  });
});
