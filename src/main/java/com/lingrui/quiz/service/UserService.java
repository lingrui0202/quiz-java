package com.lingrui.quiz.service;

import com.lingrui.quiz.common.PasswordHasher;
import com.lingrui.quiz.entity.User;
import com.lingrui.quiz.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 用户业务层。
 *
 * 校验规则和前端 web-quiz-admin 的弹窗保持一致：
 * 用户名 2—32 位、密码 6—32 位、邮箱可为空但填了要合法。
 * 两边规则一致的意义是：前端拦一道是为了体验，后端拦一道才是真正生效的那道。
 */
@Service
public class UserService {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

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
        String username = normalizeUsername(user.getUsername());
        String raw = user.getPasswordHash();
        checkPassword(raw);

        if (userMapper.findByUsername(username) != null) {
            throw new IllegalArgumentException("用户名已存在：" + username);
        }

        user.setUsername(username);
        user.setEmail(normalizeEmail(user.getEmail()));
        user.setPasswordHash(PasswordHasher.hash(raw));
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
        String username = normalizeUsername(user.getUsername());

        User sameName = userMapper.findByUsername(username);
        if (sameName != null && !sameName.getId().equals(id)) {
            throw new IllegalArgumentException("用户名已存在：" + username);
        }

        String raw = user.getPasswordHash();
        if (raw == null || raw.isBlank()) {
            user.setPasswordHash(old.getPasswordHash());
        } else {
            checkPassword(raw);
            user.setPasswordHash(PasswordHasher.hash(raw));
        }

        user.setId(id);
        user.setUsername(username);
        user.setEmail(normalizeEmail(user.getEmail()));
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

    private String normalizeUsername(String username) {
        if (username == null || username.trim().length() < 2) {
            throw new IllegalArgumentException("用户名至少 2 位");
        }
        String name = username.trim();
        if (name.length() > 32) {
            throw new IllegalArgumentException("用户名最多 32 位");
        }
        return name;
    }

    private void checkPassword(String raw) {
        if (raw == null || raw.length() < 6) {
            throw new IllegalArgumentException("密码至少 6 位");
        }
        if (raw.length() > 32) {
            throw new IllegalArgumentException("密码最多 32 位");
        }
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        String value = email.trim();
        if (!EMAIL.matcher(value).matches()) {
            throw new IllegalArgumentException("邮箱格式不对");
        }
        return value;
    }
}
