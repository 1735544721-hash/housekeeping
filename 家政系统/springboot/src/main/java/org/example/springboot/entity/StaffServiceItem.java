package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StaffServiceItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long staffId;
    
    private Long serviceId;
    
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    // 关联的服务项目信息
    @TableField(exist = false)
    private ServiceItem serviceItem;
} 