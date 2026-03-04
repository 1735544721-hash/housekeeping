package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_favorite")
@Schema(description = "服务收藏实体")
public class ServiceFavorite {
    @TableId(type = IdType.AUTO)
    @Schema(description = "收藏ID")
    private Long id;

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID")
    private Long userId;

    @NotNull(message = "服务ID不能为空")
    @Schema(description = "服务ID")
    private Long serviceId;



    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;




    @TableField(exist = false)
    @Schema(description = "关联的用户信息")
    private User user;

    @TableField(exist = false)
    @Schema(description = "关联的服务信息")
    private ServiceItem serviceItem;
} 