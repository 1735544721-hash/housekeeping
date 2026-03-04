package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.example.springboot.entity.Menu;
import org.example.springboot.entity.Role;
import org.example.springboot.entity.RoleMenu;
import org.example.springboot.entity.User;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.MenuMapper;
import org.example.springboot.mapper.RoleMapper;
import org.example.springboot.mapper.RoleMenuMapper;
import org.example.springboot.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoleService.class);

    // 添加系统保留的角色编码常量
    private static final List<String> PROTECTED_ROLE_CODES = List.of("USER", "ADMIN");

    @Resource
    private RoleMapper roleMapper;
    
    @Resource
    private RoleMenuMapper roleMenuMapper;
    
    @Resource
    private MenuMapper menuMapper;

    @Resource
    private UserMapper userMapper;

    @Transactional
    public void createRole(Role role) {
        // 检查角色编码是否已存在
        if (isCodeExists(role.getCode())) {
            throw new ServiceException("角色编码已存在");
        }

        // 设置创建和更新时间
        LocalDateTime now = LocalDateTime.now();
        role.setCreatedTime(now);
        role.setUpdatedTime(now);
        
        if (roleMapper.insert(role) <= 0) {
            throw new ServiceException("角色创建失败");
        }
    }

    @Transactional
    public void updateRole(Role role) {
        // 检查角色是否存在
        Role existingRole = roleMapper.selectById(role.getId());
        if (existingRole == null) {
            throw new ServiceException("角色不存在");
        }

        // 检查是否是系统保留角色
        if (PROTECTED_ROLE_CODES.contains(existingRole.getCode())) {
            // 不允许修改角色编码
            if (!existingRole.getCode().equals(role.getCode())) {
                throw new ServiceException("系统保留角色不允许修改角色编码");
            }
        } else if (!existingRole.getCode().equals(role.getCode()) && isCodeExists(role.getCode())) {
            // 如果不是系统角色且修改了编码，检查新编码是否已存在
            throw new ServiceException("角色编码已存在");
        }

        // 设置更新时间
        role.setUpdatedTime(LocalDateTime.now());
        
        if (roleMapper.updateById(role) <= 0) {
            throw new ServiceException("角色更新失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Integer id) {
        // 检查角色是否存在且未删除
        Role role = roleMapper.selectOne(
            new LambdaQueryWrapper<Role>()
                .eq(Role::getId, id)
                .eq(Role::getIsDeleted, 0)
        );
        if (role == null) {
            throw new ServiceException("角色不存在");
        }

        // 检查是否是系统保留角色
        if (PROTECTED_ROLE_CODES.contains(role.getCode())) {
            throw new ServiceException("系统保留角色不允许删除");
        }

        // 检查是否有用户关联此角色
        Long userCount = userMapper.selectCount(
            new LambdaQueryWrapper<User>()
                .eq(User::getRoleCode, role.getCode())
        );
        if (userCount > 0) {
            throw new ServiceException("该角色下存在用户,不能删除");
        }

        // 删除角色菜单关联
        roleMenuMapper.delete(
            new LambdaQueryWrapper<RoleMenu>()
                .eq(RoleMenu::getRoleId, id)
        );

        // 执行软删除(手动设置isDeleted为1)
        Role updateRole = new Role();
        updateRole.setId(id);
        updateRole.setIsDeleted(1);
        if (roleMapper.updateById(updateRole) <= 0) {
            throw new ServiceException("删除角色失败");
        }

        LOGGER.info("角色软删除成功: {}", id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteRoles(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        // 检查角色是否存在
        List<Role> roles = roleMapper.selectList(
            new LambdaQueryWrapper<Role>()
                .in(Role::getId, ids)
                .eq(Role::getIsDeleted, 0)
        );
        if (roles.size() != ids.size()) {
            throw new ServiceException("部分角色不存在");
        }

        // 检查是否包含系统保留角色
        boolean hasProtectedRole = roles.stream()
                .anyMatch(role -> PROTECTED_ROLE_CODES.contains(role.getCode()));
        if (hasProtectedRole) {
            throw new ServiceException("选中的角色包含系统保留角色，不能删除");
        }

        // 检查是否有用户关联这些角色
        List<String> roleCodes = roles.stream()
                .map(Role::getCode)
                .collect(Collectors.toList());
        
        Long userCount = userMapper.selectCount(
            new LambdaQueryWrapper<User>()
                .in(User::getRoleCode, roleCodes)

        );
        if (userCount > 0) {
            throw new ServiceException("选中的角色中存在关联用户,不能删除");
        }

        // 批量删除角色菜单关联
        roleMenuMapper.delete(
            new LambdaQueryWrapper<RoleMenu>()
                .in(RoleMenu::getRoleId, ids)
        );

        // 执行批量软删除
        for (Integer id : ids) {
            Role updateRole = new Role();
            updateRole.setId(id);
            updateRole.setIsDeleted(1);
            if (roleMapper.updateById(updateRole) <= 0) {
                throw new ServiceException("批量删除角色失败");
            }
        }

        LOGGER.info("批量软删除角色成功: count={}", ids.size());
    }

    public Role getRoleById(Integer id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new ServiceException("角色不存在");
        }
        return role;
    }

    public Role getRoleByCode(String code) {
        return roleMapper.selectOne(
            new LambdaQueryWrapper<Role>()
                .eq(Role::getCode, code)
        );
    }

    public List<Role> getAllRoles() {
        return roleMapper.selectList(
            new LambdaQueryWrapper<Role>()
                .orderByAsc(Role::getCode)
        );
    }

    public Page<Role> getRolesByPage(String code, String name, Integer currentPage, Integer pageSize) {
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        
        if (code != null && !code.trim().isEmpty()) {
            queryWrapper.like(Role::getCode, code);
        }
        if (name != null && !name.trim().isEmpty()) {
            queryWrapper.like(Role::getName, name);
        }
        
        queryWrapper.orderByAsc(Role::getCode);
        
        return roleMapper.selectPage(new Page<>(currentPage, pageSize), queryWrapper);
    }

    public List<Menu> getRoleMenus(Integer roleId) {
        // 获取角色菜单关联
        List<RoleMenu> roleMenus = roleMenuMapper.selectList(
            new LambdaQueryWrapper<RoleMenu>()
                .eq(RoleMenu::getRoleId, roleId)
        );

        // 获取菜单ID列表
        List<Integer> menuIds = roleMenus.stream()
                .map(RoleMenu::getMenuId)
                .collect(Collectors.toList());

        // 如果没有关联菜单，返回空列表
        if (menuIds.isEmpty()) {
            return List.of();
        }

        // 查询菜单信息
        return menuMapper.selectList(
            new LambdaQueryWrapper<Menu>()
                .in(Menu::getId, menuIds)
                .orderByAsc(Menu::getSortNum)
        );
    }

    @Transactional
    public void assignMenusToRole(Integer roleId, List<Integer> menuIds) {
        // 检查角色是否存在
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new ServiceException("角色不存在");
        }

        // 如果menuIds为空，直接清空角色菜单关联并返回
        if (menuIds == null || menuIds.isEmpty()) {
            roleMenuMapper.delete(
                new LambdaQueryWrapper<RoleMenu>()
                    .eq(RoleMenu::getRoleId, roleId)
            );
            return;
        }

        // 获取所有选中菜单及其父菜单
        List<Menu> selectedMenus = menuMapper.selectList(
            new LambdaQueryWrapper<Menu>()
                .in(Menu::getId, menuIds)
        );
        
        if (selectedMenus.size() != menuIds.size()) {
            throw new ServiceException("存在无效的菜单ID");
        }

        // 收集所有需要添加的菜单ID（包括必要的父菜单）
        List<Integer> allMenuIds = new java.util.ArrayList<>(menuIds);
        
        // 检查每个选中菜单的父菜单
        for (Menu menu : selectedMenus) {
            Integer parentId = menu.getPid();
            while (parentId != null && parentId != 0) {
                // 检查父菜单是否已分配给该角色
                Long count = roleMenuMapper.selectCount(
                    new LambdaQueryWrapper<RoleMenu>()
                        .eq(RoleMenu::getRoleId, roleId)
                        .eq(RoleMenu::getMenuId, parentId)
                );
                
                // 如果父菜单未分配且不在待分配列表中，则添加到待分配列表
                if (count == 0 && !allMenuIds.contains(parentId)) {
                    Menu parentMenu = menuMapper.selectById(parentId);
                    if (parentMenu == null) {
                        break;
                    }
                    allMenuIds.add(parentId);
                }
                Menu parentMenu = menuMapper.selectById(parentId);
                if (parentMenu == null) {
                    break;
                }
                parentId = parentMenu.getPid();
            }
        }

        // 删除原有的角色菜单关联
        roleMenuMapper.delete(
            new LambdaQueryWrapper<RoleMenu>()
                .eq(RoleMenu::getRoleId, roleId)
        );

        // 批量插入新的角色菜单关联（包含必要的父菜单）
        LocalDateTime now = LocalDateTime.now();
        for (Integer menuId : allMenuIds) {
            RoleMenu roleMenu = new RoleMenu();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            roleMenu.setCreatedTime(now);
            roleMenuMapper.insert(roleMenu);
        }

        // 更新角色的更新时间
        Role updateRole = new Role();
        updateRole.setId(roleId);
        updateRole.setUpdatedTime(now);
        roleMapper.updateById(updateRole);
    }

    public List<Integer> getRoleMenuIds(Integer roleId) {
        return roleMenuMapper.selectList(
            new LambdaQueryWrapper<RoleMenu>()
                .eq(RoleMenu::getRoleId, roleId)
        ).stream()
        .map(RoleMenu::getMenuId)
        .collect(Collectors.toList());
    }

    private boolean isCodeExists(String code) {
        return roleMapper.selectCount(
            new LambdaQueryWrapper<Role>()
                .eq(Role::getCode, code)
        ) > 0;
    }
} 