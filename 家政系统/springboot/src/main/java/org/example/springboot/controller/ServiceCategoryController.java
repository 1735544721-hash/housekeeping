package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.example.springboot.common.Result;
import org.example.springboot.entity.ServiceCategory;
import org.example.springboot.service.ServiceCategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "服务类别管理接口")
@RestController
@RequestMapping("/category")

public class ServiceCategoryController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCategoryController.class);

    @Resource
    private ServiceCategoryService categoryService;

    @Operation(summary = "创建服务类别")
    @PostMapping("/create")
    public Result<?> createCategory(@RequestBody @Valid ServiceCategory category) {
        LOGGER.info("创建服务类别: {}", category.getCategoryName());
        categoryService.createCategory(category);
        return Result.success("创建成功");
    }

    @Operation(summary = "更新服务类别")
    @PutMapping("/update")

    public Result<?> updateCategory(@RequestBody @Valid ServiceCategory category) {
        LOGGER.info("更新服务类别: {}", category);

        categoryService.updateCategory(category);
        return Result.success("更新成功");
    }

    @Operation(summary = "获取服务类别详情")
    @GetMapping("/{id}")
    public Result<?> getCategory(@PathVariable Long id) {
        LOGGER.info("获取服务类别详情: {}", id);
        return Result.success(categoryService.getCategoryById(id));
    }

    @Operation(summary = "获取服务类别树")
    @GetMapping("/tree")
    public Result<?> getCategoryTree() {
        LOGGER.info("获取服务类别树");
        return Result.success(categoryService.getCategoryTree());
    }

    @Operation(summary = "删除服务类别")
    @DeleteMapping("/{id}")

    public Result<?> deleteCategory(@PathVariable Long id) {
        LOGGER.info("删除服务类别: {}", id);
        categoryService.deleteCategory(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "获取子类别列表")
    @GetMapping("/children/{parentId}")
    public Result<?> getChildCategories(@PathVariable Long parentId) {
        LOGGER.info("获取子类别列表: parentId={}", parentId);
        return Result.success(categoryService.getChildCategories(parentId));
    }

    @Operation(summary = "获取服务类别分页列表")
    @GetMapping("/page")
    public Result<?> getCategoryList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) Integer status) {
        LOGGER.info("获取服务类别列表: pageNum={}, pageSize={}", pageNum, pageSize);
        return Result.success(categoryService.getCategoriesByPage(categoryName, status, pageNum, pageSize));
    }

    @Operation(summary = "批量删除服务类别")
    @DeleteMapping("/batch")
    public Result<?> batchDeleteCategories(@RequestBody List<Long> ids) {
        LOGGER.info("批量删除服务类别: {}", ids);
        categoryService.batchDeleteCategories(ids);
        return Result.success("批量删除成功");
    }
} 