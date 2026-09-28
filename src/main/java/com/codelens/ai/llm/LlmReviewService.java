package com.codelens.ai.llm;

import com.codelens.ai.dto.LlmReviewResponse;
import com.codelens.ai.dto.OllamaRequest;
import com.codelens.ai.dto.OllamaResponse;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class LlmReviewService {

    @Value("${ollama.url}")
    private String ollamaUrl;

    @Value("${ollama.model}")
    private String ollamaModel;

    private final PromptBuilderService promptBuilder;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public LlmReviewService(PromptBuilderService promptBuilder) {
        this.promptBuilder = promptBuilder;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public LlmReviewResponse getReview(String language, String sourceCode) {
        String prompt = promptBuilder.buildPrompt(language, sourceCode);

        OllamaRequest request = OllamaRequest.builder()
                .model(ollamaModel)
                .prompt(prompt)
                .stream(false)
                .format("json") // Inform ollama to output JSON
                .build();

        try {
            ResponseEntity<OllamaResponse> responseEntity = restTemplate.postForEntity(ollamaUrl + "/api/generate", request, OllamaResponse.class);
            OllamaResponse response = responseEntity.getBody();
            if (response != null && response.getResponse() != null) {
                String jsonStr = response.getResponse();
                return objectMapper.readValue(jsonStr, LlmReviewResponse.class);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to communicate with local LLM or parse response: " + e.getMessage(), e);
        }
        
        throw new RuntimeException("Empty response from LLM");
    }
}
