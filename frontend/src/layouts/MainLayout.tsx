import { Outlet } from 'react-router-dom';
import Header from '@/layouts/Header';
import PlayerBar from '@/layouts/PlayerBar';
import styles from './MainLayout.module.css';

export default function MainLayout() {
  return (
    <div className={styles.layout}>
      <Header />
      <main className={styles.main}>
        <Outlet />
      </main>
      <PlayerBar />
    </div>
  );
}
