package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.OrderRefund;
import org.example.springboot.service.RefundService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * 退款控制器
 */
@Tag(name = "退款管理接口")
@RestController
@RequestMapping("/refund")
public class RefundController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RefundController.class);

    @Resource
    private RefundService refundService;

    @Operation(summary = "申请退款")
    @PostMapping("/apply")
    public Result<?> applyRefund(
            @RequestParam String orderId,
            @RequestParam Long userId,
            @RequestParam String refundReason,
            @RequestParam(defaultValue = "1") Integer refundType) {
        LOGGER.info("申请退款: orderId={}, userId={}", orderId, userId);
        
        OrderRefund refund = refundService.applyRefund(orderId, userId, refundReason, refundType);
        return Result.success("退款申请已提交", refund);
    }

    @Operation(summary = "审核退款")
    @PostMapping("/audit/{refundId}")
    public Result<?> auditRefund(
            @PathVariable Long refundId,
            @RequestParam Long auditUserId,
            @RequestParam Integer auditResult,
            @RequestParam(required = false) String auditRemark) {
        LOGGER.info("审核退款: refundId={}, auditResult={}", refundId, auditResult);
        
        refundService.auditRefund(refundId, auditUserId, auditResult, auditRemark);
        return Result.success("审核完成");
    }

    @Operation(summary = "查询退款详情")
    @GetMapping("/{refundId}")
    public Result<?> getRefundDetail(@PathVariable Long refundId) {
        LOGGER.info("查询退款详情: refundId={}", refundId);
        return Result.success(refundService.getRefundDetail(refundId));
    }

    @Operation(summary = "退款列表")
    @GetMapping("/list")
    public Result<?> getRefundList(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer refundStatus,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        LOGGER.info("查询退款列表: userId={}, refundStatus={}", userId, refundStatus);
        
        return Result.success(refundService.getRefundList(userId, refundStatus, pageNum, pageSize));
    }
}
