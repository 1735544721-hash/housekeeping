package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.example.springboot.entity.ServiceItem;
import org.example.springboot.entity.StaffServiceItem;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.ServiceItemMapper;
import org.example.springboot.mapper.StaffServiceItemMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StaffServiceItemService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StaffServiceItemService.class);

    @Resource
    private StaffServiceItemMapper staffServiceItemMapper;

    @Resource
    private ServiceItemMapper serviceItemMapper;

    /**
     * 为服务人员添加服务项目
     */
    @Transactional(rollbackFor = Exception.class)
    public void addServiceItem(Long staffId, Long serviceId) {
        // 检查是否已存在关联
        if (checkServiceItemExists(staffId, serviceId)) {
            throw new ServiceException("该服务项目已添加");
        }

        // 创建关联记录
        StaffServiceItem item = new StaffServiceItem();
        item.setStaffId(staffId);
        item.setServiceId(serviceId);
        item.setStatus(1); // 默认启用

        if (staffServiceItemMapper.insert(item) <= 0) {
            throw new ServiceException("添加服务项目失败");
        }

        LOGGER.info("服务人员添加服务项目成功: staffId={}, serviceId={}", staffId, serviceId);
    }

    /**
     * 批量添加服务项目
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchAddServiceItems(Long staffId, List<Long> serviceIds) {
        if (serviceIds == null || serviceIds.isEmpty()) {
            return;
        }

        for (Long serviceId : serviceIds) {
            if (!checkServiceItemExists(staffId, serviceId)) {
                StaffServiceItem item = new StaffServiceItem();
                item.setStaffId(staffId);
                item.setServiceId(serviceId);
                item.setStatus(1);
                staffServiceItemMapper.insert(item);
            }
        }

        LOGGER.info("服务人员批量添加服务项目成功: staffId={}, count={}", staffId, serviceIds.size());
    }

    /**
     * 移除服务项目
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeServiceItem(Long staffId, Long serviceId) {
        int result = staffServiceItemMapper.delete(
            new LambdaQueryWrapper<StaffServiceItem>()
                .eq(StaffServiceItem::getStaffId, staffId)
                .eq(StaffServiceItem::getServiceId, serviceId)
        );

        if (result <= 0) {
            throw new ServiceException("移除服务项目失败");
        }

        LOGGER.info("服务人员移除服务项目成功: staffId={}, serviceId={}", staffId, serviceId);
    }

    /**
     * 批量移除服务项目
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchRemoveServiceItems(Long staffId, List<Long> serviceIds) {
        if (serviceIds == null || serviceIds.isEmpty()) {
            return;
        }

        staffServiceItemMapper.delete(
            new LambdaQueryWrapper<StaffServiceItem>()
                .eq(StaffServiceItem::getStaffId, staffId)
                .in(StaffServiceItem::getServiceId, serviceIds)
        );

        LOGGER.info("服务人员批量移除服务项目成功: staffId={}, count={}", staffId, serviceIds.size());
    }

    /**
     * 更新服务项目状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateServiceItemStatus(Long staffId, Long serviceId, Integer status) {
        StaffServiceItem item = new StaffServiceItem();
        item.setStatus(status);

        int result = staffServiceItemMapper.update(item,
            new LambdaQueryWrapper<StaffServiceItem>()
                .eq(StaffServiceItem::getStaffId, staffId)
                .eq(StaffServiceItem::getServiceId, serviceId)
        );

        if (result <= 0) {
            throw new ServiceException("更新服务项目状态失败");
        }

        LOGGER.info("更新服务项目状态成功: staffId={}, serviceId={}, status={}", staffId, serviceId, status);
    }

    /**
     * 获取服务人员的所有服务项目
     */
    public List<StaffServiceItem> getServiceItems(Long staffId) {
        List<StaffServiceItem> items = staffServiceItemMapper.selectList(
            new LambdaQueryWrapper<StaffServiceItem>()
                .eq(StaffServiceItem::getStaffId, staffId)
        );

        // 填充服务项目详情
        if (!items.isEmpty()) {
            List<Long> serviceIds = items.stream()
                    .map(StaffServiceItem::getServiceId)
                    .collect(Collectors.toList());

            List<ServiceItem> serviceItems = serviceItemMapper.selectList(
                new LambdaQueryWrapper<ServiceItem>()
                    .in(ServiceItem::getId, serviceIds)
            );

            Map<Long, ServiceItem> serviceItemMap = serviceItems.stream()
                    .collect(Collectors.toMap(ServiceItem::getId, item -> item));

            items.forEach(item -> item.setServiceItem(serviceItemMap.get(item.getServiceId())));
        }

        return items;
    }

    /**
     * 检查服务项目是否已存在
     */
    private boolean checkServiceItemExists(Long staffId, Long serviceId) {
        return staffServiceItemMapper.selectCount(
            new LambdaQueryWrapper<StaffServiceItem>()
                .eq(StaffServiceItem::getStaffId, staffId)
                .eq(StaffServiceItem::getServiceId, serviceId)
        ) > 0;
    }

    /**
     * 根据服务项目获取提供该服务的服务人员关联记录
     */
    public List<StaffServiceItem> getStaffsByServiceItem(Long serviceId) {
        return staffServiceItemMapper.selectList(
            new LambdaQueryWrapper<StaffServiceItem>()
                .eq(StaffServiceItem::getServiceId, serviceId)
                .eq(StaffServiceItem::getStatus, 1)  // 只查询启用状态的关联
        );
    }
} 