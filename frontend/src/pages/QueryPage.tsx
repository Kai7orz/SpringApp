import { useState } from 'react';
import SqlEditor from '../components/SqlEditor';
import ResultTable from '../components/ResultTable';
import PerformanceMetrics from '../components/PerformanceMetrics';
import { queryApi } from '../services/api';
import type { QueryResult } from '../types';

const SAMPLE_QUERIES = [
  'SELECT * FROM sample_customers LIMIT 10',
  'SELECT * FROM sample_products WHERE category = "Electronics" LIMIT 20',
  'SELECT c.first_name, c.last_name, COUNT(o.id) as order_count FROM sample_customers c LEFT JOIN sample_orders o ON c.id = o.customer_id GROUP BY c.id LIMIT 10',
  'SELECT p.name, SUM(oi.quantity) as total_sold FROM sample_products p JOIN sample_order_items oi ON p.id = oi.product_id GROUP BY p.id ORDER BY total_sold DESC LIMIT 10',
];

export default function QueryPage() {
  const [sql, setSql] = useState('');
  const [result, setResult] = useState<QueryResult | null>(null);
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState<'results' | 'explain'>('results');

  const handleExecute = async () => {
    if (!sql.trim()) return;
    setLoading(true);
    setResult(null);

    try {
      const data = await queryApi.execute(sql);
      setResult(data);
    } catch (err: unknown) {
      if (err && typeof err === 'object' && 'response' in err) {
        const axiosErr = err as { response?: { data?: { message?: string } } };
        setResult({
          status: 'ERROR',
          originalSql: sql,
          processedSql: null,
          columns: null,
          data: null,
          executionTimeMs: null,
          rowsReturned: null,
          rowsScanned: null,
          indexUsed: null,
          explainResult: null,
          errorMessage: axiosErr.response?.data?.message || 'Query failed',
        });
      }
    } finally {
      setLoading(false);
    }
  };

  const handleExplainOnly = async () => {
    if (!sql.trim()) return;
    setLoading(true);

    try {
      const data = await queryApi.explain(sql);
      if (data.success) {
        setResult({
          status: 'SUCCESS',
          originalSql: sql,
          processedSql: sql,
          columns: null,
          data: null,
          executionTimeMs: null,
          rowsReturned: null,
          rowsScanned: data.rowsScanned,
          indexUsed: data.indexUsed,
          explainResult: data.explainData,
          errorMessage: null,
        });
        setActiveTab('explain');
      } else {
        setResult({
          status: 'ERROR',
          originalSql: sql,
          processedSql: null,
          columns: null,
          data: null,
          executionTimeMs: null,
          rowsReturned: null,
          rowsScanned: null,
          indexUsed: null,
          explainResult: null,
          errorMessage: data.errorMessage,
        });
      }
    } catch (err: unknown) {
      if (err && typeof err === 'object' && 'response' in err) {
        const axiosErr = err as { response?: { data?: { message?: string } } };
        setResult({
          status: 'ERROR',
          originalSql: sql,
          processedSql: null,
          columns: null,
          data: null,
          executionTimeMs: null,
          rowsReturned: null,
          rowsScanned: null,
          indexUsed: null,
          explainResult: null,
          errorMessage: axiosErr.response?.data?.message || 'EXPLAIN failed',
        });
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="bg-white rounded-lg shadow p-6">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-lg font-semibold text-gray-900">SQL Editor</h2>
          <div className="flex space-x-2">
            <select
              className="text-sm border rounded px-2 py-1"
              onChange={(e) => setSql(e.target.value)}
              value=""
            >
              <option value="">Sample Queries...</option>
              {SAMPLE_QUERIES.map((q, idx) => (
                <option key={idx} value={q}>
                  {q.length > 60 ? q.substring(0, 60) + '...' : q}
                </option>
              ))}
            </select>
          </div>
        </div>

        <SqlEditor value={sql} onChange={setSql} height="200px" />

        <div className="mt-4 flex space-x-3">
          <button
            onClick={handleExecute}
            disabled={loading || !sql.trim()}
            className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {loading ? 'Executing...' : 'Execute (F5)'}
          </button>
          <button
            onClick={handleExplainOnly}
            disabled={loading || !sql.trim()}
            className="px-4 py-2 bg-gray-600 text-white rounded hover:bg-gray-700 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            EXPLAIN Only
          </button>
          <button
            onClick={() => {
              setSql('');
              setResult(null);
            }}
            className="px-4 py-2 border border-gray-300 rounded hover:bg-gray-50"
          >
            Clear
          </button>
        </div>

        {result?.processedSql && result.processedSql !== result.originalSql && (
          <div className="mt-3 p-2 bg-yellow-50 border border-yellow-200 rounded text-sm">
            <span className="font-medium">Modified SQL:</span> {result.processedSql}
          </div>
        )}
      </div>

      {/* Results Section */}
      {result && (
        <div className="bg-white rounded-lg shadow p-6">
          {result.status === 'ERROR' || result.status === 'TIMEOUT' ? (
            <div className="bg-red-50 border border-red-200 rounded p-4">
              <h3 className="font-medium text-red-800">
                {result.status === 'TIMEOUT' ? 'Query Timeout' : 'Error'}
              </h3>
              <p className="mt-1 text-red-600">{result.errorMessage}</p>
            </div>
          ) : (
            <>
              {/* Tabs */}
              <div className="border-b border-gray-200 mb-4">
                <nav className="flex space-x-8">
                  <button
                    onClick={() => setActiveTab('results')}
                    className={`py-2 px-1 border-b-2 font-medium text-sm ${
                      activeTab === 'results'
                        ? 'border-blue-500 text-blue-600'
                        : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                    }`}
                  >
                    Results {result.rowsReturned !== null && `(${result.rowsReturned})`}
                  </button>
                  <button
                    onClick={() => setActiveTab('explain')}
                    className={`py-2 px-1 border-b-2 font-medium text-sm ${
                      activeTab === 'explain'
                        ? 'border-blue-500 text-blue-600'
                        : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                    }`}
                  >
                    Performance
                  </button>
                </nav>
              </div>

              {activeTab === 'results' && result.columns && result.data && (
                <ResultTable columns={result.columns} data={result.data} />
              )}

              {activeTab === 'explain' && <PerformanceMetrics result={result} />}
            </>
          )}
        </div>
      )}
    </div>
  );
}
