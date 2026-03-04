# 系统角色表（sys_role）字段清单

来源：`housekeeping/家政系统/housekeeping_db.sql`

| 编号 | 字段名 | 类型 | 长度 | 是否非空 | 是否主键 | 注释 |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | id | int | — | 是 | 是 | 主键ID |
| 2 | code | varchar | 50 | 是 | 否 | 角色编码 |
| 3 | name | varchar | 50 | 是 | 否 | 角色名称 |
| 4 | description | varchar | 200 | 否 | 否 | 描述 |
| 5 | is_deleted | tinyint | — | 否 | 否 | 是否删除(0:未删除,1:已删除) |
| 6 | created_time | datetime | — | 否 | 否 | 创建时间 |
| 7 | updated_time | datetime | — | 否 | 否 | 更新时间 |
