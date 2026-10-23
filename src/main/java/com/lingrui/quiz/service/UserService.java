package com.lingrui.quiz.service;

import com.lingrui.quiz.entity.User;
import com.lingrui.quiz.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

/**
 * 用户业务层。
 */
@Service
public class UserService {

    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public List<User> list() {
        return userMapper.findAll();
    }

    public User register(User user) {
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (user.getPasswordHash() == null || user.getPasswordHash().length() < 6) {
            throw new IllegalArgumentException("密码至少 6 位");
        }
        if (userMapper.findByUsername(user.getUsername()) != null) {
            throw new IllegalArgumentException("用户名已存在：" + user.getUsername());
        }
        user.setPasswordHash(sha256(user.getPasswordHash()));
        userMapper.insert(user);
        // 回查一次，把数据库生成的 created_at 也带回去
        return userMapper.findByUsername(user.getUsername());
    }

    public int count() {
        return userMapper.count();
    }

    /**
     * 演示用的摘要：单轮 SHA-256。
     * 作业里够用，真实项目必须换成 BCrypt / Argon2 这类带盐、可调代价的算法。
     */
    private String sha256(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("JVM 不支持 SHA-256", e);
        }
    }
}
