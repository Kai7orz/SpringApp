interface ResultTableProps {
  columns: string[];
  data: Record<string, unknown>[];
  maxRows?: number;
}

export default function ResultTable({ columns, data, maxRows = 100 }: ResultTableProps) {
  const displayData = data.slice(0, maxRows);

  return (
    <div className="overflow-auto max-h-96 border rounded-lg">
      <table className="min-w-full divide-y divide-gray-200">
        <thead className="bg-gray-50 sticky top-0">
          <tr>
            {columns.map((col, idx) => (
              <th
                key={idx}
                className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
              >
                {col}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="bg-white divide-y divide-gray-200">
          {displayData.map((row, rowIdx) => (
            <tr key={rowIdx} className="hover:bg-gray-50">
              {columns.map((col, colIdx) => (
                <td key={colIdx} className="px-4 py-2 text-sm text-gray-900 whitespace-nowrap">
                  {formatValue(row[col])}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
      {data.length > maxRows && (
        <div className="px-4 py-2 bg-yellow-50 text-sm text-yellow-700 border-t">
          Showing {maxRows} of {data.length} rows
        </div>
      )}
    </div>
  );
}

function formatValue(value: unknown): string {
  if (value === null || value === undefined) {
    return 'NULL';
  }
  if (typeof value === 'object') {
    return JSON.stringify(value);
  }
  return String(value);
}
