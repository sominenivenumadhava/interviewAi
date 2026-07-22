package com.interviai.backend.module.resume.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for extracting text from PDF files.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Component
@Slf4j
public class PdfTextExtractor {

    private static final int MAX_TEXT_LENGTH = 1000000; // 1MB text limit
    private static final int MAX_PAGES = 50; // Maximum pages to process

    /**
     * Extract text from PDF byte array.
     */
    public String extractText(byte[] pdfBytes) {
        if (pdfBytes == null || pdfBytes.length == 0) {
            log.warn("PDF bytes are null or empty");
            return "";
        }

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(pdfBytes)) {
            return extractText(inputStream);
        } catch (Exception e) {
            log.error("Error extracting text from PDF bytes", e);
            return "";
        }
    }

    /**
     * Extract text from PDF input stream.
     */
    public String extractText(InputStream inputStream) {
        if (inputStream == null) {
            log.warn("Input stream is null");
            return "";
        }

        try (PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
            return extractTextFromDocument(document);
        } catch (IOException e) {
            log.error("Error loading PDF document", e);
            return "";
        } catch (Exception e) {
            log.error("Unexpected error extracting text from PDF", e);
            return "";
        }
    }

    /**
     * Extract text from PDF document with page limit.
     */
    private String extractTextFromDocument(PDDocument document) throws IOException {
        if (document == null) {
            log.warn("PDF document is null");
            return "";
        }

        int pageCount = document.getNumberOfPages();
        log.debug("PDF has {} pages", pageCount);

        if (pageCount == 0) {
            log.warn("PDF has no pages");
            return "";
        }

        // Limit pages to process
        int pagesToProcess = Math.min(pageCount, MAX_PAGES);
        if (pageCount > MAX_PAGES) {
            log.warn("PDF has {} pages, processing only first {}", pageCount, MAX_PAGES);
        }

        PDFTextStripper textStripper = new PDFTextStripper();
        textStripper.setStartPage(1);
        textStripper.setEndPage(pagesToProcess);
        
        // Configure text stripper for better parsing
        textStripper.setSortByPosition(true);
        textStripper.setWordSeparator(" ");
        textStripper.setLineSeparator("\n");

        String extractedText = textStripper.getText(document);
        
        if (extractedText == null) {
            log.warn("Extracted text is null");
            return "";
        }

        // Limit text length
        if (extractedText.length() > MAX_TEXT_LENGTH) {
            log.warn("Extracted text is {} characters, truncating to {}", 
                     extractedText.length(), MAX_TEXT_LENGTH);
            extractedText = extractedText.substring(0, MAX_TEXT_LENGTH);
        }

        // Clean up the text
        String cleanedText = cleanExtractedText(extractedText);
        
        log.debug("Successfully extracted {} characters from PDF", cleanedText.length());
        return cleanedText;
    }

    /**
     * Extract text page by page for better control.
     */
    public List<String> extractTextByPages(byte[] pdfBytes) {
        List<String> pages = new ArrayList<>();
        
        if (pdfBytes == null || pdfBytes.length == 0) {
            log.warn("PDF bytes are null or empty");
            return pages;
        }

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(pdfBytes);
             PDDocument document = Loader.loadPDF(pdfBytes)) {

            int pageCount = Math.min(document.getNumberOfPages(), MAX_PAGES);
            
            for (int i = 1; i <= pageCount; i++) {
                try {
                    PDFTextStripper textStripper = new PDFTextStripper();
                    textStripper.setStartPage(i);
                    textStripper.setEndPage(i);
                    textStripper.setSortByPosition(true);
                    
                    String pageText = textStripper.getText(document);
                    if (pageText != null && !pageText.trim().isEmpty()) {
                        pages.add(cleanExtractedText(pageText));
                    }
                } catch (Exception e) {
                    log.warn("Error extracting text from page {}: {}", i, e.getMessage());
                    // Continue with next page
                }
            }
            
        } catch (Exception e) {
            log.error("Error extracting text pages from PDF", e);
        }

        return pages;
    }

    /**
     * Get PDF metadata and basic info.
     */
    public PdfInfo getPdfInfo(byte[] pdfBytes) {
        if (pdfBytes == null || pdfBytes.length == 0) {
            return PdfInfo.builder().build();
        }

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(pdfBytes);
             PDDocument document = Loader.loadPDF(pdfBytes)) {

            return PdfInfo.builder()
                    .pageCount(document.getNumberOfPages())
                    .title(getDocumentTitle(document))
                    .author(getDocumentAuthor(document))
                    .isEncrypted(document.isEncrypted())
                    .fileSize((long) pdfBytes.length)
                    .textLength(extractTextFromDocument(document).length())
                    .build();
                    
        } catch (Exception e) {
            log.error("Error getting PDF info", e);
            return PdfInfo.builder()
                    .fileSize((long) pdfBytes.length)
                    .build();
        }
    }

    /**
     * Check if PDF is valid and readable.
     */
    public boolean isValidPdf(byte[] pdfBytes) {
        if (pdfBytes == null || pdfBytes.length == 0) {
            return false;
        }

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(pdfBytes);
             PDDocument document = Loader.loadPDF(pdfBytes)) {
            
            // Check if document can be loaded and has pages
            return document.getNumberOfPages() > 0;
            
        } catch (Exception e) {
            log.debug("PDF validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Clean extracted text by removing extra whitespace and formatting issues.
     */
    private String cleanExtractedText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // Replace multiple spaces with single space
        text = text.replaceAll("[ \\t]+", " ");
        
        // Replace multiple newlines with double newline
        text = text.replaceAll("\\n{3,}", "\n\n");
        
        // Remove leading/trailing whitespace from each line
        String[] lines = text.split("\n");
        StringBuilder cleanedText = new StringBuilder();
        
        for (String line : lines) {
            String trimmedLine = line.trim();
            if (!trimmedLine.isEmpty()) {
                cleanedText.append(trimmedLine).append("\n");
            }
        }
        
        return cleanedText.toString().trim();
    }

    /**
     * Get document title from metadata.
     */
    private String getDocumentTitle(PDDocument document) {
        try {
            if (document.getDocumentInformation() != null) {
                return document.getDocumentInformation().getTitle();
            }
        } catch (Exception e) {
            log.debug("Error getting document title: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Get document author from metadata.
     */
    private String getDocumentAuthor(PDDocument document) {
        try {
            if (document.getDocumentInformation() != null) {
                return document.getDocumentInformation().getAuthor();
            }
        } catch (Exception e) {
            log.debug("Error getting document author: {}", e.getMessage());
        }
        return null;
    }

    /**
     * PDF information data class.
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PdfInfo {
        private Integer pageCount;
        private String title;
        private String author;
        private Boolean isEncrypted;
        private Long fileSize;
        private Integer textLength;
    }
}