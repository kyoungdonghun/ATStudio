import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

export default function PlaylistCreatePage() {
  const navigate = useNavigate();
  useEffect(() => {
    navigate('/playlists', { replace: true, state: { openCreate: true } });
  }, [navigate]);
  return null;
}
