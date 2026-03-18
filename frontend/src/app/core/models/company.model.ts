export interface Company {
  id: number;
  name: string;
  symbol: string;
  description?: string;
  sector?: string;
  marketCap?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface CompanyPrice {
  id: number;
  companyId: number;
  value: number;
  recordedAt: string;
}

export interface CandlestickData {
  time: string;
  open: number;
  high: number;
  low: number;
  close: number;
}

export interface CompanyWithLatestPrice extends Company {
  latestPrice?: number;
  priceChange?: number;
  priceChangePercent?: number;
}
