package plus.gaga.middleware.sdk.test;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import plus.gaga.middleware.sdk.domain.model.Message;
import plus.gaga.middleware.sdk.infrastructrue.ollama.IOllama;
import plus.gaga.middleware.sdk.infrastructrue.ollama.impl.Ollama;
import plus.gaga.middleware.sdk.types.utils.BearerTokenUtils;
import plus.gaga.middleware.sdk.types.utils.WXAccessTokenUtils;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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


}
