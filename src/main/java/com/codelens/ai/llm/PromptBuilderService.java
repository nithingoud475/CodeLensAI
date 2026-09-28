package com.codelens.ai.llm;

import org.springframework.stereotype.Service;

@Service
public class PromptBuilderService {

    public String buildPrompt(String language, String sourceCode) {
        return "You are an expert software reviewer. Review the following " + language + " code.\n" +
               "Provide your response STRICTLY as a JSON object with the following structure. Do not output any markdown formatting or explanations outside the JSON.\n\n" +
               "{\n" +
               "  \"summary\": \"Brief overall summary of code condition\",\n" +
               "  \"issues\": [\n" +
               "    {\n" +
               "      \"category\": \"Bug|Security|Code Quality|Performance|Best Practices\",\n" +
               "      \"severity\": \"Critical|High|Medium|Low|Informational\",\n" +
               "      \"line\": <line_number_as_integer>,\n" +
               "      \"title\": \"Short issue title\",\n" +
               "      \"description\": \"What is the issue\",\n" +
               "      \"explanation\": \"Why it is an issue\",\n" +
               "      \"suggestedFix\": \"How to fix it\"\n" +
               "    }\n" +
               "  ],\n" +
               "  \"improvedCode\": \"Complete improved version of the code (just the code)\",\n" +
               "  \"improvementExplanation\": \"Short explanation of improvements made\"\n" +
               "}\n\n" +
               "Code to review:\n" +
               "```" + language + "\n" +
               sourceCode + "\n" +
               "```\n\n" +
               "Ensure your entire output is valid JSON, do not include any ```json tags.";
    }
}
