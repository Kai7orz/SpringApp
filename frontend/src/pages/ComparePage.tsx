import { useState } from 'react';
import SqlEditor from '../components/SqlEditor';
import { queryApi } from '../services/api';
import type { QueryResult } from '../types';

export default function ComparePage() {
  const [queries, setQueries] = useState<string[]>(['', '']);
  const [results, setResults] = useState<QueryResult[]>([]);
  const [loading, setLoading] = useState(false);

  const addQuery = () => {
    if (queries.length < 5) {
      setQueries([...queries, '']);
    }
  };

  const removeQuery = (index: number) => {
    if (queries.length > 2) {
      setQueries(queries.filter((_, i) => i !== index));
    }
  };

  const updateQuery = (index: number, value: string) => {
    const newQueries = [...queries];
    newQueries[index] = value;
    setQueries(newQueries);
  };

  const handleCompare = async () => {
    const validQueries = queries.filter((q) => q.trim());
    if (validQueries.length < 2) return;

    setLoading(true);
    setResults([]);

    try {
      const data = await queryApi.compare(validQueries);
      setResults(data);
    } catch (err) {
      console.error('Compare failed:', err);
    } finally {
      setLoading(false);
    }
  };

  const getBestIndex = (): number => {
    if (results.length === 0) return -1;
    let bestIdx = 0;
    let bestTime = Infinity;
    results.forEach((r, idx) => {
      if (r.status === 'SUCCESS' && r.executionTimeMs !== null && r.executionTimeMs < bestTime) {
        bestTime = r.executionTimeMs;
        bestIdx = idx;
      }
    });
    return bestIdx;
  };

  const bestIdx = getBestIndex();

  return (
    <div className="space-y-6">
      <div className="bg-white rounded-lg shadow p-6">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-lg font-semibold text-gray-900">Compare Queries</h2>
          <button
            onClick={addQuery}
            disabled={queries.length >= 5}
            className="px-3 py-1 text-sm border border-gray-300 rounded hover:bg-gray-50 disabled:opacity-50"
          >
            + Add Query
          </button>
        </div>

        <div className="space-y-4">
          {queries.map((query, index) => (
            <div key={index} className="relative">
              <div className="flex items-center justify-between mb-2">
                <label className="text-sm font-medium text-gray-700">Query {index + 1}</label>
                {queries.length > 2 && (
                  <button
                    onClick={() => removeQuery(index)}
                    className="text-red-500 hover:text-red-700 text-sm"
                  >
                    Remove
                  </button>
                )}
              </div>
              <SqlEditor value={query} onChange={(v) => updateQuery(index, v)} height="120px" />
            </div>
          ))}
        </div>

        <div className="mt-6">
          <button
            onClick={handleCompare}
            disabled={loading || queries.filter((q) => q.trim()).length < 2}
            className="px-6 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50"
          >
            {loading ? 'Comparing...' : 'Compare Performance'}
          </button>
        </div>
      </div>

      {/* Comparison Results */}
      {results.length > 0 && (
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Comparison Results</h3>

          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                    Query
                  </th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                    Status
                  </th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                    Time (ms)
                  </th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                    Rows Returned
                  </th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                    Rows Scanned
                  </th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                    Index Used
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {results.map((result, idx) => (
                  <tr
                    key={idx}
                    className={idx === bestIdx ? 'bg-green-50' : result.status !== 'SUCCESS' ? 'bg-red-50' : ''}
                  >
                    <td className="px-4 py-3">
                      <div className="flex items-center">
                        <span className="text-sm font-medium">Query {idx + 1}</span>
                        {idx === bestIdx && (
                          <span className="ml-2 px-2 py-0.5 text-xs bg-green-500 text-white rounded">
                            Fastest
                          </span>
                        )}
                      </div>
                      <div className="text-xs text-gray-500 mt-1 truncate max-w-xs" title={result.originalSql}>
                        {result.originalSql}
                      </div>
                    </td>
                    <td className="px-4 py-3">
                      <span
                        className={`px-2 py-1 text-xs rounded ${
                          result.status === 'SUCCESS'
                            ? 'bg-green-100 text-green-800'
                            : result.status === 'TIMEOUT'
                            ? 'bg-yellow-100 text-yellow-800'
                            : 'bg-red-100 text-red-800'
                        }`}
                      >
                        {result.status}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-sm">
                      {result.executionTimeMs !== null ? `${result.executionTimeMs} ms` : '-'}
                    </td>
                    <td className="px-4 py-3 text-sm">
                      {result.rowsReturned?.toLocaleString() ?? '-'}
                    </td>
                    <td className="px-4 py-3 text-sm">
                      {result.rowsScanned?.toLocaleString() ?? '-'}
                    </td>
                    <td className="px-4 py-3 text-sm">
                      <span className={result.indexUsed ? 'text-green-600' : 'text-red-600'}>
                        {result.indexUsed || 'None'}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* Performance Chart (Simple bar comparison) */}
          <div className="mt-6">
            <h4 className="text-sm font-medium text-gray-700 mb-2">Execution Time Comparison</h4>
            <div className="space-y-2">
              {results.map((result, idx) => {
                const maxTime = Math.max(...results.map((r) => r.executionTimeMs ?? 0));
                const width = maxTime > 0 ? ((result.executionTimeMs ?? 0) / maxTime) * 100 : 0;
                return (
                  <div key={idx} className="flex items-center space-x-2">
                    <span className="w-20 text-sm text-gray-600">Query {idx + 1}</span>
                    <div className="flex-1 bg-gray-100 rounded h-6 relative">
                      <div
                        className={`h-full rounded ${idx === bestIdx ? 'bg-green-500' : 'bg-blue-500'}`}
                        style={{ width: `${width}%` }}
                      />
                      <span className="absolute right-2 top-0.5 text-xs text-gray-700">
                        {result.executionTimeMs ?? 0} ms
                      </span>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
