import { StrictMode, useLayoutEffect } from 'react';
import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import LicenseListPage from '@/pages/subscriber/LicenseListPage';
import { useAuthStore } from '@/store/authStore';
import type { PageInfo, User } from '@/types';

const fetchMyLicenses = vi.hoisted(() => vi.fn());
const downloadTrack = vi.hoisted(() => vi.fn());

vi.mock('@/api/licenses', () => ({ fetchMyLicenses }));
vi.mock('@/api/downloads', () => ({
  downloadTrack,
  triggerBlobDownload: vi.fn(),
}));
vi.mock('@/store/toastStore', () => ({
  useToastStore: (selector: (state: { show: ReturnType<typeof vi.fn> }) => unknown) =>
    selector({ show: vi.fn() }),
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

const pageInfo: PageInfo = {
  page: 1,
  size: 20,
  total: 1,
  start: 1,
  end: 1,
  prev: false,
  next: false,
};

function AuthLayoutProbe({ onLayout }: { onLayout: (accessToken: string | null) => void }) {
  const accessToken = useAuthStore((state) => state.accessToken);

  useLayoutEffect(() => {
    onLayout(accessToken);
  }, [accessToken, onLayout]);

  return null;
}

describe('LicenseListPage load ownership', () => {
  beforeEach(() => {
    fetchMyLicenses.mockReset();
    downloadTrack.mockReset();
    useAuthStore.setState({
      user: { id: 1 } as User,
      accessToken: 'owner-token-one',
      role: 'USER',
    });
  });

  it('aborts the retired StrictMode load and suppresses its stale completion', async () => {
    const retired = deferred<{
      dataList: Array<{
        id: number;
        track: { id: number; title: string };
        licenseCode: string;
        issuedAt: string;
      }>;
      pageInfo: PageInfo;
    }>();
    const current = deferred<{
      dataList: Array<{
        id: number;
        track: { id: number; title: string };
        licenseCode: string;
        issuedAt: string;
      }>;
      pageInfo: PageInfo;
    }>();
    fetchMyLicenses.mockReturnValueOnce(retired.promise).mockReturnValueOnce(current.promise);

    render(
      <StrictMode>
        <LicenseListPage />
      </StrictMode>,
    );
    await waitFor(() => expect(fetchMyLicenses).toHaveBeenCalledTimes(2));
    const retiredSignal = fetchMyLicenses.mock.calls[0][2] as AbortSignal;
    expect(retiredSignal.aborted).toBe(true);

    await act(async () =>
      current.resolve({
        dataList: [
          {
            id: 2,
            track: { id: 20, title: '현재 라이선스' },
            licenseCode: 'CURRENT-CODE',
            issuedAt: '2026-08-13T00:00:00Z',
          },
        ],
        pageInfo,
      }),
    );
    expect(await screen.findByText('현재 라이선스')).toBeInTheDocument();

    await act(async () =>
      retired.resolve({
        dataList: [
          {
            id: 1,
            track: { id: 10, title: '이전 라이선스' },
            licenseCode: 'OLD-CODE',
            issuedAt: '2026-08-12T00:00:00Z',
          },
        ],
        pageInfo,
      }),
    );
    expect(screen.queryByText('이전 라이선스')).not.toBeInTheDocument();
    expect(screen.getByText('현재 라이선스')).toBeInTheDocument();
  });

  it('hides an open license projection and makes its detached control inert before effects', async () => {
    fetchMyLicenses.mockResolvedValue({
      dataList: [
        {
          id: 1,
          track: { id: 10, title: 'Retired license track' },
          licenseCode: 'RETIRED-LICENSE-CODE',
          issuedAt: '2026-08-13T00:00:00Z',
        },
      ],
      pageInfo,
    });
    const observations: Array<{ token: string | null; retiredVisible: boolean }> = [];
    const onLayout = (token: string | null) => {
      observations.push({
        token,
        retiredVisible: document.body.textContent?.includes('RETIRED-LICENSE-CODE') ?? false,
      });
    };

    render(
      <>
        <LicenseListPage />
        <AuthLayoutProbe onLayout={onLayout} />
      </>,
    );
    fireEvent.click(await screen.findByRole('button', { name: '상세' }));
    const staleDownload = screen.getByRole('button', { name: '↓ 다시 다운로드' });
    observations.length = 0;

    act(() => {
      useAuthStore.setState({ accessToken: 'owner-token-two' });
      fireEvent.click(staleDownload);
    });

    expect(observations).toContainEqual({ token: 'owner-token-two', retiredVisible: false });
    expect(screen.queryByText('RETIRED-LICENSE-CODE')).not.toBeInTheDocument();
    expect(downloadTrack).not.toHaveBeenCalled();
  });
});
