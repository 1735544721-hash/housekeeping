package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.springboot.entity.StaffServiceItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StaffServiceItemMapper extends BaseMapper<StaffServiceItem> {
} 