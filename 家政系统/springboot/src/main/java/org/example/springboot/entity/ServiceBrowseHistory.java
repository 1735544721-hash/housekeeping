package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("browse_history")
@Schema(description = "服务浏览记录实体")
public class ServiceBrowseHistory {
    @TableId(type = IdType.AUTO)
    @Schema(description = "记录ID")
    private Long id;

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID")
    private Long userId;

    @NotNull(message = "服务项目ID不能为空")
    @Schema(description = "服务项目ID")
    private Long serviceId;



    @Schema(description = "最后浏览时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime lastBrowseTime;






    @TableField(exist = false)
    @Schema(description = "关联的用户信息")
    private User user;

    @TableField(exist = false)
    @Schema(description = "关联的服务项目信息")
    private ServiceItem serviceItem;
} 