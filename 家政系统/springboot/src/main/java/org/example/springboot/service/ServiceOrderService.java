package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.entity.*;
import org.example.springboot.enumClass.OrderStatus;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ServiceOrderService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceOrderService.class);

    @Resource
    private ServiceOrderMapper orderMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private ServiceStaffMapper staffMapper;

    @Resource
    private ServiceItemMapper itemMapper;

    @Resource
    private ServiceReviewMapper reviewMapper;

    @Resource
    private ServiceStaffService staffService;

    @Resource
    private ServiceCategoryMapper categoryMapper;
    
    @Resource
    private StaffServiceItemMapper staffServiceItemMapper;
    
    /**
     * 创建订单
     */
    @Transactional(rollbackFor = Exception.class)
    public void createOrder(ServiceOrder order) {
        // 检查用户是否存在
        if (userMapper.selectById(order.getUserId()) == null) {
            throw new ServiceException("用户不存在");
        }

        // 检查服务人员是否存在
        if (staffMapper.selectById(order.getStaffId()) == null) {
            throw new ServiceException("服务人员不存在");
        }

        // 检查服务项目是否存在
        ServiceItem item = itemMapper.selectById(order.getServiceId());
        if (item == null) {
            throw new ServiceException("服务项目不存在");
        }
        
        // 【关键修复】检查服务人员是否提供该服务项目
        LambdaQueryWrapper<StaffServiceItem> ssiWrapper = new LambdaQueryWrapper<>();
        ssiWrapper.eq(StaffServiceItem::getStaffId, order.getStaffId())
                  .eq(StaffServiceItem::getServiceId, order.getServiceId())
                  .eq(StaffServiceItem::getStatus, 1);  // 状态为正常
        
        StaffServiceItem staffServiceItem = staffServiceItemMapper.selectOne(ssiWrapper);
        if (staffServiceItem == null) {
            throw new ServiceException("该服务人员不提供此服务项目，请重新选择");
        }
        
        LOGGER.info("验证通过：服务人员 {} 提供服务项目 {}", order.getStaffId(), order.getServiceId());
        
        // 检查服务时间和时长是否有效
        if (order.getServiceTime() == null) {
            throw new ServiceException("服务时间不能为空");
        }
        
        if (order.getDuration() == null || order.getDuration() <= 0) {
            throw new ServiceException("服务时长无效");
        }
        
        // 计算服务结束时间
        LocalDateTime serviceEndTime = order.getServiceTime().plusMinutes(Math.round(order.getDuration() * 60));
        
        // 检查同一时间段是否已有其他预约
        LambdaQueryWrapper<ServiceOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ServiceOrder::getStaffId, order.getStaffId())
                    .eq(ServiceOrder::getIsDeleted, 0)
                    .ne(ServiceOrder::getOrderStatus, OrderStatus.CANCELLED.getValue())
                    .ne(ServiceOrder::getOrderStatus, OrderStatus.CLOSED.getValue());

        // 检查是否有重叠的时间段
        // 重叠条件：两个时间段（A开始,A结束）和（B开始,B结束）重叠的条件是：A开始 < B结束 && B开始 < A结束
        queryWrapper.and(wrapper -> {
            // 已有订单的开始时间 < 新订单的结束时间
            wrapper.lt(ServiceOrder::getServiceTime, serviceEndTime)
            // 已有订单的结束时间(serviceTime + duration * 60分钟) > 新订单的开始时间
            .apply("DATE_ADD(service_time, INTERVAL ROUND(duration * 60) MINUTE) > {0}", order.getServiceTime());
        });
        
        List<ServiceOrder> conflictingOrders = orderMapper.selectList(queryWrapper);
        if (!conflictingOrders.isEmpty()) {
            throw new ServiceException("该服务人员在所选时间段已被预约，请选择其他时间");
        }

        // 设置初始状态
        order.setOrderStatus(OrderStatus.WAITING_PAY.getValue());
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());


        // 保存订单信息
        if (orderMapper.insert(order) <= 0) {
            throw new ServiceException("创建订单失败");
        }

        LOGGER.info("创建订单成功: {}", order.getId());
    }

    /**
     * 更新订单状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderStatus(String id, Integer status, String reason) {
        ServiceOrder order = getOrderById(id);
        
        // 检查状态变更是否合法
        checkStatusChange(order.getOrderStatus(), status);

        // 更新订单状态
        ServiceOrder updateOrder = new ServiceOrder();
        updateOrder.setId(id);
        updateOrder.setOrderStatus(status);

        // 处理特殊状态
        if (status.equals(OrderStatus.CANCELLED.getValue())) {
            updateOrder.setCancelTime(LocalDateTime.now());
            updateOrder.setCancelReason(reason);
        } else if (status.equals(OrderStatus.COMPLETED.getValue())) {
            updateOrder.setCompleteTime(LocalDateTime.now());
        }

        if (orderMapper.updateById(updateOrder) <= 0) {
            throw new ServiceException("更新订单状态失败");
        }
        
        // 如果订单状态变更为已完成，更新家政人员的订单数量和完成率
        if (status.equals(OrderStatus.COMPLETED.getValue())) {
            try {
                staffService.updateServiceStaffOrders(order.getStaffId());
                LOGGER.info("已更新家政人员订单数量: staffId={}", order.getStaffId());
            } catch (Exception e) {
                LOGGER.error("更新家政人员订单数量失败: staffId={}", order.getStaffId(), e);
                // 不影响主流程，记录日志即可
            }
        }

        LOGGER.info("更新订单状态成功: {}, status: {}", id, status);
    }

    /**
     * 检查订单状态变更是否合法
     */
    private void checkStatusChange(Integer currentStatus, Integer newStatus) {
        // 终态订单不能变更状态
        if (isTerminalStatus(currentStatus)) {
            throw new ServiceException("订单已是终态，不能变更状态");
        }

        // 获取当前状态和新状态的枚举对象
        OrderStatus current = OrderStatus.getByValue(currentStatus);
        OrderStatus target = OrderStatus.getByValue(newStatus);
        if (current == null || target == null) {
            throw new ServiceException("无效的订单状态");
        }

        // 检查状态流转是否合法
        switch (current) {
            case WAITING_PAY:
                // 待支付状态只能变更为已取消或待接单
                if (target != OrderStatus.CANCELLED && target != OrderStatus.WAITING_ACCEPT) {
                    throw new ServiceException("待支付订单只能取消或支付");
                }
                break;

            case WAITING_ACCEPT:
                // 待接单状态只能变更为已接单或已取消
                if (target != OrderStatus.ACCEPTED && target != OrderStatus.CANCELLED) {
                    throw new ServiceException("待接单订单只能接单或取消");
                }
                break;

            case ACCEPTED:
                // 已接单状态只能变更为服务中或已取消
                if (target != OrderStatus.IN_SERVICE && target != OrderStatus.CANCELLED) {
                    throw new ServiceException("已接单订单只能开始服务或取消");
                }
                break;

            case IN_SERVICE:
                // 服务中状态只能变更为已完成或已取消
                if (target != OrderStatus.COMPLETED && target != OrderStatus.CANCELLED) {
                    throw new ServiceException("服务中订单只能完成或取消");
                }
                break;

            case COMPLETED:
            case CANCELLED:
            case CLOSED:
                // 终态订单不能变更状态(已在前面检查)
                throw new ServiceException("订单状态不能变更");

            default:
                throw new ServiceException("未知的订单状态");
        }

        // 记录状态变更日志
        LOGGER.info("订单状态变更检查通过: {} -> {}", current.getDesc(), target.getDesc());
    }

    /**
     * 判断是否是终态
     */
    private boolean isTerminalStatus(Integer status) {
        return OrderStatus.COMPLETED.getValue().equals(status) ||
               OrderStatus.CANCELLED.getValue().equals(status) ||
               OrderStatus.CLOSED.getValue().equals(status);
    }

    /**
     * 获取订单详情
     */
    public ServiceOrder getOrderById(String id) {
        ServiceOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        fillOrderInfo(order);
        return order;
    }

    /**
     * 删除订单
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteOrder(Long id) {
        // 检查订单是否存在
        ServiceOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }

        // 检查订单状态
        if (!OrderStatus.CANCELLED.getValue().equals(order.getOrderStatus()) &&
            !OrderStatus.COMPLETED.getValue().equals(order.getOrderStatus()) &&
            !OrderStatus.CLOSED.getValue().equals(order.getOrderStatus())) {
            throw new ServiceException("只能删除已完成、已取消或已关闭的订单");
        }

        // 删除关联的评价记录
        LambdaQueryWrapper<ServiceReview> reviewWrapper = new LambdaQueryWrapper<>();
        reviewWrapper.eq(ServiceReview::getOrderId, id);
        reviewMapper.delete(reviewWrapper);
        LOGGER.info("删除订单相关评价记录: orderId={}", id);

        // 使用 MyBatis-Plus 的删除方法进行软删除
        if (orderMapper.deleteById(id) <= 0) {
            throw new ServiceException("删除订单失败");
        }

        LOGGER.info("订单软删除成功: {}", id);
    }

    /**
     * 批量删除订单
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteOrders(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        // 检查订单状态
        List<ServiceOrder> orders = orderMapper.selectBatchIds(ids);
        for (ServiceOrder order : orders) {
            if (!OrderStatus.CANCELLED.getValue().equals(order.getOrderStatus()) &&
                !OrderStatus.COMPLETED.getValue().equals(order.getOrderStatus()) &&
                !OrderStatus.CLOSED.getValue().equals(order.getOrderStatus())) {
                throw new ServiceException("只能删除已完成、已取消或已关闭的订单");
            }
        }

        // 删除关联的评价记录
        LambdaQueryWrapper<ServiceReview> reviewWrapper = new LambdaQueryWrapper<>();
        reviewWrapper.in(ServiceReview::getOrderId, ids);
        reviewMapper.delete(reviewWrapper);
        LOGGER.info("批量删除订单相关评价记录: orderIds={}", ids);

        // 使用 MyBatis-Plus 的批量删除方法进行软删除
        if (orderMapper.deleteBatchIds(ids) <= 0) {
            throw new ServiceException("批量删除订单失败");
        }

        LOGGER.info("批量软删除订单成功: count={}", ids.size());
    }

    /**
     * 分页查询订单列表
     */
    public Page<ServiceOrder> getOrdersByPage(
            Long userId, Long staffId, Integer status,
            Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<ServiceOrder> queryWrapper = new LambdaQueryWrapper<>();
        
        if (userId != null) {
            queryWrapper.eq(ServiceOrder::getUserId, userId);
        }
        if (staffId != null) {
            queryWrapper.eq(ServiceOrder::getStaffId, staffId);
        }
        if (status != null) {
            queryWrapper.eq(ServiceOrder::getOrderStatus, status);
        }

        queryWrapper.orderByDesc(ServiceOrder::getCreateTime);
        
        Page<ServiceOrder> page = orderMapper.selectPage(
            new Page<>(pageNum, pageSize), queryWrapper);
        
        fillOrderInfo(page.getRecords());
        
        return page;
    }

    /**
     * 分页查询订单列表，增加按服务时间段过滤
     */
    public Page<ServiceOrder> getOrdersByPage(
            Long userId, Long staffId, Integer status,
            LocalDateTime startTime, LocalDateTime endTime,
            Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<ServiceOrder> queryWrapper = new LambdaQueryWrapper<>();
        
        if (userId != null) {
            queryWrapper.eq(ServiceOrder::getUserId, userId);
        }
        if (staffId != null) {
            queryWrapper.eq(ServiceOrder::getStaffId, staffId);
        }
        if (status != null) {
            queryWrapper.eq(ServiceOrder::getOrderStatus, status);
        }
        // 按服务时间段过滤
        if (startTime != null) {
            queryWrapper.ge(ServiceOrder::getServiceTime, startTime);
        }
        if (endTime != null) {
            queryWrapper.le(ServiceOrder::getServiceTime, endTime);
        }

        queryWrapper.orderByDesc(ServiceOrder::getCreateTime);
        
        Page<ServiceOrder> page = orderMapper.selectPage(
            new Page<>(pageNum, pageSize), queryWrapper);
        
        fillOrderInfo(page.getRecords());
        
        return page;
    }

    /**
     * 填充订单关联信息
     */
    private void fillOrderInfo(ServiceOrder order) {
        if (order != null) {
            // 填充用户信息
            User user = userMapper.selectById(order.getUserId());
            order.setUser(user);

            // 填充服务人员信息
            ServiceStaff staff = staffMapper.selectById(order.getStaffId());
            staffService.fillUserInfo(staff);
            order.setStaff(staff);

            // 填充服务项目信息
            ServiceItem item = itemMapper.selectById(order.getServiceId());
            order.setServiceItem(item);

            // 填充服务项目类别信息
            if (item != null && item.getCategoryId() != null) {
                ServiceCategory category = categoryMapper.selectById(item.getCategoryId());
                order.setServiceItem(item);
            }

            // 填充评价信息
            ServiceReview review = reviewMapper.selectOne(
                new LambdaQueryWrapper<ServiceReview>()
                    .eq(ServiceReview::getOrderId, order.getId())
            );
            order.setReview(review);
        }
    }

    /**
     * 批量填充订单关联信息
     */
    private void fillOrderInfo(List<ServiceOrder> orders) {
        if (orders != null && !orders.isEmpty()) {
            // 收集所有ID
            List<Long> userIds = orders.stream().map(ServiceOrder::getUserId).collect(Collectors.toList());
            List<Long> staffIds = orders.stream().map(ServiceOrder::getStaffId).collect(Collectors.toList());
            List<Long> itemIds = orders.stream().map(ServiceOrder::getServiceId).collect(Collectors.toList());
            List<String> orderIds = orders.stream().map(ServiceOrder::getId).collect(Collectors.toList());

            // 批量查询关联信息
            Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                    .collect(Collectors.toMap(User::getId, user -> user));
            Map<Long, ServiceStaff> staffMap = staffMapper.selectBatchIds(staffIds).stream()
                    .collect(Collectors.toMap(ServiceStaff::getId, staff -> staff));
            Map<Long, ServiceItem> itemMap = itemMapper.selectBatchIds(itemIds).stream()
                    .collect(Collectors.toMap(ServiceItem::getId, item -> item));
            Map<String, ServiceReview> reviewMap = reviewMapper.selectList(
                new LambdaQueryWrapper<ServiceReview>()
                    .in(ServiceReview::getOrderId, orderIds)
            ).stream().collect(Collectors.toMap(ServiceReview::getOrderId, review -> review));

            // 获取所有服务人员的用户ID
            List<Long> staffUserIds = staffMap.values().stream()
                    .map(ServiceStaff::getUserId)
                    .collect(Collectors.toList());

            // 批量查询服务人员关联的用户信息
            Map<Long, User> staffUserMap = userMapper.selectBatchIds(staffUserIds).stream()
                    .collect(Collectors.toMap(User::getId, user -> user));

            // 填充服务人员的用户信息
            staffMap.values().forEach(staff -> staff.setUser(staffUserMap.get(staff.getUserId())));

            // 填充服务项目的类别信息
            itemMap.values().forEach(item -> {
                if (item.getCategoryId() != null) {
                    ServiceCategory category = categoryMapper.selectById(item.getCategoryId());
                    item.setCategory(category);
                }
            });

            // 填充订单关联信息
            orders.forEach(order -> {
                order.setUser(userMap.get(order.getUserId()));
                order.setStaff(staffMap.get(order.getStaffId()));
                order.setServiceItem(itemMap.get(order.getServiceId()));
                order.setReview(reviewMap.get(order.getId()));
            });
        }
    }

    /**
     * 取消订单
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String id, String reason) {
        ServiceOrder order = getOrderById(id);
        
        // 检查订单状态是否可以取消
        if (isTerminalStatus(order.getOrderStatus())) {
            throw new ServiceException("订单已是终态，不能取消");
        }
        
        // 更新订单状态为已取消
        ServiceOrder updateOrder = new ServiceOrder();
        updateOrder.setId(id);
        updateOrder.setOrderStatus(OrderStatus.CANCELLED.getValue());
        updateOrder.setCancelTime(LocalDateTime.now());
        updateOrder.setCancelReason(reason);

        if (orderMapper.updateById(updateOrder) <= 0) {
            throw new ServiceException("取消订单失败");
        }
        
        // 订单取消时，更新家政人员的完成率
        try {
            staffService.updateServiceStaffOrders(order.getStaffId());
            LOGGER.info("订单取消，已更新家政人员完成率: staffId={}", order.getStaffId());
        } catch (Exception e) {
            LOGGER.error("更新家政人员完成率失败: staffId={}", order.getStaffId(), e);
            // 不影响主流程，记录日志即可
        }

        LOGGER.info("订单取消成功: {}, reason: {}", id, reason);
    }
} 