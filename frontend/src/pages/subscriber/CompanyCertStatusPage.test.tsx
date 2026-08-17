import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { StrictMode } from 'react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import CompanyCertStatusPage from '@/pages/subscriber/CompanyCertStatusPage';
import type { CompanyCertification } from '@/types';

const fetchMyCompanyCert = vi.fn();
const resubmitCompanyCert = vi.fn();

vi.mock('@/api/companyCerts', async () => {
  const actual = await vi.importActual<typeof import('@/api/companyCerts')>('@/api/companyCerts');
  return {
    ...actual,
    fetchMyCompanyCert: (...args: unknown[]) => fetchMyCompanyCert(...args),
    resubmitCompanyCert: (...args: unknown[]) => resubmitCompanyCert(...args),
  };
});

const MB = 1024 * 1024;

const revisionRequested: CompanyCertification = {
  id: 1,
  userId: 10,
  userNickname: '기업회원',
  userEmail: 'business@example.com',
  companyName: 'AT.M',
  phoneCompany: null,
  status: 'REVISION_REQUESTED',
  adminNote: '사업자등록증을 다시 제출해주세요.',
  documents: [],
  certificationCode: null,
  approvedAt: null,
  createdAt: '2026-07-16T00:00:00',
};

function sizedFile(name: string, size: number): File {
  const testFile = new File(['x'], name);
  Object.defineProperty(testFile, 'size', { value: size });
  return testFile;
}

function deferred<T>() {
  let resolve!: (value: T) => void;
  let reject!: (reason?: unknown) => void;
  const promise = new Promise<T>((resolvePromise, rejectPromise) => {
    resolve = resolvePromise;
    reject = rejectPromise;
  });
  return { promise, reject, resolve };
}

function renderPage(strict = false) {
  return render(
    <MemoryRouter>
      {strict ? (
        <StrictMode>
          <CompanyCertStatusPage />
        </StrictMode>
      ) : (
        <CompanyCertStatusPage />
      )}
    </MemoryRouter>,
  );
}

describe('CompanyCertStatusPage', () => {
  beforeEach(() => {
    fetchMyCompanyCert.mockReset();
    resubmitCompanyCert.mockReset();
    fetchMyCompanyCert.mockResolvedValue(revisionRequested);
  });

  it('enforces the aggregate limit when revision files are selected in batches', async () => {
    renderPage();
    const input = await screen.findByLabelText('보완 서류 선택');
    expect(screen.getByText('처리 사유')).toBeInTheDocument();

    fireEvent.change(input, {
      target: {
        files: [sizedFile('one.pdf', 18 * MB), sizedFile('two.jpg', 18 * MB)],
      },
    });
    fireEvent.change(input, {
      target: { files: [sizedFile('three.png', 15 * MB)] },
    });

    expect(screen.getByRole('alert')).toHaveTextContent(
      '첨부파일 전체 용량은 50MB를 초과할 수 없습니다.',
    );
    expect(screen.queryByText('three.png')).not.toBeInTheDocument();
  });

  it('shows a clear BUSINESS-only message for a server 403', async () => {
    fetchMyCompanyCert.mockRejectedValue({ response: { status: 403 } });

    renderPage();

    expect(await screen.findByRole('alert')).toHaveTextContent(
      '기업 회원만 기업 인증을 이용할 수 있습니다.',
    );
    expect(screen.getByRole('button', { name: '다시 시도' })).toBeInTheDocument();
  });

  it('retries a failed status load on the current route', async () => {
    fetchMyCompanyCert
      .mockRejectedValueOnce({ response: { status: 500 } })
      .mockResolvedValueOnce(revisionRequested);

    renderPage();

    expect(await screen.findByRole('alert')).toHaveTextContent('인증 현황을 불러올 수 없습니다.');
    fireEvent.click(screen.getByRole('button', { name: '다시 시도' }));

    expect(await screen.findByText('처리 사유')).toBeInTheDocument();
    expect(fetchMyCompanyCert).toHaveBeenCalledTimes(2);
  });

  it('ignores stale status completion and keeps the latest result', async () => {
    const stale = deferred<CompanyCertification>();
    const latest = deferred<CompanyCertification>();
    fetchMyCompanyCert
      .mockImplementationOnce(() => stale.promise)
      .mockImplementationOnce(() => latest.promise);

    renderPage(true);
    await waitFor(() => expect(fetchMyCompanyCert).toHaveBeenCalledTimes(2));

    await act(async () => latest.resolve(revisionRequested));
    expect(await screen.findByText('처리 사유')).toBeInTheDocument();

    await act(async () => stale.resolve({ ...revisionRequested, status: 'APPROVED' }));
    expect(screen.getByText('보완 요청')).toBeInTheDocument();
    expect(screen.queryByText('승인')).not.toBeInTheDocument();
  });
});
