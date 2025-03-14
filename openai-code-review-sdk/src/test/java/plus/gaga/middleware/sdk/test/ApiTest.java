package plus.gaga.middleware.sdk.test;

import com.alibaba.fastjson2.JSON;
import org.junit.Test;
import plus.gaga.middleware.sdk.domain.model.Message;
import plus.gaga.middleware.sdk.infrastructrue.feishu.FeiShu;
import plus.gaga.middleware.sdk.infrastructrue.ollama.impl.Ollama;
import plus.gaga.middleware.sdk.types.utils.BearerTokenUtils;
import plus.gaga.middleware.sdk.types.utils.WXAccessTokenUtils;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ApiTest {
    @Test
    public void test_wx(){
        String accessToken = WXAccessTokenUtils.getAccessToken();
        System.out.println(accessToken);
        Message message = new Message();
        message.put("auditTime","20241230");
        message.put("message","测试消息");
        String url = String.format("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=%s", accessToken);
        sendPostRequest(url, JSON.toJSONString(message));

    }

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

    @Test
    public void test_chatglm(){
        String token = BearerTokenUtils.getToken("f8a97a5577574c2890797c0152674787.ffQHG2cx9V6xBMrC");
        System.out.println(token);
    }

//    private IOllama ollama;

    @Test
    public void test_ollama(){
        Ollama ollama = new Ollama();
        String model = "deepseek-r1:1.5b";
        String ragTag = "111";
        String message = "1+1";

        String s = ollama.generateStreamRag(model, ragTag, message);
        System.out.println(s);
    }


    @Test
    public void test_feishu_api() throws Exception {
         String feishu_app_id = "cli_a739ffefce64900e";
         String feishu_app_secret = "T4jytnXOwXmnUyyzLwt7ffBnCmGvcl0s";
         String feishu_receive_id = "";
         FeiShu feishu = new FeiShu(feishu_app_id, feishu_app_secret, feishu_receive_id);
         String accessToken = feishu.getAccessToken();
         System.out.println("Access Token: " + accessToken);
    }
    
    @Test
    public void test_feishu_create_chat() throws Exception {
         String feishu_app_id = "cli_a739ffefce64900e";
         String feishu_app_secret = "T4jytnXOwXmnUyyzLwt7ffBnCmGvcl0s";
         String feishu_receive_id = "";
         FeiShu feishu = new FeiShu(feishu_app_id, feishu_app_secret, feishu_receive_id);
         
         // 创建一个新的群组
         String chatId = feishu.createChat("代码评审通知群");
         System.out.println("新创建的群组ID: " + chatId);
         //oc_29a173f7a053760e4faae53721a04fd6
         // 保存这个chatId作为feishu_receive_id使用
    }
    
    @Test
    public void test_feishu_get_chat_list() throws Exception {
         String feishu_app_id = "cli_a739ffefce64900e";
         String feishu_app_secret = "T4jytnXOwXmnUyyzLwt7ffBnCmGvcl0s";
         String feishu_receive_id = "oc_29a173f7a053760e4faae53721a04fd6";
         FeiShu feishu = new FeiShu(feishu_app_id, feishu_app_secret, feishu_receive_id);
         
         // 获取所有群组列表
         List<Map<String, Object>> chatList = feishu.getChatList();
         System.out.println("群组列表:");
         for (Map<String, Object> chat : chatList) {
             System.out.println("群组名称: " + chat.get("name") + ", 群组ID: " + chat.get("chat_id"));
         }
         
         // 从列表中选择一个群组ID作为feishu_receive_id使用
    }
    
    @Test
    public void test_feishu_send_message() throws Exception {
         String feishu_app_id = "cli_a739ffefce64900e";
         String feishu_app_secret = "T4jytnXOwXmnUyyzLwt7ffBnCmGvcl0s";
         // 这里填入上面测试获取到的群组ID
         String feishu_receive_id = "oc_29a173f7a053760e4faae53721a04fd6";
         FeiShu feishu = new FeiShu(feishu_app_id, feishu_app_secret, feishu_receive_id);
         
         // 测试发送消息
         feishu.sendMessage("https://www.baidu.com/");
    }

    @Test
    public void test_feishu_webhook() throws Exception {
        // 您的自定义机器人Webhook地址
        String webhookUrl = "https://open.feishu.cn/open-apis/bot/v2/hook/92ed993d-cd15-4b5a-a2aa-21e9cd899134";
        // 项目名称
        String projectName = "openai-code-review";
        // 评审结果地址
        String logUrl = "baidu.com";
        
        // 发送消息
        FeiShu.sendWebhookMessage(webhookUrl, projectName, logUrl);
        System.out.println("消息已发送，请在飞书群组中查看");
    }

}
