# HƯỚNG DẪN SỬ DỤNG VÀ VẬN HÀNH ĐỒ ÁN
## HỆ THỐNG NỘP BÀI TẬP TRỰC TUYẾN QUA MẠNG CỤC BỘ (LAN)

> **Môn học:** Lập trình Mạng (Network Programming)  
> **Kiến trúc:** Client - Server (TCP Socket + Multi-threading + I/O Streams)  
> **Giao diện:** Java Swing GUI & CLI (Command-Line Interface)  
> **Cơ sở dữ liệu:** SQLite Database nhúng tự động (Tích hợp Migration Engine)  
> **Bảo mật & Toàn vẹn:** Băm mã hóa SHA-256, Chống tấn công Path Traversal, Chặn file thực thi độc hại, Tự động đánh số phiên bản file (Versioning), Quản lý Hạn nộp bài & Ca thi (Deadline & Gate Control).

---

## MỤC LỤC
1. [Giới thiệu tổng quan đồ án](#1-giới-thiệu-tổng-quan-đồ-án)
2. [Cấu trúc thư mục dự án](#2-cấu-trúc-thư-mục-dự-án)
3. [Các tính năng trọng tâm của hệ thống](#3-các-tính-năng-trọng-tâm-của-hệ-thống)
4. [Yêu cầu môi trường cài đặt](#4-yêu-cầu-môi-trường-cài-đặt)
5. [Hướng dẫn biên dịch và chạy chương trình](#5-hướng-dẫn-biên-dịch-và-chạy-chương-trình)
6. [Hướng dẫn kết nối trong mạng LAN thực tế](#6-hướng-dẫn-kết-nối-trong-mạng-lan-thực-tế)
7. [Kịch bản kiểm thử & Báo cáo Demo](#7-kịch-bản-kiểm-thử--báo-cáo-demo)
8. [Cơ sở dữ liệu & Cơ chế Database Migration](#8-cơ-sở-dữ-liệu--cơ-chế-database-migration)
9. [Bộ câu hỏi vấn đáp thường gặp khi bảo vệ đồ án](#9-bộ-câu-hỏi-vấn-đáp-thường-gặp-khi-bảo-vệ-đồ-án)

---

## 1. GIỚI THIỆU TỔNG QUAN ĐỒ ÁN

Trong các giờ thi thực hành hoặc kiểm tra môn học tại giảng đường và phòng máy tính (Lab), nhu cầu thu thập bài nộp của sinh viên một cách nhanh chóng, bảo mật và chính xác là rất lớn. Các phương thức truyền thống như dùng USB dễ lây nhiễm virus, hoặc nộp qua Google Drive/Email lại đòi hỏi kết nối Internet bên ngoài (dễ tạo cơ hội cho sinh viên trao đổi qua Internet).

**Hệ thống Nộp Bài Tập Trực Tuyến Qua LAN** ra đời nhằm giải quyết triệt để các vấn đề trên:
- **Hoạt động độc lập trong mạng nội bộ (LAN):** Không yêu cầu kết nối Internet.
- **Truyền tải tin cậy qua TCP Socket:** Đảm bảo toàn bộ byte dữ liệu của file bài nộp được truyền tải chính xác, không thất lạc hay đảo lộn thứ tự.
- **Xử lý đồng thời (Multi-threaded):** Phục vụ cùng lúc hàng chục, hàng trăm sinh viên nộp bài đồng thời trong phòng máy mà không bị nghẽn mạng.
- **Toàn vẹn & Chống gian lận:** Tính toán mã băm SHA-256 theo thời gian thực để đối soát toàn vẹn file, tự động cảnh báo bài nộp có nội dung giống hệt nhau giữa các sinh viên.
- **Quản lý hạn chót (Deadline):** Tự động khóa cổng nộp hoặc gắn cờ nộp muộn khi hết thời gian quy định của ca thi.
- **Lưu trữ CSDL SQLite tự động:** Không đòi hỏi sinh viên hay giảng viên phải cài đặt MySQL Server phức tạp, toàn bộ dữ liệu bài nộp và sinh viên được lưu trữ bền vững trong file `damh.db`.

---

## 2. CẤU TRÚC THƯ MỤC DỰ ÁN

```text
DAMH/
│
├── dist/
│   ├── DAMH.jar                 # File thực thi chương trình sau khi build
│   └── README.TXT
│
├── lib/
│   └── sqlite-jdbc-3.36.0.3.jar # Thư viện kết nối CSDL SQLite
│
├── migrations/                  # Các kịch bản tạo bảng & nâng cấp CSDL tự động
│   ├── V1__create_tables.sql
│   ├── V2__seed_sample_students.sql
│   ├── V3__add_file_hash.sql
│   ├── V4__add_system_config.sql
│   └── mysql_schema.sql         # Script tham khảo nếu chuyển sang dùng MySQL
│
├── nbproject/                   # Cấu hình dự án của Apache NetBeans IDE
│   ├── build-impl.xml
│   └── project.properties
│
├── src/                         # Mã nguồn Java chính
│   └── damh/
│       ├── DAMH.java            # Cửa sổ khởi chạy (Launcher) chọn Server / Client
│       ├── SubmissionModel.java # Đối tượng đại diện cho một bài tập đã nộp
│       ├── sv_sv.java           # Client nộp bài phiên bản dòng lệnh (CLI)
│       │
│       ├── SerVer/
│       │   ├── ServerCore.java  # Lõi mạng TCP Socket Server đa luồng (Multi-threading)
│       │   └── main_sv.java     # Server cơ bản dòng lệnh (CLI)
│       │
│       ├── giaodien/
│       │   ├── ServerGUI.java   # Giao diện đồ họa Máy chủ (Dành cho Giảng viên)
│       │   └── ClientGUI.java   # Giao diện đồ họa Máy khách (Dành cho Sinh viên)
│       │
│       ├── db/
│       │   ├── DatabaseManager.java   # Quản lý kết nối Singleton tới SQLite
│       │   ├── DatabaseMigration.java # Động cơ tự động chạy Migration từ thư mục migrations
│       │   ├── StudentDAO.java        # Thao tác CSDL bảng Sinh viên (students)
│       │   ├── StudentModel.java      # Đối tượng đại diện thông tin Sinh viên
│       │   ├── SubmissionDAO.java     # Thao tác CSDL bảng Bài nộp (submissions)
│       │   └── ConfigDAO.java         # Thao tác CSDL bảng Cấu hình (system_config)
│       │
│       └── util/
│           ├── DeadlineManager.java   # Quản lý thời gian Hạn chót & Khóa cổng nhận bài
│           └── SecurityUtil.java      # Chống Path Traversal, chặn file độc hại, băm SHA-256
│
├── Submitted_Assignments/       # Thư mục tự động tạo chứa bài nộp của sinh viên
│   └── <MSSV>/
│       ├── BaiTap.zip
│       └── BaiTap_v1.zip        # Tự động đánh version nếu sinh viên nộp lại
│
├── build.xml                    # Script tự động biên dịch Ant
├── damh.db                      # File CSDL SQLite (tự động tạo lần đầu khởi động)
└── HUONG_DAN_SU_DUNG.md         # File tài liệu hướng dẫn này
```

---

## 3. CÁC TÍNH NĂNG TRỌNG TÂM CỦA HỆ THỐNG

### 3.1. Phía Máy Chủ (Server - Giảng viên / Quản trị viên)
- **Tự động nhận diện IP LAN:** Tự động quét các card mạng nội bộ (Ethernet / Wi-Fi) để hiển thị chính xác địa chỉ IP LAN (ví dụ: `192.168.1.15`), giúp giảng viên dễ dàng đọc IP cho sinh viên kết nối.
- **Xử lý song song đa luồng:** Mỗi sinh viên kết nối được cấp phát một `Thread` riêng biệt (`ClientHandler`), hỗ trợ nhận file qua `BufferedInputStream`/`BufferedOutputStream` với kích thước buffer 8KB tối ưu.
- **Quản lý hạn chót nộp bài (Deadline & Gate Control):**
  - Bật/tắt hạn chót theo ngày giờ tùy chỉnh.
  - Các nút chọn nhanh: `+15 phút`, `+30 phút`, `+1 giờ`, `23:59 hôm nay`.
  - Hai chế độ quá hạn: **"Cho phép nộp muộn (gắn nhãn đỏ)"** hoặc **"Khóa cổng khi quá hạn (từ chối nhận file)"**.
  - Nút **"Khóa nhận bài ngay"** để đóng cổng khẩn cấp bất kỳ lúc nào.
  - Đồng hồ đếm ngược thời gian thực hiển thị từng giây.
- **Bảo mật & Toàn vẹn dữ liệu nâng cao:**
  - **Chống Path Traversal:** Làm sạch MSSV và tên file, ngăn chặn các ký tự `../`, `..\`, `/`, `\` nhằm ngăn hacker ghi đè file hệ thống máy chủ.
  - **Chặn file độc hại:** Từ chối nhận các file thực thi và kịch bản nguy hiểm: `.exe`, `.bat`, `.cmd`, `.sh`, `.vbs`, `.ps1`, `.msi`, `.scr`, `.reg`.
  - **Giới hạn dung lượng:** Hạn chế file không vượt quá 150 MB (có thể điều chỉnh trong `SecurityUtil`).
  - **Toàn vẹn SHA-256:** Tính toán mã băm SHA-256 trực tiếp trên luồng nhận file và so khớp với mã băm từ Client. Nếu lệch dù chỉ 1 bit, máy chủ lập tức xóa file hỏng và từ chối.
  - **Chống ghi đè (File Versioning):** Khi sinh viên nộp lại file cùng tên, hệ thống tự động lưu thành `<tên_file>_v1.<ext>`, `<tên_file>_v2.<ext>` để giảng viên có thể xem lại toàn bộ lịch sử các lần nộp.
  - **Cảnh báo sao chép bài (Plagiarism / Duplicate Check):** So sánh mã SHA-256 của bài mới với toàn bộ bài đã nộp của các sinh viên khác. Nếu trùng khớp, Server cảnh báo màu đỏ trong nhật ký hoạt động.
- **Giao diện quản lý chuyên nghiệp:**
  - **Tab 1 - Danh sách bài nộp:** Hiển thị STT, Thời gian nộp, MSSV, Họ tên, Lớp, Tên file, Loại file, Kích thước, Mã SHA-256 rút gọn, Trạng thái (Tô xanh khi đúng hạn, tô đỏ khi nộp muộn). Hỗ trợ tìm kiếm theo thời gian thực. **Click đúp vào dòng để mở trực tiếp file bài làm.**
  - **Tab 2 - Quản lý sinh viên:** Thống kê danh sách sinh viên đã đăng ký trong CSDL, số lượng bài đã nộp, thời điểm nộp gần nhất.
  - **Xuất báo cáo CSV:** Xuất toàn bộ danh sách nộp bài ra file Excel (CSV chuẩn UTF-8 có BOM hiển thị tiếng Việt có dấu hoàn hảo).
  - **Mở thư mục lưu bài:** Nút mở nhanh thư mục `Submitted_Assignments` trên Windows Explorer.
  - **Nhật ký hoạt động (Real-time Log):** Ghi nhận chi tiết từng kết nối, ping, nộp bài, cảnh báo bảo mật.

### 3.2. Phía Máy Khách (Client - Sinh viên)
- **Giao diện thân thiện, dễ sử dụng:** Sinh viên chỉ cần nhập IP, Port, thông tin cá nhân và chọn file.
- **Kiểm tra kết nối & Đồng bộ Hạn nộp (Ping):** Khi bấm "Kiểm tra kết nối", Client gửi lệnh `PING` lên Server để xác thực kết nối, đồng thời tự động cập nhật thời gian còn lại của hạn nộp bài.
- **Tính toán SHA-256 tại Client:** Tự động băm file trước khi gửi để đảm bảo đối soát hai đầu truyền nhận.
- **Thanh tiến trình (Progress Bar):** Hiển thị trực quan quá trình băm file và trạng thái nộp bài.
- **Hỗ trợ phiên bản CLI (`sv_sv.java`):** Dành cho các hệ máy không có giao diện đồ họa hoặc người dùng thích thao tác dòng lệnh.

---

## 4. YÊU CẦU MÔI TRƯỜNG CÀI ĐẶT

1. **Hệ điều hành:** Windows 10/11, macOS hoặc Linux (Ubuntu, Debian, Fedora...).
2. **Java Runtime Environment (JRE) / Java Development Kit (JDK):**
   - Phiên bản tối thiểu: **Java SE 8 (JDK 1.8)**.
   - Hoàn toàn tương thích tốt với các phiên bản mới: **JDK 11, JDK 17, JDK 21 LTS**.
3. **Thư viện phụ thuộc (Libraries):**
   - File driver CSDL: `lib/sqlite-jdbc-3.36.0.3.jar` (đã có sẵn trong thư mục dự án).
4. **Công cụ phát triển khuyến nghị (Tùy chọn):**
   - Apache NetBeans IDE (Khuyên dùng vì dự án đã có sẵn cấu hình Ant `nbproject/`).
   - IntelliJ IDEA hoặc Eclipse.
   - Visual Studio Code (kèm Extension *Extension Pack for Java*).

---

## 5. HƯỚNG DẪN BIÊN DỊCH VÀ CHẠY CHƯƠNG TRÌNH

### Cách 1: Sử dụng Launcher đồ họa trung tâm (Khuyên dùng)
Dự án đã tích hợp sẵn lớp điều hướng `damh.DAMH`. Khi chạy file này, một cửa sổ lựa chọn vai trò sẽ xuất hiện để bạn chọn mở **Server** hay **Client**.

- **Chạy trực tiếp từ file JAR đã đóng gói:**
  Mở terminal (PowerShell hoặc Command Prompt) tại thư mục gốc của dự án:
  ```powershell
  java -jar dist/DAMH.jar
  ```
  *(Cửa sổ Launcher sẽ mở ra -> Bấm nút xanh lá để mở Server, bấm nút xanh dương để mở Client).*

- **Khởi chạy thẳng vai trò Server qua tham số dòng lệnh:**
  ```powershell
  java -jar dist/DAMH.jar server
  ```

- **Khởi chạy thẳng vai trò Client qua tham số dòng lệnh:**
  ```powershell
  java -jar dist/DAMH.jar client
  ```

---

### Cách 2: Chạy trực tiếp trong Apache NetBeans IDE
1. Khởi động NetBeans IDE.
2. Chọn menu **File** -> **Open Project...** -> Trỏ đến thư mục `d:\ltMang\DAMH`.
3. Bấm chuột phải vào Project `DAMH`:
   - Chọn **Clean and Build** để NetBeans tự động biên dịch và tạo file `dist/DAMH.jar`.
   - Chọn **Run** để khởi chạy ứng dụng (Mặc định chạy `damh.DAMH`).
4. Để mở riêng từng giao diện:
   - Mở file `src/damh/giaodien/ServerGUI.java` -> Bấm chuột phải chọn **Run File** (hoặc `Shift + F6`).
   - Mở file `src/damh/giaodien/ClientGUI.java` -> Bấm chuột phải chọn **Run File** (hoặc `Shift + F6`).

---

### Cách 3: Biên dịch và chạy bằng Dòng lệnh (Command Line / Terminal)

Nếu không cài NetBeans, bạn có thể tự biên dịch và chạy bằng lệnh `javac` và `java`:

1. **Biên dịch toàn bộ mã nguồn:**
   ```powershell
   javac -encoding UTF-8 -cp "lib/sqlite-jdbc-3.36.0.3.jar" -d build/classes src/damh/*.java src/damh/*/*.java
   ```

2. **Chạy Server GUI (Giảng viên):**
   ```powershell
   java -cp "build/classes;lib/sqlite-jdbc-3.36.0.3.jar" damh.giaodien.ServerGUI
   ```
   *(Trên Linux/macOS, thay dấu chấm phẩy `;` bằng dấu hai chấm `:`)*

3. **Chạy Client GUI (Sinh viên):**
   ```powershell
   java -cp "build/classes;lib/sqlite-jdbc-3.36.0.3.jar" damh.giaodien.ClientGUI
   ```

4. **Chạy Client dòng lệnh (CLI):**
   ```powershell
   java -cp "build/classes;lib/sqlite-jdbc-3.36.0.3.jar" damh.sv_sv
   ```

---

## 6. HƯỚNG DẪN KẾT NỐI TRONG MẠNG LAN THỰC TẾ

Khi mang đồ án đi báo cáo hoặc chạy thực tế giữa 2 máy tính (1 máy Giảng viên làm Server, 1 máy Sinh viên làm Client):

```text
       ┌────────────────────────┐
       │   ROUTER WI-FI / LAN   │
       └───────────┬────────────┘
                   │
         ┌─────────┴─────────┐
         │                   │
         ▼                   ▼
┌──────────────────┐  ┌──────────────────┐
│   MÁY SERVER     │  │   MÁY CLIENT     │
│ (Máy Giảng viên) │  │ (Máy Sinh viên)  │
│ IP: 192.168.1.15 │  │ IP: 192.168.1.20 │
│ Port: 12345      │  │ Kết nối tới:     │
│                  │  │ 192.168.1.15:    │
│                  │  │ 12345            │
└──────────────────┘  └──────────────────┘
```

### Bước 1: Kết nối cùng một mạng
- Đảm bảo cả máy Server và máy Client đều kết nối vào **cùng một mạng Wi-Fi** hoặc cắm chung một Switch mạng LAN.

### Bước 2: Khởi động Server và lấy địa chỉ IP
1. Trên máy Giảng viên, mở chương trình Server (`ServerGUI`).
2. Xem dòng **IP Máy chủ (LAN)** trên giao diện (ví dụ: `192.168.1.15`).
   - *Mẹo kiểm tra bằng lệnh trên Windows:* Mở CMD gõ `ipconfig`, tìm dòng `IPv4 Address` của mạng Wi-Fi hoặc Ethernet.
3. Nhập cổng (Port): Mặc định là `12345`.
4. Bấm nút **"Khởi động Server"**. Trạng thái sẽ chuyển sang màu xanh: `● Đang hoạt động`.

### Bước 3: Cấu hình Tường lửa (Windows Defender Firewall) trên máy Server
> **Lưu ý quan trọng:** Windows Firewall mặc định có thể chặn các kết nối Socket từ máy ngoài vào máy chủ. Nếu máy sinh viên không kết nối được:
1. Mở **Windows Defender Firewall** -> **Advanced Settings** (Cài đặt nâng cao).
2. Chọn **Inbound Rules** (Quy tắc nhận) -> Chọn **New Rule...** (Tạo quy tắc mới).
3. Chọn **Port** -> **TCP** -> Nhập cổng `12345`.
4. Chọn **Allow the connection** (Cho phép kết nối) -> Nhấn Next đến hết và đặt tên là `DAMH_LAN_SERVER`.
*(Hoặc trong lúc ứng dụng Java yêu cầu quyền truy cập mạng, hãy tích chọn cả Private Network và Public Network rồi bấm Allow Access).*

### Bước 4: Máy Sinh viên kết nối và nộp bài
1. Mở giao diện `ClientGUI` trên máy sinh viên.
2. Tại ô **IP Server**, nhập địa chỉ IP của máy giảng viên (ví dụ: `192.168.1.15`).
3. Tại ô **Port**, nhập `12345`.
4. Bấm nút **"Kiểm tra kết nối"**:
   - Nếu thành công: hiển thị dòng chữ xanh `● Kết nối thành công! (Phản hồi: PONG)` cùng thông tin hạn chót nộp bài được tự động đồng bộ từ Server.
5. Điền MSSV, Họ tên, Lớp, bấm **"Chọn file..."** rồi bấm **"Gửi bài tập"**.

---

## 7. KỊCH BẢN KIỂM THỬ & BÁO CÁO DEMO

Dưới đây là các kịch bản thực nghiệm mẫu chuẩn bị sẵn để biểu diễn trực tiếp trước Giảng viên hướng dẫn nhằm đạt điểm tối đa:

### Kịch bản 1: Nộp bài hợp lệ đúng hạn
1. **Server:** Bật hạn nộp bài là `23:59 Hôm nay`, bấm "Áp dụng hạn nộp".
2. **Client:** Nhập MSSV `B21DCCN001`, Họ tên `Nguyễn Văn An`, Lớp `D21CQCN01-N`. Chọn một file `.zip` hoặc `.pdf`.
3. Bấm **"Gửi bài tập"**.
4. **Kết quả:**
   - Client hiển thị thông báo thành công kèm mã SHA-256.
   - Server ghi nhận bài nộp vào danh sách với Trạng thái xanh: `Đúng hạn`.
   - CSDL SQLite lưu thông tin vào bảng `submissions` và cập nhật bảng `students`.
   - File được lưu vào thư mục `Submitted_Assignments/B21DCCN001/<tên_file>`.

### Kịch bản 2: Mở trực tiếp bài tập trên máy Giảng viên
1. Tại bảng danh sách bài nộp của Server, click đúp chuột vào dòng bài nộp của sinh viên `B21DCCN001`.
2. Hệ thống sẽ lập tức gọi ứng dụng mặc định của hệ điều hành (Notepad, Word, PDF Viewer, WinRAR...) để mở trực tiếp file bài làm mà không cần phải tìm thủ công trong ổ cứng.

### Kịch bản 3: Nộp lại bài (Cơ chế File Versioning - Chống ghi đè)
1. Giữ nguyên thông tin sinh viên `B21DCCN001`, nộp lại đúng file đó một lần nữa.
2. **Kết quả:** Server không ghi đè làm mất file cũ, mà tự động sinh ra file `BaiTap_v1.zip`. Cả hai bản nộp đều được ghi lại trong CSDL.

### Kịch bản 4: Cảnh báo gian lận / Sao chép bài (Trùng mã băm SHA-256)
1. Một sinh viên khác có MSSV `B21DCCN002` chọn đúng file bài làm của bạn `B21DCCN001` (hoặc đổi tên file nhưng giữ nguyên nội dung bên trong).
2. Tiến hành nộp bài lên Server.
3. **Kết quả:** Server đối soát mã băm SHA-256 và lập tức in dòng cảnh báo màu đỏ lên màn hình Log:
   `[CẢNH BÁO TRÙNG LẶP] Phát hiện bài của SV [B21DCCN002] có nội dung GIỐNG HỆT bài của SV [B21DCCN001]!`

### Kịch bản 5: Kiểm tra cơ chế An toàn & Bảo mật
- **Thử nghiệm chặn file nguy hiểm:** Chọn nộp file `run.bat` hoặc `setup.exe`.
  -> Hệ thống từ chối ngay lập tức: *"Hệ thống từ chối nhận định dạng file thực thi/script"*.
- **Thử nghiệm chống Path Traversal:** Cố tình nhập MSSV là `../../System32` hoặc `../hack`.
  -> Hệ thống báo lỗi: *"Mã số sinh viên chứa ký tự không hợp lệ hoặc có dấu hiệu điều hướng thư mục"*.
- **Thử nghiệm đối soát toàn vẹn:** Giả lập đường truyền bị ngắt giữa chừng.
  -> Server phát hiện số byte nhận thiếu hoặc sai lệch SHA-256, tự động xóa file rác và yêu cầu client gửi lại.

### Kịch bản 6: Kiểm tra chính sách Hạn chót & Ca thi
- **Chính sách 1 (Cho phép nộp muộn):** Chỉnh hạn nộp về thời gian trong quá khứ -> Nộp bài -> Server vẫn nhận nhưng gắn trạng thái màu đỏ: `Nộp muộn (Trễ X phút)`.
- **Chính sách 2 (Khóa cổng khi quá hạn):** Chọn chính sách *"Khóa cổng khi quá hạn"* -> Nộp bài -> Server lập tức từ chối nhận bài và thông báo: *"Đã quá hạn chót nộp bài! Máy chủ đang chặn nộp muộn"*.
- **Khóa nhận bài khẩn cấp:** Giảng viên bấm nút **"Khóa nhận bài ngay"** -> Mọi kết nối nộp bài tiếp theo đều bị chặn.

### Kịch bản 7: Xuất dữ liệu ra Excel (CSV)
1. Bấm nút **"Xuất danh sách (CSV)"** trên Server.
2. Chọn nơi lưu file.
3. Mở file CSV bằng Microsoft Excel. Danh sách hiển thị đầy đủ, không bị lỗi font tiếng Việt nhờ cơ chế chèn UTF-8 BOM (`\ufeff`).

---

## 8. CƠ SỞ DỮ LIỆU & CƠ CHẾ DATABASE MIGRATION

Đồ án sử dụng cơ sở dữ liệu nhúng **SQLite** lưu trong file `damh.db`. Điểm đặc biệt của đồ án là tích hợp cơ chế **Migration tự động** tương tự các framework hiện đại (như Flyway, Laravel Migrations, Django Migrations):

### Cách thức hoạt động của Migration Engine:
Khi Server khởi động, lớp `DatabaseMigration.java` sẽ:
1. Tạo bảng `schema_version` nếu chưa có.
2. Quét toàn bộ các file `.sql` trong thư mục `migrations/` theo thứ tự phiên bản (`V1`, `V2`, `V3`, `V4`...).
3. Kiểm tra xem phiên bản nào chưa được áp dụng trên file `damh.db` hiện tại.
4. Tự động thực thi phiên bản mới trong một Database Transaction duy nhất (`commit`/`rollback`).
5. Nhờ đó, cơ sở dữ liệu luôn tự động nâng cấp mà không bao giờ làm mất dữ liệu cũ của người dùng.

### Chi tiết các bảng trong CSDL:
```sql
-- 1. Bảng lưu trữ phiên bản migration
CREATE TABLE schema_version (
    version INTEGER PRIMARY KEY,
    description TEXT NOT NULL,
    applied_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
);

-- 2. Bảng quản lý sinh viên
CREATE TABLE students (
    student_id TEXT PRIMARY KEY,       -- MSSV (Khóa chính)
    full_name TEXT NOT NULL,          -- Họ và tên
    class_name TEXT,                  -- Lớp học
    created_at TEXT,
    updated_at TEXT
);

-- 3. Bảng lưu lịch sử nộp bài tập
CREATE TABLE submissions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id TEXT NOT NULL,         -- Mã sinh viên (Khóa ngoại tham chiếu students)
    file_name TEXT NOT NULL,          -- Tên file đã lưu
    file_type TEXT,                   -- Phần mở rộng (.zip, .pdf, .docx...)
    file_size INTEGER NOT NULL,       -- Kích thước file theo bytes
    submission_time TEXT NOT NULL,    -- Thời điểm nộp
    status TEXT NOT NULL,             -- Trạng thái: Đúng hạn / Nộp muộn
    saved_path TEXT NOT NULL,         -- Đường dẫn tuyệt đối của file trên đĩa
    file_hash TEXT,                   -- Mã băm toàn vẹn SHA-256 (64 ký tự hex)
    note TEXT,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
);

-- 4. Bảng lưu cấu hình hệ thống
CREATE TABLE system_config (
    config_key TEXT PRIMARY KEY,      -- Khóa cấu hình (VD: deadline_time, block_late)
    config_value TEXT NOT NULL,       -- Giá trị cấu hình
    updated_at TEXT
);
```

---

## 9. BỘ CÂU HỎI VẤN ĐÁP THƯỜNG GẶP KHI BẢO VỆ ĐỒ ÁN

### Câu 1: Tại sao đồ án lại chọn giao thức TCP thay vì UDP?
**Trả lời:**
- Ứng dụng truyền file bài nộp đòi hỏi **độ chính xác và toàn vẹn tuyệt đối 100%**. Mỗi byte mã nguồn hay dữ liệu bài làm đều không được phép sai lệch hay thất lạc.
- Giao thức **TCP (Transmission Control Protocol)** là giao thức hướng kết nối (Connection-oriented), có cơ chế bắt tay 3 bước (3-way handshake), kiểm soát luồng (Flow control), kiểm soát nghẽn (Congestion control), tự động gửi lại gói tin bị mất và đảm bảo thứ tự các gói tin gửi đến đích.
- Ngược lại, **UDP** là giao thức phi kết nối (Connectionless), không đảm bảo độ tin cậy, nếu dùng UDP để gửi file thì file bài nộp rất dễ bị hỏng hoặc mất đoạn dữ liệu.

---

### Câu 2: Làm thế nào Server có thể phục vụ nhiều sinh viên nộp bài cùng lúc mà không bị đơ giao diện?
**Trả lời:**
- Đồ án áp dụng mô hình **Đa luồng (Multi-threading)**:
  - Luồng giao diện (Event Dispatch Thread - EDT): Chịu trách nhiệm hiển thị UI, nhận thao tác chuột/phím của người dùng.
  - Luồng lắng nghe Server (`ServerThread`): Chạy vòng lặp `while (isRunning) { Socket socket = serverSocket.accept(); }`.
  - Luồng xử lý từng Client (`ClientHandler`): Khi có một sinh viên kết nối vào cổng `12345`, `serverSocket.accept()` sẽ trả về một `Socket` mới. Server ngay lập tức tạo một `Thread(new ClientHandler(socket)).start()` độc lập để xử lý riêng việc nhận file của sinh viên đó.
- Nhờ vậy, Server có thể phục vụ song song nhiều kết nối mà không làm tắc nghẽn giao diện đồ họa.

---

### Câu 3: Mục đích của việc sử dụng thuật toán SHA-256 trong bài toán này là gì?
**Trả lời:**
Có 2 mục đích cốt lõi:
1. **Kiểm tra tính toàn vẹn (Integrity Check):** Trong quá trình truyền qua mạng LAN, có thể xảy ra nhiễu hoặc gián đoạn. Bằng cách tính mã băm SHA-256 tại Client và tính lại tại Server trên luồng byte nhận được, nếu hai mã băm trùng khớp 100%, ta chứng minh được file không hề bị thay đổi hay suy hao trên đường truyền.
2. **Phát hiện sao chép bài (Plagiarism Detection):** Thuật toán băm một chiều SHA-256 có tính chất chống va chạm cực cao. Nếu hai sinh viên nộp file có cùng mã băm SHA-256, điều đó khẳng định chắc chắn 100% nội dung hai bài tập đó giống hệt nhau từng byte, giúp giảng viên phát hiện trường hợp copy bài của nhau.

---

### Câu 4: Tấn công Path Traversal là gì và đồ án phòng chống như thế nào?
**Trả lời:**
- **Path Traversal (Điều hướng thư mục):** Là kỹ thuật tấn công khi kẻ xấu gửi lên các tên file hoặc MSSV có chứa ký tự đặc biệt như `../../../../Windows/System32` hoặc `..\\..\\boot.ini` nhằm lừa máy chủ ghi đè hoặc can thiệp vào các tệp tin quan trọng của hệ điều hành bên ngoài thư mục cho phép.
- **Giải pháp trong đồ án (`SecurityUtil.java`):**
  - Hàm `sanitizeStudentId`: Kiểm tra MSSV bằng biểu thức chính quy (Regex) `^[a-zA-Z0-9_\-]+$`, loại bỏ hoàn toàn các ký tự `.` và `/` hoặc `\`.
  - Hàm `sanitizeFileName`: Dùng `new File(rawFileName).getName()` kết hợp thay thế toàn bộ ký tự cấm của hệ điều hành thành ký tự an toàn.
  - Sử dụng `getCanonicalPath().startsWith(baseDir.getCanonicalPath())` để đảm bảo file lưu trữ luôn nằm chặt chẽ bên trong thư mục `Submitted_Assignments`.

---

### Câu 5: Ưu điểm của việc dùng SQLite so với MySQL trong đồ án này là gì?
**Trả lời:**
- **Tính di động (Portability):** SQLite là cơ sở dữ liệu serverless, toàn bộ CSDL nằm gọn trong một file đơn lẻ `damh.db`.
- **Zero Configuration:** Giảng viên và người chấm chỉ cần bấm chạy file JAR là CSDL tự động khởi tạo, không cần phải cài đặt dịch vụ MySQL Server, không cần cấu hình user, password hay lo ngại xung đột cổng `3306`.
- Đồ án vẫn chuẩn bị sẵn file `migrations/mysql_schema.sql` để nếu giảng viên yêu cầu mở rộng sang MySQL quy mô lớn thì cấu trúc bảng đã hoàn toàn tương thích.

---

*(Tài liệu được xây dựng phục vụ báo cáo Đồ án môn học Lập trình Mạng)*
