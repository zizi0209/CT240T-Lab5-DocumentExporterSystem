package com.ctu.lab5.exporter;

/**
 * Concrete Observer: ghi nhật ký kiểm toán mỗi khi xuất báo cáo thành công.
 */
public class AuditLogObserver implements ExportObserver {

    @Override
    public void onExportSuccess(String fileName, int compressedSize) {
        System.out.println("📜 [Audit Log] Đã ghi nhật ký xuất file: " + fileName
                + " (Kích thước: " + compressedSize + " bytes)");
    }
}
