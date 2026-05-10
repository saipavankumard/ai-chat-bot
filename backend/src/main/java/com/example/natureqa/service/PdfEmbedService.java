package com.example.natureqa.service;

import com.example.natureqa.config.OpenAiProperties;
import com.example.natureqa.dto.PdfEmbedChunk;
import com.example.natureqa.dto.PdfEmbedResponse;
import com.example.natureqa.dto.PdfExtractResponse;
import com.example.natureqa.repository.DocumentChunkRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PdfEmbedService {

    private final PdfTextExtractionService pdfTextExtractionService;
    private final TextChunkingService textChunkingService;
    private final OpenAiEmbeddingService openAiEmbeddingService;
    private final OpenAiProperties openAiProperties;
    private final DocumentChunkRepository documentChunkRepository;

    public PdfEmbedService(
            PdfTextExtractionService pdfTextExtractionService,
            TextChunkingService textChunkingService,
            OpenAiEmbeddingService openAiEmbeddingService,
            OpenAiProperties openAiProperties,
            DocumentChunkRepository documentChunkRepository) {
        this.pdfTextExtractionService = pdfTextExtractionService;
        this.textChunkingService = textChunkingService;
        this.openAiEmbeddingService = openAiEmbeddingService;
        this.openAiProperties = openAiProperties;
        this.documentChunkRepository = documentChunkRepository;
    }

    public PdfEmbedResponse embed(MultipartFile file) {
        PdfExtractResponse extracted = pdfTextExtractionService.extract(file);
        int maxChars = openAiProperties.getEmbeddingChunkSizeChars();
        int overlap = openAiProperties.getEmbeddingChunkOverlapChars();
        List<String> chunkTexts =
                textChunkingService.chunk(extracted.text(), maxChars, overlap);
        List<List<Double>> vectors = openAiEmbeddingService.embedTexts(chunkTexts);

        List<PdfEmbedChunk> chunks = new ArrayList<>(chunkTexts.size());
        for (int i = 0; i < chunkTexts.size(); i++) {
            chunks.add(new PdfEmbedChunk(i, chunkTexts.get(i), vectors.get(i)));
        }

        if (!chunks.isEmpty()) {
            List<float[]> embeddingArrays = new ArrayList<>(chunks.size());
            for (PdfEmbedChunk chunk : chunks) {
                embeddingArrays.add(toFloatArray(chunk.embedding()));
            }
            documentChunkRepository.insertChunks(chunkTexts, embeddingArrays);
        }

        Integer dimensions = chunks.isEmpty() ? null : chunks.get(0).embedding().size();
        String model = openAiProperties.getEmbeddingModel();
        return new PdfEmbedResponse(extracted.filename(), extracted.pageCount(), model, dimensions, chunks);
    }

    private static float[] toFloatArray(List<Double> values) {
        float[] out = new float[values.size()];
        for (int i = 0; i < values.size(); i++) {
            out[i] = values.get(i).floatValue();
        }
        return out;
    }
}
