package damh.giaodien;

import damh.giaodien.MeetTheme.*;
import damh.util.SecurityUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ClientGUI extends JFrame {

    // Kết nối mạng
    private MeetTextField txtServerIp;
    private MeetTextField txtServerPort;
    private MeetButton btnPing;
    private MeetBadge badgeConnection;
    private MeetBadge badgeDeadline;

    // Thông tin sinh viên
    private MeetTextField txtStudentId;
    private MeetTextField txtStudentName;
    private MeetTextField txtStudentClass;
    private MeetAvatar studentAvatar;
    private JLabel lblStudentTag;

    // Chọn file bài làm
    private MeetTextField txtFilePath;
    private MeetButton btnBrowse;
    private JLabel lblFileNameDisplay;
    private JLabel lblFileSizeDisplay;
    private JLabel lblSha256Display;
    private File selectedFile;

    // Tiến trình và thao tác
    private MeetProgressBar progressBar;
    private JLabel lblProgressText;
    private MeetButton btnSubmit;
    private MeetButton btnCheckGrade;
    private MeetButton btnReset;
    private MeetButton btnLeave;

    // Chat / Phản hồi máy chủ
    private JTextArea txtServerResponse;

    public ClientGUI() {
        setTitle("Google Meet — Phòng Nộp Bài Thi Trực Tuyến (Sinh Viên)");
        setSize(960, 720);
        setMinimumSize(new Dimension(860, 660));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(MeetTheme.BG_DARK);

        initUI();
        initEvents();
    }

    private void initUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        JPanel rootPanel = new JPanel(new BorderLayout(0, 10));
        rootPanel.setBackground(MeetTheme.BG_DARK);
        rootPanel.setBorder(new EmptyBorder(12, 16, 12, 16));

        // 1. TOP BAR: Tiêu đề cuộc họp, Badge Hạn nộp & Trạng thái kết nối
        JPanel topBar = new JPanel(new BorderLayout(10, 0));
        topBar.setOpaque(false);
        topBar.setPreferredSize(new Dimension(800, 46));

        JPanel topLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        topLeft.setOpaque(false);

        // Logo Google Meet thu nhỏ
        JPanel miniLogo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                MeetTheme.drawMeetLogo((Graphics2D) g, 0, 2, 28);
            }
        };
        miniLogo.setPreferredSize(new Dimension(32, 32));
        miniLogo.setOpaque(false);
        topLeft.add(miniLogo);

        JLabel lblRoomTitle = new JLabel("Phòng Thu Bài Thi LAN");
        lblRoomTitle.setFont(MeetTheme.fontBold(15));
        lblRoomTitle.setForeground(MeetTheme.TEXT_PRIMARY);
        topLeft.add(lblRoomTitle);

        MeetBadge roomCodeBadge = new MeetBadge("lan-exam-12345", null, MeetTheme.TEXT_SECONDARY, MeetTheme.CARD_BG_ALT);
        topLeft.add(roomCodeBadge);

        topBar.add(topLeft, BorderLayout.WEST);

        // Center / Right Badges: Hạn nộp bài & Trạng thái kết nối
        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        topRight.setOpaque(false);

        badgeDeadline = new MeetBadge("Hạn chót: Nhấn 'Kiểm tra' để đồng bộ", MeetTheme.GOOGLE_YELLOW, MeetTheme.TEXT_PRIMARY, MeetTheme.CARD_BG);
        badgeConnection = new MeetBadge("Chưa kết nối máy chủ", Color.GRAY, MeetTheme.TEXT_SECONDARY, MeetTheme.CARD_BG);

        topRight.add(badgeDeadline);
        topRight.add(badgeConnection);
        topBar.add(topRight, BorderLayout.EAST);

        rootPanel.add(topBar, BorderLayout.NORTH);

        // 2. CENTER STAGE: Hai ô (Tiles) phong cách Google Meet
        JPanel stagePanel = new JPanel(new GridLayout(1, 2, 14, 0));
        stagePanel.setOpaque(false);

        // ================= TILE 1: THÔNG TIN SINH VIÊN (Người tham gia) =================
        MeetCard tileStudent = new MeetCard(16);
        tileStudent.setLayout(new BorderLayout(10, 10));
        tileStudent.setBorder(new EmptyBorder(14, 16, 14, 16));

        // Top của Tile 1: Tiêu đề thẻ & Mic badge
        JPanel tile1Header = new JPanel(new BorderLayout());
        tile1Header.setOpaque(false);
        JLabel lblTile1 = new JLabel("Thông Tin Sinh Viên (Người Tham Gia)");
        lblTile1.setFont(MeetTheme.fontBold(13));
        lblTile1.setForeground(MeetTheme.GOOGLE_BLUE_LIGHT);

        MeetBadge micBadge = new MeetBadge("Sẵn sàng", MeetTheme.GOOGLE_GREEN_BRIGHT, MeetTheme.TEXT_PRIMARY, MeetTheme.CARD_BG_ALT);
        tile1Header.add(lblTile1, BorderLayout.WEST);
        tile1Header.add(micBadge, BorderLayout.EAST);
        tileStudent.add(tile1Header, BorderLayout.NORTH);

        // Giữa của Tile 1: Avatar và Form nhập
        JPanel tile1Body = new JPanel();
        tile1Body.setLayout(new BoxLayout(tile1Body, BoxLayout.Y_AXIS));
        tile1Body.setOpaque(false);

        // Avatar tròn lớn
        JPanel avatarRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
        avatarRow.setOpaque(false);
        studentAvatar = new MeetAvatar("SV", 74);
        avatarRow.add(studentAvatar);
        tile1Body.add(avatarRow);

        tile1Body.add(Box.createVerticalStrut(6));

        // Form Fields
        JLabel lblId = new JLabel("Mã số sinh viên (MSSV) (*):");
        lblId.setFont(MeetTheme.fontBold(12));
        lblId.setForeground(MeetTheme.TEXT_SECONDARY);
        lblId.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtStudentId = new MeetTextField("Nhập MSSV (ví dụ: B20DCCN001)");
        txtStudentId.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtStudentId.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel lblName = new JLabel("Họ và tên sinh viên (*):");
        lblName.setFont(MeetTheme.fontBold(12));
        lblName.setForeground(MeetTheme.TEXT_SECONDARY);
        lblName.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtStudentName = new MeetTextField("Nhập đầy đủ Họ và Tên");
        txtStudentName.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtStudentName.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel lblClass = new JLabel("Lớp sinh hoạt / Học phần:");
        lblClass.setFont(MeetTheme.fontBold(12));
        lblClass.setForeground(MeetTheme.TEXT_SECONDARY);
        lblClass.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtStudentClass = new MeetTextField("Nhập lớp (ví dụ: D20CQCN01-N)");
        txtStudentClass.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtStudentClass.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        // Cấu hình IP và Port máy chủ
        JLabel lblServerConfig = new JLabel("Địa chỉ Máy chủ (Server):");
        lblServerConfig.setFont(MeetTheme.fontBold(12));
        lblServerConfig.setForeground(MeetTheme.TEXT_SECONDARY);
        lblServerConfig.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel netRow = new JPanel(new BorderLayout(8, 0));
        netRow.setOpaque(false);
        netRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        netRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        txtServerIp = new MeetTextField("127.0.0.1");
        txtServerIp.setPreferredSize(new Dimension(180, 36));
        txtServerPort = new MeetTextField("12345");
        txtServerPort.setPreferredSize(new Dimension(65, 36));
        btnPing = new MeetButton("Kiểm tra", MeetButton.Variant.NEUTRAL);
        btnPing.setPreferredSize(new Dimension(95, 36));

        JPanel ipPortPanel = new JPanel(new BorderLayout(4, 0));
        ipPortPanel.setOpaque(false);
        ipPortPanel.add(txtServerIp, BorderLayout.CENTER);
        ipPortPanel.add(txtServerPort, BorderLayout.EAST);

        netRow.add(ipPortPanel, BorderLayout.CENTER);
        netRow.add(btnPing, BorderLayout.EAST);

        tile1Body.add(lblId);
        tile1Body.add(Box.createVerticalStrut(3));
        tile1Body.add(txtStudentId);
        tile1Body.add(Box.createVerticalStrut(8));

        tile1Body.add(lblName);
        tile1Body.add(Box.createVerticalStrut(3));
        tile1Body.add(txtStudentName);
        tile1Body.add(Box.createVerticalStrut(8));

        tile1Body.add(lblClass);
        tile1Body.add(Box.createVerticalStrut(3));
        tile1Body.add(txtStudentClass);
        tile1Body.add(Box.createVerticalStrut(8));

        tile1Body.add(lblServerConfig);
        tile1Body.add(Box.createVerticalStrut(3));
        tile1Body.add(netRow);

        tileStudent.add(tile1Body, BorderLayout.CENTER);

        // Tag sinh viên góc dưới trái phong cách Google Meet
        JPanel tile1Footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        tile1Footer.setOpaque(false);
        lblStudentTag = new JLabel("Sinh viên chưa nhập thông tin");
        lblStudentTag.setFont(MeetTheme.fontBold(12));
        lblStudentTag.setForeground(MeetTheme.TEXT_MUTED);
        tile1Footer.add(lblStudentTag);
        tileStudent.add(tile1Footer, BorderLayout.SOUTH);

        stagePanel.add(tileStudent);

        // ================= TILE 2: CHIA SẺ FILE BÀI TẬP (Presentation Tile) =================
        MeetCard tileFile = new MeetCard(16);
        tileFile.setLayout(new BorderLayout(10, 10));
        tileFile.setBorder(new EmptyBorder(14, 16, 14, 16));

        // Top của Tile 2
        JPanel tile2Header = new JPanel(new BorderLayout());
        tile2Header.setOpaque(false);
        JLabel lblTile2 = new JLabel("Chia Sẻ Bài Làm (File Submission)");
        lblTile2.setFont(MeetTheme.fontBold(13));
        lblTile2.setForeground(MeetTheme.GOOGLE_GREEN_BRIGHT);

        MeetBadge shareBadge = new MeetBadge("Tải tệp tin", MeetTheme.GOOGLE_BLUE, MeetTheme.TEXT_PRIMARY, MeetTheme.CARD_BG_ALT);
        tile2Header.add(lblTile2, BorderLayout.WEST);
        tile2Header.add(shareBadge, BorderLayout.EAST);
        tileFile.add(tile2Header, BorderLayout.NORTH);

        // Body của Tile 2: Khung chọn file, hiển thị SHA-256, Progress, Phản hồi
        JPanel tile2Body = new JPanel();
        tile2Body.setLayout(new BoxLayout(tile2Body, BoxLayout.Y_AXIS));
        tile2Body.setOpaque(false);

        // Box chọn file
        MeetCard fileBox = new MeetCard(12);
        fileBox.setCardBackground(MeetTheme.CARD_BG_ALT);
        fileBox.setLayout(new BorderLayout(8, 8));
        fileBox.setBorder(new EmptyBorder(10, 12, 10, 12));
        fileBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        JPanel fileChooseRow = new JPanel(new BorderLayout(8, 0));
        fileChooseRow.setOpaque(false);
        txtFilePath = new MeetTextField("Chưa chọn tệp bài làm");
        txtFilePath.setEditable(false);
        btnBrowse = new MeetButton("Chọn file", MeetButton.Variant.NEUTRAL);
        btnBrowse.setPreferredSize(new Dimension(110, 36));
        fileChooseRow.add(txtFilePath, BorderLayout.CENTER);
        fileChooseRow.add(btnBrowse, BorderLayout.EAST);

        JPanel fileDetailsPanel = new JPanel();
        fileDetailsPanel.setLayout(new BoxLayout(fileDetailsPanel, BoxLayout.Y_AXIS));
        fileDetailsPanel.setOpaque(false);

        lblFileNameDisplay = new JLabel("Tên file: Chưa chọn");
        lblFileNameDisplay.setFont(MeetTheme.fontBold(12));
        lblFileNameDisplay.setForeground(MeetTheme.TEXT_PRIMARY);

        lblFileSizeDisplay = new JLabel("Kích thước: 0 Bytes");
        lblFileSizeDisplay.setFont(MeetTheme.fontPlain(11));
        lblFileSizeDisplay.setForeground(MeetTheme.TEXT_SECONDARY);

        lblSha256Display = new JLabel("SHA-256: Sẽ tự động đối soát khi chọn file");
        lblSha256Display.setFont(MeetTheme.fontMono(11));
        lblSha256Display.setForeground(MeetTheme.GOOGLE_BLUE_LIGHT);

        fileDetailsPanel.add(lblFileNameDisplay);
        fileDetailsPanel.add(Box.createVerticalStrut(2));
        fileDetailsPanel.add(lblFileSizeDisplay);
        fileDetailsPanel.add(Box.createVerticalStrut(2));
        fileDetailsPanel.add(lblSha256Display);

        fileBox.add(fileChooseRow, BorderLayout.NORTH);
        fileBox.add(fileDetailsPanel, BorderLayout.CENTER);

        tile2Body.add(fileBox);
        tile2Body.add(Box.createVerticalStrut(10));

        // Thanh tiến trình tải lên
        JLabel lblProgTitle = new JLabel("Tiến trình tải lên & xác thực toàn vẹn:");
        lblProgTitle.setFont(MeetTheme.fontBold(12));
        lblProgTitle.setForeground(MeetTheme.TEXT_SECONDARY);
        lblProgTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        progressBar = new MeetProgressBar();
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

        lblProgressText = new JLabel("Sẵn sàng nộp bài.");
        lblProgressText.setFont(MeetTheme.fontPlain(11));
        lblProgressText.setForeground(MeetTheme.TEXT_MUTED);
        lblProgressText.setAlignmentX(Component.LEFT_ALIGNMENT);

        tile2Body.add(lblProgTitle);
        tile2Body.add(Box.createVerticalStrut(4));
        tile2Body.add(progressBar);
        tile2Body.add(Box.createVerticalStrut(2));
        tile2Body.add(lblProgressText);
        tile2Body.add(Box.createVerticalStrut(8));

        // Khung phản hồi cuộc họp / Chat từ Server
        JLabel lblResponseTitle = new JLabel("Tin nhắn & phản hồi từ Giảng viên / Server:");
        lblResponseTitle.setFont(MeetTheme.fontBold(12));
        lblResponseTitle.setForeground(MeetTheme.TEXT_SECONDARY);
        lblResponseTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtServerResponse = new JTextArea(4, 20);
        txtServerResponse.setEditable(false);
        txtServerResponse.setLineWrap(true);
        txtServerResponse.setWrapStyleWord(true);
        txtServerResponse.setFont(MeetTheme.fontMono(12));
        txtServerResponse.setBackground(MeetTheme.CARD_BG_ALT);
        txtServerResponse.setForeground(MeetTheme.TEXT_PRIMARY);
        txtServerResponse.setCaretColor(Color.WHITE);
        txtServerResponse.setBorder(new EmptyBorder(6, 8, 6, 8));

        JScrollPane scrollResponse = new JScrollPane(txtServerResponse);
        MeetTheme.styleScrollPane(scrollResponse);
        scrollResponse.setAlignmentX(Component.LEFT_ALIGNMENT);

        tile2Body.add(lblResponseTitle);
        tile2Body.add(Box.createVerticalStrut(4));
        tile2Body.add(scrollResponse);

        tileFile.add(tile2Body, BorderLayout.CENTER);
        stagePanel.add(tileFile);

        rootPanel.add(stagePanel, BorderLayout.CENTER);

        // 3. BOTTOM CONTROL DOCK: Thanh điều khiển Google Meet bo tròn
        JPanel dockContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
        dockContainer.setOpaque(false);

        MeetCard controlDock = new MeetCard(28);
        controlDock.setCardBackground(MeetTheme.BG_SURFACE);
        controlDock.setLayout(new FlowLayout(FlowLayout.CENTER, 14, 8));
        controlDock.setBorder(new EmptyBorder(4, 16, 4, 16));

        btnSubmit = new MeetButton("Nộp bài ngay", MeetButton.Variant.PRIMARY);
        btnSubmit.setPreferredSize(new Dimension(160, 44));
        btnSubmit.setCornerRadius(22);

        btnCheckGrade = new MeetButton("Xem điểm", MeetButton.Variant.SUCCESS);
        btnCheckGrade.setPreferredSize(new Dimension(135, 44));
        btnCheckGrade.setCornerRadius(22);
        btnCheckGrade.setToolTipText("Tra cứu điểm số và nhận xét bài nộp của bạn từ Giảng viên");

        btnReset = new MeetButton("Làm Mới", MeetButton.Variant.NEUTRAL);
        btnReset.setPreferredSize(new Dimension(100, 44));
        btnReset.setCornerRadius(22);

        btnLeave = new MeetButton("Rời Phòng", MeetButton.Variant.DANGER);
        btnLeave.setPreferredSize(new Dimension(110, 44));
        btnLeave.setCornerRadius(22);

        controlDock.add(btnSubmit);
        controlDock.add(btnCheckGrade);
        controlDock.add(btnReset);
        controlDock.add(btnLeave);

        dockContainer.add(controlDock);
        rootPanel.add(dockContainer, BorderLayout.SOUTH);

        setContentPane(rootPanel);
    }

    private void initEvents() {
        // Áp dụng bộ lọc tự động viết hoa (UPPERCASE) cho MSSV và Lớp khi gõ
        UppercaseDocumentFilter upperFilter = new UppercaseDocumentFilter();
        if (txtStudentId.getDocument() instanceof javax.swing.text.AbstractDocument) {
            ((javax.swing.text.AbstractDocument) txtStudentId.getDocument()).setDocumentFilter(upperFilter);
        }
        if (txtStudentClass.getDocument() instanceof javax.swing.text.AbstractDocument) {
            ((javax.swing.text.AbstractDocument) txtStudentClass.getDocument()).setDocumentFilter(upperFilter);
        }

        // Tự động chuyển Họ và tên sang IN HOA khi rời ô nhập hoặc nhấn Enter (tránh xung đột bộ gõ tiếng Việt Telex/Unikey)
        txtStudentName.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                String cur = txtStudentName.getText();
                if (cur != null && !cur.trim().isEmpty()) {
                    txtStudentName.setText(cur.trim().toUpperCase());
                }
            }
        });
        txtStudentName.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String cur = txtStudentName.getText();
                if (cur != null && !cur.trim().isEmpty()) {
                    txtStudentName.setText(cur.trim().toUpperCase());
                }
            }
        });

        txtStudentId.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                String cur = txtStudentId.getText();
                if (cur != null && !cur.trim().isEmpty()) {
                    txtStudentId.setText(cur.trim().toUpperCase());
                }
            }
        });
        txtStudentClass.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                String cur = txtStudentClass.getText();
                if (cur != null && !cur.trim().isEmpty()) {
                    txtStudentClass.setText(cur.trim().toUpperCase());
                }
            }
        });

        // Cập nhật Avatar và Tag sinh viên theo thời gian thực khi gõ phím
        DocumentListener studentInfoListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updateStudentDisplay(); }
            @Override
            public void removeUpdate(DocumentEvent e) { updateStudentDisplay(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updateStudentDisplay(); }
        };
        txtStudentId.getDocument().addDocumentListener(studentInfoListener);
        txtStudentName.getDocument().addDocumentListener(studentInfoListener);
        txtStudentClass.getDocument().addDocumentListener(studentInfoListener);

        btnPing.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pingServer();
            }
        });

        btnBrowse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseFile();
            }
        });

        btnSubmit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitAssignment();
            }
        });

        btnCheckGrade.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkGradeFromServer();
            }
        });

        btnReset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetForm();
            }
        });

        btnLeave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    private void updateStudentDisplay() {
        String name = txtStudentName.getText().trim().toUpperCase();
        String id = txtStudentId.getText().trim().toUpperCase();
        String clazz = txtStudentClass.getText().trim().toUpperCase();

        // Cập nhật chữ trên Avatar
        if (!name.isEmpty()) {
            String[] parts = name.split("\\s+");
            if (parts.length >= 2) {
                String firstChar = parts[0].substring(0, 1).toUpperCase();
                String lastChar = parts[parts.length - 1].substring(0, 1).toUpperCase();
                studentAvatar.setInitials(firstChar + lastChar);
            } else {
                studentAvatar.setInitials(parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase());
            }
        } else if (!id.isEmpty()) {
            studentAvatar.setInitials(id.substring(0, Math.min(2, id.length())).toUpperCase());
        } else {
            studentAvatar.setInitials("SV");
        }

        // Cập nhật nhãn tên góc trái
        if (!name.isEmpty() || !id.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            if (!name.isEmpty()) sb.append(name);
            if (!id.isEmpty()) sb.append(" (").append(id).append(")");
            if (!clazz.isEmpty()) sb.append(" - ").append(clazz);
            lblStudentTag.setText(sb.toString());
            lblStudentTag.setForeground(MeetTheme.TEXT_PRIMARY);
        } else {
            lblStudentTag.setText("Sinh viên chưa nhập thông tin");
            lblStudentTag.setForeground(MeetTheme.TEXT_MUTED);
        }
    }

    private void pingServer() {
        final String ip = txtServerIp.getText().trim();
        final int port;
        try {
            port = Integer.parseInt(txtServerPort.getText().trim());
        } catch (NumberFormatException e) {
            badgeConnection.updateBadge("Port không hợp lệ", Color.RED, Color.WHITE, new Color(0x3c, 0x10, 0x10));
            return;
        }

        badgeConnection.updateBadge("Đang kiểm tra...", MeetTheme.GOOGLE_YELLOW, MeetTheme.TEXT_PRIMARY, MeetTheme.CARD_BG_ALT);
        btnPing.setEnabled(false);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(ip, port), 3000);
                    DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                    DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

                    dos.writeUTF("PING");
                    dos.flush();

                    String reply = dis.readUTF();
                    if ("PONG".equalsIgnoreCase(reply)) {
                        boolean hasDeadline = false;
                        String deadlineStr = "Không giới hạn";
                        boolean isExpired = false;
                        boolean isBlockLate = false;
                        long remainingSec = 0;
                        boolean isManualClosed = false;

                        try {
                            hasDeadline = dis.readBoolean();
                            deadlineStr = dis.readUTF();
                            isExpired = dis.readBoolean();
                            isBlockLate = dis.readBoolean();
                            remainingSec = dis.readLong();
                            isManualClosed = dis.readBoolean();
                        } catch (Exception ignored) {}

                        final boolean fHasDeadline = hasDeadline;
                        final String fDeadlineStr = deadlineStr;
                        final boolean fIsExpired = isExpired;
                        final boolean fIsBlockLate = isBlockLate;
                        final long fRemainingSec = remainingSec;
                        final boolean fIsManualClosed = isManualClosed;

                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                badgeConnection.updateBadge("Kết nối tốt (" + ip + ":" + port + ")", MeetTheme.GOOGLE_GREEN_BRIGHT, Color.WHITE, new Color(0x13, 0x73, 0x33));

                                if (fIsManualClosed) {
                                    badgeDeadline.updateBadge("CỔNG ĐÃ KHÓA THỦ CÔNG", MeetTheme.GOOGLE_RED, Color.WHITE, new Color(0x73, 0x13, 0x13));
                                } else if (!fHasDeadline) {
                                    badgeDeadline.updateBadge("Không giới hạn thời gian nộp", MeetTheme.GOOGLE_GREEN_BRIGHT, Color.WHITE, MeetTheme.CARD_BG_ALT);
                                } else {
                                    if (fIsExpired) {
                                        long diffMins = Math.abs(fRemainingSec) / 60;
                                        if (fIsBlockLate) {
                                            badgeDeadline.updateBadge("ĐÃ HẾT HẠN - MÁY CHỦ KHÓA CỔNG", MeetTheme.GOOGLE_RED, Color.WHITE, new Color(0x73, 0x13, 0x13));
                                        } else {
                                            badgeDeadline.updateBadge("ĐÃ HẾT HẠN (Trễ " + Math.max(1, diffMins) + "p - Vẫn nhận bài)", MeetTheme.GOOGLE_AMBER, Color.WHITE, new Color(0x73, 0x40, 0x13));
                                        }
                                    } else {
                                        long hours = fRemainingSec / 3600;
                                        long mins = (fRemainingSec % 3600) / 60;
                                        long secs = fRemainingSec % 60;
                                        String timeRemain = String.format("%02d:%02d:%02d", hours, mins, secs);
                                        badgeDeadline.updateBadge("Hạn: " + fDeadlineStr + " (Còn: " + timeRemain + ")", MeetTheme.GOOGLE_BLUE_LIGHT, Color.WHITE, new Color(0x13, 0x33, 0x73));
                                    }
                                }
                            }
                        });
                    } else {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                badgeConnection.updateBadge("Phản hồi không khớp", MeetTheme.GOOGLE_AMBER, Color.WHITE, MeetTheme.CARD_BG_ALT);
                            }
                        });
                    }
                } catch (final Exception ex) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            badgeConnection.updateBadge("Không thể kết nối Server", MeetTheme.GOOGLE_RED, Color.WHITE, new Color(0x73, 0x13, 0x13));
                        }
                    });
                } finally {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            btnPing.setEnabled(true);
                        }
                    });
                }
            }
        }).start();
    }

    private void chooseFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn tệp bài tập để nộp");
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            txtFilePath.setText(selectedFile.getAbsolutePath());
            long length = selectedFile.length();
            String sizeStr;
            if (length < 1024) {
                sizeStr = length + " Bytes";
            } else if (length < 1024 * 1024) {
                sizeStr = String.format("%.2f KB (%,d bytes)", length / 1024.0, length);
            } else {
                sizeStr = String.format("%.2f MB (%,d bytes)", length / (1024.0 * 1024.0), length);
            }

            lblFileNameDisplay.setText("Tệp: " + selectedFile.getName());
            lblFileSizeDisplay.setText("Kích thước: " + sizeStr);

            if (SecurityUtil.isBlockedExtension(selectedFile.getName())) {
                lblSha256Display.setText("CẢNH BÁO: File thực thi bị cấm, máy chủ sẽ từ chối!");
                lblSha256Display.setForeground(MeetTheme.GOOGLE_RED);
                JOptionPane.showMessageDialog(this,
                        "Hệ thống từ chối nhận các file thực thi/script (.exe, .bat, .sh, .vbs...) vì lý do an toàn thông tin!",
                        "Định dạng không được phép",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            lblSha256Display.setText("SHA-256: Đang tính toán mã băm an toàn...");
            lblSha256Display.setForeground(MeetTheme.GOOGLE_BLUE_LIGHT);

            // Tính mã SHA-256 nền
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final String hash = SecurityUtil.computeSHA256(selectedFile);
                        final String shortHash = hash.substring(0, 16) + "...";
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                lblSha256Display.setText("SHA-256: " + shortHash + " (Đã đối soát cục bộ)");
                                lblSha256Display.setForeground(MeetTheme.GOOGLE_GREEN_BRIGHT);
                            }
                        });
                    } catch (Exception ex) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                lblSha256Display.setText("SHA-256: Không thể tính toán (" + ex.getMessage() + ")");
                                lblSha256Display.setForeground(MeetTheme.GOOGLE_AMBER);
                            }
                        });
                    }
                }
            }).start();
        }
    }

    private void submitAssignment() {
        final String serverIp = txtServerIp.getText().trim();
        final String portStr = txtServerPort.getText().trim();
        final String studentId = txtStudentId.getText().trim().toUpperCase();
        final String studentName = txtStudentName.getText().trim().toUpperCase();
        final String studentClass = txtStudentClass.getText().trim().toUpperCase();

        // Đồng bộ chữ in hoa lên giao diện
        txtStudentId.setText(studentId);
        txtStudentName.setText(studentName);
        txtStudentClass.setText(studentClass);

        if (studentId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Mã số sinh viên (MSSV)!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            txtStudentId.requestFocus();
            return;
        }

        if (studentName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Họ và tên sinh viên!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            txtStudentName.requestFocus();
            return;
        }

        if (selectedFile == null || !selectedFile.exists() || !selectedFile.isFile()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn file bài tập hợp lệ!", "Chưa chọn file", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (SecurityUtil.isBlockedExtension(selectedFile.getName())) {
            JOptionPane.showMessageDialog(this, "File này có định dạng thực thi bị cấm. Vui lòng nén thành file .zip hoặc .pdf!", "Từ chối nộp", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (selectedFile.length() > SecurityUtil.MAX_FILE_SIZE) {
            JOptionPane.showMessageDialog(this, "File vượt quá giới hạn 150MB!", "File quá lớn", JOptionPane.ERROR_MESSAGE);
            return;
        }

        final int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Cổng Server không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        setFormEnabled(false);
        progressBar.setValue(0);
        lblProgressText.setText("Đang chuẩn bị gói tin và kết nối máy chủ...");
        txtServerResponse.setText("");

        SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
            private boolean success = false;
            private String serverMessage = "";

            @Override
            protected Void doInBackground() throws Exception {
                String sha256 = SecurityUtil.computeSHA256(selectedFile);
                publish(0);

                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(serverIp, port), 5000);
                    socket.setSoTimeout(30000);

                    try (
                        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                        DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                        FileInputStream fis = new FileInputStream(selectedFile);
                        BufferedInputStream bis = new BufferedInputStream(fis)
                    ) {
                        long fileSize = selectedFile.length();

                        dos.writeUTF("SUBMIT");
                        dos.writeUTF(studentId);
                        dos.writeUTF(studentName);
                        dos.writeUTF(studentClass.isEmpty() ? "N/A" : studentClass);
                        dos.writeUTF(selectedFile.getName());
                        dos.writeLong(fileSize);
                        dos.writeUTF(sha256);
                        dos.flush();

                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        long totalSent = 0;

                        while ((bytesRead = bis.read(buffer)) != -1) {
                            dos.write(buffer, 0, bytesRead);
                            totalSent += bytesRead;

                            int percent = (int) ((totalSent * 100) / fileSize);
                            publish(percent);
                        }
                        dos.flush();

                        success = dis.readBoolean();
                        serverMessage = dis.readUTF();
                    }
                } catch (Exception ex) {
                    success = false;
                    serverMessage = "Lỗi truyền dữ liệu: " + ex.getMessage();
                }
                return null;
            }

            @Override
            protected void process(java.util.List<Integer> chunks) {
                if (!chunks.isEmpty()) {
                    int percent = chunks.get(chunks.size() - 1);
                    progressBar.setValue(percent);
                    lblProgressText.setText("Đang tải lên bài làm: " + percent + "%");
                }
            }

            @Override
            protected void done() {
                setFormEnabled(true);
                String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
                if (success) {
                    progressBar.setValue(100);
                    lblProgressText.setText("Hoàn tất 100% — Đối soát SHA-256 thành công.");
                    txtServerResponse.setText("[" + timestamp + " THÀNH CÔNG]\n" + serverMessage);

                    if (serverMessage.contains("CẢNH BÁO") || serverMessage.contains("NỘP MUỘN") || serverMessage.contains("Nộp muộn")) {
                        JOptionPane.showMessageDialog(ClientGUI.this,
                                "Bài tập đã được tải lên và bảo vệ toàn vẹn,\nnhưng HỆ THỐNG GHI NHẬN BẠN ĐÃ NỘP MUỘN!\n\nPhản hồi từ Server:\n" + serverMessage,
                                "Nộp bài thành công (Ghi nhận nộp muộn)",
                                JOptionPane.WARNING_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(ClientGUI.this,
                                "Chúc mừng! Bài tập đã được nộp đúng hạn và xác thực an toàn lên máy chủ.\n\nPhản hồi từ Server:\n" + serverMessage,
                                "Nộp bài thành công (Đúng hạn)",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                } else {
                    lblProgressText.setText("Nộp bài thất bại.");
                    txtServerResponse.setText("[" + timestamp + " LỖI]\n" + serverMessage);
                    JOptionPane.showMessageDialog(ClientGUI.this,
                            "Nộp bài thất bại!\n\nLý do: " + serverMessage,
                            "Thất bại",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    private void setFormEnabled(boolean enabled) {
        btnSubmit.setEnabled(enabled);
        btnCheckGrade.setEnabled(enabled);
        btnBrowse.setEnabled(enabled);
        btnPing.setEnabled(enabled);
        btnReset.setEnabled(enabled);
        txtStudentId.setEditable(enabled);
        txtStudentName.setEditable(enabled);
        txtStudentClass.setEditable(enabled);
    }

    private void checkGradeFromServer() {
        String studentId = txtStudentId.getText().trim().toUpperCase();
        if (studentId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Mã số sinh viên (MSSV) để tra cứu điểm!", "Yêu cầu MSSV", JOptionPane.WARNING_MESSAGE);
            txtStudentId.requestFocus();
            return;
        }

        final String ip = txtServerIp.getText().trim();
        final int port;
        try {
            port = Integer.parseInt(txtServerPort.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Cổng Server không hợp lệ!", "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
            return;
        }

        btnCheckGrade.setEnabled(false);
        txtServerResponse.setText("[ĐANG TRA CỨU ĐIỂM] Kết nối tới máy chủ " + ip + ":" + port + "...");

        new Thread(() -> {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(ip, port), 4000);
                socket.setSoTimeout(10000);

                DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

                dos.writeUTF("CHECK_GRADE");
                dos.writeUTF(studentId);
                dos.flush();

                boolean success = dis.readBoolean();
                int count = dis.readInt();

                StringBuilder sb = new StringBuilder();
                sb.append("=== KẾT QUẢ TRA CỨU ĐIỂM & ĐÁNH GIÁ CỦA GIẢNG VIÊN ===\n");
                sb.append("Sinh viên: ").append(studentId).append("\n");
                sb.append("Tổng số bài đã nộp: ").append(count).append("\n\n");

                if (count == 0) {
                    sb.append("Chưa tìm thấy bài nộp nào của MSSV ").append(studentId).append(" trên hệ thống máy chủ.\n");
                } else {
                    for (int i = 1; i <= count; i++) {
                        int subId = dis.readInt();
                        String fileName = dis.readUTF();
                        String subTime = dis.readUTF();
                        String status = dis.readUTF();
                        boolean hasScore = dis.readBoolean();
                        Double score = hasScore ? dis.readDouble() : null;
                        String feedback = dis.readUTF();
                        String gradedAt = dis.readUTF();

                        sb.append(String.format("【Bài #%d】 File: %s\n", i, fileName));
                        sb.append(String.format("  • Thời gian nộp: %s (%s)\n", subTime, status));
                        if (hasScore) {
                            sb.append(String.format("  • ĐIỂM SỐ: %.1f / 10\n", score));
                            sb.append(String.format("  • Nhận xét giảng viên: %s\n", feedback.isEmpty() ? "(Không có nhận xét)" : feedback));
                            sb.append(String.format("  • Thời gian chấm: %s\n", gradedAt));
                        } else {
                            sb.append("  • ĐIỂM SỐ: [Chưa chấm điểm — Đang chờ giảng viên đánh giá]\n");
                        }
                        sb.append("--------------------------------------------------\n");
                    }
                }

                final String resultText = sb.toString();
                SwingUtilities.invokeLater(() -> {
                    btnCheckGrade.setEnabled(true);
                    txtServerResponse.setText(resultText);
                    txtServerResponse.setCaretPosition(0);
                    JOptionPane.showMessageDialog(ClientGUI.this,
                            resultText,
                            "Bảng Điểm Sinh Viên (" + studentId + ")",
                            JOptionPane.INFORMATION_MESSAGE);
                });

            } catch (Exception ex) {
                final String err = ex.getMessage();
                SwingUtilities.invokeLater(() -> {
                    btnCheckGrade.setEnabled(true);
                    txtServerResponse.setText("[LỖI TRA CỨU] Không thể kết nối tới máy chủ: " + err);
                    JOptionPane.showMessageDialog(ClientGUI.this,
                            "Không thể kết nối tới máy chủ để tra cứu điểm: " + err,
                            "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }

    private void resetForm() {
        selectedFile = null;
        txtFilePath.setText("Chưa chọn tệp bài làm");
        lblFileNameDisplay.setText("📄 Tên file: (Chưa chọn)");
        lblFileSizeDisplay.setText("📦 Kích thước: 0 Bytes");
        lblSha256Display.setText("SHA-256: Sẽ tự động đối soát khi chọn file");
        lblSha256Display.setForeground(MeetTheme.GOOGLE_BLUE_LIGHT);
        progressBar.setValue(0);
        lblProgressText.setText("Sẵn sàng nộp bài.");
        txtServerResponse.setText("");
    }

    private static class UppercaseDocumentFilter extends javax.swing.text.DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String text, javax.swing.text.AttributeSet attr) throws javax.swing.text.BadLocationException {
            if (text != null) {
                super.insertString(fb, offset, text.toUpperCase(), attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, javax.swing.text.AttributeSet attrs) throws javax.swing.text.BadLocationException {
            if (text != null) {
                super.replace(fb, offset, length, text.toUpperCase(), attrs);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClientGUI().setVisible(true);
            }
        });
    }
}
