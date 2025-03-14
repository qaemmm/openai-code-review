package plus.gaga.middleware.sdk.domain.service;

import plus.gaga.middleware.sdk.infrastructrue.git.GitCommand;
import plus.gaga.middleware.sdk.infrastructrue.feishu.FeiShu;
import plus.gaga.middleware.sdk.infrastructrue.ollama.IOllama;
import plus.gaga.middleware.sdk.infrastructrue.openai.IOpenAI;

public abstract class AbstractOpenAiCodeReviewService {

    protected final FeiShu feiShu;
    protected final GitCommand gitCommand;
    protected final IOpenAI iOpenAI;
    protected final IOllama ollama;

    public AbstractOpenAiCodeReviewService(FeiShu feiShu, GitCommand gitCommand, IOpenAI iOpenAI , IOllama ollama) {
        this.feiShu = feiShu;
        this.gitCommand = gitCommand;
        this.iOpenAI = iOpenAI;
        this.ollama = ollama;
    }

    public void exec() throws Exception {
        // 1. 获取提交代码
        String diffCode = getDiffCode();
        // 2. 开始评审代码
        System.out.println("gitCommand---" + gitCommand.getGithubReviewLogUri());
        String recommend = codeReview(diffCode, gitCommand.getGithubReviewLogUri());
        // 3. 记录评审结果；返回日志地址
        String logUrl = commitAndPush(recommend);
        // 4. 发送飞书消息通知
        sendMessage(logUrl);
    }

    protected void sendMessage(String logUrl) throws Exception {
        feiShu.sendMessage(logUrl);
    }

    protected abstract String getDiffCode() throws Exception;

    protected abstract String codeReview(String diffCode, String githubReviewLogUri);

    protected abstract String commitAndPush(String recommend) throws Exception;
}
