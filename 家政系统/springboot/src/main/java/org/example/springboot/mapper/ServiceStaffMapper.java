package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.springboot.entity.ServiceStaff;

import java.util.List;

@Mapper
public interface ServiceStaffMapper extends BaseMapper<ServiceStaff> {
    
    @Select("SELECT DISTINCT ss.* FROM service_staff ss " +
            "INNER JOIN staff_service_item ssi ON ss.id = ssi.staff_id " +
            "WHERE ssi.service_id = #{serviceId} " +
            "AND ssi.status = 1 " +  // 服务项目关联状态正常
            "AND ss.is_deleted = 0 " +  // 服务人员未删除
            "ORDER BY ss.rating DESC, ss.completion_rate DESC")
    List<ServiceStaff> selectAvailableStaffByServiceItem(Long serviceId);

    @Select("SELECT DISTINCT ss.* FROM service_staff ss " +
            "INNER JOIN user u ON ss.user_id = u.id " +
            "WHERE (u.name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR u.username LIKE CONCAT('%', #{keyword}, '%') " +
            "OR ss.description LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND ss.is_deleted = 0 " +  // 服务人员未删除
            "ORDER BY ss.rating DESC, ss.completion_rate DESC " +
            "LIMIT 10")
    List<ServiceStaff> searchStaffByKeyword(String keyword);
} 