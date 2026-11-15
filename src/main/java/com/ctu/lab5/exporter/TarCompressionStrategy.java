package com.ctu.lab5.exporter;

/**
 * Concrete Strategy mới: nén dữ liệu theo chuẩn TAR.
 * Bổ sung theo nguyên lý OCP — DocumentExporterService chỉ gọi setCompressionStrategy(),
 * không cần sửa mã nguồn sẵn có của service.
 */
public class TarCompressionStrategy implements CompressionStrategy {

    @Override
    public byte[] compress(String rawData) {
        System.out.println("🗜️ Nén dữ liệu theo chuẩn TAR...");
        return ("TAR[" + rawData + "]").getBytes();
    }
}
