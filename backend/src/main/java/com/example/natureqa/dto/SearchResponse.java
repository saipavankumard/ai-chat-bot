package com.example.natureqa.dto;

import java.util.List;

public record SearchResponse(List<SimilarChunk> chunks) {
}
