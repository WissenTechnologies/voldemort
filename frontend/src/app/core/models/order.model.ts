export interface OrderRequest {
  userId: number;
  portfolioId: number;
  companyId: number;
  quantity: number;
}

export interface OrderResponse {
  id: number;
  userId: number;
  portfolioId: number;
  companyId: number;
  companyName: string;
  type: string;
  quantity: number;
  price: number;
  status: string;
  createdAt: string;
}
