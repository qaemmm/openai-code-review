package plus.gaga.middleware.sdk.infrastructrue.openai.impl;

import com.alibaba.fastjson2.JSON;
import plus.gaga.middleware.sdk.infrastructrue.openai.IOpenAI;
import plus.gaga.middleware.sdk.infrastructrue.openai.dto.ChatCompletionRequestDTO;
import plus.gaga.middleware.sdk.infrastructrue.openai.dto.ChatCompletionSyncResponseDTO;
import plus.gaga.middleware.sdk.types.utils.BearerTokenUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;


public class ChatGLM implements IOpenAI {

    private final String apiHost;
    private final String apiKeySecret;

    public ChatGLM(String apiHost, String apiKeySecret) {
        this.apiHost = apiHost;
        this.apiKeySecret = apiKeySecret;
    }

    //操作glm需要地址和秘钥key
    @Override
    public ChatCompletionSyncResponseDTO completions(ChatCompletionRequestDTO requestDTO) throws Exception {
        String token = BearerTokenUtils.getToken(apiHost, apiKeySecret);

        URL url = new URL(apiHost);
        HttpURLConnection connection =(HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("authorization","Bearer "+token);
        connection.setRequestProperty("Content-Type","application/json");
        connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        connection.setDoOutput(true);
        try(OutputStream os = connection.getOutputStream()){
            byte[] bytes = JSON.toJSONString(requestDTO).getBytes(StandardCharsets.UTF_8);
            os.write(bytes,0,bytes.length);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder sb = new StringBuilder();
        while((inputLine = in.readLine())!=null){
            sb.append(inputLine);
        }
        in.close();
        connection.disconnect();
        return JSON.parseObject(sb.toString(), ChatCompletionSyncResponseDTO.class);
    }
}
