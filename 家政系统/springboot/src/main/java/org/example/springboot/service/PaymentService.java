package org.example.springboot.service;

import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import jakarta.annotation.Resource;
import org.example.springboot.config.AlipayConfig;
import org.example.springboot.entity.ServiceOrder;
import org.example.springboot.enumClass.OrderStatus;
import org.example.springboot.enumClass.PaymentMethod;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.ServiceOrderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付服务（模拟支付）
 */
@Service
public class PaymentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentService.class);

    @Resource
    private ServiceOrderMapper orderMapper;
    
    @Resource
    private AlipayClient alipayClient;
    
    @Resource
    private AlipayConfig alipayConfig;

    /**
     * 模拟支付
     * @param orderId 订单ID
     * @param paymentMethod 支付方式
     * @return 支付结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean mockPayment(String orderId, String paymentMethod) {
        LOGGER.info("开始模拟支付: orderId={}, paymentMethod={}", orderId, paymentMethod);

        // 1. 查询订单
        ServiceOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }

        // 2. 检查订单状态
        if (!OrderStatus.WAITING_PAY.getValue().equals(order.getOrderStatus())) {
            throw new ServiceException("订单状态不正确，当前状态：" + 
                OrderStatus.getByValue(order.getOrderStatus()).getDesc());
        }

        // 3. 验证支付方式
        PaymentMethod method = PaymentMethod.getByCode(paymentMethod);
        if (method == null) {
            throw new ServiceException("不支持的支付方式");
        }

        // 4. 模拟支付处理（实际项目中这里会调用第三方支付接口）
        try {
            // 模拟网络延迟
            Thread.sleep(1000);
            
            // 模拟支付成功（实际项目中需要等待支付回调）
            LOGGER.info("模拟支付成功: orderId={}", orderId);
            
            // 5. 更新订单状态
            ServiceOrder updateOrder = new ServiceOrder();
            updateOrder.setId(orderId);
            updateOrder.setOrderStatus(OrderStatus.WAITING_ACCEPT.getValue());
            updateOrder.setPaymentMethod(paymentMethod);
            updateOrder.setPaymentTime(LocalDateTime.now());
            updateOrder.setPaidAmount(order.getTotalAmount());
            updateOrder.setRefundAmount(BigDecimal.ZERO);
            updateOrder.setRefundStatus(0);
            
            int result = orderMapper.updateById(updateOrder);
            if (result <= 0) {
                throw new ServiceException("更新订单状态失败");
            }
            
            LOGGER.info("订单支付成功: orderId={}, amount={}", orderId, order.getTotalAmount());
            return true;
            
        } catch (InterruptedException e) {
            LOGGER.error("模拟支付异常", e);
            Thread.currentThread().interrupt();
            throw new ServiceException("支付处理异常");
        }
    }

    /**
     * 取消支付
     * @param orderId 订单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelPayment(String orderId) {
        LOGGER.info("取消支付: orderId={}", orderId);

        ServiceOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }

        if (!OrderStatus.WAITING_PAY.getValue().equals(order.getOrderStatus())) {
            throw new ServiceException("只能取消待支付的订单");
        }

        // 更新订单状态为已取消
        ServiceOrder updateOrder = new ServiceOrder();
        updateOrder.setId(orderId);
        updateOrder.setOrderStatus(OrderStatus.CANCELLED.getValue());
        updateOrder.setCancelReason("用户取消支付");
        updateOrder.setCancelTime(LocalDateTime.now());

        orderMapper.updateById(updateOrder);
        LOGGER.info("取消支付成功: orderId={}", orderId);
    }

    /**
     * 查询支付状态
     * @param orderId 订单ID
     * @return 订单信息
     */
    public ServiceOrder getPaymentStatus(String orderId) {
        ServiceOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        return order;
    }
    
    /**
     * 支付宝沙箱支付
     * @param orderId 订单ID
     * @return 支付表单HTML
     */
    public String alipayPay(String orderId) {
        LOGGER.info("开始支付宝支付: orderId={}", orderId);
        
        // 1. 查询订单
        ServiceOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        
        // 2. 检查订单状态
        if (!OrderStatus.WAITING_PAY.getValue().equals(order.getOrderStatus())) {
            throw new ServiceException("订单状态不正确，当前状态：" + 
                OrderStatus.getByValue(order.getOrderStatus()).getDesc());
        }
        
        try {
            // 3. 创建支付请求
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            
            // 设置异步通知地址
            request.setNotifyUrl(alipayConfig.getNotifyUrl());
            
            // 设置同步跳转地址
            request.setReturnUrl(alipayConfig.getReturnUrl());
            
            // 设置请求参数
            String bizContent = String.format(
                "{\"out_trade_no\":\"%s\",\"total_amount\":\"%s\",\"subject\":\"%s\",\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}",
                orderId,
                order.getTotalAmount(),
                "家政服务-" + (order.getServiceItem() != null ? order.getServiceItem().getTitle() : "订单" + orderId)
            );
            request.setBizContent(bizContent);
            
            // 4. 执行请求
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
            
            if (response.isSuccess()) {
                LOGGER.info("支付宝支付表单生成成功: orderId={}", orderId);
                return response.getBody();
            } else {
                LOGGER.error("支付宝支付表单生成失败: orderId={}, msg={}", orderId, response.getMsg());
                throw new ServiceException("支付宝支付失败：" + response.getMsg());
            }
            
        } catch (Exception e) {
            LOGGER.error("支付宝支付异常: orderId={}", orderId, e);
            throw new ServiceException("支付宝支付异常：" + e.getMessage());
        }
    }
    
    /**
     * 支付宝支付回调处理
     * @param orderId 订单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void alipayCallback(String orderId, String tradeNo, BigDecimal totalAmount) {
        LOGGER.info("处理支付宝回调: orderId={}, tradeNo={}, totalAmount={}", orderId, tradeNo, totalAmount);
        
        ServiceOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        
        // 检查订单状态，避免重复处理
        if (!OrderStatus.WAITING_PAY.getValue().equals(order.getOrderStatus())) {
            LOGGER.warn("订单已支付或已取消，忽略回调: orderId={}", orderId);
            return;
        }
        
        // 更新订单状态
        ServiceOrder updateOrder = new ServiceOrder();
        updateOrder.setId(orderId);
        updateOrder.setOrderStatus(OrderStatus.WAITING_ACCEPT.getValue());
        updateOrder.setPaymentMethod(PaymentMethod.ALIPAY.getCode());
        updateOrder.setPaymentTime(LocalDateTime.now());
        updateOrder.setPaidAmount(totalAmount);
        updateOrder.setRefundAmount(BigDecimal.ZERO);
        updateOrder.setRefundStatus(0);
        
        orderMapper.updateById(updateOrder);
        LOGGER.info("订单支付成功: orderId={}, amount={}", orderId, totalAmount);
    }
}
