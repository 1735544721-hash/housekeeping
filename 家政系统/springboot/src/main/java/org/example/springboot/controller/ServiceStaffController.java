package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.example.springboot.common.Result;
import org.example.springboot.entity.ServiceStaff;
import org.example.springboot.service.ServiceStaffService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Tag(name = "服务人员管理接口")
@RestController
@RequestMapping("/staff")

public class ServiceStaffController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceStaffController.class);

    @Resource
    private ServiceStaffService serviceStaffService;

    @Operation(summary = "创建服务人员")
    @PostMapping("/create")

    public Result<?> createServiceStaff(@RequestBody @Valid ServiceStaff staff) {
        LOGGER.info("创建服务人员: {}", staff);
        serviceStaffService.createServiceStaff(staff);
        return Result.success("创建成功");
    }

    @Operation(summary = "更新服务人员信息")
    @PutMapping("/update")

    public Result<?> updateServiceStaff( @RequestBody @Valid ServiceStaff staff) {
        LOGGER.info("更新服务人员信息: {}", staff);

        serviceStaffService.updateServiceStaff(staff);
        return Result.success("更新成功");
    }

    @Operation(summary = "获取服务人员详情")
    @GetMapping("/{id}")
    public Result<?> getServiceStaff(@PathVariable Long id) {
        LOGGER.info("获取服务人员详情: {}", id);
        return Result.success(serviceStaffService.getServiceStaffById(id));
    }

    @Operation(summary = "获取服务人员列表")
    @GetMapping("/list")
    public Result<?> getServiceStaffList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String serviceType,
            @RequestParam(required = false) BigDecimal minRating) {
        LOGGER.info("获取服务人员列表: pageNum={}, pageSize={}, minRating={}", pageNum, pageSize, minRating);
        return Result.success(serviceStaffService.getServiceStaffsByPage(name, serviceType, pageNum, pageSize, minRating));
    }

    @Operation(summary = "删除服务人员")
    @DeleteMapping("/{id}")

    public Result<?> deleteServiceStaff(@PathVariable Long id) {
        LOGGER.info("删除服务人员: {}", id);
        serviceStaffService.deleteServiceStaff(id);
        return Result.success("删除成功");
    }
    @Operation(summary = "根据用户ID查询服务人员信息")
    @GetMapping("/user/{userId}")
    public Result<?> getServiceStaffByUserId(@PathVariable Long userId) {
        LOGGER.info("根据用户ID查询服务人员信息: {}", userId);
        return Result.success(serviceStaffService.getServiceStaffByUserId(userId));
    }

    @Operation(summary = "获取评分前10的服务人员")
    @GetMapping("/top-rated")
    public Result<?> getTopRatedStaff() {
        LOGGER.info("获取评分前10的服务人员");
        return Result.success(serviceStaffService.getTopRatedStaff());
    }

    @Operation(summary = "获取可提供指定服务的服务人员")
    @GetMapping("/available")
    public Result<?> getAvailableStaff(
           @RequestParam Long serviceId) {
        LOGGER.info("获取可提供服务的服务人员: serviceId={}", serviceId);
        return Result.success(serviceStaffService.getAvailableStaffByServiceItem(serviceId));
    }

//    @Operation(summary = "获取用户信息")
//    @GetMapping("/user/{staffId}")
//    public Result<?> getUserId(@PathVariable Long staffId) {
//        LOGGER.info("获取用户信息: staffId={}", staffId);
//        return Result.success(serviceStaffService.getUserInfo(staffId));
//    }
} 