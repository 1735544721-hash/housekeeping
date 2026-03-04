package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.example.springboot.common.Result;
import org.example.springboot.entity.ServiceOrder;
import org.example.springboot.service.ServiceOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Tag(name = "订单管理接口")
@RestController
@RequestMapping("/order")

public class ServiceOrderController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceOrderController.class);

    @Resource
    private ServiceOrderService orderService;

    @Operation(summary = "创建订单")
    @PostMapping

    public Result<?> createOrder(@RequestBody @Valid ServiceOrder order) {
        LOGGER.info("创建订单: userId={}, staffId={}, serviceTime={}, duration={}", 
                    order.getUserId(), order.getStaffId(), order.getServiceTime(), order.getDuration());
        orderService.createOrder(order);
        // 返回订单对象，包含订单ID
        return Result.success("创建成功", order);
    }

    @Operation(summary = "更新订单状态")
    @PutMapping("/{id}/status")

    public Result<?> updateOrderStatus(
            @PathVariable String id,
            @RequestParam Integer status,
            @RequestParam(required = false) String reason) {
        LOGGER.info("更新订单状态: {}, status: {}", id, status);
        orderService.updateOrderStatus(id, status, reason);
        return Result.success("状态更新成功");
    }

    @Operation(summary = "获取订单详情")
    @GetMapping("/{id}")

    public Result<?> getOrder(@PathVariable String id) {
        LOGGER.info("获取订单详情: {}", id);
        return Result.success(orderService.getOrderById(id));
    }

    @Operation(summary = "获取订单列表")
    @GetMapping("/list")

    public Result<?> getOrderList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long staffId,
            @RequestParam(required = false) Integer status) {
        LOGGER.info("获取订单列表: pageNum={}, pageSize={}", pageNum, pageSize);
        return Result.success(orderService.getOrdersByPage(userId, staffId, status, pageNum, pageSize));
    }

    @Operation(summary = "按时间段获取订单列表")
    @GetMapping("/list/time")
    public Result<?> getOrderListByTime(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long staffId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        LOGGER.info("按时间段获取订单列表: pageNum={}, pageSize={}, startTime={}, endTime={}", 
                    pageNum, pageSize, startTime, endTime);
        
        LocalDateTime start = null;
        LocalDateTime end = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        try {
            if (startTime != null && !startTime.isEmpty()) {
                start = LocalDateTime.parse(startTime, formatter);
            }
            if (endTime != null && !endTime.isEmpty()) {
                end = LocalDateTime.parse(endTime, formatter);
            }
        } catch (DateTimeParseException e) {
            LOGGER.error("日期格式解析错误", e);
            return Result.error("日期格式错误，请使用yyyy-MM-dd HH:mm:ss格式");
        }
        
        return Result.success(orderService.getOrdersByPage(userId, staffId, status, start, end, pageNum, pageSize));
    }

    @Operation(summary = "取消订单")
    @PutMapping("/{id}/cancel")
    public Result<?> cancelOrder(
            @PathVariable String id,
            @RequestParam String reason) {
        LOGGER.info("取消订单: {}, reason: {}", id, reason);
        orderService.cancelOrder(id, reason);
        return Result.success("订单取消成功");
    }

    @Operation(summary = "删除订单")
    @DeleteMapping("/{id}")
    public Result<?> deleteOrder(@PathVariable Long id) {
        LOGGER.info("删除订单: {}", id);
        orderService.deleteOrder(id);
        return Result.success("订单删除成功");
    }

    @Operation(summary = "批量删除订单")
    @DeleteMapping("/batch")
    public Result<?> batchDeleteOrders(@RequestBody List<Long> ids) {
        LOGGER.info("批量删除订单: {}", ids);
        orderService.batchDeleteOrders(ids);
        return Result.success("批量删除成功");
    }
} 