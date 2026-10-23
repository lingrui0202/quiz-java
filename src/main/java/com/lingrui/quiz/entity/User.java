package com.lingrui.quiz.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户表 t_user 的映射对象。
 *
 * 密码只存摘要，返回给前端时永远不带出去（WRITE_ONLY）。
 *
 * 关于字段名：这个字段在库里/对象里叫 passwordHash，但请求体里传进来的其实是**明文**，
 * 叫 passwordHash 是历史包袱。所以这里额外提供一个 password 别名，
 * 两个键都收：新代码请用 password，老的调用方（前端目前）传 passwordHash 也照样能用。
 */
@Data
public class User {

    private Long id;

    private String username;

    /** 写入时是明文密码（马上会被哈希掉），读出时是摘要。只允许写入。 */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String passwordHash;

    /** 同上的别名，语义更清楚的写法 */
    @JsonProperty(value = "password", access = JsonProperty.Access.WRITE_ONLY)
    public void setPassword(String password) {
        this.passwordHash = password;
    }

    private String email;

    private LocalDateTime createdAt;
}
