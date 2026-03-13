import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

export default function PlaylistImageListPage() {
  const navigate = useNavigate();
  useEffect(() => {
    navigate('/playlists', { replace: true });
  }, [navigate]);
  return null;
}
