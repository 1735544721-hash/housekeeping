package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.example.springboot.entity.ServiceCategory;
import org.example.springboot.entity.ServiceItem;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.ServiceCategoryMapper;
import org.example.springboot.mapper.ServiceItemMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ServiceCategoryService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCategoryService.class);

    @Resource
    private ServiceCategoryMapper categoryMapper;

    @Resource
    private ServiceItemMapper serviceItemMapper;

    /**
     * 创建服务类别
     */
    @Transactional(rollbackFor = Exception.class)
    public void createCategory(ServiceCategory category) {
        // 设置默认值
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        if (category.getSortNum() == null) {
            category.setSortNum(1);
        }

        // 检查父类别是否存在
        if (category.getParentId() != null) {
            ServiceCategory parent = categoryMapper.selectById(category.getParentId());
            if (parent == null) {
                throw new ServiceException("父类别不存在");
            }
        }

        // 保存类别信息
        if (categoryMapper.insert(category) <= 0) {
            throw new ServiceException("创建服务类别失败");
        }

        LOGGER.info("创建服务类别成功: {}", category.getId());
    }

    /**
     * 更新服务类别
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(ServiceCategory category) {
        // 检查类别是否存在
        ServiceCategory existCategory = categoryMapper.selectById(category.getId());
        if (existCategory == null) {
            throw new ServiceException("服务类别不存在");
        }

        // 检查父类别是否存在且不能设置自己为父类别
        if (category.getParentId() != null) {
            if (category.getParentId().equals(category.getId())) {
                throw new ServiceException("不能设置自己为父类别");
            }
            ServiceCategory parent = categoryMapper.selectById(category.getParentId());
            if (parent == null) {
                throw new ServiceException("父类别不存在");
            }
        }

        // 更新类别信息
        if (categoryMapper.updateById(category) <= 0) {
            throw new ServiceException("更新服务类别失败");
        }

        LOGGER.info("更新服务类别成功: {}", category.getId());
    }

    /**
     * 获取服务类别详情
     */
    public ServiceCategory getCategoryById(Long id) {
        ServiceCategory category = categoryMapper.selectOne(
            new LambdaQueryWrapper<ServiceCategory>()
                .eq(ServiceCategory::getId, id)
                .eq(ServiceCategory::getIsDeleted, 0)  // 只查询未删除的类别
        );
        if (category == null) {
            throw new ServiceException("服务类别不存在");
        }
        return category;
    }

    /**
     * 获取服务类别树
     */
    public List<ServiceCategory> getCategoryTree() {
        // 查询所有未删除的类别
        List<ServiceCategory> allCategories = categoryMapper.selectList(
            new LambdaQueryWrapper<ServiceCategory>()
                .eq(ServiceCategory::getIsDeleted, 0)  // 只查询未删除的类别
                .orderByAsc(ServiceCategory::getSortNum)
        );

        // 构建树形结构
        return buildCategoryTree(allCategories);
    }

    /**
     * 删除服务类别(软删除)
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        // 检查类别是否存在
        ServiceCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new ServiceException("服务类别不存在");
        }

        // 检查是否存在子类别
        Long childCount = categoryMapper.selectCount(
            new LambdaQueryWrapper<ServiceCategory>()
                .eq(ServiceCategory::getParentId, id)
                .eq(ServiceCategory::getIsDeleted, 0)  // 只检查未删除的子类别
        );
        if (childCount > 0) {
            throw new ServiceException("存在子类别,不能删除");
        }

        // 检查是否有关联的服务项目
        Long itemCount = serviceItemMapper.selectCount(
            new LambdaQueryWrapper<ServiceItem>()
                .eq(ServiceItem::getCategoryId, id)
                .eq(ServiceItem::getIsDeleted, 0)  // 只检查未删除的服务项目
        );
        if (itemCount > 0) {
            throw new ServiceException("该类别下存在服务项目,不能删除");
        }

        // 执行软删除
        if (categoryMapper.deleteById(id) <= 0) {
            throw new ServiceException("删除服务类别失败");
        }

        LOGGER.info("服务类别软删除成功: {}", id);
    }

    /**
     * 获取子类别列表
     */
    public List<ServiceCategory> getChildCategories(Long parentId) {
        return categoryMapper.selectList(
            new LambdaQueryWrapper<ServiceCategory>()
                .eq(ServiceCategory::getParentId, parentId)
                .eq(ServiceCategory::getIsDeleted, 0)  // 只查询未删除的类别
                .orderByAsc(ServiceCategory::getSortNum)
        );
    }

    /**
     * 构建类别树
     */
    private List<ServiceCategory> buildCategoryTree(List<ServiceCategory> categories) {
        // 按父ID分组所有类别
        Map<Long, List<ServiceCategory>> childrenMap = categories.stream()
                .filter(cat -> cat.getParentId() != null)
                .collect(Collectors.groupingBy(ServiceCategory::getParentId));
        
        // 递归设置子类别
        return categories.stream()
                .filter(cat -> cat.getParentId() == null)  // 获取顶级类别
                .map(cat -> fillChildren(cat, childrenMap))
                .collect(Collectors.toList());
    }

    /**
     * 递归填充子类别
     */
    private ServiceCategory fillChildren(ServiceCategory category, Map<Long, List<ServiceCategory>> childrenMap) {
        List<ServiceCategory> children = childrenMap.get(category.getId());
        
        if (children != null) {
            // 递归处理每个子类别
            List<ServiceCategory> processedChildren = children.stream()
                    .map(child -> fillChildren(child, childrenMap))
                    .collect(Collectors.toList());
            
            category.setChildren(processedChildren);
            category.setHasChildren(true);
        } else {
            category.setChildren(null);
            category.setHasChildren(false);
        }
        
        return category;
    }

    /**
     * 分页查询服务类别
     */
    public Page<ServiceCategory> getCategoriesByPage(String categoryName, Integer status, 
            Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<ServiceCategory> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.isNotBlank(categoryName)) {
            queryWrapper.like(ServiceCategory::getCategoryName, categoryName);
        }
        if (status != null) {
            queryWrapper.eq(ServiceCategory::getStatus, status);
        }
        
        // 按排序号升序、创建时间降序排序
        queryWrapper.orderByAsc(ServiceCategory::getSortNum)
                   .orderByDesc(ServiceCategory::getCreateTime);
        
        return categoryMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);
    }

    /**
     * 批量删除服务类别
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteCategories(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        // 检查是否包含有子类别的分类
        for (Long id : ids) {
            List<ServiceCategory> children = getChildCategories(id);
            if (!children.isEmpty()) {
                throw new ServiceException("选中的类别中包含有子类别，不能直接删除");
            }
        }

        // 执行批量删除
        if (categoryMapper.deleteBatchIds(ids) <= 0) {
            throw new ServiceException("批量删除服务类别失败");
        }

        LOGGER.info("批量删除服务类别成功: count={}", ids.size());
    }

    /**
     * 搜索服务类别
     */
    public List<ServiceCategory> searchCategories(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // 构建查询条件
        LambdaQueryWrapper<ServiceCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(ServiceCategory::getCategoryName, keyword)
                   .or()
                   .like(ServiceCategory::getDescription, keyword)
                   .eq(ServiceCategory::getStatus, 1)  // 只查询正常状态的类别
                   .eq(ServiceCategory::getIsDeleted, 0)  // 只查询未删除的类别
                   .orderByAsc(ServiceCategory::getSortNum)  // 按排序号升序
                   .last("LIMIT 10");  // 限制返回10条记录

        // 执行查询
        return categoryMapper.selectList(queryWrapper);
    }
} 