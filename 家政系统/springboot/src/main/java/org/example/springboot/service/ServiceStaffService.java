package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.entity.*;
import org.example.springboot.enumClass.OrderStatus;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.ServiceStaffMapper;
import org.example.springboot.mapper.UserMapper;
import org.example.springboot.mapper.ServiceCategoryMapper;
import org.example.springboot.mapper.ServiceOrderMapper;
import org.example.springboot.mapper.ServiceReviewMapper;
import org.example.springboot.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import jakarta.annotation.PostConstruct;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.math.RoundingMode;
import java.util.function.Function;

@Service
public class ServiceStaffService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceStaffService.class);

    @Resource
    private ServiceStaffMapper serviceStaffMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private ServiceCategoryMapper categoryMapper;

    @Resource
    private ServiceOrderMapper orderMapper;

    @Resource
    private ServiceReviewMapper reviewMapper;

    @Resource
    private StaffServiceItemService staffServiceItemService;

    /**
     * 创建服务人员
     */
    @Transactional(rollbackFor = Exception.class)
    public void createServiceStaff(ServiceStaff staff) {
        // 检查用户是否存在
        User user = userMapper.selectById(staff.getUserId());
        if (user == null) {
            throw new ServiceException("关联用户不存在");
        }

        // 检查是否已经是服务人员
        if (serviceStaffMapper.selectOne(new LambdaQueryWrapper<ServiceStaff>()
                .eq(ServiceStaff::getUserId, staff.getUserId())) != null) {
            throw new ServiceException("该用户已经是服务人员");
        }

        // 设置初始值
        staff.setRating(new BigDecimal("5.0"));
        staff.setTotalOrders(0);
        staff.setCompletionRate(new BigDecimal("100.0"));

        // 保存服务人员信息
        if (serviceStaffMapper.insert(staff) <= 0) {
            throw new ServiceException("创建服务人员失败");
        }

        LOGGER.info("创建服务人员成功: {}", staff.getId());
    }

    /**
     * 更新服务人员信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateServiceStaff(ServiceStaff staff) {
        // 检查服务人员是否存在
        if (serviceStaffMapper.selectById(staff.getId()) == null) {
            throw new ServiceException("服务人员不存在");
        }

        // 更新服务人员信息
        if (serviceStaffMapper.updateById(staff) <= 0) {
            throw new ServiceException("更新服务人员信息失败");
        }

        LOGGER.info("更新服务人员信息成功: {}", staff.getId());
    }

    /**
     * 填充用户信息
     */
    public void fillUserInfo(ServiceStaff staff) {
        if (staff != null && staff.getUserId() != null) {
            User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                    .eq(User::getId, staff.getUserId())

            );
            staff.setUser(user);
        }
    }

    /**
     * 批量填充用户信息
     */
    private void fillUserInfo(List<ServiceStaff> staffList) {
        if (staffList != null && !staffList.isEmpty()) {
            // 提取所有用户ID
            List<Long> userIds = staffList.stream()
                    .map(ServiceStaff::getUserId)
                    .collect(Collectors.toList());
            
            // 批量查询未删除的用户信息
            List<User> users = userMapper.selectList(
                new LambdaQueryWrapper<User>()
                    .in(User::getId, userIds)

            );
            
            // 构建用户ID到用户信息的映射
            Map<Long, User> userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, user -> user));
            
            // 填充用户信息
            staffList.forEach(staff -> staff.setUser(userMap.get(staff.getUserId())));
        }
    }

    /**
     * 填充订单信息
     */
    private void fillOrders(ServiceStaff staff) {
        if (staff != null) {
            // 查询该服务人员的所有订单
            List<ServiceOrder> orders = orderMapper.selectList(
                new LambdaQueryWrapper<ServiceOrder>()
                    .eq(ServiceOrder::getStaffId, staff.getId())
                    .orderByDesc(ServiceOrder::getCreateTime)
            );
            staff.setOrders(orders);
        }
    }

    /**
     * 填充评价信息
     */
    private void fillReviews(ServiceStaff staff) {
        if (staff != null) {
            // 查询该服务人员的所有评价
            List<ServiceReview> reviews = reviewMapper.selectList(
                new LambdaQueryWrapper<ServiceReview>()
                    .eq(ServiceReview::getStaffId, staff.getId())
                    .orderByDesc(ServiceReview::getCreateTime)
            );
            staff.setReviews(reviews);
        }
    }

    /**
     * 填充服务类型信息
     */
    private void fillCategories(ServiceStaff staff) {
        if (staff != null && staff.getServiceType() != null) {
            try {
                // 从JSON字符串中解析服务类型ID列表
                List<Long> categoryIds = JsonUtils.parseArray(staff.getServiceType(), Long.class);
                if (!categoryIds.isEmpty()) {
                    // 批量查询服务类型信息
                    List<ServiceCategory> categories = categoryMapper.selectList(
                        new LambdaQueryWrapper<ServiceCategory>()
                            .in(ServiceCategory::getId, categoryIds)
                            .orderByAsc(ServiceCategory::getSortNum)
                    );
                    staff.setCategories(categories);
                }
            } catch (Exception e) {
                LOGGER.error("解析服务类型JSON失败: {}", staff.getServiceType(), e);
            }
        }
    }

    /**
     * 批量填充订单信息
     */
    private void fillOrders(List<ServiceStaff> staffList) {
        if (staffList != null && !staffList.isEmpty()) {
            // 收集所有服务人员ID
            List<Long> staffIds = staffList.stream()
                    .map(ServiceStaff::getId)
                    .collect(Collectors.toList());

            // 批量查询所有订单
            List<ServiceOrder> allOrders = orderMapper.selectList(
                new LambdaQueryWrapper<ServiceOrder>()
                    .in(ServiceOrder::getStaffId, staffIds)
                    .orderByDesc(ServiceOrder::getCreateTime)
            );

            // 按服务人员ID分组
            Map<Long, List<ServiceOrder>> orderMap = allOrders.stream()
                    .collect(Collectors.groupingBy(ServiceOrder::getStaffId));

            // 为每个服务人员设置订单列表
            staffList.forEach(staff -> 
                staff.setOrders(orderMap.getOrDefault(staff.getId(), new ArrayList<>()))
            );
        }
    }

    /**
     * 批量填充评价信息
     */
    private void fillReviews(List<ServiceStaff> staffList) {
        if (staffList != null && !staffList.isEmpty()) {
            // 收集所有服务人员ID
            List<Long> staffIds = staffList.stream()
                    .map(ServiceStaff::getId)
                    .collect(Collectors.toList());

            // 批量查询所有评价
            List<ServiceReview> allReviews = reviewMapper.selectList(
                new LambdaQueryWrapper<ServiceReview>()
                    .in(ServiceReview::getStaffId, staffIds)
                    .orderByDesc(ServiceReview::getCreateTime)
            );

            // 按服务人员ID分组
            Map<Long, List<ServiceReview>> reviewMap = allReviews.stream()
                    .collect(Collectors.groupingBy(ServiceReview::getStaffId));

            // 为每个服务人员设置评价列表
            staffList.forEach(staff -> 
                staff.setReviews(reviewMap.getOrDefault(staff.getId(), new ArrayList<>()))
            );
        }
    }

    /**
     * 批量填充服务类型信息
     */
    private void fillCategories(List<ServiceStaff> staffList) {
//        if (staffList != null && !staffList.isEmpty()) {
//            try {
//                // 收集所有服务类型ID
//                Set<Long> categoryIds = new HashSet<>();
//                for (ServiceStaff staff : staffList) {
//                    if (staff.getServiceType() != null) {
//                        List<Long> ids = JsonUtils.parseArray(staff.getServiceType(), Long.class);
//                        if (ids != null) {
//                            categoryIds.addAll(ids);
//                        }
//                    }
//                }
//
//                if (!categoryIds.isEmpty()) {
//                    // 批量查询服务类型信息
//                    List<ServiceCategory> categories = categoryMapper.selectList(
//                        new LambdaQueryWrapper<ServiceCategory>()
//                            .in(ServiceCategory::getId, categoryIds)
//                            .orderByAsc(ServiceCategory::getSortNum)
//                    );
//                    Map<Long, ServiceCategory> categoryMap = categories.stream()
//                            .collect(Collectors.toMap(ServiceCategory::getId, category -> category));
//
//                    // 为每个服务人员填充服务类型信息
//                    for (ServiceStaff staff : staffList) {
//                        if (staff.getServiceType() != null) {
//                            List<Long> ids = JsonUtils.parseArray(staff.getServiceType(), Long.class);
//                            if (ids != null) {
//                                List<ServiceCategory> staffCategories = ids.stream()
//                                        .map(categoryMap::get)
//                                        .filter(Objects::nonNull)
//                                        .collect(Collectors.toList());
//                                staff.setCategories(staffCategories);
//                            }
//                        }
//                    }
//                }
//            } catch (Exception e) {
//                LOGGER.error("批量填充服务类型信息失败", e);
//            }
//        }
    }

    /**
     * 获取服务人员详情时填充服务项目信息
     */
    private void fillServiceItems(ServiceStaff staff) {
        if (staff != null) {
            List<StaffServiceItem> serviceItems = staffServiceItemService.getServiceItems(staff.getId());
            staff.setServiceItems(serviceItems);
        }
    }

    /**
     * 批量填充服务项目信息
     */
    private void fillServiceItems(List<ServiceStaff> staffList) {
        if (staffList != null && !staffList.isEmpty()) {
            for (ServiceStaff staff : staffList) {
                fillServiceItems(staff);
            }
        }
    }

    /**
     * 获取服务人员详情(包含所有关联信息)
     */
    public ServiceStaff getServiceStaffDetail(Long id) {
        ServiceStaff staff = serviceStaffMapper.selectById(id);
        if (staff == null) {
            throw new ServiceException("服务人员不存在");
        }
        
        // 填充所有关联信息
        fillUserInfo(staff);
        fillOrders(staff);
        fillReviews(staff);
        fillCategories(staff);
        fillServiceItems(staff);
        
        return staff;
    }

    /**
     * 分页查询服务人员列表(可选择填充关联信息)
     */
    public Page<ServiceStaff> getServiceStaffsByPage(String name, String serviceType, 
            Integer pageNum, Integer pageSize, boolean withOrders, boolean withReviews) {
        // 执行基本查询
        Page<ServiceStaff> page = getServiceStaffsByPage(name, serviceType, pageNum, pageSize, null);
        
        // 根据需要填充关联信息
        if (withOrders) {
            fillOrders(page.getRecords());
        }
        if (withReviews) {
            fillReviews(page.getRecords());
        }
        // 始终填充服务类型信息
        fillCategories(page.getRecords());
        
        return page;
    }

    /**
     * 更新服务人员评分
     */
    private void updateStaffRating(Long staffId) {
        // 查询该服务人员的所有评价
        List<ServiceReview> reviews = reviewMapper.selectList(
            new LambdaQueryWrapper<ServiceReview>()
                .eq(ServiceReview::getStaffId, staffId)
        );

        if (!reviews.isEmpty()) {
            // 计算平均评分
            BigDecimal avgRating = reviews.stream()
                    .map(ServiceReview::getOverallRating)  // 使用总体评分
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(new BigDecimal(reviews.size()), 1, RoundingMode.HALF_UP);

            // 更新服务人员评分
            ServiceStaff staff = new ServiceStaff();
            staff.setId(staffId);
            staff.setRating(avgRating);
            serviceStaffMapper.updateById(staff);
        }
    }

    /**
     * 获取服务人员统计信息
     */
    public Map<String, Object> getServiceStaffStatistics(Long staffId) {
        // 检查服务人员是否存在且未删除
        ServiceStaff staff = serviceStaffMapper.selectOne(
            new LambdaQueryWrapper<ServiceStaff>()
                .eq(ServiceStaff::getId, staffId)
                .eq(ServiceStaff::getIsDeleted, 0)
        );
        if (staff == null) {
            throw new ServiceException("服务人员不存在");
        }

        Map<String, Object> statistics = new HashMap<>();

        // 1. 订单统计
        LambdaQueryWrapper<ServiceOrder> orderWrapper = new LambdaQueryWrapper<ServiceOrder>()
            .eq(ServiceOrder::getStaffId, staffId)
            .eq(ServiceOrder::getIsDeleted, 0);  // 只统计未删除的订单

        // 总订单数
        Long totalOrders = orderMapper.selectCount(orderWrapper);
        statistics.put("totalOrders", totalOrders);

        // 各状态订单数量
        Map<String, Long> orderStatusCount = new HashMap<>();
        orderStatusCount.put("waitingPay", getOrderCountByStatus(staffId, OrderStatus.WAITING_PAY.getValue()));
        orderStatusCount.put("waitingAccept", getOrderCountByStatus(staffId, OrderStatus.WAITING_ACCEPT.getValue()));
        orderStatusCount.put("accepted", getOrderCountByStatus(staffId, OrderStatus.ACCEPTED.getValue()));
        orderStatusCount.put("inService", getOrderCountByStatus(staffId, OrderStatus.IN_SERVICE.getValue()));
        orderStatusCount.put("completed", getOrderCountByStatus(staffId, OrderStatus.COMPLETED.getValue()));
        orderStatusCount.put("cancelled", getOrderCountByStatus(staffId, OrderStatus.CANCELLED.getValue()));
        statistics.put("orderStatusCount", orderStatusCount);

        // 2. 评价统计
        LambdaQueryWrapper<ServiceReview> reviewWrapper = new LambdaQueryWrapper<ServiceReview>()
            .eq(ServiceReview::getStaffId, staffId);


        // 总评价数
        Long totalReviews = reviewMapper.selectCount(reviewWrapper);
        statistics.put("totalReviews", totalReviews);

        // 评分统计
        List<ServiceReview> reviews = reviewMapper.selectList(reviewWrapper);
        if (!reviews.isEmpty()) {
            // 计算各维度平均分
            Map<String, BigDecimal> ratingStats = new HashMap<>();
            
            // 技能评分平均分
            BigDecimal avgSkillRating = calculateAverageRating(reviews, ServiceReview::getSkillRating);
            ratingStats.put("avgSkillRating", avgSkillRating);
            
            // 态度评分平均分
            BigDecimal avgAttitudeRating = calculateAverageRating(reviews, ServiceReview::getAttitudeRating);
            ratingStats.put("avgAttitudeRating", avgAttitudeRating);
            
            // 体验评分平均分
            BigDecimal avgExperienceRating = calculateAverageRating(reviews, ServiceReview::getExperienceRating);
            ratingStats.put("avgExperienceRating", avgExperienceRating);
            
            // 总体评分平均分
            BigDecimal avgOverallRating = reviews.stream()
                .map(ServiceReview::getOverallRating)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(reviews.size()), 1, RoundingMode.HALF_UP);
            ratingStats.put("avgOverallRating", avgOverallRating);

            statistics.put("ratingStats", ratingStats);

            // 好评率(总体评分4分以上)
            long goodReviews = reviews.stream()
                .filter(r -> r.getOverallRating().compareTo(new BigDecimal("4.0")) >= 0)
                .count();
            BigDecimal goodRate = BigDecimal.valueOf(goodReviews)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(reviews.size()), 1, RoundingMode.HALF_UP);
            statistics.put("goodRate", goodRate);
        } else {
            Map<String, BigDecimal> ratingStats = new HashMap<>();
            ratingStats.put("avgSkillRating", new BigDecimal("5.0"));
            ratingStats.put("avgAttitudeRating", new BigDecimal("5.0"));
            ratingStats.put("avgExperienceRating", new BigDecimal("5.0"));
            ratingStats.put("avgOverallRating", new BigDecimal("5.0"));
            statistics.put("ratingStats", ratingStats);
            statistics.put("goodRate", new BigDecimal("100.0"));
        }

        // 3. 收入统计
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal monthIncome = BigDecimal.ZERO;

        // 查询已完成的订单
        List<ServiceOrder> completedOrders = orderMapper.selectList(
            new LambdaQueryWrapper<ServiceOrder>()
                .eq(ServiceOrder::getStaffId, staffId)
                .eq(ServiceOrder::getOrderStatus, OrderStatus.COMPLETED.getValue())
                .eq(ServiceOrder::getIsDeleted, 0)
        );

        if (!completedOrders.isEmpty()) {
            // 总收入
            totalIncome = completedOrders.stream()
                .map(ServiceOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 本月收入
            LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            monthIncome = completedOrders.stream()
                .filter(order -> order.getCompleteTime() != null && order.getCompleteTime().isAfter(monthStart))
                .map(ServiceOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        statistics.put("totalIncome", totalIncome);
        statistics.put("monthIncome", monthIncome);

        LOGGER.info("获取服务人员统计信息成功: staffId={}", staffId);
        return statistics;
    }

    /**
     * 计算评分平均值
     */
    private BigDecimal calculateAverageRating(List<ServiceReview> reviews, Function<ServiceReview, Integer> ratingGetter) {
        double avgRating = reviews.stream()
            .mapToInt(ratingGetter::apply)
            .average()
            .orElse(5.0);
        return new BigDecimal(avgRating).setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * 获取指定状态的订单数量
     */
    private Long getOrderCountByStatus(Long staffId, Integer status) {
        return orderMapper.selectCount(
            new LambdaQueryWrapper<ServiceOrder>()
                .eq(ServiceOrder::getStaffId, staffId)
                .eq(ServiceOrder::getOrderStatus, status)
                .eq(ServiceOrder::getIsDeleted, 0)
        );
    }

    /**
     * 获取服务人员详情
     */
    public ServiceStaff getServiceStaffById(Long id) {
        ServiceStaff staff = serviceStaffMapper.selectOne(
            new LambdaQueryWrapper<ServiceStaff>()
                .eq(ServiceStaff::getId, id)
                .eq(ServiceStaff::getIsDeleted, 0)  // 只查询未删除的服务人员
        );
        if (staff == null) {
            throw new ServiceException("服务人员不存在");
        }
        fillUserInfo(staff);
        return staff;
    }
    /**
     * 根据用户ID查询服务人员信息
     */
    public ServiceStaff getServiceStaffByUserId(Long userId) {
        LOGGER.info("userId:{}",userId);
        ServiceStaff staff = serviceStaffMapper.selectOne(
            new LambdaQueryWrapper<ServiceStaff>()
                .eq(ServiceStaff::getUserId, userId)
                // .eq(ServiceStaff::getIsDeleted, 0)
        );
        if (staff == null) {
            throw new ServiceException("服务人员不存在");
        }
        fillUserInfo(staff);
        return staff;
    }

    /**
     * 分页查询服务人员列表
     */
    public Page<ServiceStaff> getServiceStaffsByPage(String name, String serviceType, 
            Integer pageNum, Integer pageSize, BigDecimal minRating) {
        LambdaQueryWrapper<ServiceStaff> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (name != null && !name.trim().isEmpty()) {
            // 查询未删除的用户中匹配名字的用户ID列表
            List<Long> ids = userMapper.selectList(
                new LambdaQueryWrapper<User>()
                    .like(User::getName, name)
            ).stream()
             .map(User::getId)
             .collect(Collectors.toList());
            
            if (!ids.isEmpty()) {
                queryWrapper.in(ServiceStaff::getUserId, ids);
            } else {
                // 如果没有匹配的用户，返回空结果
                return new Page<>(pageNum, pageSize);
            }
        }

        // 添加最低评分过滤条件
        if (minRating != null) {
            queryWrapper.ge(ServiceStaff::getRating, minRating);
        }

        // 只查询未删除的服务人员
        queryWrapper.eq(ServiceStaff::getIsDeleted, 0);
        // 按评分降序排序
        queryWrapper.orderByDesc(ServiceStaff::getRating);
        
        // 执行分页查询
        Page<ServiceStaff> page = serviceStaffMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);
        
        // 如果需要按服务类型筛选
        if (serviceType != null && !serviceType.trim().isEmpty()) {
            String searchType = serviceType.trim();
            // 过滤包含指定服务类型的记录
            List<ServiceStaff> filteredRecords = page.getRecords().stream()
                .filter(staff -> {
                    try {
                        List<String> types = JsonUtils.parseArray(staff.getServiceType(), String.class);
                        return types != null && types.contains(searchType);
                    } catch (Exception e) {
                        LOGGER.error("解析服务类型JSON失败: {}", staff.getServiceType(), e);
                        return false;
                    }
                })
                .collect(Collectors.toList());
            
            // 更新分页结果
            page.setRecords(filteredRecords);
            page.setTotal(filteredRecords.size());
        }
        
        // 填充用户信息
        fillUserInfo(page.getRecords());
        
        return page;
    }

    /**
     * 获取评分前10的服务人员
     */
    public List<ServiceStaff> getTopRatedStaff() {
        // 构建查询条件
        LambdaQueryWrapper<ServiceStaff> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ServiceStaff::getIsDeleted, 0)  // 只查询未删除的服务人员
                   .orderByDesc(ServiceStaff::getRating)  // 按评分降序排序
                   .last("LIMIT 10");  // 限制返回前10条记录
        
        List<ServiceStaff> staffList = serviceStaffMapper.selectList(queryWrapper);
        
        // 填充用户信息和服务类型信息
        fillUserInfo(staffList);
        fillCategories(staffList);
        
        return staffList;
    }

    /**
     * 删除服务人员(软删除)
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteServiceStaff(Long id) {
        // 检查服务人员是否存在且未删除
        ServiceStaff staff = serviceStaffMapper.selectOne(
            new LambdaQueryWrapper<ServiceStaff>()
                .eq(ServiceStaff::getId, id)
                .eq(ServiceStaff::getIsDeleted, 0)
        );
        if (staff == null) {
            throw new ServiceException("服务人员不存在");
        }

        // 检查是否存在未完成的订单
        Long unfinishedCount = orderMapper.selectCount(
            new LambdaQueryWrapper<ServiceOrder>()
                .eq(ServiceOrder::getStaffId, id)
                .eq(ServiceOrder::getIsDeleted, 0)  // 只检查未删除的订单
                .notIn(ServiceOrder::getOrderStatus, 
                    OrderStatus.COMPLETED.getValue(),
                    OrderStatus.CANCELLED.getValue(),
                    OrderStatus.CLOSED.getValue())
        );
        
        if (unfinishedCount > 0) {
            throw new ServiceException("该服务人员存在未完成的订单，不能删除");
        }

        // 执行软删除(手动设置isDeleted为1)
        ServiceStaff updateStaff = new ServiceStaff();
        updateStaff.setId(id);
//        updateStaff.setIsDeleted(1);
        if (serviceStaffMapper.deleteById(updateStaff) <= 0) {
            throw new ServiceException("删除服务人员失败");
        }

        LOGGER.info("服务人员软删除成功: {}", id);
    }

    /**
     * 批量删除服务人员(软删除)
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteServiceStaff(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        // 检查是否存在未完成的订单
        Long unfinishedCount = orderMapper.selectCount(
            new LambdaQueryWrapper<ServiceOrder>()
                .in(ServiceOrder::getStaffId, ids)
                .eq(ServiceOrder::getIsDeleted, 0)  // 只检查未删除的订单
                .notIn(ServiceOrder::getOrderStatus, 
                    OrderStatus.COMPLETED.getValue(),
                    OrderStatus.CANCELLED.getValue(),
                    OrderStatus.CLOSED.getValue())
        );
        
        if (unfinishedCount > 0) {
            throw new ServiceException("选中的服务人员中存在未完成的订单，不能删除");
        }

        // 执行批量软删除(手动设置isDeleted为1)
        for (Long id : ids) {
            ServiceStaff updateStaff = new ServiceStaff();
            updateStaff.setId(id);
//            updateStaff.setIsDeleted(1);
            if (serviceStaffMapper.deleteById(updateStaff) <= 0) {
                throw new ServiceException("批量删除服务人员失败");
            }
        }

        LOGGER.info("批量软删除服务人员成功: count={}", ids.size());
    }

    /**
     * 根据服务项目查找可用的服务人员
     */
    public List<ServiceStaff> getAvailableStaffByServiceItem(Long serviceId) {
        // 获取提供该服务的所有服务人员
        List<ServiceStaff> staffList = serviceStaffMapper.selectAvailableStaffByServiceItem(serviceId);
        
        // 填充关联信息
        fillUserInfo(staffList);
        fillServiceItems(staffList);
        
        return staffList;
    }

    /**
     * 搜索服务人员
     */
    public List<ServiceStaff> searchServiceStaff(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // 使用联合查询直接搜索
        List<ServiceStaff> staffList = serviceStaffMapper.searchStaffByKeyword(keyword);

        // 填充用户信息和服务类型信息
        fillUserInfo(staffList);
        fillCategories(staffList);

        return staffList;
    }

    /**
     * 系统启动时更新所有家政人员的订单数量和完成率
     */
    @PostConstruct
    public void updateAllServiceStaffOrdersOnStartup() {
        LOGGER.info("系统启动，开始更新所有家政人员订单数量...");
        updateAllServiceStaffOrders();
        LOGGER.info("系统启动，更新所有家政人员订单数量完成");
    }

    /**
     * 每天凌晨1点更新所有家政人员的订单数量和完成率
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void updateAllServiceStaffOrdersDaily() {
        LOGGER.info("开始执行每日定时任务：更新所有家政人员订单数量...");
        updateAllServiceStaffOrders();
        LOGGER.info("每日定时任务执行完成：更新所有家政人员订单数量");
    }

    /**
     * 更新所有家政人员的订单数量和完成率
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateAllServiceStaffOrders() {
        try {
            // 查询所有未删除的家政人员
            List<ServiceStaff> staffList = serviceStaffMapper.selectList(
                new LambdaQueryWrapper<ServiceStaff>()
                    .eq(ServiceStaff::getIsDeleted, 0)
            );

            for (ServiceStaff staff : staffList) {
                updateServiceStaffOrders(staff.getId());
            }
        } catch (Exception e) {
            LOGGER.error("更新家政人员订单数量失败", e);
            throw new ServiceException("更新家政人员订单数量失败: " + e.getMessage());
        }
    }

    /**
     * 更新指定家政人员的订单数量和完成率
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateServiceStaffOrders(Long staffId) {
        try {
            // 查询该家政人员是否存在
            ServiceStaff staff = serviceStaffMapper.selectById(staffId);
            if (staff == null) {
                LOGGER.warn("家政人员不存在，ID: {}", staffId);
                return;
            }

            // 查询已完成的订单数量
            Long completedOrders = orderMapper.selectCount(
                new LambdaQueryWrapper<ServiceOrder>()
                    .eq(ServiceOrder::getStaffId, staffId)
                    .eq(ServiceOrder::getOrderStatus, OrderStatus.COMPLETED.getValue())
                    .eq(ServiceOrder::getIsDeleted, 0)
            );

            // 查询总订单数量(不包括取消和关闭的订单)
            Long totalValidOrders = orderMapper.selectCount(
                new LambdaQueryWrapper<ServiceOrder>()
                    .eq(ServiceOrder::getStaffId, staffId)
                    .eq(ServiceOrder::getIsDeleted, 0)
                    .notIn(ServiceOrder::getOrderStatus, 
                        OrderStatus.CANCELLED.getValue(),
                        OrderStatus.CLOSED.getValue())
            );

            // 计算完成率
            BigDecimal completionRate = BigDecimal.ZERO;
            if (totalValidOrders > 0) {
                completionRate = BigDecimal.valueOf(completedOrders)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalValidOrders), 1, RoundingMode.HALF_UP);
            } else {
                completionRate = new BigDecimal("100.0"); // 如果没有有效订单，完成率设为100%
            }

            // 更新家政人员信息
            ServiceStaff updateStaff = new ServiceStaff();
            updateStaff.setId(staffId);
            updateStaff.setTotalOrders(completedOrders.intValue());
            updateStaff.setCompletionRate(completionRate);

            if (serviceStaffMapper.updateById(updateStaff) <= 0) {
                throw new ServiceException("更新家政人员订单数量失败");
            }

            LOGGER.info("更新家政人员订单数量成功: staffId={}, totalOrders={}, completionRate={}%", 
                staffId, completedOrders, completionRate);
        } catch (Exception e) {
            LOGGER.error("更新家政人员订单数量失败: staffId={}", staffId, e);
            throw new ServiceException("更新家政人员订单数量失败: " + e.getMessage());
        }
    }
} 