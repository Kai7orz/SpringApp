import axios from 'axios';
import type { AuthResponse, QueryResult, ExplainResult, HistoryItem, PagedResponse } from '../types';

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// リクエストインターセプター：トークンを自動付与
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// レスポンスインターセプター：401エラー時にログアウト
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Auth API
export const authApi = {
  register: async (username: string, email: string, password: string): Promise<AuthResponse> => {
    const response = await api.post<AuthResponse>('/auth/register', { username, email, password });
    return response.data;
  },

  login: async (email: string, password: string): Promise<AuthResponse> => {
    const response = await api.post<AuthResponse>('/auth/login', { email, password });
    return response.data;
  },
};

// Query API
export const queryApi = {
  execute: async (sql: string): Promise<QueryResult> => {
    const response = await api.post<QueryResult>('/query/execute', { sql });
    return response.data;
  },

  explain: async (sql: string): Promise<ExplainResult> => {
    const response = await api.post<ExplainResult>('/query/explain', { sql });
    return response.data;
  },

  compare: async (queries: string[]): Promise<QueryResult[]> => {
    const response = await api.post<QueryResult[]>('/query/compare', { queries });
    return response.data;
  },
};

// History API
export const historyApi = {
  getHistory: async (page: number = 0, size: number = 20): Promise<PagedResponse<HistoryItem>> => {
    const response = await api.get<PagedResponse<HistoryItem>>('/history', {
      params: { page, size },
    });
    return response.data;
  },

  getHistoryDetail: async (id: number): Promise<HistoryItem> => {
    const response = await api.get<HistoryItem>(`/history/${id}`);
    return response.data;
  },
};

// Sample Data API (Admin only)
export const sampleApi = {
  generate: async (customers: number, products: number, orders: number, itemsPerOrder: number) => {
    const response = await api.post('/sample/generate', null, {
      params: { customers, products, orders, itemsPerOrder },
    });
    return response.data;
  },

  getStatus: async () => {
    const response = await api.get('/sample/status');
    return response.data;
  },
};

export default api;
