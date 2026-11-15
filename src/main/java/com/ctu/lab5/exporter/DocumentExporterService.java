package com.ctu.lab5.exporter;

import java.util.ArrayList;
import java.util.List;

/**
 * Context/Subject kết hợp 3 Design Patterns:
 * - Factory Method: đọc nội dung tài liệu theo định dạng.
 * - Strategy: nén dữ liệu theo thuật toán được gắn lúc runtime.
 * - Observer: phát sự kiện xuất thành công tới các bộ lắng nghe đã đăng ký.
 *
 * Tuân thủ OCP: mở rộng reader/strategy mới không cần sửa phương thức exportDocument().
 */
public class DocumentExporterService {

    private CompressionStrategy compressionStrategy;
    private final List<ExportObserver> observers = new ArrayList<>();

    public void setCompressionStrategy(CompressionStrategy strategy) {
        this.compressionStrategy = strategy;
    }

    public void attachObserver(ExportObserver observer) {
        observers.add(observer);
    }

    public byte[] exportDocument(String filePath, String fileType) {
        if (compressionStrategy == null) {
            throw new IllegalStateException("Chưa thiết lập thuật toán nén!");
        }
        // 1. Dùng Factory Method đọc nội dung
        DocumentReader reader = DocumentReaderFactory.getReader(fileType);
        String rawContent = reader.readContent(filePath);
        // 2. Dùng Strategy Pattern nén dữ liệu
        byte[] compressedData = compressionStrategy.compress(rawContent);
        // 3. Dùng Observer Pattern phát sự kiện
        for (ExportObserver obs : observers) {
            obs.onExportSuccess(filePath, compressedData.length);
        }
        return compressedData;
    }

    /**
     * Runner kiểm thử hệ thống: skeleton gốc (PDF + ZIP) và phần mở rộng OCP (DOCX + RAR, CSV + TAR).
     * Phương thức exportDocument() giữ nguyên, client chỉ gắn Strategy / truyền fileType mới.
     */
    public static void main(String[] args) {
        DocumentExporterService exporter = new DocumentExporterService();

        // Đăng ký Observers
        exporter.attachObserver(new AuditLogObserver());
        exporter.attachObserver(new ConsoleNotificationObserver());

        // Thiết lập Strategy nén ZIP — kịch bản skeleton gốc
        exporter.setCompressionStrategy(new ZipCompressionStrategy());
        System.out.println("🚀 Đang tiến hành xuất file PDF...");
        exporter.exportDocument("BaoCaoTaiChinh2026.pdf", "PDF");

        System.out.println();

        // Đổi Strategy lúc runtime (RAR) mà không sửa exportDocument()
        exporter.setCompressionStrategy(new RarCompressionStrategy());
        System.out.println("🚀 Đang tiến hành xuất file DOCX...");
        exporter.exportDocument("BienBanHop.docx", "DOCX");

        System.out.println();

        // Mở rộng OCP: CsvReader + TarCompressionStrategy — không sửa exportDocument()
        exporter.setCompressionStrategy(new TarCompressionStrategy());
        System.out.println("🚀 Đang tiến hành xuất file CSV (mở rộng OCP)...");
        exporter.exportDocument("DanhSachSinhVien.csv", "CSV");
    }
}
