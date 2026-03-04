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
 * 基于物品的协同过滤推荐服务
 * 通过分析服务之间的相似关系来进行推荐，为用户推荐与其正在查看的服务相似的其他服务
 */
@Service
public class ItemBasedRecommendationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemBasedRecommendationService.class);

    @Resource
    private ServiceItemMapper serviceItemMapper;
    
    @Resource
    private ServiceOrderMapper orderMapper;
    
    @Resource
    private ServiceReviewMapper reviewMapper;

    @Resource
    private ServiceItemService serviceItemService;

    /**
     * 获取推荐服务列表
     * @param serviceId 目标服务ID
     * @param limit 推荐数量限制
     * @return 推荐的服务列表
     */
    public List<ServiceItem> getRecommendations(Long serviceId, Integer limit) {
        // 1. 获取目标服务信息
        ServiceItem targetService = serviceItemMapper.selectById(serviceId);
        if (targetService == null) {
            return new ArrayList<>();
        }

        // 2. 获取所有服务项目
        List<ServiceItem> allServices = serviceItemMapper.selectList(
            new LambdaQueryWrapper<ServiceItem>()
                .eq(ServiceItem::getStatus, 1)  // 只获取上架的服务
                .ne(ServiceItem::getId, serviceId)  // 排除目标服务
        );

        // 3. 计算服务相似度
        Map<Long, Double> serviceSimilarities = calculateServiceSimilarities(targetService, allServices);

        // 4. 排序并返回最相似的服务
        List<ServiceItem> res = serviceSimilarities.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> serviceItemMapper.selectById(entry.getKey()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        serviceItemService.fillCategory(res);
        return res;

    }

    /**
     * 计算服务之间的相似度
     * @param targetService 目标服务
     * @param services 其他服务列表
     * @return 服务ID到相似度的映射
     */
    private Map<Long, Double> calculateServiceSimilarities(ServiceItem targetService, List<ServiceItem> services) {
        Map<Long, Double> similarities = new HashMap<>();
        
        for (ServiceItem service : services) {
            double similarity = calculateSimilarity(targetService, service);
            similarities.put(service.getId(), similarity);
        }
        
        return similarities;
    }

    /**
     * 计算两个服务之间的相似度
     * 基于类别、价格、用户群体重叠度等因素
     * @param service1 服务1
     * @param service2 服务2
     * @return 相似度分数(0-1)
     */
    private double calculateSimilarity(ServiceItem service1, ServiceItem service2) {
        int matchCount = 0;
        int totalFactors = 0;

        // 1. 类别相似度
        if (service1.getCategoryId().equals(service2.getCategoryId())) {
            matchCount += 2;  // 同类别权重加倍
        }
        totalFactors += 2;

        // 2. 价格相似度（价差在20%以内）
        double priceRatio = service1.getPrice().doubleValue() / service2.getPrice().doubleValue();
        if (priceRatio >= 0.8 && priceRatio <= 1.2) {
            matchCount++;
        }
        totalFactors++;

        // 3. 共同订购用户分析
        Set<Long> service1Users = getServiceUsers(service1.getId());
        Set<Long> service2Users = getServiceUsers(service2.getId());
        
        // 计算用户重叠率
        int commonUsers = 0;
        for (Long userId : service1Users) {
            if (service2Users.contains(userId)) {
                commonUsers++;
            }
        }
        
        if (!service1Users.isEmpty() || !service2Users.isEmpty()) {
            double overlapRate = (double) commonUsers / 
                Math.max(service1Users.size(), service2Users.size());
            matchCount += (int) (overlapRate * 2);  // 用户重叠权重加倍
            totalFactors += 2;
        }

        return (double) matchCount / totalFactors;
    }

    /**
     * 获取使用过该服务的用户集合
     * @param serviceId 服务ID
     * @return 用户ID集合
     */
    private Set<Long> getServiceUsers(Long serviceId) {
        return orderMapper.selectList(
            new LambdaQueryWrapper<ServiceOrder>()
                .eq(ServiceOrder::getServiceId, serviceId)
                .eq(ServiceOrder::getOrderStatus, OrderStatus.COMPLETED.getValue())
        ).stream()
        .map(ServiceOrder::getUserId)
        .collect(Collectors.toSet());
    }
}

/*
算法原理：
1. 基于物品的协同过滤（Item-Based Collaborative Filtering）通过分析服务之间的相似关系来实现推荐
2. 核心思想是相似的服务可能会被相同的用户群体使用

算法实现过程：
1. 服务特征提取：
   - 服务类别
   - 价格区间
   - 使用用户群体

2. 相似度计算：
   - 类别相同权重最高(2分)
   - 价格差异在20%以内得分
   - 计算用户群体重叠度
   - 综合多个维度的得分

3. 推荐生成：
   - 获取所有在售服务
   - 计算与目标服务的相似度
   - 按相似度排序，返回top-K推荐结果

优化方向：
1. 引入服务评分数据
2. 考虑服务的时间属性
3. 增加更多服务特征维度
4. 使用缓存优化性能
*/ 