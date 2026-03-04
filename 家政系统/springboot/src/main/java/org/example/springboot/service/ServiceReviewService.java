package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.entity.*;
import org.example.springboot.enumClass.OrderStatus;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.regex.Pattern;
import jakarta.annotation.PostConstruct;


@Service
public class ServiceReviewService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceReviewService.class);

    // 敏感词文件路径，默认在 classpath 下
    @Value("${sensitive.words.file:sensitive-words.txt}")
    private String sensitiveWordsFile;
    
    // 敏感词集合
    private Set<String> sensitiveWords = new HashSet<>();

    @Resource
    private ServiceReviewMapper reviewMapper;

    @Resource
    private ServiceOrderMapper orderMapper;

    @Resource
    private ServiceStaffMapper staffMapper;

    @Resource
    private UserMapper userMapper;
    
    /**
     * 初始化敏感词列表
     */
    @PostConstruct
    public void init() {
        try {
            loadSensitiveWords();
            LOGGER.info("敏感词列表加载成功，共 {} 个词", sensitiveWords.size());
        } catch (Exception e) {
            LOGGER.error("加载敏感词列表失败", e);
            // 加载失败时使用默认的敏感词列表
            sensitiveWords.addAll(Arrays.asList(
                    "傻逼", "垃圾", "混蛋", "骗子", "滚蛋", "sb", "fuck", "shit", "傻子",
                    "操你", "滚", "骂人", "脏话", "色情", "赌博", "毒品"
            ));
            LOGGER.info("使用默认敏感词列表，共 {} 个词", sensitiveWords.size());
        }
    }
    
    /**
     * 从文件加载敏感词列表
     */
    private void loadSensitiveWords() throws IOException {
        sensitiveWords.clear();
        try {
            ClassPathResource resource = new ClassPathResource(sensitiveWordsFile);
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        sensitiveWords.add(line);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("读取敏感词文件失败: {}", sensitiveWordsFile, e);
            throw e;
        }
    }

    /**
     * 创建评价
     */
    @Transactional(rollbackFor = Exception.class)
    public void createReview(ServiceReview review) {
        // 检查订单是否存在且已完成
        ServiceOrder order = orderMapper.selectById(review.getOrderId());
        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        if (!OrderStatus.COMPLETED.getValue().equals(order.getOrderStatus())) {
            throw new ServiceException("订单未完成，不能评价");
        }
        // 检查是否已评价
        if (reviewMapper.selectCount(new LambdaQueryWrapper<ServiceReview>()
                .eq(ServiceReview::getOrderId, review.getOrderId())) > 0) {
            throw new ServiceException("订单已评价");
        }
        // 计算总体评分
        BigDecimal overallRating = calculateOverallRating(
            review.getSkillRating(),
            review.getAttitudeRating(),
            review.getExperienceRating()
        );
        review.setOverallRating(overallRating);
                // 过滤评价内容中的敏感词
        if (review.getContent() != null) {
            review.setContent(filterSensitiveWords(review.getContent()));
        }
        // 保存评价信息
        if (reviewMapper.insert(review) <= 0) {
            throw new ServiceException("创建评价失败");
        }
        // 更新服务人员评分
        updateStaffRating(review.getStaffId());
        LOGGER.info("创建评价成功: {}", review.getId());
    }

    /**
     * 删除评价
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteReview(Long id) {
        ServiceReview review = reviewMapper.selectById(id);
        if (review == null) {
            throw new ServiceException("评价不存在");
        }
        // 删除评价
        if (reviewMapper.deleteById(id) <= 0) {
            throw new ServiceException("删除评价失败");
        }

    }
    /**
     * 计算总体评分
     */
    private BigDecimal calculateOverallRating(Integer skillRating, Integer attitudeRating, Integer experienceRating) {
        BigDecimal total = BigDecimal.valueOf(skillRating + attitudeRating + experienceRating);
        return total.divide(BigDecimal.valueOf(3), 1, RoundingMode.HALF_UP);
    }



    /**
     * 敏感词过滤，将敏感词替换为星号
     * @param text 需要过滤的文本
     * @return 过滤后的文本
     */
    private String filterSensitiveWords(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        String result = text;
        for (String word : sensitiveWords) {
            if (result.toLowerCase().contains(word.toLowerCase())) {
                // 创建相同长度的星号替换敏感词
                String stars = "*".repeat(word.length());
                // 使用(?i)使匹配不区分大小写
                result = result.replaceAll("(?i)" + Pattern.quote(word), stars);
            }
        }
        return result;
    }

    /**
     * 手动重新加载敏感词列表
     */
    public void reloadSensitiveWords() {
        try {
            loadSensitiveWords();
            LOGGER.info("敏感词列表重新加载成功，共 {} 个词", sensitiveWords.size());
        } catch (Exception e) {
            LOGGER.error("重新加载敏感词列表失败", e);
            throw new ServiceException("重新加载敏感词列表失败");
        }
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
                    .map(ServiceReview::getOverallRating)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(new BigDecimal(reviews.size()), 1, RoundingMode.HALF_UP);

            // 更新服务人员评分
            ServiceStaff staff = new ServiceStaff();
            staff.setId(staffId);
            staff.setRating(avgRating);
            staffMapper.updateById(staff);
        }
    }

    /**
     * 获取评价详情
     */
    public ServiceReview getReviewById(Long id) {
        ServiceReview review = reviewMapper.selectById(id);
        if (review == null) {
            throw new ServiceException("评价不存在");
        }
        fillReviewInfo(review);
        return review;
    }

    /**
     * 分页查询评价列表
     */
    public Page<ServiceReview> getReviewsByPage(
            Long userId, Long staffId, String orderId,
            Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<ServiceReview> queryWrapper = new LambdaQueryWrapper<>();
        
        if (userId != null) {
            queryWrapper.eq(ServiceReview::getUserId, userId);
        }
        if (staffId != null) {
            queryWrapper.eq(ServiceReview::getStaffId, staffId);
        }
        if (orderId != null) {
            queryWrapper.eq(ServiceReview::getOrderId, orderId);
        }

        queryWrapper.orderByDesc(ServiceReview::getCreateTime);
        
        Page<ServiceReview> page = reviewMapper.selectPage(
            new Page<>(pageNum, pageSize), queryWrapper);
        
        fillReviewInfo(page.getRecords());
        
        return page;
    }

    /**
     * 填充评价关联信息
     */
    private void fillReviewInfo(ServiceReview review) {
        if (review != null) {
            // 填充用户信息
            User user = userMapper.selectById(review.getUserId());
            review.setUser(user);

            // 填充服务人员信息
            ServiceStaff staff = staffMapper.selectById(review.getStaffId());
            if (staff != null) {
                // 填充服务人员关联的用户信息
                User staffUser = userMapper.selectById(staff.getUserId());
                staff.setUser(staffUser);
            }
            review.setStaff(staff);

            // 填充订单信息
            ServiceOrder order = orderMapper.selectById(review.getOrderId());
            review.setOrder(order);
        }
    }

    /**
     * 批量填充评价关联信息
     */
    private void fillReviewInfo(List<ServiceReview> reviews) {
        if (reviews != null && !reviews.isEmpty()) {
            // 收集所有ID
            List<Long> userIds = reviews.stream()
                .map(ServiceReview::getUserId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
            
            List<Long> staffIds = reviews.stream()
                .map(ServiceReview::getStaffId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
            
            List<String> orderIds = reviews.stream()
                .map(ServiceReview::getOrderId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

            // 批量查询关联信息
            Map<Long, User> userMap = !userIds.isEmpty() ? 
                userMapper.selectBatchIds(userIds).stream()
                    .collect(Collectors.toMap(User::getId, user -> user)) : 
                new HashMap<>();
            
            // 查询服务人员信息
            List<ServiceStaff> staffList = !staffIds.isEmpty() ? 
                staffMapper.selectBatchIds(staffIds) : 
                new ArrayList<>();
            
            Map<Long, ServiceStaff> staffMap = staffList.stream()
                .collect(Collectors.toMap(ServiceStaff::getId, staff -> staff));
            
            // 收集服务人员关联的用户ID
            List<Long> staffUserIds = staffList.stream()
                .map(ServiceStaff::getUserId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
            
            // 批量查询服务人员关联的用户信息
            Map<Long, User> staffUserMap = !staffUserIds.isEmpty() ? 
                userMapper.selectBatchIds(staffUserIds).stream()
                    .collect(Collectors.toMap(User::getId, user -> user)) : 
                new HashMap<>();
            
            // 填充服务人员的用户信息
            staffList.forEach(staff -> staff.setUser(staffUserMap.get(staff.getUserId())));

            Map<String, ServiceOrder> orderMap = !orderIds.isEmpty() ?
                orderMapper.selectBatchIds(orderIds).stream()
                    .collect(Collectors.toMap(ServiceOrder::getId, order -> order)) : 
                new HashMap<>();

            // 填充关联信息
            reviews.forEach(review -> {
                review.setUser(userMap.get(review.getUserId()));
                review.setStaff(staffMap.get(review.getStaffId()));
                review.setOrder(orderMap.get(review.getOrderId()));
            });
        }
    }
} 