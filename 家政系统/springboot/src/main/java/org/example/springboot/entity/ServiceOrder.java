package org.example.springboot.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("service_order")
@Schema(description = "服务订单实体")
public class ServiceOrder {
    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "订单ID")
    private String id;

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID")
    private Long userId;

    @NotNull(message = "服务人员ID不能为空")
    @Schema(description = "服务人员ID")
    private Long staffId;

    @NotNull(message = "服务项目ID不能为空")
    @Schema(description = "服务项目ID")
    private Long serviceId;

    @Schema(description = "订单状态(1:待支付,2:待接单,3:已接单,4:服务中,5:已完成,6:已取消,7:已关闭)")
    private Integer orderStatus;

    @Schema(description = "服务开始时间")
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime serviceTime;
    
    @Schema(description = "服务时长(小时)")
    private Float duration;

    @NotNull(message = "订单金额不能为空")
    @DecimalMin(value = "0.0", message = "订单金额不能小于0")
    @Schema(description = "订单金额")
    private BigDecimal totalAmount;
    
    @Schema(description = "支付方式(WECHAT:微信,ALIPAY:支付宝,BALANCE:余额)")
    private String paymentMethod;
    
    @Schema(description = "支付时间")
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentTime;
    
    @Schema(description = "实付金额")
    private BigDecimal paidAmount;
    
    @Schema(description = "退款金额")
    private BigDecimal refundAmount;
    
    @Schema(description = "退款状态(0:无退款,1:退款中,2:已退款,3:退款失败)")
    private Integer refundStatus;

    @Size(max = 500, message = "取消原因长度不能超过500个字符")
    @Schema(description = "取消原因")
    private String cancelReason;
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "取消时间")
    private LocalDateTime cancelTime;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "完成时间")
    private LocalDateTime completeTime;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    @Schema(description = "备注")
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableLogic(value = "0", delval = "1")
    @Schema(description = "是否删除(0:未删除,1:已删除)")
    private Integer isDeleted;

    @TableField(exist = false)
    @Schema(description = "关联的用户信息")
    private User user;

    @TableField(exist = false)
    @Schema(description = "关联的服务人员信息")
    private ServiceStaff staff;

    @TableField(exist = false)
    @Schema(description = "关联的服务项目信息")
    private ServiceItem serviceItem;

    @TableField(exist = false)
    @Schema(description = "关联的评价信息")
    private ServiceReview review;
} 