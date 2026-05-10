package com.example.natureqa.service;

import com.example.natureqa.config.OpenAiProperties;
import com.example.natureqa.dto.PdfExtractResponse;
import com.example.natureqa.exception.PdfProcessingException;
import com.example.natureqa.repository.DocumentChunkRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PdfRagAnswerService {

    private final PdfTextExtractionService pdfTextExtractionService;
    private final TextChunkingService textChunkingService;
    private final OpenAiEmbeddingService openAiEmbeddingService;
    private final OpenAiService openAiService;
    private final OpenAiProperties openAiProperties;
    private final int ragChunkLimit;

    public PdfRagAnswerService(
            PdfTextExtractionService pdfTextExtractionService,
            TextChunkingService textChunkingService,
            OpenAiEmbeddingService openAiEmbeddingService,
            OpenAiService openAiService,
            OpenAiProperties openAiProperties,
            @Value("${app.rag.chunk-limit:5}") int ragChunkLimit) {
        this.pdfTextExtractionService = pdfTextExtractionService;
        this.textChunkingService = textChunkingService;
        this.openAiEmbeddingService = openAiEmbeddingService;
        this.openAiService = openAiService;
        this.openAiProperties = openAiProperties;
        this.ragChunkLimit = Math.min(
                DocumentChunkRepository.MAX_NEAREST_LIMIT, Math.max(1, ragChunkLimit));
    }

    public String answer(MultipartFile file, String question) {
        String q = question == null ? "" : question.trim();
        if (q.isEmpty()) {
            throw new PdfProcessingException("Question is required.");
        }
        if (q.length() > 2000) {
            throw new PdfProcessingException("Question is too long.");
        }

        PdfExtractResponse extracted = pdfTextExtractionService.extract(file);
        int maxChars = openAiProperties.getEmbeddingChunkSizeChars();
        int overlap = openAiProperties.getEmbeddingChunkOverlapChars();
        List<String> chunkTexts = textChunkingService.chunk(extracted.text(), maxChars, overlap);

        if (chunkTexts.isEmpty()) {
            return "No text could be extracted from this PDF to answer from.";
        }

        List<List<Double>> chunkVectors = openAiEmbeddingService.embedTexts(chunkTexts);
        List<Double> queryVector = openAiEmbeddingService.embedQuery(q);

        List<ScoredChunk> scored = new ArrayList<>(chunkTexts.size());
        for (int i = 0; i < chunkTexts.size(); i++) {
            double dist = l2Distance(chunkVectors.get(i), queryVector);
            scored.add(new ScoredChunk(chunkTexts.get(i), dist));
        }
        scored.sort(Comparator.comparingDouble(ScoredChunk::distance));

        int k = Math.min(ragChunkLimit, scored.size());
        String contextBlock =
                scored.subList(0, k).stream().map(ScoredChunk::text).collect(Collectors.joining("\n---\n"));

        String userMessage =
                "Answer only from the provided context.\n\nContext:\n\n"
                        + contextBlock
                        + "\n\nQuestion:\n"
                        + q;

        return openAiService.chat(List.of(Map.of("role", "user", "content", userMessage)));
    }

    private static double l2Distance(List<Double> a, List<Double> b) {
        if (a.size() != b.size()) {
            throw new IllegalStateException("Embedding dimension mismatch for distance.");
        }
        double sum = 0.0;
        for (int i = 0; i < a.size(); i++) {
            double d = a.get(i) - b.get(i);
            sum += d * d;
        }
        return Math.sqrt(sum);
    }

    private record ScoredChunk(String text, double distance) {}
}
