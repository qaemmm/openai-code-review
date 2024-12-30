package plus.gaga.middleware.sdk.test;

import com.alibaba.fastjson2.JSON;
import org.junit.Test;
import plus.gaga.middleware.sdk.domain.model.Message;
import plus.gaga.middleware.sdk.types.utils.BearerTokenUtils;
import plus.gaga.middleware.sdk.types.utils.WXAccessTokenUtils;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ApiTest {
//    @Test
//    public void test_wx(){
//        String accessToken = WXAccessTokenUtils.getAccessToken();
//        System.out.println(accessToken);
//        Message message = new Message();
//        message.put("auditTime","20241230");
//        message.put("message","测试消息");
//        String url = String.format("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=%s", accessToken);
//        sendPostRequest(url, JSON.toJSONString(message));
//
//    }
//
//    private static void sendPostRequest(String urlString,String jsonBody){
//        try{
//            URL url = new URL(urlString);
//            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
//            connection.setRequestMethod("POST");
//            connection.setRequestProperty("Content-Type", "application/json; utf-8");
//            connection.setRequestProperty("Accept", "application/json");
//            connection.setDoOutput(true);
//
//            try(OutputStream os =connection.getOutputStream()){
//                byte[] bytes = jsonBody.getBytes(StandardCharsets.UTF_8);
//                os.write(bytes,0,bytes.length);
//            }
//
//            try(Scanner scanner = new Scanner(connection.getInputStream(),StandardCharsets.UTF_8.name())){
//                String response = scanner.useDelimiter("\\A").next();
//                System.out.println(response);
//            }
//
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void test_chatglm(){
//        String token = BearerTokenUtils.getToken("dcacdd5f69824f4909e278cb3ac175a1.tV8KJedMqQNUKXhd");
//        System.out.println(token);
//    }
}
