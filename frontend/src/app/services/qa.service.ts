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
  private readonly askUrl = `${environment.apiBaseUrl}/api/ask`;

  constructor(private readonly http: HttpClient) {}

  askQuestion(question: string): Observable<AskResponse> {
    const payload: AskRequest = { question };
    return this.http.post<AskResponse>(this.askUrl, payload);
  }
}
