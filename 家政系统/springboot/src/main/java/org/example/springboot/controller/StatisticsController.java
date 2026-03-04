package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Pattern;
import org.example.springboot.common.Result;
import org.example.springboot.service.StatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "数据统计接口")
@RestController
@RequestMapping("/statistics")
@Validated
public class StatisticsController {
    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsController.class);

    @Resource
    private StatisticsService statisticsService;

    @Operation(summary = "获取系统概览数据")
    @GetMapping("/overview")
    public Result<?> getSystemOverview(@RequestParam(required = false) String staffId) {
        LOGGER.info("获取系统概览数据");
        try {
            return Result.success(statisticsService.getSystemOverview(staffId));
        } catch (Exception e) {
            LOGGER.error("获取系统概览数据失败", e);
            return Result.error("获取统计数据失败");
        }
    }

    @Operation(summary = "获取订单金额趋势")
    @GetMapping("/order-trend")
    public Result<?> getOrderTrend(
            @Parameter(description = "时间范围(month:近一月, year:近一年, three_years:近三年)")
            @Pattern(regexp = "^(month|year|three_years)$", message = "无效的时间范围")
            @RequestParam String timeRange,
            
            @Parameter(description = "分组方式(day:按天, week:按周, month:按月)")
            @Pattern(regexp = "^(day|week|month)$", message = "无效的分组方式")
            @RequestParam(required = false) String staffId,
            @RequestParam(required = false) String groupBy) {
        LOGGER.info("获取订单金额趋势: timeRange={}, groupBy={}", timeRange, groupBy,staffId);
        try {
            return Result.success(statisticsService.getOrderTrend(timeRange, groupBy, staffId));
        } catch (Exception e) {
            LOGGER.error("获取订单金额趋势失败", e);
            return Result.error(e.getMessage());
        }
    }
} 