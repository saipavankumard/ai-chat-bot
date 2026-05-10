package com.example.natureqa.service;

import com.example.natureqa.config.OpenAiProperties;
import com.example.natureqa.exception.EmbeddingDimensionMismatchException;
import com.example.natureqa.exception.OpenAiException;
import com.example.natureqa.exception.PdfProcessingException;
import com.example.natureqa.repository.DocumentChunkRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

@Service
public class OpenAiEmbeddingService {

    private final RestTemplate restTemplate;
    private final OpenAiProperties openAiProperties;

    public OpenAiEmbeddingService(RestTemplateBuilder restTemplateBuilder, OpenAiProperties openAiProperties) {
        this.openAiProperties = openAiProperties;
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(openAiProperties.getTimeoutMillis()))
                .setReadTimeout(Duration.ofMillis(openAiProperties.getTimeoutMillis()))
                .build();
    }

    /**
     * Returns one embedding list per input string, same order as {@code texts}.
     */
    public List<List<Double>> embedTexts(List<String> texts) {
        if (openAiProperties.getApiKey() == null || openAiProperties.getApiKey().isBlank()) {
            throw new OpenAiException("OpenAI API key is not configured on the server.");
        }
        String url = openAiProperties.getEmbeddingApiUrl();
        if (url == null || url.isBlank()) {
            throw new OpenAiException("OpenAI embedding API URL is not configured.");
        }
        String model = openAiProperties.getEmbeddingModel();
        if (model == null || model.isBlank()) {
            throw new OpenAiException("OpenAI embedding model is not configured.");
        }

        if (texts.isEmpty()) {
            return List.of();
        }

        int batchSize = Math.max(1, openAiProperties.getEmbeddingBatchSize());
        List<List<Double>> all = new ArrayList<>(texts.size());

        for (int offset = 0; offset < texts.size(); offset += batchSize) {
            int end = Math.min(offset + batchSize, texts.size());
            List<String> batch = texts.subList(offset, end);
            List<List<Double>> vectors = callEmbeddingsApi(url, model, batch);
            if (vectors.size() != batch.size()) {
                throw new OpenAiException("OpenAI returned an unexpected number of embeddings.");
            }
            all.addAll(vectors);
        }
        return all;
    }

    /**
     * Embeds a single query string for similarity search. Vector length must match pgvector column dimensions.
     */
    public List<Double> embedQuery(String text) {
        if (text == null || text.isBlank()) {
            throw new PdfProcessingException("Search query must not be empty.");
        }
        List<List<Double>> batch = embedTexts(List.of(text.trim()));
        List<Double> vector = batch.get(0);
        if (vector.size() != DocumentChunkRepository.EMBEDDING_DIMENSIONS) {
            throw new EmbeddingDimensionMismatchException(
                    "Expected embedding dimension " + DocumentChunkRepository.EMBEDDING_DIMENSIONS
                            + " for similarity search, got " + vector.size() + ".");
        }
        return vector;
    }

    @SuppressWarnings("unchecked")
    private List<List<Double>> callEmbeddingsApi(String url, String model, List<String> batch) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(openAiProperties.getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("input", batch);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response;
        try {
            response = restTemplate.postForEntity(url, entity, Map.class);
        } catch (HttpStatusCodeException ex) {
            int status = ex.getStatusCode().value();
            if (status == 401) {
                throw new OpenAiException("OpenAI rejected the API key. Set a valid OPENAI_API_KEY and restart backend.");
            }
            if (status == 429) {
                throw new OpenAiException("OpenAI rate limit/quota reached. Check billing and usage limits.");
            }
            throw new OpenAiException("OpenAI embeddings request failed with status " + status + ".");
        } catch (ResourceAccessException ex) {
            throw new OpenAiException("OpenAI request timed out or network is unavailable. Please try again.");
        } catch (RestClientException ex) {
            throw new OpenAiException("Could not reach OpenAI. Please try again.");
        }

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new OpenAiException("OpenAI returned an unexpected response.");
        }

        Object dataObj = response.getBody().get("data");
        if (!(dataObj instanceof List<?> dataList) || dataList.isEmpty()) {
            throw new OpenAiException("OpenAI returned no embedding data.");
        }

        List<List<Double>> ordered = new ArrayList<>(batch.size());
        for (int i = 0; i < batch.size(); i++) {
            ordered.add(null);
        }

        for (Object item : dataList) {
            if (!(item instanceof Map<?, ?> row)) {
                throw new OpenAiException("OpenAI embedding row format was invalid.");
            }
            Object indexObj = row.get("index");
            if (!(indexObj instanceof Number indexNum)) {
                throw new OpenAiException("OpenAI embedding index was missing.");
            }
            int idx = indexNum.intValue();
            if (idx < 0 || idx >= batch.size()) {
                throw new OpenAiException("OpenAI returned an out-of-range embedding index.");
            }
            Object embObj = row.get("embedding");
            if (!(embObj instanceof List<?> embList)) {
                throw new OpenAiException("OpenAI embedding vector was missing.");
            }
            List<Double> vector = new ArrayList<>(embList.size());
            for (Object n : embList) {
                if (n instanceof Number num) {
                    vector.add(num.doubleValue());
                } else {
                    throw new OpenAiException("OpenAI embedding vector contained a non-numeric value.");
                }
            }
            ordered.set(idx, vector);
        }

        for (List<Double> v : ordered) {
            if (v == null || v.isEmpty()) {
                throw new OpenAiException("OpenAI returned incomplete embedding data.");
            }
        }
        return ordered;
    }
}
