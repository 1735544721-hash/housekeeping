package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("user")
@Schema(description = "用户实体")
public class User {
    @TableId(type = IdType.AUTO)
    @Schema(description = "用户ID")
    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3到50个字符之间")
    @Schema(description = "用户名")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6到100个字符之间")
    @Schema(description = "密码(加密存储)")
    private String password;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "角色编码(USER/ADMIN)")
    private String roleCode;

    @Size(max = 50, message = "姓名长度不能超过50个字符")
    @Schema(description = "姓名")
    private String name;

    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$", 
            message = "身份证号格式不正确")
    @Schema(description = "身份证号")
    private String idCard;

    @Size(max = 200, message = "地址长度不能超过200个字符")
    @Schema(description = "地址")
    private String address;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "性别(0:女,1:男)")
    private Integer gender;

    @Min(value = 0, message = "年龄不能小于0")
    @Max(value = 150, message = "年龄不能大于150")
    @Schema(description = "年龄")
    private Integer age;

    @Schema(description = "状态(0:禁用,1:正常)")
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @TableField(exist = false)
    @Schema(description = "token信息")
    private String token;
    @TableField(exist = false)
    @Schema(description = "菜单信息")
    private List<Menu> menuList;
}
