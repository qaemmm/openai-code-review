package plus.gaga.middleware.sdk.infrastructrue.ollama.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.UriComponentsBuilder;
import plus.gaga.middleware.sdk.infrastructrue.ollama.IOllama;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

//@Service
@Slf4j
public class Ollama implements IOllama {
    private final String OLLAMA_API_URL = "http://localhost:8090/api/v1/ollama/generate_stream_rag";
    private final ObjectMapper objectMapper;
    private static final int MAX_RETRIES = 3;
    private static final int CONNECT_TIMEOUT = 5000; // 5 seconds
    private static final int READ_TIMEOUT = 30000;   // 30 seconds

    public Ollama() {
        this.objectMapper = new ObjectMapper();
    }

    public String generateStreamRag(String model, String ragTag, String message) {
        log.info("Generating stream Rag begin with model: {}, ragTag: {}, message: {}", model, ragTag, message);
        StringBuilder contentBuilder = new StringBuilder();
        int retryCount = 0;
        Exception lastException = null;

        while (retryCount < MAX_RETRIES) {
            try {
                // 替换 UriComponentsBuilder 的核心代码段
                String encodedModel = URLEncoder.encode(model, StandardCharsets.UTF_8);
                String encodedRagTag = URLEncoder.encode(ragTag, StandardCharsets.UTF_8);
                String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);

                String urlString = OLLAMA_API_URL
                        + "?model=" + encodedModel
                        + "&ragTag=" + encodedRagTag
                        + "&message=" + encodedMessage;

//                log.info("Requesting URL: {}", urlString);

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("GET");
                conn.setConnectTimeout(CONNECT_TIMEOUT);
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setRequestProperty("Accept", "text/event-stream");
                conn.setRequestProperty("Cache-Control", "no-cache");
                conn.setRequestProperty("Connection", "keep-alive");

                int responseCode = conn.getResponseCode();
                log.info("Response code: {}", responseCode);

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    String errorMessage = readErrorStream(conn);
                    log.error("Error response: {}", errorMessage);
                    throw new RuntimeException("Server returned error code: " + responseCode + ", message: " + errorMessage);
                }

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    log.info("Starting to read response stream...");

                    while ((line = reader.readLine()) != null) {
                        log.info("Raw line received: {}", line);

                        if (line.trim().isEmpty()) {
                            continue;
                        }

                        if (line.startsWith("data:")) {

                            String jsonData = line.substring(5);
                            log.info("Processing SSE data: {}", jsonData);

                            try {
                                JsonNode resultNode = objectMapper.readTree(jsonData);
                                JsonNode outputNode = resultNode.path("result").path("output");
                                String content = outputNode.path("content").asText("");

                                log.info("Extracted content: {}", content);

                                if (!content.isEmpty()) {
                                    contentBuilder.append(content);
                                    log.info("Current accumulated content: {}", contentBuilder);
                                }

                                String finishReason = resultNode.path("result").path("metadata").path("finishReason").asText();
                                log.info("Finish reason: {}", finishReason);

                                if ("STOP".equals(finishReason)) {
                                    log.info("Stream completed with STOP reason");
                                    break;
                                }
                            } catch (Exception e) {
                                log.error("Error parsing SSE data: {}", jsonData, e);
                                throw e;
                            }
                        }
                    }
                    log.info("Finished reading response stream");
                }

                conn.disconnect();
                break;

            } catch (Exception e) {
                lastException = e;
                retryCount++;
                log.error("Error in attempt {}: {}", retryCount, e.getMessage(), e);

                if (retryCount < MAX_RETRIES) {
                    try {
                        Thread.sleep(1000 * retryCount);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted during retry wait", ie);
                    }
                }
            }
        }

        if (retryCount == MAX_RETRIES && lastException != null) {
            throw new RuntimeException("Failed after " + MAX_RETRIES + " attempts", lastException);
        }

        String finalContent = contentBuilder.toString()
                .replaceAll("<think>\\s*</think>", "")
                .replaceAll("<think>", "")
                .replaceAll("</think>", "")
                .trim();

        log.info("Generating stream Rag over, final content length: {}, content: {}",
                finalContent.length(), finalContent);
        return finalContent;
    }

    private String readErrorStream(HttpURLConnection conn) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
            StringBuilder error = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                error.append(line);
            }
            return error.toString();
        } catch (Exception e) {
            return "Could not read error stream: " + e.getMessage();
        }
    }
}
