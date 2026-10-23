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
| POST | `/api/users` | 注册（密码转摘要后入库） |

统一返回 `{"code":0,"message":"ok","data":...}`；参数错误返回 400 + `code:1`。

---

## 四、实现要点

**MyBatis 用了两种写法，是为了对照课堂讲的两种方式：**

- `QuestionMapper` + `resources/mapper/QuestionMapper.xml`：SQL 写在 XML 里，含 `<where>` / `<if>` 动态 SQL、`<resultMap>` 下划线转驼峰、`<include>` 复用列清单；
- `UserMapper` / `SystemMapper`：用 `@Select` `@Insert` 注解写 SQL，适合短句。

**分层：** Controller（收参、返回统一结构）→ Service（校验、业务）→ Mapper（SQL）→ MySQL。Service 里没有一行 JDBC 代码，这就是 MyBatis 存在的意义。

**几个有意为之的点：**

- `Question.getOptions()` 是派生字段，标了 `access = READ_ONLY`，只在响应里出现，请求体不用传 `options`；
- `User.passwordHash` 标了 `WRITE_ONLY`，永远不会被序列化返回；
- 密码摘要用单轮 SHA-256，**仅作业演示**；真实项目必须 BCrypt/Argon2（带盐 + 可调代价）；
- 全局异常处理器让错误也走 `Result` 结构，不返回 Spring 默认错误页。

---

## 五、和前后端联调的关系

第五次课的 `web-quiz-admin` 用的是本地数组假数据，README 里写了「等以后做前后端联调再换成接口」。这个仓库就是那个"以后"——把 `QuestionView.vue` / `UserView.vue` 的数据源从数组换成 `axios.get('/api/questions')` 即可接上。前端 Vue dev server 在 8080、后端在 8081，跨域需要给后端加 `@CrossOrigin` 或 Vite/vue-cli 的 proxy（下一步再做）。

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
- 密码摘要用单轮 SHA-256，仅演示；生产必须上 BCrypt/Argon2。
- 没做鉴权（登录态、Token），接口目前任何人可调。
- 没配 CORS，前端的 Vue dev server 直接调会被浏览器拦。

