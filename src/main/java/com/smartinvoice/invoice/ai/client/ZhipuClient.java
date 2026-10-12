package com.smartinvoice.invoice.ai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ZhipuClient {

    private final String apiKey;
    private final String model;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public ZhipuClient(@Value("${ai.zhipu.base-url}") String baseUrl,
                       @Value("${ai.zhipu.api-key}") String apiKey,
                       @Value("${ai.zhipu.model}") String model,
                       ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.model = model;
        this.objectMapper = objectMapper;

        // 设置连接超时 10 秒、读取超时 60 秒（AI 识别可能较慢）
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(60000);

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    // 调用智谱接口，返回模型输出的文本
    public String chat(String prompt, String imageDataUrl) {
        Map<String, Object> body = buildBody(prompt, imageDataUrl);
        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new RuntimeException("构造请求失败", e);
        }

        String response;
        try {
            response = restClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonBody)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException e) {
            // 把接口返回的错误信息带出来，方便排查（比如 key 无效、限流等）
            throw new RuntimeException("调用智谱接口失败: " + e.getStatusCode() + " " + e.getResponseBodyAsString(), e);
        }

        return extractContent(response);
    }

    private Map<String, Object> buildBody(String prompt, String imageDataUrl) {
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("type", "text");
        textPart.put("text", prompt);

        Map<String, Object> imageUrl = new HashMap<>();
        imageUrl.put("url", imageDataUrl);

        Map<String, Object> imagePart = new HashMap<>();
        imagePart.put("type", "image_url");
        imagePart.put("image_url", imageUrl);

        List<Object> content = new ArrayList<>();
        content.add(textPart);
        content.add(imagePart);

        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", content);

        List<Object> messages = new ArrayList<>();
        messages.add(message);

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("temperature", 0.1);
        return body;
    }

    private String extractContent(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            throw new RuntimeException("解析模型响应失败", e);
        }
    }
}
