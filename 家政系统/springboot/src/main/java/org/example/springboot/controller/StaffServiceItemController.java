package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.example.springboot.common.Result;
import org.example.springboot.entity.StaffServiceItem;
import org.example.springboot.service.StaffServiceItemService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "服务人员服务项目接口")
@RestController
@RequestMapping("/staff/service-items")
@Validated
public class StaffServiceItemController {

    @Resource
    private StaffServiceItemService staffServiceItemService;

    @Operation(summary = "添加服务项目")
    @PostMapping("/add")
    public Result<?> addServiceItem(
            @NotNull(message = "服务人员ID不能为空") @RequestParam Long staffId,
            @NotNull(message = "服务项目ID不能为空") @RequestParam Long serviceId) {
        staffServiceItemService.addServiceItem(staffId, serviceId);
        return Result.success("添加成功");
    }

    @Operation(summary = "批量添加服务项目")
    @PostMapping("/batch-add")
    public Result<?> batchAddServiceItems(
            @NotNull(message = "服务人员ID不能为空") @RequestParam Long staffId,
            @NotNull(message = "服务项目ID列表不能为空") @RequestParam List<Long> serviceIds) {
        staffServiceItemService.batchAddServiceItems(staffId, serviceIds);
        return Result.success("添加成功");
    }

    @Operation(summary = "移除服务项目")
    @PostMapping("/remove")
    public Result<?> removeServiceItem(
            @NotNull(message = "服务人员ID不能为空") @RequestParam Long staffId,
            @NotNull(message = "服务项目ID不能为空") @RequestParam Long serviceId) {
        staffServiceItemService.removeServiceItem(staffId, serviceId);
        return Result.success("移除成功");
    }

    @Operation(summary = "批量移除服务项目")
    @PostMapping("/batch-remove")
    public Result<?> batchRemoveServiceItems(
            @NotNull(message = "服务人员ID不能为空") @RequestParam Long staffId,
            @NotNull(message = "服务项目ID列表不能为空") @RequestParam List<Long> serviceIds) {
        staffServiceItemService.batchRemoveServiceItems(staffId, serviceIds);
        return Result.success("移除成功");
    }

    @Operation(summary = "更新服务项目状态")
    @PostMapping("/update-status")
    public Result<?> updateServiceItemStatus(
            @NotNull(message = "服务人员ID不能为空") @RequestParam Long staffId,
            @NotNull(message = "服务项目ID不能为空") @RequestParam Long serviceId,
            @NotNull(message = "状态不能为空") @RequestParam Integer status) {
        staffServiceItemService.updateServiceItemStatus(staffId, serviceId, status);
        return Result.success("更新成功");
    }

    @Operation(summary = "获取服务项目列表")
    @GetMapping("/list")
    public Result<List<StaffServiceItem>> getServiceItems(
            @NotNull(message = "服务人员ID不能为空") @RequestParam Long staffId) {
        List<StaffServiceItem> items = staffServiceItemService.getServiceItems(staffId);
        return Result.success(items);
    }

    @Operation(summary = "分配服务项目")
    @PostMapping("/assign")
    public Result<?> assignServiceItems(
            @NotNull(message = "服务人员ID不能为空") @RequestParam Long staffId,
            @NotNull(message = "服务项目ID列表不能为空") @RequestParam List<Long> serviceIds,
            @RequestParam(required = false, defaultValue = "1") Integer status) {
        // 先移除原有的所有服务项目
        List<StaffServiceItem> existingItems = staffServiceItemService.getServiceItems(staffId);
        if (!existingItems.isEmpty()) {
            List<Long> existingIds = existingItems.stream()
                    .map(StaffServiceItem::getServiceId)
                    .collect(Collectors.toList());
            staffServiceItemService.batchRemoveServiceItems(staffId, existingIds);
        }
        
        // 添加新的服务项目
        if (!serviceIds.isEmpty()) {
            staffServiceItemService.batchAddServiceItems(staffId, serviceIds);
            // 如果状态不是默认值1，则更新所有新添加项目的状态
            if (status != 1) {
                for (Long serviceId : serviceIds) {
                    staffServiceItemService.updateServiceItemStatus(staffId, serviceId, status);
                }
            }
        }
        
        return Result.success("分配成功");
    }
} 