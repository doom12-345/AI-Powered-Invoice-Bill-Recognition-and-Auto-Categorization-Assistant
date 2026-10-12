package com.smartinvoice.invoice.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartinvoice.invoice.ai.client.ZhipuClient;
import com.smartinvoice.invoice.ai.dto.RecognitionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Locale;

@Service
public class RecognitionService {

    private static final Logger log = LoggerFactory.getLogger(RecognitionService.class);

    private final ZhipuClient zhipuClient;
    private final ObjectMapper objectMapper;

    public RecognitionService(ZhipuClient zhipuClient, ObjectMapper objectMapper) {
        this.zhipuClient = zhipuClient;
        this.objectMapper = objectMapper;
    }

    // 根据本地文件路径识别发票
    public RecognitionResult recognize(String filePath) {
        byte[] bytes = readImage(filePath);
        String mime = detectMime(filePath);
        String base64 = Base64.getEncoder().encodeToString(bytes);
        String dataUrl = "data:" + mime + ";base64," + base64;

        String content = zhipuClient.chat(buildPrompt(), dataUrl);
        return parseResult(content);
    }

    private byte[] readImage(String filePath) {
        try {
            return Files.readAllBytes(Path.of(filePath));
        } catch (IOException e) {
            throw new RuntimeException("读取图片失败: " + filePath, e);
        }
    }

    private String detectMime(String filePath) {
        String lower = filePath.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".gif")) return "image/gif";
        return "image/jpeg";
    }

    private String buildPrompt() {
        return "你是发票信息抽取助手。请先判断这张图片是否是一张发票或可报销的票据（如小票、车票、行程单等）。只返回一个 JSON 对象，不要任何多余文字或解释：\n"
                + "{\n"
                + "  \"isInvoice\": true 或 false，表示这张图是不是发票/可报销票据,\n"
                + "  \"amount\": 发票总金额的数字（不含货币符号，如 128.5），看不清或不是发票填 null,\n"
                + "  \"date\": 开票日期，格式 \"YYYY-MM-DD\"，看不清或不是发票填 null,\n"
                + "  \"company\": 公司名或商户名，看不清或不是发票填 null,\n"
                + "  \"category\": 分类，只能填以下之一：DINING(餐饮) / TRANSPORT(交通) / SHOPPING(购物) / OFFICE(办公) / RESEARCH(科研) / LODGING(住宿) / CONFERENCE(会议培训) / OTHER(其他),\n"
                + "  \"confidence\": 0 到 1 之间的数字，表示你这次抽取的总体置信度\n"
                + "}\n"
                + "分类规则：买书/教材/文具/打印/办公耗材归 OFFICE；试剂/实验室耗材/实验设备/论文版面费/润色归 RESEARCH；火车票/机票/打车归 TRANSPORT，酒店归 LODGING；会议注册费/培训费归 CONFERENCE。\n"
                + "如果不是发票，isInvoice 填 false，其余字段全部填 null。";
    }

    private RecognitionResult parseResult(String content) {
        String json = extractJson(content);
        RecognitionResult result = new RecognitionResult();
        try {
            JsonNode root = objectMapper.readTree(json);
            result.setAmount(readAmount(root));
            result.setInvoiceDate(readDate(root));
            result.setCompany(readText(root, "company"));
            result.setCategory(readCategory(root));
            result.setConfidence(readConfidence(root));
            result.setIsInvoice(readBoolean(root, "isInvoice"));
        } catch (Exception e) {
            log.error("解析识别结果失败，原始内容: {}", json, e);
            throw new RuntimeException("解析 AI 识别结果失败", e);
        }
        return result;
    }

    // 剥离可能的 ```json 代码块，截取第一个 { 到最后一个 }
    private String extractJson(String content) {
        String s = content == null ? "" : content.trim();
        int start = s.indexOf("{");
        int end = s.lastIndexOf("}");
        if (start >= 0 && end > start) {
            return s.substring(start, end + 1);
        }
        return s;
    }

    private BigDecimal readAmount(JsonNode root) {
        JsonNode node = root.get("amount");
        if (node == null || node.isNull()) return null;
        try {
            return new BigDecimal(node.asText().trim());
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDate readDate(JsonNode root) {
        JsonNode node = root.get("date");
        if (node == null || node.isNull()) return null;
        try {
            return LocalDate.parse(node.asText().trim());
        } catch (Exception e) {
            return null;
        }
    }

    private String readText(JsonNode root, String field) {
        JsonNode node = root.get(field);
        if (node == null || node.isNull()) return null;
        String s = node.asText().trim();
        return s.isEmpty() ? null : s;
    }

    private String readCategory(JsonNode root) {
        JsonNode node = root.get("category");
        if (node == null || node.isNull()) return "OTHER";
        String s = node.asText().trim().toUpperCase(Locale.ROOT);
        if (s.equals("DINING") || s.equals("TRANSPORT") || s.equals("SHOPPING")
                || s.equals("OFFICE") || s.equals("RESEARCH") || s.equals("LODGING")
                || s.equals("CONFERENCE")) {
            return s;
        }
        return "OTHER";
    }

    private BigDecimal readConfidence(JsonNode root) {
        JsonNode node = root.get("confidence");
        if (node == null || node.isNull()) return null;
        try {
            return new BigDecimal(node.asText().trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Boolean readBoolean(JsonNode root, String field) {
        JsonNode node = root.get(field);
        if (node == null || node.isNull()) return null;
        if (node.isBoolean()) return node.asBoolean();
        String s = node.asText().trim().toLowerCase(Locale.ROOT);
        if (s.equals("true")) return true;
        if (s.equals("false")) return false;
        return null;
    }
}
