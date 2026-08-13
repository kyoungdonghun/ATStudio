import { createBrowserRouter, Navigate, redirect, type RouteObject } from 'react-router-dom';
import { type ReactNode } from 'react';
import ProtectedRoute from '@/router/ProtectedRoute';
import SubscriberRoute from '@/router/SubscriberRoute';
import MainLayout from '@/layouts/MainLayout';
import AdminLayout from '@/layouts/AdminLayout';
import { safeStorage } from '@/utils/safeStorage';
import { createLazyPage } from '@/router/LazyRoute';

/* ── Lazy-loaded pages ── */

// Public
const HomePage = createLazyPage(() => import('@/pages/public/HomePage'));
const TrackListPage = createLazyPage(() => import('@/pages/public/TrackListPage'));
const TrackDetailPage = createLazyPage(() => import('@/pages/public/TrackDetailPage'));
const AlbumListImagePage = createLazyPage(() => import('@/pages/public/AlbumListImagePage'));
const AlbumListPage = createLazyPage(() => import('@/pages/public/AlbumListPage'));
const AlbumDetailPage = createLazyPage(() => import('@/pages/public/AlbumDetailPage'));
const SubscriptionPlanPage = createLazyPage(() => import('@/pages/public/SubscriptionPlanPage'));
const NoticeListPage = createLazyPage(() => import('@/pages/public/NoticeListPage'));
const NoticeDetailPage = createLazyPage(() => import('@/pages/public/NoticeDetailPage'));

// Auth
const LoginPage = createLazyPage(() => import('@/pages/auth/LoginPage'));
const SignupPage = createLazyPage(() => import('@/pages/auth/SignupPage'));
const EmailVerifyPage = createLazyPage(() => import('@/pages/auth/EmailVerifyPage'));
const PasswordResetPage = createLazyPage(() => import('@/pages/auth/PasswordResetPage'));
const SocialLoginPage = createLazyPage(() => import('@/pages/auth/SocialLoginPage'));
const SocialCompleteProfilePage = createLazyPage(
  () => import('@/pages/auth/SocialCompleteProfilePage'),
);

// Subscriber
const PlaylistListPage = createLazyPage(() => import('@/pages/subscriber/PlaylistListPage'));
const PlaylistDetailPage = createLazyPage(() => import('@/pages/subscriber/PlaylistDetailPage'));
const PlaylistEditPage = createLazyPage(() => import('@/pages/subscriber/PlaylistEditPage'));
const ProfilePage = createLazyPage(() => import('@/pages/subscriber/ProfilePage'));
const LikeListPage = createLazyPage(() => import('@/pages/subscriber/LikeListPage'));
const PlayHistoryPage = createLazyPage(() => import('@/pages/subscriber/PlayHistoryPage'));
const LicenseListPage = createLazyPage(() => import('@/pages/subscriber/LicenseListPage'));
const LicenseDetailPage = createLazyPage(() => import('@/pages/subscriber/LicenseDetailPage'));
const DownloadHistoryPage = createLazyPage(() => import('@/pages/subscriber/DownloadHistoryPage'));
const SubscriptionPaymentPage = createLazyPage(
  () => import('@/pages/subscriber/SubscriptionPaymentPage'),
);
const SubscriptionManagePage = createLazyPage(
  () => import('@/pages/subscriber/SubscriptionManagePage'),
);
const WhitelistChannelPage = createLazyPage(
  () => import('@/pages/subscriber/WhitelistChannelPage'),
);
const CompanyCertApplyPage = createLazyPage(
  () => import('@/pages/subscriber/CompanyCertApplyPage'),
);
const CompanyCertStatusPage = createLazyPage(
  () => import('@/pages/subscriber/CompanyCertStatusPage'),
);
const QuestionListPage = createLazyPage(() => import('@/pages/subscriber/QuestionListPage'));
const QuestionCreatePage = createLazyPage(() => import('@/pages/subscriber/QuestionCreatePage'));
const QuestionDetailPage = createLazyPage(() => import('@/pages/subscriber/QuestionDetailPage'));

// Creator (ADMIN-only in current spec)
const TrackUploadPage = createLazyPage(() => import('@/pages/creator/TrackUploadPage'));
const TrackEditPage = createLazyPage(() => import('@/pages/creator/TrackEditPage'));
const AlbumCreatePage = createLazyPage(() => import('@/pages/creator/AlbumCreatePage'));
const AlbumEditPage = createLazyPage(() => import('@/pages/creator/AlbumEditPage'));
const AlbumManagePage = createLazyPage(() => import('@/pages/creator/AlbumManagePage'));

// Admin
const DashboardPage = createLazyPage(() => import('@/pages/admin/DashboardPage'));
const UserManagePage = createLazyPage(() => import('@/pages/admin/UserManagePage'));
const AdminSubscriptionManagePage = createLazyPage(
  () => import('@/pages/admin/SubscriptionManagePage'),
);
const LicenseManagePage = createLazyPage(() => import('@/pages/admin/LicenseManagePage'));
const QuestionManagePage = createLazyPage(() => import('@/pages/admin/QuestionManagePage'));
const CompanyCertManagePage = createLazyPage(() => import('@/pages/admin/CompanyCertManagePage'));
const TagManagePage = createLazyPage(() => import('@/pages/admin/TagManagePage'));
const TrackManagePage = createLazyPage(() => import('@/pages/admin/TrackManagePage'));
const NoticeCreatePage = createLazyPage(() => import('@/pages/admin/NoticeCreatePage'));
const NoticeEditPage = createLazyPage(() => import('@/pages/admin/NoticeEditPage'));
const UserSubscriptionManagePage = createLazyPage(
  () => import('@/pages/admin/UserSubscriptionManagePage'),
);
const PaymentOperationsPage = createLazyPage(() => import('@/pages/admin/PaymentOperationsPage'));
const WhitelistChannelManagePage = createLazyPage(
  () => import('@/pages/admin/WhitelistChannelManagePage'),
);
const SiteSettingsPage = createLazyPage(() => import('@/pages/admin/SiteSettingsPage'));

// Error
const NotFoundPage = createLazyPage(() => import('@/pages/error/NotFoundPage'));
const ServerErrorPage = createLazyPage(() => import('@/pages/error/ServerErrorPage'));

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

function userPaymentOnly(element: ReactNode): ReactNode {
  return (
    <ProtectedRoute minRole="USER" maxRole="USER" deniedRedirect="/admin/payments">
      {element}
    </ProtectedRoute>
  );
}

function businessOnly(element: ReactNode): ReactNode {
  return (
    <ProtectedRoute minRole="USER" maxRole="USER" requiredUserType="BUSINESS" deniedRedirect="/">
      {element}
    </ProtectedRoute>
  );
}

/* Route declarations; see docs/ui/atstudio-front-list.md for the counting contract. */

export const routes: RouteObject[] = [
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
      { path: '/playlists/:playlistId/edit', element: subscriberOnly(<PlaylistEditPage />) },
      { path: '/profile', element: authRequired(<ProfilePage />) },
      { path: '/likes', element: authRequired(<LikeListPage />) },
      { path: '/play-history', element: authRequired(<PlayHistoryPage />) },
      { path: '/licenses', element: authRequired(<LicenseListPage />) },
      { path: '/licenses/:licenseId', element: authRequired(<LicenseDetailPage />) },
      { path: '/downloads', element: subscriberOnly(<DownloadHistoryPage />) },
      { path: '/subscriptions/checkout', element: userPaymentOnly(<SubscriptionPaymentPage />) },
      {
        path: '/subscriptions/checkout/success',
        element: userPaymentOnly(<SubscriptionPaymentPage />),
      },
      {
        path: '/subscriptions/checkout/fail',
        element: userPaymentOnly(<SubscriptionPaymentPage />),
      },
      { path: '/subscriptions/manage', element: authRequired(<SubscriptionManagePage />) },
      { path: '/whitelist-channels', element: authRequired(<WhitelistChannelPage />) },
      { path: '/company-certification/apply', element: businessOnly(<CompanyCertApplyPage />) },
      { path: '/company-certification/status', element: businessOnly(<CompanyCertStatusPage />) },
      {
        path: '/questions',
        element: authRequired(<QuestionListPage />),
        loader: () => {
          try {
            const raw = safeStorage.getItem('user');
            const user = raw ? JSON.parse(raw) : null;
            if (user?.role === 'ADMIN') return redirect('/admin/questions');
          } catch {
            /* ignore */
          }
          return null;
        },
      },
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

      /* ── Admin (14 routes) ── */
      { path: 'dashboard', element: <DashboardPage /> },
      { path: 'users', element: <UserManagePage /> },
      { path: 'subscriptions', element: <AdminSubscriptionManagePage /> },
      { path: 'licenses', element: <LicenseManagePage /> },
      { path: 'questions', element: <QuestionManagePage /> },
      { path: 'company-certifications', element: <CompanyCertManagePage /> },
      { path: 'tags', element: <TagManagePage /> },
      { path: 'track-manage', element: <TrackManagePage /> },
      { path: 'user-subscriptions', element: <UserSubscriptionManagePage /> },
      { path: 'payments', element: <PaymentOperationsPage /> },
      { path: 'whitelist-channels', element: <WhitelistChannelManagePage /> },
      { path: 'notices/new', element: <NoticeCreatePage /> },
      { path: 'notices/:noticeId/edit', element: <NoticeEditPage /> },
      { path: 'settings', element: <SiteSettingsPage /> },
    ],
  },
];

export const router = createBrowserRouter(routes);
