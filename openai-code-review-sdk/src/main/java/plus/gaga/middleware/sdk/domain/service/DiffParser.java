package plus.gaga.middleware.sdk.domain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import plus.gaga.middleware.sdk.domain.model.CodeChange;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Git Diff解析器，用于解析Git Diff输出
 */
public class DiffParser {
    private static final Logger logger = LoggerFactory.getLogger(DiffParser.class);
    
    // 匹配diff文件头
    private static final Pattern FILE_HEADER_PATTERN = 
            Pattern.compile("^diff --git a/(.*) b/(.*)$");
    
    // 匹配新文件
    private static final Pattern NEW_FILE_PATTERN = 
            Pattern.compile("^new file mode \\d+$");
    
    // 匹配删除文件
    private static final Pattern DELETED_FILE_PATTERN = 
            Pattern.compile("^deleted file mode \\d+$");
    
    // 匹配块头（@@ -start,lines +start,lines @@）
    private static final Pattern CHUNK_HEADER_PATTERN = 
            Pattern.compile("^@@ -(\\d+)(?:,(\\d+))? \\+(\\d+)(?:,(\\d+))? @@.*$");
    
    /**
     * 解析Git Diff输出
     * 
     * @param diffOutput Git diff命令的输出
     * @return 解析后的代码变更列表
     */
    public List<CodeChange> parse(String diffOutput) {
        List<CodeChange> changes = new ArrayList<>();
        
        if (diffOutput == null || diffOutput.trim().isEmpty()) {
            logger.warn("Git diff输出为空");
            return changes;
        }
        
        String[] lines = diffOutput.split("\n");
        
        int lineIndex = 0;
        while (lineIndex < lines.length) {
            // 查找文件头
            Matcher fileHeaderMatcher = FILE_HEADER_PATTERN.matcher(lines[lineIndex]);
            if (fileHeaderMatcher.matches()) {
                String filePath = fileHeaderMatcher.group(1);
                lineIndex++;
                
                // 确定变更类型
                CodeChange.ChangeType changeType = CodeChange.ChangeType.MODIFY;
                while (lineIndex < lines.length && !lines[lineIndex].startsWith("+++") && !lines[lineIndex].startsWith("---")) {
                    if (NEW_FILE_PATTERN.matcher(lines[lineIndex]).matches()) {
                        changeType = CodeChange.ChangeType.ADD;
                    } else if (DELETED_FILE_PATTERN.matcher(lines[lineIndex]).matches()) {
                        changeType = CodeChange.ChangeType.DELETE;
                    }
                    lineIndex++;
                }
                
                // 跳过文件名行
                lineIndex += 2;
                
                // 创建代码变更对象
                CodeChange codeChange = new CodeChange(filePath, changeType);
                
                // 解析代码块
                lineIndex = parseCodeBlocks(codeChange, lines, lineIndex);
                
                changes.add(codeChange);
            } else {
                lineIndex++;
            }
        }
        
        return changes;
    }
    
    /**
     * 解析代码块
     * 
     * @param codeChange 代码变更对象
     * @param lines Git diff输出的所有行
     * @param startLineIndex 开始行索引
     * @return 解析完当前文件后的行索引
     */
    private int parseCodeBlocks(CodeChange codeChange, String[] lines, int startLineIndex) {
        int lineIndex = startLineIndex;
        
        while (lineIndex < lines.length) {
            String line = lines[lineIndex];
            
            // 检查是否是新文件的开始
            if (line.startsWith("diff --git")) {
                return lineIndex;
            }
            
            // 解析代码块头
            Matcher chunkHeaderMatcher = CHUNK_HEADER_PATTERN.matcher(line);
            if (chunkHeaderMatcher.matches()) {
                int oldStart = Integer.parseInt(chunkHeaderMatcher.group(1));
                int oldLines = chunkHeaderMatcher.group(2) != null ? 
                        Integer.parseInt(chunkHeaderMatcher.group(2)) : 1;
                int newStart = Integer.parseInt(chunkHeaderMatcher.group(3));
                int newLines = chunkHeaderMatcher.group(4) != null ? 
                        Integer.parseInt(chunkHeaderMatcher.group(4)) : 1;
                
                // 创建代码块
                CodeChange.CodeBlock codeBlock = new CodeChange.CodeBlock(oldStart, oldLines, newStart, newLines);
                codeChange.addBlock(codeBlock);
                
                lineIndex++;
                
                // 解析代码行
                while (lineIndex < lines.length) {
                    line = lines[lineIndex];
                    
                    // 检查是否是下一个代码块的开始或文件结束
                    if (line.startsWith("@@") || line.startsWith("diff --git")) {
                        break;
                    }
                    
                    // 处理代码行
                    if (line.startsWith("-")) {
                        codeBlock.addOldLine(line.substring(1));
                    } else if (line.startsWith("+")) {
                        codeBlock.addNewLine(line.substring(1));
                    } else if (!line.startsWith("\\")) { // 忽略 "\ No newline at end of file" 等注释
                        // 上下文行（在新旧版本中都存在）
                        if (line.startsWith(" ")) {
                            line = line.substring(1);
                        }
                        codeBlock.addOldLine(line);
                        codeBlock.addNewLine(line);
                    }
                    
                    lineIndex++;
                }
            } else {
                lineIndex++;
            }
        }
        
        return lineIndex;
    }
} 