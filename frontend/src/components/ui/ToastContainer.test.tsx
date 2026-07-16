import { fireEvent, render, screen } from '@testing-library/react';
import { beforeEach, describe, expect, it } from 'vitest';
import ToastContainer from '@/components/ui/ToastContainer';
import { useToastStore } from '@/store/toastStore';

describe('ToastContainer accessibility', () => {
  beforeEach(() => {
    useToastStore.setState({ toasts: [] });
  });

  it('announces errors assertively and passive feedback politely', () => {
    useToastStore.setState({
      toasts: [
        { id: 1, type: 'error', message: '저장에 실패했습니다.' },
        { id: 2, type: 'success', message: '저장했습니다.' },
      ],
    });

    render(<ToastContainer />);

    expect(screen.getByRole('alert')).toHaveAttribute('aria-live', 'assertive');
    expect(screen.getByRole('status')).toHaveAttribute('aria-live', 'polite');
  });

  it('uses a focusable native dismiss button', () => {
    useToastStore.setState({
      toasts: [{ id: 7, type: 'info', message: '새 알림입니다.' }],
    });

    render(<ToastContainer />);

    const dismissButton = screen.getByRole('button', { name: '새 알림입니다. 알림 닫기' });
    dismissButton.focus();
    expect(dismissButton).toHaveFocus();
    expect(dismissButton.tagName).toBe('BUTTON');

    fireEvent.click(dismissButton);

    expect(screen.queryByText('새 알림입니다.')).not.toBeInTheDocument();
  });
});
