import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

interface AskRequest {
  question: string;
}

export interface AskResponse {
  answer: string;
}

export interface ErrorResponse {
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class QaService {
  private readonly askDirectUrl = `${environment.apiBaseUrl}/api/ask/direct`;
  private readonly askPdfUrl = `${environment.apiBaseUrl}/api/ask/pdf`;

  constructor(private readonly http: HttpClient) {}

  askDirect(question: string): Observable<AskResponse> {
    const payload: AskRequest = { question };
    return this.http.post<AskResponse>(this.askDirectUrl, payload);
  }

  askPdfQuestion(file: File, question: string): Observable<AskResponse> {
    const form = new FormData();
    form.append('file', file, file.name);
    form.append('question', question);
    return this.http.post<AskResponse>(this.askPdfUrl, form);
  }
}
