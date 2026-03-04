package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.entity.OrderRefund;
import org.example.springboot.entity.ServiceOrder;
import org.example.springboot.entity.User;
import org.example.springboot.enumClass.OrderStatus;
import org.example.springboot.enumClass.RefundStatus;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.*;

import org.example.springboot.entity.ServiceStaff;
import org.example.springboot.entity.ServiceItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 退款服务
 */
@Service
public class RefundService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RefundService.class);

    @Resource
    private OrderRefundMapper refundMapper;

    @Resource
    private ServiceOrderMapper orderMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private ServiceStaffMapper staffMapper;

    @Resource
    private ServiceItemMapper serviceItemMapper;

    /**
     * 申请退款
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderRefund applyRefund(String orderId, Long userId, String refundReason, Integer refundType) {
        LOGGER.info("申请退款: orderId={}, userId={}", orderId, userId);

        // 1. 查询订单
        ServiceOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }

        // 2. 验证订单所有权
        if (!order.getUserId().equals(userId)) {
            throw new ServiceException("无权操作此订单");
        }

        // 3. 检查订单状态
        if (OrderStatus.WAITING_PAY.getValue().equals(order.getOrderStatus())) {
            throw new ServiceException("待支付订单无需退款，可直接取消");
        }

        if (OrderStatus.COMPLETED.getValue().equals(order.getOrderStatus())) {
            throw new ServiceException("已完成的订单不支持退款");
        }

        if (OrderStatus.CANCELLED.getValue().equals(order.getOrderStatus())) {
            throw new ServiceException("订单已取消");
        }

        // 4. 检查是否已支付
        if (order.getPaymentTime() == null) {
            throw new ServiceException("订单未支付，无需退款");
        }

        // 5. 检查是否已有退款申请
        LambdaQueryWrapper<OrderRefund> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderRefund::getOrderId, orderId)
               .in(OrderRefund::getRefundStatus, 
                   RefundStatus.PENDING_AUDIT.getValue(),
                   RefundStatus.AUDIT_PASSED.getValue(),
                   RefundStatus.REFUNDING.getValue(),
                   RefundStatus.REFUNDED.getValue());
        
        OrderRefund existingRefund = refundMapper.selectOne(wrapper);
        if (existingRefund != null) {
            throw new ServiceException("该订单已有退款申请，请勿重复提交");
        }

        // 6. 创建退款记录
        OrderRefund refund = new OrderRefund();
        refund.setOrderId(orderId);
        refund.setUserId(userId);
        refund.setRefundAmount(order.getPaidAmount());
        refund.setRefundReason(refundReason);
        refund.setRefundStatus(RefundStatus.PENDING_AUDIT.getValue());
        refund.setRefundType(refundType);
        refund.setCreateTime(LocalDateTime.now());
        refund.setUpdateTime(LocalDateTime.now());

        refundMapper.insert(refund);

        // 7. 更新订单退款状态
        ServiceOrder updateOrder = new ServiceOrder();
        updateOrder.setId(orderId);
        updateOrder.setRefundStatus(1); // 退款中
        orderMapper.updateById(updateOrder);

        LOGGER.info("退款申请创建成功: refundId={}, orderId={}", refund.getId(), orderId);
        return refund;
    }

    /**
     * 审核退款（管理员）
     */
    @Transactional(rollbackFor = Exception.class)
    public void auditRefund(Long refundId, Long auditUserId, Integer auditResult, String auditRemark) {
        LOGGER.info("审核退款: refundId={}, auditResult={}", refundId, auditResult);

        // 1. 查询退款记录
        OrderRefund refund = refundMapper.selectById(refundId);
        if (refund == null) {
            throw new ServiceException("退款记录不存在");
        }

        // 2. 检查退款状态
        if (!RefundStatus.PENDING_AUDIT.getValue().equals(refund.getRefundStatus())) {
            throw new ServiceException("该退款申请已处理");
        }

        // 3. 更新退款状态
        OrderRefund updateRefund = new OrderRefund();
        updateRefund.setId(refundId);
        updateRefund.setAuditUserId(auditUserId);
        updateRefund.setAuditTime(LocalDateTime.now());
        updateRefund.setAuditRemark(auditRemark);

        if (RefundStatus.AUDIT_PASSED.getValue().equals(auditResult)) {
            // 审核通过，执行退款
            updateRefund.setRefundStatus(RefundStatus.AUDIT_PASSED.getValue());
            refundMapper.updateById(updateRefund);
            
            // 执行退款
            processRefund(refundId);
            
        } else if (RefundStatus.AUDIT_REJECTED.getValue().equals(auditResult)) {
            // 审核拒绝
            updateRefund.setRefundStatus(RefundStatus.AUDIT_REJECTED.getValue());
            refundMapper.updateById(updateRefund);
            
            // 更新订单退款状态
            ServiceOrder order = orderMapper.selectById(refund.getOrderId());
            if (order != null) {
                ServiceOrder updateOrder = new ServiceOrder();
                updateOrder.setId(order.getId());
                updateOrder.setRefundStatus(3); // 退款失败
                orderMapper.updateById(updateOrder);
            }
            
            LOGGER.info("退款审核拒绝: refundId={}", refundId);
        } else {
            throw new ServiceException("无效的审核结果");
        }
    }

    /**
     * 处理退款（模拟）
     */
    @Transactional(rollbackFor = Exception.class)
    public void processRefund(Long refundId) {
        LOGGER.info("开始处理退款: refundId={}", refundId);

        OrderRefund refund = refundMapper.selectById(refundId);
        if (refund == null) {
            throw new ServiceException("退款记录不存在");
        }

        try {
            // 更新退款状态为退款中
            OrderRefund updateRefund = new OrderRefund();
            updateRefund.setId(refundId);
            updateRefund.setRefundStatus(RefundStatus.REFUNDING.getValue());
            refundMapper.updateById(updateRefund);

            // 模拟退款处理（实际项目中调用第三方退款接口）
            Thread.sleep(1000);

            // 模拟退款成功
            updateRefund = new OrderRefund();
            updateRefund.setId(refundId);
            updateRefund.setRefundStatus(RefundStatus.REFUNDED.getValue());
            updateRefund.setRefundTime(LocalDateTime.now());
            refundMapper.updateById(updateRefund);

            // 更新订单状态
            ServiceOrder order = orderMapper.selectById(refund.getOrderId());
            if (order != null) {
                ServiceOrder updateOrder = new ServiceOrder();
                updateOrder.setId(order.getId());
                updateOrder.setOrderStatus(OrderStatus.CANCELLED.getValue());
                updateOrder.setRefundStatus(2); // 已退款
                updateOrder.setRefundAmount(refund.getRefundAmount());
                updateOrder.setCancelReason("退款成功");
                updateOrder.setCancelTime(LocalDateTime.now());
                orderMapper.updateById(updateOrder);
            }

            LOGGER.info("退款处理成功: refundId={}, amount={}", refundId, refund.getRefundAmount());

        } catch (InterruptedException e) {
            LOGGER.error("退款处理异常", e);
            Thread.currentThread().interrupt();
            
            // 更新退款状态为失败
            OrderRefund updateRefund = new OrderRefund();
            updateRefund.setId(refundId);
            updateRefund.setRefundStatus(RefundStatus.REFUND_FAILED.getValue());
            refundMapper.updateById(updateRefund);
            
            throw new ServiceException("退款处理失败");
        }
    }

    /**
     * 查询退款详情
     */
    public OrderRefund getRefundDetail(Long refundId) {
        OrderRefund refund = refundMapper.selectById(refundId);
        if (refund == null) {
            throw new ServiceException("退款记录不存在");
        }
        
        // 填充关联信息
        fillRefundInfo(refund);
        return refund;
    }

    /**
     * 分页查询退款列表
     */
    public Page<OrderRefund> getRefundList(Long userId, Integer refundStatus, 
                                           Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<OrderRefund> wrapper = new LambdaQueryWrapper<>();
        
        if (userId != null) {
            wrapper.eq(OrderRefund::getUserId, userId);
        }
        if (refundStatus != null) {
            wrapper.eq(OrderRefund::getRefundStatus, refundStatus);
        }
        
        wrapper.orderByDesc(OrderRefund::getCreateTime);
        
        Page<OrderRefund> page = refundMapper.selectPage(
            new Page<>(pageNum, pageSize), wrapper);
        
        // 填充关联信息
        fillRefundInfo(page.getRecords());
        
        return page;
    }

    /**
     * 填充退款关联信息
     */
    private void fillRefundInfo(OrderRefund refund) {
        if (refund != null) {
            // 填充订单信息
            ServiceOrder order = orderMapper.selectById(refund.getOrderId());
            if (order != null) {
                // 填充订单的关联信息
                if (order.getStaffId() != null) {
                    ServiceStaff staff = staffMapper.selectById(order.getStaffId());
                    if (staff != null && staff.getUserId() != null) {
                        User staffUser = userMapper.selectById(staff.getUserId());
                        staff.setUser(staffUser);
                    }
                    order.setStaff(staff);
                }
                if (order.getServiceId() != null) {
                    ServiceItem serviceItem = serviceItemMapper.selectById(order.getServiceId());
                    order.setServiceItem(serviceItem);
                }
            }
            refund.setOrder(order);
            
            // 填充用户信息
            User user = userMapper.selectById(refund.getUserId());
            refund.setUser(user);
            
            // 填充审核人信息
            if (refund.getAuditUserId() != null) {
                User auditUser = userMapper.selectById(refund.getAuditUserId());
                refund.setAuditUser(auditUser);
            }
        }
    }

    /**
     * 批量填充退款关联信息
     */
    private void fillRefundInfo(List<OrderRefund> refunds) {
        if (refunds != null && !refunds.isEmpty()) {
            List<String> orderIds = refunds.stream()
                .map(OrderRefund::getOrderId).collect(Collectors.toList());
            List<Long> userIds = refunds.stream()
                .map(OrderRefund::getUserId).collect(Collectors.toList());
            
            // 查询订单
            List<ServiceOrder> orders = orderMapper.selectBatchIds(orderIds);
            
            // 收集 staffId 和 serviceItemId
            List<Long> staffIds = orders.stream()
                .map(ServiceOrder::getStaffId)
                .filter(id -> id != null)
                .collect(Collectors.toList());
            List<Long> serviceItemIds = orders.stream()
                .map(ServiceOrder::getServiceId)
                .filter(id -> id != null)
                .collect(Collectors.toList());
            
            // 批量查询
            Map<Long, ServiceStaff> staffMap = staffMapper.selectBatchIds(staffIds).stream()
                .collect(Collectors.toMap(ServiceStaff::getId, s -> s));
            Map<Long, ServiceItem> serviceItemMap = serviceItemMapper.selectBatchIds(serviceItemIds).stream()
                .collect(Collectors.toMap(ServiceItem::getId, s -> s));
            Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
            
            // 查询 staff 的 user 信息
            List<Long> staffUserIds = staffMap.values().stream()
                .map(ServiceStaff::getUserId)
                .filter(id -> id != null)
                .collect(Collectors.toList());
            if (!staffUserIds.isEmpty()) {
                Map<Long, User> staffUserMap = userMapper.selectBatchIds(staffUserIds).stream()
                    .collect(Collectors.toMap(User::getId, u -> u));
                staffMap.values().forEach(staff -> {
                    if (staff.getUserId() != null) {
                        staff.setUser(staffUserMap.get(staff.getUserId()));
                    }
                });
            }
            
            // 填充订单的关联信息
            Map<String, ServiceOrder> orderMap = orders.stream()
                .peek(order -> {
                    if (order.getStaffId() != null) {
                        order.setStaff(staffMap.get(order.getStaffId()));
                    }
                    if (order.getServiceId() != null) {
                        order.setServiceItem(serviceItemMap.get(order.getServiceId()));
                    }
                })
                .collect(Collectors.toMap(ServiceOrder::getId, o -> o));
            
            // 填充退款信息
            refunds.forEach(refund -> {
                refund.setOrder(orderMap.get(refund.getOrderId()));
                refund.setUser(userMap.get(refund.getUserId()));
                
                if (refund.getAuditUserId() != null) {
                    User auditUser = userMapper.selectById(refund.getAuditUserId());
                    refund.setAuditUser(auditUser);
                }
            });
        }
    }
}
