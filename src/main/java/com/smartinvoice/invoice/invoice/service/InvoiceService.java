package com.smartinvoice.invoice.invoice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.smartinvoice.invoice.ai.dto.RecognitionResult;
import com.smartinvoice.invoice.invoice.dto.InvoiceUpdateRequest;
import com.smartinvoice.invoice.invoice.entity.Invoice;
import com.smartinvoice.invoice.invoice.mapper.InvoiceMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InvoiceService {

    private final InvoiceMapper invoiceMapper;

    public InvoiceService(InvoiceMapper invoiceMapper) {
        this.invoiceMapper = invoiceMapper;
    }

    // 上传成功后，插入一条「待识别」记录
    public Invoice createPending(String fileId, String filePath, String originalFilename) {
        Invoice invoice = new Invoice();
        invoice.setFileId(fileId);
        invoice.setFilePath(filePath);
        invoice.setOriginalFilename(originalFilename);
        invoice.setStatus("PENDING");
        invoice.setNeedManualReview(false);
        invoice.setCorrected(false);
        invoice.setCreatedAt(LocalDateTime.now());
        invoice.setUpdatedAt(LocalDateTime.now());
        invoiceMapper.insert(invoice);
        return invoice;
    }

    // 查询所有记录，按 id 倒序（最新在前）
    public List<Invoice> listAll() {
        return invoiceMapper.selectList(new QueryWrapper<Invoice>().orderByDesc("id"));
    }

    // 根据 fileId 查询记录
    public Invoice findByFileId(String fileId) {
        return invoiceMapper.selectOne(new QueryWrapper<Invoice>().eq("file_id", fileId));
    }

    // 把识别结果写回记录；识别与分类解耦：先判是不是发票，再判字段完整度
    public String applyRecognitionResult(Long id, RecognitionResult r) {
        boolean notInvoice = (r.getIsInvoice() != null && !r.getIsInvoice());
        boolean partial = (r.getAmount() == null || r.getInvoiceDate() == null || r.getCompany() == null);

        String status;
        boolean needReview;
        if (notInvoice) {
            status = "NOT_INVOICE";
            needReview = false;
        } else if (partial) {
            status = "PARTIAL";
            needReview = true;
        } else {
            status = "SUCCESS";
            needReview = false;
        }

        Invoice invoice = new Invoice();
        invoice.setId(id);
        invoice.setAmount(r.getAmount());
        invoice.setInvoiceDate(r.getInvoiceDate());
        invoice.setCompany(r.getCompany());
        invoice.setCategory(r.getCategory());
        invoice.setConfidence(r.getConfidence());
        invoice.setStatus(status);
        invoice.setNeedManualReview(needReview);
        invoice.setUpdatedAt(LocalDateTime.now());
        invoiceMapper.updateById(invoice);
        return status;
    }

    // 识别失败时标记
    public void markFailed(Long id, String reason) {
        Invoice invoice = new Invoice();
        invoice.setId(id);
        invoice.setStatus("FAILED");
        invoice.setNeedManualReview(true);
        invoice.setRawText(reason);
        invoice.setUpdatedAt(LocalDateTime.now());
        invoiceMapper.updateById(invoice);
    }

    // 详情
    public Invoice getById(Long id) {
        return invoiceMapper.selectById(id);
    }

    // 人工修正：更新金额/日期/公司/分类，标记已人工修正
    public String update(Long id, InvoiceUpdateRequest req) {
        boolean partial = (req.getAmount() == null || req.getInvoiceDate() == null
                || req.getCompany() == null || req.getCompany().trim().isEmpty());
        String status = partial ? "PARTIAL" : "SUCCESS";

        invoiceMapper.update(null, new UpdateWrapper<Invoice>()
                .eq("id", id)
                .set("amount", req.getAmount())
                .set("invoice_date", req.getInvoiceDate())
                .set("company", req.getCompany())
                .set("category", req.getCategory())
                .set("status", status)
                .set("corrected", true)
                .set("need_manual_review", partial)
                .set("updated_at", LocalDateTime.now()));
        return status;
    }

    // 删除记录：先删数据库，再尽力删除磁盘图片
    public void delete(Long id) {
        Invoice invoice = invoiceMapper.selectById(id);
        if (invoice == null) {
            return;
        }
        invoiceMapper.deleteById(id);

        // 尽力删除磁盘图片：删不掉不报错，只留日志
        String filePath = invoice.getFilePath();
        if (filePath != null && !filePath.isBlank()) {
            try {
                File file = new File(filePath);
                if (file.exists()) {
                    file.delete();
                }
            } catch (Exception e) {
                // 图片删除失败不影响记录删除结果，仅记录日志
            }
        }
    }
}
