package com.example.natureqa.controller;

import com.example.natureqa.dto.PdfEmbedResponse;
import com.example.natureqa.dto.PdfExtractResponse;
import com.example.natureqa.service.PdfEmbedService;
import com.example.natureqa.service.PdfTextExtractionService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    private final PdfTextExtractionService pdfTextExtractionService;
    private final PdfEmbedService pdfEmbedService;

    public PdfController(PdfTextExtractionService pdfTextExtractionService, PdfEmbedService pdfEmbedService) {
        this.pdfTextExtractionService = pdfTextExtractionService;
        this.pdfEmbedService = pdfEmbedService;
    }

    @PostMapping(value = "/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PdfExtractResponse extract(@RequestParam("file") MultipartFile file) {
        return pdfTextExtractionService.extract(file);
    }

    @PostMapping(value = "/embed", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PdfEmbedResponse embed(@RequestParam("file") MultipartFile file) {
        return pdfEmbedService.embed(file);
    }
}
