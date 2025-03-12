package plus.gaga.middleware.sdk.infrastructrue.ollama.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import plus.gaga.middleware.sdk.infrastructrue.ollama.IOllama;

//@Service
public class Ollama implements IOllama {

    private final String OLLAMA_API_URL = "http://localhost:8090/api/v1/ollama/generate_stream";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public Ollama() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    @SuppressWarnings("unchecked")
    public String generateStreamRag(String model, String ragTag, String message) {
        StringBuilder contentBuilder = new StringBuilder();
        
        String url = UriComponentsBuilder.fromHttpUrl(OLLAMA_API_URL)
            .queryParam("model", model)
            .queryParam("ragTag", ragTag)
            .queryParam("message", message)
            .toUriString();

        ResponseEntity<Object[]> response = restTemplate.getForEntity(url, Object[].class);
        Object[] responses = response.getBody();
        
        if (responses != null) {
            for (Object chatResponse : responses) {
                try {
                    JsonNode resultNode = objectMapper.valueToTree(chatResponse);
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
                    throw new RuntimeException("Error processing response", e);
                }
            }
        }
        
        // 处理返回结果，去掉<think></think>标签
        return contentBuilder.toString()
                .replaceAll("<think>\\s*</think>", "") // 移除空的think标签
                .replaceAll("<think>", "") // 移除开始标签
                .replaceAll("</think>", "") // 移除结束标签
                .trim();
    }
}
