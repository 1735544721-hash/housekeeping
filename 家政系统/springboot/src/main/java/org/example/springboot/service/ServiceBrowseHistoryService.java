package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.entity.*;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ServiceBrowseHistoryService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBrowseHistoryService.class);

    @Resource
    private ServiceBrowseHistoryMapper historyMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private ServiceItemMapper serviceItemMapper;

    /**
     * 记录浏览历史
     */
    @Transactional(rollbackFor = Exception.class)
    public void recordBrowseHistory(Long userId, Long serviceId) {
        // 检查用户是否存在
        if (userMapper.selectById(userId) == null) {
            throw new ServiceException("用户不存在");
        }

        // 检查服务项目是否存在
        if (serviceItemMapper.selectById(serviceId) == null) {
            throw new ServiceException("服务项目不存在");
        }

        // 查询是否已有浏览记录
        ServiceBrowseHistory history = historyMapper.selectOne(
            new LambdaQueryWrapper<ServiceBrowseHistory>()
                .eq(ServiceBrowseHistory::getUserId, userId)
                .eq(ServiceBrowseHistory::getServiceId, serviceId)
        );

        if (history == null) {
            // 创建新记录
            history = new ServiceBrowseHistory();
            history.setUserId(userId);
            history.setServiceId(serviceId);

            history.setLastBrowseTime(LocalDateTime.now());
            
            if (historyMapper.insert(history) <= 0) {
                throw new ServiceException("记录浏览历史失败");
            }
        } else {
            // 更新已有记录
            history.setLastBrowseTime(LocalDateTime.now());
                        if (historyMapper.updateById(history) <= 0) {
                throw new ServiceException("更新浏览历史失败");
            }
        }
        LOGGER.info("记录浏览历史成功: userId={}, serviceId={}", userId, serviceId);
    }

    /**
     * 清除浏览历史
     */
    @Transactional(rollbackFor = Exception.class)
    public void clearBrowseHistory(Long userId) {
        int count = historyMapper.delete(
            new LambdaQueryWrapper<ServiceBrowseHistory>()
                .eq(ServiceBrowseHistory::getUserId, userId)
        );
        LOGGER.info("清除浏览历史成功: userId={}, count={}", userId, count);
    }

    /**
     * 获取浏览历史详情
     */
    public ServiceBrowseHistory getHistoryById(Long id) {
        ServiceBrowseHistory history = historyMapper.selectById(id);
        if (history == null) {
            throw new ServiceException("浏览记录不存在");
        }
        fillHistoryInfo(history);
        return history;
    }

    /**
     * 分页查询浏览历史
     */
    public Page<ServiceBrowseHistory> getHistoryByPage(
            Long userId, Long serviceId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<ServiceBrowseHistory> queryWrapper = new LambdaQueryWrapper<>();
        
        if (userId != null) {
            queryWrapper.eq(ServiceBrowseHistory::getUserId, userId);
        }
        if (serviceId != null) {
            queryWrapper.eq(ServiceBrowseHistory::getServiceId, serviceId);
        }

        queryWrapper.orderByDesc(ServiceBrowseHistory::getLastBrowseTime);
        
        Page<ServiceBrowseHistory> page = historyMapper.selectPage(
            new Page<>(pageNum, pageSize), queryWrapper);
        
        fillHistoryInfo(page.getRecords());
        
        return page;
    }

    /**
     * 填充浏览记录关联信息
     */
    private void fillHistoryInfo(ServiceBrowseHistory history) {
        if (history != null) {
            // 填充用户信息
            User user = userMapper.selectById(history.getUserId());
            history.setUser(user);

            // 填充服务项目信息
            ServiceItem serviceItem = serviceItemMapper.selectById(history.getServiceId());
            history.setServiceItem(serviceItem);
        }
    }

    /**
     * 批量填充浏览记录关联信息
     */
    private void fillHistoryInfo(List<ServiceBrowseHistory> histories) {
        if (histories != null && !histories.isEmpty()) {
            // 收集所有ID
            List<Long> userIds = histories.stream()
                .map(ServiceBrowseHistory::getUserId)
                .collect(Collectors.toList());
            List<Long> serviceIds = histories.stream()
                .map(ServiceBrowseHistory::getServiceId)
                .collect(Collectors.toList());

            // 批量查询关联信息
            Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));
            Map<Long, ServiceItem> serviceMap = serviceItemMapper.selectBatchIds(serviceIds).stream()
                .collect(Collectors.toMap(ServiceItem::getId, item -> item));

            // 填充关联信息
            histories.forEach(history -> {
                history.setUser(userMap.get(history.getUserId()));
                history.setServiceItem(serviceMap.get(history.getServiceId()));
            });
        }
    }
} 