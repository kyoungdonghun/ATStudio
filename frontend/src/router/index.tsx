import { createBrowserRouter, Navigate, redirect, type RouteObject } from 'react-router-dom';
import { lazy, Suspense, type ComponentType, type ReactNode } from 'react';
import ProtectedRoute from '@/router/ProtectedRoute';
import SubscriberRoute from '@/router/SubscriberRoute';
import MainLayout from '@/layouts/MainLayout';
import AdminLayout from '@/layouts/AdminLayout';
import { safeStorage } from '@/utils/safeStorage';

type PageModule = { default: ComponentType };

const routeFallback = (
  <div
    style={{
      minHeight: '40vh',
      display: 'grid',
      placeItems: 'center',
      color: 'var(--text1, #6b7280)',
    }}
  >
    로딩 중...
  </div>
);

function lazyPage(loader: () => Promise<PageModule>): ComponentType {
  const Component = lazy(loader);
  return function LazyPage() {
    return (
      <Suspense fallback={routeFallback}>
        <Component />
      </Suspense>
    );
  };
}

/* ── Lazy-loaded pages ── */

// Public
const HomePage = lazyPage(() => import('@/pages/public/HomePage'));
const TrackListPage = lazyPage(() => import('@/pages/public/TrackListPage'));
const TrackDetailPage = lazyPage(() => import('@/pages/public/TrackDetailPage'));
const AlbumListImagePage = lazyPage(() => import('@/pages/public/AlbumListImagePage'));
const AlbumListPage = lazyPage(() => import('@/pages/public/AlbumListPage'));
const AlbumDetailPage = lazyPage(() => import('@/pages/public/AlbumDetailPage'));
const SubscriptionPlanPage = lazyPage(() => import('@/pages/public/SubscriptionPlanPage'));
const NoticeListPage = lazyPage(() => import('@/pages/public/NoticeListPage'));
const NoticeDetailPage = lazyPage(() => import('@/pages/public/NoticeDetailPage'));

// Auth
const LoginPage = lazyPage(() => import('@/pages/auth/LoginPage'));
const SignupPage = lazyPage(() => import('@/pages/auth/SignupPage'));
const EmailVerifyPage = lazyPage(() => import('@/pages/auth/EmailVerifyPage'));
const PasswordResetPage = lazyPage(() => import('@/pages/auth/PasswordResetPage'));
const SocialLoginPage = lazyPage(() => import('@/pages/auth/SocialLoginPage'));
const SocialCompleteProfilePage = lazyPage(() => import('@/pages/auth/SocialCompleteProfilePage'));

// Subscriber
const PlaylistListPage = lazyPage(() => import('@/pages/subscriber/PlaylistListPage'));
const PlaylistDetailPage = lazyPage(() => import('@/pages/subscriber/PlaylistDetailPage'));
const PlaylistCreatePage = lazyPage(() => import('@/pages/subscriber/PlaylistCreatePage'));
const PlaylistEditPage = lazyPage(() => import('@/pages/subscriber/PlaylistEditPage'));
const ProfilePage = lazyPage(() => import('@/pages/subscriber/ProfilePage'));
const LikeListPage = lazyPage(() => import('@/pages/subscriber/LikeListPage'));
const PlayHistoryPage = lazyPage(() => import('@/pages/subscriber/PlayHistoryPage'));
const LicenseListPage = lazyPage(() => import('@/pages/subscriber/LicenseListPage'));
const LicenseDetailPage = lazyPage(() => import('@/pages/subscriber/LicenseDetailPage'));
const DownloadHistoryPage = lazyPage(() => import('@/pages/subscriber/DownloadQueuePage'));
const SubscriptionPaymentPage = lazyPage(() => import('@/pages/subscriber/SubscriptionPaymentPage'));
const SubscriptionManagePage = lazyPage(() => import('@/pages/subscriber/SubscriptionManagePage'));
const WhitelistChannelPage = lazyPage(() => import('@/pages/subscriber/WhitelistChannelPage'));
const CompanyCertApplyPage = lazyPage(() => import('@/pages/subscriber/CompanyCertApplyPage'));
const CompanyCertStatusPage = lazyPage(() => import('@/pages/subscriber/CompanyCertStatusPage'));
const QuestionListPage = lazyPage(() => import('@/pages/subscriber/QuestionListPage'));
const QuestionCreatePage = lazyPage(() => import('@/pages/subscriber/QuestionCreatePage'));
const QuestionDetailPage = lazyPage(() => import('@/pages/subscriber/QuestionDetailPage'));

// Creator (ADMIN-only in current spec)
const TrackUploadPage = lazyPage(() => import('@/pages/creator/TrackUploadPage'));
const TrackEditPage = lazyPage(() => import('@/pages/creator/TrackEditPage'));
const AlbumCreatePage = lazyPage(() => import('@/pages/creator/AlbumCreatePage'));
const AlbumEditPage = lazyPage(() => import('@/pages/creator/AlbumEditPage'));
const AlbumManagePage = lazyPage(() => import('@/pages/creator/AlbumManagePage'));

// Admin
const DashboardPage = lazyPage(() => import('@/pages/admin/DashboardPage'));
const UserManagePage = lazyPage(() => import('@/pages/admin/UserManagePage'));
const AdminSubscriptionManagePage = lazyPage(() => import('@/pages/admin/SubscriptionManagePage'));
const LicenseManagePage = lazyPage(() => import('@/pages/admin/LicenseManagePage'));
const QuestionManagePage = lazyPage(() => import('@/pages/admin/QuestionManagePage'));
const CompanyCertManagePage = lazyPage(() => import('@/pages/admin/CompanyCertManagePage'));
const TagManagePage = lazyPage(() => import('@/pages/admin/TagManagePage'));
const TrackManagePage = lazyPage(() => import('@/pages/admin/TrackManagePage'));
const NoticeCreatePage = lazyPage(() => import('@/pages/admin/NoticeCreatePage'));
const NoticeEditPage = lazyPage(() => import('@/pages/admin/NoticeEditPage'));
const UserSubscriptionManagePage = lazyPage(() => import('@/pages/admin/UserSubscriptionManagePage'));
const SiteSettingsPage = lazyPage(() => import('@/pages/admin/SiteSettingsPage'));

// Error
const NotFoundPage = lazyPage(() => import('@/pages/error/NotFoundPage'));
const ServerErrorPage = lazyPage(() => import('@/pages/error/ServerErrorPage'));

/* ── Guard helpers ── */

function authRequired(element: ReactNode): ReactNode {
  return <ProtectedRoute minRole="USER">{element}</ProtectedRoute>;
}

function subscriberOnly(element: ReactNode): ReactNode {
  return <SubscriberRoute>{element}</SubscriberRoute>;
}

function adminOnly(element: ReactNode): ReactNode {
  return <ProtectedRoute minRole="ADMIN">{element}</ProtectedRoute>;
}

/* ── Route definitions (current route paths: 52 + admin index redirect) ── */

const routes: RouteObject[] = [
  {
    element: <MainLayout />,
    children: [
      /* ── Public (9 routes) ── */
      { path: '/', element: <HomePage /> },
      { path: '/tracks', element: <TrackListPage /> },
      { path: '/tracks/:trackId', element: <TrackDetailPage /> },
      { path: '/albums', element: <AlbumListImagePage /> },
      { path: '/albums/list', element: <AlbumListPage /> },
      { path: '/albums/:albumId', element: <AlbumDetailPage /> },
      { path: '/subscriptions', element: <SubscriptionPlanPage /> },
      { path: '/notices', element: <NoticeListPage /> },
      { path: '/notices/:noticeId', element: <NoticeDetailPage /> },

      /* ── Auth (6 routes) ── */
      { path: '/login', element: <LoginPage /> },
      { path: '/signup', element: <SignupPage /> },
      { path: '/email-verify', element: <EmailVerifyPage /> },
      { path: '/password-reset', element: <PasswordResetPage /> },
      { path: '/social-login/:provider', element: <SocialLoginPage /> },
      { path: '/complete-profile', element: authRequired(<SocialCompleteProfilePage />) },

      /* ── Subscriber / auth-required (19 routes) ── */
      { path: '/playlists', element: subscriberOnly(<PlaylistListPage />) },
      { path: '/playlists/:playlistId', element: subscriberOnly(<PlaylistDetailPage />) },
      { path: '/playlists/new', element: subscriberOnly(<PlaylistCreatePage />) },
      { path: '/playlists/:playlistId/edit', element: subscriberOnly(<PlaylistEditPage />) },
      { path: '/profile', element: authRequired(<ProfilePage />) },
      { path: '/likes', element: authRequired(<LikeListPage />) },
      { path: '/play-history', element: authRequired(<PlayHistoryPage />) },
      { path: '/licenses', element: authRequired(<LicenseListPage />) },
      { path: '/licenses/:licenseId', element: authRequired(<LicenseDetailPage />) },
      { path: '/download-queue', element: subscriberOnly(<DownloadHistoryPage />) },
      { path: '/subscriptions/payment', element: authRequired(<SubscriptionPaymentPage />) },
      { path: '/subscriptions/payment/success', element: authRequired(<SubscriptionPaymentPage />) },
      { path: '/subscriptions/payment/fail', element: authRequired(<SubscriptionPaymentPage />) },
      { path: '/subscriptions/manage', element: authRequired(<SubscriptionManagePage />) },
      { path: '/whitelist-channels', element: subscriberOnly(<WhitelistChannelPage />) },
      { path: '/company-certification/apply', element: authRequired(<CompanyCertApplyPage />) },
      { path: '/company-certification/status', element: authRequired(<CompanyCertStatusPage />) },
      { path: '/questions', element: authRequired(<QuestionListPage />), loader: () => {
        try {
          const raw = safeStorage.getItem('user');
          const user = raw ? JSON.parse(raw) : null;
          if (user?.role === 'ADMIN') return redirect('/admin/questions');
        } catch { /* ignore */ }
        return null;
      } },
      { path: '/questions/new', element: authRequired(<QuestionCreatePage />) },
      { path: '/questions/:questionId', element: authRequired(<QuestionDetailPage />) },

      /* ── Error (2 routes) ── */
      { path: '/error', element: <ServerErrorPage /> },
      { path: '*', element: <NotFoundPage /> },
    ],
  },

  /* ── Admin layout (sidebar + topbar, no PlayerBar) ── */
  {
    path: '/admin',
    element: adminOnly(<AdminLayout />),
    children: [
      /* ── Creator / Admin (5 routes) ── */
      { index: true, element: <Navigate to="dashboard" replace /> },
      { path: 'tracks/upload', element: <TrackUploadPage /> },
      { path: 'tracks/:trackId/edit', element: <TrackEditPage /> },
      { path: 'albums', element: <AlbumManagePage /> },
      { path: 'albums/new', element: <AlbumCreatePage /> },
      { path: 'albums/:albumId/edit', element: <AlbumEditPage /> },

      /* ── Admin (9 routes) ── */
      { path: 'dashboard', element: <DashboardPage /> },
      { path: 'users', element: <UserManagePage /> },
      { path: 'subscriptions', element: <AdminSubscriptionManagePage /> },
      { path: 'licenses', element: <LicenseManagePage /> },
      { path: 'questions', element: <QuestionManagePage /> },
      { path: 'company-certifications', element: <CompanyCertManagePage /> },
      { path: 'tags', element: <TagManagePage /> },
      { path: 'track-manage', element: <TrackManagePage /> },
      { path: 'user-subscriptions', element: <UserSubscriptionManagePage /> },
      { path: 'notices/new', element: <NoticeCreatePage /> },
      { path: 'notices/:noticeId/edit', element: <NoticeEditPage /> },
      { path: 'settings', element: <SiteSettingsPage /> },
    ],
  },
];

export const router = createBrowserRouter(routes);
