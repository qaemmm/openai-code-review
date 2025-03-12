package plus.gaga.middleware.sdk.domain.service;

import plus.gaga.middleware.sdk.infrastructrue.git.GitCommand;
import plus.gaga.middleware.sdk.infrastructrue.ollama.IOllama;
import plus.gaga.middleware.sdk.infrastructrue.openai.IOpenAI;
import plus.gaga.middleware.sdk.infrastructrue.weixin.WeiXin;

public abstract class AbstractOpenAiCodeReviewService {

    protected final WeiXin weiXin;
    protected final GitCommand gitCommand;
    protected final IOpenAI iOpenAI;
    protected final IOllama ollama;

    public AbstractOpenAiCodeReviewService(WeiXin weiXin, GitCommand gitCommand, IOpenAI iOpenAI ,IOllama ollama) {
        this.weiXin = weiXin;
        this.gitCommand = gitCommand;
        this.iOpenAI = iOpenAI;
        this.ollama = ollama;
    }

    public void exec() throws Exception {
        // 1. 获取提交代码 ；
        String diffCode = getDiffCode();
        // 2、开始评审代码--glm，
        System.out.println("gitCommand---"+gitCommand.getGithubReviewLogUri());

        String recommend = codeReview(diffCode,gitCommand.getGithubReviewLogUri());
        // 3、记录评审结果；返回日志地址；
        String logUrl = commitAndPush(recommend);
        // 4、发送消息通知；日志地址通知的内容；
        sendMessage(logUrl);
    }

    protected abstract void sendMessage(String logUrl) throws Exception;

    protected abstract String commitAndPush(String recommend) throws Exception;

    protected abstract String codeReview(String diffCode,String logUrl) throws Exception;

    protected abstract String getDiffCode() throws Exception;


}
