package com.codelens.ai.dto;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class OllamaRequest {
    private String model;
    private String prompt;
    private boolean stream;
    private String format;
}
