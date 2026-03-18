export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export interface Portfolio {
  id: number;
  userId: number;
  name: string;
}

export interface Holding {
  companyId: number;
  companyName: string;
  quantity: number;
  avgPrice: number;
  currentPrice: number;
  profitLoss: number;
}

export interface PortfolioSummary {
  totalInvestment: number;
  currentValue: number;
  overallPL: number;
  holdings: Holding[];
}
