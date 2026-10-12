package com.smartinvoice.invoice.invoice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("invoice")
public class Invoice {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String fileId;
    private String filePath;
    private String originalFilename;
    private BigDecimal amount;
    private LocalDate invoiceDate;
    private String company;
    private String category;
    private BigDecimal confidence;
    private String status;
    private Boolean needManualReview;
    private Boolean corrected;
    private String rawText;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileId() { return fileId; }
    public void setFileId(String fileId) { this.fileId = fileId; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDate getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(LocalDate invoiceDate) { this.invoiceDate = invoiceDate; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getConfidence() { return confidence; }
    public void setConfidence(BigDecimal confidence) { this.confidence = confidence; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Boolean getNeedManualReview() { return needManualReview; }
    public void setNeedManualReview(Boolean needManualReview) { this.needManualReview = needManualReview; }

    public Boolean getCorrected() { return corrected; }
    public void setCorrected(Boolean corrected) { this.corrected = corrected; }

    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
