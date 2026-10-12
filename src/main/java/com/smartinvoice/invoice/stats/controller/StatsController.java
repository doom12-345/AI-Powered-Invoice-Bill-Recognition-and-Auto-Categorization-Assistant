package com.smartinvoice.invoice.stats.controller;

import com.smartinvoice.invoice.stats.service.StatsService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    // 统计页面
    @GetMapping("/stats")
    public String statsPage() {
        return "stats";
    }

    // 分类汇总
    @GetMapping("/api/stats/summary")
    @ResponseBody
    public Map<String, Object> summary() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "success");
        result.put("data", statsService.categorySummary());
        return result;
    }

    // 月度汇总
    @GetMapping("/api/stats/monthly")
    @ResponseBody
    public Map<String, Object> monthly() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "success");
        result.put("data", statsService.monthlySummary());
        return result;
    }
}
