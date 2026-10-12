package com.smartinvoice.invoice.stats.dto;

import java.math.BigDecimal;

public class CategoryStat {

    private String category;       // 枚举值，如 DINING
    private String categoryName;   // 中文名，如 餐饮
    private BigDecimal totalAmount;
    private Long count;
    private BigDecimal percentage; // 占比，0~100

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public Long getCount() { return count; }
    public void setCount(Long count) { this.count = count; }

    public BigDecimal getPercentage() { return percentage; }
    public void setPercentage(BigDecimal percentage) { this.percentage = percentage; }
}
