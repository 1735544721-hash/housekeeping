package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.service.recommendation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@Tag(name = "推荐服务接口")
@RestController
@RequestMapping("/recommend")
public class RecommendationController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecommendationController.class);

    @Resource
    private UserBasedRecommendationService userBasedService;
    
    @Resource
    private ItemBasedRecommendationService itemBasedService;
    
    @Resource
    private ContentBasedRecommendationService contentBasedService;

    @Operation(summary = "获取基于用户的推荐服务")
    @GetMapping("/user-based/{userId}")
    public Result<?> getUserBasedRecommendations(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "5") Integer limit) {
        LOGGER.info("获取基于用户的推荐: userId={}, limit={}", userId, limit);
        return Result.success(userBasedService.getRecommendations(userId, limit));
    }

    @Operation(summary = "获取基于服务项目的推荐")
    @GetMapping("/item-based/{serviceId}")
    public Result<?> getItemBasedRecommendations(
            @PathVariable Long serviceId,
            @RequestParam(defaultValue = "5") Integer limit) {
        LOGGER.info("获取基于服务的推荐: serviceId={}, limit={}", serviceId, limit);
        return Result.success(itemBasedService.getRecommendations(serviceId, limit));
    }

    @Operation(summary = "获取基于内容的推荐服务")
    @GetMapping("/content-based/{userId}")
    public Result<?> getContentBasedRecommendations(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "5") Integer limit) {
        LOGGER.info("获取基于内容的推荐: userId={}, limit={}", userId, limit);
        return Result.success(contentBasedService.getRecommendations(userId, limit));
    }
} 