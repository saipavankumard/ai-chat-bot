import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface PdfExtractResponse {
  filename: string;
  text: string;
  pageCount: number;
}

export interface PdfEmbedChunk {
  index: number;
  text: string;
  embedding: number[];
}

export interface PdfEmbedResponse {
  filename: string;
  pageCount: number;
  embeddingModel: string;
  dimensions: number | null;
  chunks: PdfEmbedChunk[];
}

@Injectable({
  providedIn: 'root'
})
export class PdfService {
  private readonly extractUrl = `${environment.apiBaseUrl}/api/pdf/extract`;
  private readonly embedUrl = `${environment.apiBaseUrl}/api/pdf/embed`;

  constructor(private readonly http: HttpClient) {}

  extractText(file: File): Observable<PdfExtractResponse> {
    const form = new FormData();
    form.append('file', file, file.name);
    return this.http.post<PdfExtractResponse>(this.extractUrl, form);
  }

  embedPdf(file: File): Observable<PdfEmbedResponse> {
    const form = new FormData();
    form.append('file', file, file.name);
    return this.http.post<PdfEmbedResponse>(this.embedUrl, form);
  }
}
