export interface Company {
  id: number;
  name: string;
  symbol: string;
  description?: string;
  sector?: string;
  industry?: string;
  volume?: number;
  value?: number;
  marketCap?: number;
  peRatio?: number;
  eps?: number;
  foundedYear?: number;
  headquarters?: string;
  ceo?: string;
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
