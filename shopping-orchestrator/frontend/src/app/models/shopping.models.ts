// Matches backend ShoppingRequest.java
export interface ShoppingRequest {
  productCategory: string;
  budget: number;
  preferences: string;
  brand?: string;
  additionalRequirements?: string;
}

// Matches backend ProductInfo.java
export interface ProductInfo {
  id: string;
  name: string;
  brand: string;
  category: string;
  price: number;
  priceRange: string;
  keyFeatures: string[];
  notableHighlights: string;
  rating: number;
  imageUrl: string;
  sourceUrl: string;
  platform?: string;        // "Amazon", "Flipkart", or "Mock"
  reviewCount?: number;
  availability?: string;
  discount?: string;
}

// Matches backend ComparisonResult.java
export interface ComparisonResult {
  products: ProductInfo[];
  featureComparison: { [key: string]: string[] };
  prosAndCons: { [key: string]: string[] };
  summary: string;
}

// Matches backend BudgetAnalysis.java
export interface BudgetAnalysis {
  userBudget: number;
  productsWithinBudget: ProductInfo[];
  cheaperAlternatives: ProductInfo[];
  costVsValueInsight: string;
  bestValueProduct: ProductInfo;
}

// Matches backend RecommendationResult.java
export interface RecommendationResult {
  bestProduct: ProductInfo;
  reasonForRecommendation: string;
  alternativeOptions: ProductInfo[];
  finalDecision: string;
}

// Matches backend OrchestratorResponse.java
export interface OrchestratorResponse {
  requestId: string;
  timestamp: string;
  productCategory: string;
  budget: number;
  preferences: string;
  topOptionsFound: ProductInfo[];
  comparisonSummary: ComparisonResult;
  budgetAnalysis: BudgetAnalysis;
  finalRecommendation: RecommendationResult;
  status: string;
  errorMessage: string;
}

// Matches backend SearchHistory.java
export interface SearchHistory {
  id: number;
  requestId: string;
  productCategory: string;
  budget: number;
  preferences: string;
  bestProductName: string;
  bestProductPrice: number;
  createdAt: string;
  status: string;
}
