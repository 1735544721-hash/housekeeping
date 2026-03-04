package org.example.springboot.service.recommendation;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.example.springboot.entity.*;
import org.example.springboot.enumClass.OrderStatus;
import org.example.springboot.mapper.*;
import org.example.springboot.service.ServiceItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 基于用户的协同过滤推荐服务
 * 通过分析用户的相似度来进行推荐，找到与目标用户相似的用户群体，推荐他们使用过的服务
 */
@Service
public class UserBasedRecommendationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserBasedRecommendationService.class);

    @Resource
    private UserMapper userMapper;
    
    @Resource
    private ServiceOrderMapper orderMapper;
    
    @Resource
    private ServiceItemMapper serviceItemMapper;

    @Resource
    private ServiceItemService serviceItemService;

    /**
     * 获取推荐服务列表
     * @param userId 目标用户ID
     * @param limit 推荐数量限制
     * @return 推荐的服务列表
     */
    public List<ServiceItem> getRecommendations(Long userId, Integer limit) {
        // 1. 获取目标用户信息
        User targetUser = userMapper.selectById(userId);
        if (targetUser == null) {
            return new ArrayList<>();
        }
        // 2. 获取所有用户及其订单信息
        List<User> allUsers = userMapper.selectList(null);
        Map<Long, List<ServiceOrder>> userOrders = getUserOrders(allUsers.stream()
                .map(User::getId)
                .collect(Collectors.toList()));
        // 3. 计算用户相似度
        Map<Long, Double> userSimilarities = calculateUserSimilarities(targetUser, allUsers);
        // 4. 获取最相似的N个用户
        List<Long> similarUserIds = userSimilarities.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 5. 获取相似用户订购的服务
        Set<Long> targetUserServices = userOrders.getOrDefault(userId, new ArrayList<>())
                .stream()
                .map(ServiceOrder::getServiceId)
                .collect(Collectors.toSet());
        // 6. 统计服务出现频率并排序
        Map<Long, Integer> serviceFrequency = new HashMap<>();
        for (Long similarUserId : similarUserIds) {
            List<ServiceOrder> orders = userOrders.get(similarUserId);
            if (orders != null) {
                orders.stream()
                        .map(ServiceOrder::getServiceId)
                        .filter(serviceId -> !targetUserServices.contains(serviceId))
                        .forEach(serviceId -> 
                            serviceFrequency.merge(serviceId, 1, Integer::sum));
            }
        }
        // 7. 获取推荐服务详情
        List<ServiceItem> res= serviceFrequency.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> serviceItemMapper.selectById(entry.getKey()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        serviceItemService.fillCategory(res);
        return res;
    }

    /**
     * 获取用户的已完成订单
     * @param userIds 用户ID列表
     * @return 用户ID到订单列表的映射
     */
    private Map<Long, List<ServiceOrder>> getUserOrders(List<Long> userIds) {
        return userIds.stream()
                .collect(Collectors.toMap(
                    userId -> userId,
                    userId -> orderMapper.selectList(new LambdaQueryWrapper<ServiceOrder>()
                            .eq(ServiceOrder::getUserId, userId)
                            .eq(ServiceOrder::getOrderStatus, OrderStatus.COMPLETED.getValue()))
                ));
    }

    /**
     * 计算目标用户与其他用户的相似度
     * @param targetUser 目标用户
     * @param users 其他用户列表
     * @return 用户ID到相似度的映射
     */
    private Map<Long, Double> calculateUserSimilarities(User targetUser, List<User> users) {
        Map<Long, Double> similarities = new HashMap<>();
        
        for (User user : users) {
            if (!user.getId().equals(targetUser.getId())) {
                double similarity = calculateSimilarity(targetUser, user);
                similarities.put(user.getId(), similarity);
            }
        }
        
        return similarities;
    }

    /**
     * 计算两个用户之间的相似度
     * 基于用户的年龄、性别、地址等属性计算相似度
     * @param user1 用户1
     * @param user2 用户2
     * @return 相似度分数(0-1)
     */
    private double calculateSimilarity(User user1, User user2) {
        // 基于用户属性计算相似度
        int matchCount = 0;
        int totalAttributes = 0;

        // 年龄相似度（±5岁）
        if (Math.abs(user1.getAge() - user2.getAge()) <= 5) {
            matchCount++;
        }
        totalAttributes++;

        // 性别匹配
        if (user1.getGender().equals(user2.getGender())) {
            matchCount++;
        }
        totalAttributes++;

        // 地址区域匹配（简单字符串前缀匹配）
        if (user1.getAddress() != null && user2.getAddress() != null &&
            user1.getAddress().substring(0, Math.min(6, user1.getAddress().length()))
                .equals(user2.getAddress().substring(0, Math.min(6, user2.getAddress().length())))) {
            matchCount++;
        }
        totalAttributes++;

        return (double) matchCount / totalAttributes;
    }
}

/*
算法原理：
1. 基于用户的协同过滤（User-Based Collaborative Filtering）是通过发现用户之间的相似性来实现推荐
2. 核心思想是"物以类聚，人以群分"，相似的用户可能有相似的服务需求

算法实现过程：
1. 用户特征提取：
   - 提取用户的基本属性（年龄、性别、地址等）
   - 这些特征用于计算用户间的相似度

2. 相似度计算：
   - 对每个特征进行匹配度计算
   - 年龄差距在5岁内视为相似
   - 性别相同得到匹配分
   - 地址前缀相同表示区域相近

3. 推荐生成：
   - 找出最相似的N个用户（当前设置为5个）
   - 统计这些用户使用过的服务
   - 排除目标用户已使用的服务
   - 按使用频率排序，返回top-K推荐结果

优化方向：
1. 引入时间衰减因子，降低较早行为的权重
2. 考虑用户评价数据来调整相似度
3. 增加更多用户特征维度
4. 使用余弦相似度等更复杂的相似度计算方法
*/ 