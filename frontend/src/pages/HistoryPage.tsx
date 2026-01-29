import { useState, useEffect } from 'react';
import { historyApi } from '../services/api';
import type { HistoryItem, PagedResponse } from '../types';

export default function HistoryPage() {
  const [history, setHistory] = useState<PagedResponse<HistoryItem> | null>(null);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);

  useEffect(() => {
    loadHistory();
  }, [page]);

  const loadHistory = async () => {
    setLoading(true);
    try {
      const data = await historyApi.getHistory(page, 20);
      setHistory(data);
    } catch (err) {
      console.error('Failed to load history:', err);
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleString();
  };

  const getStatusBadge = (status: string) => {
    const colors: Record<string, string> = {
      SUCCESS: 'bg-green-100 text-green-800',
      ERROR: 'bg-red-100 text-red-800',
      TIMEOUT: 'bg-yellow-100 text-yellow-800',
    };
    return colors[status] || 'bg-gray-100 text-gray-800';
  };

  if (loading && !history) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow">
      <div className="px-6 py-4 border-b border-gray-200">
        <h2 className="text-lg font-semibold text-gray-900">Query History</h2>
        <p className="text-sm text-gray-500">
          {history?.totalItems ?? 0} total queries executed
        </p>
      </div>

      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                SQL
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Status
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Time
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Rows
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Index
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Date
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {history?.items.map((item) => (
              <tr key={item.id} className="hover:bg-gray-50">
                <td className="px-6 py-4">
                  <div
                    className="text-sm text-gray-900 max-w-md truncate font-mono"
                    title={item.sqlText}
                  >
                    {item.sqlText}
                  </div>
                </td>
                <td className="px-6 py-4">
                  <span className={`px-2 py-1 text-xs rounded ${getStatusBadge(item.status)}`}>
                    {item.status}
                  </span>
                </td>
                <td className="px-6 py-4 text-sm text-gray-500">
                  {item.executionTimeMs !== null ? `${item.executionTimeMs} ms` : '-'}
                </td>
                <td className="px-6 py-4 text-sm text-gray-500">
                  {item.rowsReturned !== null ? item.rowsReturned.toLocaleString() : '-'}
                </td>
                <td className="px-6 py-4 text-sm">
                  <span className={item.indexUsed ? 'text-green-600' : 'text-red-600'}>
                    {item.indexUsed || 'None'}
                  </span>
                </td>
                <td className="px-6 py-4 text-sm text-gray-500">{formatDate(item.createdAt)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Pagination */}
      {history && history.totalPages > 1 && (
        <div className="px-6 py-4 border-t border-gray-200 flex items-center justify-between">
          <div className="text-sm text-gray-500">
            Page {history.page + 1} of {history.totalPages}
          </div>
          <div className="flex space-x-2">
            <button
              onClick={() => setPage(page - 1)}
              disabled={page === 0}
              className="px-3 py-1 border rounded text-sm disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
            >
              Previous
            </button>
            <button
              onClick={() => setPage(page + 1)}
              disabled={page >= history.totalPages - 1}
              className="px-3 py-1 border rounded text-sm disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
            >
              Next
            </button>
          </div>
        </div>
      )}

      {history?.items.length === 0 && (
        <div className="px-6 py-12 text-center text-gray-500">No query history yet</div>
      )}
    </div>
  );
}
