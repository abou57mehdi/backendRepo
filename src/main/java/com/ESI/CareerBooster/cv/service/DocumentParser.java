package com.ESI.CareerBooster.cv.service;

import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class DocumentParser {
    private final Tika tika;

    public String parseDocument(MultipartFile file) {
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds 5MB limit");
        }

        String mimeType = tika.detect(file.getOriginalFilename());
        if (!mimeType.equals("application/pdf") && 
            !mimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
            throw new IllegalArgumentException("Only PDF and DOCX files are supported");
        }

        try {
            return tika.parseToString(file.getInputStream());
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse document", e);
        }
    }
} 