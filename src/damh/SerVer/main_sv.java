package damh.SerVer;

import java.io.*;
import java.net.*;

public class main_sv {
    private static final int PORT = 12345;
    private static final String STORAGE_DIR = "Submitted_Assignments";

    public static void main(String[] args) {
        File dir = new File(STORAGE_DIR);
        if (!dir.exists()) dir.mkdir();

        System.out.println("Server đang khởi động trên cổng " + PORT + "...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Đã kết nối với sinh viên từ: " + socket.getInetAddress());
                // Xử lý mỗi client bằng một luồng riêng biệt
                new ClientHandler(socket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream())
            ) {
                String firstToken = dis.readUTF();
                String studentID;
                String fileName;
                long fileSize;

                if ("SUBMIT".equalsIgnoreCase(firstToken)) {
                    studentID = dis.readUTF().trim().toUpperCase();
                    String studentName = dis.readUTF().trim().toUpperCase();
                    String studentClass = dis.readUTF().trim().toUpperCase();
                    fileName = dis.readUTF();
                    fileSize = dis.readLong();
                    String clientSha = dis.readUTF();
                } else {
                    studentID = firstToken.trim().toUpperCase();
                    fileName = dis.readUTF();
                    fileSize = dis.readLong();
                }

                System.out.println("Nhận bài từ [MSSV: " + studentID + "] - File: " + fileName + " (" + fileSize + " bytes)");

                // Tạo thư mục riêng cho sinh viên nếu chưa có
                File studentDir = new File(STORAGE_DIR + "/" + studentID);
                if (!studentDir.exists()) studentDir.mkdir();

                // 2. Nhận nội dung file qua FileOutputStream
                File outputFile = new File(studentDir, fileName);
                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    long totalRead = 0;

                    while (totalRead < fileSize && (bytesRead = dis.read(buffer, 0, (int)Math.min(buffer.length, fileSize - totalRead))) != -1) {
                        fos.write(buffer, 0, bytesRead);
                        totalRead += bytesRead;
                    }
                    fos.flush();
                }

                System.out.println("Đã lưu thành công bài của: " + studentID);
                // 3. Phản hồi về client (tương thích boolean + UTF)
                dos.writeBoolean(true);
                dos.writeUTF("Nộp bài thành công!");
                dos.flush();

            } catch (IOException e) {
                System.err.println("Lỗi xử lý client: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
