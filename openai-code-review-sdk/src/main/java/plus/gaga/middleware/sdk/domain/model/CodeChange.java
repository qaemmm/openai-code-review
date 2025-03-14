package plus.gaga.middleware.sdk.domain.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 代码变更模型，表示Git Diff中的一个文件变更
 */
public class CodeChange {
    /**
     * 变更类型
     */
    public enum ChangeType {
        ADD,     // 新增文件
        MODIFY,  // 修改文件
        DELETE   // 删除文件
    }
    
    /**
     * 文件路径
     */
    private String filePath;
    
    /**
     * 变更类型
     */
    private ChangeType changeType;
    
    /**
     * 代码变更块列表
     */
    private List<CodeBlock> blocks = new ArrayList<>();
    
    /**
     * 文件类型（根据扩展名）
     */
    private String fileType;
    
    public CodeChange(String filePath, ChangeType changeType) {
        this.filePath = filePath;
        this.changeType = changeType;
        this.fileType = extractFileType(filePath);
    }
    
    /**
     * 提取文件类型（扩展名）
     */
    private String extractFileType(String filePath) {
        int dotIndex = filePath.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filePath.length() - 1) {
            return filePath.substring(dotIndex + 1);
        }
        return "unknown";
    }
    
    public void addBlock(CodeBlock block) {
        this.blocks.add(block);
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public ChangeType getChangeType() {
        return changeType;
    }
    
    public List<CodeBlock> getBlocks() {
        return blocks;
    }
    
    public String getFileType() {
        return fileType;
    }
    
    /**
     * 代码块，表示连续的代码变更
     */
    public static class CodeBlock {
        /**
         * 旧版本的起始行号
         */
        private int oldStart;
        
        /**
         * 旧版本的行数
         */
        private int oldLines;
        
        /**
         * 新版本的起始行号
         */
        private int newStart;
        
        /**
         * 新版本的行数
         */
        private int newLines;
        
        /**
         * 旧版本的代码内容
         */
        private List<String> oldContent = new ArrayList<>();
        
        /**
         * 新版本的代码内容
         */
        private List<String> newContent = new ArrayList<>();
        
        public CodeBlock(int oldStart, int oldLines, int newStart, int newLines) {
            this.oldStart = oldStart;
            this.oldLines = oldLines;
            this.newStart = newStart;
            this.newLines = newLines;
        }
        
        public void addOldLine(String line) {
            oldContent.add(line);
        }
        
        public void addNewLine(String line) {
            newContent.add(line);
        }
        
        public int getOldStart() {
            return oldStart;
        }
        
        public int getOldLines() {
            return oldLines;
        }
        
        public int getNewStart() {
            return newStart;
        }
        
        public int getNewLines() {
            return newLines;
        }
        
        public List<String> getOldContent() {
            return oldContent;
        }
        
        public List<String> getNewContent() {
            return newContent;
        }
    }
} 