# 服务评价表（service_review）字段清单

来源：`housekeeping/家政系统/housekeeping_db.sql`

| 编号 | 字段名 | 类型 | 长度 | 是否非空 | 是否主键 | 注释 |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | id | bigint | — | 是 | 是 | 评价ID |
| 2 | order_id | varchar | 32 | 是 | 否 | 订单ID |
| 3 | user_id | bigint | — | 是 | 否 | 用户ID |
| 4 | staff_id | bigint | — | 是 | 否 | 服务人员ID |
| 5 | skill_rating | int | — | 是 | 否 | 技能满意度评分(1-5) |
| 6 | attitude_rating | int | — | 是 | 否 | 服务态度评分(1-5) |
| 7 | experience_rating | int | — | 是 | 否 | 综合体验评分(1-5) |
| 8 | overall_rating | decimal | 2,1 | 是 | 否 | 总体评分 |
| 9 | content | text | — | 否 | 否 | 评价内容 |
| 10 | create_time | datetime | — | 否 | 否 | 创建时间 |
| 11 | update_time | datetime | — | 否 | 否 | 更新时间 |
