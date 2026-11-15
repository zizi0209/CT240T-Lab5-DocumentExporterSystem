package com.ctu.lab5.exporter;

/**
 * Concrete Product mới: đọc nội dung từ tệp CSV.
 * Bổ sung theo nguyên lý OCP — không sửa DocumentExporterService.
 */
public class CsvReader implements DocumentReader {

    @Override
    public String readContent(String filePath) {
        return "Content from CSV file: " + filePath;
    }
}
