package damh.giaodien;

import damh.SerVer.ServerCore;
import damh.SubmissionModel;
import damh.db.DatabaseManager;
import damh.db.DatabaseMigration;
import damh.db.StudentDAO;
import damh.db.StudentModel;
import damh.db.SubmissionDAO;
import damh.giaodien.MeetTheme.*;
import damh.util.DeadlineManager;
import damh.util.SecurityUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ServerGUI extends JFrame implements ServerCore.ServerListener {

    // --- Header & Kết nối ---
    private MeetTextField txtPort;
    private MeetBadge badgeIp;
    private MeetBadge badgeServerStatus;
    private MeetBadge badgeDbStatus;
    private MeetButton btnToggleServer;
    private MeetButton btnOpenFolder;
    private MeetButton btnExportCsv;

    // --- Thẻ thống kê nhanh trên đầu ---
    private JLabel lblStatTotal;
    private JLabel lblStatGraded;
    private JLabel lblStatUngraded;
    private JLabel lblStatAvgScore;

    // --- Tab 1: Danh sách Bài nộp & Chấm điểm ---
    private JTable tableSubmissions;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private MeetTextField txtSearch;
    private JComboBox<String> cboFilterStatus;
    private MeetButton btnGradeSelected;
    private MeetButton btnOpenFileSelected;
    private MeetButton btnReloadSubmissions;
    private final List<SubmissionModel> submissionList = new ArrayList<>();

    // --- Tab 2: Quản lý Sinh viên & Điểm trung bình ---
    private JTable tableStudents;
    private DefaultTableModel studentTableModel;
    private TableRowSorter<DefaultTableModel> studentRowSorter;
    private MeetTextField txtStudentSearch;
    private MeetButton btnReloadStudents;
    private final List<StudentModel> studentList = new ArrayList<>();

    // --- Tab 3: Cấu hình Ca thi & Hạn nộp ---
    private JCheckBox chkEnableDeadline;
    private JSpinner spnDeadline;
    private JComboBox<String> cboDeadlinePolicy;
    private MeetButton btnSaveDeadline;
    private MeetButton btnToggleGate;
    private JLabel lblDeadlineCountdown;
    private javax.swing.Timer countdownTimer;

    // --- Tab 4: Thống kê & Phổ điểm ---
    private JLabel lblStatExCount;
    private JLabel lblStatGoodCount;
    private JLabel lblStatAvgCount;
    private JLabel lblStatWeakCount;
    private MeetProgressBar pbEx;
    private MeetProgressBar pbGood;
    private MeetProgressBar pbAvg;
    private MeetProgressBar pbWeak;
    private JLabel lblStatHighest;
    private JLabel lblStatLowest;

    // --- Tab 5: Nhật ký hoạt động ---
    private JTextArea txtLog;
    private MeetButton btnClearLog;

    // Status bar dưới cùng
    private JLabel lblStatusBarLeft;
    private JLabel lblStatusBarRight;

    private ServerCore serverCore;

    public ServerGUI() {
        setTitle("Google Meet — Bàn Làm Việc Giảng Viên & Chấm Điểm Bài Tập (Server)");
        setSize(1180, 800);
        setMinimumSize(new Dimension(1020, 680));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(MeetTheme.BG_DARK);

        initUI();
        initDatabaseAndLoadData();
    }

    private void initUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBackground(MeetTheme.BG_DARK);
        mainPanel.setBorder(new EmptyBorder(12, 16, 12, 16));

        // =========================================================================
        // 1. TOP HEADER: Logo Google Meet, Badges & Server Controls
        // =========================================================================
        MeetCard topCard = new MeetCard(14);
        topCard.setLayout(new BorderLayout(12, 8));
        topCard.setBorder(new EmptyBorder(10, 16, 10, 16));

        // Trái: Logo Google Meet + Title
        MeetLogoHeader logoHeader = new MeetLogoHeader("Google Meet — Bàn Làm Việc Giảng Viên", "Hệ Thống Thu Bài Tập, Tổ Chức Ca Thi & Chấm Điểm Qua Mạng LAN");
        topCard.add(logoHeader, BorderLayout.WEST);

        // Phải: IP Badge, Status Badge, Port Input, Nút Toggle Server, Nút Mở thư mục, Nút Xuất CSV
        JPanel topRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        topRightPanel.setOpaque(false);

        String localIp = ServerCore.getLocalIPAddress();
        badgeIp = new MeetBadge("LAN IP: " + localIp, MeetTheme.GOOGLE_BLUE_LIGHT, MeetTheme.TEXT_PRIMARY, MeetTheme.INPUT_BG);
        badgeServerStatus = new MeetBadge("Đã dừng", MeetTheme.GOOGLE_RED, MeetTheme.TEXT_PRIMARY, MeetTheme.INPUT_BG);
        badgeDbStatus = new MeetBadge("CSDL: Kết nối...", MeetTheme.GOOGLE_YELLOW, MeetTheme.TEXT_PRIMARY, MeetTheme.INPUT_BG);

        topRightPanel.add(badgeIp);
        topRightPanel.add(badgeServerStatus);
        topRightPanel.add(badgeDbStatus);

        topRightPanel.add(Box.createHorizontalStrut(4));
        JLabel lblPort = new JLabel("Port:");
        lblPort.setForeground(MeetTheme.TEXT_SECONDARY);
        lblPort.setFont(MeetTheme.fontBold(12));
        topRightPanel.add(lblPort);

        txtPort = new MeetTextField("12345", 5);
        txtPort.setFont(MeetTheme.fontBold(12));
        txtPort.setPreferredSize(new Dimension(65, 32));
        topRightPanel.add(txtPort);

        btnToggleServer = new MeetButton("Khởi động Server", MeetButton.Variant.SUCCESS);
        btnToggleServer.setCornerRadius(18);
        btnToggleServer.setPreferredSize(new Dimension(145, 34));
        topRightPanel.add(btnToggleServer);

        btnOpenFolder = new MeetButton("Thư mục bài", MeetButton.Variant.NEUTRAL);
        btnOpenFolder.setCornerRadius(18);
        btnOpenFolder.setPreferredSize(new Dimension(120, 34));
        btnOpenFolder.setToolTipText("Mở thư mục lưu trữ bài nộp của sinh viên");
        topRightPanel.add(btnOpenFolder);

        btnExportCsv = new MeetButton("Xuất bảng điểm", MeetButton.Variant.PRIMARY);
        btnExportCsv.setCornerRadius(18);
        btnExportCsv.setPreferredSize(new Dimension(140, 34));
        btnExportCsv.setToolTipText("Xuất toàn bộ danh sách bài nộp và điểm số ra file CSV (Excel UTF-8)");
        topRightPanel.add(btnExportCsv);

        topCard.add(topRightPanel, BorderLayout.EAST);

        // =========================================================================
        // 2. STATS CARDS ROW: Thẻ chỉ số tổng quan nhanh
        // =========================================================================
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 12, 0));
        statsRow.setOpaque(false);
        statsRow.setPreferredSize(new Dimension(900, 68));

        statsRow.add(createMiniStatCard("Tổng bài đã nhận", "0", MeetTheme.GOOGLE_BLUE_LIGHT, c -> lblStatTotal = c));
        statsRow.add(createMiniStatCard("Đã chấm điểm", "0", MeetTheme.GOOGLE_GREEN_BRIGHT, c -> lblStatGraded = c));
        statsRow.add(createMiniStatCard("Chưa chấm điểm", "0", MeetTheme.GOOGLE_AMBER, c -> lblStatUngraded = c));
        statsRow.add(createMiniStatCard("Điểm trung bình lớp", "0.0 / 10", Color.WHITE, c -> lblStatAvgScore = c));

        JPanel northContainer = new JPanel();
        northContainer.setLayout(new BoxLayout(northContainer, BoxLayout.Y_AXIS));
        northContainer.setOpaque(false);
        northContainer.add(topCard);
        northContainer.add(Box.createVerticalStrut(8));
        northContainer.add(statsRow);

        mainPanel.add(northContainer, BorderLayout.NORTH);

        // =========================================================================
        // 3. CENTER: TABBED PANE (5 TABS HIỆN ĐẠI)
        // =========================================================================
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(MeetTheme.fontBold(13));
        tabbedPane.setBackground(MeetTheme.BG_SURFACE);
        tabbedPane.setForeground(MeetTheme.TEXT_PRIMARY);

        // --- TAB 1: DANH SÁCH BÀI NỘP & CHẤM ĐIỂM ---
        JPanel tabSubmissions = createSubmissionsTab();
        tabbedPane.addTab("  Bài Nộp & Chấm Điểm  ", tabSubmissions);

        // --- TAB 2: QUẢN LÝ SINH VIÊN & BẢNG ĐIỂM ---
        JPanel tabStudents = createStudentsTab();
        tabbedPane.addTab("  Sinh Viên & Điểm TB  ", tabStudents);

        // --- TAB 3: THỐNG KÊ & PHỔ ĐIỂM ---
        JPanel tabStats = createStatsTab();
        tabbedPane.addTab("  Phổ Điểm & Thống Kê  ", tabStats);

        // --- TAB 4: CẤU HÌNH HẠN NỘP BÀI & CA THI ---
        JPanel tabDeadline = createDeadlineTab();
        tabbedPane.addTab("  Hạn Nộp & Ca Thi  ", tabDeadline);

        // --- TAB 5: NHẬT KÝ HỆ THỐNG ---
        JPanel tabLogs = createLogsTab();
        tabbedPane.addTab("  Nhật Ký Hoạt Động  ", tabLogs);

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // =========================================================================
        // 4. BOTTOM STATUS BAR
        // =========================================================================
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setOpaque(false);
        statusBar.setBorder(new EmptyBorder(4, 4, 0, 4));

        lblStatusBarLeft = new JLabel("Sẵn sàng phục vụ giảng viên và sinh viên");
        lblStatusBarLeft.setFont(MeetTheme.fontPlain(12));
        lblStatusBarLeft.setForeground(MeetTheme.TEXT_SECONDARY);

        lblStatusBarRight = new JLabel("Đồ án Lập trình Mạng • Socket TCP Đa luồng • SQLite Migration • Google Meet UI");
        lblStatusBarRight.setFont(MeetTheme.fontPlain(12));
        lblStatusBarRight.setForeground(MeetTheme.TEXT_MUTED);

        statusBar.add(lblStatusBarLeft, BorderLayout.WEST);
        statusBar.add(lblStatusBarRight, BorderLayout.EAST);
        mainPanel.add(statusBar, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        initEvents();
    }

    private MeetCard createMiniStatCard(String title, String initialValue, Color valueColor, java.util.function.Consumer<JLabel> labelConsumer) {
        MeetCard card = new MeetCard(12);
        card.setLayout(new BorderLayout(8, 2));
        card.setBorder(new EmptyBorder(8, 14, 8, 14));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(MeetTheme.fontPlain(11));
        lblTitle.setForeground(MeetTheme.TEXT_SECONDARY);

        JLabel lblVal = new JLabel(initialValue);
        lblVal.setFont(MeetTheme.fontBold(20));
        lblVal.setForeground(valueColor);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblVal, BorderLayout.CENTER);

        labelConsumer.accept(lblVal);
        return card;
    }

    // =========================================================================
    // TAB 1: DANH SÁCH BÀI NỘP & CHẤM ĐIỂM
    // =========================================================================
    private JPanel createSubmissionsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(MeetTheme.BG_DARK);
        panel.setBorder(new EmptyBorder(8, 4, 4, 4));

        // Toolbar trên cùng: Search, Filter, Grade Button, Open File Button, Refresh
        MeetCard toolbarCard = new MeetCard(12);
        toolbarCard.setLayout(new BorderLayout(10, 0));
        toolbarCard.setBorder(new EmptyBorder(8, 12, 8, 12));

        JPanel leftTools = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftTools.setOpaque(false);

        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setForeground(MeetTheme.TEXT_SECONDARY);
        lblSearch.setFont(MeetTheme.fontBold(12));
        leftTools.add(lblSearch);

        txtSearch = new MeetTextField("Nhập MSSV, Tên SV, Tên file...", 20);
        txtSearch.setPreferredSize(new Dimension(240, 32));
        leftTools.add(txtSearch);

        JLabel lblFilter = new JLabel("Lọc trạng thái:");
        lblFilter.setForeground(MeetTheme.TEXT_SECONDARY);
        lblFilter.setFont(MeetTheme.fontBold(12));
        leftTools.add(lblFilter);

        cboFilterStatus = new JComboBox<>(new String[]{"Tất cả bài nộp", "Chưa chấm điểm", "Đã chấm điểm", "Nộp muộn", "Đúng hạn"});
        cboFilterStatus.setPreferredSize(new Dimension(145, 32));
        MeetTheme.styleComboBox(cboFilterStatus);
        leftTools.add(cboFilterStatus);

        toolbarCard.add(leftTools, BorderLayout.WEST);

        JPanel rightTools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightTools.setOpaque(false);

        btnGradeSelected = new MeetButton("Chấm điểm bài đã chọn", MeetButton.Variant.SUCCESS);
        btnGradeSelected.setCornerRadius(16);
        btnGradeSelected.setPreferredSize(new Dimension(185, 32));
        btnGradeSelected.setToolTipText("Mở hộp thoại chấm điểm & nhận xét cho bài nộp được chọn");
        rightTools.add(btnGradeSelected);

        btnOpenFileSelected = new MeetButton("Mở file", MeetButton.Variant.PRIMARY);
        btnOpenFileSelected.setCornerRadius(16);
        btnOpenFileSelected.setPreferredSize(new Dimension(100, 32));
        btnOpenFileSelected.setToolTipText("Mở file bài làm trực tiếp");
        rightTools.add(btnOpenFileSelected);

        btnReloadSubmissions = new MeetButton("Làm mới", MeetButton.Variant.NEUTRAL);
        btnReloadSubmissions.setCornerRadius(16);
        btnReloadSubmissions.setPreferredSize(new Dimension(100, 32));
        rightTools.add(btnReloadSubmissions);

        toolbarCard.add(rightTools, BorderLayout.EAST);
        panel.add(toolbarCard, BorderLayout.NORTH);

        // Bảng dữ liệu Bài nộp
        String[] columns = {
                "STT", "Thời gian nộp", "MSSV", "Họ và tên", "Lớp",
                "Tên file bài làm", "Loại", "Dung lượng", "Mã SHA-256",
                "Trạng thái", "Điểm số", "Đánh giá / Nhận xét"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableSubmissions = new JTable(tableModel);
        MeetTheme.styleTable(tableSubmissions);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        tableSubmissions.getColumnModel().getColumn(0).setPreferredWidth(45);
        tableSubmissions.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tableSubmissions.getColumnModel().getColumn(1).setPreferredWidth(125);
        tableSubmissions.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        tableSubmissions.getColumnModel().getColumn(2).setPreferredWidth(85);
        tableSubmissions.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        tableSubmissions.getColumnModel().getColumn(3).setPreferredWidth(140);
        tableSubmissions.getColumnModel().getColumn(4).setPreferredWidth(80);
        tableSubmissions.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        tableSubmissions.getColumnModel().getColumn(5).setPreferredWidth(160);
        tableSubmissions.getColumnModel().getColumn(6).setPreferredWidth(55);
        tableSubmissions.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        tableSubmissions.getColumnModel().getColumn(7).setPreferredWidth(75);
        tableSubmissions.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);
        tableSubmissions.getColumnModel().getColumn(8).setPreferredWidth(90);
        tableSubmissions.getColumnModel().getColumn(8).setCellRenderer(centerRenderer);

        // Renderer cho Trạng thái
        tableSubmissions.getColumnModel().getColumn(9).setPreferredWidth(110);
        tableSubmissions.getColumnModel().getColumn(9).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                if (value != null) {
                    String str = value.toString();
                    if (str.startsWith("Đúng hạn")) {
                        setForeground(MeetTheme.GOOGLE_GREEN_BRIGHT);
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else if (str.startsWith("Nộp muộn") || str.startsWith("Nộp trễ")) {
                        setForeground(MeetTheme.GOOGLE_RED);
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else {
                        setForeground(MeetTheme.GOOGLE_BLUE_LIGHT);
                    }
                }
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? MeetTheme.CARD_BG : MeetTheme.TABLE_ROW_ALT);
                }
                return c;
            }
        });

        // Renderer cho Điểm số (Nổi bật điểm số)
        tableSubmissions.getColumnModel().getColumn(10).setPreferredWidth(80);
        tableSubmissions.getColumnModel().getColumn(10).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                if (value != null) {
                    String str = value.toString();
                    if ("Chưa chấm".equals(str)) {
                        setForeground(MeetTheme.TEXT_MUTED);
                        setFont(getFont().deriveFont(Font.ITALIC));
                    } else {
                        try {
                            double s = Double.parseDouble(str);
                            if (s >= 8.5) {
                                setForeground(new Color(0x34, 0xa8, 0x53)); // Xanh lá xuất sắc
                            } else if (s >= 7.0) {
                                setForeground(new Color(0x8a, 0xb4, 0xf8)); // Xanh dương khá
                            } else if (s >= 5.0) {
                                setForeground(new Color(0xfb, 0xbc, 0x04)); // Vàng trung bình
                            } else {
                                setForeground(new Color(0xea, 0x43, 0x35)); // Đỏ yếu
                            }
                        } catch (Exception ex) {
                            setForeground(MeetTheme.GOOGLE_BLUE_LIGHT);
                        }
                        setFont(getFont().deriveFont(Font.BOLD, 13f));
                    }
                }
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? MeetTheme.CARD_BG : MeetTheme.TABLE_ROW_ALT);
                }
                return c;
            }
        });

        // Renderer cho Nhận xét
        tableSubmissions.getColumnModel().getColumn(11).setPreferredWidth(220);
        tableSubmissions.getColumnModel().getColumn(11).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value != null && !value.toString().isEmpty()) {
                    setForeground(MeetTheme.TEXT_PRIMARY);
                    setToolTipText(value.toString());
                } else {
                    setForeground(MeetTheme.TEXT_MUTED);
                    setToolTipText(null);
                }
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? MeetTheme.CARD_BG : MeetTheme.TABLE_ROW_ALT);
                }
                return c;
            }
        });

        rowSorter = new TableRowSorter<>(tableModel);
        tableSubmissions.setRowSorter(rowSorter);

        JScrollPane scrollPane = new JScrollPane(tableSubmissions);
        MeetTheme.styleScrollPane(scrollPane);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // =========================================================================
    // TAB 2: QUẢN LÝ SINH VIÊN & BẢNG ĐIỂM
    // =========================================================================
    private JPanel createStudentsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(MeetTheme.BG_DARK);
        panel.setBorder(new EmptyBorder(8, 4, 4, 4));

        MeetCard toolbarCard = new MeetCard(12);
        toolbarCard.setLayout(new BorderLayout(10, 0));
        toolbarCard.setBorder(new EmptyBorder(8, 12, 8, 12));

        JPanel leftTools = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftTools.setOpaque(false);

        JLabel lblSearch = new JLabel("Tìm sinh viên:");
        lblSearch.setForeground(MeetTheme.TEXT_SECONDARY);
        lblSearch.setFont(MeetTheme.fontBold(12));
        leftTools.add(lblSearch);

        txtStudentSearch = new MeetTextField("Nhập MSSV, Họ tên hoặc Lớp...", 22);
        txtStudentSearch.setPreferredSize(new Dimension(260, 32));
        leftTools.add(txtStudentSearch);
        toolbarCard.add(leftTools, BorderLayout.WEST);

        JPanel rightTools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightTools.setOpaque(false);
        btnReloadStudents = new MeetButton("Làm mới danh sách", MeetButton.Variant.NEUTRAL);
        btnReloadStudents.setCornerRadius(16);
        btnReloadStudents.setPreferredSize(new Dimension(170, 32));
        rightTools.add(btnReloadStudents);
        toolbarCard.add(rightTools, BorderLayout.EAST);

        panel.add(toolbarCard, BorderLayout.NORTH);

        String[] studentCols = {"STT", "MSSV", "Họ và tên", "Lớp", "Số bài đã nộp", "Số bài đã chấm", "Điểm trung bình", "Lần nộp gần nhất"};
        studentTableModel = new DefaultTableModel(studentCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableStudents = new JTable(studentTableModel);
        MeetTheme.styleTable(tableStudents);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        tableStudents.getColumnModel().getColumn(0).setPreferredWidth(45);
        tableStudents.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tableStudents.getColumnModel().getColumn(1).setPreferredWidth(95);
        tableStudents.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        tableStudents.getColumnModel().getColumn(2).setPreferredWidth(180);
        tableStudents.getColumnModel().getColumn(3).setPreferredWidth(110);
        tableStudents.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        tableStudents.getColumnModel().getColumn(4).setPreferredWidth(95);
        tableStudents.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        tableStudents.getColumnModel().getColumn(5).setPreferredWidth(100);
        tableStudents.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

        // Renderer cho Điểm TB
        tableStudents.getColumnModel().getColumn(6).setPreferredWidth(110);
        tableStudents.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                if (value != null) {
                    String str = value.toString();
                    if ("Chưa có".equals(str)) {
                        setForeground(MeetTheme.TEXT_MUTED);
                    } else {
                        try {
                            double s = Double.parseDouble(str);
                            if (s >= 8.5) setForeground(MeetTheme.GOOGLE_GREEN_BRIGHT);
                            else if (s >= 7.0) setForeground(MeetTheme.GOOGLE_BLUE_LIGHT);
                            else if (s >= 5.0) setForeground(MeetTheme.GOOGLE_YELLOW);
                            else setForeground(MeetTheme.GOOGLE_RED);
                        } catch (Exception ex) {
                            setForeground(MeetTheme.TEXT_PRIMARY);
                        }
                        setFont(getFont().deriveFont(Font.BOLD));
                    }
                }
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? MeetTheme.CARD_BG : MeetTheme.TABLE_ROW_ALT);
                }
                return c;
            }
        });

        tableStudents.getColumnModel().getColumn(7).setPreferredWidth(150);
        tableStudents.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);

        studentRowSorter = new TableRowSorter<>(studentTableModel);
        tableStudents.setRowSorter(studentRowSorter);

        JScrollPane scrollStudents = new JScrollPane(tableStudents);
        MeetTheme.styleScrollPane(scrollStudents);
        panel.add(scrollStudents, BorderLayout.CENTER);

        return panel;
    }

    // =========================================================================
    // TAB 3: THỐNG KÊ & PHỔ ĐIỂM
    // =========================================================================
    private JPanel createStatsTab() {
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setBackground(MeetTheme.BG_DARK);
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));

        // Container chính chứa các thẻ phân tích
        JPanel cardsGrid = new JPanel(new GridLayout(1, 2, 14, 0));
        cardsGrid.setOpaque(false);

        // THẺ 1: Phổ điểm & Phân loại học lực
        MeetCard distCard = new MeetCard(14);
        distCard.setLayout(new BorderLayout(10, 10));
        distCard.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel lblDistTitle = new JLabel("Phổ Điểm & Phân Loại Kết Quả Bài Tập");
        lblDistTitle.setFont(MeetTheme.fontBold(15));
        lblDistTitle.setForeground(MeetTheme.TEXT_PRIMARY);
        distCard.add(lblDistTitle, BorderLayout.NORTH);

        JPanel distBody = new JPanel();
        distBody.setLayout(new BoxLayout(distBody, BoxLayout.Y_AXIS));
        distBody.setOpaque(false);

        // Xuất sắc & Giỏi (8.5 - 10.0)
        distBody.add(createProgressRow("Giỏi & Xuất sắc (8.5 - 10.0):", lblStatExCount = new JLabel("0 bài (0%)"), pbEx = new MeetProgressBar()));
        distBody.add(Box.createVerticalStrut(14));

        // Khá (7.0 - 8.4)
        distBody.add(createProgressRow("Khá (7.0 - 8.4):", lblStatGoodCount = new JLabel("0 bài (0%)"), pbGood = new MeetProgressBar()));
        distBody.add(Box.createVerticalStrut(14));

        // Trung bình (5.0 - 6.9)
        distBody.add(createProgressRow("Trung bình (5.0 - 6.9):", lblStatAvgCount = new JLabel("0 bài (0%)"), pbAvg = new MeetProgressBar()));
        distBody.add(Box.createVerticalStrut(14));

        // Cần cố gắng (< 5.0)
        distBody.add(createProgressRow("Cần cố gắng (< 5.0):", lblStatWeakCount = new JLabel("0 bài (0%)"), pbWeak = new MeetProgressBar()));

        distCard.add(distBody, BorderLayout.CENTER);
        cardsGrid.add(distCard);

        // THẺ 2: Tóm tắt điểm số & Khuyến nghị
        MeetCard summaryCard = new MeetCard(14);
        summaryCard.setLayout(new BorderLayout(10, 10));
        summaryCard.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel lblSumTitle = new JLabel("Chỉ Số Đánh Giá Toàn Lớp");
        lblSumTitle.setFont(MeetTheme.fontBold(15));
        lblSumTitle.setForeground(MeetTheme.TEXT_PRIMARY);
        summaryCard.add(lblSumTitle, BorderLayout.NORTH);

        JPanel sumBody = new JPanel();
        sumBody.setLayout(new BoxLayout(sumBody, BoxLayout.Y_AXIS));
        sumBody.setOpaque(false);

        lblStatHighest = new JLabel("Điểm cao nhất: —");
        lblStatHighest.setFont(MeetTheme.fontBold(14));
        lblStatHighest.setForeground(MeetTheme.GOOGLE_GREEN_BRIGHT);

        lblStatLowest = new JLabel("Điểm thấp nhất: —");
        lblStatLowest.setFont(MeetTheme.fontBold(14));
        lblStatLowest.setForeground(MeetTheme.GOOGLE_RED);

        JLabel lblTip1 = new JLabel(" Mẹo chấm bài nhanh:");
        lblTip1.setFont(MeetTheme.fontBold(13));
        lblTip1.setForeground(MeetTheme.GOOGLE_BLUE_LIGHT);

        JLabel lblTip2 = new JLabel(" Bạn có thể click đúp vào bất kỳ bài nộp nào trong Tab 'Bài Nộp & Chấm Điểm' để mở cửa sổ chấm điểm.");
        lblTip2.setFont(MeetTheme.fontPlain(12));
        lblTip2.setForeground(MeetTheme.TEXT_SECONDARY);

        JLabel lblTip3 = new JLabel(" Trong cửa sổ chấm điểm, bấm 'Lưu & Chấm Bài Kế Tiếp' để chấm liên tục cả lớp chỉ trong vài phút.");
        lblTip3.setFont(MeetTheme.fontPlain(12));
        lblTip3.setForeground(MeetTheme.TEXT_SECONDARY);

        JLabel lblTip4 = new JLabel(" Xuất bảng điểm Excel (CSV UTF-8) bất cứ lúc nào để báo cáo điểm tổng kết cho khoa/bộ môn.");
        lblTip4.setFont(MeetTheme.fontPlain(12));
        lblTip4.setForeground(MeetTheme.TEXT_SECONDARY);

        sumBody.add(lblStatHighest);
        sumBody.add(Box.createVerticalStrut(10));
        sumBody.add(lblStatLowest);
        sumBody.add(Box.createVerticalStrut(20));
        sumBody.add(lblTip1);
        sumBody.add(Box.createVerticalStrut(6));
        sumBody.add(lblTip2);
        sumBody.add(Box.createVerticalStrut(4));
        sumBody.add(lblTip3);
        sumBody.add(Box.createVerticalStrut(4));
        sumBody.add(lblTip4);
        sumBody.add(Box.createVerticalGlue());

        summaryCard.add(sumBody, BorderLayout.CENTER);
        cardsGrid.add(summaryCard);

        panel.add(cardsGrid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createProgressRow(String labelText, JLabel countLabel, MeetProgressBar bar) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(MeetTheme.fontBold(12));
        lbl.setForeground(MeetTheme.TEXT_PRIMARY);

        countLabel.setFont(MeetTheme.fontBold(12));
        countLabel.setForeground(MeetTheme.GOOGLE_BLUE_LIGHT);

        header.add(lbl, BorderLayout.WEST);
        header.add(countLabel, BorderLayout.EAST);

        bar.setPreferredSize(new Dimension(300, 18));

        row.add(header);
        row.add(Box.createVerticalStrut(4));
        row.add(bar);
        return row;
    }

    // =========================================================================
    // TAB 4: CẤU HÌNH HẠN NỘP BÀI & CA THI
    // =========================================================================
    private JPanel createDeadlineTab() {
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setBackground(MeetTheme.BG_DARK);
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));

        MeetCard card = new MeetCard(14);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(18, 22, 18, 22));

        JLabel lblTitle = new JLabel("Quản Lý Hạn Nộp Bài & Ca Thi (Deadline & Gate Control)");
        lblTitle.setFont(MeetTheme.fontBold(16));
        lblTitle.setForeground(MeetTheme.TEXT_PRIMARY);
        card.add(lblTitle);
        card.add(Box.createVerticalStrut(14));

        // Hàng 1: Bật hạn nộp & Picker
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        row1.setOpaque(false);

        chkEnableDeadline = new JCheckBox("Kích hoạt hạn nộp bài");
        chkEnableDeadline.setFont(MeetTheme.fontBold(13));
        chkEnableDeadline.setForeground(MeetTheme.GOOGLE_BLUE_LIGHT);
        chkEnableDeadline.setOpaque(false);

        spnDeadline = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.MINUTE));
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spnDeadline, "dd/MM/yyyy HH:mm");
        spnDeadline.setEditor(dateEditor);
        spnDeadline.setPreferredSize(new Dimension(150, 32));

        row1.add(chkEnableDeadline);
        row1.add(Box.createHorizontalStrut(8));
        JLabel lblH = new JLabel("Hạn chót:");
        lblH.setForeground(MeetTheme.TEXT_SECONDARY);
        lblH.setFont(MeetTheme.fontBold(12));
        row1.add(lblH);
        row1.add(spnDeadline);

        // Nút chọn nhanh
        row1.add(Box.createHorizontalStrut(12));
        JLabel lblQuick = new JLabel("Đặt nhanh:");
        lblQuick.setForeground(MeetTheme.TEXT_MUTED);
        lblQuick.setFont(MeetTheme.fontPlain(12));
        row1.add(lblQuick);

        MeetButton btnQ15 = new MeetButton("+15p", MeetButton.Variant.NEUTRAL);
        MeetButton btnQ30 = new MeetButton("+30p", MeetButton.Variant.NEUTRAL);
        MeetButton btnQ60 = new MeetButton("+1h", MeetButton.Variant.NEUTRAL);
        MeetButton btnQToday = new MeetButton("23:59 Hôm nay", MeetButton.Variant.NEUTRAL);
        btnQ15.setCornerRadius(14);
        btnQ30.setCornerRadius(14);
        btnQ60.setCornerRadius(14);
        btnQToday.setCornerRadius(14);
        btnQ15.setPreferredSize(new Dimension(80, 28));
        btnQ30.setPreferredSize(new Dimension(80, 28));
        btnQ60.setPreferredSize(new Dimension(80, 28));
        btnQToday.setPreferredSize(new Dimension(150, 28));

        btnQ15.addActionListener(e -> setQuickDeadline(15, false));
        btnQ30.addActionListener(e -> setQuickDeadline(30, false));
        btnQ60.addActionListener(e -> setQuickDeadline(60, false));
        btnQToday.addActionListener(e -> setQuickDeadline(0, true));

        row1.add(btnQ15);
        row1.add(btnQ30);
        row1.add(btnQ60);
        row1.add(btnQToday);
        card.add(row1);
        card.add(Box.createVerticalStrut(10));

        // Hàng 2: Chính sách xử lý nộp muộn
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        row2.setOpaque(false);

        JLabel lblPolicy = new JLabel("Khi quá hạn chót:");
        lblPolicy.setForeground(MeetTheme.TEXT_SECONDARY);
        lblPolicy.setFont(MeetTheme.fontBold(12));
        row2.add(lblPolicy);

        cboDeadlinePolicy = new JComboBox<>(new String[]{
                "Cho phép nộp muộn (Gắn nhãn cảnh báo đỏ)",
                "Khóa cổng khi quá hạn (Từ chối nhận bài)"
        });
        cboDeadlinePolicy.setPreferredSize(new Dimension(280, 32));
        MeetTheme.styleComboBox(cboDeadlinePolicy);
        row2.add(cboDeadlinePolicy);

        btnSaveDeadline = new MeetButton("Áp Dụng Cấu Hình", MeetButton.Variant.PRIMARY);
        btnSaveDeadline.setCornerRadius(16);
        btnSaveDeadline.setPreferredSize(new Dimension(150, 32));
        row2.add(btnSaveDeadline);

        btnToggleGate = new MeetButton("Khóa nhận bài ngay", MeetButton.Variant.DANGER);
        btnToggleGate.setCornerRadius(16);
        btnToggleGate.setPreferredSize(new Dimension(165, 32));
        row2.add(btnToggleGate);

        card.add(row2);
        card.add(Box.createVerticalStrut(14));

        // Hàng 3: Countdown trực quan
        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        row3.setOpaque(false);

        lblDeadlineCountdown = new JLabel("Trạng thái: Đang tải cấu hình");
        lblDeadlineCountdown.setFont(MeetTheme.fontBold(14));
        lblDeadlineCountdown.setForeground(MeetTheme.GOOGLE_GREEN_BRIGHT);
        row3.add(lblDeadlineCountdown);
        card.add(row3);

        card.add(Box.createVerticalGlue());
        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    // =========================================================================
    // TAB 5: NHẬT KÝ HỆ THỐNG
    // =========================================================================
    private JPanel createLogsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(MeetTheme.BG_DARK);
        panel.setBorder(new EmptyBorder(8, 4, 4, 4));

        txtLog = new JTextArea();
        txtLog.setEditable(false);
        txtLog.setFont(MeetTheme.fontMono(12));
        txtLog.setBackground(MeetTheme.INPUT_BG);
        txtLog.setForeground(MeetTheme.TEXT_PRIMARY);
        txtLog.setCaretColor(MeetTheme.GOOGLE_BLUE_LIGHT);
        txtLog.setBorder(new EmptyBorder(8, 10, 8, 10));

        JScrollPane scrollLog = new JScrollPane(txtLog);
        MeetTheme.styleScrollPane(scrollLog);
        panel.add(scrollLog, BorderLayout.CENTER);

        JPanel logActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        logActions.setOpaque(false);

        btnClearLog = new MeetButton("Xóa nhật ký", MeetButton.Variant.NEUTRAL);
        btnClearLog.setCornerRadius(14);
        btnClearLog.setPreferredSize(new Dimension(110, 30));
        logActions.add(btnClearLog);
        panel.add(logActions, BorderLayout.SOUTH);

        return panel;
    }

    // =========================================================================
    // SỰ KIỆN & ĐIỀU KHIỂN
    // =========================================================================
    private void initEvents() {
        // Khởi động / Dừng Server
        btnToggleServer.addActionListener(e -> {
            if (serverCore != null && serverCore.isRunning()) {
                serverCore.stopServer();
            } else {
                startServerCore();
            }
        });

        // Mở thư mục lưu bài
        btnOpenFolder.addActionListener(e -> {
            File dir = new File(ServerCore.STORAGE_DIR);
            if (!dir.exists()) dir.mkdirs();
            try {
                Desktop.getDesktop().open(dir);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Không thể mở thư mục: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Xuất file CSV
        btnExportCsv.addActionListener(e -> exportToCsv());

        // Làm mới dữ liệu
        btnReloadSubmissions.addActionListener(e -> {
            loadSubmissionsFromDatabase();
            loadStudentsFromDatabase();
        });
        btnReloadStudents.addActionListener(e -> loadStudentsFromDatabase());
        btnClearLog.addActionListener(e -> txtLog.setText(""));

        // Chấm điểm bài đã chọn
        btnGradeSelected.addActionListener(e -> openGradingForSelectedRow());

        // Mở file bài đã chọn
        btnOpenFileSelected.addActionListener(e -> openFileForSelectedRow());

        // Double-click vào bảng bài nộp -> Mở hộp thoại chấm điểm ngay lập tức!
        tableSubmissions.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    openGradingForSelectedRow();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showTableContextMenu(e);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showTableContextMenu(e);
                }
            }
        });

        // Tìm kiếm và Lọc bài nộp
        javax.swing.event.DocumentListener subDocListener = new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applySubmissionFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applySubmissionFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applySubmissionFilter(); }
        };
        txtSearch.getDocument().addDocumentListener(subDocListener);
        cboFilterStatus.addActionListener(e -> applySubmissionFilter());

        // Tìm kiếm sinh viên
        txtStudentSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterStudents(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterStudents(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterStudents(); }
            private void filterStudents() {
                String text = txtStudentSearch.getText().trim();
                if (text.isEmpty()) {
                    studentRowSorter.setRowFilter(null);
                } else {
                    studentRowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 1, 2, 3));
                }
            }
        });

        // Áp dụng hạn nộp bài
        btnSaveDeadline.addActionListener(e -> {
            DeadlineManager dm = DeadlineManager.getInstance();
            dm.setEnabled(chkEnableDeadline.isSelected());
            dm.setDeadline((Date) spnDeadline.getValue());
            dm.setBlockLate(cboDeadlinePolicy.getSelectedIndex() == 1);
            dm.saveToDatabase();
            updateDeadlineCountdownUI();
            onLog(String.format("[HẠN NỘP] Cập nhật: %s | Hạn chót: %s | Xử lý muộn: %s",
                    dm.isEnabled() ? "KÍCH HOẠT" : "VÔ HIỆU HÓA",
                    dm.getFormattedDeadline(),
                    dm.isBlockLate() ? "Khóa cổng khi quá hạn" : "Cho phép nộp muộn"));
            JOptionPane.showMessageDialog(this, "Đã lưu và áp dụng cấu hình hạn nộp bài thành công!", "Cập nhật thành công", JOptionPane.INFORMATION_MESSAGE);
        });

        // Khóa / Mở cổng nộp bài thủ công
        btnToggleGate.addActionListener(e -> {
            DeadlineManager dm = DeadlineManager.getInstance();
            boolean isClosed = dm.isManualClosed();
            dm.setManualClosed(!isClosed);
            dm.saveToDatabase();
            updateDeadlineCountdownUI();
            if (dm.isManualClosed()) {
                onLog("[CỔNG NHẬN BÀI] Giảng viên đã KHÓA KHẨN CẤP cổng nhận bài!");
            } else {
                onLog("[CỔNG NHẬN BÀI] Giảng viên đã MỞ LẠI cổng nhận bài.");
            }
        });

        // Bộ đếm đếm ngược thời gian thực
        countdownTimer = new javax.swing.Timer(1000, e -> updateDeadlineCountdownUI());
        countdownTimer.start();
    }

    private void showTableContextMenu(MouseEvent e) {
        int row = tableSubmissions.rowAtPoint(e.getPoint());
        if (row >= 0 && row < tableSubmissions.getRowCount()) {
            tableSubmissions.setRowSelectionInterval(row, row);
        } else {
            return;
        }

        JPopupMenu popup = new JPopupMenu();
        popup.setBackground(MeetTheme.CARD_BG);
        popup.setBorder(BorderFactory.createLineBorder(MeetTheme.CARD_BORDER, 1));

        JMenuItem miGrade = new JMenuItem("Chấm điểm & đánh giá bài này");
        miGrade.setForeground(MeetTheme.TEXT_PRIMARY);
        miGrade.setBackground(MeetTheme.CARD_BG);
        miGrade.addActionListener(evt -> openGradingForSelectedRow());

        JMenuItem miOpen = new JMenuItem("Mở file bài làm");
        miOpen.setForeground(MeetTheme.TEXT_PRIMARY);
        miOpen.setBackground(MeetTheme.CARD_BG);
        miOpen.addActionListener(evt -> openFileForSelectedRow());

        JMenuItem miCopyHash = new JMenuItem("Sao chép mã SHA-256");
        miCopyHash.setForeground(MeetTheme.TEXT_PRIMARY);
        miCopyHash.setBackground(MeetTheme.CARD_BG);
        miCopyHash.addActionListener(evt -> {
            int selectedRow = tableSubmissions.getSelectedRow();
            if (selectedRow != -1) {
                int modelRow = tableSubmissions.convertRowIndexToModel(selectedRow);
                if (modelRow >= 0 && modelRow < submissionList.size()) {
                    String hash = submissionList.get(modelRow).getFileHash();
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(hash), null);
                    JOptionPane.showMessageDialog(this, "Đã sao chép SHA-256:\n" + hash, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        popup.add(miGrade);
        popup.add(miOpen);
        popup.addSeparator();
        popup.add(miCopyHash);

        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    private void applySubmissionFilter() {
        String text = txtSearch.getText().trim();
        int filterIndex = cboFilterStatus.getSelectedIndex();

        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        if (!text.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + text, 2, 3, 5, 6, 8, 11));
        }

        if (filterIndex == 1) { // Chưa chấm điểm
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<?, ?> entry) {
                    Object val = entry.getValue(10);
                    return val != null && "Chưa chấm".equals(val.toString());
                }
            });
        } else if (filterIndex == 2) { // Đã chấm điểm
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<?, ?> entry) {
                    Object val = entry.getValue(10);
                    return val != null && !"Chưa chấm".equals(val.toString());
                }
            });
        } else if (filterIndex == 3) { // Nộp muộn
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<?, ?> entry) {
                    Object val = entry.getValue(9);
                    return val != null && (val.toString().startsWith("Nộp muộn") || val.toString().startsWith("Nộp trễ"));
                }
            });
        } else if (filterIndex == 4) { // Đúng hạn
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<?, ?> entry) {
                    Object val = entry.getValue(9);
                    return val != null && val.toString().startsWith("Đúng hạn");
                }
            });
        }

        if (filters.isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    private void openGradingForSelectedRow() {
        int selectedRow = tableSubmissions.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một bài nộp trong danh sách để chấm điểm!", "Chưa chọn bài nộp", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int modelRow = tableSubmissions.convertRowIndexToModel(selectedRow);
        if (modelRow >= 0 && modelRow < submissionList.size()) {
            GradingDialog dlg = new GradingDialog(this, submissionList, modelRow, updated -> {
                // Cập nhật lại dòng trong bảng
                tableModel.setValueAt(updated.getFormattedScore(), modelRow, 10);
                tableModel.setValueAt(updated.getFeedback(), modelRow, 11);
                updateStatsOverview();
                loadStudentsFromDatabase();
            });
            dlg.setVisible(true);
        }
    }

    private void openFileForSelectedRow() {
        int selectedRow = tableSubmissions.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một bài nộp để mở file!", "Chưa chọn bài nộp", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int modelRow = tableSubmissions.convertRowIndexToModel(selectedRow);
        if (modelRow >= 0 && modelRow < submissionList.size()) {
            SubmissionModel item = submissionList.get(modelRow);
            File file = new File(item.getSavedPath());
            if (file.exists()) {
                if (SecurityUtil.isBlockedExtension(file.getName())) {
                    int confirm = JOptionPane.showConfirmDialog(this,
                            "Cảnh báo bảo mật: File '" + file.getName() + "' là file thực thi/script có thể chứa mã độc.\nBạn có chắc chắn muốn mở?",
                            "Cảnh báo an toàn hệ thống",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                    if (confirm != JOptionPane.YES_OPTION) return;
                }
                try {
                    Desktop.getDesktop().open(file);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Không thể mở file: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "File không tồn tại trên đĩa:\n" + item.getSavedPath(), "Lỗi tìm file", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void setQuickDeadline(int minutes, boolean endOfDay) {
        Calendar cal = Calendar.getInstance();
        if (endOfDay) {
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
        } else {
            cal.add(Calendar.MINUTE, minutes);
        }
        spnDeadline.setValue(cal.getTime());
        chkEnableDeadline.setSelected(true);
    }

    private void syncDeadlineToUI() {
        DeadlineManager dm = DeadlineManager.getInstance();
        dm.loadFromDatabase();
        SwingUtilities.invokeLater(() -> {
            chkEnableDeadline.setSelected(dm.isEnabled());
            if (dm.getDeadline() != null) {
                spnDeadline.setValue(dm.getDeadline());
            }
            cboDeadlinePolicy.setSelectedIndex(dm.isBlockLate() ? 1 : 0);
            updateDeadlineCountdownUI();
        });
    }

    private void updateDeadlineCountdownUI() {
        DeadlineManager dm = DeadlineManager.getInstance();
        if (dm.isManualClosed()) {
            lblDeadlineCountdown.setText("CỔNG ĐANG KHÓA THỦ CÔNG BỞI GIẢNG VIÊN");
            lblDeadlineCountdown.setForeground(MeetTheme.GOOGLE_RED);
            btnToggleGate.setText("Mở lại nhận bài");
            btnToggleGate.setVariant(MeetButton.Variant.SUCCESS);
        } else {
            btnToggleGate.setText("Khóa nhận bài ngay");
            btnToggleGate.setVariant(MeetButton.Variant.DANGER);

            if (!dm.isEnabled()) {
                lblDeadlineCountdown.setText("Hạn nộp: Không giới hạn thời gian (Cổng mở tự do)");
                lblDeadlineCountdown.setForeground(MeetTheme.TEXT_SECONDARY);
            } else {
                if (dm.isExpired()) {
                    String lateNote = dm.isBlockLate() ? " (ĐÃ KHÓA CỔNG TỪ CHỐI BÀI)" : " (VẪN NHẬN BÀI - GẮN NHÃN MUỘN)";
                    lblDeadlineCountdown.setText(dm.getRemainingTimeFormatted() + lateNote);
                    lblDeadlineCountdown.setForeground(MeetTheme.GOOGLE_RED);
                } else {
                    lblDeadlineCountdown.setText("Còn lại: " + dm.getRemainingTimeFormatted() + " (Hạn chót: " + dm.getFormattedDeadline() + ")");
                    lblDeadlineCountdown.setForeground(MeetTheme.GOOGLE_GREEN_BRIGHT);
                }
            }
        }
    }

    public void startServerCore() {
        if (serverCore != null && serverCore.isRunning()) return;
        int port;
        try {
            port = Integer.parseInt(txtPort.getText().trim());
            if (port < 1024 || port > 65535) {
                port = 12345;
                txtPort.setText("12345");
            }
        } catch (NumberFormatException ex) {
            port = 12345;
            txtPort.setText("12345");
        }
        serverCore = new ServerCore(port, ServerGUI.this);
        serverCore.startServer();
    }

    private void initDatabaseAndLoadData() {
        new Thread(() -> {
            try {
                DatabaseManager.initialize(message -> onLog(message));
                syncDeadlineToUI();

                SwingUtilities.invokeLater(() -> {
                    badgeDbStatus.updateBadge("CSDL: Kết nối tốt (SQLite)", MeetTheme.GOOGLE_GREEN_BRIGHT, MeetTheme.TEXT_PRIMARY, MeetTheme.INPUT_BG);
                });

                loadSubmissionsFromDatabase();
                loadStudentsFromDatabase();

                SwingUtilities.invokeLater(this::startServerCore);

            } catch (Exception ex) {
                final String msg = ex.getMessage();
                SwingUtilities.invokeLater(() -> {
                    badgeDbStatus.updateBadge("CSDL: Lỗi kết nối", MeetTheme.GOOGLE_RED, MeetTheme.TEXT_PRIMARY, MeetTheme.INPUT_BG);
                    onLog("[LỖI DATABASE] " + msg);
                });
            }
        }).start();
    }

    private void loadSubmissionsFromDatabase() {
        try {
            List<SubmissionModel> list = SubmissionDAO.getAllSubmissions();
            SwingUtilities.invokeLater(() -> {
                submissionList.clear();
                tableModel.setRowCount(0);
                int stt = 1;
                for (SubmissionModel sub : list) {
                    submissionList.add(sub);
                    tableModel.addRow(new Object[]{
                            stt++,
                            sub.getSubmissionTime(),
                            sub.getStudentId(),
                            sub.getStudentName(),
                            sub.getStudentClass(),
                            sub.getFileName(),
                            sub.getFileType().toUpperCase(),
                            sub.getFormattedFileSize(),
                            sub.getShortHash(),
                            sub.getStatus(),
                            sub.getFormattedScore(),
                            sub.getFeedback()
                    });
                }
                updateStatsOverview();
                onLog("Đã nạp " + submissionList.size() + " bài nộp từ cơ sở dữ liệu SQLite.");
            });
        } catch (SQLException e) {
            onLog("[LỖI ĐỌC BÀI NỘP] " + e.getMessage());
        }
    }

    private void loadStudentsFromDatabase() {
        try {
            List<StudentModel> list = StudentDAO.getAllStudentsWithStats();
            SwingUtilities.invokeLater(() -> {
                studentList.clear();
                studentTableModel.setRowCount(0);
                int stt = 1;
                for (StudentModel s : list) {
                    studentList.add(s);
                    studentTableModel.addRow(new Object[]{
                            stt++,
                            s.getStudentId(),
                            s.getFullName(),
                            s.getClassName(),
                            s.getSubmissionCount(),
                            s.getGradedCount(),
                            s.getFormattedAverageScore(),
                            s.getLastSubmissionTime()
                    });
                }
                lblStatusBarLeft.setText(String.format("Tổng số: %d bài nộp | %d sinh viên đăng ký", submissionList.size(), studentList.size()));
            });
        } catch (SQLException e) {
            onLog("[LỖI ĐỌC SINH VIÊN] " + e.getMessage());
        }
    }

    private void updateStatsOverview() {
        try {
            SubmissionDAO.GradeStats stats = SubmissionDAO.getGradeStats();

            lblStatTotal.setText(String.valueOf(stats.totalSubmissions));
            lblStatGraded.setText(String.valueOf(stats.gradedCount));
            lblStatUngraded.setText(String.valueOf(stats.ungradedCount));

            if (stats.gradedCount > 0) {
                lblStatAvgScore.setText(String.format("%.1f / 10", stats.averageScore));
                lblStatHighest.setText(String.format("Điểm cao nhất: %.1f / 10", stats.maxScore));
                lblStatLowest.setText(String.format("Điểm thấp nhất: %.1f / 10", stats.minScore));

                int g = stats.gradedCount;
                int pEx = (int) Math.round((double) stats.countExcellent / g * 100);
                int pGood = (int) Math.round((double) stats.countGood / g * 100);
                int pAvg = (int) Math.round((double) stats.countAverage / g * 100);
                int pWeak = (int) Math.round((double) stats.countWeak / g * 100);

                lblStatExCount.setText(String.format("%d bài (%d%%)", stats.countExcellent, pEx));
                lblStatGoodCount.setText(String.format("%d bài (%d%%)", stats.countGood, pGood));
                lblStatAvgCount.setText(String.format("%d bài (%d%%)", stats.countAverage, pAvg));
                lblStatWeakCount.setText(String.format("%d bài (%d%%)", stats.countWeak, pWeak));

                pbEx.setValue(pEx);
                pbGood.setValue(pGood);
                pbAvg.setValue(pAvg);
                pbWeak.setValue(pWeak);
            } else {
                lblStatAvgScore.setText("0.0 / 10");
                lblStatHighest.setText("Điểm cao nhất: 0.0 / 10");
                lblStatLowest.setText("Điểm thấp nhất: 0.0 / 10");

                lblStatExCount.setText("0 bài (0%)");
                lblStatGoodCount.setText("0 bài (0%)");
                lblStatAvgCount.setText("0 bài (0%)");
                lblStatWeakCount.setText("0 bài (0%)");

                pbEx.setValue(0);
                pbGood.setValue(0);
                pbAvg.setValue(0);
                pbWeak.setValue(0);
            }
        } catch (SQLException ex) {
            onLog("[LỖI THỐNG KÊ] " + ex.getMessage());
        }
    }

    private void exportToCsv() {
        if (submissionList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chưa có bài nộp nào để xuất bảng điểm!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("Bang_diem_bai_nop_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".csv"));
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (FileWriter fw = new FileWriter(fileToSave)) {
                fw.write('\ufeff'); // BOM UTF-8 cho Microsoft Excel
                fw.write("STT,Thời gian nộp,MSSV,Họ và tên,Lớp,Tên file,Loại file,Kích thước,Mã SHA-256,Trạng thái,Điểm số,Đánh giá / Nhận xét,Thời gian chấm,Đường dẫn file\n");

                int index = 1;
                for (SubmissionModel sub : submissionList) {
                    fw.write(String.format("\"%d\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                            index++,
                            sub.getSubmissionTime(),
                            sub.getStudentId(),
                            sub.getStudentName(),
                            sub.getStudentClass(),
                            sub.getFileName(),
                            sub.getFileType().toUpperCase(),
                            sub.getFormattedFileSize(),
                            sub.getFileHash(),
                            sub.getStatus(),
                            sub.isGraded() ? sub.getFormattedScore() : "Chưa chấm",
                            sub.getFeedback().replace("\"", "\"\""),
                            sub.getGradedAt(),
                            sub.getSavedPath().replace("\\", "/")
                    ));
                }
                JOptionPane.showMessageDialog(this, "Xuất bảng điểm CSV thành công:\n" + fileToSave.getAbsolutePath(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi ghi file: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================================
    // LISTENER TỪ SERVER CORE
    // =========================================================================
    @Override
    public void onServerStarted(final String ip, final int port) {
        SwingUtilities.invokeLater(() -> {
            badgeIp.updateBadge("LAN IP: " + ip, MeetTheme.GOOGLE_BLUE_LIGHT, MeetTheme.TEXT_PRIMARY, MeetTheme.INPUT_BG);
            badgeServerStatus.updateBadge("Đang nhận bài (Port " + port + ")", MeetTheme.GOOGLE_GREEN_BRIGHT, MeetTheme.TEXT_PRIMARY, MeetTheme.INPUT_BG);
            btnToggleServer.setText("Dừng Server");
            btnToggleServer.setVariant(MeetButton.Variant.DANGER);
            txtPort.setEnabled(false);
            lblStatusBarLeft.setText("Server đang hoạt động trên cổng " + port + ". Sẵn sàng nhận file.");
        });
    }

    @Override
    public void onServerStopped() {
        SwingUtilities.invokeLater(() -> {
            badgeServerStatus.updateBadge("Đã dừng", MeetTheme.GOOGLE_RED, MeetTheme.TEXT_PRIMARY, MeetTheme.INPUT_BG);
            btnToggleServer.setText("Khởi động Server");
            btnToggleServer.setVariant(MeetButton.Variant.SUCCESS);
            txtPort.setEnabled(true);
            lblStatusBarLeft.setText("Máy chủ đã dừng hoạt động.");
        });
    }

    @Override
    public void onLog(final String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
            txtLog.append("[" + timestamp + "] " + message + "\n");
            txtLog.setCaretPosition(txtLog.getDocument().getLength());
        });
    }

    @Override
    public void onSubmissionReceived(final SubmissionModel submission) {
        SwingUtilities.invokeLater(() -> {
            submissionList.add(submission);
            int stt = submissionList.size();
            tableModel.addRow(new Object[]{
                    stt,
                    submission.getSubmissionTime(),
                    submission.getStudentId(),
                    submission.getStudentName(),
                    submission.getStudentClass(),
                    submission.getFileName(),
                    submission.getFileType().toUpperCase(),
                    submission.getFormattedFileSize(),
                    submission.getShortHash(),
                    submission.getStatus(),
                    submission.getFormattedScore(),
                    submission.getFeedback()
            });

            updateStatsOverview();
            loadStudentsFromDatabase();
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ServerGUI().setVisible(true));
    }
}
