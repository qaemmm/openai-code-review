package plus.gaga.middleware.sdk.domain.service.impl;

import plus.gaga.middleware.sdk.domain.service.AbstractOpenAiCodeReviewService;
import plus.gaga.middleware.sdk.infrastructrue.git.GitCommand;
import plus.gaga.middleware.sdk.infrastructrue.ollama.IOllama;
import plus.gaga.middleware.sdk.infrastructrue.openai.IOpenAI;
import plus.gaga.middleware.sdk.infrastructrue.weixin.WeiXin;
import plus.gaga.middleware.sdk.infrastructrue.weixin.dto.TemplateMessageDTO;

import java.util.HashMap;
import java.util.Map;

public class OpenAiCodeReviewService extends AbstractOpenAiCodeReviewService {

    public OpenAiCodeReviewService(GitCommand gitCommand, WeiXin weiXin, IOpenAI iOpenAI, IOllama ollama){
        super(weiXin,gitCommand,iOpenAI,ollama);

    }
    @Override
    protected void sendMessage(String logUrl) throws Exception {
        Map<String, Map<String, String>> data = new HashMap<>();
        //项目名称、项目分支、作者、提交信息
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.REPO_NAME, gitCommand.getProject());
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.BRANCH_NAME, gitCommand.getBranch());
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.COMMIT_AUTHOR, gitCommand.getAuthor());
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.COMMIT_MESSAGE, gitCommand.getMessage());
        weiXin.sendTemplateMessage(logUrl,data);
    }

    @Override
    protected String commitAndPush(String recommend) throws Exception {
        return gitCommand.commitAndPush(recommend);
    }

//    @Override
//    protected String codeReview(String diffCode) throws Exception {
//        ChatCompletionRequestDTO chatCompletionRequestDTO = new ChatCompletionRequestDTO();
//        chatCompletionRequestDTO.setModel(Model.GLM_4_FLASH.getCode());
//        chatCompletionRequestDTO.setMessages(new ArrayList<ChatCompletionRequestDTO.Prompt>() {
//            private static final long serialVersionUID = -7988151926241837899L;
//
//            {
//                add(new ChatCompletionRequestDTO.Prompt("user", "你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审。代码如下:"));
//                add(new ChatCompletionRequestDTO.Prompt("user", diffCode));
//            }
//        });
//
//        ChatCompletionSyncResponseDTO completions = iOpenAI.completions(chatCompletionRequestDTO);
//        return  completions.getChoices().get(0).getMessage().getContent();
//    }



    protected String codeReview(String diffCode,String logUrl) throws Exception {
//        ChatCompletionRequestDTO chatCompletionRequestDTO = new ChatCompletionRequestDTO();
//        chatCompletionRequestDTO.setModel(Model.GLM_4_FLASH.getCode());
//        chatCompletionRequestDTO.setMessages(new ArrayList<ChatCompletionRequestDTO.Prompt>() {
//            private static final long serialVersionUID = -7988151926241837899L;
//
//            {
//                add(new ChatCompletionRequestDTO.Prompt("user", "你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审。代码如下:"));
//                add(new ChatCompletionRequestDTO.Prompt("user", diffCode));
//            }
//        });
        String model = "deepseek-r1:1.5b";
        String ragTag =  extractProjectName(logUrl);
        String message = "你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审。代码如下:"+diffCode;
        return ollama.generateStreamRag(model,ragTag,message);
//        ChatCompletionSyncResponseDTO completions = iOpenAI.completions(chatCompletionRequestDTO);
//        return  completions.getChoices().get(0).getMessage().getContent();
    }


    @Override
    protected String getDiffCode() throws Exception {
        return gitCommand.diff();
    }

    private String extractProjectName(String repoUrl) {
        String[] parts = repoUrl.split("/");
        String projectNameWithGit = parts[parts.length - 1];
        return projectNameWithGit.replace(".git", "");
    }

}
