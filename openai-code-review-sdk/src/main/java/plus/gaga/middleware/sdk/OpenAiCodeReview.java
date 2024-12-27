package plus.gaga.middleware.sdk;

import com.alibaba.fastjson2.JSON;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import plus.gaga.middleware.sdk.domain.model.ChatCompletionRequest;
import plus.gaga.middleware.sdk.domain.model.ChatCompletionSyncResponse;
import plus.gaga.middleware.sdk.domain.model.Model;
import plus.gaga.middleware.sdk.types.utils.BearerTokenUtils;
import plus.gaga.middleware.sdk.types.utils.WXAccessTokenUtils;
import plus.gaga.middleware.sdk.domain.model.Message;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

/**
 * 整一个流程大致是git diff HEAD~1 HEAD --前一版的代码和当前的代码变化，
 * 然后调用glm大模型，构建请求参数和响应结果，选择合适的model模型，然后设置prompt作为一个高级开发，对如下的这个内容怎么看？
 * 接着将评审日志进行返回，通过命令行的形式进行代码的git add\commit\push 同时生成相对应的文件，后续还可以配置当前编辑的作者是谁，哪个分支，哪个代码。然后返回一个代码地址。
 */
public class OpenAiCodeReview {
    public static void main(String[] args) throws Exception {
        System.out.println("openai 代码评审，测试执行");

        String token = System.getenv("GITHUB_TOKEN");
        if (null == token || token.isEmpty()) {
            throw new RuntimeException("token is null");
        }

        // 1. 代码检出
        System.out.println("测试执行");
        ProcessBuilder processBuilder = new ProcessBuilder("git","diff","HEAD~1","HEAD");
        Process process = processBuilder.start();

        BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;

        StringBuilder differCode = new StringBuilder();
        while((line = in.readLine())!=null){
            differCode.append(line);
        }
        int exitCode = process.waitFor();
        System.out.println("Exit with code"+exitCode);

        System.out.println("diff code：" + differCode.toString());

        // 2. chatglm 代码评审
        String log = codeReview(differCode.toString());
        System.out.println("code review：" + log);

        // 3. 写入评审日志
        String logUrl  = writeLog(token, log);
        System.out.println("writeLog：" + logUrl);

        // 4. 消息通知
        System.out.println("pushMessage：" + logUrl);
        sendMessage(logUrl);

    }


    private static void sendMessage(String logUrl){
        String accessToken = WXAccessTokenUtils.getAccessToken();
        System.out.println("accessToken:"+accessToken);
        Message message = new Message();
        message.put("project", "big-market");
        message.put("review", logUrl);
        message.setUrl(logUrl);

        String url = String.format("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=%s", accessToken);
        sendPostRequest(url, JSON.toJSONString(message));

    }
    //2、发送消息
    // POST https://api.weixin.qq.com/cgi-bin/message/template/send
    // access_token=ACCESS_TOKEN
    private static void sendPostRequest(String urlString,String jsonBody){
        try{
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            try(OutputStream os =connection.getOutputStream()){
                byte[] bytes = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(bytes,0,bytes.length);
            }

            try(Scanner scanner = new Scanner(connection.getInputStream(),StandardCharsets.UTF_8.name())){
                String response = scanner.useDelimiter("\\A").next();
                System.out.println(response);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    // 代码评审
    public static String codeReview (String code)throws  Exception{
        String apiKeySecret = "";
        String token = BearerTokenUtils.getToken(apiKeySecret);
        URL url = new URL("https://open.bigmodel.cn/api/paas/v4/chat/completions");
        HttpURLConnection connection =(HttpURLConnection) url.openConnection();

        connection.setRequestMethod("post");
        connection.addRequestProperty("Authorization","Bearer "+token);
        connection.addRequestProperty("Content-Type","application/json");
        connection.setRequestProperty("User-Agent","Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        connection.setDoOutput(true);

        //实际修改的代码-先用写死的


        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setModel(Model.GLM_4_FLASH.getCode());
        chatCompletionRequest.setMessages(new ArrayList<ChatCompletionRequest.Prompt>(){
            private static final long serialVersionUID = -7988151926241837899L;
            {
                add(new ChatCompletionRequest.Prompt("user", "你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审。代码如下:"));
                add(new ChatCompletionRequest.Prompt("user", code));
            }
        });

        //相当于把请求体进行序列话，然后添加到表头中作为请求体
        try(OutputStream os =connection.getOutputStream()){
            byte[] bytes = JSON.toJSONString(chatCompletionRequest).getBytes(StandardCharsets.UTF_8);
            os.write(bytes);
        }

        int responseCode = connection.getResponseCode();
        System.out.println(responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;

        StringBuilder content = new StringBuilder();
        while((inputLine=in.readLine())!=null){
            content.append(inputLine);
        }

        in.close();
        connection.disconnect();

        System.out.println("评审结果"+content.toString());
        ChatCompletionSyncResponse chatCompletionSyncResponse = JSON.parseObject(content.toString(), ChatCompletionSyncResponse.class);

        return chatCompletionSyncResponse.getChoices().get(0).getMessage().getContent();

    }

    //编写评审日志
    private static String writeLog(String token,String log)throws Exception{
        Git git = Git.cloneRepository()
                .setURI("")
                .setDirectory(new File("repo"))
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(token, ""))
                .call();

        String dateFolderName = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        File dateFolder = new File("/repo/"+dateFolderName);
        if (!dateFolder.exists()){
            dateFolder.mkdirs();
        }

        String fileName = generateRandomString(12)+".md";
        File newFile = new File(dateFolderName+fileName);
        try(FileWriter writer = new FileWriter(newFile)){
            writer.write(log);
        }

        git.add().addFilepattern(dateFolderName + "/" + fileName).call();
        git.commit().setMessage("Add new file via GitHub Actions").call();
        git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(token, "")).call();

        System.out.println("Changes have been pushed to the repository.");

        return "https://github.com/fuzhengwei/openai-code-review-log/blob/master/" + dateFolderName + "/" + fileName;

    }
    private static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }




}
