package com.example.natureqa.dto;

import java.util.List;

public record PdfEmbedResponse(
        String filename,
        int pageCount,
        String embeddingModel,
        Integer dimensions,
        List<PdfEmbedChunk> chunks) {
}
