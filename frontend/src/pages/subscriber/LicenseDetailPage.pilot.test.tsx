import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { afterEach, beforeEach, expect, it, vi } from 'vitest';
import { fetchLicenseDetail } from '@/api/licenses';
import { useAuthStore } from '@/store/authStore';
import { startPilotSession } from '@/store/pilotSession';
import { useToastStore } from '@/store/toastStore';
import type { User } from '@/types';
import LicenseDetailPage from './LicenseDetailPage';

vi.mock('@/config/pilotMode', () => ({ isPilotMode: true }));
vi.mock('@/api/licenses', () => ({ fetchLicenseDetail: vi.fn() }));

const code = '00000000-0000-4000-8000-000000000011';
const writeText = vi.fn<(text: string) => Promise<void>>();
const originalClipboard = Object.getOwnPropertyDescriptor(navigator, 'clipboard');

beforeEach(() => {
  useAuthStore.getState().login('user-A', { id: 11, role: 'USER' } as User);
  useToastStore.setState({ toasts: [] });
  writeText.mockReset();
  Object.defineProperty(navigator, 'clipboard', { configurable: true, value: { writeText } });
  vi.mocked(fetchLicenseDetail).mockResolvedValue({
    id: 7,
    track: { id: 106, title: '공개곡' },
    user: { id: 11, nickname: '회원 A' },
    issuedAt: '2026-11-21T12:00:00',
    licenseCode: code,
  });
});

afterEach(() => {
  if (originalClipboard) Object.defineProperty(navigator, 'clipboard', originalClipboard);
  else Reflect.deleteProperty(navigator, 'clipboard');
  useAuthStore.getState().clearSession();
  vi.clearAllMocks();
});

async function copyButton() {
  render(
    <MemoryRouter initialEntries={['/licenses/7']}>
      <Routes>
        <Route path="/licenses/:licenseId" element={<LicenseDetailPage />} />
      </Routes>
    </MemoryRouter>,
  );
  return screen.findByRole('button', { name: '코드 복사' });
}

it('copies the complete displayed UUID on click and reports success', async () => {
  writeText.mockResolvedValue(undefined);
  fireEvent.click(await copyButton());
  expect(writeText).toHaveBeenCalledExactlyOnceWith(code);
  await waitFor(() =>
    expect(useToastStore.getState().toasts[0]?.message).toBe('라이선스 코드가 복사되었습니다.'),
  );
});

it('retains selectable text and reports clipboard denial', async () => {
  writeText.mockRejectedValue(new Error('Denied'));
  fireEvent.click(await copyButton());
  await waitFor(() => expect(useToastStore.getState().toasts[0]?.type).toBe('error'));
  expect(screen.getByText(code)).toBeInTheDocument();
});

it('ignores the previous owner when session storage changes before React receives an event', async () => {
  const button = await copyButton();
  vi.mocked(fetchLicenseDetail).mockRejectedValue(new Error('Unavailable'));
  startPilotSession({ id: 22, role: 'USER' } as User, 'user-B', 'refresh-B');
  fireEvent.click(button);
  expect(writeText).not.toHaveBeenCalled();
  expect(useToastStore.getState().toasts).toHaveLength(0);
});

it('retires clipboard feedback if the account changes during a pending write', async () => {
  let settle!: () => void;
  writeText.mockReturnValue(new Promise<void>((resolve) => (settle = resolve)));
  fireEvent.click(await copyButton());
  vi.mocked(fetchLicenseDetail).mockRejectedValue(new Error('Unavailable'));
  act(() => useAuthStore.getState().login('user-B', { id: 22, role: 'USER' } as User));
  await act(async () => settle());
  expect(writeText).toHaveBeenCalledExactlyOnceWith(code);
  expect(useToastStore.getState().toasts).toHaveLength(0);
  expect(screen.queryByText(code)).not.toBeInTheDocument();
});
