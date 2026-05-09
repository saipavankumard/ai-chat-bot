package com.example.natureqa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AskRequest(
        @NotBlank(message = "Question is required")
        @Size(max = 2000, message = "Question is too long")
        String question
) {
}
