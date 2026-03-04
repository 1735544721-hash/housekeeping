# 服务人员表（service_staff）字段清单

来源：`housekeeping/家政系统/housekeeping_db.sql`

| 编号 | 字段名 | 类型 | 长度 | 是否非空 | 是否主键 | 注释 |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | id | bigint | — | 是 | 是 | 服务人员ID |
| 2 | user_id | bigint | — | 是 | 否 | 关联用户ID |
| 3 | service_type | varchar | 200 | 是 | 否 | 服务类型(JSON格式) |
| 4 | experience | int | — | 否 | 否 | 工作经验年限 |
| 5 | rating | decimal | 2,1 | 否 | 否 | 综合评分 |
| 6 | description | text | — | 否 | 否 | 个人描述 |
| 7 | certificates | json | — | 否 | 否 | 证书信息(JSON格式) |
| 8 | work_area | varchar | 200 | 否 | 否 | 服务区域 |
| 9 | total_orders | int | — | 否 | 否 | 总订单数 |
| 10 | completion_rate | decimal | 5,2 | 否 | 否 | 完成率 |
| 11 | create_time | datetime | — | 否 | 否 | 创建时间 |
| 12 | update_time | datetime | — | 否 | 否 | 更新时间 |
| 13 | is_deleted | tinyint | — | 否 | 否 | 是否删除(0:未删除,1:已删除) |
