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

  it('accepts exactly 500 review-note characters and sends the exact payload', async () => {
    const note = '가'.repeat(500);
    const dialog = await openReview('반려');
    const input = within(dialog).getByLabelText('처리 사유 (필수)');

    fireEvent.change(input, { target: { value: note } });
    expect(within(dialog).getByText('500/500')).toBeInTheDocument();
    fireEvent.click(within(dialog).getByRole('button', { name: '반려' }));

    await waitFor(() => {
      expect(processCompanyCert).toHaveBeenCalledTimes(1);
      expect(processCompanyCert).toHaveBeenCalledWith(1, {
        status: 'REJECTED',
        adminNote: note,
      });
    });
  });

  it('blocks a 501-character review note before the request boundary', async () => {
    const dialog = await openReview('반려');
    const input = within(dialog).getByLabelText('처리 사유 (필수)');

    fireEvent.change(input, { target: { value: '가'.repeat(501) } });
    fireEvent.click(within(dialog).getByRole('button', { name: '반려' }));

    expect(within(dialog).getByRole('alert')).toHaveTextContent('최대 500자');
    expect(processCompanyCert).not.toHaveBeenCalled();
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

  it('blocks all review close paths and duplicate processing while pending', async () => {
    const pendingReview = deferred<unknown>();
    processCompanyCert.mockReturnValueOnce(pendingReview.promise);
    const reviewDialog = await openReview('승인');
    const detailDialog = screen.getByRole('dialog', { name: '기업 인증 상세' });
    const approveButton = within(reviewDialog).getByRole('button', { name: '승인' });
    fireEvent.click(approveButton);

    expect(reviewDialog).toHaveAttribute('aria-busy', 'true');
    expect(detailDialog).toHaveAttribute('aria-busy', 'true');
    expect(within(reviewDialog).getByRole('button', { name: '닫기' })).toBeDisabled();
    expect(within(reviewDialog).getByRole('button', { name: '취소' })).toBeDisabled();
    expect(within(detailDialog).getByRole('button', { name: '닫기' })).toBeDisabled();
    fireEvent.keyDown(document, { key: 'Escape' });
    fireEvent.click(reviewDialog.parentElement!);
    fireEvent.click(within(reviewDialog).getByRole('button', { name: '닫기' }));
    fireEvent.click(within(reviewDialog).getByRole('button', { name: '취소' }));
    fireEvent.click(approveButton);

    expect(screen.getByRole('dialog', { name: '승인 처리' })).toBeInTheDocument();
    expect(processCompanyCert).toHaveBeenCalledTimes(1);

    await act(async () =>
      pendingReview.resolve({
        id: detail.id,
        status: 'APPROVED',
        certificationCode: 'CERT-1',
        approvedAt: '2026-08-14T00:00:00',
      }),
    );
    await waitFor(() =>
      expect(screen.queryByRole('dialog', { name: '승인 처리' })).not.toBeInTheDocument(),
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

  it('retries the failed current list with the same filter and page', async () => {
    const pageOne = { ...pageInfo, total: 21, end: 2, next: true };
    const pageTwo = { ...pageInfo, page: 2, total: 21, start: 1, end: 2, prev: true };
    fetchCompanyCerts.mockReset();
    fetchCompanyCerts
      .mockResolvedValueOnce({ dataList: [detail], pageInfo: pageOne })
      .mockResolvedValueOnce({ dataList: [{ ...detail, status: 'REJECTED' }], pageInfo: pageOne })
      .mockRejectedValueOnce(new Error('list failure'))
      .mockResolvedValueOnce({
        dataList: [{ ...detail, id: 3, companyName: 'Retried company', status: 'REJECTED' }],
        pageInfo: pageTwo,
      });

    render(<CompanyCertManagePage />);
    fireEvent.change(await screen.findByRole('combobox'), { target: { value: 'REJECTED' } });
    await waitFor(() => expect(fetchCompanyCerts).toHaveBeenCalledTimes(2));
    fireEvent.click(await screen.findByRole('button', { name: '2페이지' }));

    expect(
      await screen.findByText('기업 인증 신청 목록을 불러오지 못했습니다.'),
    ).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '다시 시도' }));

    expect(await screen.findByText('Retried company')).toBeInTheDocument();
    expect(fetchCompanyCerts).toHaveBeenLastCalledWith({ page: 2, size: 20, status: 'REJECTED' });
  });

  it('retries a failed detail load for the selected certification ID', async () => {
    fetchCompanyCert
      .mockRejectedValueOnce(new Error('detail failure'))
      .mockResolvedValueOnce(detail);
    render(<CompanyCertManagePage />);

    fireEvent.click(await screen.findByRole('button', { name: '상세' }));
    const dialog = await screen.findByRole('dialog', { name: '기업 인증 상세' });
    expect(
      within(dialog).getByText('기업 인증 신청 상세를 불러오지 못했습니다.'),
    ).toBeInTheDocument();
    fireEvent.click(within(dialog).getByRole('button', { name: '다시 시도' }));

    expect(await within(dialog).findByText(detail.userNickname)).toBeInTheDocument();
    expect(fetchCompanyCert).toHaveBeenCalledTimes(2);
    expect(fetchCompanyCert).toHaveBeenNthCalledWith(1, detail.id);
    expect(fetchCompanyCert).toHaveBeenNthCalledWith(2, detail.id);
  });

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

  it.each(['success', 'failure'] as const)(
    'blocks detail close until the late post-review refresh $completion settles',
    async (completion) => {
      const lateRefresh = deferred<CompanyCertification>();
      fetchCompanyCert
        .mockResolvedValueOnce(detail)
        .mockImplementationOnce(() => lateRefresh.promise);
      const reviewDialog = await openReview('승인');

      fireEvent.click(within(reviewDialog).getByRole('button', { name: '승인' }));
      await waitFor(() => expect(fetchCompanyCert).toHaveBeenCalledTimes(2));
      const detailDialog = screen.getByRole('dialog', { name: '기업 인증 상세' });
      const closeButton = within(detailDialog).getByRole('button', { name: '닫기' });
      expect(closeButton).toBeDisabled();
      fireEvent.click(closeButton);
      expect(screen.getByRole('dialog', { name: '기업 인증 상세' })).toBeInTheDocument();

      if (completion === 'success') {
        await act(async () => lateRefresh.resolve({ ...detail, status: 'APPROVED' }));
      } else {
        await act(async () => lateRefresh.reject(new Error('late refresh failure')));
      }

      const settledDetailDialog = await screen.findByRole('dialog', {
        name: '기업 인증 상세',
      });
      const settledCloseButton = within(settledDetailDialog).getByRole('button', {
        name: '닫기',
      });
      await waitFor(() => expect(settledCloseButton).toBeEnabled());
      fireEvent.click(settledCloseButton);
      expect(screen.queryByRole('dialog', { name: '기업 인증 상세' })).not.toBeInTheDocument();
      expect(
        screen.queryByText('기업 인증 신청 상세를 불러오지 못했습니다.'),
      ).not.toBeInTheDocument();
    },
  );

  it('keeps review target A immutable until its detail and list refreshes settle', async () => {
    const pendingMutation = deferred<unknown>();
    const pendingDetailRefresh = deferred<CompanyCertification>();
    const pendingListRefresh = deferred<{
      dataList: CompanyCertification[];
      pageInfo: typeof pageInfo;
    }>();
    const newerDetail: CompanyCertification = {
      ...detail,
      id: 2,
      userId: 20,
      userNickname: 'newer-owner',
      userEmail: 'newer@example.com',
      companyName: 'Newer company',
    };
    const listResult = {
      dataList: [detail, newerDetail],
      pageInfo: { ...pageInfo, total: 2, end: 2 },
    };
    fetchCompanyCerts
      .mockResolvedValueOnce(listResult)
      .mockReturnValueOnce(pendingListRefresh.promise);
    fetchCompanyCert
      .mockResolvedValue(newerDetail)
      .mockResolvedValueOnce(detail)
      .mockReturnValueOnce(pendingDetailRefresh.promise);
    processCompanyCert.mockReturnValueOnce(pendingMutation.promise);

    render(<CompanyCertManagePage />);
    const detailButtons = await screen.findAllByRole('button', { name: '상세' });
    fireEvent.click(detailButtons[0]);
    const detailDialog = await screen.findByRole('dialog', { name: '기업 인증 상세' });
    expect(await within(detailDialog).findByText(detail.userNickname)).toBeInTheDocument();
    fireEvent.click(within(detailDialog).getByRole('button', { name: '승인' }));
    const reviewDialog = await screen.findByRole('dialog', { name: '승인 처리' });
    fireEvent.click(within(reviewDialog).getByRole('button', { name: '승인' }));

    expect(detailButtons[1]).toBeDisabled();
    fireEvent.click(detailButtons[1]);
    expect(fetchCompanyCert).toHaveBeenCalledTimes(1);
    expect(within(detailDialog).getByText(detail.userNickname)).toBeInTheDocument();

    await act(async () =>
      pendingMutation.resolve({
        id: detail.id,
        status: 'APPROVED',
        certificationCode: 'CERT-1',
        approvedAt: '2026-08-14T00:00:00',
      }),
    );
    await waitFor(() => expect(fetchCompanyCert).toHaveBeenCalledTimes(2));
    expect(detailButtons[1]).toBeDisabled();
    fireEvent.click(detailButtons[1]);
    expect(fetchCompanyCert).toHaveBeenCalledTimes(2);

    await act(async () => pendingDetailRefresh.resolve({ ...detail, status: 'APPROVED' }));
    await waitFor(() => expect(fetchCompanyCerts).toHaveBeenCalledTimes(2));
    expect(detailButtons[1]).toBeDisabled();
    fireEvent.click(detailButtons[1]);
    expect(fetchCompanyCert).toHaveBeenCalledTimes(2);

    await act(async () =>
      pendingListRefresh.resolve({
        dataList: [{ ...detail, status: 'APPROVED' }, newerDetail],
        pageInfo: { ...pageInfo, total: 2, end: 2 },
      }),
    );
    await waitFor(() => expect(detailButtons[1]).toBeEnabled());

    expect(processCompanyCert).toHaveBeenCalledTimes(1);
    expect(fetchCompanyCert).toHaveBeenNthCalledWith(1, detail.id);
    expect(fetchCompanyCert).toHaveBeenNthCalledWith(2, detail.id);
    expect(fetchCompanyCerts).toHaveBeenCalledTimes(2);
    expect(within(detailDialog).getByText(detail.userNickname)).toBeInTheDocument();
    expect(within(detailDialog).queryByText(newerDetail.userNickname)).not.toBeInTheDocument();

    fireEvent.click(detailButtons[1]);
    expect(await within(detailDialog).findByText(newerDetail.userNickname)).toBeInTheDocument();
    expect(fetchCompanyCert).toHaveBeenNthCalledWith(3, newerDetail.id);
  });

  it('keeps a failed review attached to target A when target B is activated while pending', async () => {
    const pendingMutation = deferred<unknown>();
    const newerDetail: CompanyCertification = {
      ...detail,
      id: 2,
      userId: 20,
      userNickname: 'newer-owner',
      userEmail: 'newer@example.com',
      companyName: 'Newer company',
    };
    fetchCompanyCerts.mockResolvedValue({
      dataList: [detail, newerDetail],
      pageInfo: { ...pageInfo, total: 2, end: 2 },
    });
    processCompanyCert.mockReturnValueOnce(pendingMutation.promise);

    render(<CompanyCertManagePage />);
    const detailButtons = await screen.findAllByRole('button', { name: '상세' });
    fireEvent.click(detailButtons[0]);
    const detailDialog = await screen.findByRole('dialog', { name: '기업 인증 상세' });
    expect(await within(detailDialog).findByText(detail.userNickname)).toBeInTheDocument();
    fireEvent.click(within(detailDialog).getByRole('button', { name: '승인' }));
    const reviewDialog = await screen.findByRole('dialog', { name: '승인 처리' });
    fireEvent.click(within(reviewDialog).getByRole('button', { name: '승인' }));

    expect(detailButtons[1]).toBeDisabled();
    fireEvent.click(detailButtons[1]);
    await act(async () => pendingMutation.reject(new Error('late mutation failure')));

    expect(await within(reviewDialog).findByRole('alert')).toHaveTextContent(
      '기업 인증 심사 처리에 실패했습니다.',
    );
    expect(within(detailDialog).getByText(detail.userNickname)).toBeInTheDocument();
    expect(within(detailDialog).queryByText(newerDetail.userNickname)).not.toBeInTheDocument();
    expect(processCompanyCert).toHaveBeenCalledTimes(1);
    expect(fetchCompanyCert).toHaveBeenCalledTimes(1);
    expect(fetchCompanyCert).toHaveBeenCalledWith(detail.id);
    expect(fetchCompanyCerts).toHaveBeenCalledTimes(1);
  });
});
