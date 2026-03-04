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
import java.util.HashMap;

@Service
public class ServiceFavoriteService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceFavoriteService.class);

    @Resource
    private ServiceFavoriteMapper favoriteMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private ServiceItemMapper serviceItemMapper;

    /**
     * 添加收藏
     */
    @Transactional(rollbackFor = Exception.class)
    public void addFavorite(ServiceFavorite favorite) {
        // 检查用户是否存在
        if (userMapper.selectById(favorite.getUserId()) == null) {
            throw new ServiceException("用户不存在");
        }

        // 检查服务项目是否存在
        ServiceItem item = serviceItemMapper.selectById(favorite.getServiceId());
        if (item == null) {
            throw new ServiceException("服务项目不存在");
        }
        if (item.getStatus() == 0) {
            throw new ServiceException("该服务项目已下架");
        }

        // 检查是否已收藏
        if (favoriteMapper.selectCount(new LambdaQueryWrapper<ServiceFavorite>()
                .eq(ServiceFavorite::getUserId, favorite.getUserId())
                .eq(ServiceFavorite::getServiceId, favorite.getServiceId())) > 0) {
            throw new ServiceException("已经收藏过该服务项目");
        }
        favorite.setCreateTime(LocalDateTime.now());

        // 保存收藏信息
        if (favoriteMapper.insert(favorite) <= 0) {
            throw new ServiceException("添加收藏失败");
        }

        LOGGER.info("添加收藏成功: {}", favorite.getId());
    }

    /**
     * 取消收藏
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelFavorite(Long userId, Long serviceId) {
        int count = favoriteMapper.delete(new LambdaQueryWrapper<ServiceFavorite>()
                .eq(ServiceFavorite::getUserId, userId)
                .eq(ServiceFavorite::getServiceId, serviceId));

        if (count <= 0) {
            throw new ServiceException("取消收藏失败");
        }

        LOGGER.info("取消收藏成功: userId={}, serviceId={}", userId, serviceId);
    }
        /**
     * 获取收藏详情
     */
    public ServiceFavorite getFavoriteById(Long id) {
        ServiceFavorite favorite = favoriteMapper.selectById(id);
        if (favorite == null) {
            throw new ServiceException("收藏记录不存在");
        }
        fillFavoriteInfo(favorite);
        return favorite;
    }

    /**
     * 分页查询收藏列表
     */
    public Page<ServiceFavorite> getFavoritesByPage(
            Long userId, Long serviceId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<ServiceFavorite> queryWrapper = new LambdaQueryWrapper<>();
        
        if (userId != null) {
            queryWrapper.eq(ServiceFavorite::getUserId, userId);
        }
        if (serviceId != null) {
            queryWrapper.eq(ServiceFavorite::getServiceId, serviceId);
        }

        queryWrapper.orderByDesc(ServiceFavorite::getCreateTime);
        
        Page<ServiceFavorite> page = favoriteMapper.selectPage(
            new Page<>(pageNum, pageSize), queryWrapper);
        
        fillFavoriteInfo(page.getRecords());
        
        return page;
    }

    /**
     * 检查是否已收藏
     */
    public boolean isFavorite(Long userId, Long serviceId) {
        return favoriteMapper.selectCount(new LambdaQueryWrapper<ServiceFavorite>()
                .eq(ServiceFavorite::getUserId, userId)
                .eq(ServiceFavorite::getServiceId, serviceId)) > 0;
    }

    /**
     * 填充收藏关联信息
     */
    private void fillFavoriteInfo(ServiceFavorite favorite) {
        if (favorite != null) {
            // 填充用户信息
            User user = userMapper.selectById(favorite.getUserId());
            favorite.setUser(user);

            // 填充服务项目信息
            ServiceItem item = serviceItemMapper.selectById(favorite.getServiceId());
            favorite.setServiceItem(item);
        }
    }

    /**
     * 批量填充收藏关联信息
     */
    private void fillFavoriteInfo(List<ServiceFavorite> favorites) {
        if (favorites != null && !favorites.isEmpty()) {
            // 收集所有ID
            List<Long> userIds = favorites.stream()
                .map(ServiceFavorite::getUserId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
            
            List<Long> serviceIds = favorites.stream()
                .map(ServiceFavorite::getServiceId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

            // 批量查询关联信息
            Map<Long, User> userMap = !userIds.isEmpty() ? 
                userMapper.selectBatchIds(userIds).stream()
                    .collect(Collectors.toMap(User::getId, user -> user)) : 
                new HashMap<>();
            
            Map<Long, ServiceItem> itemMap = !serviceIds.isEmpty() ? 
                serviceItemMapper.selectBatchIds(serviceIds).stream()
                    .collect(Collectors.toMap(ServiceItem::getId, item -> item)) : 
                new HashMap<>();

            // 填充关联信息
            favorites.forEach(favorite -> {
                favorite.setUser(userMap.get(favorite.getUserId()));
                favorite.setServiceItem(itemMap.get(favorite.getServiceId()));
            });
        }
    }
} 