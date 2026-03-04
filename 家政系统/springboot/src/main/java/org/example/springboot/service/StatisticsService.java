package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import jakarta.annotation.Resource;
import org.example.springboot.entity.*;
import org.example.springboot.enumClass.OrderStatus;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsService.class);
    
    @Resource
    private UserMapper userMapper;
    
    @Resource
    private ServiceItemMapper serviceItemMapper;
    
    @Resource
    private ServiceCategoryMapper categoryMapper;
    
    @Resource
    private ServiceStaffMapper staffMapper;
    
    @Resource
    private ServiceOrderMapper orderMapper;

    /**
     * 获取系统概览数据
     */
    @Cacheable(value = "statistics", key = "'overview'", unless = "#result == null")
    public Map<String, Object> getSystemOverview(String staffId) {
        LOGGER.info("获取统计数据，staffId:{}",staffId);
        Map<String, Object> overview = new HashMap<>();
        
        // 统计用户总数
        Long totalUsers = userMapper.selectCount(
            new LambdaQueryWrapper<User>()

        );
        overview.put("totalUsers", totalUsers);
        
        // 统计服务项目总数
        Long totalServices = serviceItemMapper.selectCount(
            new LambdaQueryWrapper<ServiceItem>()
                .eq(ServiceItem::getStatus, 1)
                .eq(ServiceItem::getIsDeleted, 0)
        );
        overview.put("totalServices", totalServices);
        
        // 统计服务类型总数
        Long totalCategories = categoryMapper.selectCount(
            new LambdaQueryWrapper<ServiceCategory>()
                .eq(ServiceCategory::getIsDeleted, 0)
        );
        overview.put("totalCategories", totalCategories);
        
        // 统计服务人员总数
        Long totalStaff = staffMapper.selectCount(
            new LambdaQueryWrapper<ServiceStaff>()
                .eq(ServiceStaff::getIsDeleted, 0)
        );
        overview.put("totalStaff", totalStaff);
        
        // 统计订单相关数据
        LambdaQueryWrapper<ServiceOrder> orderWrapper = new LambdaQueryWrapper<ServiceOrder>()
            .eq(ServiceOrder::getIsDeleted, 0);
        if(StringUtils.isNotBlank(staffId)){
            orderWrapper.eq(ServiceOrder::getStaffId,staffId);
        }
        // 订单总数
        Long totalOrders = orderMapper.selectCount(orderWrapper);
        overview.put("totalOrders", totalOrders);
        
        // 计算订单完成率
        Long completedOrders = orderMapper.selectCount(
            orderWrapper.eq(ServiceOrder::getOrderStatus, OrderStatus.COMPLETED.getValue())
        );
        BigDecimal completionRate = totalOrders > 0 
            ? BigDecimal.valueOf(completedOrders)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        overview.put("completionRate", completionRate);
        
        // 统计总交易金额和平均订单金额
        LambdaQueryWrapper<ServiceOrder> completedOrderWrapper = new LambdaQueryWrapper<ServiceOrder>()
                .eq(ServiceOrder::getOrderStatus, OrderStatus.COMPLETED.getValue())
                .eq(ServiceOrder::getIsDeleted, 0);

        if(StringUtils.isNotBlank(staffId)){
            completedOrderWrapper.eq(ServiceOrder::getStaffId,staffId);
        }

        List<ServiceOrder> completedOrderList = orderMapper.selectList(
           completedOrderWrapper
        );
        
        BigDecimal totalAmount = completedOrderList.stream()
            .map(ServiceOrder::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        overview.put("totalAmount", totalAmount);
        
        BigDecimal avgOrderAmount = completedOrders > 0
            ? totalAmount.divide(BigDecimal.valueOf(completedOrders), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        overview.put("avgOrderAmount", avgOrderAmount);
        
        return overview;
    }

    /**
     * 获取订单金额趋势
     */
    @Cacheable(value = "statistics", key = "'orderTrend:' + #timeRange + ':' + #groupBy", unless = "#result == null")
    public Map<String, Object> getOrderTrend(String timeRange, String groupBy,String staffId) {
        // 验证并设置时间范围
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime;
        
        switch (timeRange.toLowerCase()) {
            case "month":
                startTime = endTime.minusMonths(1);
                groupBy = groupBy == null ? "day" : groupBy;
                break;
            case "year":
                startTime = endTime.minusYears(1);
                groupBy = groupBy == null ? "month" : groupBy;
                break;
            case "three_years":
                startTime = endTime.minusYears(3);
                groupBy = groupBy == null ? "month" : groupBy;
                break;
            default:
                throw new ServiceException("无效的时间范围");
        }
        
        // 查询指定时间范围内的已完成订单
        LambdaQueryWrapper<ServiceOrder> serviceOrderLambdaQueryWrapper = new LambdaQueryWrapper<ServiceOrder>()
                .eq(ServiceOrder::getOrderStatus, OrderStatus.COMPLETED.getValue())
                .eq(ServiceOrder::getIsDeleted, 0)
                .ge(ServiceOrder::getCompleteTime, startTime)
                .le(ServiceOrder::getCompleteTime, endTime)
                .orderByAsc(ServiceOrder::getCompleteTime);
        if(StringUtils.isNotBlank(staffId)){
            serviceOrderLambdaQueryWrapper.eq(ServiceOrder::getStaffId,staffId);
        }
        List<ServiceOrder> orders = orderMapper.selectList(
                serviceOrderLambdaQueryWrapper
        );

        
        // 根据分组方式处理数据
        DateTimeFormatter formatter;
        Map<String, List<ServiceOrder>> groupedOrders;
        
        switch (groupBy.toLowerCase()) {
            case "day":
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                groupedOrders = orders.stream()
                    .collect(Collectors.groupingBy(
                        order -> order.getCompleteTime().format(formatter)
                    ));
                break;
            case "week":
                groupedOrders = orders.stream()
                    .collect(Collectors.groupingBy(order -> {
                        LocalDateTime orderTime = order.getCompleteTime();
                        LocalDateTime weekStart = orderTime.minusDays(orderTime.getDayOfWeek().getValue() - 1);
                        return weekStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "周";
                    }));
                break;
            case "month":
                // 修改月份显示格式
                groupedOrders = orders.stream()
                    .collect(Collectors.groupingBy(order -> {
                        LocalDateTime orderTime = order.getCompleteTime();
                        return orderTime.format(DateTimeFormatter.ofPattern("yyyy年MM月"));
                    }));
                break;
            default:
                throw new ServiceException("无效的分组方式");
        }
        
        // 准备返回数据
        List<String> xAxis = new ArrayList<>(groupedOrders.keySet());
        // 根据日期字符串排序
        Collections.sort(xAxis, (a, b) -> {
            // 移除"年"、"月"和"周"后缀进行比较
            String dateA = a.replaceAll("[年月周]", "");
            String dateB = b.replaceAll("[年月周]", "");
            return dateA.compareTo(dateB);
        });
        
        List<BigDecimal> series = xAxis.stream()
            .map(date -> groupedOrders.get(date).stream()
                .map(ServiceOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add))
            .collect(Collectors.toList());
            
        // 计算总计数据
        BigDecimal totalAmount = orders.stream()
            .map(ServiceOrder::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        BigDecimal avgAmount = orders.isEmpty() ? BigDecimal.ZERO :
            totalAmount.divide(BigDecimal.valueOf(orders.size()), 2, RoundingMode.HALF_UP);
            
        Map<String, Object> result = new HashMap<>();
        result.put("xAxis", xAxis);
        result.put("series", Collections.singletonList(
            new HashMap<String, Object>() {{
                put("name", "订单金额");
                put("data", series);
            }}
        ));
        result.put("total", new HashMap<String, Object>() {{
            put("amount", totalAmount);
            put("count", orders.size());
            put("avgAmount", avgAmount);
        }});
        
        return result;
    }
} 