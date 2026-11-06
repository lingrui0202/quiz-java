# quiz-java

《Web 开发技术》第六次课作业：**Spring Boot + MyBatis + MySQL**。

课程要求（`第六次课.pptx` 2026-10-23）：

1. 安装 MySQL 数据库、安装 IDEA 64；
2. 跑通 Java 通过 MyBatis 访问 MySQL 数据库；
3. 跑通 controller 访问。

这个仓库把三条都落地了：MySQL 8.4 跑在 E 盘，MyBatis 用「XML + 注解」两套写法访问库，Controller 暴露 REST 接口并能用浏览器直接验证。题库沿用第二次课 `quiz-game` 的那 8 道前端基础题，前后端数据能对上。

---

## 本机环境（全部在 E 盘，C 盘零占用）

| 组件 | 版本 | 位置 |
|---|---|---|
| JDK | Temurin 17.0.20.1 | `E:\Dev\tools\jdk17` |
| Maven | 3.9.9 | `E:\Dev\tools\maven`（本地仓库 `E:\Dev\m2`） |
| MySQL | 8.4.6 便携版 | `E:\Dev\tools\mysql-8.4.6-winx64` |
| MySQL 数据 | — | `E:\Dev\mysql-data` |
| MySQL 日志/临时 | — | `E:\Dev\mysql-log`、`E:\Dev\mysql-tmp` |

MySQL 用的是官方 **winx64 免安装 zip**，不是 MSI 安装包——MSI 会把服务和数据硬塞进 `C:\ProgramData\MySQL`，重装 C 盘又要丢一次。便携版所有东西都在 E 盘，删掉一个目录就干净了。

---

## 一、初始化数据库（只需做一次）

```bash
# 1) 初始化数据目录（--initialize-insecure：root 初始密码为空，本机开发够用）
E:\Dev\tools\mysql-8.4.6-winx64\bin\mysqld --defaults-file=E:\Dev\tools\mysql-my.ini --initialize-insecure

# 2) 启动（前台跑，关掉窗口就停；这是便携版的正常用法）
E:\Dev\tools\mysql-8.4.6-winx64\bin\mysqld --defaults-file=E:\Dev\tools\mysql-my.ini --console

# 3) 另开一个终端，建库建表、灌初始数据
E:\Dev\tools\mysql-8.4.6-winx64\bin\mysql -u root --skip-password < db/schema.sql
E:\Dev\tools\mysql-8.4.6-winx64\bin\mysql -u root --skip-password < db/seed.sql

# 4) 建应用账号（application.yml 里用的是 quiz / quiz123456）
E:\Dev\tools\mysql-8.4.6-winx64\bin\mysql -u root --skip-password -e "
  CREATE USER IF NOT EXISTS 'quiz'@'localhost' IDENTIFIED BY 'quiz123456';
  CREATE USER IF NOT EXISTS 'quiz'@'127.0.0.1' IDENTIFIED BY 'quiz123456';
  GRANT ALL PRIVILEGES ON quiz_db.* TO 'quiz'@'localhost';
  GRANT ALL PRIVILEGES ON quiz_db.* TO 'quiz'@'127.0.0.1';
  FLUSH PRIVILEGES;"
```

> 便携版没注册成 Windows 服务，**每次开机要手动启一次**。要省事可以以后加个计划任务或 `sc create`，但那就又回到"装进系统"的老路，当前刻意不这么做。

## 二、启动后端

```bash
cd E:\Dev\GitHub\lingrui0202\quiz-java
E:\Dev\tools\maven\bin\mvn spring-boot:run
# 或先打包再跑
E:\Dev\tools\maven\bin\mvn -DskipTests package
java -jar target/quiz-java-1.0.0.jar
```

端口 **8081**（8080 留给 `web-quiz-admin` 的 Vue dev server）。

## 三、接口

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/db/ping` | 自检：MySQL 版本、当前库、库时间、题目数、用户数 |
| GET | `/api/questions?keyword=&category=` | 题目列表，支持关键词/分类过滤（动态 SQL） |
| GET | `/api/questions/{id}` | 题目详情 |
| GET | `/api/questions/stats` | 每个分类各几道题（GROUP BY） |
| POST | `/api/questions` | 新增题目 |
| PUT | `/api/questions/{id}` | 修改题目 |
| DELETE | `/api/questions/{id}` | 删除题目 |
| GET | `/api/users` | 用户列表（不含密码） |
| GET | `/api/users/{id}` | 用户详情 |
| POST | `/api/users` | 注册（明文密码传 `password` 或 `passwordHash`，入库前哈希） |
| PUT | `/api/users/{id}` | 修改用户（密码留空 = 不改） |
| DELETE | `/api/users/{id}` | 逻辑删除（置 `is_delete=1`，行还在） |
| GET | `/api/users/page?page=1&pageSize=5&keyword=` | 分页查询，`total` + 当前页；keyword 可选（7 周新增） |
| GET | `/api/users/search?keyword=` | 用户名模糊查询，不分页（7 周新增） |
| GET | `/api/users/exists?username=` | 用户名是否已存在（7 周新增） |
| GET | `/api/users/delete?id=` 或 `?username=` | 逻辑删除的 GET 写法，与课堂一致（7 周新增） |
| DELETE | `/api/users/{id}/hard` | 硬删除，真的从表里删（9-6 的练习项） |

统一返回 `{"code":0,"message":"ok","data":...}`；参数错误返回 400 + `code:1`，路径不存在返回 404。

`CorsConfig` 对 `/api/**` 放开了 `http://localhost:*` / `http://127.0.0.1:*`，方便前端 dev server 直接调；生产环境应当收窄到真实域名。

---

## 四、实现要点

**MyBatis 用了两种写法，是为了对照课堂讲的两种方式：**

- `QuestionMapper` + `resources/mapper/QuestionMapper.xml`：SQL 写在 XML 里，含 `<where>` / `<if>` 动态 SQL、`<resultMap>` 下划线转驼峰、`<include>` 复用列清单；
- `UserMapper` / `SystemMapper`：用 `@Select` `@Insert` 注解写 SQL，适合短句。

**分层：** Controller（收参、返回统一结构）→ Service（校验、业务）→ Mapper（SQL）→ MySQL。Service 里没有一行 JDBC 代码，这就是 MyBatis 存在的意义。

**几个有意为之的点：**

- `Question.getOptions()` 是派生字段，标了 `access = READ_ONLY`，只在响应里出现，请求体不用传 `options`；
- `User.passwordHash` 标了 `WRITE_ONLY`，永远不会被序列化返回；
- 密码摘要用 **PBKDF2-HMAC-SHA256**（随机 16 字节盐、12 万轮、JDK 自带 `SecretKeyFactory`，不引第三方依赖），存储格式 `pbkdf2$120000$salt$hash`；
- 请求体里 `password` 和 `passwordHash` 两个键都收（后者是历史命名，前端目前用的它），传进来的都是明文，入库前统一哈希；
- 全局异常处理器让错误也走 `Result` 结构，不返回 Spring 默认错误页。

---

## 五、和前后端联调的关系

第五次课的 `web-quiz-admin` 原来用的是本地数组假数据，README 里写着「等以后做前后端联调再换成接口」。**2026-10-23 已经接上了**：

- 前端新增 `src/api/`（axios 实例 + 题目/用户接口 + 字段转换），`QuestionView.vue` / `UserView.vue` 改成 `created()` 时拉数据，增删改全部走接口；
- 跨域两条路都通了：`vue.config.js` 的 `devServer.proxy` 把 `/api` 转发到 8081；本仓库的 `CorsConfig` 也开了 CORS；
- 为了配合前端已有的"编辑/删除用户"按钮，这次补了 `PUT /api/users/{id}` 和 `DELETE /api/users/{id}`——改用户时密码留空表示不改，填了才重新算摘要。

**链路验收**（实测）：`8080 → /api 代理 → 8081 → MySQL`，新增一道题后前端分页总数从 8 变 9，删掉又回到 8。

线上部署的那份静态站仍然是"本地数组"版：Vercel 上没有后端，直接换成接口版会 404。等后端上了公网再切。

---

## 六、验证记录（2026-10-23，本机实测）

环境：Windows，MySQL 8.4.6 便携版跑在 3306，应用跑在 8081，JDK 17。

**连通性自检** `GET /api/db/ping`：

```json
{"code":0,"message":"ok","data":{"mysqlVersion":"8.4.6","database":"quiz_db",
 "dbTime":"2026-10-23 11:27:13","questionCount":8,"userCount":2}}
```

**逐项结果：**

| 验证项 | 结果 |
|---|---|
| MyBatis 读取 MySQL（题库 8 条，中文正常） | ✅ |
| 分类统计 `GROUP BY`：CSS 3 / JS 3 / HTML 2 | ✅ |
| 关键词过滤（含中文「标题」「绑定」） | ✅ |
| 分类过滤 `category=JAVASCRIPT` → id 6/7/8 | ✅ |
| 新增 `POST` → 返回自增 id 与 `created_at` | ✅ |
| 详情 `GET /{id}`、修改 `PUT`、删除 `DELETE` | ✅ |
| 参数校验 `answer=9` → 400 `answer 必须是 0-3` | ✅ |
| 查不存在的 id → 400「题目不存在」 | ✅ |
| 路径写错 → 404「接口不存在」（不再是 500） | ✅ |
| 用户注册 → 密码存摘要，响应不含 `passwordHash` | ✅ |
| 列表接口响应不含密码字段 | ✅ |

**踩过的坑（记下来免得重复）：**

1. **`mysql` 客户端读 `.sql` 文件报 `ERROR 1366 Incorrect string value`**／原因：Windows 上客户端按本地代码页（GBK）读 UTF-8 文件／解法：加 `--default-character-set=utf8mb4`，或在 `my.ini` 的 `[client]` 段里设好并用 `--defaults-file` 启动。
2. **用 `curl -d '中文'` 发请求报 `Invalid UTF-8 middle byte 0xd0`**／原因：Windows 终端把命令行里的中文按 GBK 编码发出／解法：JSON 写进 UTF-8 文件用 `--data-binary @file.json`，或对 URL 参数做百分号编码。
3. **`mvn package` 报 `Unable to rename ... .jar`**／原因：上一次启动的 java 进程还占着 jar／解法：先停应用再打包。

## 七、已知限制

- 便携版 MySQL 没注册成服务，**开机要手动启一次**（`mysql.bat start`）。
- 密码摘要用 PBKDF2-HMAC-SHA256（12 万轮 + 随机盐），已经不算"明码"，但迭代参数仍应按机器性能和年限再调；更彻底可以换 Argon2。
- 校验规则与前端弹窗对齐：用户名 2—32 位、密码 6—32 位、邮箱填空可以、填了要合法。**前端拦一道是体验，后端拦一道才生效**。
- 没做鉴权（登录态、Token），接口目前任何人可调。
- 没配 CORS，前端的 Vue dev server 直接调会被浏览器拦。

---

## 八、跟着第五次作业的修改同步（2026-10-23 第二轮）

第五次作业（`web-quiz-admin`）按老师反馈改过一轮，后端这边跟着对齐：

| 第五次改了什么 | 第六次相应调整 |
|---|---|
| 密码规则收紧到 6—32 位、用户名 2—32 位、邮箱要校验 | 后端校验和前端完全一致（原来只挡了"非空"和"≥6"） |
| 强调「密码绝对不能以明码显示」 | 摘要从**单轮 SHA-256** 升级为 **PBKDF2-HMAC-SHA256**（随机盐 + 12 万轮），`password_hash` 字段从 `VARCHAR(64)` 加宽到 `VARCHAR(128)` |
| 前端错误提示要能看懂 | 后端返回的中文提示与前端一致（"密码最多 32 位"等），前端直接 `$message.error` 展示 |
| 前端仍然用 `passwordHash` 这个键传明文 | 后端同时接受 `password` 与 `passwordHash`，前端不用改、也不用重新部署 |

**实测（2026-10-23）**：

| 验证项 | 结果 |
|---|---|
| 用 `password` 键注册 | ✅ 入库为 `pbkdf2$120000$…`（83 字符） |
| 用 `passwordHash` 键注册（兼容旧调用） | ✅ |
| 同一密码注册两个账号，摘要不同（加盐生效） | ✅ 2 条不同摘要 |
| 用户名 1 位 / 40 位、密码 5 位 / 40 位、邮箱非法 | ✅ 全部 400 + 明确中文提示 |
| 改用户不传密码 → 摘要不变 | ✅ |
| 改用户传新密码 → 摘要变化 | ✅ |
| 前端 CDP 实测（新增用户、二次确认、无明文） | ✅ 通过 |

> 注意：`db/schema.sql` 里 `password_hash` 已改为 `VARCHAR(128)`。如果库是更早建的，要先跑：
> `ALTER TABLE t_user MODIFY password_hash VARCHAR(128) NOT NULL;`
> 否则 PBKDF2 串会被截断（83 > 64），密码就废了。

---

## 九、第七周迭代（2026-10-30，对应视频 9-4 ~ 9-7）

课件要求：完成 Java 后端对用户的增删改查——实现新用户的添加及 Service 层、用户名是否存在的检查、按 id 和 username 的逻辑删除、按分页和用户名查询用户。**课件写明"无作业需要提交"**，这次按四条全做了，另外把视频里留作练习的两项（硬删除、带关键字的其实该分页）也补上了。

### 逐条落地

| 视频 | 内容 | 实现位置 |
|---|---|---|
| 9-4 | 新用户添加 + Service 层 | `UserController#register` → `UserService#register`（校验、哈希、入库、回查） |
| 9-5 | 用户名是否已存在 | `UserMapper#countByUsername` → `UserService#exists` → `GET /api/users/exists`，注册前再查一次 |
| 9-6 | 按 id / username 逻辑删除 | `logicDeleteById` / `logicDeleteByUsername`，`GET /api/users/delete?id=` 或 `?username=` |
| 9-7 | 分页 + 用户名查询 | `PageBean{total,list}` + `LIMIT start,pageSize`，`GET /api/users/page` 与 `/search` |
| 9-6 练习 | 硬删除 | `DELETE /api/users/{id}/hard` |
| 9-7 作业 | 关键字查询也要分页 | `/api/users/page?keyword=` 已合并处理，翻页不丢关键字 |

### 设计上的三个决定（都有代价，记下来）

1. **逻辑删除用 `is_delete` 标记，所有查询都要带 `is_delete = 0`**。
   视频里特意提醒过"删了还能查到"这个坑——五处查询（findAll / findById / findByUsername / countByUsername / 分页 / 模糊）全都加了过滤条件。

2. **`uk_username` 唯一键必须去掉，改成普通索引**。
   这条视频没讲，但逻辑删除和唯一键是直接冲突的：数据还在表里，唯一键就仍然占着那个用户名，**删掉的用户名永远不能重新注册**。
   改法是唯一性交给应用层判断（`countByUsername` 只看 `is_delete = 0` 的行），这样"删了能重名"就成立了。实测：删掉 `tom` 之后重新注册 `tom` 成功。

3. **老接口形状不动**。
   `GET /api/users` 仍然返回数组（第五次作业的线上前端在调它），分页单独放在 `/page`。
   如果直接把 `/users` 改成返回 `PageBean`，线上那份前端会拿到对象当数组用，静默退化到离线演示模式——这类"不报错的坏"最难查。

### 与课堂代码的两处有意不同

- 课堂的 `Result` 用 `code = 1` 表示成功；本项目一直是 `code = 0`（前端拦截器按 0 判断）。不改，改了要连前端一起动。
- 课堂的删除用 `@GetMapping`；本项目保留了 RESTful 的 `DELETE /{id}` 给前端，同时提供课堂风格的 `GET /delete`，两条路都能走。

### 验证记录（2026-11-06 实测，15 项全过）

| 验证项 | 结果 |
|---|---|
| 新增三个用户 | ✅ |
| 用户名重复被拦（400「用户名已存在：tom」） | ✅ |
| 模糊查询 `keyword=tom` → tom / tommy | ✅ |
| 分页 + 关键字 `page=1&pageSize=1&keyword=tom` → total=2，只返回 1 条 | ✅ |
| 按 id 逻辑删除 → 返回「用户已删除」 | ✅ |
| 删除后 `exists` 为 false、查询里看不到 | ✅ |
| **库里数据还在，`is_delete=1`** | ✅ |
| **删掉的用户名能重新注册** | ✅ |
| 按 username 逻辑删除 | ✅ |
| 删不存在的用户名 → 「用户不存在或已被删除」 | ✅ |
| REST `DELETE /{id}` 也是逻辑删除 | ✅ |
| 硬删除后行真的消失（count=0） | ✅ |
| 分页默认值（不传参 = 第 1 页 5 条） | ✅ |
| 所有响应里没有 password / 摘要 | ✅ |
| 有效用户数与全表行数能对上（active=3, all=5） | ✅ |


### 课堂示例兼容层

课程笔记 2.4 / 2.5 里，管理端前端调的是**扁平路径**、字段名也和本项目不同。两条路都留着：

| 课堂路径（笔记里前端直接调） | 本项目路径（自己前端用） | 返回形状 |
|---|---|---|
| `GET /users?page=&pageSize=` | `GET /api/users/page` | 课堂：`data = {total, row}`；本项目：`data = {total, list}` |
| `GET /findUser?keyword=` | `GET /api/users/search` | 课堂：`data = [...]`；本项目：同上 |
| `POST /register` | `POST /api/users` | 课堂入参 `username/password/checkpassword`；本项目 `username/password` |
| `GET /deleteById?id=` | `GET /api/users/delete?id=` 或 `DELETE /api/users/{id}` | 都是逻辑删除 |

实现：`web/CourseCompatController`，只是把课堂的"外壳"映射到同一套 `UserService`，行为完全一致。字段别名给了 `userName`/`name`、`createTime`/`date` 两套，因为笔记正文和示例表格用的不是同一个名字。`userPassword` 恒为 `null`——本项目从不下发密码。

**一个反过来的坑**：笔记里让前端改到 8081 端口、把后端当 8080。本项目正好相反（前端 8080、后端 8081），所以**别照抄笔记里的 `vue.config.js`**，照抄会端口打架。代理规则沿用现有的 `/api` → `:8081` 即可，只是 axios 的地址要么写 `/api/users/page`，要么走兼容层的 `/users`。

### 迁移（老库要跑一次）

```sql
ALTER TABLE t_user ADD COLUMN is_delete TINYINT NOT NULL DEFAULT 0 AFTER email;
ALTER TABLE t_user ADD KEY idx_delete_username (is_delete, username);
ALTER TABLE t_user DROP INDEX uk_username;   -- 逻辑删除后用户名要能重用
ALTER TABLE t_user ADD KEY idx_username (username);
```

脚本在 `db/migrate-20261030.sql`，新库直接跑 `schema.sql` 就带这些。

> **本轮改动没有推送到 GitHub**（按用户要求）。

---

## 十、第八周迭代（2026-11-06，对应视频 9-8 ~ 10-2）

课件要求：继续完成 Java 后端的题目页，与管理台前后端联调——插入新题目、删除题目、分页查询、关键词查询。**"无作业需要提交"**，按内容全做了。

### 后端：题目模块补齐（照着用户模块的模板）

| 视频 | 内容 | 实现 |
|---|---|---|
| 9-8 | 插入新题目 | `POST /api/questions`（`/addQuestion` 兼容版接受 `answer` 为字母/下标/选项文字） |
| 9-9 | 删除题目 | 逻辑删除，`GET /api/questions/delete?id=` 与 `DELETE /{id}` |
| 9-10 | 分页 + 关键词查询 | `PageBean{total,list}` + `LIMIT`，`/api/questions/page`、`/api/questions/search` |
| 10-1 / 10-2 | 管理端页面联调 | 前端改成服务端分页（见下） |

数据库：`t_question` 增加 `is_delete`，六处查询全部带 `is_delete = 0` 过滤；迁移脚本 `db/migrate-20261106.sql`。

### 兼容层同步补了题目四个接口

| 课堂路径 | 等价的本项目接口 | 返回形状 |
|---|---|---|
| `GET /questions?page=&pageSize=` | `GET /api/questions/page` | `data = {total, qsBeanList}` |
| `GET /findQuestion?keyword=` | `GET /api/questions/search` | `data = [...]` |
| `POST /addQuestion` | `POST /api/questions` | 入参 `questionText/optionA~D/answer` |
| `GET /delQuestion?id=` | `GET /api/questions/delete?id=` | 逻辑删除 |

`qsBeanList` 里的 `answer` 给的是**正确选项的文字**（课堂 9-10 就是这么转的），不是下标；`/addQuestion` 则三种写法都认（`"B"`、`1`、选项原文）。

### 前端：题目页改成服务端分页

- `QuestionView.vue` 不再一次性拉全部再前端切片，改成 `currentPage / pageSize / total` + `@current-change`，每次翻页只取当前页。
- 关键字查询走 `/api/questions/page?keyword=`，**查询时回到第 1 页**；关键词会跟随翻页，不会翻到第二页就丢了条件。
- 删掉最后一页最后一条时页码会越界，代码里自动回退一页再取。
- 离线演示模式仍然有效：接口不通时用本地数组模拟分页（`pageQuestions` 的 fallback 分支）。

### 验证（2026-11-06 实测）

**后端（curl）**：`/questions` 第一页 5 条 / 总数 8、第二页 3 条；`/findQuestion?keyword=CSS` 命中 3 条；`/addQuestion` 传 `answer: "B"` → 库里存下标 1、返回 `"Proxy"`；`/delQuestion` 后 `is_delete=1`、总数回到 8。

**前端（Edge CDP 真点，9 项全过）**：

| 验证项 | 结果 |
|---|---|
| 第 1 页 5 条、总数显示 8 | ✅ |
| 点第 2 页 → 3 条 | ✅ |
| 关键字 CSS → 3 条且回到第 1 页 | ✅ |
| 清空关键字 → 回到 8 | ✅ |
| 新增一道题 → 总数 9 | ✅ |
| 删除该题 → 总数回 8 | ✅ |

> 本轮改动未推送 GitHub。
