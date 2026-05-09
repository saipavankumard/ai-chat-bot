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
  question = '';
  answer = '';
  errorMessage = '';
  loading = false;

  constructor(
    private readonly qaService: QaService,
    private readonly sanitizer: DomSanitizer
  ) {}

  ask(): void {
    const trimmedQuestion = this.question.trim();
    if (!trimmedQuestion) {
      this.errorMessage = 'Please enter a question before asking.';
      this.answer = '';
      return;
    }

    this.loading = true;
    this.answer = '';
    this.errorMessage = '';

    this.qaService.askQuestion(trimmedQuestion).subscribe({
      next: (response) => {
        this.answer = response.answer;
        this.loading = false;
      },
      error: (error: HttpErrorResponse) => {
        const backend = error.error as ErrorResponse | undefined;
        this.errorMessage = backend?.message || 'Unable to get an answer right now. Please try again.';
        this.loading = false;
      }
    });
  }

  get formattedAnswer(): SafeHtml {
    const escaped = this.escapeHtml(this.answer);
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
