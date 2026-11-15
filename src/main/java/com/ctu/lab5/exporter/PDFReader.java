package com.ctu.lab5.exporter;

/**
 * Concrete Product: đọc nội dung từ tệp PDF.
 */
public class PDFReader implements DocumentReader {

    @Override
    public String readContent(String filePath) {
        return "Content from PDF file: " + filePath;
    }
}
