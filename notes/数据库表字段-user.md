# 用户信息表（user）字段清单

来源：`housekeeping/家政系统/housekeeping_db.sql`

| 编号 | 字段名 | 类型 | 长度 | 是否非空 | 是否主键 | 注释 |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | id | bigint | — | 是 | 是 | 用户ID |
| 2 | username | varchar | 50 | 是 | 否 | 用户名 |
| 3 | password | varchar | 100 | 是 | 否 | 密码(加密存储) |
| 4 | email | varchar | 100 | 是 | 否 | 邮箱 |
| 5 | phone | varchar | 20 | 否 | 否 | 手机号 |
| 6 | role_code | varchar | 50 | 否 | 否 | 角色code(USER/STAFF/ADMIN) |
| 7 | name | varchar | 50 | 否 | 否 | 姓名 |
| 8 | id_card | varchar | 18 | 否 | 否 | 身份证号 |
| 9 | address | varchar | 200 | 否 | 否 | 地址 |
| 10 | avatar | varchar | 200 | 否 | 否 | 头像URL |
| 11 | gender | tinyint | — | 否 | 否 | 性别(0:女,1:男) |
| 12 | age | int | — | 否 | 否 | 年龄 |
| 13 | status | tinyint | — | 否 | 否 | 状态(0:禁用,1:正常) |
| 14 | create_time | timestamp | — | 否 | 否 | 创建时间 |
| 15 | update_time | datetime | — | 否 | 否 | 更新时间 |
