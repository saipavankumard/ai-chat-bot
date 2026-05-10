import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface SimilarChunk {
  id: number;
  content: string;
  distance: number;
}

export interface SearchResponse {
  chunks: SimilarChunk[];
}

interface SearchRequest {
  query: string;
  limit?: number;
}

@Injectable({
  providedIn: 'root'
})
export class SearchService {
  private readonly searchUrl = `${environment.apiBaseUrl}/api/search`;

  constructor(private readonly http: HttpClient) {}

  searchSimilar(query: string, limit?: number): Observable<SearchResponse> {
    const body: SearchRequest = { query };
    if (limit != null && limit > 0) {
      body.limit = limit;
    }
    return this.http.post<SearchResponse>(this.searchUrl, body);
  }
}
