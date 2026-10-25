from docx import Document
from docx.shared import Pt
from docx.enum.text import WD_PARAGRAPH_ALIGNMENT

# Create document

doc = Document()
section = doc.sections[0]
section.top_margin = Inches(1)
section.bottom_margin = Inches(1)
section.left_margin = Inches(1)
section.right_margin = Inches(1)

# Title
p = doc.add_paragraph()
p.alignment = WD_PARAGRAPH_ALIGNMENT.CENTER
run = p.add_run('BÁO CÁO ĐỒ ÁN: HỆ THỐNG NỘP BÀI TẬP TRỰC TUYẾN QUA MẠNG LAN')
run.bold = True
run.font.size = Pt(20)

# Chapter 3
p = doc.add_paragraph()
p.add_run('CHƯƠNG 3. KẾT QUẢ THỰC NGHIỆM')
p.runs[0].bold = True
p.runs[0].font.size = Pt(14)

chapter_3 = [
    ('3.1. Mục đích thực nghiệm', 'Mục tiêu của chương này là kiểm tra tính đúng đắn của hệ thống đã xây dựng, bao gồm khả năng kết nối giữa Client và Server trong mạng LAN, độ ổn định khi nhiều sinh viên cùng nộp bài, hiệu quả của các chức năng bảo mật, cũng như mức độ chính xác trong lưu trữ, quản lý hạn nộp và chấm điểm.'),
    ('3.2. Môi trường thực nghiệm', 'Nhóm đã triển khai hệ thống trên một môi trường mạng LAN đơn giản với một máy chủ và một hoặc nhiều máy khách. Hệ điều hành sử dụng là Windows 10/11, ứng dụng Java được chạy bằng JDK tương thích, và cơ sở dữ liệu SQLite được dùng để lưu thông tin sinh viên, bài nộp và điểm số.'),
    ('', '[CHÈN ẢNH 1: Giao diện Server – màn hình quản lý bài nộp]'),
    ('', '[CHÈN ẢNH 2: Giao diện Client – màn hình sinh viên gửi bài]'),
    ('3.3. Kịch bản kiểm thử', 'Nhóm đã kiểm tra các tình huống nộp bài đúng hạn, nộp muộn, nhiều Client đồng thời, kiểm tra bảo mật file và mô hình chấm điểm. Kết quả cho thấy hệ thống hoạt động ổn định và đáp ứng hầu hết các chức năng cốt lõi đã đặt ra.'),
    ('', '[CHÈN ẢNH 3: Màn hình xác nhận nộp bài thành công]'),
    ('', '[CHÈN ẢNH 4: Bảng danh sách bài nộp nhiều sinh viên]'),
    ('', '[CHÈN ẢNH 5: Cảnh báo file độc hại hoặc mã băm trùng]'),
    ('', '[CHÈN ẢNH 6: Màn hình chấm điểm và nhận xét]'),
    ('3.4. Kết quả thực nghiệm', 'Kết quả kiểm thử cho thấy các chức năng chính như kết nối, nộp bài, quản lý hạn nộp, chấm điểm và xem điểm đều hoạt động đúng. Hệ thống có thể áp dụng cho môi trường học tập và phòng máy thực tế.'),
    ('3.5. Đánh giá chung về hiệu quả', 'Hệ thống có nhiều ưu điểm như dễ triển khai, ít phụ thuộc môi trường, bảo mật tốt, và hỗ trợ giảng viên quản lý bài nộp thuận tiện. Tuy nhiên, dự án vẫn còn hạn chế ở chỗ chỉ phù hợp với mạng LAN và cần thêm các tính năng nâng cao để phát triển hơn nữa.'),
]

for title, text in chapter_3:
    if title:
        p = doc.add_paragraph()
        p.add_run(title)
        p.runs[0].bold = True
        p.runs[0].font.size = Pt(13)
    p = doc.add_paragraph()
    p.add_run(text)
    p.runs[0].font.size = Pt(12)

# Chapter 4
p = doc.add_page_break()
p = doc.add_paragraph()
p.add_run('CHƯƠNG 4. KẾT LUẬN VÀ KIẾN NGHỊ')
p.runs[0].bold = True
p.runs[0].font.size = Pt(14)

chapter_4 = [
    ('4.1. Kết luận', 'Sau khi kiểm thử, hệ thống đã đạt được mục tiêu ban đầu của đề tài. Dự án không chỉ giúp sinh viên nộp bài trực tiếp mà còn giúp giảng viên quản lý, chấm điểm và thống kê tốt hơn.'),
    ('4.2. Những ưu điểm đạt được', 'Dự án có kiến trúc Client – Server rõ ràng, xử lý nhiều Client cùng lúc, bảo mật tốt nhờ SHA-256, và giao diện dễ sử dụng cho cả sinh viên lẫn giảng viên.'),
    ('4.3. Hạn chế của hệ thống', 'Hệ thống mới chỉ hoạt động hiệu quả trong mạng LAN, chưa có phân quyền người dùng chặt chẽ và cần thêm các tính năng nâng cấp cho quy mô lớn hơn.'),
    ('4.4. Kiến nghị và hướng phát triển', 'Nên bổ sung mã hóa dữ liệu, phân quyền người dùng, tăng cường bảo mật và mở rộng khả năng chạy trên môi trường web hoặc mô hình lớn hơn.'),
    ('4.5. Kết luận cuối cùng', 'Dự án này là một nền tảng phù hợp để phát triển tiếp trong tương lai, đặc biệt trong môi trường giáo dục và phòng máy tính.'),
]

for title, text in chapter_4:
    p = doc.add_paragraph()
    p.add_run(title)
    p.runs[0].bold = True
    p.runs[0].font.size = Pt(13)
    p = doc.add_paragraph()
    p.add_run(text)
    p.runs[0].font.size = Pt(12)

# Notes for adding product images
p = doc.add_paragraph()
p.add_run('GHI CHÚ THÊM ẢNH SẢN PHẨM:')
p.runs[0].bold = True
p.runs[0].font.size = Pt(13)

notes = [
    '- Ảnh 1: chèn ở phần 3.2 để minh họa giao diện Server.',
    '- Ảnh 2: chèn ở phần 3.2 để minh họa giao diện Client.',
    '- Ảnh 3: chèn ở phần 3.3.2 để minh họa quá trình nộp bài thành công.',
    '- Ảnh 4: chèn ở phần 3.3.4 để minh họa nhiều sinh viên nộp bài cùng lúc.',
    '- Ảnh 5: chèn ở phần 3.3.5 để minh họa cảnh báo file độc hại / mã băm trùng.',
    '- Ảnh 6: chèn ở phần 3.3.6 để minh họa màn hình chấm điểm.',
    '- Nên dùng cùng kích thước, thêm chú thích ngắn và đặt ảnh ở giữa trang để báo cáo trông gọn và chuyên nghiệp hơn.'
]
for item in notes:
    p = doc.add_paragraph(item, style='List Bullet')
    p.runs[0].font.size = Pt(11)

filename = 'BaoCao_DAMH_Chuong3_4.docx'
doc.save(filename)
print(f'Created: {filename}')
