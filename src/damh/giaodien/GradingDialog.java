package damh.giaodien;

import damh.SubmissionModel;
import damh.db.SubmissionDAO;
import damh.giaodien.MeetTheme.*;
import damh.util.SecurityUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.SQLException;
import java.util.List;

public class GradingDialog extends JDialog {

    public interface GradingCallback {
        void onGraded(SubmissionModel updatedSubmission);
    }

    private final List<SubmissionModel> submissionList;
    private int currentIndex;
    private final GradingCallback callback;

    // Component hiển thị thông tin sinh viên
    private MeetAvatar avatar;
    private JLabel lblStudentName;
    private JLabel lblStudentId;
    private JLabel lblStudentClass;

    // Metadata bài nộp
    private JLabel lblFileName;
    private JLabel lblFileSize;
    private JLabel lblSubTime;
    private MeetBadge badgeStatus;
    private JLabel lblHash;
    private MeetButton btnOpenFile;

    // Form chấm điểm
    private MeetTextField txtScore;
    private MeetTextArea txtFeedback;
    private JLabel lblGradedAt;
    private JLabel lblIndexIndicator;

    private MeetButton btnSave;
    private MeetButton btnSaveAndNext;
    private MeetButton btnPrev;
    private MeetButton btnNext;

    public GradingDialog(Window owner, List<SubmissionModel> list, int initialIndex, GradingCallback callback) {
        super(owner, "Chấm Điểm & Đánh Giá Bài Tập — Giảng Viên", ModalityType.APPLICATION_MODAL);
        this.submissionList = list;
        this.currentIndex = initialIndex;
        this.callback = callback;

        setSize(780, 680);
        setMinimumSize(new Dimension(720, 620));
        setLocationRelativeTo(owner);
        getContentPane().setBackground(MeetTheme.BG_DARK);

        initUI();
        displayCurrentSubmission();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(MeetTheme.BG_DARK);
        root.setBorder(new EmptyBorder(16, 20, 16, 20));

        // 1. TOP HEADER: Avatar, thông tin sinh viên & Bộ điều hướng (Prev / Next)
        MeetCard headerCard = new MeetCard(14);
        headerCard.setLayout(new BorderLayout(15, 0));
        headerCard.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftHeader.setOpaque(false);

        avatar = new MeetAvatar("SV", 48);
        leftHeader.add(avatar);

        JPanel infoTextPanel = new JPanel();
        infoTextPanel.setLayout(new BoxLayout(infoTextPanel, BoxLayout.Y_AXIS));
        infoTextPanel.setOpaque(false);

        lblStudentName = new JLabel("HỌ VÀ TÊN SINH VIÊN");
        lblStudentName.setFont(MeetTheme.fontBold(16));
        lblStudentName.setForeground(MeetTheme.TEXT_PRIMARY);

        JPanel subMeta = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        subMeta.setOpaque(false);

        lblStudentId = new JLabel("MSSV: B21DCCN000");
        lblStudentId.setFont(MeetTheme.fontBold(13));
        lblStudentId.setForeground(MeetTheme.GOOGLE_BLUE_LIGHT);

        lblStudentClass = new JLabel("Lớp: D21CQCN01-N");
        lblStudentClass.setFont(MeetTheme.fontPlain(13));
        lblStudentClass.setForeground(MeetTheme.TEXT_SECONDARY);

        subMeta.add(lblStudentId);
        subMeta.add(new JLabel("•") {{ setForeground(MeetTheme.TEXT_MUTED); }});
        subMeta.add(lblStudentClass);

        infoTextPanel.add(lblStudentName);
        infoTextPanel.add(Box.createVerticalStrut(2));
        infoTextPanel.add(subMeta);

        leftHeader.add(infoTextPanel);
        headerCard.add(leftHeader, BorderLayout.WEST);

        // Nút điều hướng trước / sau
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        navPanel.setOpaque(false);

        lblIndexIndicator = new JLabel("0 / 0");
        lblIndexIndicator.setFont(MeetTheme.fontBold(12));
        lblIndexIndicator.setForeground(MeetTheme.TEXT_MUTED);

        btnPrev = new MeetButton("Bài trước", MeetButton.Variant.NEUTRAL);
        btnPrev.setCornerRadius(16);
        btnPrev.setPreferredSize(new Dimension(100, 32));
        btnPrev.addActionListener(e -> navigate(-1));

        btnNext = new MeetButton("Bài sau", MeetButton.Variant.NEUTRAL);
        btnNext.setCornerRadius(16);
        btnNext.setPreferredSize(new Dimension(100, 32));
        btnNext.addActionListener(e -> navigate(1));

        navPanel.add(lblIndexIndicator);
        navPanel.add(Box.createHorizontalStrut(8));
        navPanel.add(btnPrev);
        navPanel.add(btnNext);
        headerCard.add(navPanel, BorderLayout.EAST);

        root.add(headerCard, BorderLayout.NORTH);

        // 2. CENTER: Split thành 2 thẻ (Thẻ File bài nộp bên trái/trên, Thẻ Chấm điểm bên phải/dưới)
        JPanel centerGrid = new JPanel(new GridLayout(1, 2, 14, 0));
        centerGrid.setOpaque(false);

        // --- CỘT TRÁI: Chi tiết bài nộp & Mở file ---
        MeetCard fileCard = new MeetCard(14);
        fileCard.setLayout(new BorderLayout(10, 10));
        fileCard.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel lblFileTitle = new JLabel("Thông Tin Bài Nộp");
        lblFileTitle.setFont(MeetTheme.fontBold(14));
        lblFileTitle.setForeground(MeetTheme.TEXT_PRIMARY);
        fileCard.add(lblFileTitle, BorderLayout.NORTH);

        JPanel fileDetails = new JPanel();
        fileDetails.setLayout(new BoxLayout(fileDetails, BoxLayout.Y_AXIS));
        fileDetails.setOpaque(false);

        lblFileName = new JLabel("Tên file: Chưa có dữ liệu");
        lblFileName.setFont(MeetTheme.fontBold(13));
        lblFileName.setForeground(MeetTheme.TEXT_PRIMARY);

        lblFileSize = new JLabel("Kích thước: 0 KB");
        lblFileSize.setFont(MeetTheme.fontPlain(12));
        lblFileSize.setForeground(MeetTheme.TEXT_SECONDARY);

        lblSubTime = new JLabel("Thời gian nộp: Chưa có dữ liệu");
        lblSubTime.setFont(MeetTheme.fontPlain(12));
        lblSubTime.setForeground(MeetTheme.TEXT_SECONDARY);

        JPanel statusWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        statusWrapper.setOpaque(false);
        badgeStatus = new MeetBadge("Đúng hạn", MeetTheme.GOOGLE_GREEN_BRIGHT, Color.WHITE, new Color(0x1e, 0x8e, 0x3e, 60));
        statusWrapper.add(badgeStatus);

        lblHash = new JLabel("SHA-256: Chưa có dữ liệu");
        lblHash.setFont(MeetTheme.fontMono(11));
        lblHash.setForeground(MeetTheme.TEXT_MUTED);

        MeetButton btnCopyHash = new MeetButton("Sao chép SHA-256", MeetButton.Variant.OUTLINE);
        btnCopyHash.setCornerRadius(14);
        btnCopyHash.setFont(MeetTheme.fontPlain(11));
        btnCopyHash.setPreferredSize(new Dimension(140, 26));
        btnCopyHash.addActionListener(e -> {
            SubmissionModel cur = getCurrent();
            if (cur != null && !cur.getFileHash().isEmpty()) {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(cur.getFileHash()), null);
                JOptionPane.showMessageDialog(this, "Đã sao chép mã băm SHA-256:\n" + cur.getFileHash(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        fileDetails.add(lblFileName);
        fileDetails.add(Box.createVerticalStrut(6));
        fileDetails.add(lblFileSize);
        fileDetails.add(Box.createVerticalStrut(4));
        fileDetails.add(lblSubTime);
        fileDetails.add(Box.createVerticalStrut(6));
        fileDetails.add(statusWrapper);
        fileDetails.add(Box.createVerticalStrut(6));
        fileDetails.add(lblHash);
        fileDetails.add(Box.createVerticalStrut(4));
        fileDetails.add(btnCopyHash);
        fileDetails.add(Box.createVerticalGlue());

        fileCard.add(fileDetails, BorderLayout.CENTER);

        // Nút Mở file bài làm lớn nổi bật
        btnOpenFile = new MeetButton("Mở file bài làm", MeetButton.Variant.PRIMARY);
        btnOpenFile.setCornerRadius(20);
        btnOpenFile.setFont(MeetTheme.fontBold(13));
        btnOpenFile.setPreferredSize(new Dimension(200, 40));
        btnOpenFile.addActionListener(e -> openSubmissionFile());
        fileCard.add(btnOpenFile, BorderLayout.SOUTH);

        centerGrid.add(fileCard);

        // --- CỘT PHẢI: Chấm Điểm & Nhận Xét ---
        MeetCard gradeCard = new MeetCard(14);
        gradeCard.setLayout(new BorderLayout(10, 10));
        gradeCard.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel lblGradeTitle = new JLabel("Chấm Điểm & Đánh Giá Giảng Viên");
        lblGradeTitle.setFont(MeetTheme.fontBold(14));
        lblGradeTitle.setForeground(MeetTheme.TEXT_PRIMARY);
        gradeCard.add(lblGradeTitle, BorderLayout.NORTH);

        JPanel gradeBody = new JPanel();
        gradeBody.setLayout(new BoxLayout(gradeBody, BoxLayout.Y_AXIS));
        gradeBody.setOpaque(false);

        // Nhập điểm
        JPanel scoreInputRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        scoreInputRow.setOpaque(false);
        JLabel lblScorePrompt = new JLabel("Điểm số (thang 10):");
        lblScorePrompt.setFont(MeetTheme.fontBold(13));
        lblScorePrompt.setForeground(MeetTheme.TEXT_PRIMARY);

        txtScore = new MeetTextField("VD: 9.0", 8);
        txtScore.setFont(MeetTheme.fontBold(15));
        txtScore.setPreferredSize(new Dimension(90, 32));
        scoreInputRow.add(lblScorePrompt);
        scoreInputRow.add(txtScore);

        gradeBody.add(scoreInputRow);
        gradeBody.add(Box.createVerticalStrut(4));

        // Nút điểm nhanh
        JPanel quickScorePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        quickScorePanel.setOpaque(false);
        JLabel lblQuick = new JLabel("Điểm nhanh:");
        lblQuick.setFont(MeetTheme.fontPlain(11));
        lblQuick.setForeground(MeetTheme.TEXT_MUTED);
        quickScorePanel.add(lblQuick);

        String[] quickScores = {"5.0", "6.5", "7.0", "8.0", "8.5", "9.0", "9.5", "10.0"};
        for (String qs : quickScores) {
            MeetButton btnQs = new MeetButton(qs, MeetButton.Variant.NEUTRAL);
            btnQs.setCornerRadius(12);
            btnQs.setFont(MeetTheme.fontBold(11));
            btnQs.setPreferredSize(new Dimension(56, 24));
            btnQs.addActionListener(e -> txtScore.setText(qs));
            quickScorePanel.add(btnQs);
        }
        gradeBody.add(quickScorePanel);
        gradeBody.add(Box.createVerticalStrut(8));

        // Nhận xét
        JLabel lblFeedbackPrompt = new JLabel("Đánh giá & Nhận xét bài làm:");
        lblFeedbackPrompt.setFont(MeetTheme.fontBold(13));
        lblFeedbackPrompt.setForeground(MeetTheme.TEXT_PRIMARY);
        gradeBody.add(lblFeedbackPrompt);
        gradeBody.add(Box.createVerticalStrut(4));

        txtFeedback = new MeetTextArea("Nhập nhận xét chi tiết cho sinh viên...", 5, 20);
        JScrollPane scrollFeedback = new JScrollPane(txtFeedback);
        scrollFeedback.setPreferredSize(new Dimension(300, 100));
        MeetTheme.styleScrollPane(scrollFeedback);
        gradeBody.add(scrollFeedback);
        gradeBody.add(Box.createVerticalStrut(6));

        // Gợi ý nhận xét mẫu nhanh
        JPanel quickFeedbackPanel = new JPanel();
        quickFeedbackPanel.setLayout(new BoxLayout(quickFeedbackPanel, BoxLayout.Y_AXIS));
        quickFeedbackPanel.setOpaque(false);

        JLabel lblQuickFb = new JLabel("Gợi ý nhận xét nhanh (Click để chèn):");
        lblQuickFb.setFont(MeetTheme.fontPlain(11));
        lblQuickFb.setForeground(MeetTheme.TEXT_MUTED);
        quickFeedbackPanel.add(lblQuickFb);
        quickFeedbackPanel.add(Box.createVerticalStrut(3));

        String[][] templates = {
                {"OK - Làm tốt, chuẩn yêu cầu", "Bài làm rất tốt, đúng yêu cầu kỹ thuật và cấu trúc rõ ràng."},
                {"OK - Hoàn thành tốt", "Đáp ứng đầy đủ các tiêu chí bài tập, chạy ổn định."},
                {"Cảnh báo - Cần tối ưu thêm", "Bài làm khá, cần chú ý thêm việc xử lý ngoại lệ và tối ưu luồng mạng."},
                {"Cảnh báo - Nộp muộn (-1 điểm)", "Nộp muộn so với hạn quy định của ca thi (-1 điểm theo quy chế)."}
        };

        JPanel tplGrid = new JPanel(new GridLayout(2, 2, 4, 4));
        tplGrid.setOpaque(false);
        for (String[] tpl : templates) {
            MeetButton btnTpl = new MeetButton(tpl[0], MeetButton.Variant.OUTLINE);
            btnTpl.setCornerRadius(10);
            btnTpl.setFont(MeetTheme.fontPlain(11));
            btnTpl.setToolTipText(tpl[1]);
            btnTpl.addActionListener(e -> {
                String cur = txtFeedback.getText().trim();
                if (cur.isEmpty()) {
                    txtFeedback.setText(tpl[1]);
                } else {
                    txtFeedback.setText(cur + " " + tpl[1]);
                }
            });
            tplGrid.add(btnTpl);
        }
        quickFeedbackPanel.add(tplGrid);
        gradeBody.add(quickFeedbackPanel);

        gradeCard.add(gradeBody, BorderLayout.CENTER);


        lblGradedAt = new JLabel("Chưa chấm");
        lblGradedAt.setFont(MeetTheme.fontPlain(11));
        lblGradedAt.setForeground(MeetTheme.TEXT_MUTED);
        gradeCard.add(lblGradedAt, BorderLayout.SOUTH);

        centerGrid.add(gradeCard);
        root.add(centerGrid, BorderLayout.CENTER);

        // 3. BOTTOM ACTIONS: Lưu điểm, Lưu & Chấm bài kế tiếp, Đóng
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);

        MeetButton btnClose = new MeetButton("Đóng", MeetButton.Variant.NEUTRAL);
        btnClose.setCornerRadius(20);
        btnClose.setPreferredSize(new Dimension(90, 38));
        btnClose.addActionListener(e -> dispose());
        bottomBar.add(btnClose, BorderLayout.WEST);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setOpaque(false);

        btnSave = new MeetButton("💾 Lưu Điểm & Đánh Giá", MeetButton.Variant.SUCCESS);
        btnSave.setCornerRadius(20);
        btnSave.setPreferredSize(new Dimension(190, 38));
        btnSave.addActionListener(e -> saveCurrentGrade(false));

        btnSaveAndNext = new MeetButton("💾 Lưu & Chấm Bài Kế Tiếp ⏭", MeetButton.Variant.PRIMARY);
        btnSaveAndNext.setCornerRadius(20);
        btnSaveAndNext.setPreferredSize(new Dimension(220, 38));
        btnSaveAndNext.addActionListener(e -> saveCurrentGrade(true));

        rightActions.add(btnSave);
        rightActions.add(btnSaveAndNext);
        bottomBar.add(rightActions, BorderLayout.EAST);

        root.add(bottomBar, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private SubmissionModel getCurrent() {
        if (submissionList == null || submissionList.isEmpty() || currentIndex < 0 || currentIndex >= submissionList.size()) {
            return null;
        }
        return submissionList.get(currentIndex);
    }

    private void displayCurrentSubmission() {
        SubmissionModel cur = getCurrent();
        if (cur == null) return;

        // Cập nhật chỉ số
        lblIndexIndicator.setText((currentIndex + 1) + " / " + submissionList.size());
        btnPrev.setEnabled(currentIndex > 0);
        btnNext.setEnabled(currentIndex < submissionList.size() - 1);

        // Sinh viên & Avatar
        String name = cur.getStudentName();
        lblStudentName.setText(name);
        lblStudentId.setText("MSSV: " + cur.getStudentId());
        lblStudentClass.setText("Lớp: " + cur.getStudentClass());

        String initials = "SV";
        if (name != null && !name.trim().isEmpty()) {
            String[] parts = name.trim().split("\\s+");
            if (parts.length >= 2) {
                initials = parts[0].substring(0, 1).toUpperCase() + parts[parts.length - 1].substring(0, 1).toUpperCase();
            } else if (parts[0].length() >= 2) {
                initials = parts[0].substring(0, 2).toUpperCase();
            }
        }
        avatar.setInitials(initials);

        // Bài nộp
        lblFileName.setText("Tên file: " + cur.getFileName());
        lblFileSize.setText(String.format("Kích thước: %s (Loại: %s)", cur.getFormattedFileSize(), cur.getFileType().toUpperCase()));
        lblSubTime.setText("Thời gian nộp: " + cur.getSubmissionTime());

        String status = cur.getStatus();
        if (status.startsWith("Đúng hạn")) {
            badgeStatus.updateBadge("OK - " + status, MeetTheme.GOOGLE_GREEN_BRIGHT, Color.WHITE, new Color(0x1e, 0x8e, 0x3e, 70));
        } else if (status.startsWith("Nộp muộn") || status.startsWith("Nộp trễ")) {
            badgeStatus.updateBadge("Cảnh báo - " + status, MeetTheme.GOOGLE_RED, Color.WHITE, new Color(0xea, 0x43, 0x35, 70));
        } else {
            badgeStatus.updateBadge(status, MeetTheme.GOOGLE_BLUE_LIGHT, Color.WHITE, new Color(0x1a, 0x73, 0xe8, 70));
        }

        lblHash.setText("SHA-256: " + cur.getShortHash());

        // Điểm số và nhận xét
        if (cur.isGraded()) {
            txtScore.setText(cur.getFormattedScore());
            txtFeedback.setText(cur.getFeedback());
            lblGradedAt.setText("Đã chấm lúc: " + cur.getGradedAt());
            lblGradedAt.setForeground(MeetTheme.GOOGLE_GREEN_BRIGHT);
        } else {
            txtScore.setText("");
            txtFeedback.setText("");
            lblGradedAt.setText("Trạng thái: Chưa chấm điểm");
            lblGradedAt.setForeground(MeetTheme.TEXT_MUTED);
        }
    }

    private void navigate(int delta) {
        int next = currentIndex + delta;
        if (next >= 0 && next < submissionList.size()) {
            currentIndex = next;
            displayCurrentSubmission();
        }
    }

    private void saveCurrentGrade(boolean goToNext) {
        SubmissionModel cur = getCurrent();
        if (cur == null) return;

        String scoreStr = txtScore.getText().trim();
        Double score = null;

        if (!scoreStr.isEmpty()) {
            try {
                scoreStr = scoreStr.replace(',', '.');
                score = Double.parseDouble(scoreStr);
                if (score < 0.0 || score > 10.0) {
                    JOptionPane.showMessageDialog(this, "Điểm số phải nằm trong khoảng từ 0.0 đến 10.0!", "Điểm không hợp lệ", JOptionPane.WARNING_MESSAGE);
                    txtScore.requestFocus();
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập điểm số là số thực hợp lệ (VD: 8.5)!", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
                txtScore.requestFocus();
                return;
            }
        }

        String feedback = txtFeedback.getText().trim();

        try {
            boolean success = SubmissionDAO.updateGradeAndFeedback(cur.getId(), score, feedback);
            if (success) {
                cur.setScore(score);
                cur.setFeedback(feedback);

                if (callback != null) {
                    callback.onGraded(cur);
                }

                if (goToNext) {
                    if (currentIndex < submissionList.size() - 1) {
                        currentIndex++;
                        displayCurrentSubmission();
                    } else {
                        JOptionPane.showMessageDialog(this, "Đã lưu điểm cho bài cuối cùng trong danh sách!", "Hoàn tất chấm bài", JOptionPane.INFORMATION_MESSAGE);
                        displayCurrentSubmission();
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Đã lưu điểm và đánh giá thành công!", "Lưu thành công", JOptionPane.INFORMATION_MESSAGE);
                    displayCurrentSubmission();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Không thể cập nhật cơ sở dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi lưu vào SQLite: " + ex.getMessage(), "Lỗi Cơ Sở Dữ Liệu", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openSubmissionFile() {
        SubmissionModel cur = getCurrent();
        if (cur == null) return;

        File file = new File(cur.getSavedPath());
        if (!file.exists()) {
            JOptionPane.showMessageDialog(this, "File không tồn tại trên đĩa:\n" + cur.getSavedPath(), "Lỗi tìm file", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (SecurityUtil.isBlockedExtension(file.getName())) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Cảnh báo bảo mật: File '" + file.getName() + "' là định dạng thực thi/script nguy hiểm.\nBạn có chắc chắn muốn mở?",
                    "Cảnh báo an toàn hệ thống",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;
        }

        try {
            Desktop.getDesktop().open(file);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể mở file bằng chương trình mặc định: " + ex.getMessage(), "Lỗi mở file", JOptionPane.ERROR_MESSAGE);
        }
    }
}
