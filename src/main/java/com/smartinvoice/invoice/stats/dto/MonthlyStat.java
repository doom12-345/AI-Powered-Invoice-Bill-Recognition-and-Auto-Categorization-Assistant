package com.smartinvoice.invoice.stats.dto;

import java.math.BigDecimal;

public class MonthlyStat {

    private String month;          // 如 2026-10
    private BigDecimal totalAmount;
    private Long count;

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public Long getCount() { return count; }
    public void setCount(Long count) { this.count = count; }
}
