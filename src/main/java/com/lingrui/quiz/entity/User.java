package com.lingrui.quiz.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户表 t_user 的映射对象。
 *
 * 密码只存摘要，返回给前端时永远不带出去（WRITE_ONLY）。
 */
@Data
public class User {

    private Long id;

    private String username;

    /** 密码摘要。只允许写入，序列化时不输出。 */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String passwordHash;

    private String email;

    private LocalDateTime createdAt;
}
