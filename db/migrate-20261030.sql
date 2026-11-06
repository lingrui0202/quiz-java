-- 第七周（2026-10-30）迁移：给用户表加逻辑删除标记。
-- 老库执行一次即可；新建库直接跑 schema.sql 就带这一列。
--
-- 执行：mysql --defaults-file=E:/Dev/tools/mysql-my.ini -u root --skip-password quiz_db < db/migrate-20261030.sql

USE quiz_db;

ALTER TABLE t_user
    ADD COLUMN is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常，1 已删除' AFTER email;

ALTER TABLE t_user
    ADD KEY idx_delete_username (is_delete, username);

-- 逻辑删除和唯一键是冲突的：删掉的用户名仍然占着 uk_username，
-- 想用同一个名字重新注册就会撞唯一键。改成普通索引，
-- 用户名是否重复由 UserService 在应用层判断（只看 is_delete = 0 的行）。
ALTER TABLE t_user DROP INDEX uk_username;
ALTER TABLE t_user ADD KEY idx_username (username);
