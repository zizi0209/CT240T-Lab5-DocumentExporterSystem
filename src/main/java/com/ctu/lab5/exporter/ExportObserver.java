package com.ctu.lab5.exporter;

/**
 * Observer interface: nhận sự kiện khi báo cáo được xuất thành công.
 */
public interface ExportObserver {

    void onExportSuccess(String fileName, int compressedSize);
}
