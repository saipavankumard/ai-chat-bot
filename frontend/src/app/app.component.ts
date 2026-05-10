import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { QaService, ErrorResponse } from './services/qa.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  generalQuestion = '';
  generalAnswer = '';
  generalErrorMessage = '';
  generalLoading = false;

  ragPdfFile: File | null = null;
  ragPdfFileName = '';
  pdfQuestion = '';
  pdfRagAnswer = '';
  pdfRagErrorMessage = '';
  pdfRagLoading = false;

  constructor(
    private readonly qaService: QaService,
    private readonly sanitizer: DomSanitizer
  ) {}

  askGeneral(): void {
    const trimmedQuestion = this.generalQuestion.trim();
    if (!trimmedQuestion) {
      this.generalErrorMessage = 'Please enter a question before asking.';
      this.generalAnswer = '';
      return;
    }

    this.generalLoading = true;
    this.generalAnswer = '';
    this.generalErrorMessage = '';

    this.qaService.askDirect(trimmedQuestion).subscribe({
      next: (response) => {
        this.generalAnswer = response.answer;
        this.generalLoading = false;
      },
      error: (error: HttpErrorResponse) => {
        const backend = error.error as ErrorResponse | undefined;
        this.generalErrorMessage =
          backend?.message || 'Unable to get an answer right now. Please try again.';
        this.generalLoading = false;
      }
    });
  }

  onRagPdfSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    this.pdfRagAnswer = '';
    this.pdfRagErrorMessage = '';

    if (!file) {
      this.ragPdfFile = null;
      this.ragPdfFileName = '';
      return;
    }

    if (!file.name.toLowerCase().endsWith('.pdf')) {
      this.pdfRagErrorMessage = 'Please choose a PDF file.';
      this.ragPdfFile = null;
      this.ragPdfFileName = '';
      input.value = '';
      return;
    }

    this.ragPdfFile = file;
    this.ragPdfFileName = file.name;
  }

  askPdf(): void {
    if (!this.ragPdfFile) {
      this.pdfRagErrorMessage = 'Choose a PDF file first.';
      return;
    }
    const q = this.pdfQuestion.trim();
    if (!q) {
      this.pdfRagErrorMessage = 'Enter your question about the PDF.';
      return;
    }

    this.pdfRagLoading = true;
    this.pdfRagAnswer = '';
    this.pdfRagErrorMessage = '';

    this.qaService.askPdfQuestion(this.ragPdfFile, q).subscribe({
      next: (response) => {
        this.pdfRagAnswer = response.answer;
        this.pdfRagLoading = false;
      },
      error: (error: HttpErrorResponse) => {
        const backend = error.error as ErrorResponse | undefined;
        this.pdfRagErrorMessage =
          backend?.message || 'Could not answer from this PDF. Please try again.';
        this.pdfRagLoading = false;
      }
    });
  }

  get formattedGeneralAnswer(): SafeHtml {
    return this.toFormattedHtml(this.generalAnswer);
  }

  get formattedPdfRagAnswer(): SafeHtml {
    return this.toFormattedHtml(this.pdfRagAnswer);
  }

  private toFormattedHtml(answer: string): SafeHtml {
    const escaped = this.escapeHtml(answer);
    const blocks = escaped.split(/\n{2,}/).map((block) => block.trim()).filter(Boolean);
    const html = blocks.map((block) => this.renderBlock(block)).join('');
    return this.sanitizer.bypassSecurityTrustHtml(html);
  }

  private renderBlock(block: string): string {
    if (block.startsWith('```') && block.endsWith('```')) {
      const lines = block.split('\n');
      const code = lines.slice(1, -1).join('\n');
      return `<pre><code>${code}</code></pre>`;
    }

    if (block.startsWith('### ')) {
      return `<h3>${this.renderInline(block.slice(4))}</h3>`;
    }

    if (block.startsWith('## ')) {
      return `<h2>${this.renderInline(block.slice(3))}</h2>`;
    }

    if (block.startsWith('# ')) {
      return `<h1>${this.renderInline(block.slice(2))}</h1>`;
    }

    const lines = block.split('\n').map((line) => line.trim()).filter(Boolean);
    const numbered = lines.every((line) => /^\d+\.\s+/.test(line));
    const bulleted = lines.every((line) => /^[-*]\s+/.test(line));

    if (numbered) {
      const items = lines.map((line) => line.replace(/^\d+\.\s+/, ''));
      return `<ol>${items.map((item) => `<li>${this.renderInline(item)}</li>`).join('')}</ol>`;
    }

    if (bulleted) {
      const items = lines.map((line) => line.replace(/^[-*]\s+/, ''));
      return `<ul>${items.map((item) => `<li>${this.renderInline(item)}</li>`).join('')}</ul>`;
    }

    return `<p>${this.renderInline(block).replace(/\n/g, '<br>')}</p>`;
  }

  private renderInline(text: string): string {
    return text
      .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
      .replace(/`([^`]+)`/g, '<code>$1</code>');
  }

  private escapeHtml(text: string): string {
    return text
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }
}
