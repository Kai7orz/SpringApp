import { Link, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Layout() {
  const { user, logout, isAdmin } = useAuth();
  const location = useLocation();

  const navLinks = [
    { path: '/query', label: 'SQL Editor' },
    { path: '/query/compare', label: 'Compare' },
    { path: '/history', label: 'History' },
  ];

  if (isAdmin) {
    navLinks.push({ path: '/admin', label: 'Admin' });
  }

  const isActive = (path: string) => location.pathname.startsWith(path);

  return (
    <div className="min-h-screen flex flex-col">
      <header className="bg-gray-800 text-white">
        <div className="max-w-7xl mx-auto px-4 py-3 flex items-center justify-between">
          <div className="flex items-center space-x-8">
            <h1 className="text-xl font-bold">SQL Performance Tuning</h1>
            <nav className="flex space-x-4">
              {navLinks.map((link) => (
                <Link
                  key={link.path}
                  to={link.path}
                  className={`px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                    isActive(link.path)
                      ? 'bg-gray-900 text-white'
                      : 'text-gray-300 hover:bg-gray-700 hover:text-white'
                  }`}
                >
                  {link.label}
                </Link>
              ))}
            </nav>
          </div>
          <div className="flex items-center space-x-4">
            <span className="text-sm text-gray-300">
              {user?.username}
              {isAdmin && (
                <span className="ml-2 px-2 py-0.5 text-xs bg-yellow-600 rounded">ADMIN</span>
              )}
            </span>
            <button
              onClick={logout}
              className="px-3 py-1 text-sm bg-red-600 hover:bg-red-700 rounded transition-colors"
            >
              Logout
            </button>
          </div>
        </div>
      </header>

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 py-6">
        <Outlet />
      </main>

      <footer className="bg-gray-100 border-t">
        <div className="max-w-7xl mx-auto px-4 py-3 text-center text-sm text-gray-600">
          SQL Performance Tuning Learning App
        </div>
      </footer>
    </div>
  );
}
