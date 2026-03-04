package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.example.springboot.DTO.UserPasswordUpdateDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.springboot.common.Result;
import org.example.springboot.entity.User;

import org.example.springboot.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户管理接口")
@RestController
@RequestMapping("/user")

public class UserController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Resource
    private UserService userService;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<?> register(@RequestBody  User user) {
        LOGGER.info("用户注册: {}", user.getUsername());
        userService.createUser(user);
        return Result.success("注册成功");
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<?> login(@RequestBody  User user) {
        LOGGER.info("用户登录: {}", user.getUsername());
        return Result.success(userService.login(user));
    }

//    @Operation(summary = "获取用户信息")
//    @GetMapping("/info")
//
//    public Result<?> getUserInfo() {
//        LOGGER.info("获取用户信息");
//        return Result.success(userService.get());
//    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/info")

    public Result<?> updateUserInfo(@RequestBody @Valid User user) {
        LOGGER.info("更新用户信息: {}", user.getId());
        userService.updateUser( user);
        return Result.success("更新成功");
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password/{id}")

    public Result<?> updatePassword(@PathVariable Long id, @RequestBody UserPasswordUpdateDTO dto) {
        LOGGER.info("修改密码");
        userService.updatePassword(id,dto);
        return Result.success("密码修改成功");
    }

    @Operation(summary = "获取用户列表")
    @GetMapping("/list")

    public Result<?> getUserList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String roleCode) {
        LOGGER.info("获取用户列表: pageNum={}, pageSize={}", pageNum, pageSize);
        return Result.success(userService.getUsersByPage(username, roleCode, pageNum, pageSize));
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")

    public Result<?> deleteUser(@PathVariable Long id) {
        LOGGER.info("删除用户: {}", id);
        userService.deleteUserById(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "批量删除用户")
    @DeleteMapping("/batch")
    public Result<?> batchDeleteUsers(@RequestBody List<Long> ids) {
        LOGGER.info("批量删除用户: {}", ids);
        userService.batchDeleteUsers(ids);
        return Result.success("批量删除成功");
    }

    @Operation(summary = "重置用户密码")
    @PutMapping("/{id}/reset-password")
    public Result<?> resetPassword(@PathVariable Long id) {
        LOGGER.info("重置用户密码: {}", id);
      String newPwd=  userService.resetPassword(id);
        return Result.success("密码重置成功,新密码为："+newPwd);
    }

    @Operation(summary = "根据角色查找用户")
    @GetMapping("/role/{roleCode}")
    public Result<?> getUsersByRole(
            @PathVariable String roleCode
           ) {
        LOGGER.info("根据角色查找用户: roleCode={}", roleCode);
        return Result.success(userService.getUsersByPage(null, roleCode, 1,Integer.MAX_VALUE));
    }
}
