package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.DTO.UserPasswordUpdateDTO;
import org.example.springboot.entity.Menu;
import org.example.springboot.entity.ServiceStaff;
import org.example.springboot.entity.User;

import org.example.springboot.enumClass.AccountStatus;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.UserMapper;
import org.example.springboot.util.JwtTokenUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Resource
    private UserMapper userMapper;
    
    @Resource
    private MenuService menuService;
    
    @Value("${user.defaultPassword}")
    private String DEFAULT_PWD;

    @Resource
    private PasswordEncoder bCryptPasswordEncoder;

    @Resource
    private ServiceStaffService serviceStaffService;

    private static final String STAFF_ROLE = "STAFF";

    /**
     * 获取用户详情
     */
    public User getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        return user;
    }

    /**
     * 根据用户名查询用户
     */
    public User getByUsername(String username) {
        User user = userMapper.selectOne(
            new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
        );
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        return user;
    }

    /**
     * 分页查询用户列表
     */
    public Page<User> getUsersByPage(String username, String roleCode,
            Integer currentPage, Integer size) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like(User::getUsername, username);
        }
        if (StringUtils.isNotBlank(roleCode)) {
            queryWrapper.eq(User::getRoleCode, roleCode);
        }
        
        return userMapper.selectPage(new Page<>(currentPage, size), queryWrapper);
    }

    /**
     * 根据角色查找用户
     */
    public Page<User> getUsersByRole(String roleCode, Integer pageNum, Integer pageSize) {
        if (StringUtils.isBlank(roleCode)) {
            throw new ServiceException("角色编码不能为空");
        }

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getRoleCode, roleCode)
                   .orderByDesc(User::getCreateTime);

        return userMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);
    }

    /**
     * 删除用户(修改删除逻辑)
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteUserById(Long id) {
        // 检查用户是否存在
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }

        // 检查是否是服务人员
        if (STAFF_ROLE.equals(user.getRoleCode())) {
            throw new ServiceException("该用户是服务人员，不能直接删除");
        }

//        // 检查是否是服务人员
//        ServiceStaff staff = serviceStaffService.getServiceStaffByUserId(id);
//        if (staff != null) {
//            throw new ServiceException("该用户是服务人员，不能直接删除");
//        }

        // 执行删除
        if (userMapper.deleteById(id) <= 0) {
            throw new ServiceException("删除用户失败");
        }

        LOGGER.info("用户删除成功: {}", id);
    }

    /**
     * 批量删除用户(修改删除逻辑)
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteUsers(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        // 检查是否有服务人员
        for (Long id : ids) {
            User user = userMapper.selectById(id);
            if (user != null && STAFF_ROLE.equals(user.getRoleCode())) {
                throw new ServiceException("选中的用户中存在服务人员，不能删除");
            }
            ServiceStaff staff = serviceStaffService.getServiceStaffByUserId(id);
            if (staff != null) {
                throw new ServiceException("选中的用户中存在服务人员，不能删除");
            }
        }

        // 执行批量删除
        if (userMapper.deleteBatchIds(ids) <= 0) {
            throw new ServiceException("批量删除用户失败");
        }

        LOGGER.info("批量删除用户成功: count={}", ids.size());
    }

    public User login(User user) {
        User dbUser = getByUsername(user.getUsername());
        // 用户存在性检查已经在 getByUsername 中处理
        if (dbUser.getStatus().equals(AccountStatus.DISABLE.getValue())) {
            throw new ServiceException("账号正在审核");
        }
        if (!bCryptPasswordEncoder.matches(user.getPassword(), dbUser.getPassword())) {
            throw new ServiceException("用户名或密码错误");
        }
        // 获取用户的菜单权限
        List<Menu> menus = menuService.getMenusByRoleCode(dbUser.getRoleCode());
        String token = JwtTokenUtils.genToken(String.valueOf(dbUser.getId()), dbUser.getPassword());
        dbUser.setMenuList(menus);
        dbUser.setToken(token);
        return dbUser;
    }

    public List<User> getUserByRole(String roleCode) {
        List<User> users = userMapper.selectList(
            new LambdaQueryWrapper<User>()
                .eq(User::getRoleCode, roleCode)
        );
        if (users.isEmpty()) {
            throw new ServiceException("未找到该角色的用户");
        }
        return users;
    }

    public void createUser(User user) {
        // 检查用户名是否存在
        if (userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                    .eq(User::getUsername, user.getUsername())
            ) != null) {
            throw new ServiceException("用户名已存在");
        }
        user.setPassword(StringUtils.isNotBlank(user.getPassword()) ? user.getPassword() : DEFAULT_PWD);
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));//加密
        user.setCreateTime(LocalDateTime.now());
        if (userMapper.insert(user) <= 0) {
            throw new ServiceException("用户创建失败");
        }
    }

    /**
     * 更新用户信息(修改更新逻辑)
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(User user) {
        // 检查用户是否存在
        User existUser = userMapper.selectById(user.getId());
        if (existUser == null) {
            throw new ServiceException("用户不存在");
        }

        // 如果是服务人员，不允许修改角色
        if (STAFF_ROLE.equals(existUser.getRoleCode())) {
            if (!STAFF_ROLE.equals(user.getRoleCode())) {
                throw new ServiceException("服务人员用户不能修改角色");
            }
        }

        // 执行更新
        if (userMapper.updateById(user) <= 0) {
            throw new ServiceException("更新用户失败");
        }

        LOGGER.info("用户信息更新成功: {}", user.getId());
    }

    public void deleteBatch(List<Integer> ids) {
        if (userMapper.deleteByIds(ids) <= 0) {
            throw new ServiceException("批量删除失败");
        }
    }

    public List<User> getUserList() {
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<>());
        if (users.isEmpty()) {
            throw new ServiceException("未找到任何用户");
        }
        return users;
    }

    public void updatePassword(Long id, UserPasswordUpdateDTO update) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        
        // 验证旧密码
        if (!bCryptPasswordEncoder.matches(update.getOldPassword(), user.getPassword())) {
            throw new ServiceException("原密码错误");
        }
        
        // 更新新密码
        user.setPassword(bCryptPasswordEncoder.encode(update.getNewPassword()));
        if (userMapper.updateById(user) <= 0) {
            throw new ServiceException("密码修改失败");
        }
    }

    public void forgetPassword(String email, String newPassword) {
        User user = userMapper.selectOne(
            new LambdaQueryWrapper<User>()
                .eq(User::getEmail, email)
        );
        if (user == null) {
            throw new ServiceException("邮箱不存在");
        }
        
        user.setPassword(bCryptPasswordEncoder.encode(newPassword));
        if (userMapper.updateById(user) <= 0) {
            throw new ServiceException("密码重置失败");
        }
    }

    /**
     * 重置用户密码
     */
    public String resetPassword(Long id) {
        // 检查用户是否存在
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        
        // 重置为默认密码
        User updateUser = new User();
        updateUser.setId(id);
        updateUser.setPassword(bCryptPasswordEncoder.encode(DEFAULT_PWD));
        
        if (userMapper.updateById(updateUser) <= 0) {
            throw new ServiceException("密码重置失败");
        }
        LOGGER.info("用户密码重置成功: {}", id);
        return DEFAULT_PWD;

    }
}
