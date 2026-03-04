package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.example.springboot.common.Result;
import org.example.springboot.entity.ServiceItem;
import org.example.springboot.service.ServiceItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "服务项目管理接口")
@RestController
@RequestMapping("/service")

public class ServiceItemController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceItemController.class);

    @Resource
    private ServiceItemService serviceItemService;

    @Operation(summary = "创建服务项目")
    @PostMapping

    public Result<?> createServiceItem(@RequestBody @Valid ServiceItem item) {
        LOGGER.info("创建服务项目: {}", item.getTitle());
        serviceItemService.createServiceItem(item);
        return Result.success("创建成功");
    }

    @Operation(summary = "更新服务项目")
    @PutMapping("/{id}")

    public Result<?> updateServiceItem(@PathVariable Long id, @RequestBody @Valid ServiceItem item) {
        LOGGER.info("更新服务项目: {}", id);
        item.setId(id);
        serviceItemService.updateServiceItem(item);
        return Result.success("更新成功");
    }

    @Operation(summary = "获取服务项目详情")
    @GetMapping("/{id}")
    public Result<?> getServiceItem(@PathVariable Long id) {
        LOGGER.info("获取服务项目详情: {}", id);
        return Result.success(serviceItemService.getServiceItemById(id));
    }

    @Operation(summary = "获取服务项目列表")
    @GetMapping("/list")
    public Result<?> getServiceItemList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status) {
        LOGGER.info("获取服务项目列表: pageNum={}, pageSize={}", pageNum, pageSize);
        return Result.success(serviceItemService.getServiceItemsByPage(
            title, categoryId, status, pageNum, pageSize));
    }

    @Operation(summary = "删除服务项目")
    @DeleteMapping("/{id}")

    public Result<?> deleteServiceItem(@PathVariable Long id) {
        LOGGER.info("删除服务项目: {}", id);
        serviceItemService.deleteServiceItem(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "修改服务项目状态")
    @PutMapping("/{id}/status")

    public Result<?> updateStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        LOGGER.info("修改服务项目状态: {}, status: {}", id, status);
        serviceItemService.updateStatus(id, status);
        return Result.success("状态修改成功");
    }

    @Operation(summary = "获取类别下的所有服务项目")
    @GetMapping("/category/{categoryId}")
    public Result<?> getServiceItemsByCategory(@PathVariable Long categoryId) {
        LOGGER.info("获取类别下的服务项目: categoryId={}", categoryId);
        return Result.success(serviceItemService.getServiceItemsByCategory(categoryId));
    }
} 