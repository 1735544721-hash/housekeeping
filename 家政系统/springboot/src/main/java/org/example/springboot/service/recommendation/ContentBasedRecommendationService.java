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
 * 基于内容的推荐服务
 * 通过分析用户的历史行为和服务的内容特征来进行个性化推荐
 */
@Service
public class ContentBasedRecommendationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContentBasedRecommendationService.class);

    @Resource
    private UserMapper userMapper;
    
    @Resource
    private ServiceItemMapper serviceItemMapper;
    
    @Resource
    private ServiceOrderMapper orderMapper;
    
    @Resource
    private ServiceCategoryMapper categoryMapper;
    @Resource
    private ServiceItemService serviceItemService;
    /**
     * 获取推荐服务列表
     * @param userId 目标用户ID
     * @param limit 推荐数量限制
     * @return 推荐的服务列表
     */
    public List<ServiceItem> getRecommendations(Long userId, Integer limit) {
        // 1. 获取用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            return new ArrayList<>();
        }

        // 2. 获取用户的历史订单和浏览记录
        List<ServiceOrder> userOrders = orderMapper.selectList(
            new LambdaQueryWrapper<ServiceOrder>()
                .eq(ServiceOrder::getUserId, userId)
                .eq(ServiceOrder::getOrderStatus, OrderStatus.COMPLETED.getValue())
        );

        // 3. 分析用户偏好
        Map<Long, Integer> categoryPreferences = analyzeCategoryPreferences(userOrders);
        
        // 4. 获取所有可用服务
        List<ServiceItem> availableServices = serviceItemMapper.selectList(
            new LambdaQueryWrapper<ServiceItem>()
                .eq(ServiceItem::getStatus, 1)  // 只获取上架的服务
        );

        // 5. 计算服务匹配度
        Map<Long, Double> serviceScores = calculateServiceScores(
            availableServices, 
            categoryPreferences,
            user
        );

        // 6. 过滤掉用户已使用的服务
        Set<Long> usedServices = userOrders.stream()
                .map(ServiceOrder::getServiceId)
                .collect(Collectors.toSet());

        // 7. 返回推荐结果
        List<ServiceItem> res= serviceScores.entrySet().stream()
                .filter(entry -> !usedServices.contains(entry.getKey()))
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> serviceItemMapper.selectById(entry.getKey()))
                .filter(Objects::nonNull)
                .toList();
        serviceItemService.fillCategory(res);
        return res;
    }

    /**
     * 分析用户对不同类别的偏好
     * @param orders 用户的历史订单
     * @return 类别ID到使用次数的映射
     */
    private Map<Long, Integer> analyzeCategoryPreferences(List<ServiceOrder> orders) {
        Map<Long, Integer> preferences = new HashMap<>();
        
        for (ServiceOrder order : orders) {
            ServiceItem service = serviceItemMapper.selectById(order.getServiceId());
            if (service != null) {
                preferences.merge(service.getCategoryId(), 1, Integer::sum);
            }
        }
        
        return preferences;
    }

    /**
     * 计算服务的匹配得分
     * @param services 可用服务列表
     * @param categoryPreferences 用户类别偏好
     * @param user 用户信息
     * @return 服务ID到匹配得分的映射
     */
    private Map<Long, Double> calculateServiceScores(
            List<ServiceItem> services,
            Map<Long, Integer> categoryPreferences,
            User user) {
        Map<Long, Double> scores = new HashMap<>();
        
        for (ServiceItem service : services) {
            double score = calculateServiceScore(service, categoryPreferences, user);
            scores.put(service.getId(), score);
        }
        
        return scores;
    }

    /**
     * 计算单个服务的匹配得分
     * 基于类别偏好、价格匹配度等因素
     * @param service 服务信息
     * @param categoryPreferences 用户类别偏好
     * @param user 用户信息
     * @return 匹配得分(0-1)
     */
    private double calculateServiceScore(
            ServiceItem service,
            Map<Long, Integer> categoryPreferences,
            User user) {
        double score = 0.0;
        
        // 1. 类别偏好匹配 (权重: 0.4)
        int categoryFrequency = categoryPreferences.getOrDefault(service.getCategoryId(), 0);
        score += 0.4 * (categoryFrequency > 0 ? 1.0 : 0.0);
        
        // 2. 价格匹配 (权重: 0.3)
        // 假设根据用户历史订单的平均价格来判断价格匹配度
        double avgOrderAmount = getAverageOrderAmount(user.getId());
        double priceRatio = service.getPrice().doubleValue() / avgOrderAmount;
        if (priceRatio >= 0.8 && priceRatio <= 1.2) {
            score += 0.3;
        }
        
        // 3. 服务时间匹配 (权重: 0.3)
        // 这里可以根据实际需求添加更多匹配逻辑
        score += 0.3;
        
        return score;
    }

    /**
     * 获取用户历史订单的平均金额
     * @param userId 用户ID
     * @return 平均订单金额
     */
    private double getAverageOrderAmount(Long userId) {
        List<ServiceOrder> orders = orderMapper.selectList(
            new LambdaQueryWrapper<ServiceOrder>()
                .eq(ServiceOrder::getUserId, userId)
                .eq(ServiceOrder::getOrderStatus, OrderStatus.COMPLETED.getValue())
        );
        
        if (orders.isEmpty()) {
            return 100.0; // 默认值
        }
        
        return orders.stream()
                .mapToDouble(order -> order.getTotalAmount().doubleValue())
                .average()
                .orElse(100.0);
    }
}

/*
算法原理：
1. 基于内容的推荐（Content-Based Recommendation）通过分析用户的历史行为和服务的内容特征来实现个性化推荐
2. 核心思想是用户过去喜欢什么，未来可能也会喜欢类似的内容

算法实现过程：
1. 用户画像构建：
   - 分析历史订单
   - 统计类别偏好
   - 计算平均消费水平

2. 服务特征提取：
   - 服务类别
   - 价格区间
   - 其他属性特征

3. 匹配度计算：
   - 类别偏好匹配(权重0.4)
   - 价格水平匹配(权重0.3)
   - 其他特征匹配(权重0.3)

4. 推荐生成：
   - 过滤已使用服务
   - 按匹配得分排序
   - 返回top-K推荐结果

优化方向：
1. 引入更多用户行为数据
2. 考虑时间衰减因素
3. 动态调整特征权重
4. 增加多样性控制
*/ 