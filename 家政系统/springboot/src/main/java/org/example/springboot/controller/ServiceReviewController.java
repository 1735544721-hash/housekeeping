package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.example.springboot.common.Result;
import org.example.springboot.entity.ServiceReview;
import org.example.springboot.service.ServiceReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "评价管理接口")
@RestController
@RequestMapping("/review")

public class ServiceReviewController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceReviewController.class);

    @Resource
    private ServiceReviewService reviewService;

    @Operation(summary = "创建评价")
    @PostMapping

    public Result<?> createReview(@RequestBody @Valid ServiceReview review) {
        LOGGER.info("创建评价: orderId={}, userId={}", review.getOrderId(), review.getUserId());
        reviewService.createReview(review);
        return Result.success("评价成功");
    }

    @Operation(summary="删除评价")
    @DeleteMapping("/{id}")
    public Result<?> deleteReview(@PathVariable Long id) {
        LOGGER.info("删除评价: {}", id);
        reviewService.deleteReview(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "获取评价详情")
    @GetMapping("/{id}")
    public Result<?> getReview(@PathVariable Long id) {
        LOGGER.info("获取评价详情: {}", id);
        return Result.success(reviewService.getReviewById(id));
    }

    @Operation(summary = "获取评价列表")
    @GetMapping("/list")
    public Result<?> getReviewList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long staffId,
            @RequestParam(required = false) String orderId) {
        LOGGER.info("获取评价列表: pageNum={}, pageSize={}", pageNum, pageSize);
        return Result.success(reviewService.getReviewsByPage(userId, staffId, orderId, pageNum, pageSize));
    }

    @Operation(summary = "获取服务人员评价列表")
    @GetMapping("/staff/{staffId}")
    public Result<?> getStaffReviews(
            @PathVariable Long staffId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        LOGGER.info("获取服务人员评价列表: staffId={}", staffId);
        return Result.success(reviewService.getReviewsByPage(null, staffId, null, pageNum, pageSize));
    }

    @Operation(summary = "获取用户评价列表")
    @GetMapping("/user/{userId}")

    public Result<?> getUserReviews(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        LOGGER.info("获取用户评价列表: userId={}", userId);
        return Result.success(reviewService.getReviewsByPage(userId, null, null, pageNum, pageSize));
    }

    // 根据订单id和用户id查询评价
    @Operation(summary = "根据订单id和用户id查询评价")
    @GetMapping("/order/{orderId}/user/{userId}")
    public Result<?> getReviewByOrderIdAndUserId(
            @PathVariable String orderId,
            @PathVariable Long userId) {
        LOGGER.info("根据订单id和用户id查询评价: orderId={}, userId={}", orderId, userId);
        List<ServiceReview> records = reviewService.getReviewsByPage(userId, null, orderId, 1, 1).getRecords();
        if (records.isEmpty()) {
            return Result.success(null);
        }else{
            return Result.success(records.get(0));
        }

    }
} 