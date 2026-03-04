# 订单退款表（order_refund）字段清单

来源：`housekeeping/家政系统/housekeeping_db.sql`

| 编号 | 字段名 | 类型 | 长度 | 是否非空 | 是否主键 | 注释 |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | id | bigint | — | 是 | 是 | 退款ID |
| 2 | order_id | varchar | 32 | 是 | 否 | 订单ID |
| 3 | user_id | bigint | — | 是 | 否 | 用户ID |
| 4 | refund_amount | decimal | 10,2 | 是 | 否 | 退款金额 |
| 5 | refund_reason | varchar | 500 | 否 | 否 | 退款原因 |
| 6 | refund_status | tinyint | — | 否 | 否 | 退款状态(1:待审核,2:审核通过,3:审核拒绝,4:退款中,5:已退款,6:退款失败) |
| 7 | refund_type | tinyint | — | 否 | 否 | 退款类型(1:用户取消,2:服务人员取消,3:超时未接单,4:服务纠纷) |
| 8 | audit_user_id | bigint | — | 否 | 否 | 审核人ID |
| 9 | audit_time | datetime | — | 否 | 否 | 审核时间 |
| 10 | audit_remark | varchar | 500 | 否 | 否 | 审核备注 |
| 11 | refund_time | datetime | — | 否 | 否 | 退款完成时间 |
| 12 | create_time | datetime | — | 否 | 否 | 创建时间 |
| 13 | update_time | datetime | — | 否 | 否 | 更新时间 |
| 14 | is_deleted | tinyint | — | 否 | 否 | 是否删除(0:未删除,1:已删除) |
