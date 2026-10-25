package damh;

import damh.SerVer.ServerCore;
import damh.giaodien.ClientGUI;
import damh.giaodien.MeetTheme;
import damh.giaodien.MeetTheme.*;
import damh.giaodien.ServerGUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Trình khởi chạy trung tâm (Launcher) mang phong cách Google Meet Dark Mode.
 * Cho phép người dùng lựa chọn khởi chạy Máy chủ Giảng viên hoặc Máy khách Sinh viên.
 */
public class DAMH {

    public static void main(String[] args) {
        if (args.length > 0) {
            if ("server".equalsIgnoreCase(args[0])) {
                ServerGUI.main(args);
                return;
            } else if ("client".equalsIgnoreCase(args[0])) {
                ClientGUI.main(args);
                return;
            }
        }

        // Khởi chạy cửa sổ chọn vai trò (Launcher) phong cách Google Meet
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                showLauncher();
            }
        });
    }

    private static void showLauncher() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        final JFrame frame = new JFrame("Google Meet - Cổng Nộp Bài & Thi Qua Mạng LAN");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(760, 480);
        frame.setMinimumSize(new Dimension(720, 450));
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(MeetTheme.BG_DARK);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(MeetTheme.BG_DARK);
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        // 1. TOP HEADER (Google Meet Logo & Status)
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);

        MeetLogoHeader logoHeader = new MeetLogoHeader("Google Meet", "Hệ Thống Thu Bài & Tổ Chức Thi Trực Tuyến Qua LAN");
        topPanel.add(logoHeader, BorderLayout.WEST);

        String localIp = ServerCore.getLocalIPAddress();
        MeetBadge ipBadge = new MeetBadge("LAN IP: " + localIp, MeetTheme.GOOGLE_GREEN_BRIGHT, MeetTheme.TEXT_PRIMARY, MeetTheme.CARD_BG);
        JPanel topBadgePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        topBadgePanel.setOpaque(false);
        topBadgePanel.add(ipBadge);
        topPanel.add(topBadgePanel, BorderLayout.EAST);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // 2. CENTER CONTENT: Tiêu đề cuộc họp và 2 Thẻ (Cards) chọn vai trò
        JPanel centerPanel = new JPanel(new BorderLayout(12, 12));
        centerPanel.setOpaque(false);

        JPanel introPanel = new JPanel();
        introPanel.setLayout(new BoxLayout(introPanel, BoxLayout.Y_AXIS));
        introPanel.setOpaque(false);

        JLabel lblHeadline = new JLabel("Cuộc họp nộp bài thi chất lượng cao và an toàn cho mọi người");
        lblHeadline.setFont(MeetTheme.fontBold(17));
        lblHeadline.setForeground(MeetTheme.TEXT_PRIMARY);

        JLabel lblSubHeadline = new JLabel("Chọn vai trò của bạn để bắt đầu phòng thi hoặc tham gia nộp bài trực tuyến:");
        lblSubHeadline.setFont(MeetTheme.fontPlain(13));
        lblSubHeadline.setForeground(MeetTheme.TEXT_SECONDARY);

        introPanel.add(lblHeadline);
        introPanel.add(Box.createVerticalStrut(4));
        introPanel.add(lblSubHeadline);
        introPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(introPanel, BorderLayout.NORTH);

        // 2 Lựa chọn theo dạng Thẻ Google Meet
        JPanel cardsGrid = new JPanel(new GridLayout(1, 2, 18, 0));
        cardsGrid.setOpaque(false);

        // THẺ 1: HOST / SERVER (Giảng viên)
        MeetCard cardServer = new MeetCard(16);
        cardServer.setLayout(new BorderLayout(10, 12));
        cardServer.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel serverTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        serverTop.setOpaque(false);
        MeetBadge serverRoleBadge = new MeetBadge("HOST / QUẢN TRỊ", MeetTheme.GOOGLE_BLUE, MeetTheme.GOOGLE_BLUE_LIGHT, new Color(0x1a, 0x73, 0xe8, 40));
        serverTop.add(serverRoleBadge);

        JPanel serverCenter = new JPanel();
        serverCenter.setLayout(new BoxLayout(serverCenter, BoxLayout.Y_AXIS));
        serverCenter.setOpaque(false);

        JLabel lblServerTitle = new JLabel("Tạo phòng thu bài (Server)");
        lblServerTitle.setFont(MeetTheme.fontBold(16));
        lblServerTitle.setForeground(MeetTheme.TEXT_PRIMARY);

        JLabel lblServerDesc = new JLabel("<html>Khởi tạo máy chủ nhận bài thi qua TCP Socket đa luồng. Quản lý hạn nộp bài, khóa cổng khẩn cấp, tra cứu sinh viên và xuất báo cáo CSV.</html>");
        lblServerDesc.setFont(MeetTheme.fontPlain(12));
        lblServerDesc.setForeground(MeetTheme.TEXT_SECONDARY);

        serverCenter.add(Box.createVerticalStrut(8));
        serverCenter.add(lblServerTitle);
        serverCenter.add(Box.createVerticalStrut(8));
        serverCenter.add(lblServerDesc);

        MeetButton btnServer = new MeetButton("Bắt đầu với vai trò Server", MeetButton.Variant.PRIMARY);
        btnServer.setPreferredSize(new Dimension(280, 42));

        cardServer.add(serverTop, BorderLayout.NORTH);
        cardServer.add(serverCenter, BorderLayout.CENTER);
        cardServer.add(btnServer, BorderLayout.SOUTH);

        // THẺ 2: CLIENT (Sinh viên)
        MeetCard cardClient = new MeetCard(16);
        cardClient.setLayout(new BorderLayout(10, 12));
        cardClient.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel clientTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        clientTop.setOpaque(false);
        MeetBadge clientRoleBadge = new MeetBadge("SINH VIÊN NỘP BÀI", MeetTheme.GOOGLE_GREEN_BRIGHT, MeetTheme.GOOGLE_GREEN_BRIGHT, new Color(0x1e, 0x8e, 0x3e, 40));
        clientTop.add(clientRoleBadge);

        JPanel clientCenter = new JPanel();
        clientCenter.setLayout(new BoxLayout(clientCenter, BoxLayout.Y_AXIS));
        clientCenter.setOpaque(false);

        JLabel lblClientTitle = new JLabel("Tham gia nộp bài (Client)");
        lblClientTitle.setFont(MeetTheme.fontBold(16));
        lblClientTitle.setForeground(MeetTheme.TEXT_PRIMARY);

        JLabel lblClientDesc = new JLabel("<html>Tham gia phòng nộp bài bằng địa chỉ IP máy chủ. Nhập thông tin sinh viên, chọn file bài làm và xác thực toàn vẹn bằng mã băm SHA-256 theo thời gian thực.</html>");
        lblClientDesc.setFont(MeetTheme.fontPlain(12));
        lblClientDesc.setForeground(MeetTheme.TEXT_SECONDARY);

        clientCenter.add(Box.createVerticalStrut(8));
        clientCenter.add(lblClientTitle);
        clientCenter.add(Box.createVerticalStrut(8));
        clientCenter.add(lblClientDesc);

        MeetButton btnClient = new MeetButton("Tham gia với vai trò Client", MeetButton.Variant.SUCCESS);
        btnClient.setPreferredSize(new Dimension(280, 42));

        cardClient.add(clientTop, BorderLayout.NORTH);
        cardClient.add(clientCenter, BorderLayout.CENTER);
        cardClient.add(btnClient, BorderLayout.SOUTH);

        cardsGrid.add(cardServer);
        cardsGrid.add(cardClient);
        centerPanel.add(cardsGrid, BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // 3. BOTTOM FOOTER
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        String timeStr = new SimpleDateFormat("HH:mm - EEEE, dd/MM/yyyy").format(new Date());
        JLabel lblTime = new JLabel("🕒 " + timeStr + "  •  Google Meet UI Theme");
        lblTime.setFont(MeetTheme.fontPlain(12));
        lblTime.setForeground(MeetTheme.TEXT_MUTED);

        JLabel lblCopyright = new JLabel("Đồ án Lập trình Mạng • Socket TCP + SQLite");
        lblCopyright.setFont(MeetTheme.fontPlain(12));
        lblCopyright.setForeground(MeetTheme.TEXT_MUTED);

        footerPanel.add(lblTime, BorderLayout.WEST);
        footerPanel.add(lblCopyright, BorderLayout.EAST);

        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        // EVENTS
        btnServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ServerGUI().setVisible(true);
            }
        });

        btnClient.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ClientGUI().setVisible(true);
            }
        });

        frame.setContentPane(mainPanel);
        frame.setVisible(true);
    }
}
