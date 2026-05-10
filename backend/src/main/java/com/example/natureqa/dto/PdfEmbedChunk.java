package com.example.natureqa.dto;

import java.util.List;

public record PdfEmbedChunk(int index, String text, List<Double> embedding) {
}
