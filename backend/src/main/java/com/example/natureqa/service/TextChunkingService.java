package com.example.natureqa.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TextChunkingService {

    /**
     * Splits text on paragraph boundaries where possible, then merges into chunks up to {@code maxChunkChars},
     * with overlap carried from the end of each chunk into the start of the next. Paragraphs longer than
     * {@code maxChunkChars} are split with a sliding window.
     */
    public List<String> chunk(String text, int maxChunkChars, int overlapChars) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        int max = Math.max(256, maxChunkChars);
        int overlap = Math.min(Math.max(0, overlapChars), max / 2);

        String normalized = text.trim().replace("\r\n", "\n");
        String[] rawParagraphs = normalized.split("\n{2,}");
        List<String> units = new ArrayList<>();
        for (String raw : rawParagraphs) {
            String p = raw.strip();
            if (p.isEmpty()) {
                continue;
            }
            if (p.length() <= max) {
                units.add(p);
            } else {
                units.addAll(splitLongSegment(p, max, overlap));
            }
        }

        if (units.isEmpty()) {
            return List.of();
        }

        List<String> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String unit : units) {
            if (current.isEmpty()) {
                current.append(unit);
                continue;
            }
            if (current.length() + 2 + unit.length() <= max) {
                current.append("\n\n").append(unit);
            } else {
                String finished = current.toString();
                chunks.add(finished);
                String prefix = suffixForOverlap(finished, overlap);
                if (!prefix.isEmpty() && prefix.length() + 2 + unit.length() <= max) {
                    current = new StringBuilder(prefix).append("\n\n").append(unit);
                } else {
                    current = new StringBuilder(unit);
                }
            }
        }
        if (!current.isEmpty()) {
            chunks.add(current.toString());
        }
        return chunks;
    }

    private static List<String> splitLongSegment(String segment, int max, int overlap) {
        List<String> parts = new ArrayList<>();
        int start = 0;
        while (start < segment.length()) {
            int end = Math.min(start + max, segment.length());
            parts.add(segment.substring(start, end));
            if (end == segment.length()) {
                break;
            }
            int nextStart = end - overlap;
            if (nextStart <= start) {
                nextStart = start + 1;
            }
            start = nextStart;
        }
        return parts;
    }

    private static String suffixForOverlap(String text, int overlap) {
        if (overlap <= 0 || text.isEmpty()) {
            return "";
        }
        if (text.length() <= overlap) {
            return text;
        }
        return text.substring(text.length() - overlap);
    }
}
