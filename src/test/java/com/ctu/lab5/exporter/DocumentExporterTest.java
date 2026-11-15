package com.ctu.lab5.exporter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử hệ thống xuất báo cáo tài liệu (Document Exporter System)")
class DocumentExporterTest {

    @Nested
    @DisplayName("Yêu cầu bắt buộc: IllegalStateException khi chưa set Strategy nén")
    class MissingStrategyTests {

        @Test
        @DisplayName("exportDocument() ném IllegalStateException với thông báo đúng khi chưa setCompressionStrategy")
        void exportWithoutStrategy_throwsIllegalStateException() {
            DocumentExporterService exporter = new DocumentExporterService();

            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> exporter.exportDocument("BaoCaoTaiChinh2026.pdf", "PDF")
            );

            assertEquals("Chưa thiết lập thuật toán nén!", ex.getMessage());
        }

        @Test
        @DisplayName("IllegalStateException xảy ra trước khi Factory/Observer được gọi")
        void exportWithoutStrategy_doesNotNotifyObservers() {
            DocumentExporterService exporter = new DocumentExporterService();
            RecordingObserver observer = new RecordingObserver();
            exporter.attachObserver(observer);

            assertThrows(IllegalStateException.class,
                    () -> exporter.exportDocument("test.pdf", "PDF"));
            assertTrue(observer.events.isEmpty(), "Observer không được thông báo khi Strategy chưa gắn");
        }
    }

    @Nested
    @DisplayName("Factory Method Pattern — DocumentReaderFactory")
    class FactoryTests {

        @Test
        @DisplayName("PDF → PDFReader trả về nội dung đúng")
        void getReader_pdf() {
            DocumentReader reader = DocumentReaderFactory.getReader("PDF");
            assertInstanceOf(PDFReader.class, reader);
            assertEquals("Content from PDF file: report.pdf", reader.readContent("report.pdf"));
        }

        @Test
        @DisplayName("DOCX → DocxReader trả về nội dung đúng")
        void getReader_docx() {
            DocumentReader reader = DocumentReaderFactory.getReader("DOCX");
            assertInstanceOf(DocxReader.class, reader);
            assertEquals("Content from DOCX file: notes.docx", reader.readContent("notes.docx"));
        }

        @Test
        @DisplayName("CSV → CsvReader (mở rộng OCP) trả về nội dung đúng")
        void getReader_csv() {
            DocumentReader reader = DocumentReaderFactory.getReader("CSV");
            assertInstanceOf(CsvReader.class, reader);
            assertEquals("Content from CSV file: students.csv", reader.readContent("students.csv"));
        }

        @ParameterizedTest(name = "fileType = \"{0}\" vẫn tạo đúng reader (không phân biệt hoa thường)")
        @ValueSource(strings = {"pdf", "Pdf", "docx", "Docx", "csv", "Csv"})
        void getReader_isCaseInsensitive(String fileType) {
            assertDoesNotThrow(() -> DocumentReaderFactory.getReader(fileType));
        }

        @Test
        @DisplayName("fileType null → IllegalArgumentException(\"File type null\")")
        void getReader_nullType() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> DocumentReaderFactory.getReader(null)
            );
            assertEquals("File type null", ex.getMessage());
        }

        @ParameterizedTest(name = "fileType không hỗ trợ = \"{0}\"")
        @NullAndEmptySource
        @ValueSource(strings = {"TXT", "XLSX", "JSON"})
        void getReader_unsupportedType(String fileType) {
            if (fileType == null) {
                assertThrows(IllegalArgumentException.class,
                        () -> DocumentReaderFactory.getReader(null));
                return;
            }
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> DocumentReaderFactory.getReader(fileType)
            );
            if (fileType.isEmpty()) {
                assertTrue(ex.getMessage().contains("Unsupported file type"));
            } else {
                assertEquals("Unsupported file type: " + fileType, ex.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("Strategy Pattern — thuật toán nén")
    class StrategyTests {

        @Test
        @DisplayName("ZipCompressionStrategy bọc dữ liệu trong ZIP[...]")
        void zipCompress() {
            byte[] actual = new ZipCompressionStrategy().compress("hello");
            assertArrayEquals("ZIP[hello]".getBytes(), actual);
        }

        @Test
        @DisplayName("RarCompressionStrategy bọc dữ liệu trong RAR[...]")
        void rarCompress() {
            byte[] actual = new RarCompressionStrategy().compress("hello");
            assertArrayEquals("RAR[hello]".getBytes(), actual);
        }

        @Test
        @DisplayName("TarCompressionStrategy (mở rộng OCP) bọc dữ liệu trong TAR[...]")
        void tarCompress() {
            byte[] actual = new TarCompressionStrategy().compress("hello");
            assertArrayEquals("TAR[hello]".getBytes(), actual);
        }

        @Test
        @DisplayName("Có thể đổi Strategy lúc runtime mà không sửa DocumentExporterService")
        void swapStrategyAtRuntime() {
            DocumentExporterService exporter = new DocumentExporterService();
            String filePath = "data.csv";

            exporter.setCompressionStrategy(new ZipCompressionStrategy());
            byte[] zip = exporter.exportDocument(filePath, "CSV");
            assertArrayEquals("ZIP[Content from CSV file: data.csv]".getBytes(), zip);

            exporter.setCompressionStrategy(new TarCompressionStrategy());
            byte[] tar = exporter.exportDocument(filePath, "CSV");
            assertArrayEquals("TAR[Content from CSV file: data.csv]".getBytes(), tar);
        }
    }

    @Nested
    @DisplayName("Observer Pattern — AuditLog & ConsoleNotification")
    class ObserverTests {

        @Test
        @DisplayName("Cả hai Observer được gọi đúng 1 lần, theo thứ tự đăng ký, với kích thước nén đúng")
        void bothObserversNotifiedInAttachOrder() {
            DocumentExporterService exporter = new DocumentExporterService();
            RecordingObserver first = new RecordingObserver();
            RecordingObserver second = new RecordingObserver();
            exporter.attachObserver(first);
            exporter.attachObserver(second);
            exporter.setCompressionStrategy(new ZipCompressionStrategy());

            String filePath = "BaoCaoTaiChinh2026.pdf";
            byte[] data = exporter.exportDocument(filePath, "PDF");

            assertEquals(1, first.events.size());
            assertEquals(1, second.events.size());
            assertEquals(filePath, first.events.get(0).fileName);
            assertEquals(data.length, first.events.get(0).compressedSize);
            assertEquals(filePath, second.events.get(0).fileName);
            assertEquals(data.length, second.events.get(0).compressedSize);
        }

        @Test
        @DisplayName("AuditLogObserver in nhật ký kèm kích thước file")
        void auditLogObserverPrintsExpectedMessage() {
            String output = captureStdout(() -> {
                new AuditLogObserver().onExportSuccess("report.pdf", 42);
            });
            assertTrue(output.contains("[Audit Log] Đã ghi nhật ký xuất file: report.pdf (Kích thước: 42 bytes)"));
        }

        @Test
        @DisplayName("ConsoleNotificationObserver in thông báo xuất thành công")
        void consoleNotificationObserverPrintsExpectedMessage() {
            String output = captureStdout(() -> {
                new ConsoleNotificationObserver().onExportSuccess("report.pdf", 42);
            });
            assertTrue(output.contains("[Thông báo] Xuất báo cáo thành công file report.pdf"));
        }

        @Test
        @DisplayName("Không đăng ký Observer vẫn xuất được (danh sách rỗng)")
        void exportWithoutObservers_stillReturnsCompressedData() {
            DocumentExporterService exporter = new DocumentExporterService();
            exporter.setCompressionStrategy(new ZipCompressionStrategy());

            byte[] data = exporter.exportDocument("a.pdf", "PDF");
            assertArrayEquals("ZIP[Content from PDF file: a.pdf]".getBytes(), data);
        }
    }

    @Nested
    @DisplayName("Tích hợp 3 Pattern & kiểm chứng OCP")
    class IntegrationTests {

        @Test
        @DisplayName("Luồng skeleton: PDF + ZIP + 2 Observer")
        void exportPdfWithZip() {
            DocumentExporterService exporter = new DocumentExporterService();
            RecordingObserver observer = new RecordingObserver();
            exporter.attachObserver(observer);
            exporter.setCompressionStrategy(new ZipCompressionStrategy());

            String filePath = "BaoCaoTaiChinh2026.pdf";
            byte[] data = exporter.exportDocument(filePath, "PDF");

            assertArrayEquals("ZIP[Content from PDF file: BaoCaoTaiChinh2026.pdf]".getBytes(), data);
            assertEquals(1, observer.events.size());
            assertEquals(filePath, observer.events.get(0).fileName);
            assertEquals(data.length, observer.events.get(0).compressedSize);
        }

        @Test
        @DisplayName("OCP: CSV + TAR chạy qua exportDocument() gốc, không sửa service")
        void exportCsvWithTar_withoutModifyingService() {
            DocumentExporterService exporter = new DocumentExporterService();
            RecordingObserver observer = new RecordingObserver();
            exporter.attachObserver(observer);
            exporter.setCompressionStrategy(new TarCompressionStrategy());

            String filePath = "DanhSachSinhVien.csv";
            byte[] data = exporter.exportDocument(filePath, "CSV");

            assertArrayEquals("TAR[Content from CSV file: DanhSachSinhVien.csv]".getBytes(), data);
            assertEquals(filePath, observer.events.get(0).fileName);
            assertEquals(data.length, observer.events.get(0).compressedSize);
        }

        @Test
        @DisplayName("OCP: DOCX + RAR chạy qua exportDocument() gốc")
        void exportDocxWithRar() {
            DocumentExporterService exporter = new DocumentExporterService();
            exporter.setCompressionStrategy(new RarCompressionStrategy());

            byte[] data = exporter.exportDocument("BienBanHop.docx", "DOCX");
            assertArrayEquals("RAR[Content from DOCX file: BienBanHop.docx]".getBytes(), data);
        }

        @Test
        @DisplayName("fileType không hỗ trợ sau khi đã set Strategy → IllegalArgumentException từ Factory")
        void exportUnsupportedType_afterStrategySet() {
            DocumentExporterService exporter = new DocumentExporterService();
            exporter.setCompressionStrategy(new ZipCompressionStrategy());

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> exporter.exportDocument("notes.txt", "TXT")
            );
            assertEquals("Unsupported file type: TXT", ex.getMessage());
        }
    }

    private static String captureStdout(Runnable action) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream capture = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        System.setOut(capture);
        try {
            action.run();
        } finally {
            System.setOut(original);
            capture.close();
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    /**
     * Observer giả lập dùng trong test: ghi lại sự kiện để khẳng định thứ tự và dữ liệu.
     */
    private static final class RecordingObserver implements ExportObserver {
        final List<ExportEvent> events = new ArrayList<>();

        @Override
        public void onExportSuccess(String fileName, int compressedSize) {
            events.add(new ExportEvent(fileName, compressedSize));
        }
    }

    private static final class ExportEvent {
        final String fileName;
        final int compressedSize;

        ExportEvent(String fileName, int compressedSize) {
            this.fileName = fileName;
            this.compressedSize = compressedSize;
        }
    }
}
