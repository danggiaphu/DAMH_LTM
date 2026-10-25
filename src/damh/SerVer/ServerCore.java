package damh.SerVer;

import damh.SubmissionModel;
import damh.db.DatabaseManager;
import damh.db.DatabaseMigration;
import damh.db.StudentDAO;
import damh.db.SubmissionDAO;
import damh.util.DeadlineManager;
import damh.util.SecurityUtil;

import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class ServerCore {
    public static final String STORAGE_DIR = "Submitted_Assignments";
    
    public interface ServerListener {
        void onServerStarted(String ip, int port);
        void onServerStopped();
        void onLog(String message);
        void onSubmissionReceived(SubmissionModel submission);
    }

    private int port;
    private ServerSocket serverSocket;
    private boolean isRunning = false;
    private Thread serverThread;
    private ServerListener listener;
    private final List<Socket> activeClientSockets = new ArrayList<>();

    public ServerCore(int port, ServerListener listener) {
        this.port = port;
        this.listener = listener;
    }

    public synchronized void startServer() {
        if (isRunning) return;

        File dir = new File(STORAGE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Khởi tạo Database và chạy Migration tự động
        try {
            DatabaseManager.initialize(new DatabaseMigration.MigrationLogger() {
                @Override
                public void log(String message) {
                    if (listener != null) {
                        listener.onLog(message);
                    }
                }
            });
            // Nạp cấu hình hạn nộp bài từ CSDL
            DeadlineManager.getInstance().loadFromDatabase();

            if (listener != null) {
                listener.onLog("[DATABASE] Khởi tạo CSDL SQLite thành công (" + DatabaseManager.DB_FILE_NAME + ")");
                DeadlineManager dm = DeadlineManager.getInstance();
                if (dm.isEnabled()) {
                    listener.onLog("[HẠN NỘP] Đang kích hoạt hạn chót: " + dm.getFormattedDeadline() + " (" + dm.getRemainingTimeFormatted() + ")");
                }
            }
        } catch (Exception ex) {
            if (listener != null) {
                listener.onLog("[DATABASE LỖI] " + ex.getMessage());
            }
        }

        serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(port);
                    isRunning = true;
                    String localIp = getLocalIPAddress();
                    if (listener != null) {
                        listener.onServerStarted(localIp, port);
                        listener.onLog("Server đã khởi động thành công trên IP: " + localIp + ", Cổng: " + port);
                        listener.onLog("Thư mục lưu trữ: " + new File(STORAGE_DIR).getAbsolutePath());
                    }

                    while (isRunning) {
                        try {
                            Socket socket = serverSocket.accept();
                            synchronized (activeClientSockets) {
                                activeClientSockets.add(socket);
                            }
                            new Thread(new ClientHandler(socket)).start();
                        } catch (SocketException se) {
                            // Server socket bị đóng khi dừng
                            break;
                        }
                    }
                } catch (IOException e) {
                    if (isRunning && listener != null) {
                        listener.onLog("Lỗi Server: " + e.getMessage());
                    }
                } finally {
                    stopServer();
                }
            }
        });

        serverThread.start();
    }

    public synchronized void stopServer() {
        if (!isRunning && serverSocket == null) return;
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            // Ignore
        }

        synchronized (activeClientSockets) {
            for (Socket s : activeClientSockets) {
                try {
                    if (!s.isClosed()) s.close();
                } catch (IOException ignored) {}
            }
            activeClientSockets.clear();
        }

        if (listener != null) {
            listener.onServerStopped();
            listener.onLog("Server đã dừng hoạt động.");
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public static String getLocalIPAddress() {
        try {
            List<String> candidates = new ArrayList<>();
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces != null && interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp() || iface.isVirtual()) continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        String ip = addr.getHostAddress();
                        // Ưu tiên dải mạng LAN nội bộ (192.168.x.x hoặc 10.x.x.x)
                        if (ip.startsWith("192.168.") || ip.startsWith("10.")) {
                            return ip;
                        }
                        candidates.add(ip);
                    }
                }
            }
            if (!candidates.isEmpty()) {
                return candidates.get(0);
            }
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }

    private class ClientHandler implements Runnable {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            String clientAddress = socket.getInetAddress().getHostAddress();
            File targetFile = null;
            try {
                // Đặt timeout 30 giây để tránh treo socket vĩnh viễn khi mạng ngắt đột ngột
                socket.setSoTimeout(30000);

                DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

                // Đọc lệnh từ Client
                String command = dis.readUTF();

                if ("PING".equalsIgnoreCase(command)) {
                    if (listener != null) {
                        listener.onLog("[PING] Kiểm tra kết nối từ Client IP: " + clientAddress);
                    }
                    dos.writeUTF("PONG");

                    // Gửi thông tin Hạn nộp bài & Trạng thái cổng nộp về Client
                    DeadlineManager dm = DeadlineManager.getInstance();
                    dos.writeBoolean(dm.isEnabled());
                    dos.writeUTF(dm.getFormattedDeadline());
                    dos.writeBoolean(dm.isExpired());
                    dos.writeBoolean(dm.isBlockLate());
                    dos.writeLong(dm.getRemainingSeconds());
                    dos.writeBoolean(dm.isManualClosed());
                    dos.flush();
                    return;
                }

                if ("CHECK_GRADE".equalsIgnoreCase(command)) {
                    String queryStudentId = dis.readUTF().trim().toUpperCase();
                    if (listener != null) {
                        listener.onLog("[TRA CỨU ĐIỂM] Sinh viên [" + queryStudentId + "] tra cứu điểm từ Client IP: " + clientAddress);
                    }
                    List<SubmissionModel> subs = SubmissionDAO.getSubmissionsByStudent(queryStudentId);
                    dos.writeBoolean(true);
                    dos.writeInt(subs.size());
                    for (SubmissionModel sm : subs) {
                        dos.writeInt(sm.getId());
                        dos.writeUTF(sm.getFileName());
                        dos.writeUTF(sm.getSubmissionTime());
                        dos.writeUTF(sm.getStatus());
                        boolean hasScore = sm.isGraded();
                        dos.writeBoolean(hasScore);
                        if (hasScore) {
                            dos.writeDouble(sm.getScore());
                        }
                        dos.writeUTF(sm.getFeedback());
                        dos.writeUTF(sm.getGradedAt());
                    }
                    dos.flush();
                    return;
                }

                if ("SUBMIT".equalsIgnoreCase(command)) {
                    String rawStudentId = dis.readUTF();
                    String studentName = dis.readUTF();
                    String studentClass = dis.readUTF();
                    String rawFileName = dis.readUTF();
                    long fileSize = dis.readLong();
                    String clientSha256 = dis.readUTF(); // Nhận mã băm SHA-256 từ client

                    // 0. Kiểm tra Hạn nộp & Cổng nhận bài của Máy chủ
                    DeadlineManager dm = DeadlineManager.getInstance();
                    if (!dm.isSubmissionAllowed()) {
                        // Đọc bỏ các byte file đang gửi lên để tránh làm vỡ kết nối Socket của Client
                        byte[] discard = new byte[8192];
                        long remaining = fileSize;
                        while (remaining > 0) {
                            int r = dis.read(discard, 0, (int) Math.min(discard.length, remaining));
                            if (r == -1) break;
                            remaining -= r;
                        }

                        String reason;
                        if (dm.isManualClosed()) {
                            reason = "Cổng nhận bài đã được Giảng viên khóa thủ công!";
                        } else {
                            reason = "Đã quá hạn chót nộp bài (" + dm.getFormattedDeadline() + ")! Máy chủ đang chặn nộp muộn.";
                        }

                        if (listener != null) {
                            listener.onLog(String.format("[TỪ CHỐI BÀI NỘP] Sinh viên [%s - %s] bị từ chối: %s",
                                    rawStudentId, studentName, reason));
                        }

                        dos.writeBoolean(false);
                        dos.writeUTF("Từ chối nhận bài: " + reason);
                        dos.flush();
                        return;
                    }

                    // 1. Kiểm tra kích thước file
                    if (fileSize <= 0) {
                        dos.writeBoolean(false);
                        dos.writeUTF("Lỗi: Kích thước file phải lớn hơn 0 byte!");
                        dos.flush();
                        return;
                    }
                    if (fileSize > SecurityUtil.MAX_FILE_SIZE) {
                        dos.writeBoolean(false);
                        dos.writeUTF(String.format("Lỗi: Dung lượng file vượt quá giới hạn tối đa cho phép (%,d MB)!",
                                SecurityUtil.MAX_FILE_SIZE / (1024 * 1024)));
                        dos.flush();
                        return;
                    }

                    // 2. Chặn các file thực thi nguy hiểm (Bảo vệ Giảng viên khi mở file)
                    if (SecurityUtil.isBlockedExtension(rawFileName)) {
                        dos.writeBoolean(false);
                        dos.writeUTF("Bảo mật: Hệ thống từ chối nhận định dạng file thực thi/script (" + rawFileName + ")!");
                        dos.flush();
                        if (listener != null) {
                            listener.onLog("[CẢNH BÁO BẢO MẬT] Từ chối file thực thi nguy hiểm từ Client [" + clientAddress + "]: " + rawFileName);
                        }
                        return;
                    }

                    // 3. Làm sạch MSSV & Tên file để chống tấn công Path Traversal và chuẩn hóa UPPERCASE
                    final String safeStudentId;
                    try {
                        safeStudentId = SecurityUtil.sanitizeStudentId(rawStudentId).toUpperCase();
                    } catch (IllegalArgumentException ex) {
                        dos.writeBoolean(false);
                        dos.writeUTF("Lỗi bảo mật: " + ex.getMessage());
                        dos.flush();
                        return;
                    }

                    studentName = (studentName != null ? studentName.trim().toUpperCase() : "");
                    studentClass = (studentClass != null ? studentClass.trim().toUpperCase() : "N/A");

                    String safeFileName = SecurityUtil.sanitizeFileName(rawFileName);

                    // 4. Tạo thư mục an toàn và xử lý chống ghi đè (File Versioning)
                    File storageRoot = new File(STORAGE_DIR);
                    File studentDir = SecurityUtil.getSafeStudentDir(storageRoot, safeStudentId);
                    targetFile = SecurityUtil.getUniqueTargetFile(studentDir, safeFileName);

                    if (listener != null) {
                        listener.onLog(String.format("[SUBMIT] Nhận bài từ SV: %s - %s (%s) - File: %s (%,d bytes) | Lưu tại: %s",
                                safeStudentId, studentName, studentClass, rawFileName, fileSize, targetFile.getName()));
                    }

                    // 5. Nhận stream file và tính đồng thời mã SHA-256 trên luồng dữ liệu
                    MessageDigest md = MessageDigest.getInstance("SHA-256");
                    try (FileOutputStream fos = new FileOutputStream(targetFile);
                         BufferedOutputStream bos = new BufferedOutputStream(fos)) {

                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        long totalRead = 0;

                        while (totalRead < fileSize &&
                                (bytesRead = dis.read(buffer, 0, (int) Math.min(buffer.length, fileSize - totalRead))) != -1) {
                            bos.write(buffer, 0, bytesRead);
                            md.update(buffer, 0, bytesRead);
                            totalRead += bytesRead;
                        }
                        bos.flush();

                        if (totalRead != fileSize) {
                            if (targetFile.exists()) targetFile.delete();
                            throw new IOException("Dữ liệu nhận không đầy đủ: " + totalRead + "/" + fileSize + " bytes.");
                        }
                    }

                    // 6. Kiểm tra mã băm SHA-256 đối soát toàn vẹn
                    String serverSha256 = SecurityUtil.bytesToHex(md.digest());
                    if (clientSha256 != null && !clientSha256.trim().isEmpty() &&
                            !serverSha256.equalsIgnoreCase(clientSha256.trim())) {
                        if (targetFile.exists()) targetFile.delete(); // Xóa file lỗi toàn vẹn
                        if (listener != null) {
                            listener.onLog(String.format("[CẢNH BÁO TOÀN VẸN] File [%s] của SV [%s] bị sai lệch mã băm! (Client SHA: %s, Server SHA: %s). Đã xóa file hỏng.",
                                    targetFile.getName(), safeStudentId, clientSha256, serverSha256));
                        }
                        dos.writeBoolean(false);
                        dos.writeUTF("Lỗi toàn vẹn dữ liệu: Mã băm SHA-256 không khớp! File trên máy chủ đã được hủy để bảo đảm an toàn. Vui lòng nộp lại.");
                        dos.flush();
                        return;
                    }

                    // 7. Kiểm tra trùng lặp / nghi vấn sao chép bài tập (Duplicate Check)
                    try {
                        SubmissionModel duplicate = SubmissionDAO.findDuplicateByHash(serverSha256, safeStudentId);
                        if (duplicate != null && listener != null) {
                            listener.onLog(String.format("[CẢNH BÁO TRÙNG LẶP] Phát hiện bài của SV [%s - %s] có nội dung (SHA-256: %s) GIỐNG HỆT bài của SV [%s - %s] (File: %s)!",
                                    safeStudentId, studentName, serverSha256.substring(0, 8) + "...",
                                    duplicate.getStudentId(), duplicate.getStudentName(), duplicate.getFileName()));
                        }
                    } catch (Exception ex) {
                        // Bỏ qua lỗi tra cứu trùng lặp để không cản trở việc lưu bài
                    }

                    // 8. Lưu hoặc cập nhật thông tin sinh viên vào Database
                    try {
                        StudentDAO.upsertStudent(safeStudentId, studentName, studentClass);
                    } catch (Exception ex) {
                        if (listener != null) {
                            listener.onLog("[DB LỖI] Không thể lưu thông tin sinh viên: " + ex.getMessage());
                        }
                    }

                    // 9. Xác định trạng thái nộp bài (Đúng hạn / Nộp muộn) và tạo model
                    String submissionStatus = dm.evaluateSubmissionStatus();
                    SubmissionModel submission = new SubmissionModel(
                            safeStudentId, studentName, studentClass, targetFile.getName(), fileSize, submissionStatus, targetFile.getAbsolutePath(), serverSha256
                    );

                    try {
                        SubmissionDAO.insertSubmission(submission);
                    } catch (Exception ex) {
                        if (listener != null) {
                            listener.onLog("[DB LỖI] Không thể lưu bài nộp vào CSDL: " + ex.getMessage());
                        }
                    }

                    if (listener != null) {
                        listener.onLog(String.format("--> [LƯU BÀI THÀNH CÔNG] SV [%s - %s] - File: %s | SHA-256: %s | Trạng thái: %s",
                                safeStudentId, studentName, targetFile.getName(), submission.getShortHash(), submissionStatus));
                        listener.onSubmissionReceived(submission);
                    }

                    // 10. Gửi kết quả xác nhận thành công về Client
                    dos.writeBoolean(true);
                    if (submissionStatus.startsWith("Nộp muộn")) {
                        dos.writeUTF(String.format("Nộp bài thành công (CẢNH BÁO: %s)!\nFile '%s' đã được bảo vệ toàn vẹn (SHA-256: %s)\nHạn chót: %s | Thời điểm nộp: %s",
                                submissionStatus.toUpperCase(), targetFile.getName(), submission.getShortHash(), dm.getFormattedDeadline(), submission.getSubmissionTime()));
                    } else {
                        dos.writeUTF(String.format("Nộp bài thành công (%s)! File '%s' đã được bảo vệ toàn vẹn (SHA-256: %s) lúc %s",
                                submissionStatus, targetFile.getName(), submission.getShortHash(), submission.getSubmissionTime()));
                    }
                    dos.flush();
                } else {
                    dos.writeBoolean(false);
                    dos.writeUTF("Lệnh không hợp lệ: " + command);
                    dos.flush();
                }

            } catch (Exception e) {
                if (targetFile != null && targetFile.exists() && targetFile.length() == 0) {
                    targetFile.delete();
                }
                if (listener != null) {
                    listener.onLog("Lỗi truyền nhận với Client [" + clientAddress + "]: " + e.getMessage());
                }
                try {
                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                    dos.writeBoolean(false);
                    dos.writeUTF("Lỗi trên Server: " + e.getMessage());
                    dos.flush();
                } catch (Exception ignored) {}
            } finally {
                synchronized (activeClientSockets) {
                    activeClientSockets.remove(socket);
                }
                try {
                    if (!socket.isClosed()) socket.close();
                } catch (IOException ignored) {}
            }
        }
    }
}
