export type OrderMode = 'MARKET' | 'LIMIT' | 'STOP_LOSS';
export type OrderStatus = 'PENDING' | 'EXECUTED' | 'FAILED' | 'EXPIRED';
export type OrderType = 'BUY' | 'SELL';

export interface OrderRequest {
  userId: number;
  portfolioId: number;
  companyId: number;
  quantity: number;
  orderMode?: OrderMode;
  targetPrice?: number;
}

export interface OrderResponse {
  id: number;
  userId: number;
  portfolioId: number;
  companyId: number;
  companyName: string;
  type: OrderType;
  orderMode: OrderMode;
  quantity: number;
  price: number;
  targetPrice?: number;
  executedPrice?: number;
  status: OrderStatus;
  createdAt: string;
  executedAt?: string;
}

export interface OrderFilterParams {
  userId?: number;
  portfolioId?: number;
  status?: OrderStatus;
}
