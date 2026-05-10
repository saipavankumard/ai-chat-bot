package com.example.natureqa.controller;

import com.example.natureqa.dto.AskRequest;
import com.example.natureqa.dto.AskResponse;
import com.example.natureqa.service.OpenAiService;
import com.example.natureqa.service.PdfRagAnswerService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class AskController {

    private final OpenAiService openAiService;
    private final PdfRagAnswerService pdfRagAnswerService;

    public AskController(OpenAiService openAiService, PdfRagAnswerService pdfRagAnswerService) {
        this.openAiService = openAiService;
        this.pdfRagAnswerService = pdfRagAnswerService;
    }

    @PostMapping("/ask/direct")
    public AskResponse askDirect(@Valid @RequestBody AskRequest request) {
        return new AskResponse(openAiService.ask(request.question()));
    }

    @PostMapping(value = "/ask/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AskResponse askPdf(@RequestParam("file") MultipartFile file, @RequestParam("question") String question) {
        return new AskResponse(pdfRagAnswerService.answer(file, question));
    }
}
