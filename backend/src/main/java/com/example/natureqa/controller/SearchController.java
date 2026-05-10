package com.example.natureqa.controller;

import com.example.natureqa.dto.SearchRequest;
import com.example.natureqa.dto.SearchResponse;
import com.example.natureqa.service.SimilaritySearchService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SearchController {

    private final SimilaritySearchService similaritySearchService;

    public SearchController(SimilaritySearchService similaritySearchService) {
        this.similaritySearchService = similaritySearchService;
    }

    @PostMapping("/search")
    public SearchResponse search(@Valid @RequestBody SearchRequest request) {
        return similaritySearchService.search(request.query(), request.limit());
    }
}
