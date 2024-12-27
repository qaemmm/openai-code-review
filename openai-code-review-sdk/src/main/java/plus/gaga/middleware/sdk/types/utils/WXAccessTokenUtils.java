package plus.gaga.middleware.sdk.types.utils;

import com.alibaba.fastjson2.JSON;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

//1、获取token
//https请求方式: GET https://api.weixin.qq.com/cgi-bin/token
// grant_type=client_credential&appid=APPID&secret=APPSECRET


public class WXAccessTokenUtils {
    private static final String APPID  ="wx0076ab8a1d922248";
    private static final String SECRET="23eca4792ecb94bc7682a6c2dffdf2d3";
    private static final String GRANT_TYPE="client_credential";
    private static final String URL_TEMPLATE = "https://api.weixin.qq.com/cgi-bin/token?grant_type=%s&&appid=%s&secret=%s";

    public static String getAccessToken(){
        String urlString = String.format(URL_TEMPLATE, GRANT_TYPE, APPID, SECRET);
        try {
            URL url  = new URL(urlString);
            HttpURLConnection connection =(HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            //请求成功
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code"+responseCode);

            if(responseCode==HttpURLConnection.HTTP_OK){
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while((inputLine=in.readLine())!=null){
                    content.append(inputLine);
                }
                in.close();
                connection.disconnect();
                System.out.println("Response:"+content.toString());
                Token token = JSON.parseObject(content.toString(), Token.class);
                return token.getAccess_token();
            }else{
                System.out.println("get请求失败");
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }


    public static class Token{
        private String access_token;
        private Integer expires_in;
        public String getAccess_token() {
            return access_token;
        }

        public void setAccess_token(String access_token) {
            this.access_token = access_token;
        }

        public Integer getExpires_in() {
            return expires_in;
        }

        public void setExpires_in(Integer expires_in) {
            this.expires_in = expires_in;
        }

    }




}
