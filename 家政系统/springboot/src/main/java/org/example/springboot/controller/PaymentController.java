package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 支付控制器
 */
@Tag(name = "支付管理接口")
@RestController
@RequestMapping("/payment")
public class PaymentController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentController.class);

    @Resource
    private PaymentService paymentService;

    @Operation(summary = "模拟支付")
    @PostMapping("/mock")
    public Result<?> mockPayment(
            @RequestParam String orderId,
            @RequestParam String paymentMethod) {
        LOGGER.info("模拟支付请求: orderId={}, paymentMethod={}", orderId, paymentMethod);
        
        boolean success = paymentService.mockPayment(orderId, paymentMethod);
        
        if (success) {
            return Result.success("支付成功");
        } else {
            return Result.error("支付失败");
        }
    }

    @Operation(summary = "取消支付")
    @PostMapping("/cancel/{orderId}")
    public Result<?> cancelPayment(@PathVariable String orderId) {
        LOGGER.info("取消支付: orderId={}", orderId);
        paymentService.cancelPayment(orderId);
        return Result.success("取消成功");
    }

    @Operation(summary = "查询支付状态")
    @GetMapping("/status/{orderId}")
    public Result<?> getPaymentStatus(@PathVariable String orderId) {
        LOGGER.info("查询支付状态: orderId={}", orderId);
        return Result.success(paymentService.getPaymentStatus(orderId));
    }
    
    @Operation(summary = "支付宝支付")
    @GetMapping("/alipay/pay")
    public String alipayPay(@RequestParam String orderId) {
        LOGGER.info("支付宝支付请求: orderId={}", orderId);
        return paymentService.alipayPay(orderId);
    }
    
    @Operation(summary = "支付宝支付回调")
    @PostMapping("/alipay/notify")
    public String alipayNotify(@RequestParam("out_trade_no") String outTradeNo,
                              @RequestParam("trade_no") String tradeNo,
                              @RequestParam("total_amount") String totalAmount,
                              @RequestParam("trade_status") String tradeStatus) {
        LOGGER.info("支付宝回调: outTradeNo={}, tradeNo={}, totalAmount={}, tradeStatus={}", 
                    outTradeNo, tradeNo, totalAmount, tradeStatus);
        
        try {
            // 验证交易状态
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                String orderId = outTradeNo;
                BigDecimal amount = new BigDecimal(totalAmount);
                paymentService.alipayCallback(orderId, tradeNo, amount);
                return "success";
            }
            return "fail";
        } catch (Exception e) {
            LOGGER.error("支付宝回调处理失败", e);
            return "fail";
        }
    }
}
