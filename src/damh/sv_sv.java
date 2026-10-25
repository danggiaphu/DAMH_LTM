
package damh;

import damh.util.SecurityUtil;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class sv_sv {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Nhập IP Server trong LAN (VD: 127.0.0.1): ");
        String serverIP = scanner.nextLine().trim();
        if (serverIP.isEmpty()) serverIP = "127.0.0.1";
        int port = 12345;

        System.out.print("Nhập Mã số sinh viên (MSSV): ");
        String studentID = scanner.nextLine().trim().toUpperCase();

        System.out.print("Nhập Họ và tên sinh viên: ");
        String studentName = scanner.nextLine().trim().toUpperCase();
        if (studentName.isEmpty()) studentName = "SINH VIEN (CLI)";

        System.out.print("Nhập Lớp: ");
        String studentClass = scanner.nextLine().trim().toUpperCase();
        if (studentClass.isEmpty()) studentClass = "N/A";

        System.out.print("Nhập đường dẫn tuyệt đối đến file bài tập: ");
        String filePath = scanner.nextLine().trim();

        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            System.out.println("Lỗi: File không tồn tại!");
            return;
        }

        if (SecurityUtil.isBlockedExtension(file.getName())) {
            System.out.println("Lỗi bảo mật: Định dạng file bị cấm nộp (" + file.getName() + ")!");
            return;
        }

        try {
            System.out.println("Đang tính toán mã băm SHA-256...");
            String sha256 = SecurityUtil.computeSHA256(file);
            System.out.println("Mã băm SHA-256: " + sha256);

            try (Socket socket = new Socket(serverIP, port);
                 DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                 DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                 FileInputStream fis = new FileInputStream(file)) {

                socket.setSoTimeout(30000);

                // 1. Gửi lệnh và thông tin bài nộp
                dos.writeUTF("SUBMIT");
                dos.writeUTF(studentID);
                dos.writeUTF(studentName);
                dos.writeUTF(studentClass);
                dos.writeUTF(file.getName());
                dos.writeLong(file.length());
                dos.writeUTF(sha256);
                dos.flush();

                // 2. Gửi nội dung file qua mạng bằng buffer
                System.out.println("Đang truyền file lên server...");
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    dos.write(buffer, 0, bytesRead);
                }
                dos.flush();

                // 3. Nhận phản hồi từ Server
                boolean success = dis.readBoolean();
                String response = dis.readUTF();
                if (success) {
                    System.out.println("[THÀNH CÔNG] " + response);
                } else {
                    System.out.println("[THẤT BẠI] " + response);
                }
            }
        } catch (Exception e) {
            System.out.println("Lỗi: " + e.getMessage());
        }
    }
}
