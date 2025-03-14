package plus.gaga.middleware.sdk.infrastructrue.feishu;

import com.alibaba.fastjson2.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class FeiShu {
    private final Logger logger = LoggerFactory.getLogger(FeiShu.class);

    private final String appId;
    private final String appSecret;
    private final String receiveId;

    public FeiShu(String appId, String appSecret, String receiveId) {
        this.appId = appId;
        this.appSecret = appSecret;
        this.receiveId = receiveId;
    }

    public void sendMessage(String logUrl) throws Exception {
        // 1. 获取访问令牌
        String accessToken = getAccessToken();

        // 2. 构建消息内容
        Map<String, Object> card = new HashMap<>();
        card.put("config", new HashMap<String, Object>() {{
            put("wide_screen_mode", true);
        }});
        
        card.put("header", new HashMap<String, Object>() {{
            put("title", new HashMap<String, Object>() {{
                put("tag", "plain_text");
                put("content", "代码评审结果通知");
            }});
        }});

        card.put("elements", new Object[]{
            new HashMap<String, Object>() {{
                put("tag", "div");
                put("text", new HashMap<String, Object>() {{
                    put("tag", "lark_md");
                    put("content", "您有新的代码评审结果，请查看详情");
                }});
            }},
            new HashMap<String, Object>() {{
                put("tag", "action");
                put("actions", new Object[]{
                    new HashMap<String, Object>() {{
                        put("tag", "button");
                        put("text", new HashMap<String, Object>() {{
                            put("tag", "plain_text");
                            put("content", "查看详情");
                        }});
                        put("url", logUrl);
                        put("type", "default");
                    }}
                });
            }}
        });

        // 3. 发送消息
        Map<String, Object> body = new HashMap<>();
        body.put("receive_id", receiveId);
        body.put("msg_type", "interactive");
        body.put("content", JSON.toJSONString(card));

        URL url = new URL("https://open.feishu.cn/open-apis/im/v1/messages?receive_id_type=chat_id");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = JSON.toJSONString(body).getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8.name())) {
            String response = scanner.useDelimiter("\\A").next();
            logger.info("openai-code-review feishu message response: {}", response);
        }
    }

    /**
     * 发送带有更多信息的消息
     * @param logUrl 日志URL
     * @param data 额外数据
     * @throws Exception 异常
     */
    public void sendMessage(String logUrl, Map<String, Map<String, String>> data) throws Exception {
        // 1. 获取访问令牌
        String accessToken = getAccessToken();

        // 2. 构建消息内容
        Map<String, Object> card = new HashMap<>();
        card.put("config", new HashMap<String, Object>() {{
            put("wide_screen_mode", true);
        }});
        
        card.put("header", new HashMap<String, Object>() {{
            put("title", new HashMap<String, Object>() {{
                put("tag", "plain_text");
                put("content", "代码评审结果通知");
            }});
        }});

        // 构建元素列表
        Object[] elements = new Object[data.size() + 2]; // +2 是为了添加描述和按钮
        
        // 添加描述
        elements[0] = new HashMap<String, Object>() {{
            put("tag", "div");
            put("text", new HashMap<String, Object>() {{
                put("tag", "lark_md");
                put("content", "您有新的代码评审结果，请查看详情");
            }});
        }};
        
        // 添加数据字段
        int index = 1;
        for (Map.Entry<String, Map<String, String>> entry : data.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue().get("value");
            
            elements[index++] = new HashMap<String, Object>() {{
                put("tag", "div");
                put("fields", new Object[]{
                    new HashMap<String, Object>() {{
                        put("is_short", true);
                        put("text", new HashMap<String, Object>() {{
                            put("tag", "lark_md");
                            put("content", "**" + key + "**");
                        }});
                    }},
                    new HashMap<String, Object>() {{
                        put("is_short", true);
                        put("text", new HashMap<String, Object>() {{
                            put("tag", "lark_md");
                            put("content", value);
                        }});
                    }}
                });
            }};
        }
        
        // 添加按钮
        elements[elements.length - 1] = new HashMap<String, Object>() {{
            put("tag", "action");
            put("actions", new Object[]{
                new HashMap<String, Object>() {{
                    put("tag", "button");
                    put("text", new HashMap<String, Object>() {{
                        put("tag", "plain_text");
                        put("content", "查看详情");
                    }});
                    put("url", logUrl);
                    put("type", "default");
                }}
            });
        }};
        
        card.put("elements", elements);

        // 3. 发送消息
        Map<String, Object> body = new HashMap<>();
        body.put("receive_id", receiveId);
        body.put("msg_type", "interactive");
        body.put("content", JSON.toJSONString(card));

        URL url = new URL("https://open.feishu.cn/open-apis/im/v1/messages?receive_id_type=chat_id");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = JSON.toJSONString(body).getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8.name())) {
            String response = scanner.useDelimiter("\\A").next();
            logger.info("openai-code-review feishu message response: {}", response);
        }
    }

    public String getAccessToken() throws Exception {
        URL url = new URL("https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setDoOutput(true);

        Map<String, String> body = new HashMap<>();
        body.put("app_id", appId);
        body.put("app_secret", appSecret);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = JSON.toJSONString(body).getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8.name())) {
            String response = scanner.useDelimiter("\\A").next();
            Map<String, Object> responseMap = JSON.parseObject(response);
            return (String) responseMap.get("tenant_access_token");
        }
    }

    /**
     * 获取群组列表
     * @return 群组ID列表
     * @throws Exception 异常
     */
    public List<Map<String, Object>> getChatList() throws Exception {
        String accessToken = getAccessToken();
        
        URL url = new URL("https://open.feishu.cn/open-apis/im/v1/chats");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        
        try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8.name())) {
            String response = scanner.useDelimiter("\\A").next();
            logger.info("Get chat list response: {}", response);
            
            Map<String, Object> responseMap = JSON.parseObject(response);
            Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
            return (List<Map<String, Object>>) data.get("items");
        }
    }
    
    /**
     * 创建群组
     * @param name 群组名称
     * @return 群组ID
     * @throws Exception 异常
     */
    public String createChat(String name) throws Exception {
        String accessToken = getAccessToken();
        
        URL url = new URL("https://open.feishu.cn/open-apis/im/v1/chats");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setDoOutput(true);
        
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("description", "代码评审通知群组");
        
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = JSON.toJSONString(body).getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8.name())) {
            String response = scanner.useDelimiter("\\A").next();
            logger.info("Create chat response: {}", response);
            
            Map<String, Object> responseMap = JSON.parseObject(response);
            Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
            return (String) data.get("chat_id");
        }
    }

    /**
     * 使用Webhook发送消息（自定义机器人方式）
     * @param webhookUrl 机器人的Webhook地址
     * @param projectName 项目名称
     * @param logUrl 评审结果地址
     * @throws Exception 异常
     */
    public static void sendWebhookMessage(String webhookUrl, String projectName, String logUrl) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("msg_type", "interactive");
        
        // 构建卡片消息
        Map<String, Object> card = new HashMap<>();
        
        // 卡片配置
        card.put("config", new HashMap<String, Object>() {{
            put("wide_screen_mode", true);
        }});
        
        // 卡片标题
        card.put("header", new HashMap<String, Object>() {{
            put("title", new HashMap<String, Object>() {{
                put("tag", "plain_text");
                put("content", "代码评审结果通知");
            }});
            put("template", "blue");
        }});
        
        // 卡片内容
        card.put("elements", new Object[]{
            // 项目信息
            new HashMap<String, Object>() {{
                put("tag", "div");
                put("text", new HashMap<String, Object>() {{
                    put("tag", "lark_md");
                    put("content", "**项目名称**: " + projectName);
                }});
            }},
            // 评审结果描述
            new HashMap<String, Object>() {{
                put("tag", "div");
                put("text", new HashMap<String, Object>() {{
                    put("tag", "lark_md");
                    put("content", "您的代码已经部署完成，请查看评审结果");
                }});
            }},
            // 分割线
            new HashMap<String, Object>() {{
                put("tag", "hr");
            }},
            // 按钮
            new HashMap<String, Object>() {{
                put("tag", "action");
                put("actions", new Object[]{
                    new HashMap<String, Object>() {{
                        put("tag", "button");
                        put("text", new HashMap<String, Object>() {{
                            put("tag", "plain_text");
                            put("content", "查看评审结果");
                        }});
                        put("url", logUrl);
                        put("type", "primary");
                    }}
                });
            }}
        });
        
        body.put("card", card);
        
        // 发送请求
        URL url = new URL(webhookUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setDoOutput(true);
        
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = JSON.toJSONString(body).getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        // 读取响应
        StringBuilder response = new StringBuilder();
        try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8.name())) {
            while (scanner.hasNextLine()) {
                response.append(scanner.nextLine());
            }
        } catch (Exception e) {
            // 如果出错，尝试读取错误流
            try (Scanner scanner = new Scanner(conn.getErrorStream(), StandardCharsets.UTF_8.name())) {
                while (scanner.hasNextLine()) {
                    response.append(scanner.nextLine());
                }
            }
            throw new RuntimeException("发送消息失败: " + response.toString(), e);
        }
        
        LoggerFactory.getLogger(FeiShu.class).info("飞书机器人响应: {}", response.toString());
    }
} 