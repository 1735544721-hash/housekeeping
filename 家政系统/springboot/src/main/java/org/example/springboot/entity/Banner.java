package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("banner")
@Schema(description = "轮播图实体")
public class Banner {
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @NotBlank(message = "轮播图标题不能为空")
    @Size(max = 100, message = "标题长度不能超过100个字符")
    @Schema(description = "轮播图标题")
    private String title;

    @NotBlank(message = "图片URL不能为空")
    @Schema(description = "图片URL")
    private String imageUrl;

    @Size(max = 255, message = "描述长度不能超过255个字符")
    @Schema(description = "图片描述")
    private String description;

    @Size(max = 50, message = "标签长度不能超过50个字符")
    @Schema(description = "标签")
    private String tag;

    @Schema(description = "状态(0:禁用,1:启用)")
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
} 