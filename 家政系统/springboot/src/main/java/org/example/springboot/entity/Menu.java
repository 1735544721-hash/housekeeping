package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description ="菜单类实体")
@TableName("sys_menu")
public class Menu {
    @Schema(description ="菜单id")
    @TableId(type = IdType.AUTO)
    private Integer id;

    @Schema(description ="菜单名")
    private String name;

    @Schema(description ="菜单路径")
    private String path;

    @Schema(description ="组件路径")
    private String component;

    @Schema(description ="菜单图标")
    private String icon;

    private String description;

    @Schema(description ="父级菜单id")
    private Integer pid;

    @Schema(description ="排序")
    @TableField("sort_num")
    private Integer sortNum;

    @Schema(description ="是否隐藏")
    private Boolean hidden;

    @Schema(description = "创建时间")
    @TableField(value = "created_time",fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @Schema(description = "更新时间")


    @TableField(value = "updated_time",fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    @TableField(exist = false)
    private List<Menu> children;

    @TableField(exist = false)
    private Boolean hasChildren = false;
}
