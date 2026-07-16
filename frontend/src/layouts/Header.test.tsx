import { render, screen, within } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it } from 'vitest';
import Header from '@/layouts/Header';
import { useAuthStore } from '@/store/authStore';
import { useThemeStore } from '@/store/themeStore';

describe('Header search accessibility', () => {
  beforeEach(() => {
    useAuthStore.setState({ accessToken: null, user: null, role: 'GUEST' });
    useThemeStore.setState({ theme: 'dark' });
  });

  it('provides explicit labels for desktop and mobile search', () => {
    render(
      <MemoryRouter>
        <Header />
      </MemoryRouter>,
    );

    const desktopSearch = screen.getByRole('search', { name: '곡 검색' });
    const mobileSearchInput = screen.getByLabelText('모바일 곡 제목 및 용도 검색');

    const desktopSearchInput = within(desktopSearch).getByRole('textbox', {
      name: '곡 제목 및 용도 검색',
    });

    expect(desktopSearchInput).toBe(screen.getByLabelText('곡 제목 및 용도 검색'));
    expect(desktopSearchInput).toHaveAttribute('placeholder', '곡 제목, 용도 검색');
    expect(mobileSearchInput).toHaveAttribute('type', 'text');
    expect(mobileSearchInput).toHaveAttribute('placeholder', '곡 제목, 용도 검색');
    expect(mobileSearchInput.closest('form')).toHaveAttribute('aria-label', '모바일 곡 검색');
  });
});
