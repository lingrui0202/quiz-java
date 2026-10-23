-- 建库建表。对应课堂里"先把数据库准备出来"这一步。
-- 执行：mysql -u root -p < db/schema.sql

CREATE DATABASE IF NOT EXISTS quiz_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE quiz_db;

DROP TABLE IF EXISTS t_question;
CREATE TABLE t_question (
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    category   VARCHAR(32)  NOT NULL COMMENT '分类，如 HTML BASICS',
    content    VARCHAR(255) NOT NULL COMMENT '题干',
    option_a   VARCHAR(255) NOT NULL,
    option_b   VARCHAR(255) NOT NULL,
    option_c   VARCHAR(255) NOT NULL,
    option_d   VARCHAR(255) NOT NULL,
    answer     TINYINT      NOT NULL COMMENT '正确选项下标 0-3',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_category (category)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '题目表：题库沿用第二次课 quiz-game 的前端题库';

DROP TABLE IF EXISTS t_user;
CREATE TABLE t_user (
    id            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    username      VARCHAR(32) NOT NULL COMMENT '用户名',
    password_hash VARCHAR(64) NOT NULL COMMENT '密码摘要（演示用 SHA-256）',
    email         VARCHAR(64) DEFAULT NULL,
    created_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '用户表';
