import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import SiteSettingsPage from './SiteSettingsPage';

const mocks = vi.hoisted(() => ({
  getSetting: vi.fn(),
  updateSetting: vi.fn(),
  showToast: vi.fn(),
}));

vi.mock('@/api/settings', () => ({
  getSetting: (...args: unknown[]) => mocks.getSetting(...args),
  updateSetting: (...args: unknown[]) => mocks.updateSetting(...args),
}));

vi.mock('@/store/toastStore', () => ({
  useToastStore: (selector: (state: { show: typeof mocks.showToast }) => unknown) =>
    selector({ show: mocks.showToast }),
}));

function deferred<T>() {
  let resolve!: (value: T) => void;
  let reject!: (reason?: unknown) => void;
  const promise = new Promise<T>((nextResolve, nextReject) => {
    resolve = nextResolve;
    reject = nextReject;
  });
  return { promise, resolve, reject };
}

describe('SiteSettingsPage canonical save', () => {
  beforeEach(() => {
    mocks.getSetting.mockReset();
    mocks.updateSetting.mockReset();
    mocks.showToast.mockReset();
  });

  it('freezes the submitted draft and shows the canonical public-read value before success', async () => {
    const saveRequest = deferred<void>();
    mocks.getSetting
      .mockResolvedValueOnce('Original guide')
      .mockResolvedValueOnce('Canonical saved guide');
    mocks.updateSetting.mockReturnValue(saveRequest.promise);
    render(<SiteSettingsPage />);

    const textarea = await screen.findByDisplayValue('Original guide');
    fireEvent.change(textarea, { target: { value: 'Submitted guide' } });
    fireEvent.click(screen.getByRole('button', { name: '저장' }));

    expect(textarea).toBeDisabled();
    expect(mocks.updateSetting).toHaveBeenCalledTimes(1);
    expect(mocks.updateSetting).toHaveBeenCalledWith('COMPANY_CERT_GUIDE', 'Submitted guide');
    expect(mocks.showToast).not.toHaveBeenCalledWith('success', expect.any(String));

    await act(async () => saveRequest.resolve());

    expect(await screen.findByDisplayValue('Canonical saved guide')).toBeEnabled();
    expect(mocks.getSetting).toHaveBeenCalledTimes(2);
    expect(mocks.showToast).toHaveBeenCalledWith('success', '설정이 저장되었습니다.');
  });

  it('does not claim success when the canonical public read cannot be verified', async () => {
    mocks.getSetting
      .mockResolvedValueOnce('Original guide')
      .mockRejectedValueOnce(new Error('read failed'));
    mocks.updateSetting.mockResolvedValue(undefined);
    render(<SiteSettingsPage />);

    fireEvent.change(await screen.findByDisplayValue('Original guide'), {
      target: { value: 'Submitted guide' },
    });
    fireEvent.click(screen.getByRole('button', { name: '저장' }));

    await waitFor(() =>
      expect(mocks.showToast).toHaveBeenCalledWith(
        'error',
        '저장 결과를 확인하지 못했습니다. 초기화로 최신 값을 확인해 주세요.',
      ),
    );
    expect(mocks.showToast).not.toHaveBeenCalledWith('success', expect.any(String));
  });
});
