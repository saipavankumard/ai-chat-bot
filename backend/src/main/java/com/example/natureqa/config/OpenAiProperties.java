package com.example.natureqa.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "openai")
public class OpenAiProperties {
    private String apiKey;
    private String model;
    private String apiUrl;
    private int timeoutMillis;
    private String embeddingApiUrl;
    private String embeddingModel;
    private int embeddingChunkSizeChars = 1000;
    private int embeddingChunkOverlapChars = 150;
    private int embeddingBatchSize = 64;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public int getTimeoutMillis() {
        return timeoutMillis;
    }

    public void setTimeoutMillis(int timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    public String getEmbeddingApiUrl() {
        return embeddingApiUrl;
    }

    public void setEmbeddingApiUrl(String embeddingApiUrl) {
        this.embeddingApiUrl = embeddingApiUrl;
    }

    public String getEmbeddingModel() {
        return embeddingModel;
    }

    public void setEmbeddingModel(String embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public int getEmbeddingChunkSizeChars() {
        return embeddingChunkSizeChars;
    }

    public void setEmbeddingChunkSizeChars(int embeddingChunkSizeChars) {
        this.embeddingChunkSizeChars = embeddingChunkSizeChars;
    }

    public int getEmbeddingChunkOverlapChars() {
        return embeddingChunkOverlapChars;
    }

    public void setEmbeddingChunkOverlapChars(int embeddingChunkOverlapChars) {
        this.embeddingChunkOverlapChars = embeddingChunkOverlapChars;
    }

    public int getEmbeddingBatchSize() {
        return embeddingBatchSize;
    }

    public void setEmbeddingBatchSize(int embeddingBatchSize) {
        this.embeddingBatchSize = embeddingBatchSize;
    }
}
