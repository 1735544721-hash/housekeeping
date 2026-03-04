package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.example.springboot.config.JsonArrayToStringDeserializer;
import org.example.springboot.config.StringToJsonArraySerializer;

@Data
@TableName("service_staff")
@Schema(description = "服务人员实体")
public class ServiceStaff {
    @TableId(type = IdType.AUTO)
    @Schema(description = "服务人员ID")
    private Long id;

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "关联用户ID")
    private Long userId;



    @NotBlank(message = "服务类型不能为空")
    @Schema(description = "服务类型(JSON格式)")
    @JsonDeserialize(using = JsonArrayToStringDeserializer.class)
    @JsonSerialize(using = StringToJsonArraySerializer.class)
    private String serviceType;

    @Min(value = 0, message = "工作经验年限不能小于0")
    @Schema(description = "工作经验年限")
    private Integer experience;

    @DecimalMin(value = "0.0", message = "评分不能小于0")
    @DecimalMax(value = "5.0", message = "评分不能大于5")
    @Schema(description = "综合评分")
    private BigDecimal rating;



    @Schema(description = "个人描述")
    private String description;

    @Schema(description = "证书信息(JSON格式)")
    @JsonDeserialize(using = JsonArrayToStringDeserializer.class)
    @JsonSerialize(using = StringToJsonArraySerializer.class)
    private String certificates;

    @Schema(description = "服务区域")
    private String workArea;

    @Schema(description = "总订单数")
    private Integer totalOrders;

    @DecimalMin(value = "0.0", message = "完成率不能小于0")
    @DecimalMax(value = "100.0", message = "完成率不能大于100")
    @Schema(description = "完成率")
    private BigDecimal completionRate;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @TableLogic(value = "0", delval = "1")
    @Schema(description = "是否删除(0:未删除,1:已删除)")
    private Integer isDeleted;

    @TableField(exist = false)
    @Schema(description = "关联的用户信息")
    private User user;

    @TableField(exist = false)
    @Schema(description = "服务订单列表")
    private List<ServiceOrder> orders;

    @TableField(exist = false)
    @Schema(description = "服务评价列表")
    private List<ServiceReview> reviews;

    @TableField(exist = false)
    @Schema(description = "服务类型信息")
    private List<ServiceCategory> categories;

    @TableField(exist = false)
    private List<StaffServiceItem> serviceItems;

} 