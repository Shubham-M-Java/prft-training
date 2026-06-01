import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { OrchestratorResponse, SearchHistory, ShoppingRequest } from '../models/shopping.models';

@Injectable({
  providedIn: 'root'
})
export class ShoppingService {
  private apiUrl = '/api/orchestrator';

  constructor(private http: HttpClient) {}

  search(request: ShoppingRequest): Observable<OrchestratorResponse> {
    return this.http.post<OrchestratorResponse>(`${this.apiUrl}/search`, request);
  }

  getHistory(): Observable<SearchHistory[]> {
    return this.http.get<SearchHistory[]>(`${this.apiUrl}/history`).pipe(
      catchError(() => of([]))
    );
  }

  getHistoryById(id: number): Observable<OrchestratorResponse> {
    return this.http.get<OrchestratorResponse>(`${this.apiUrl}/history/${id}`);
  }

  healthCheck(): Observable<string> {
    return this.http.get(`${this.apiUrl}/health`, { responseType: 'text' }).pipe(
      catchError(() => of('Backend not available'))
    );
  }
}
