# 员工服务项目关联表（staff_service_item）字段清单

来源：`housekeeping/家政系统/housekeeping_db.sql`

| 编号 | 字段名 | 类型 | 长度 | 是否非空 | 是否主键 | 注释 |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | id | bigint | — | 是 | 是 | 关联ID |
| 2 | staff_id | bigint | — | 是 | 否 | 服务人员ID |
| 3 | service_id | bigint | — | 是 | 否 | 服务项目ID |
| 4 | status | tinyint | — | 否 | 否 | 状态(0:禁用,1:正常) |
| 5 | create_time | datetime | — | 否 | 否 | 创建时间 |
| 6 | update_time | datetime | — | 否 | 否 | 更新时间 |
