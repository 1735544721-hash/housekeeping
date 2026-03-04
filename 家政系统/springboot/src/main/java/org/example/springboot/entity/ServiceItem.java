package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("service_item")
@Schema(description = "服务项目实体")
public class ServiceItem {
    @TableId(type = IdType.AUTO)
    @Schema(description = "服务ID")
    private Long id;

    @NotNull(message = "类别ID不能为空")
    @Schema(description = "类别ID")
    private Long categoryId;

    @NotBlank(message = "服务标题不能为空")
    @Size(max = 100, message = "标题长度不能超过100个字符")
    @Schema(description = "服务标题")
    private String title;

    @Schema(description = "服务描述")
    private String description;

    @NotNull(message = "服务价格不能为空")
    @DecimalMin(value = "0.0", message = "价格不能小于0")
    @Schema(description = "服务价格")
    private BigDecimal price;

    @Schema(description = "状态(0:下架,1:上架)")
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @TableLogic(value = "0", delval = "1")
    @Schema(description = "是否删除(0:未删除,1:已删除)")
    private Integer isDeleted;

    @TableField(exist = false)
    @Schema(description = "关联的类别信息")
    private ServiceCategory category;
} 