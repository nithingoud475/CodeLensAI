package com.codelens.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CodeReviewRequest {
    @NotBlank
    private String language;

    @NotBlank
    private String sourceCode;
}
