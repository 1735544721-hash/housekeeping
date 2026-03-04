package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.example.springboot.common.Result;
import org.example.springboot.entity.ServiceFavorite;
import org.example.springboot.service.ServiceFavoriteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "收藏管理接口")
@RestController
@RequestMapping("/favorite")

public class ServiceFavoriteController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceFavoriteController.class);

    @Resource
    private ServiceFavoriteService favoriteService;

    @Operation(summary = "添加收藏")
    @PostMapping

    public Result<?> addFavorite(@RequestBody @Valid ServiceFavorite favorite) {
        LOGGER.info("添加收藏: userId={}", favorite.getUserId());
        favoriteService.addFavorite(favorite);
        return Result.success("收藏成功");
    }

    @Operation(summary = "取消收藏")
    @DeleteMapping

    public Result<?> cancelFavorite(
            @RequestParam Long userId,
            @RequestParam Long serviceId) {
        LOGGER.info("取消收藏: userId={}, serviceId={}", userId, serviceId);
        favoriteService.cancelFavorite(userId, serviceId);
        return Result.success("取消成功");
    }

    @Operation(summary = "获取收藏详情")
    @GetMapping("/{id}")

    public Result<?> getFavorite(@PathVariable Long id) {
        LOGGER.info("获取收藏详情: {}", id);
        return Result.success(favoriteService.getFavoriteById(id));
    }

    @Operation(summary = "获取收藏列表")
    @GetMapping("/list")

    public Result<?> getFavoriteList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long serviceId) {
        LOGGER.info("获取收藏列表: pageNum={}, pageSize={}", pageNum, pageSize);
        return Result.success(favoriteService.getFavoritesByPage(userId, serviceId, pageNum, pageSize));
    }

    @Operation(summary = "检查是否已收藏")
    @GetMapping("/check")

    public Result<?> checkFavorite(
            @RequestParam Long userId,
            @RequestParam Long serviceId) {
        LOGGER.info("检查是否已收藏: userId={}, serviceId={}", userId, serviceId);
        return Result.success(favoriteService.isFavorite(userId, serviceId));
    }
} 