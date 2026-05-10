package com.example.natureqa.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class TextChunkingServiceTest {

    private final TextChunkingService service = new TextChunkingService();

    @Test
    void blankReturnsEmpty() {
        assertThat(service.chunk("", 1000, 150)).isEmpty();
        assertThat(service.chunk("   ", 1000, 150)).isEmpty();
    }

    @Test
    void shortTextSingleChunk() {
        List<String> chunks = service.chunk("Hello world.", 1000, 150);
        assertThat(chunks).containsExactly("Hello world.");
    }

    @Test
    void paragraphsMergedUpToMax() {
        String p1 = "a".repeat(400);
        String p2 = "b".repeat(400);
        String text = p1 + "\n\n" + p2;
        List<String> chunks = service.chunk(text, 1000, 50);
        assertThat(chunks).hasSize(1);
        assertThat(chunks.get(0)).contains(p1).contains(p2);
    }

    @Test
    void longParagraphSplit() {
        String segment = "x".repeat(2500);
        List<String> chunks = service.chunk(segment, 1000, 100);
        assertThat(chunks).isNotEmpty();
        assertThat(chunks.stream().mapToInt(String::length).max().orElse(0)).isLessThanOrEqualTo(1000);
    }
}
