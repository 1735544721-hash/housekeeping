# 用户收藏表（user_favorite）字段清单

来源：`housekeeping/家政系统/housekeeping_db.sql`

| 编号 | 字段名 | 类型 | 长度 | 是否非空 | 是否主键 | 注释 |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | id | bigint | — | 是 | 是 | 收藏ID |
| 2 | user_id | bigint | — | 是 | 否 | 用户ID |
| 3 | service_id | bigint | — | 是 | 否 | 服务ID |
| 4 | create_time | timestamp | — | 是 | 否 | 创建时间 |
