import { RouterProvider } from 'react-router-dom';
import { router } from '@/router';
import { REACT_ROUTER_FUTURE } from '@/router/routerFuture';
import '@/styles/tokens.css';

export default function App() {
  return <RouterProvider router={router} future={REACT_ROUTER_FUTURE} />;
}
