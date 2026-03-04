package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.entity.ServiceItem;
import org.example.springboot.entity.ServiceCategory;
import org.example.springboot.entity.ServiceOrder;
import org.example.springboot.enumClass.OrderStatus;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.ServiceItemMapper;
import org.example.springboot.mapper.ServiceCategoryMapper;
import org.example.springboot.mapper.ServiceOrderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
public class ServiceItemService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceItemService.class);

    @Resource
    private ServiceItemMapper serviceItemMapper;

    @Resource
    private ServiceCategoryMapper serviceCategoryMapper;

    @Resource
    private ServiceOrderMapper orderMapper;

    /**
     * 创建服务项目
     */
    @Transactional(rollbackFor = Exception.class)
    public void createServiceItem(ServiceItem item) {
        // 检查类别是否存在
        if (serviceCategoryMapper.selectById(item.getCategoryId()) == null) {
            throw new ServiceException("服务类别不存在");
        }

        // 设置默认状态
        if (item.getStatus() == null) {
            item.setStatus(1); // 默认上架
        }

        // 保存服务项目
        if (serviceItemMapper.insert(item) <= 0) {
            throw new ServiceException("创建服务项目失败");
        }

        LOGGER.info("创建服务项目成功: {}", item.getId());
    }

    /**
     * 更新服务项目
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateServiceItem(ServiceItem item) {
        // 检查服务项目是否存在
        if (serviceItemMapper.selectById(item.getId()) == null) {
            throw new ServiceException("服务项目不存在");
        }

        // 如果修改了类别，检查新类别是否存在
        if (item.getCategoryId() != null && 
            serviceCategoryMapper.selectById(item.getCategoryId()) == null) {
            throw new ServiceException("服务类别不存在");
        }

        // 更新服务项目
        if (serviceItemMapper.updateById(item) <= 0) {
            throw new ServiceException("更新服务项目失败");
        }

        LOGGER.info("更新服务项目成功: {}", item.getId());
    }

    /**
     * 获取服务项目详情
     */
    public ServiceItem getServiceItemById(Long id) {
        ServiceItem item = serviceItemMapper.selectOne(
            new LambdaQueryWrapper<ServiceItem>()
                .eq(ServiceItem::getId, id)
                .eq(ServiceItem::getIsDeleted, 0)  // 只查询未删除的项目
        );
        if (item == null) {
            throw new ServiceException("服务项目不存在");
        }
        fillCategory(item);
        return item;
    }

    /**
     * 分页查询服务项目列表
     */
    public Page<ServiceItem> getServiceItemsByPage(
            String title, Long categoryId, Integer status,
            Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<ServiceItem> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (title != null && !title.trim().isEmpty()) {
            queryWrapper.like(ServiceItem::getTitle, title);
        }
        if (categoryId != null) {
            queryWrapper.eq(ServiceItem::getCategoryId, categoryId);
        }
        if (status != null) {
            queryWrapper.eq(ServiceItem::getStatus, status);
        }
        
        // 只查询未删除的项目
        queryWrapper.eq(ServiceItem::getIsDeleted, 0);

        // 执行分页查询
        Page<ServiceItem> page = serviceItemMapper.selectPage(
            new Page<>(pageNum, pageSize), queryWrapper);
        
        // 填充类别信息
        fillCategory(page.getRecords());
        
        return page;
    }

    /**
     * 删除服务项目
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteServiceItem(Long id) {
        // 检查服务项目是否存在且未删除
        ServiceItem item = serviceItemMapper.selectOne(
            new LambdaQueryWrapper<ServiceItem>()
                .eq(ServiceItem::getId, id)
        );
        if (item == null) {
            throw new ServiceException("服务项目不存在");
        }

        // 检查是否存在未完成的订单
        Long unfinishedCount = orderMapper.selectCount(
            new LambdaQueryWrapper<ServiceOrder>()
                .eq(ServiceOrder::getServiceId, id)
                .notIn(ServiceOrder::getOrderStatus, 
                    OrderStatus.COMPLETED.getValue(),
                    OrderStatus.CANCELLED.getValue(),
                    OrderStatus.CLOSED.getValue())
        );
        
        if (unfinishedCount > 0) {
            throw new ServiceException("该服务项目存在未完成的订单，不能删除");
        }

        // 使用 MyBatis-Plus 的删除方法进行软删除
        if (serviceItemMapper.deleteById(id) <= 0) {
            throw new ServiceException("删除服务项目失败");
        }

        LOGGER.info("服务项目软删除成功: {}", id);
    }

    /**
     * 批量删除服务项目(软删除)
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteServiceItems(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        // 检查是否存在未完成的订单
        Long unfinishedCount = orderMapper.selectCount(
            new LambdaQueryWrapper<ServiceOrder>()
                .in(ServiceOrder::getServiceId, ids)
                .notIn(ServiceOrder::getOrderStatus, 
                    OrderStatus.COMPLETED.getValue(),
                    OrderStatus.CANCELLED.getValue(),
                    OrderStatus.CLOSED.getValue())
        );
        
        if (unfinishedCount > 0) {
            throw new ServiceException("选中的服务项目中存在未完成的订单，不能删除");
        }

        // 使用 MyBatis-Plus 的批量删除方法进行软删除
        if (serviceItemMapper.deleteBatchIds(ids) <= 0) {
            throw new ServiceException("批量删除服务项目失败");
        }

        LOGGER.info("批量软删除服务项目成功: count={}", ids.size());
    }

    /**
     * 修改服务项目状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        ServiceItem item = new ServiceItem();
        item.setId(id);
        item.setStatus(status);
        
        if (serviceItemMapper.updateById(item) <= 0) {
            throw new ServiceException("修改状态失败");
        }

        LOGGER.info("修改服务项目状态成功: {}, status: {}", id, status);
    }

    /**
     * 填充类别信息
     */
    private void fillCategory(ServiceItem item) {
        if (item != null && item.getCategoryId() != null) {
            ServiceCategory category = serviceCategoryMapper.selectById(item.getCategoryId());
            item.setCategory(category);
        }
    }

    /**
     * 批量填充类别信息
     */
    public void fillCategory(List<ServiceItem> items) {
        if (items != null && !items.isEmpty()) {
            // 提取所有类别ID
            List<Long> categoryIds = items.stream()
                    .map(ServiceItem::getCategoryId)
                    .collect(Collectors.toList());
            
            // 批量查询类别信息
            List<ServiceCategory> categories = serviceCategoryMapper.selectBatchIds(categoryIds);
            Map<Long, ServiceCategory> categoryMap = categories.stream()
                    .collect(Collectors.toMap(ServiceCategory::getId, category -> category));
            
            // 填充类别信息
            items.forEach(item -> item.setCategory(categoryMap.get(item.getCategoryId())));
        }
    }

    /**
     * 根据类别ID获取服务项目列表(包含子类别的项目)
     */
    public List<ServiceItem> getServiceItemsByCategory(Long categoryId) {
        // 获取类别及其所有子类别
        List<Long> categoryIds = new ArrayList<>();
        categoryIds.add(categoryId);
        categoryIds.addAll(getSubCategoryIds(categoryId));

        // 查询服务项目
        List<ServiceItem> items = serviceItemMapper.selectList(
            new LambdaQueryWrapper<ServiceItem>()
                .in(ServiceItem::getCategoryId, categoryIds)
                .eq(ServiceItem::getStatus, 1)  // 只查询上架的项目
                .eq(ServiceItem::getIsDeleted, 0)  // 只查询未删除的项目
        );

        // 填充类别信息
        fillCategory(items);
        return items;
    }

    /**
     * 搜索服务项目
     */
    public List<ServiceItem> searchServiceItems(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // 构建查询条件
        LambdaQueryWrapper<ServiceItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(ServiceItem::getTitle, keyword)
                   .or()
                   .like(ServiceItem::getDescription, keyword)
                   .eq(ServiceItem::getStatus, 1)  // 只查询上架的项目
                   .eq(ServiceItem::getIsDeleted, 0)  // 只查询未删除的项目
                   .orderByDesc(ServiceItem::getCreateTime)  // 按创建时间降序
                   .last("LIMIT 10");  // 限制返回10条记录

        // 执行查询
        List<ServiceItem> items = serviceItemMapper.selectList(queryWrapper);

        // 填充类别信息
        fillCategory(items);
        return items;
    }

    /**
     * 获取子类别ID列表
     */
    private List<Long> getSubCategoryIds(Long parentId) {
        List<Long> ids = new ArrayList<>();
        
        List<ServiceCategory> children = serviceCategoryMapper.selectList(
            new LambdaQueryWrapper<ServiceCategory>()
                .eq(ServiceCategory::getParentId, parentId)
                .eq(ServiceCategory::getStatus, 1)
                .eq(ServiceCategory::getIsDeleted, 0)
        );
        
        if (!children.isEmpty()) {
            for (ServiceCategory child : children) {
                ids.add(child.getId());
                ids.addAll(getSubCategoryIds(child.getId()));
            }
        }
        
        return ids;
    }
} 