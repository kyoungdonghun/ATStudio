import { Link } from 'react-router-dom';

export default function NotFoundPage() {
  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: 'calc(100vh - var(--header-h) - var(--player-h))',
        padding: '32px var(--page-px)',
        textAlign: 'center',
      }}
    >
      <h1
        style={{
          fontSize: 72,
          fontWeight: 700,
          color: 'var(--text2)',
          lineHeight: 1,
          marginBottom: 12,
        }}
      >
        404
      </h1>
      <p style={{ fontSize: 16, color: 'var(--text1)', marginBottom: 24 }}>
        {'페이지를 찾을 수 없습니다'}
      </p>
      <Link
        to="/"
        style={{
          color: 'var(--accent)',
          fontSize: 14,
          fontWeight: 500,
          textDecoration: 'none',
        }}
      >
        {'홈으로 돌아가기'}
      </Link>
    </div>
  );
}
