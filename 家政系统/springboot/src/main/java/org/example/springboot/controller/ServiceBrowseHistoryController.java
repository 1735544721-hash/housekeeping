package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.service.ServiceBrowseHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "浏览记录管理接口")
@RestController
@RequestMapping("/browse-history")

public class ServiceBrowseHistoryController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBrowseHistoryController.class);

    @Resource
    private ServiceBrowseHistoryService historyService;

    @Operation(summary = "记录浏览历史")
    @PostMapping

    public Result<?> recordHistory(
            @RequestParam Long userId,
            @RequestParam Long serviceId) {
        LOGGER.info("记录浏览历史: userId={}, serviceId={}", userId, serviceId);
        historyService.recordBrowseHistory(userId, serviceId);
        return Result.success("记录成功");
    }

    @Operation(summary = "清除浏览历史")
    @DeleteMapping

    public Result<?> clearHistory(@RequestParam Long userId) {
        LOGGER.info("清除浏览历史: userId={}", userId);
        historyService.clearBrowseHistory(userId);
        return Result.success("清除成功");
    }

    @Operation(summary = "获取浏览历史详情")
    @GetMapping("/{id}")

    public Result<?> getHistory(@PathVariable Long id) {
        LOGGER.info("获取浏览历史详情: {}", id);
        return Result.success(historyService.getHistoryById(id));
    }

    @Operation(summary = "获取浏览历史列表")
    @GetMapping("/list")

    public Result<?> getHistoryList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long serviceId) {
        LOGGER.info("获取浏览历史列表: pageNum={}, pageSize={}", pageNum, pageSize);
        return Result.success(historyService.getHistoryByPage(userId, serviceId, pageNum, pageSize));
    }
} 