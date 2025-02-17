package plus.gaga.middleware.sdk.infrastructrue.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import plus.gaga.middleware.sdk.types.utils.RandomStringUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GitCommand {

    private final Logger logger= LoggerFactory.getLogger(GitCommand.class);
    //会回传的url地址
    private final String githubReviewLogUri;

    private final String githubToken;

    private final String project;

    private final String branch;

    private final String author;

    private final String message;

    public GitCommand(String githubReviewLogUri, String githubToken, String project, String branch, String author, String message) {
        this.githubReviewLogUri = githubReviewLogUri;
        this.githubToken = githubToken;
        this.project = project;
        this.branch = branch;
        this.author = author;
        this.message = message;

    }
    //查看当前不同的

    public String diff() throws  InterruptedException, IOException {
        // openai.itedus.cn
        ProcessBuilder logProcessBuilder = new ProcessBuilder("git", "log", "-1", "--pretty=format:%H");
        logProcessBuilder.directory(new File("."));
        Process logProcess = logProcessBuilder.start();

        BufferedReader logReader = new BufferedReader(new InputStreamReader(logProcess.getInputStream()));
        String latestCommitHash = logReader.readLine();
        logReader.close();
        logProcess.waitFor();

        ProcessBuilder diffProcessBuilder = new ProcessBuilder("git", "diff", latestCommitHash + "^", latestCommitHash);
        diffProcessBuilder.directory(new File("."));
        Process diffProcess = diffProcessBuilder.start();

        StringBuilder diffCode = new StringBuilder();
        BufferedReader diffReader = new BufferedReader(new InputStreamReader(diffProcess.getInputStream()));
        String line;
        while ((line = diffReader.readLine()) != null) {
            diffCode.append(line).append("\n");
        }
        diffReader.close();

        int exitCode = diffProcess.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Failed to get diff, exit code:" + exitCode);
        }

        return diffCode.toString();
    }


    public String commitAndPush(String recommend) throws Exception {
        logger.info(githubReviewLogUri + ".git");

        // 通过 Java 操作 git 命令完成 commit 和 push 操作
        Git git = Git.cloneRepository()
                .setURI(githubReviewLogUri + ".git")
                .setDirectory(new File("repo"))  // 克隆到 repo 目录
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubToken, ""))
                .call();

        // 创建分支和目录
        String dateFolderName = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        File repoDir = new File("repo");
        File dateFolder = new File(repoDir, dateFolderName);
        if (!dateFolder.exists()) {
            dateFolder.mkdirs();  // 如果目录不存在则创建
        }

        String fileName = dateFolderName + "/" + project + "_" + branch + "_" + author + "_"
                + System.currentTimeMillis() + "-" + RandomStringUtils.randomNumeric(4) + ".md";
        logger.info("open-ai-review git commit and push fileName:{}", fileName);

        // 创建文件
        File newFile = new File(dateFolder, fileName);

        // 写入文件
        try (FileWriter writer = new FileWriter(newFile)) {
            writer.write(recommend);
        }

        // 添加文件到 git
        git.add().addFilepattern(dateFolderName + "/" + fileName).call();

        // 提交更改
        git.commit().setMessage("add code review new file " + fileName).call();

        // 推送到远程仓库
        git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubToken, "")).call();

        logger.info("open-ai-review git commit and push done!{}", fileName);

        // 返回 GitHub 上的文件 URL
        return githubReviewLogUri + "/blob/master/" + dateFolderName + "/" + fileName;
    }



    public String getMessage() {
        return message;
    }

    public String getAuthor() {
        return author;
    }

    public String getBranch() {
        return branch;
    }

    public String getProject() {
        return project;
    }

    public String getGithubToken() {
        return githubToken;
    }

    public String getGithubReviewLogUri() {
        return githubReviewLogUri;
    }
}
