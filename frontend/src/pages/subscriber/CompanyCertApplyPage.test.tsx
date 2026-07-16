import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import CompanyCertApplyPage from '@/pages/subscriber/CompanyCertApplyPage';

const applyCompanyCert = vi.fn();
const fetchMyCompanyCert = vi.fn();
const getSetting = vi.fn();

vi.mock('@/api/companyCerts', async () => {
  const actual = await vi.importActual<typeof import('@/api/companyCerts')>('@/api/companyCerts');
  return {
    ...actual,
    applyCompanyCert: (...args: unknown[]) => applyCompanyCert(...args),
    fetchMyCompanyCert: (...args: unknown[]) => fetchMyCompanyCert(...args),
  };
});

vi.mock('@/api/settings', () => ({
  getSetting: (...args: unknown[]) => getSetting(...args),
}));

const MB = 1024 * 1024;

function sizedFile(name: string, size: number): File {
  const testFile = new File(['x'], name);
  Object.defineProperty(testFile, 'size', { value: size });
  return testFile;
}

function renderPage() {
  return render(
    <MemoryRouter initialEntries={['/company-certification/apply']}>
      <Routes>
        <Route path="/company-certification/apply" element={<CompanyCertApplyPage />} />
        <Route path="/company-certification/status" element={<div>인증 현황</div>} />
      </Routes>
    </MemoryRouter>,
  );
}

describe('CompanyCertApplyPage', () => {
  beforeEach(() => {
    applyCompanyCert.mockReset();
    fetchMyCompanyCert.mockReset();
    getSetting.mockReset();
    fetchMyCompanyCert.mockRejectedValue({ response: { status: 404 } });
    getSetting.mockResolvedValue(null);
  });

  it('enforces the 50MB aggregate limit across consecutive selections', async () => {
    renderPage();
    const input = await screen.findByLabelText('기업 인증 서류 선택');

    fireEvent.change(input, {
      target: {
        files: [sizedFile('one.pdf', 18 * MB), sizedFile('two.jpg', 18 * MB)],
      },
    });
    expect(screen.getByText('one.pdf')).toBeInTheDocument();
    expect(screen.getByText('two.jpg')).toBeInTheDocument();

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
    expect(screen.queryByLabelText('기업 인증 서류 선택')).not.toBeInTheDocument();
  });

  it('shows the backend validation message when application submission fails', async () => {
    applyCompanyCert.mockRejectedValue({
      response: { status: 400, data: { message: '파일 서명을 확인해주세요.' } },
    });
    renderPage();
    const input = await screen.findByLabelText('기업 인증 서류 선택');
    fireEvent.change(input, { target: { files: [sizedFile('certificate.pdf', MB)] } });

    fireEvent.click(screen.getByRole('button', { name: '신청하기' }));

    await waitFor(() => expect(applyCompanyCert).toHaveBeenCalledTimes(1));
    expect(await screen.findByRole('alert')).toHaveTextContent('파일 서명을 확인해주세요.');
  });
});
