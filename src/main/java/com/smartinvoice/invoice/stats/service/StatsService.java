package com.smartinvoice.invoice.stats.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.smartinvoice.invoice.invoice.entity.Invoice;
import com.smartinvoice.invoice.invoice.mapper.InvoiceMapper;
import com.smartinvoice.invoice.stats.dto.CategoryStat;
import com.smartinvoice.invoice.stats.dto.MonthlyStat;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatsService {

    private final InvoiceMapper invoiceMapper;

    public StatsService(InvoiceMapper invoiceMapper) {
        this.invoiceMapper = invoiceMapper;
    }

    // 分类中文名映射
    private static final Map<String, String> CATEGORY_NAMES = Map.of(
            "DINING", "餐饮",
            "TRANSPORT", "交通",
            "SHOPPING", "购物",
            "OFFICE", "办公",
            "RESEARCH", "科研",
            "LODGING", "住宿",
            "CONFERENCE", "会议培训",
            "OTHER", "其他"
    );

    // 查所有有效记录：状态是 SUCCESS/PARTIAL，且金额不为空
    private List<Invoice> validInvoices() {
        return invoiceMapper.selectList(new QueryWrapper<Invoice>()
                .in("status", "SUCCESS", "PARTIAL")
                .isNotNull("amount"));
    }

    // 分类汇总：每个分类的金额合计、笔数、占比
    public List<CategoryStat> categorySummary() {
        List<Invoice> list = validInvoices();

        BigDecimal total = list.stream()
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, List<Invoice>> grouped = list.stream()
                .collect(Collectors.groupingBy(inv -> inv.getCategory() == null ? "OTHER" : inv.getCategory()));

        List<CategoryStat> result = new ArrayList<>();
        for (Map.Entry<String, List<Invoice>> e : grouped.entrySet()) {
            String category = e.getKey();
            List<Invoice> invoices = e.getValue();

            BigDecimal sum = invoices.stream()
                    .map(Invoice::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            CategoryStat stat = new CategoryStat();
            stat.setCategory(category);
            stat.setCategoryName(CATEGORY_NAMES.getOrDefault(category, "其他"));
            stat.setTotalAmount(sum);
            stat.setCount((long) invoices.size());
            if (total.compareTo(BigDecimal.ZERO) > 0) {
                stat.setPercentage(sum.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP));
            } else {
                stat.setPercentage(BigDecimal.ZERO);
            }
            result.add(stat);
        }

        // 按金额从大到小排序
        result.sort((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount()));
        return result;
    }

    // 月度汇总：每个月的金额合计、笔数
    public List<MonthlyStat> monthlySummary() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");

        List<Invoice> list = validInvoices().stream()
                .filter(inv -> inv.getInvoiceDate() != null)
                .collect(Collectors.toList());

        Map<String, List<Invoice>> grouped = list.stream()
                .collect(Collectors.groupingBy(inv -> inv.getInvoiceDate().format(fmt)));

        List<MonthlyStat> result = new ArrayList<>();
        for (Map.Entry<String, List<Invoice>> e : grouped.entrySet()) {
            BigDecimal sum = e.getValue().stream()
                    .map(Invoice::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            MonthlyStat stat = new MonthlyStat();
            stat.setMonth(e.getKey());
            stat.setTotalAmount(sum);
            stat.setCount((long) e.getValue().size());
            result.add(stat);
        }

        // 按月份升序
        result.sort(Comparator.comparing(MonthlyStat::getMonth));
        return result;
    }
}
