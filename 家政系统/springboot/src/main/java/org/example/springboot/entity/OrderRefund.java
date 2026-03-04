package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单退款实体类
 */
@Data
@TableName("order_refund")
public class OrderRefund {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String orderId;
    
    private Long userId;
    
    private BigDecimal refundAmount;
    
    private String refundReason;
    
    /**
     * 退款状态
     * 1:待审核, 2:审核通过, 3:审核拒绝, 4:退款中, 5:已退款, 6:退款失败
     */
    private Integer refundStatus;
    
    /**
     * 退款类型
     * 1:用户取消, 2:服务人员取消, 3:超时未接单, 4:服务纠纷
     */
    private Integer refundType;
    
    private Long auditUserId;
    
    private LocalDateTime auditTime;
    
    private String auditRemark;
    
    private LocalDateTime refundTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer isDeleted;
    
    // 关联信息（不存储在数据库）
    @TableField(exist = false)
    private ServiceOrder order;
    
    @TableField(exist = false)
    private User user;
    
    @TableField(exist = false)
    private User auditUser;
}
