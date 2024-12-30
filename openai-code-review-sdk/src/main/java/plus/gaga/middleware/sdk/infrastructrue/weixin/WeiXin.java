package plus.gaga.middleware.sdk.infrastructrue.weixin;

import com.alibaba.fastjson2.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import plus.gaga.middleware.sdk.infrastructrue.weixin.dto.TemplateMessageDTO;
import plus.gaga.middleware.sdk.types.utils.WXAccessTokenUtils;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;


public class WeiXin {

    private final Logger logger
            = LoggerFactory.getLogger(WeiXin.class);

    //appid、secret、touser、template_id;
    private final String appid;
    private final String secret;
    private final String touser;
    private final String template_id;
    public WeiXin(String appid,String secret,String touser,String template_id){
        this.appid = appid;
        this.secret = secret;
        this.touser = touser;
        this.template_id = template_id;
    }

    public void sendTemplateMessage(String logUrl, Map<String,Map<String,String>> data) throws Exception{
        String accessToken = WXAccessTokenUtils.getToken(appid,secret);

        //获取对应的token，将Map数据封装成TemplateMessageDTO
        TemplateMessageDTO templateMessageDTO = new TemplateMessageDTO(touser,template_id);
        templateMessageDTO.setUrl(logUrl);
        templateMessageDTO.setData(data);
        URL url = new URL(String.format("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=%s",accessToken));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);

        //将templateMessageDTO转换为二进制数据写入请求
        try(OutputStream os = conn.getOutputStream()){
            byte[] input = JSON.toJSONString(templateMessageDTO).getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        //读取响应数据。
        try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8.name())) {
            String response = scanner.useDelimiter("\\A").next();
            logger.info("openai-code-review weixin template message! {}", response);
        }

    }
}
