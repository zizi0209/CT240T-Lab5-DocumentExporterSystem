package com.ctu.lab5.exporter;

/**
 * Product interface của Factory Method Pattern.
 * Mỗi định dạng tài liệu (PDF, DOCX, CSV, ...) cung cấp một cách đọc nội dung riêng.
 */
public interface DocumentReader {

    String readContent(String filePath);
}
