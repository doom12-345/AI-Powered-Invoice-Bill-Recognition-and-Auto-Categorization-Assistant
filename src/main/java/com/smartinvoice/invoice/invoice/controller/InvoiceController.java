package com.smartinvoice.invoice.invoice.controller;

import com.smartinvoice.invoice.invoice.dto.InvoiceUpdateRequest;
import com.smartinvoice.invoice.invoice.entity.Invoice;
import com.smartinvoice.invoice.invoice.service.InvoiceService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    // 发票列表页
    @GetMapping("/list")
    public String listPage() {
        return "list";
    }

    // 查询所有发票记录
    @GetMapping("/api/invoice/list")
    @ResponseBody
    public Map<String, Object> list() {
        List<Invoice> list = invoiceService.listAll();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "success");
        result.put("data", list);
        return result;
    }

    // 详情
    @GetMapping("/api/invoice/{id}")
    @ResponseBody
    public Map<String, Object> detail(@PathVariable("id") Long id) {
        Invoice invoice = invoiceService.getById(id);
        Map<String, Object> result = new HashMap<>();
        if (invoice == null) {
            result.put("code", 1);
            result.put("message", "记录不存在");
            result.put("data", null);
            return result;
        }
        result.put("code", 0);
        result.put("message", "success");
        result.put("data", invoice);
        return result;
    }

    // 人工修正
    @PutMapping("/api/invoice/{id}")
    @ResponseBody
    public Map<String, Object> update(@PathVariable("id") Long id,
                                      @RequestBody InvoiceUpdateRequest req) {
        String status = invoiceService.update(id, req);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "修改成功");
        result.put("data", status);
        return result;
    }

    // 删除
    @DeleteMapping("/api/invoice/{id}")
    @ResponseBody
    public Map<String, Object> delete(@PathVariable("id") Long id) {
        invoiceService.delete(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "删除成功");
        result.put("data", null);
        return result;
    }
}
