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

  quickCategories = [
    '💻 Laptop', '📱 Smartphone', '🎧 Headphones', '📷 Camera',
    '📺 TV', '⌚ Smartwatch', '🎮 Gaming', '🖥️ Monitor'
  ];

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
    // History loaded after successful search
  }

  loadHistory(): void {
    this.shoppingService.getHistory().subscribe({
      next: (data: SearchHistory[]) => this.history = data,
      error: () => this.history = []
    });
  }

  setCategory(cat: string): void {
    // Strip emoji prefix if present
    const clean = cat.replace(/^[\p{Emoji}\s]+/u, '').trim();
    this.request.productCategory = clean;
  }

  onSearch(): void {
    if (!this.request.productCategory || !this.request.budget) return;

    this.isLoading = true;
    this.response = null;
    this.error = null;
    this.activeStep = 0;

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

  onImageError(event: Event): void {
    const img = event.target as HTMLImageElement;
    if (img) {
      img.style.display = 'none';
      const placeholder = img.nextElementSibling as HTMLElement;
      if (placeholder) placeholder.style.display = 'flex';
    }
  }

  getCategoryIcon(category: string): string {
    if (!category) return '📦';
    const cat = category.toLowerCase();
    if (cat.includes('laptop') || cat.includes('computer')) return '💻';
    if (cat.includes('phone') || cat.includes('mobile') || cat.includes('smartphone')) return '📱';
    if (cat.includes('headphone') || cat.includes('audio') || cat.includes('earphone')) return '🎧';
    if (cat.includes('camera') || cat.includes('photo')) return '📷';
    if (cat.includes('tv') || cat.includes('television')) return '📺';
    if (cat.includes('watch') || cat.includes('wearable')) return '⌚';
    if (cat.includes('gaming') || cat.includes('game')) return '🎮';
    if (cat.includes('tablet') || cat.includes('ipad')) return '📱';
    if (cat.includes('monitor') || cat.includes('display')) return '🖥️';
    if (cat.includes('speaker')) return '🔊';
    if (cat.includes('keyboard') || cat.includes('mouse')) return '⌨️';
    return '📦';
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
