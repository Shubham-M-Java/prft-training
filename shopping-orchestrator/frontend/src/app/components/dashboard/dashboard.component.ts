import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ShoppingService } from '../../services/shopping.service';
import {
  OrchestratorResponse,
  SearchHistory,
  ShoppingRequest
} from '../../models/shopping.models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  request: ShoppingRequest = {
    productCategory: '',
    budget: 0,
    preferences: ''
  };

  categories = [
    'Electronics', 'Smartphones', 'Laptops', 'Tablets',
    'Cameras', 'Audio', 'TVs', 'Appliances',
    'Clothing', 'Footwear', 'Books', 'Sports', 'Other'
  ];

  selectedCategory = 'Electronics';

  isLoading = false;
  response: OrchestratorResponse | null = null;
  error: string | null = null;
  history: SearchHistory[] = [];
  activeStep = 0;
  stepLabels = [
    '🔍 Researching Products',
    '⚖️ Comparing Options',
    '💰 Analyzing Budget',
    '🏆 Generating Recommendation'
  ];

  constructor(private shoppingService: ShoppingService) {}

  ngOnInit(): void {
    this.loadHistory();
  }

  loadHistory(): void {
    this.shoppingService.getHistory().subscribe({
      next: (data: SearchHistory[]) => this.history = data,
      error: () => this.history = []
    });
  }

  onSearch(): void {
    if (!this.request.productCategory || !this.request.budget) return;

    this.isLoading = true;
    this.response = null;
    this.error = null;
    this.activeStep = 0;

    // Simulate agent steps progression
    const stepInterval = setInterval(() => {
      if (this.activeStep < this.stepLabels.length - 1) {
        this.activeStep++;
      }
    }, 800);

    this.shoppingService.search(this.request).subscribe({
      next: (data: OrchestratorResponse) => {
        clearInterval(stepInterval);
        this.isLoading = false;
        this.response = data;
        this.loadHistory();
      },
      error: (err: { error?: { message?: string } }) => {
        clearInterval(stepInterval);
        this.isLoading = false;
        this.error = err.error?.message || 'Failed to process request. Please ensure all backend services are running.';
      }
    });
  }

  onHistoryClick(item: SearchHistory): void {
    this.request = {
      productCategory: item.productCategory,
      budget: item.budget,
      preferences: item.preferences
    };
    this.onSearch();
  }

  onReset(): void {
    this.response = null;
    this.error = null;
    this.request = { productCategory: '', budget: 0, preferences: '' };
  }

  getStars(rating: number): string {
    const full = Math.floor(rating);
    const half = rating % 1 >= 0.5 ? 1 : 0;
    return '★'.repeat(full) + (half ? '½' : '') + '☆'.repeat(5 - full - half);
  }

  formatPrice(price: number): string {
    return '₹' + price.toLocaleString('en-IN');
  }

  getFeatureKeys(obj: { [key: string]: string[] }): string[] {
    return obj ? Object.keys(obj) : [];
  }

  getProsConsKeys(obj: { [key: string]: string[] }): string[] {
    return obj ? Object.keys(obj) : [];
  }
}
