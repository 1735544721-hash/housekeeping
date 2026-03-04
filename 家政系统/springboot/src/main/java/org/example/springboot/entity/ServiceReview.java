package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("service_review")
@Schema(description = "服务评价实体")
public class ServiceReview {
    @TableId(type = IdType.AUTO)
    @Schema(description = "评价ID")
    private Long id;

    @NotNull(message = "订单ID不能为空")
    @Schema(description = "订单ID")
    private String orderId;

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID")
    private Long userId;

    @NotNull(message = "服务人员ID不能为空")
    @Schema(description = "服务人员ID")
    private Long staffId;

    @NotNull(message = "技能评分不能为空")
    @Min(value = 1, message = "技能评分不能小于1")
    @Max(value = 5, message = "技能评分不能大于5")
    @Schema(description = "技能满意度评分(1-5)")
    private Integer skillRating;

    @NotNull(message = "态度评分不能为空")
    @Min(value = 1, message = "态度评分不能小于1")
    @Max(value = 5, message = "态度评分不能大于5")
    @Schema(description = "服务态度评分(1-5)")
    private Integer attitudeRating;

    @NotNull(message = "体验评分不能为空")
    @Min(value = 1, message = "体验评分不能小于1")
    @Max(value = 5, message = "体验评分不能大于5")
    @Schema(description = "综合体验评分(1-5)")
    private Integer experienceRating;

    @Schema(description = "总体评分")
    private BigDecimal overallRating;

    @Size(max = 1000, message = "评价内容长度不能超过1000个字符")
    @Schema(description = "评价内容")
    private String content;



    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;


    @TableField(exist = false)
    @Schema(description = "关联的用户信息")
    private User user;

    @TableField(exist = false)
    @Schema(description = "关联的服务人员信息")
    private ServiceStaff staff;

    @TableField(exist = false)
    @Schema(description = "关联的订单信息")
    private ServiceOrder order;
} 