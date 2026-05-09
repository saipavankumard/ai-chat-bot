package com.example.natureqa.controller;

import com.example.natureqa.dto.AskRequest;
import com.example.natureqa.dto.AskResponse;
import com.example.natureqa.service.OpenAiService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AskController {

    private final OpenAiService openAiService;

    public AskController(OpenAiService openAiService) {
        this.openAiService = openAiService;
    }

    @PostMapping("/ask")
    public AskResponse ask(@Valid @RequestBody AskRequest request) {
        return new AskResponse(openAiService.ask(request.question()));
    }
}
