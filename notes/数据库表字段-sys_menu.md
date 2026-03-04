# 系统菜单表（sys_menu）字段清单

来源：`housekeeping/家政系统/housekeeping_db.sql`

| 编号 | 字段名 | 类型 | 长度 | 是否非空 | 是否主键 | 注释 |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | id | int | — | 是 | 是 | 菜单ID |
| 2 | name | varchar | 50 | 是 | 否 | 菜单名称 |
| 3 | path | varchar | 100 | 否 | 否 | 菜单路径 |
| 4 | component | varchar | 100 | 否 | 否 | 组件路径 |
| 5 | icon | varchar | 50 | 否 | 否 | 菜单图标 |
| 6 | description | varchar | 200 | 否 | 否 | 描述 |
| 7 | pid | int | — | 否 | 否 | 父菜单ID |
| 8 | sort_num | int | — | 否 | 否 | 排序号 |
| 9 | hidden | tinyint | — | 否 | 否 | 是否隐藏 |
| 10 | created_time | datetime | — | 否 | 否 | 创建时间 |
| 11 | updated_time | datetime | — | 否 | 否 | 更新时间 |
