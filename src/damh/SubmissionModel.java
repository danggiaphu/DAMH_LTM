package damh;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SubmissionModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String studentId;
    private String studentName;
    private String studentClass;
    private String fileName;
    private String fileType; // VD: zip, rar, pdf, docx, txt...
    private long fileSize;
    private String submissionTime;
    private String status; // "Thành công", "Nộp trễ", "Lỗi", ...
    private String savedPath;
    private String fileHash; // Mã băm SHA-256 để xác thực toàn vẹn
    private Double score; // Điểm số (0.0 - 10.0), null nếu chưa chấm
    private String feedback; // Đánh giá / Nhận xét của giảng viên
    private String gradedAt; // Thời gian chấm điểm

    public SubmissionModel(String studentId, String studentName, String studentClass, 
                           String fileName, long fileSize, String status, String savedPath) {
        this(studentId, studentName, studentClass, fileName, fileSize, status, savedPath, "");
    }

    public SubmissionModel(String studentId, String studentName, String studentClass, 
                           String fileName, long fileSize, String status, String savedPath, String fileHash) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentClass = studentClass;
        this.fileName = fileName;
        this.fileType = extractFileType(fileName);
        this.fileSize = fileSize;
        this.status = status;
        this.savedPath = savedPath;
        this.fileHash = fileHash;
        this.score = null;
        this.feedback = null;
        this.gradedAt = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.submissionTime = sdf.format(new Date());
    }

    public SubmissionModel(int id, String studentId, String studentName, String studentClass, 
                           String fileName, String fileType, long fileSize, String submissionTime, 
                           String status, String savedPath) {
        this(id, studentId, studentName, studentClass, fileName, fileType, fileSize, submissionTime, status, savedPath, "");
    }

    public SubmissionModel(int id, String studentId, String studentName, String studentClass, 
                           String fileName, String fileType, long fileSize, String submissionTime, 
                           String status, String savedPath, String fileHash) {
        this(id, studentId, studentName, studentClass, fileName, fileType, fileSize, submissionTime, status, savedPath, fileHash, null, null, null);
    }

    public SubmissionModel(int id, String studentId, String studentName, String studentClass, 
                           String fileName, String fileType, long fileSize, String submissionTime, 
                           String status, String savedPath, String fileHash,
                           Double score, String feedback, String gradedAt) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentClass = studentClass;
        this.fileName = fileName;
        this.fileType = (fileType != null && !fileType.isEmpty()) ? fileType.toLowerCase() : extractFileType(fileName);
        this.fileSize = fileSize;
        this.submissionTime = submissionTime;
        this.status = status;
        this.savedPath = savedPath;
        this.fileHash = fileHash;
        this.score = score;
        this.feedback = feedback;
        this.gradedAt = gradedAt;
    }

    public static String extractFileType(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "UNKNOWN";
        }
        int dot = fileName.lastIndexOf('.');
        if (dot >= 0 && dot < fileName.length() - 1) {
            return fileName.substring(dot + 1).toLowerCase();
        }
        return "UNKNOWN";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentClass() {
        return studentClass;
    }

    public void setStudentClass(String studentClass) {
        this.studentClass = studentClass;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
        this.fileType = extractFileType(fileName);
    }

    public String getFileType() {
        if (fileType == null || fileType.isEmpty()) {
            fileType = extractFileType(fileName);
        }
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getSubmissionTime() {
        return submissionTime;
    }

    public void setSubmissionTime(String submissionTime) {
        this.submissionTime = submissionTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSavedPath() {
        return savedPath;
    }

    public void setSavedPath(String savedPath) {
        this.savedPath = savedPath;
    }

    public String getFormattedFileSize() {
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.2f KB", fileSize / 1024.0);
        } else {
            return String.format("%.2f MB", fileSize / (1024.0 * 1024.0));
        }
    }

    public String getFileHash() {
        return fileHash != null ? fileHash : "";
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public String getShortHash() {
        if (fileHash == null || fileHash.isEmpty()) {
            return "N/A";
        }
        if (fileHash.length() > 8) {
            return fileHash.substring(0, 8) + "...";
        }
        return fileHash;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public boolean isGraded() {
        return score != null;
    }

    public String getFormattedScore() {
        if (score == null) {
            return "Chưa chấm";
        }
        if (score == Math.floor(score)) {
            return String.format("%.0f", score);
        }
        return String.format("%.1f", score);
    }

    public String getFeedback() {
        return feedback != null ? feedback : "";
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getGradedAt() {
        return gradedAt != null ? gradedAt : "";
    }

    public void setGradedAt(String gradedAt) {
        this.gradedAt = gradedAt;
    }
}
