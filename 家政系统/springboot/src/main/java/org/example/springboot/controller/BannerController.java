package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Banner;
import org.example.springboot.service.BannerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "轮播图管理接口")
@RestController
@RequestMapping("/banner")
public class BannerController {
    private static final Logger LOGGER = LoggerFactory.getLogger(BannerController.class);

    @Resource
    private BannerService bannerService;

    @Operation(summary = "创建轮播图")
    @PostMapping
    public Result<?> createBanner(@Validated @RequestBody Banner banner) {
        LOGGER.info("创建轮播图: {}", banner.getTitle());
        bannerService.createBanner(banner);
        return Result.success("创建成功");
    }

    @Operation(summary = "更新轮播图")
    @PutMapping("/{id}")
    public Result<?> updateBanner(@PathVariable Long id, @Validated @RequestBody Banner banner) {
        LOGGER.info("更新轮播图: {}", id);
        banner.setId(id);
        bannerService.updateBanner(banner);
        return Result.success("更新成功");
    }

    @Operation(summary = "删除轮播图")
    @DeleteMapping("/{id}")
    public Result<?> deleteBanner(@PathVariable Long id) {
        LOGGER.info("删除轮播图: {}", id);
        bannerService.deleteBanner(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "获取轮播图详情")
    @GetMapping("/{id}")
    public Result<?> getBannerById(@PathVariable Long id) {
        LOGGER.info("获取轮播图详情: {}", id);
        return Result.success(bannerService.getBannerById(id));
    }

    @Operation(summary = "获取启用的轮播图列表")
    @GetMapping("/active")
    public Result<?> getActiveBanners() {
        LOGGER.info("获取启用的轮播图列表");
        return Result.success(bannerService.getActiveBanners());
    }

    @Operation(summary = "获取轮播图分页列表")
    @GetMapping("/page")
    public Result<?> getBannerList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer status) {
        LOGGER.info("获取轮播图列表: pageNum={}, pageSize={}", pageNum, pageSize);
        return Result.success(bannerService.getBannersByPage(title, status, pageNum, pageSize));
    }
} 