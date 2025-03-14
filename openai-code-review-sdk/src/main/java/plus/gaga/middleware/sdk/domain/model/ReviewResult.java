package plus.gaga.middleware.sdk.domain.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 代码评审结果模型
 */
public class ReviewResult {
    /**
     * 评审项目名称
     */
    private String projectName;
    
    /**
     * 评审分支
     */
    private String branchName;
    
    /**
     * 评审作者
     */
    private String author;
    
    /**
     * 提交信息
     */
    private String commitMessage;
    
    /**
     * 评审时间
     */
    private String reviewTime;
    
    /**
     * 整体评价
     */
    private String overallComment;
    
    /**
     * 文件评审结果列表
     */
    private List<FileReview> fileReviews = new ArrayList<>();
    
    /**
     * 总体改进建议
     */
    private List<String> improvements = new ArrayList<>();
    
    /**
     * 代码质量得分（0-100）
     */
    private int qualityScore;
    
    public ReviewResult(String projectName, String branchName, String author, String commitMessage) {
        this.projectName = projectName;
        this.branchName = branchName;
        this.author = author;
        this.commitMessage = commitMessage;
    }
    
    public void addFileReview(FileReview fileReview) {
        this.fileReviews.add(fileReview);
    }
    
    public void addImprovement(String improvement) {
        this.improvements.add(improvement);
    }
    
    public String getProjectName() {
        return projectName;
    }
    
    public String getBranchName() {
        return branchName;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public String getCommitMessage() {
        return commitMessage;
    }
    
    public String getReviewTime() {
        return reviewTime;
    }
    
    public void setReviewTime(String reviewTime) {
        this.reviewTime = reviewTime;
    }
    
    public String getOverallComment() {
        return overallComment;
    }
    
    public void setOverallComment(String overallComment) {
        this.overallComment = overallComment;
    }
    
    public List<FileReview> getFileReviews() {
        return fileReviews;
    }
    
    public List<String> getImprovements() {
        return improvements;
    }
    
    public int getQualityScore() {
        return qualityScore;
    }
    
    public void setQualityScore(int qualityScore) {
        this.qualityScore = qualityScore;
    }
    
    /**
     * 将评审结果转化为Markdown格式
     */
    public String toMarkdown() {
        StringBuilder md = new StringBuilder();
        
        // 标题
        md.append("# 代码评审报告\n\n");
        
        // 基本信息
        md.append("## 基本信息\n\n");
        md.append("- **项目**: ").append(projectName).append("\n");
        md.append("- **分支**: ").append(branchName).append("\n");
        md.append("- **作者**: ").append(author).append("\n");
        md.append("- **提交信息**: ").append(commitMessage).append("\n");
        md.append("- **评审时间**: ").append(reviewTime).append("\n");
        md.append("- **代码质量得分**: ").append(qualityScore).append("/100\n\n");
        
        // 整体评价
        md.append("## 整体评价\n\n");
        md.append(overallComment).append("\n\n");
        
        // 文件评审
        md.append("## 详细评审\n\n");
        for (FileReview fileReview : fileReviews) {
            md.append("### ").append(fileReview.getFilePath()).append("\n\n");
            md.append(fileReview.getComment()).append("\n\n");
            
            // 代码评注
            if (!fileReview.getCodeComments().isEmpty()) {
                md.append("#### 代码评注\n\n");
                for (CodeComment codeComment : fileReview.getCodeComments()) {
                    md.append("- **第").append(codeComment.getLineNumber()).append("行**: ")
                      .append(codeComment.getComment()).append("\n");
                    if (codeComment.getSuggestion() != null && !codeComment.getSuggestion().isEmpty()) {
                        md.append("  - 建议: ").append(codeComment.getSuggestion()).append("\n");
                    }
                }
                md.append("\n");
            }
        }
        
        // 改进建议
        md.append("## 改进建议\n\n");
        for (String improvement : improvements) {
            md.append("- ").append(improvement).append("\n");
        }
        
        return md.toString();
    }
    
    /**
     * 文件评审结果
     */
    public static class FileReview {
        /**
         * 文件路径
         */
        private String filePath;
        
        /**
         * 评审评论
         */
        private String comment;
        
        /**
         * 代码评注列表
         */
        private List<CodeComment> codeComments = new ArrayList<>();
        
        public FileReview(String filePath, String comment) {
            this.filePath = filePath;
            this.comment = comment;
        }
        
        public void addCodeComment(CodeComment codeComment) {
            this.codeComments.add(codeComment);
        }
        
        public String getFilePath() {
            return filePath;
        }
        
        public String getComment() {
            return comment;
        }
        
        public List<CodeComment> getCodeComments() {
            return codeComments;
        }
    }
    
    /**
     * 代码评注
     */
    public static class CodeComment {
        /**
         * 行号
         */
        private int lineNumber;
        
        /**
         * 评注内容
         */
        private String comment;
        
        /**
         * 改进建议
         */
        private String suggestion;
        
        /**
         * 严重程度
         */
        private Severity severity;
        
        /**
         * 严重程度枚举
         */
        public enum Severity {
            INFO,
            WARNING,
            ERROR
        }
        
        public CodeComment(int lineNumber, String comment, Severity severity) {
            this.lineNumber = lineNumber;
            this.comment = comment;
            this.severity = severity;
        }
        
        public int getLineNumber() {
            return lineNumber;
        }
        
        public String getComment() {
            return comment;
        }
        
        public String getSuggestion() {
            return suggestion;
        }
        
        public void setSuggestion(String suggestion) {
            this.suggestion = suggestion;
        }
        
        public Severity getSeverity() {
            return severity;
        }
    }
} 