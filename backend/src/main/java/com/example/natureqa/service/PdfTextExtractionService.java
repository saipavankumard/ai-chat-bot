package com.example.natureqa.service;

import com.example.natureqa.dto.PdfExtractResponse;
import com.example.natureqa.exception.PdfProcessingException;
import java.io.IOException;
import java.util.Locale;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PdfTextExtractionService {

    public PdfExtractResponse extract(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new PdfProcessingException("No file uploaded.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && !originalFilename.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw new PdfProcessingException("Only PDF files are supported.");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new PdfProcessingException("Could not read uploaded file.", e);
        }

        try (PDDocument document = Loader.loadPDF(bytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String raw = stripper.getText(document);
            String text = raw == null ? "" : raw.trim();
            String name = originalFilename != null && !originalFilename.isBlank()
                    ? originalFilename
                    : "upload.pdf";
            return new PdfExtractResponse(name, text, document.getNumberOfPages());
        } catch (IOException e) {
            throw new PdfProcessingException("Invalid or unreadable PDF.", e);
        }
    }
}
