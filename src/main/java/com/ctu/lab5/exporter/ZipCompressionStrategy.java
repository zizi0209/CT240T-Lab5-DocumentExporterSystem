package com.ctu.lab5.exporter;

/**
 * Concrete Strategy: nén dữ liệu theo chuẩn ZIP.
 */
public class ZipCompressionStrategy implements CompressionStrategy {

    @Override
    public byte[] compress(String rawData) {
        System.out.println("🗜️ Nén dữ liệu theo chuẩn ZIP...");
        return ("ZIP[" + rawData + "]").getBytes();
    }
}
