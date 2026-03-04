package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("sys_role")
@Schema(description = "角色实体")
public class Role {
    @TableId(type = IdType.AUTO)
    @Schema(description = "角色ID")
    private Integer id;
    
    @NotBlank(message = "角色编码不能为空")
    @Size(min = 2, max = 50, message = "角色编码长度必须在2到50个字符之间")
    @Schema(description = "角色编码")
    private String code;
    
    @NotBlank(message = "角色名称不能为空")
    @Size(min = 2, max = 50, message = "角色名称长度必须在2到50个字符之间")
    @Schema(description = "角色名称")
    private String name;
    @TableLogic
    @Schema(description = "是否删除(0:未删除,1:已删除)")
    private Integer isDeleted;

    @Schema(description = "描述")
    private String description;
    
    @Schema(description = "创建时间")
    @TableField(value = "created_time",fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
    
    @Schema(description = "更新时间")


    @TableField(value = "updated_time",fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    // 非数据库字段
    @TableField(exist = false)
    private List<Menu> menuList;
} 