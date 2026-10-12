package com.smartinvoice.invoice.ai.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class RecognitionResult {

    private BigDecimal amount;
    private LocalDate invoiceDate;
    private String company;
    private String category;
    private BigDecimal confidence;
    private Boolean isInvoice;

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

    public Boolean getIsInvoice() { return isInvoice; }
    public void setIsInvoice(Boolean isInvoice) { this.isInvoice = isInvoice; }
}
