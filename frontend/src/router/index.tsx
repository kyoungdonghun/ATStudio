import { createBrowserRouter, type RouteObject } from 'react-router-dom';
import { type ReactNode } from 'react';
import ProtectedRoute from '@/router/ProtectedRoute';
import MainLayout from '@/layouts/MainLayout';
import AdminLayout from '@/layouts/AdminLayout';

/* ── Page imports ── */

// Public
import HomePage from '@/pages/public/HomePage';
import TrackListPage from '@/pages/public/TrackListPage';
import TrackDetailPage from '@/pages/public/TrackDetailPage';
import AlbumListImagePage from '@/pages/public/AlbumListImagePage';
import AlbumListPage from '@/pages/public/AlbumListPage';
import AlbumDetailPage from '@/pages/public/AlbumDetailPage';
import SubscriptionPlanPage from '@/pages/public/SubscriptionPlanPage';
import NoticeListPage from '@/pages/public/NoticeListPage';
import NoticeDetailPage from '@/pages/public/NoticeDetailPage';

// Auth
import LoginPage from '@/pages/auth/LoginPage';
import SignupPage from '@/pages/auth/SignupPage';
import EmailVerifyPage from '@/pages/auth/EmailVerifyPage';
import PasswordResetPage from '@/pages/auth/PasswordResetPage';
import SocialLoginPage from '@/pages/auth/SocialLoginPage';
import SocialCompleteProfilePage from '@/pages/auth/SocialCompleteProfilePage';

// Subscriber
import PlaylistListPage from '@/pages/subscriber/PlaylistListPage';
import PlaylistDetailPage from '@/pages/subscriber/PlaylistDetailPage';
import PlaylistCreatePage from '@/pages/subscriber/PlaylistCreatePage';
import PlaylistEditPage from '@/pages/subscriber/PlaylistEditPage';
import ProfilePage from '@/pages/subscriber/ProfilePage';
import LikeListPage from '@/pages/subscriber/LikeListPage';
import PlayHistoryPage from '@/pages/subscriber/PlayHistoryPage';
import LicenseListPage from '@/pages/subscriber/LicenseListPage';
import LicenseDetailPage from '@/pages/subscriber/LicenseDetailPage';
import DownloadQueuePage from '@/pages/subscriber/DownloadQueuePage';
import SubscriptionPaymentPage from '@/pages/subscriber/SubscriptionPaymentPage';
import SubscriptionManagePage from '@/pages/subscriber/SubscriptionManagePage';
import WhitelistChannelPage from '@/pages/subscriber/WhitelistChannelPage';
import CompanyCertApplyPage from '@/pages/subscriber/CompanyCertApplyPage';
import CompanyCertStatusPage from '@/pages/subscriber/CompanyCertStatusPage';
import QuestionListPage from '@/pages/subscriber/QuestionListPage';
import QuestionCreatePage from '@/pages/subscriber/QuestionCreatePage';
import QuestionDetailPage from '@/pages/subscriber/QuestionDetailPage';

// Creator (ADMIN-only in current spec)
import TrackUploadPage from '@/pages/creator/TrackUploadPage';
import TrackEditPage from '@/pages/creator/TrackEditPage';
import AlbumCreatePage from '@/pages/creator/AlbumCreatePage';
import AlbumEditPage from '@/pages/creator/AlbumEditPage';
import AlbumManagePage from '@/pages/creator/AlbumManagePage';

// Admin
import DashboardPage from '@/pages/admin/DashboardPage';
import UserManagePage from '@/pages/admin/UserManagePage';
import AdminSubscriptionManagePage from '@/pages/admin/SubscriptionManagePage';
import LicenseManagePage from '@/pages/admin/LicenseManagePage';
import QuestionManagePage from '@/pages/admin/QuestionManagePage';
import CompanyCertManagePage from '@/pages/admin/CompanyCertManagePage';
import TagManagePage from '@/pages/admin/TagManagePage';
import TrackManagePage from '@/pages/admin/TrackManagePage';
import NoticeCreatePage from '@/pages/admin/NoticeCreatePage';
import NoticeEditPage from '@/pages/admin/NoticeEditPage';
import UserSubscriptionManagePage from '@/pages/admin/UserSubscriptionManagePage';

// Error
import NotFoundPage from '@/pages/error/NotFoundPage';
import ServerErrorPage from '@/pages/error/ServerErrorPage';

/* ── Guard helpers ── */

function authRequired(element: ReactNode): ReactNode {
  return <ProtectedRoute minRole="USER">{element}</ProtectedRoute>;
}

function adminOnly(element: ReactNode): ReactNode {
  return <ProtectedRoute minRole="ADMIN">{element}</ProtectedRoute>;
}

/* ── Route definitions (48 screens) ── */

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
      { path: '/playlists', element: authRequired(<PlaylistListPage />) },
      { path: '/playlists/:playlistId', element: authRequired(<PlaylistDetailPage />) },
      { path: '/playlists/new', element: authRequired(<PlaylistCreatePage />) },
      { path: '/playlists/:playlistId/edit', element: authRequired(<PlaylistEditPage />) },
      { path: '/profile', element: authRequired(<ProfilePage />) },
      { path: '/likes', element: authRequired(<LikeListPage />) },
      { path: '/play-history', element: authRequired(<PlayHistoryPage />) },
      { path: '/licenses', element: authRequired(<LicenseListPage />) },
      { path: '/licenses/:licenseId', element: authRequired(<LicenseDetailPage />) },
      { path: '/download-queue', element: authRequired(<DownloadQueuePage />) },
      { path: '/subscriptions/payment', element: authRequired(<SubscriptionPaymentPage />) },
      { path: '/subscriptions/manage', element: authRequired(<SubscriptionManagePage />) },
      { path: '/whitelist-channels', element: authRequired(<WhitelistChannelPage />) },
      { path: '/company-certification/apply', element: authRequired(<CompanyCertApplyPage />) },
      { path: '/company-certification/status', element: authRequired(<CompanyCertStatusPage />) },
      { path: '/questions', element: authRequired(<QuestionListPage />) },
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
      { index: true, element: <DashboardPage /> },
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
    ],
  },
];

export const router = createBrowserRouter(routes);
