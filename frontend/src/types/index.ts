export interface User {
  id: number;
  username: string;
  email: string;
  role: string;
}

export interface AuthResponse {
  token: string;
  id: number;
  username: string;
  email: string;
  role: string;
}

export interface QueryResult {
  status: 'SUCCESS' | 'ERROR' | 'TIMEOUT';
  originalSql: string;
  processedSql: string | null;
  columns: string[] | null;
  data: Record<string, unknown>[] | null;
  executionTimeMs: number | null;
  rowsReturned: number | null;
  rowsScanned: number | null;
  indexUsed: string | null;
  explainResult: ExplainRow[] | null;
  errorMessage: string | null;
}

export interface ExplainRow {
  id: number;
  select_type: string;
  table: string;
  partitions: string | null;
  type: string;
  possible_keys: string | null;
  key: string | null;
  key_len: string | null;
  ref: string | null;
  rows: number;
  filtered: number;
  Extra: string | null;
}

export interface ExplainResult {
  success: boolean;
  explainData: ExplainRow[] | null;
  indexUsed: string | null;
  rowsScanned: number | null;
  errorMessage: string | null;
}

export interface HistoryItem {
  id: number;
  sqlText: string;
  executionTimeMs: number | null;
  rowsScanned: number | null;
  rowsReturned: number | null;
  indexUsed: string | null;
  status: 'SUCCESS' | 'ERROR' | 'TIMEOUT';
  createdAt: string;
}

export interface PagedResponse<T> {
  items: T[];
  page: number;
  pageSize: number;
  totalItems: number;
  totalPages: number;
}

export interface ApiError {
  status: number;
  message: string;
  timestamp: string;
}
