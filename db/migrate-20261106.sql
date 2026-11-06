-- 第八周（2026-11-06）迁移：题目表加逻辑删除标记。
-- 老库执行一次即可；新建库直接跑 schema.sql 就带这一列。
--
-- 执行：mysql --defaults-file=E:/Dev/tools/mysql-my.ini -u root --skip-password quiz_db < db/migrate-20261106.sql

USE quiz_db;

ALTER TABLE t_question
    ADD COLUMN is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常，1 已删除' AFTER answer;

ALTER TABLE t_question
    ADD KEY idx_delete_content (is_delete, content);
