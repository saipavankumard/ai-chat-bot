package com.example.natureqa.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SearchRequest(
        @NotBlank(message = "Query is required")
        @Size(max = 2000, message = "Query is too long")
        String query,
        @Min(value = 1, message = "Limit must be at least 1")
        @Max(value = 20, message = "Limit must be at most 20")
        Integer limit) {
}
