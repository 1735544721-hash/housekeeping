# 服务类别表（service_category）字段清单

来源：`housekeeping/家政系统/housekeeping_db.sql`

| 编号 | 字段名 | 类型 | 长度 | 是否非空 | 是否主键 | 注释 |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | id | bigint | — | 是 | 是 | 类别ID |
| 2 | category_name | varchar | 50 | 是 | 否 | 类别名称 |
| 3 | parent_id | bigint | — | 否 | 否 | 父类别ID |
| 4 | description | varchar | 500 | 否 | 否 | 描述 |
| 5 | icon | varchar | 200 | 否 | 否 | 图标URL |
| 6 | sort_num | int | — | 否 | 否 | 排序号 |
| 7 | status | tinyint | — | 否 | 否 | 状态(0:禁用,1:正常) |
| 8 | create_time | datetime | — | 否 | 否 | 创建时间 |
| 9 | update_time | datetime | — | 否 | 否 | 更新时间 |
| 10 | is_deleted | tinyint | — | 否 | 否 | 是否删除(0:未删除,1:已删除) |
