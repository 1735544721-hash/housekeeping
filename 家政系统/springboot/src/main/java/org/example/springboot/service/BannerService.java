package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;

import org.example.springboot.entity.Banner;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.BannerMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BannerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BannerService.class);

    @Resource
    private BannerMapper bannerMapper;

    /**
     * 创建轮播图
     */
    @Transactional(rollbackFor = Exception.class)
    public void createBanner(Banner banner) {
        // 设置默认状态
        if (banner.getStatus() == null) {
            banner.setStatus(1);
        }
        
        if (bannerMapper.insert(banner) <= 0) {
            throw new ServiceException("创建轮播图失败");
        }
        
        LOGGER.info("创建轮播图成功: {}", banner.getId());
    }

    /**
     * 更新轮播图
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateBanner(Banner banner) {
        if (bannerMapper.selectById(banner.getId()) == null) {
            throw new ServiceException("轮播图不存在");
        }
        
        if (bannerMapper.updateById(banner) <= 0) {
            throw new ServiceException("更新轮播图失败");
        }
        
        LOGGER.info("更新轮播图成功: {}", banner.getId());
    }

    /**
     * 删除轮播图
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteBanner(Long id) {
        if (bannerMapper.deleteById(id) <= 0) {
            throw new ServiceException("删除轮播图失败");
        }
        
        LOGGER.info("删除轮播图成功: {}", id);
    }

    /**
     * 获取轮播图详情
     */
    public Banner getBannerById(Long id) {
        Banner banner = bannerMapper.selectById(id);
        if (banner == null) {
            throw new ServiceException("轮播图不存在");
        }
        return banner;
    }

    /**
     * 获取启用的轮播图列表
     */
    public List<Banner> getActiveBanners() {
        return bannerMapper.selectList(
            new LambdaQueryWrapper<Banner>()
                .eq(Banner::getStatus, 1)
                .orderByAsc(Banner::getCreateTime)
        );
    }

    /**
     * 分页查询轮播图列表
     */
    public Page<Banner> getBannersByPage(String title, Integer status, 
            Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Banner> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (title != null && !title.trim().isEmpty()) {
            queryWrapper.like(Banner::getTitle, title);
        }
        if (status != null) {
            queryWrapper.eq(Banner::getStatus, status);
        }
        
        // 按创建时间降序排序
        queryWrapper.orderByDesc(Banner::getCreateTime);
        
        return bannerMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);
    }
} 