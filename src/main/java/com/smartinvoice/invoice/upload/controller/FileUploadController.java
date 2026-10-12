package com.smartinvoice.invoice.upload.controller;

import com.smartinvoice.invoice.ai.dto.RecognitionResult;
import com.smartinvoice.invoice.ai.service.RecognitionService;
import com.smartinvoice.invoice.invoice.entity.Invoice;
import com.smartinvoice.invoice.invoice.service.InvoiceService;
import com.smartinvoice.invoice.upload.service.FileStorageService;
import com.smartinvoice.invoice.upload.service.StoredFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Controller
public class FileUploadController {

    private static final Logger log = LoggerFactory.getLogger(FileUploadController.class);

    private final FileStorageService fileStorageService;
    private final InvoiceService invoiceService;
    private final RecognitionService recognitionService;

    public FileUploadController(FileStorageService fileStorageService,
                                InvoiceService invoiceService,
                                RecognitionService recognitionService) {
        this.fileStorageService = fileStorageService;
        this.invoiceService = invoiceService;
        this.recognitionService = recognitionService;
    }

    // 访问 http://localhost:8080/ 显示上传页面
    @GetMapping("/")
    public String index() {
        return "upload";
    }

    // 上传图片 → 保存 → AI 识别 → 写回数据库
    @PostMapping("/api/upload")
    @ResponseBody
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) {
        StoredFile stored = fileStorageService.store(file);
        Invoice invoice = invoiceService.createPending(stored.getFileId(), stored.getFilePath(), stored.getOriginalFilename());

        Map<String, Object> data = new HashMap<>();
        data.put("fileId", stored.getFileId());

        Map<String, Object> result = new HashMap<>();
        result.put("data", data);

        try {
            RecognitionResult r = recognitionService.recognize(stored.getFilePath());
            String status = invoiceService.applyRecognitionResult(invoice.getId(), r);
            data.put("status", status);
            data.put("isInvoice", r.getIsInvoice());
            data.put("confidence", r.getConfidence());
            data.put("amount", r.getAmount());
            data.put("invoiceDate", r.getInvoiceDate());
            data.put("company", r.getCompany());
            data.put("category", r.getCategory());
            result.put("code", 0);
            if ("NOT_INVOICE".equals(status)) {
                result.put("message", "上传成功，但这张图可能不是发票，请确认");
            } else if ("PARTIAL".equals(status)) {
                result.put("message", "上传并识别成功，但部分字段缺失，建议人工核对");
            } else {
                result.put("message", "上传并识别成功");
            }
        } catch (Exception e) {
            log.error("AI 识别失败", e);
            invoiceService.markFailed(invoice.getId(), "AI 识别失败");
            result.put("code", 0);
            result.put("message", "上传成功，但 AI 识别失败（图片可能不清晰或不是发票），请重试或人工核对");
        }
        return result;
    }

    // 独立调试接口：根据 fileId 重新识别
    @PostMapping("/api/ai/recognize")
    @ResponseBody
    public Map<String, Object> recognize(@RequestParam("fileId") String fileId) {
        Invoice invoice = invoiceService.findByFileId(fileId);
        Map<String, Object> result = new HashMap<>();
        if (invoice == null) {
            result.put("code", 1);
            result.put("message", "未找到该 fileId 对应的记录");
            result.put("data", null);
            return result;
        }
        RecognitionResult r = recognitionService.recognize(invoice.getFilePath());
        invoiceService.applyRecognitionResult(invoice.getId(), r);
        result.put("code", 0);
        result.put("message", "success");
        result.put("data", r);
        return result;
    }

}
