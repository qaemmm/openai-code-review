package plus.gaga.middleware.sdk.test;

import com.alibaba.fastjson2.JSON;
import org.checkerframework.checker.units.qual.C;
import org.junit.Test;
import plus.gaga.middleware.sdk.domain.model.ChatCompletionRequest;
import plus.gaga.middleware.sdk.domain.model.ChatCompletionSyncResponse;
import plus.gaga.middleware.sdk.domain.model.Model;
import plus.gaga.middleware.sdk.types.utils.BearerTokenUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ApiTest {

    public static void main(String[] args) throws Exception {
        String token = BearerTokenUtils.getToken("d71192a73ed6573bb1753f943907082d.sAHeF3Ux0x1RVFtb");

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

    }



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
}
