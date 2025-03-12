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
    private static final int MAX_RETRIES = 3;
    private static final int CONNECT_TIMEOUT = 5000; // 5 seconds
    private static final int READ_TIMEOUT = 30000;   // 30 seconds

    public Ollama() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String generateStreamRag(String model, String ragTag, String message) {
        StringBuilder contentBuilder = new StringBuilder();
        int retryCount = 0;
        Exception lastException = null;

        while (retryCount < MAX_RETRIES) {
            try {
                // 构建URL with parameters
                String encodedModel = URLEncoder.encode(model, StandardCharsets.UTF_8.toString());
                String encodedRagTag = URLEncoder.encode(ragTag, StandardCharsets.UTF_8.toString());
                String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8.toString());
                String urlString = String.format("%s?model=%s&ragTag=%s&message=%s",
                        OLLAMA_API_URL, encodedModel, encodedRagTag, encodedMessage);

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                
                // 设置连接参数
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(CONNECT_TIMEOUT);
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-Type", "application/json");

                // 检查响应码
                int responseCode = conn.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new RuntimeException("Server returned error code: " + responseCode);
                }

                // 读取响应
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (!line.trim().isEmpty()) {
                            try {
                                JsonNode resultNode = objectMapper.readTree(line);
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
                                System.err.println("Warning: Failed to process line: " + line);
                                e.printStackTrace();
                                // 继续处理下一行，而不是中断整个过程
                            }
                        }
                    }
                } finally {
                    conn.disconnect();
                }

                // 如果成功处理，跳出重试循环
                break;

            } catch (Exception e) {
                lastException = e;
                retryCount++;
                if (retryCount < MAX_RETRIES) {
                    // 等待一段时间后重试
                    try {
                        Thread.sleep(1000 * retryCount); // 递增等待时间
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted during retry wait", ie);
                    }
                    System.err.println("Retry attempt " + retryCount + " of " + MAX_RETRIES);
                }
            }
        }

        // 如果所有重试都失败了
        if (retryCount == MAX_RETRIES && lastException != null) {
            throw new RuntimeException("Failed after " + MAX_RETRIES + " attempts", lastException);
        }

        // 处理返回结果，去掉<think></think>标签
        return contentBuilder.toString()
                .replaceAll("<think>\\s*</think>", "") // 移除空的think标签
                .replaceAll("<think>", "") // 移除开始标签
                .replaceAll("</think>", "") // 移除结束标签
                .trim();
    }
}
