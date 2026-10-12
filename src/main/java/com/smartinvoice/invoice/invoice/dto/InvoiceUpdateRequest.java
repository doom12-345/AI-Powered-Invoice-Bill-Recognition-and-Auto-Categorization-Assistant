package com.smartinvoice.invoice.invoice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class InvoiceUpdateRequest {

    private BigDecimal amount;
    private LocalDate invoiceDate;   // 格式 YYYY-MM-DD
    private String company;
    private String category;

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDate getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(LocalDate invoiceDate) { this.invoiceDate = invoiceDate; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
