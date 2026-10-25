package damh.db;

/**
 * Model đại diện thông tin Sinh viên
 */
public class StudentModel {
    private String studentId;
    private String fullName;
    private String className;
    private String createdAt;
    private String updatedAt;
    private int submissionCount;
    private String lastSubmissionTime;
    private Double averageScore;
    private int gradedCount;

    public StudentModel() {
    }

    public StudentModel(String studentId, String fullName, String className) {
        this.studentId = studentId;
        this.fullName = fullName;
        this.className = className;
    }

    public StudentModel(String studentId, String fullName, String className, 
                        String createdAt, String updatedAt, int submissionCount, String lastSubmissionTime) {
        this(studentId, fullName, className, createdAt, updatedAt, submissionCount, lastSubmissionTime, null, 0);
    }

    public StudentModel(String studentId, String fullName, String className, 
                        String createdAt, String updatedAt, int submissionCount, String lastSubmissionTime,
                        Double averageScore, int gradedCount) {
        this.studentId = studentId;
        this.fullName = fullName;
        this.className = className;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.submissionCount = submissionCount;
        this.lastSubmissionTime = lastSubmissionTime;
        this.averageScore = averageScore;
        this.gradedCount = gradedCount;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getSubmissionCount() {
        return submissionCount;
    }

    public void setSubmissionCount(int submissionCount) {
        this.submissionCount = submissionCount;
    }

    public String getLastSubmissionTime() {
        return lastSubmissionTime != null ? lastSubmissionTime : "Chưa có bài";
    }

    public void setLastSubmissionTime(String lastSubmissionTime) {
        this.lastSubmissionTime = lastSubmissionTime;
    }

    public Double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }

    public int getGradedCount() {
        return gradedCount;
    }

    public void setGradedCount(int gradedCount) {
        this.gradedCount = gradedCount;
    }

    public String getFormattedAverageScore() {
        if (averageScore == null) return "Chưa có";
        return String.format("%.1f", averageScore);
    }
}
