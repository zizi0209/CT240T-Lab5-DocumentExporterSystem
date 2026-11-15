package com.ctu.lab5.exporter;

/**
 * Factory Method: khởi tạo DocumentReader phù hợp theo định dạng tệp.
 * Client (DocumentExporterService) chỉ phụ thuộc vào interface, tuân thủ DIP.
 */
public class DocumentReaderFactory {

    private DocumentReaderFactory() {
    }

    public static DocumentReader getReader(String fileType) {
        if (fileType == null) {
            throw new IllegalArgumentException("File type null");
        }
        return switch (fileType.toUpperCase()) {
            case "PDF" -> new PDFReader();
            case "DOCX" -> new DocxReader();
            case "CSV" -> new CsvReader();
            default -> throw new IllegalArgumentException("Unsupported file type: " + fileType);
        };
    }
}
