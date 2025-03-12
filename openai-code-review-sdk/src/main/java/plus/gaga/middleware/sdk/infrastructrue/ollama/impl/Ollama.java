package plus.gaga.middleware.sdk.infrastructrue.ollama.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import plus.gaga.middleware.sdk.infrastructrue.ollama.IOllama;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

//@Service
public class Ollama implements IOllama {

    private final String OLLAMA_API_URL = "http://localhost:8090/api/v1/ollama/generate_stream";
    private final ObjectMapper objectMapper;

    public Ollama() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String generateStreamRag(String model, String ragTag, String message) {
        StringBuilder contentBuilder = new StringBuilder();
        
        try {
            // 构建URL with parameters
            String encodedModel = URLEncoder.encode(model, StandardCharsets.UTF_8.toString());
            String encodedRagTag = URLEncoder.encode(ragTag, StandardCharsets.UTF_8.toString());
            String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8.toString());
            String urlString = String.format("%s?model=%s&ragTag=%s&message=%s",
                    OLLAMA_API_URL, encodedModel, encodedRagTag, encodedMessage);
            
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            
            // 读取响应
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            List<String> responseLines = new ArrayList<>();
            
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    responseLines.add(line);
                }
            }
            reader.close();
            
            // 处理每一行响应
            for (String responseLine : responseLines) {
                try {
                    JsonNode resultNode = objectMapper.readTree(responseLine);
                    JsonNode outputNode = resultNode.path("result").path("output");
                    String content = outputNode.path("content").asText("");
                    
                    if (!content.isEmpty()) {
                        contentBuilder.append(content);
                    }
                    
                    // 检查是否结束
                    String finishReason = resultNode.path("result").path("metadata").path("finishReason").asText();
                    if ("unknown".equals(finishReason)) {
                        break;
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Error processing response line: " + responseLine, e);
                }
            }
            
            conn.disconnect();
            
        } catch (Exception e) {
            throw new RuntimeException("Error making HTTP request", e);
        }
        
        // 处理返回结果，去掉<think></think>标签
        return contentBuilder.toString()
                .replaceAll("<think>\\s*</think>", "") // 移除空的think标签
                .replaceAll("<think>", "") // 移除开始标签
                .replaceAll("</think>", "") // 移除结束标签
                .trim();
    }
}
