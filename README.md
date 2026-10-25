# HỆ THỐNG NỘP BÀI TẬP TRỰC TUYẾN QUA MẠNG LAN
> **Đồ án Môn học:** Lập trình Mạng (Network Programming)  
> **Kiến trúc:** Client - Server (TCP Socket + Multi-threading + I/O Streams)  
> **Cơ sở dữ liệu:** SQLite Database nhúng tự động với Database Migrations  
> **Giao diện:** Java Swing GUI & CLI (Launcher trung tâm: `damh.DAMH`)

---

## 📖 TÀI LIỆU HƯỚNG DẪN CHI TIẾT
Tài liệu hướng dẫn đầy đủ từ A-Z, kịch bản báo cáo giảng viên, hướng dẫn cấu hình mạng LAN thực tế và bộ câu hỏi vấn đáp:
👉 **[Xem toàn bộ Hướng dẫn sử dụng tại đây: HUONG_DAN_SU_DUNG.md](HUONG_DAN_SU_DUNG.md)**

---

## 🚀 KHỞI CHẠY NHANH

### Cách 1: Chạy Launcher trung tâm (Khuyên dùng)
```powershell
java -jar dist/DAMH.jar
```
*(Cửa sổ sẽ hiện lên cho phép bạn chọn khởi chạy **Server Giảng viên** hoặc **Client Sinh viên** chỉ với 1 cú click).*

### Cách 2: Khởi chạy trực tiếp từng thành phần
- **Chạy Máy chủ Server (Giảng viên):**
  ```powershell
  java -jar dist/DAMH.jar server
  ```
- **Chạy Máy khách Client (Sinh viên):**
  ```powershell
  java -jar dist/DAMH.jar client
  ```
- **Chạy Client giao diện dòng lệnh (CLI):**
  ```powershell
  java -cp "build/classes;lib/sqlite-jdbc-3.36.0.3.jar" damh.sv_sv
  ```

---

## 🌟 CÁC TÍNH NĂNG NỔI BẬT

1. **Truyền nhận File qua TCP Socket đa luồng:** Nhận file tốc độ cao, hỗ trợ nhiều sinh viên nộp bài song song mà không bị nghẽn.
2. **Cơ sở dữ liệu SQLite tự động (Database Migrations):** Tự động sinh file `damh.db` và khởi tạo bảng từ `migrations/`, không cần cài thêm SQL server.
3. **Quản lý Hạn nộp bài & Ca thi (Deadline & Gate Control):**
   - Đặt hạn chót với nút chọn nhanh (`+15p`, `+30p`, `+1h`, `23:59 hôm nay`).
   - Tùy chọn chính sách: Chấp nhận nộp muộn (gắn nhãn đỏ) hoặc Khóa cổng từ chối nhận bài.
   - Nút khóa nhận bài khẩn cấp thủ công.
   - Đồng hồ đếm ngược thời gian thực.
4. **Bảo mật & Toàn vẹn dữ liệu:**
   - Đối soát mã băm **SHA-256** theo thời gian thực giữa Client và Server.
   - Chống tấn công **Path Traversal** (làm sạch MSSV và tên file).
   - Chặn các định dạng file thực thi nguy hiểm (`.exe`, `.bat`, `.cmd`, `.sh`, `.vbs`, `.ps1`...).
   - **Tự động đánh version file** (`_v1`, `_v2`...) khi nộp lại, chống ghi đè mất bài cũ.
   - **Cảnh báo sao chép bài:** Phát hiện 2 sinh viên nộp file có cùng mã băm SHA-256.
5. **Giao diện Giảng viên hiện đại Google Meet Dark Mode:**
   - Thiết kế đồng bộ phong cách **Google Meet Dark Theme**, bảng biểu mượt mà, trực quan.
   - Thẻ thống kê thời gian thực: Tổng bài nhận, Số bài đã chấm, Chưa chấm, Điểm TB lớp.
   - **Click đúp vào dòng bài nộp để mở cửa sổ chấm điểm và xem trực tiếp file bài làm.**
   - Chuột phải hỗ trợ Menu ngữ cảnh: Chấm điểm, Mở file, Sao chép mã SHA-256.
   - Bộ lọc tiện lợi: Tất cả | Chưa chấm (cần chấm) | Đã chấm | Nộp muộn | Đúng hạn.
6. **Chức năng Chấm điểm, Đánh giá bài tập & Thống kê phổ điểm:**
   - Chấm điểm theo thang điểm 10 kèm các nút chọn điểm nhanh (`5.0`, `7.0`, `8.0`, `8.5`, `9.0`, `9.5`, `10.0`).
   - Đánh giá & nhận xét bài làm chi tiết kèm mẫu nhận xét nhanh (Quick Feedback Templates).
   - Nút **"Lưu & Chấm bài kế tiếp"** giúp giảng viên chấm liên tục nhanh chóng mà không cần đóng mở popup.
   - **Thống kê phổ điểm lớp:** Tỷ lệ Giỏi/Xuất sắc (>= 8.5), Khá (7.0 - 8.4), Trung bình (5.0 - 6.9), Cần cố gắng (< 5.0) kèm thanh tiến trình trực quan.
   - **Xuất bảng điểm chi tiết ra file Excel (CSV UTF-8 có dấu)** bao gồm: Điểm số, Nhận xét, Thời gian chấm, Thời gian nộp, Trạng thái, Mã SHA-256.
   - **Sinh viên tra cứu điểm trực tuyến:** Nút "Xem Điểm" trên Client cho phép tra cứu điểm và nhận xét của giảng viên tức thì qua TCP Socket.

