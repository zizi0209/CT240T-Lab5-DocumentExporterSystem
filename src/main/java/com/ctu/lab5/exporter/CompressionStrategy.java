package com.ctu.lab5.exporter;

/**
 * Strategy interface: thuật toán nén dữ liệu báo cáo có thể thay thế lúc runtime.
 */
public interface CompressionStrategy {

    byte[] compress(String rawData);
}
