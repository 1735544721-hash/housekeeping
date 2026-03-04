# 首页轮播图表（banner）字段清单

来源：`housekeeping/家政系统/housekeeping_db.sql`

| 编号 | 字段名 | 类型 | 长度 | 是否非空 | 是否主键 | 注释 |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | id | bigint | — | 是 | 是 | 主键ID |
| 2 | title | varchar | 100 | 是 | 否 | 轮播图标题 |
| 3 | image_url | varchar | 255 | 是 | 否 | 图片URL |
| 4 | description | varchar | 255 | 否 | 否 | 图片描述 |
| 5 | tag | varchar | 50 | 否 | 否 | 标签（如：新人专享、热门活动、限时特惠等） |
| 6 | status | tinyint | — | 否 | 否 | 状态（0-禁用，1-启用） |
| 7 | create_time | datetime | — | 否 | 否 | 创建时间 |
| 8 | update_time | datetime | — | 否 | 否 | 更新时间 |
