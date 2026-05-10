package com.example.natureqa.service;

import com.example.natureqa.dto.SearchResponse;
import com.example.natureqa.dto.SimilarChunk;
import com.example.natureqa.repository.DocumentChunkRepository;
import com.example.natureqa.repository.DocumentChunkRepository.ChunkNearestRow;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SimilaritySearchService {

    private final OpenAiEmbeddingService openAiEmbeddingService;
    private final DocumentChunkRepository documentChunkRepository;

    public SimilaritySearchService(
            OpenAiEmbeddingService openAiEmbeddingService, DocumentChunkRepository documentChunkRepository) {
        this.openAiEmbeddingService = openAiEmbeddingService;
        this.documentChunkRepository = documentChunkRepository;
    }

    public SearchResponse search(String query, Integer limit) {
        String trimmed = query == null ? "" : query.trim();
        int effectiveLimit = limit == null ? DocumentChunkRepository.DEFAULT_NEAREST_LIMIT : limit;

        List<Double> vector = openAiEmbeddingService.embedQuery(trimmed);
        float[] queryEmbedding = toFloatArray(vector);
        List<ChunkNearestRow> rows = documentChunkRepository.findNearest(queryEmbedding, effectiveLimit);

        List<SimilarChunk> chunks = new ArrayList<>(rows.size());
        for (ChunkNearestRow row : rows) {
            chunks.add(new SimilarChunk(row.id(), row.content(), row.distance()));
        }
        return new SearchResponse(chunks);
    }

    private static float[] toFloatArray(List<Double> values) {
        float[] out = new float[values.size()];
        for (int i = 0; i < values.size(); i++) {
            out[i] = values.get(i).floatValue();
        }
        return out;
    }
}
