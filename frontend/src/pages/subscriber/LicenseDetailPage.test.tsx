import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { createMemoryRouter, RouterProvider, useNavigate } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import LicenseDetailPage from '@/pages/subscriber/LicenseDetailPage';
import type { LicenseDetail } from '@/api/licenses';
import { useAuthStore } from '@/store/authStore';
import type { User } from '@/types';

const fetchLicenseDetail = vi.hoisted(() => vi.fn());

vi.mock('@/api/licenses', () => ({ fetchLicenseDetail }));

function deferred<T>() {
  let resolve!: (value: T) => void;
  const promise = new Promise<T>((nextResolve) => {
    resolve = nextResolve;
  });
  return { promise, resolve };
}

function RouteHarness() {
  const navigate = useNavigate();
  return (
    <>
      <button type="button" onClick={() => navigate('/licenses/2')}>
        next license
      </button>
      <LicenseDetailPage />
    </>
  );
}

function renderPage(initialEntry: string) {
  const router = createMemoryRouter(
    [
      { path: '/licenses/:licenseId', element: <RouteHarness /> },
      { path: '/licenses', element: <div>License list</div> },
    ],
    { initialEntries: [initialEntry] },
  );
  render(<RouterProvider router={router} future={{ v7_startTransition: true }} />);
}

describe('LicenseDetailPage load ownership', () => {
  beforeEach(() => {
    fetchLicenseDetail.mockReset();
    useAuthStore.setState({
      user: { id: 1 } as User,
      accessToken: 'owner-token',
      role: 'USER',
    });
  });

  it.each(['1e3', '0x10', '+7', ' 7', '7 ', '7.5', '0', '-1', '9007199254740992', 'abc'])(
    'rejects noncanonical license id %s without a request',
    (id) => {
      renderPage(`/licenses/${id}`);

      expect(screen.getByText('라이선스 주소가 올바르지 않습니다.')).toBeInTheDocument();
      expect(screen.getByRole('link', { name: '라이선스 목록으로' })).toHaveAttribute(
        'href',
        '/licenses',
      );
      expect(fetchLicenseDetail).not.toHaveBeenCalled();
    },
  );

  it('keeps the newest license route result after a stale success', async () => {
    const oldDetail = deferred<LicenseDetail>();
    const currentDetail = deferred<LicenseDetail>();
    fetchLicenseDetail
      .mockReturnValueOnce(oldDetail.promise)
      .mockReturnValueOnce(currentDetail.promise);

    renderPage('/licenses/1');
    await waitFor(() => expect(fetchLicenseDetail).toHaveBeenCalledTimes(1));
    const oldSignal = fetchLicenseDetail.mock.calls[0][1] as AbortSignal;
    fireEvent.click(screen.getByRole('button', { name: 'next license' }));
    await waitFor(() => expect(fetchLicenseDetail).toHaveBeenCalledTimes(2));
    expect(oldSignal.aborted).toBe(true);

    await act(async () =>
      currentDetail.resolve({
        id: 2,
        track: { id: 20, title: '현재 라이선스 곡' },
        licenseCode: 'CURRENT',
        issuedAt: '2026-08-13T00:00:00Z',
        user: { id: 1, nickname: 'member' },
      }),
    );
    expect(await screen.findByText('현재 라이선스 곡')).toBeInTheDocument();

    await act(async () =>
      oldDetail.resolve({
        id: 1,
        track: { id: 10, title: '이전 라이선스 곡' },
        licenseCode: 'OLD',
        issuedAt: '2026-08-12T00:00:00Z',
        user: { id: 1, nickname: 'member' },
      }),
    );
    expect(screen.queryByText('이전 라이선스 곡')).not.toBeInTheDocument();
    expect(screen.getByText('현재 라이선스 곡')).toBeInTheDocument();
  });
});
