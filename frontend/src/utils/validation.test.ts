import { describe, expect, it } from 'vitest';
import {
  CERT_DOC_ACCEPT,
  CERT_DOC_EXTENSIONS,
  validateCompanyCertFileSelection,
} from '@/utils/validation';

const MB = 1024 * 1024;

function file(name: string, size: number): File {
  const testFile = new File(['x'], name);
  Object.defineProperty(testFile, 'size', { value: size });
  return testFile;
}

describe('company certification document validation', () => {
  it('keeps the frontend extension contract aligned to PDF/JPG/JPEG/PNG', () => {
    expect(CERT_DOC_ACCEPT).toBe('.pdf,.jpg,.jpeg,.png');
    expect(CERT_DOC_EXTENSIONS).toEqual(['pdf', 'jpg', 'jpeg', 'png']);
  });

  it('rejects an extension outside the backend contract', () => {
    expect(validateCompanyCertFileSelection([], [file('certificate.hwp', MB)])).toContain(
      'PDF, JPG, JPEG, PNG',
    );
  });

  it('rejects an original filename longer than 255 characters', () => {
    const longName = `${'a'.repeat(252)}.pdf`;

    expect(validateCompanyCertFileSelection([], [file(longName, MB)])).toContain(
      '파일 이름은 255자 이하여야 합니다',
    );
  });

  it('rejects an empty file and a file over 20MB', () => {
    expect(validateCompanyCertFileSelection([], [file('empty.pdf', 0)])).toContain(
      '내용이 비어 있는',
    );
    expect(validateCompanyCertFileSelection([], [file('large.pdf', 20 * MB + 1)])).toContain(
      '파일당 최대 20MB',
    );
  });

  it('enforces count and 50MB total across existing and newly selected files', () => {
    const tenFiles = Array.from({ length: 10 }, (_, index) => file(`${index}.pdf`, MB));
    expect(validateCompanyCertFileSelection(tenFiles, [file('extra.pdf', MB)])).toContain(
      '최대 10개',
    );

    const existing = [file('one.pdf', 18 * MB), file('two.jpg', 18 * MB)];
    expect(validateCompanyCertFileSelection(existing, [file('three.png', 15 * MB)])).toContain(
      '전체 용량은 50MB',
    );
  });
});
