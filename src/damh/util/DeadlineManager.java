package damh.util;

import damh.db.ConfigDAO;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Quản lý Hạn nộp bài tập (Deadline) và trạng thái đóng/mở cổng nhận bài
 */
public class DeadlineManager {

    private static final DeadlineManager INSTANCE = new DeadlineManager();
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DISPLAY_FORMAT = "dd/MM/yyyy HH:mm";

    private boolean enabled = false;
    private Date deadline = null;
    private boolean blockLate = false;
    private boolean manualClosed = false;

    private DeadlineManager() {
        // Mặc định hạn nộp là cuối ngày hôm nay lúc 23:59:59
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        this.deadline = cal.getTime();
    }

    public static DeadlineManager getInstance() {
        return INSTANCE;
    }

    /**
     * Nạp cấu hình từ SQLite
     */
    public synchronized void loadFromDatabase() {
        try {
            String enStr = ConfigDAO.getConfig("deadline_enabled", "false");
            this.enabled = "true".equalsIgnoreCase(enStr);

            String timeStr = ConfigDAO.getConfig("deadline_time", "");
            if (timeStr != null && !timeStr.trim().isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
                this.deadline = sdf.parse(timeStr.trim());
            }

            String blkStr = ConfigDAO.getConfig("deadline_block_late", "false");
            this.blockLate = "true".equalsIgnoreCase(blkStr);

            String mcStr = ConfigDAO.getConfig("gate_manual_closed", "false");
            this.manualClosed = "true".equalsIgnoreCase(mcStr);
        } catch (ParseException e) {
            // Giữ giá trị mặc định
        }
    }

    /**
     * Lưu cấu hình hiện tại vào SQLite
     */
    public synchronized void saveToDatabase() {
        ConfigDAO.setConfig("deadline_enabled", String.valueOf(enabled));
        if (deadline != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
            ConfigDAO.setConfig("deadline_time", sdf.format(deadline));
        } else {
            ConfigDAO.setConfig("deadline_time", "");
        }
        ConfigDAO.setConfig("deadline_block_late", String.valueOf(blockLate));
        ConfigDAO.setConfig("gate_manual_closed", String.valueOf(manualClosed));
    }

    /**
     * Kiểm tra xem có cho phép nhận bài nộp hay không
     */
    public synchronized boolean isSubmissionAllowed() {
        if (manualClosed) {
            return false;
        }
        if (!enabled || deadline == null) {
            return true;
        }
        Date now = new Date();
        if (!now.after(deadline)) {
            return true;
        }
        // Đã quá hạn: cho phép nếu không kích hoạt chặn nộp muộn
        return !blockLate;
    }

    /**
     * Kiểm tra xem đã quá hạn nộp chưa
     */
    public synchronized boolean isExpired() {
        if (!enabled || deadline == null) {
            return false;
        }
        return new Date().after(deadline);
    }

    /**
     * Số giây còn lại cho đến hạn chót (số âm nghĩa là đã quá hạn)
     */
    public synchronized long getRemainingSeconds() {
        if (deadline == null) {
            return 0;
        }
        return (deadline.getTime() - System.currentTimeMillis()) / 1000;
    }

    /**
     * Chuỗi hạn nộp hiển thị cho người dùng
     */
    public synchronized String getFormattedDeadline() {
        if (!enabled || deadline == null) {
            return "Không giới hạn";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(DISPLAY_FORMAT);
        return sdf.format(deadline);
    }

    /**
     * Chuỗi hiển thị thời gian còn lại hoặc số phút quá hạn
     */
    public synchronized String getRemainingTimeFormatted() {
        if (manualClosed) {
            return "CỔNG ĐANG KHÓA THỦ CÔNG";
        }
        if (!enabled || deadline == null) {
            return "Không giới hạn thời gian";
        }
        long sec = getRemainingSeconds();
        if (sec >= 0) {
            long hours = sec / 3600;
            long mins = (sec % 3600) / 60;
            long s = sec % 60;
            return String.format("Còn lại: %02d:%02d:%02d", hours, mins, s);
        } else {
            long diffSec = Math.abs(sec);
            long mins = diffSec / 60;
            if (mins < 60) {
                return String.format("ĐÃ QUÁ HẠN: %d phút", Math.max(1, mins));
            } else {
                long h = mins / 60;
                long m = mins % 60;
                return String.format("ĐÃ QUÁ HẠN: %d giờ %02d phút", h, m);
            }
        }
    }

    /**
     * Đánh giá trạng thái bài nộp (Đúng hạn / Nộp muộn)
     */
    public synchronized String evaluateSubmissionStatus() {
        if (!enabled || deadline == null) {
            return "Thành công";
        }
        if (!isExpired()) {
            return "Đúng hạn";
        }
        long diffSec = Math.abs(getRemainingSeconds());
        long mins = diffSec / 60;
        if (mins < 60) {
            return "Nộp muộn (+" + Math.max(1, mins) + " phút)";
        } else {
            long h = mins / 60;
            long m = mins % 60;
            return "Nộp muộn (+" + h + " giờ " + m + " phút)";
        }
    }

    public synchronized boolean isEnabled() {
        return enabled;
    }

    public synchronized void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public synchronized Date getDeadline() {
        return deadline;
    }

    public synchronized void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public synchronized boolean isBlockLate() {
        return blockLate;
    }

    public synchronized void setBlockLate(boolean blockLate) {
        this.blockLate = blockLate;
    }

    public synchronized boolean isManualClosed() {
        return manualClosed;
    }

    public synchronized void setManualClosed(boolean manualClosed) {
        this.manualClosed = manualClosed;
    }
}
