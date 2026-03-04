package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.springboot.entity.OrderRefund;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单退款Mapper
 */
@Mapper
public interface OrderRefundMapper extends BaseMapper<OrderRefund> {
}
