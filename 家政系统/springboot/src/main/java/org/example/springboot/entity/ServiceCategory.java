package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("service_category")
@Schema(description = "服务类别实体")
public class ServiceCategory {
    @TableId(type = IdType.AUTO)
    @Schema(description = "类别ID")
    private Long id;

    @NotBlank(message = "类别名称不能为空")
    @Size(max = 50, message = "类别名称长度不能超过50个字符")
    @Schema(description = "类别名称")
    private String categoryName;

    @Schema(description = "父类别ID")
    private Long parentId;

    @Size(max = 500, message = "描述长度不能超过500个字符")
    @Schema(description = "描述")
    private String description;

    @Schema(description = "图标URL")
    private String icon;

    @Schema(description = "排序号")
    private Integer sortNum;

    @Schema(description = "状态(0:禁用,1:正常)")
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @TableLogic
    @Schema(description = "是否删除(0:未删除,1:已删除)")
    private Integer isDeleted;

    @TableField(exist = false)
    @Schema(description = "子类别列表")
    private List<ServiceCategory> children;

    @TableField(exist = false)
    @Schema(description = "是否有子类别")
    private Boolean hasChildren;
} 