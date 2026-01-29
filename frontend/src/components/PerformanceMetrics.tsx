import type { QueryResult, ExplainRow } from '../types';

interface PerformanceMetricsProps {
  result: QueryResult;
}

export default function PerformanceMetrics({ result }: PerformanceMetricsProps) {
  if (result.status !== 'SUCCESS') {
    return null;
  }

  return (
    <div className="space-y-4">
      {/* Summary Metrics */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <MetricCard
          label="Execution Time"
          value={`${result.executionTimeMs ?? 0} ms`}
          color={getTimeColor(result.executionTimeMs ?? 0)}
        />
        <MetricCard
          label="Rows Returned"
          value={result.rowsReturned?.toLocaleString() ?? '0'}
          color="blue"
        />
        <MetricCard
          label="Rows Scanned"
          value={result.rowsScanned?.toLocaleString() ?? 'N/A'}
          color={getScanColor(result.rowsScanned ?? 0, result.rowsReturned ?? 0)}
        />
        <MetricCard
          label="Index Used"
          value={result.indexUsed ?? 'None'}
          color={result.indexUsed ? 'green' : 'red'}
        />
      </div>

      {/* EXPLAIN Result */}
      {result.explainResult && result.explainResult.length > 0 && (
        <div className="mt-4">
          <h4 className="text-sm font-medium text-gray-700 mb-2">EXPLAIN Analysis</h4>
          <ExplainTable data={result.explainResult} />
        </div>
      )}
    </div>
  );
}

interface MetricCardProps {
  label: string;
  value: string;
  color: string;
}

function MetricCard({ label, value, color }: MetricCardProps) {
  const colorClasses: Record<string, string> = {
    green: 'bg-green-50 text-green-700 border-green-200',
    yellow: 'bg-yellow-50 text-yellow-700 border-yellow-200',
    red: 'bg-red-50 text-red-700 border-red-200',
    blue: 'bg-blue-50 text-blue-700 border-blue-200',
    gray: 'bg-gray-50 text-gray-700 border-gray-200',
  };

  return (
    <div className={`p-4 rounded-lg border ${colorClasses[color] || colorClasses.gray}`}>
      <div className="text-xs uppercase tracking-wide opacity-75">{label}</div>
      <div className="mt-1 text-lg font-semibold truncate" title={value}>
        {value}
      </div>
    </div>
  );
}

function ExplainTable({ data }: { data: ExplainRow[] }) {
  const columns = ['id', 'select_type', 'table', 'type', 'possible_keys', 'key', 'rows', 'filtered', 'Extra'];

  return (
    <div className="overflow-x-auto border rounded-lg">
      <table className="min-w-full divide-y divide-gray-200 text-xs">
        <thead className="bg-gray-50">
          <tr>
            {columns.map((col) => (
              <th key={col} className="px-3 py-2 text-left font-medium text-gray-500 uppercase">
                {col}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="bg-white divide-y divide-gray-200">
          {data.map((row, idx) => (
            <tr key={idx}>
              {columns.map((col) => (
                <td key={col} className="px-3 py-2 whitespace-nowrap">
                  <span className={getExplainCellClass(col, row[col as keyof ExplainRow])}>
                    {formatExplainValue(row[col as keyof ExplainRow])}
                  </span>
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function getTimeColor(ms: number): string {
  if (ms < 100) return 'green';
  if (ms < 500) return 'yellow';
  return 'red';
}

function getScanColor(scanned: number, returned: number): string {
  if (scanned === 0) return 'gray';
  const ratio = returned / scanned;
  if (ratio > 0.5) return 'green';
  if (ratio > 0.1) return 'yellow';
  return 'red';
}

function getExplainCellClass(col: string, value: unknown): string {
  if (col === 'type') {
    const type = String(value).toLowerCase();
    if (['system', 'const', 'eq_ref'].includes(type)) return 'text-green-600 font-medium';
    if (['ref', 'range', 'index'].includes(type)) return 'text-yellow-600';
    if (type === 'all') return 'text-red-600 font-medium';
  }
  if (col === 'key' && !value) {
    return 'text-red-500';
  }
  return '';
}

function formatExplainValue(value: unknown): string {
  if (value === null || value === undefined) return '-';
  if (typeof value === 'number') return value.toLocaleString();
  return String(value);
}
