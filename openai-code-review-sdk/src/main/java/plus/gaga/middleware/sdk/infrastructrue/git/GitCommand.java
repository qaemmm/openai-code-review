package plus.gaga.middleware.sdk.infrastructrue.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import plus.gaga.middleware.sdk.types.utils.RandomStringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
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
    public String diff()throws Exception{
        ProcessBuilder logProcessBuilder = new ProcessBuilder("git","diff","-1","--pretty=format:%H");
        Process logProcess = logProcessBuilder.start();
        //去查看提交记录不一样的东西
        BufferedReader logReader = new BufferedReader(new InputStreamReader(logProcess.getInputStream()));
        String lastCommitHash = logReader.readLine();
        logReader.close();
        logProcess.waitFor();
        ProcessBuilder diffProcessBuilder = new ProcessBuilder("git","diff",lastCommitHash+"^",lastCommitHash);
        Process diffProcess = diffProcessBuilder.start();
        //现在就是读取到了diff的差异化数据
        StringBuilder sb = new StringBuilder();
        BufferedReader diffReader = new BufferedReader(new InputStreamReader(diffProcess.getInputStream()));
        String inputLine ;
        while((inputLine=diffReader.readLine())!=null){
            sb.append(inputLine);
        }
        diffReader.close();
        int diffCode = diffProcess.waitFor();

        if(diffCode!=0){
            throw new RuntimeException("diff命令执行失败");
        }


        return sb.toString();
    }

    public String commitAndPush(String recommend)throws Exception{
        logger.info(githubReviewLogUri+".git");
        //通过java来操作git命令完成commit和push操作
        Git git = Git.cloneRepository()
                .setURI(githubReviewLogUri+".git")
                .setDirectory(new File("repo"))
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubToken,""))
                .call()
                ;

        //创建分支
        String dateFolderName = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        File dateFolder = new File("repo/"+dateFolderName);
        if (!dateFolder.exists()){
            dateFolder.mkdirs();
        }

        String fileName = dateFolderName+"/"+project+"_"+branch+"_"+author+"_"+System.currentTimeMillis()+"-"+ RandomStringUtils.randomNumeric(4)+".md";
        logger.info("open-ai-review git commit and push fileName:{}",fileName);
        //创建一个某个目录下的文件
        File newFile = new File(dateFolderName,fileName);
        //传过来的命令行进行写入
        try(FileWriter writer = new FileWriter(newFile)){
            writer.write(recommend);
        }

        git.add().addFilepattern(dateFolderName+"/"+fileName).call();

        git.commit().setMessage("add code review new file"+fileName).call();

        git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubToken,""));

        logger.info("open-ai-review git commit and push done!{}",fileName);

        return githubReviewLogUri+"/blob/master"+dateFolderName+"/"+fileName;

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
