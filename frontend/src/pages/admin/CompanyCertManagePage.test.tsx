import { act, fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { StrictMode } from 'react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import CompanyCertManagePage from '@/pages/admin/CompanyCertManagePage';
import type { CompanyCertification } from '@/types';

const fetchCompanyCerts = vi.fn();
const fetchCompanyCert = vi.fn();
const processCompanyCert = vi.fn();
const downloadCompanyCertDocument = vi.fn();

vi.mock('@/api/admin', () => ({
  fetchCompanyCerts: (...args: unknown[]) => fetchCompanyCerts(...args),
  fetchCompanyCert: (...args: unknown[]) => fetchCompanyCert(...args),
  processCompanyCert: (...args: unknown[]) => processCompanyCert(...args),
  downloadCompanyCertDocument: (...args: unknown[]) => downloadCompanyCertDocument(...args),
}));

const detail: CompanyCertification = {
  id: 1,
  userId: 10,
  userNickname: '기업회원',
  userEmail: 'business@example.com',
  companyName: 'AT.M',
  phoneCompany: '02-1234-5678',
  status: 'PENDING',
  adminNote: null,
  documents: [],
  certificationCode: null,
  approvedAt: null,
  createdAt: '2026-07-16T00:00:00',
};

const pageInfo = {
  page: 1,
  size: 20,
  total: 1,
  start: 1,
  end: 1,
  prev: false,
  next: false,
};

function deferred<T>() {
  let resolve!: (value: T) => void;
  let reject!: (reason?: unknown) => void;
  const promise = new Promise<T>((resolvePromise, rejectPromise) => {
    resolve = resolvePromise;
    reject = rejectPromise;
  });
  return { promise, reject, resolve };
}

async function openReview(action: '승인' | '보완 요청' | '반려') {
  render(<CompanyCertManagePage />);
  const detailButton = await screen.findByRole('button', { name: '상세' });
  fireEvent.click(detailButton);

  const detailDialog = await screen.findByRole('dialog', { name: '기업 인증 상세' });
  fireEvent.click(within(detailDialog).getByRole('button', { name: action }));
  return screen.findByRole('dialog', { name: `${action} 처리` });
}

describe('CompanyCertManagePage review validation', () => {
  beforeEach(() => {
    fetchCompanyCerts.mockReset();
    fetchCompanyCert.mockReset();
    processCompanyCert.mockReset();
    downloadCompanyCertDocument.mockReset();
    fetchCompanyCerts.mockResolvedValue({
      dataList: [
        {
          id: detail.id,
          userId: detail.userId,
          userNickname: detail.userNickname,
          userEmail: detail.userEmail,
          companyName: detail.companyName,
          status: detail.status,
          createdAt: detail.createdAt,
        },
      ],
      pageInfo,
    });
    fetchCompanyCert.mockResolvedValue(detail);
    processCompanyCert.mockResolvedValue({
      id: detail.id,
      status: 'REVISION_REQUESTED',
      certificationCode: null,
      approvedAt: null,
    });
  });

  it('requires and trims a reason for REVISION_REQUESTED', async () => {
    const dialog = await openReview('보완 요청');
    const input = within(dialog).getByLabelText('처리 사유 (필수)');

    fireEvent.change(input, { target: { value: '   ' } });
    fireEvent.click(within(dialog).getByRole('button', { name: '보완 요청' }));

    expect(within(dialog).getByRole('alert')).toHaveTextContent('보완 요청 사유를 입력해주세요.');
    expect(processCompanyCert).not.toHaveBeenCalled();

    fireEvent.change(input, { target: { value: '  서류를 다시 제출해주세요.  ' } });
    fireEvent.click(within(dialog).getByRole('button', { name: '보완 요청' }));

    await waitFor(() => {
      expect(processCompanyCert).toHaveBeenCalledWith(1, {
        status: 'REVISION_REQUESTED',
        adminNote: '서류를 다시 제출해주세요.',
      });
    });
  });

  it('allows approval without a note', async () => {
    const dialog = await openReview('승인');

    fireEvent.click(within(dialog).getByRole('button', { name: '승인' }));

    await waitFor(() => {
      expect(processCompanyCert).toHaveBeenCalledWith(1, {
        status: 'APPROVED',
        adminNote: undefined,
      });
    });
  });

  it('shows a clear message when the server rejects review authorization', async () => {
    processCompanyCert.mockRejectedValue({ response: { status: 403 } });
    const dialog = await openReview('반려');
    fireEvent.change(within(dialog).getByLabelText('처리 사유 (필수)'), {
      target: { value: '신청 정보가 일치하지 않습니다.' },
    });

    fireEvent.click(within(dialog).getByRole('button', { name: '반려' }));

    expect(await within(dialog).findByRole('alert')).toHaveTextContent(
      '기업 인증 심사 권한이 없습니다.',
    );
  });

  it.each(['success', 'failure'] as const)(
    'lets only the latest list request own state after stale $completion',
    async (completion) => {
      const stale = deferred<{ dataList: (typeof detail)[]; pageInfo: typeof pageInfo }>();
      const latest = deferred<{ dataList: (typeof detail)[]; pageInfo: typeof pageInfo }>();
      fetchCompanyCerts.mockReset();
      fetchCompanyCerts
        .mockImplementationOnce(() => stale.promise)
        .mockImplementationOnce(() => latest.promise);

      render(
        <StrictMode>
          <CompanyCertManagePage />
        </StrictMode>,
      );
      await waitFor(() => expect(fetchCompanyCerts).toHaveBeenCalledTimes(2));

      if (completion === 'success') {
        await act(async () =>
          stale.resolve({
            dataList: [{ ...detail, id: 2, companyName: 'Stale company' }],
            pageInfo: { ...pageInfo, total: 21, next: true },
          }),
        );
      } else {
        await act(async () => stale.reject(new Error('stale failure')));
      }

      expect(screen.getByText('Loading...')).toBeInTheDocument();
      expect(screen.queryByText('Stale company')).not.toBeInTheDocument();

      await act(async () =>
        latest.resolve({
          dataList: [{ ...detail, id: 3, companyName: 'Latest company', status: 'REJECTED' }],
          pageInfo,
        }),
      );
      expect(await screen.findByText('Latest company')).toBeInTheDocument();
      expect(screen.queryByRole('button', { name: '2' })).not.toBeInTheDocument();
    },
  );

  it.each([
    { close: 'escape', completion: 'success' },
    { close: 'button', completion: 'failure' },
  ] as const)(
    'keeps detail closed after $close during loading and late $completion',
    async ({ close, completion }) => {
      const pendingDetail = deferred<CompanyCertification>();
      fetchCompanyCert.mockImplementationOnce(() => pendingDetail.promise);
      render(<CompanyCertManagePage />);

      fireEvent.click(await screen.findByRole('button', { name: '상세' }));
      const dialog = await screen.findByRole('dialog', { name: '기업 인증 상세' });
      if (close === 'escape') {
        fireEvent.keyDown(document, { key: 'Escape' });
      } else {
        fireEvent.click(within(dialog).getByText('X'));
      }
      await waitFor(() =>
        expect(screen.queryByRole('dialog', { name: '기업 인증 상세' })).not.toBeInTheDocument(),
      );

      if (completion === 'success') {
        await act(async () => pendingDetail.resolve(detail));
      } else {
        await act(async () => pendingDetail.reject(new Error('late failure')));
      }

      expect(screen.queryByRole('dialog', { name: '기업 인증 상세' })).not.toBeInTheDocument();
    },
  );
});
