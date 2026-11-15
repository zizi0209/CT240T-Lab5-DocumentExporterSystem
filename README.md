# BÀI THỰC HÀNH 5: THIẾT KẾ & CÀI ĐẶT DESIGN PATTERNS (GOF)
## BÀI TẬP 1: HỆ THỐNG XỬ LÝ & XUẤT BÁO CÁO TÀI LIỆU (DOCUMENT EXPORTER SYSTEM)
**Môn học:** Nguyên lý xây dựng phần mềm (CT240T)  
**Đơn vị biên soạn:** Khoa Công nghệ Phần mềm - Trường CNTT&TT - Đại học Cần Thơ  
**Sinh viên thực hiện:** B2303858  

---

## 1. MỤC TIÊU BÀI THỰC HÀNH
- Cài đặt và kết hợp **3 Design Patterns** trên cùng một luồng nghiệp vụ xuất báo cáo:
  1. **Factory Method** — khởi tạo bộ đọc tài liệu (`PDFReader`, `DocxReader`, `CsvReader`) từ interface `DocumentReader`.
  2. **Strategy** — thay thuật toán nén lúc runtime (`ZipCompressionStrategy`, `RarCompressionStrategy`, `TarCompressionStrategy`).
  3. **Observer** — phát sự kiện xuất thành công tới `AuditLogObserver` và `ConsoleNotificationObserver`.
- Bổ sung định dạng **CSV** và thuật toán nén **TAR** **mà không sửa** phương thức `DocumentExporterService.exportDocument()` — kiểm chứng nguyên lý **OCP** (Open/Closed) và **DIP** (phụ thuộc vào interface, không phụ thuộc lớp cụ thể).
- Viết bộ kiểm thử **JUnit 5** (`DocumentExporterTest`) bắt buộc phải phủ kịch bản `IllegalStateException` khi chưa gắn Strategy nén.

---

## 2. CẤU TRÚC DỰ ÁN (PROJECT STRUCTURE)

```
CT240T-Lab5-DocumentExporterSystem/
├── pom.xml                                     # Maven, JDK 17, JUnit 5.10.2
├── mvnw / mvnw.cmd / .mvn                      # Maven Wrapper
├── .gitignore
├── README.md
└── src/
    ├── main/java/com/ctu/lab5/exporter/
    │   ├── DocumentReader.java                 # Factory — Product interface
    │   ├── PDFReader.java                      # Factory — Concrete Product
    │   ├── DocxReader.java                     # Factory — Concrete Product
    │   ├── CsvReader.java                      # Factory — Product mới (OCP)
    │   ├── DocumentReaderFactory.java          # Factory Method
    │   ├── CompressionStrategy.java            # Strategy — interface
    │   ├── ZipCompressionStrategy.java         # Strategy — ZIP
    │   ├── RarCompressionStrategy.java         # Strategy — RAR
    │   ├── TarCompressionStrategy.java         # Strategy — TAR (OCP)
    │   ├── ExportObserver.java                 # Observer — interface
    │   ├── AuditLogObserver.java               # Observer — nhật ký kiểm toán
    │   ├── ConsoleNotificationObserver.java    # Observer — thông báo console
    │   └── DocumentExporterService.java        # Context + Subject + main()
    └── test/java/com/ctu/lab5/exporter/
        └── DocumentExporterTest.java           # JUnit 5 — 29 test case
```

---

## 3. KIẾN TRÚC 3 DESIGN PATTERNS

```
                    ┌──────────────────────────┐
                    │ DocumentExporterService  │
                    │  (Context + Subject)     │
                    └────────────┬─────────────┘
           ┌─────────────────────┼──────────────────────┐
           │ 1. Factory          │ 2. Strategy          │ 3. Observer
           ▼                     ▼                      ▼
  DocumentReaderFactory   CompressionStrategy     ExportObserver
           │                     │                      │
     ┌─────┼──────┐        ┌─────┼──────┐         ┌─────┴──────┐
     ▼     ▼      ▼        ▼     ▼      ▼         ▼            ▼
   PDF   DOCX   CSV      ZIP   RAR    TAR    AuditLog   ConsoleNotification
```

### 3.1. Factory Method — khởi tạo bộ đọc tài liệu
- `DocumentExporterService` **không** `new PDFReader()` / `new DocxReader()` trực tiếp.
- Service gọi `DocumentReaderFactory.getReader(fileType)` và làm việc trên interface `DocumentReader` (DIP).
- Thêm `CsvReader` chỉ cần một `case "CSV"` trong factory; `exportDocument()` không đổi.

### 3.2. Strategy — thuật toán nén thay thế được
- Service giữ một tham chiếu `CompressionStrategy` và ủy thác `compress(rawContent)`.
- Client gọi `setCompressionStrategy(...)` để đổi ZIP ↔ RAR ↔ TAR lúc runtime, không cần `if-else` phì đại trong service (OCP).

### 3.3. Observer — phát sự kiện xuất thành công
- Service là Subject: `attachObserver()` + vòng `notify` sau khi nén xong.
- `AuditLogObserver` ghi nhật ký kích thước file; `ConsoleNotificationObserver` in thông báo.
- Các observer **không** phụ thuộc lẫn nhau (loose coupling).

### 3.4. Luồng `exportDocument()` (giữ nguyên skeleton)
1. Nếu chưa gắn Strategy → ném `IllegalStateException("Chưa thiết lập thuật toán nén!")`.
2. Factory tạo `DocumentReader` theo `fileType`, đọc nội dung thô.
3. Strategy nén chuỗi thô thành `byte[]`.
4. Duyệt danh sách Observer, gọi `onExportSuccess(filePath, compressedData.length)`.
5. Trả về dữ liệu đã nén.

---

## 4. KIỂM CHỨNG NGUYÊN LÝ OCP

Yêu cầu đề bài: bổ sung `CsvReader` và `TarCompressionStrategy` **không sửa mã nguồn sẵn có** của `DocumentExporterService`.

| Thành phần được thêm | File mới / chỗ sửa | `exportDocument()` |
| :--- | :--- | :---: |
| `CsvReader` | class mới + 1 `case "CSV"` trong factory | Không đụng |
| `TarCompressionStrategy` | class mới implements `CompressionStrategy` | Không đụng |
| Client sử dụng | `setCompressionStrategy(new TarCompressionStrategy())` rồi `exportDocument(..., "CSV")` | Không đụng |

Service chỉ phụ thuộc vào **interface** (`DocumentReader`, `CompressionStrategy`, `ExportObserver`). Mở rộng tính năng = thêm class mới + gắn lúc runtime.

---

## 5. KẾT QUẢ CHẠY THỰC TẾ (`DocumentExporterService.main`)

```text
🚀 Đang tiến hành xuất file PDF...
🗜️ Nén dữ liệu theo chuẩn ZIP...
📜 [Audit Log] Đã ghi nhật ký xuất file: BaoCaoTaiChinh2026.pdf (Kích thước: 50 bytes)
🔔 [Thông báo] Xuất báo cáo thành công file BaoCaoTaiChinh2026.pdf

🚀 Đang tiến hành xuất file DOCX...
🗜️ Nén dữ liệu theo chuẩn RAR...
📜 [Audit Log] Đã ghi nhật ký xuất file: BienBanHop.docx (Kích thước: 44 bytes)
🔔 [Thông báo] Xuất báo cáo thành công file BienBanHop.docx

🚀 Đang tiến hành xuất file CSV (mở rộng OCP)...
🗜️ Nén dữ liệu theo chuẩn TAR...
📜 [Audit Log] Đã ghi nhật ký xuất file: DanhSachSinhVien.csv (Kích thước: 48 bytes)
🔔 [Thông báo] Xuất báo cáo thành công file DanhSachSinhVien.csv
```

Quan sát luồng kết hợp:
- **Factory** chọn đúng reader theo `"PDF"` / `"DOCX"` / `"CSV"`.
- **Strategy** đổi ZIP → RAR → TAR giữa các lần gọi, không sửa service.
- **Observer** cả hai bộ lắng nghe đều nhận sự kiện sau mỗi lần xuất thành công.

---

## 6. HƯỚNG DẪN BIÊN DỊCH VÀ CHẠY NGHIỆM THU

### 6.1. Chạy toàn bộ Test JUnit 5 qua Maven Wrapper
Mở terminal tại thư mục `CT240T-Lab5-DocumentExporterSystem`:

```bash
./mvnw clean test
```

*(Windows PowerShell: `.\mvnw.cmd clean test`)*

Kết quả kiểm thử: **29/29 tests passed (100% Green Bar)**.

| Nhóm test (`@Nested`) | Số case | Nội dung |
| :--- | :---: | :--- |
| MissingStrategyTests | 2 | `IllegalStateException` khi chưa set Strategy (yêu cầu bắt buộc) |
| FactoryTests | 15 | PDF / DOCX / CSV, không phân biệt hoa thường, null, type không hỗ trợ |
| StrategyTests | 4 | ZIP / RAR / TAR và đổi Strategy lúc runtime |
| ObserverTests | 4 | Thứ tự thông báo, nội dung log, xuất khi không có observer |
| IntegrationTests | 4 | PDF+ZIP, CSV+TAR (OCP), DOCX+RAR, type không hỗ trợ |

### 6.2. Chạy chương trình demo

```bash
./mvnw -q -DskipTests compile
java -cp target/classes com.ctu.lab5.exporter.DocumentExporterService
```

Hoặc mở `DocumentExporterService.java` trên IntelliJ IDEA và Run `main()`.

---

## 7. CHECKLIST NGHIỆM THU BÀI TẬP 1

- [x] **Factory Method:** `DocumentReaderFactory` tạo `PDFReader`, `DocxReader`, `CsvReader`; client không phụ thuộc lớp cụ thể.
- [x] **Strategy:** ZIP / RAR / TAR thay thế được lúc runtime qua `setCompressionStrategy()`.
- [x] **Observer:** `AuditLogObserver` và `ConsoleNotificationObserver` nhận sự kiện xuất thành công.
- [x] **OCP / DIP:** thêm CSV + TAR không sửa `exportDocument()`.
- [x] **Unit Testing:** JUnit 5 — 29/29 Green Bar, gồm bắt buộc `IllegalStateException` khi chưa set Strategy.
- [x] **Git:** repository `CT240T-Lab5-DocumentExporterSystem`, commit Conventional Commits, sẵn sàng push lên GitHub.
