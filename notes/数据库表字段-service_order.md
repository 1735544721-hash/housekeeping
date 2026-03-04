# 服务订单表（service_order）字段清单

来源：`housekeeping/家政系统/housekeeping_db.sql`

| 编号 | 字段名 | 类型 | 长度 | 是否非空 | 是否主键 | 注释 |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | id | varchar | 32 | 是 | 是 | 订单ID |
| 2 | user_id | bigint | — | 是 | 否 | 用户ID |
| 3 | staff_id | bigint | — | 是 | 否 | 服务人员ID |
| 4 | service_id | bigint | — | 是 | 否 | 服务项目ID |
| 5 | order_status | tinyint | — | 是 | 否 | 订单状态(1:待支付,2:待接单,3:已接单,4:服务中,5:已完成,6:已取消,7:已关闭) |
| 6 | total_amount | decimal | 10,2 | 是 | 否 | 订单金额 |
| 7 | payment_method | varchar | 20 | 否 | 否 | 支付方式(WECHAT:微信,ALIPAY:支付宝,BALANCE:余额) |
| 8 | payment_time | datetime | — | 否 | 否 | 支付时间 |
| 9 | paid_amount | decimal | 10,2 | 否 | 否 | 实付金额 |
| 10 | refund_amount | decimal | 10,2 | 否 | 否 | 退款金额 |
| 11 | refund_status | tinyint | — | 否 | 否 | 退款状态(0:无退款,1:退款中,2:已退款,3:退款失败) |
| 12 | cancel_reason | varchar | 500 | 否 | 否 | 取消原因 |
| 13 | cancel_time | datetime | — | 否 | 否 | 取消时间 |
| 14 | complete_time | datetime | — | 否 | 否 | 完成时间 |
| 15 | remark | varchar | 500 | 否 | 否 | 备注 |
| 16 | create_time | timestamp | — | 否 | 否 | 创建时间 |
| 17 | update_time | datetime | — | 否 | 否 | 更新时间 |
| 18 | is_deleted | tinyint | — | 否 | 否 | 是否删除(0:未删除,1:已删除) |
| 19 | service_time | datetime | — | 否 | 否 | 服务开始时间 |
| 20 | duration | float | — | 否 | 否 | 服务时长(小时) |
| 21 | old_id | bigint | — | 否 | 否 | 旧订单ID |
