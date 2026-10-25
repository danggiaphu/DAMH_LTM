package damh.util;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Tiện ích bảo mật, băm dữ liệu SHA-256 và chống tấn công Path Traversal
 */
public class SecurityUtil {

    // Danh sách các định dạng file thực thi nguy hiểm bị cấm nộp
    private static final Set<String> BLOCKED_EXTENSIONS = new HashSet<>(Arrays.asList(
            "exe", "bat", "cmd", "sh", "vbs", "ps1", "com", "msi", "scr", "pif", "reg"
    ));

    // Kích thước file tối đa cho phép: 150 MB
    public static final long MAX_FILE_SIZE = 150L * 1024 * 1024;

    /**
     * Tính mã băm SHA-256 của một File trên đĩa
     */
    public static String computeSHA256(File file) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    md.update(buffer, 0, bytesRead);
                }
            }
            return bytesToHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Thuật toán SHA-256 không khả dụng trên JVM!", e);
        }
    }

    /**
     * Chuyển mảng byte sang chuỗi Hexadecimal chữ thường
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Làm sạch Mã số sinh viên (MSSV), loại bỏ các ký tự đặc biệt và ngăn chặn Path Traversal
     */
    public static String sanitizeStudentId(String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã số sinh viên không được để trống!");
        }
        String trimmed = studentId.trim();
        // Ngăn chặn tuyệt đối ký tự điều hướng thư mục
        if (trimmed.contains("/") || trimmed.contains("\\") || trimmed.contains("..")) {
            throw new IllegalArgumentException("Mã số sinh viên chứa ký tự không hợp lệ hoặc có dấu hiệu điều hướng thư mục!");
        }
        // Chỉ cho phép chữ cái, chữ số, dấu gạch nối và gạch dưới (độ dài từ 1 đến 30)
        if (!trimmed.matches("^[a-zA-Z0-9_\\-]+$") || trimmed.length() > 30) {
            throw new IllegalArgumentException("Mã số sinh viên chỉ gồm chữ cái, chữ số và gạch nối (1-30 ký tự)!");
        }
        return trimmed;
    }

    /**
     * Làm sạch tên file, loại bỏ toàn bộ thông tin đường dẫn thư mục và ký tự đặc biệt nguy hiểm
     */
    public static String sanitizeFileName(String rawFileName) {
        if (rawFileName == null || rawFileName.trim().isEmpty()) {
            return "bai_nop_" + System.currentTimeMillis() + ".dat";
        }

        // 1. Chỉ lấy tên file gốc, loại bỏ toàn bộ tiền tố thư mục (dù là Windows hay Unix)
        String fileName = new File(rawFileName).getName();
        // Xử lý thêm trường hợp dấu gạch chéo ngược trên Linux hoặc gạch chéo xuôi trên Windows
        int lastSlash = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
        if (lastSlash >= 0 && lastSlash < fileName.length() - 1) {
            fileName = fileName.substring(lastSlash + 1);
        }

        // 2. Thay thế các ký tự không hợp lệ trong hệ điều hành bằng dấu gạch dưới
        fileName = fileName.replaceAll("[\\\\/:*?\"<>|\\x00-\\x1F]", "_").trim();

        // 3. Tránh trường hợp tên file chỉ là dấu chấm hoặc rỗng
        if (fileName.isEmpty() || fileName.equals(".") || fileName.equals("..")) {
            return "bai_nop_" + System.currentTimeMillis() + ".dat";
        }

        return fileName;
    }

    /**
     * Kiểm tra xem đuôi file có thuộc danh sách bị cấm do nguy cơ bảo mật hay không
     */
    public static boolean isBlockedExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return false;
        }
        int dot = fileName.lastIndexOf('.');
        if (dot >= 0 && dot < fileName.length() - 1) {
            String ext = fileName.substring(dot + 1).toLowerCase();
            return BLOCKED_EXTENSIONS.contains(ext);
        }
        return false;
    }

    /**
     * Tạo thư mục an toàn cho sinh viên bên trong thư mục lưu trữ gốc,
     * xác thực canonical path để chống hoàn toàn Path Traversal.
     */
    public static File getSafeStudentDir(File storageDir, String studentId) throws IOException {
        String safeId = sanitizeStudentId(studentId);
        File rootDir = storageDir.getCanonicalFile();
        File studentDir = new File(rootDir, safeId).getCanonicalFile();

        // Kiểm tra xem thư mục sinh viên có thực sự nằm bên trong thư mục gốc không
        if (!studentDir.getPath().startsWith(rootDir.getPath())) {
            throw new SecurityException("Phát hiện nguy cơ Path Traversal từ MSSV: " + studentId);
        }

        if (!studentDir.exists()) {
            studentDir.mkdirs();
        }
        return studentDir;
    }

    /**
     * Tạo file đích an toàn và không ghi đè mất bài cũ (File Versioning).
     * Nếu file đã tồn tại, tự động đổi tên thêm hậu tố (1), (2)...
     */
    public static File getUniqueTargetFile(File studentDir, String sanitizedFileName) throws IOException {
        File dir = studentDir.getCanonicalFile();
        File targetFile = new File(dir, sanitizedFileName).getCanonicalFile();

        // Xác thực không thoát khỏi thư mục sinh viên
        if (!targetFile.getPath().startsWith(dir.getPath())) {
            throw new SecurityException("Phát hiện nguy cơ Path Traversal từ tên file: " + sanitizedFileName);
        }

        // Nếu file chưa tồn tại thì dùng luôn
        if (!targetFile.exists()) {
            return targetFile;
        }

        // Tách tên và phần mở rộng để tạo file versioning không ghi đè
        String baseName = sanitizedFileName;
        String extension = "";
        int dot = sanitizedFileName.lastIndexOf('.');
        if (dot > 0) {
            baseName = sanitizedFileName.substring(0, dot);
            extension = sanitizedFileName.substring(dot);
        }

        int version = 1;
        while (targetFile.exists()) {
            String newName = baseName + " (" + version + ")" + extension;
            targetFile = new File(dir, newName).getCanonicalFile();
            if (!targetFile.getPath().startsWith(dir.getPath())) {
                throw new SecurityException("Phát hiện nguy cơ Path Traversal khi đánh phiên bản file: " + newName);
            }
            version++;
        }

        return targetFile;
    }
}
