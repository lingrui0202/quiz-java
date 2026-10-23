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

    public User get(Long id) {
        User u = userMapper.findById(id);
        if (u == null) {
            throw new IllegalArgumentException("用户不存在：id=" + id);
        }
        return u;
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
        return userMapper.findById(user.getId());
    }

    /**
     * 修改用户。
     * 密码留空表示不改；填了才重新算摘要——前端永远拿不到原密码。
     */
    public User update(Long id, User user) {
        User old = get(id);

        if (user.getUsername() == null || user.getUsername().isBlank()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        User sameName = userMapper.findByUsername(user.getUsername());
        if (sameName != null && !sameName.getId().equals(id)) {
            throw new IllegalArgumentException("用户名已存在：" + user.getUsername());
        }

        String rawPassword = user.getPasswordHash();
        if (rawPassword == null || rawPassword.isBlank()) {
            user.setPasswordHash(old.getPasswordHash());
        } else {
            if (rawPassword.length() < 6) {
                throw new IllegalArgumentException("密码至少 6 位");
            }
            user.setPasswordHash(sha256(rawPassword));
        }

        user.setId(id);
        userMapper.update(user);
        return userMapper.findById(id);
    }

    public void delete(Long id) {
        if (userMapper.deleteById(id) == 0) {
            throw new IllegalArgumentException("用户不存在：id=" + id);
        }
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
