# 服务项目表（service_item）字段清单

来源：`housekeeping/家政系统/housekeeping_db.sql`

| 编号 | 字段名 | 类型 | 长度 | 是否非空 | 是否主键 | 注释 |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | id | bigint | — | 是 | 是 | 服务ID |
| 2 | category_id | bigint | — | 是 | 否 | 类别ID |
| 3 | title | varchar | 100 | 是 | 否 | 服务标题 |
| 4 | description | text | — | 否 | 否 | 服务描述 |
| 5 | price | decimal | 10,2 | 是 | 否 | 服务价格 |
| 6 | status | tinyint | — | 否 | 否 | 状态(0:下架,1:上架) |
| 7 | create_time | datetime | — | 否 | 否 | 创建时间 |
| 8 | update_time | datetime | — | 否 | 否 | 更新时间 |
| 9 | is_deleted | tinyint | — | 否 | 否 | 是否删除(0:未删除,1:已删除) |
