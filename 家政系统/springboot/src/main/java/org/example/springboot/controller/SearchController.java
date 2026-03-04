package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.service.ServiceCategoryService;
import org.example.springboot.service.ServiceItemService;
import org.example.springboot.service.ServiceStaffService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "全局搜索接口")
@RestController
@RequestMapping("/search")
public class SearchController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchController.class);

    @Resource
    private ServiceItemService serviceItemService;

    @Resource
    private ServiceStaffService serviceStaffService;
    
    @Resource
    private ServiceCategoryService serviceCategoryService;

    @Operation(summary = "搜索服务项目")
    @GetMapping("/services")
    public Result<?> searchServices(@RequestParam String keyword) {
        LOGGER.info("搜索服务项目: keyword={}", keyword);
        return Result.success(serviceItemService.searchServiceItems(keyword));
    }

    @Operation(summary = "搜索服务人员")
    @GetMapping("/staff")
    public Result<?> searchStaff(@RequestParam String keyword) {
        LOGGER.info("搜索服务人员: keyword={}", keyword);
        return Result.success(serviceStaffService.searchServiceStaff(keyword));
    }
    
    @Operation(summary = "搜索服务分类")
    @GetMapping("/categories")
    public Result<?> searchCategories(@RequestParam String keyword) {
        LOGGER.info("搜索服务分类: keyword={}", keyword);
        return Result.success(serviceCategoryService.searchCategories(keyword));
    }

    @Operation(summary = "全局搜索服务和人员")
    @GetMapping("/global")
    public Result<?> globalSearch(@RequestParam String keyword) {
        LOGGER.info("全局搜索: keyword={}", keyword);
        
        // 搜索服务项目
        var services = serviceItemService.searchServiceItems(keyword);
        
        // 搜索服务人员 - 已经在Service中填充了user信息
        var staff = serviceStaffService.searchServiceStaff(keyword);
        
        // 搜索服务分类
        var categories = serviceCategoryService.searchCategories(keyword);
        
        // 返回合并结果
        Map<String, Object> result = new HashMap<>();
        result.put("services", services);
        result.put("staff", staff);
        result.put("categories", categories);
        
        return Result.success(result);
    }
} 