import { useState, useEffect } from 'react';
import { sampleApi } from '../services/api';

export default function AdminPage() {
  const [generating, setGenerating] = useState(false);
  const [status, setStatus] = useState<{
    isGenerating: boolean;
    progress: number;
    currentTask: string;
  } | null>(null);

  const [config, setConfig] = useState({
    customers: 10000,
    products: 1000,
    orders: 50000,
    itemsPerOrder: 3,
  });

  useEffect(() => {
    // Poll status every 2 seconds while generating
    let interval: NodeJS.Timeout;
    if (generating || status?.isGenerating) {
      interval = setInterval(checkStatus, 2000);
    }
    return () => clearInterval(interval);
  }, [generating, status?.isGenerating]);

  const checkStatus = async () => {
    try {
      const data = await sampleApi.getStatus();
      setStatus(data);
      if (!data.isGenerating && generating) {
        setGenerating(false);
      }
    } catch (err) {
      console.error('Failed to get status:', err);
    }
  };

  const handleGenerate = async () => {
    setGenerating(true);
    try {
      await sampleApi.generate(
        config.customers,
        config.products,
        config.orders,
        config.itemsPerOrder
      );
      checkStatus();
    } catch (err) {
      console.error('Failed to start generation:', err);
      setGenerating(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="bg-white rounded-lg shadow p-6">
        <h2 className="text-lg font-semibold text-gray-900 mb-4">Sample Data Generator</h2>
        <p className="text-sm text-gray-600 mb-6">
          Generate sample data for SQL performance testing. This will replace all existing sample
          data.
        </p>

        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Customers</label>
            <input
              type="number"
              value={config.customers}
              onChange={(e) => setConfig({ ...config, customers: parseInt(e.target.value) || 0 })}
              className="w-full px-3 py-2 border rounded focus:ring-blue-500 focus:border-blue-500"
              min={100}
              max={1000000}
            />
            <span className="text-xs text-gray-500">Max: 1,000,000</span>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Products</label>
            <input
              type="number"
              value={config.products}
              onChange={(e) => setConfig({ ...config, products: parseInt(e.target.value) || 0 })}
              className="w-full px-3 py-2 border rounded focus:ring-blue-500 focus:border-blue-500"
              min={100}
              max={100000}
            />
            <span className="text-xs text-gray-500">Max: 100,000</span>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Orders</label>
            <input
              type="number"
              value={config.orders}
              onChange={(e) => setConfig({ ...config, orders: parseInt(e.target.value) || 0 })}
              className="w-full px-3 py-2 border rounded focus:ring-blue-500 focus:border-blue-500"
              min={100}
              max={5000000}
            />
            <span className="text-xs text-gray-500">Max: 5,000,000</span>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Items/Order (avg)</label>
            <input
              type="number"
              value={config.itemsPerOrder}
              onChange={(e) =>
                setConfig({ ...config, itemsPerOrder: parseInt(e.target.value) || 1 })
              }
              className="w-full px-3 py-2 border rounded focus:ring-blue-500 focus:border-blue-500"
              min={1}
              max={10}
            />
            <span className="text-xs text-gray-500">1-10 items</span>
          </div>
        </div>

        <div className="bg-yellow-50 border border-yellow-200 rounded p-4 mb-6">
          <p className="text-sm text-yellow-800">
            <strong>Warning:</strong> This will delete all existing sample data and generate new
            data. This operation may take several minutes depending on the data volume.
          </p>
        </div>

        <button
          onClick={handleGenerate}
          disabled={generating || status?.isGenerating}
          className="px-6 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50"
        >
          {generating || status?.isGenerating ? 'Generating...' : 'Generate Sample Data'}
        </button>
      </div>

      {/* Progress */}
      {(generating || status?.isGenerating) && status && (
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Generation Progress</h3>
          <div className="mb-2">
            <div className="flex justify-between text-sm text-gray-600 mb-1">
              <span>{status.currentTask}</span>
              <span>{status.progress}%</span>
            </div>
            <div className="w-full bg-gray-200 rounded-full h-4">
              <div
                className="bg-blue-600 h-4 rounded-full transition-all duration-300"
                style={{ width: `${status.progress}%` }}
              />
            </div>
          </div>
        </div>
      )}

      {/* Data Summary */}
      <div className="bg-white rounded-lg shadow p-6">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">Expected Data Volume</h3>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <div className="p-4 bg-blue-50 rounded">
            <div className="text-2xl font-bold text-blue-600">
              {config.customers.toLocaleString()}
            </div>
            <div className="text-sm text-gray-600">Customers</div>
          </div>
          <div className="p-4 bg-green-50 rounded">
            <div className="text-2xl font-bold text-green-600">
              {config.products.toLocaleString()}
            </div>
            <div className="text-sm text-gray-600">Products</div>
          </div>
          <div className="p-4 bg-yellow-50 rounded">
            <div className="text-2xl font-bold text-yellow-600">
              {config.orders.toLocaleString()}
            </div>
            <div className="text-sm text-gray-600">Orders</div>
          </div>
          <div className="p-4 bg-purple-50 rounded">
            <div className="text-2xl font-bold text-purple-600">
              ~{(config.orders * config.itemsPerOrder).toLocaleString()}
            </div>
            <div className="text-sm text-gray-600">Order Items</div>
          </div>
        </div>
      </div>
    </div>
  );
}
