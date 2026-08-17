import { RouterProvider } from 'react-router-dom';
import { router } from '@/router';
import '@/styles/tokens.css';

export default function App() {
  return <RouterProvider router={router} />;
}
