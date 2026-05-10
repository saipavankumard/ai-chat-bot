package com.example.natureqa.service;

import com.example.natureqa.config.OpenAiProperties;
import com.example.natureqa.exception.OpenAiException;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAiService {

    private final RestTemplate restTemplate;
    private final OpenAiProperties openAiProperties;

    public OpenAiService(RestTemplateBuilder restTemplateBuilder, OpenAiProperties openAiProperties) {
        this.openAiProperties = openAiProperties;
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(openAiProperties.getTimeoutMillis()))
                .setReadTimeout(Duration.ofMillis(openAiProperties.getTimeoutMillis()))
                .build();
    }

    public String ask(String question) {
        return chat(List.of(Map.of("role", "user", "content", question)));
    }

    /**
     * Sends a chat-completions request with the given messages (each map must include {@code role} and {@code content}).
     */
    @SuppressWarnings("unchecked")
    public String chat(List<Map<String, String>> messages) {
        if (openAiProperties.getApiKey() == null || openAiProperties.getApiKey().isBlank()) {
            throw new OpenAiException("OpenAI API key is not configured on the server.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(openAiProperties.getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", openAiProperties.getModel());
        requestBody.put("messages", messages);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response;
        try {
            response = restTemplate.postForEntity(openAiProperties.getApiUrl(), entity, Map.class);
        } catch (HttpStatusCodeException ex) {
            int status = ex.getStatusCode().value();
            if (status == 401) {
                throw new OpenAiException("OpenAI rejected the API key. Set a valid OPENAI_API_KEY and restart backend.");
            }
            if (status == 429) {
                throw new OpenAiException("OpenAI rate limit/quota reached. Check billing and usage limits.");
            }
            throw new OpenAiException("OpenAI request failed with status " + status + ".");
        } catch (ResourceAccessException ex) {
            throw new OpenAiException("OpenAI request timed out or network is unavailable. Please try again.");
        } catch (RestClientException ex) {
            throw new OpenAiException("Could not reach OpenAI. Please try again.");
        }

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new OpenAiException("OpenAI returned an unexpected response.");
        }

        Object choicesObj = response.getBody().get("choices");
        if (!(choicesObj instanceof List<?> choices) || choices.isEmpty()) {
            throw new OpenAiException("No answer was returned by OpenAI.");
        }

        Object firstChoice = choices.get(0);
        if (!(firstChoice instanceof Map<?, ?> choiceMap)) {
            throw new OpenAiException("OpenAI response format was invalid.");
        }

        Object messageObj = choiceMap.get("message");
        if (!(messageObj instanceof Map<?, ?> messageMap)) {
            throw new OpenAiException("OpenAI message payload was missing.");
        }

        Object contentObj = messageMap.get("content");
        if (!(contentObj instanceof String content) || content.isBlank()) {
            throw new OpenAiException("OpenAI returned an empty answer.");
        }

        return content.trim();
    }
}
