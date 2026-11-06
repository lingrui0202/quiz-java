package com.lingrui.quiz.service;

import com.lingrui.quiz.common.PageBean;
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
 *
 * 第七周新增（对应 9-4 ~ 9-7）：
 * 用户名查重、逻辑删除（按 id / 按 username）、分页查询、用户名模糊查询。
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

    /** 用户名是否已存在（9-5） */
    public boolean exists(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }
        return userMapper.countByUsername(username.trim()) > 0;
    }

    public User register(User user) {
        String username = normalizeUsername(user.getUsername());
        String raw = user.getPasswordHash();
        checkPassword(raw);

        if (userMapper.countByUsername(username) > 0) {
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

    /** 逻辑删除，按 id（9-6）。删到了返回 true，没这条/已删过返回 false。 */
    public boolean deleteById(Long id) {
        return userMapper.logicDeleteById(id) > 0;
    }

    /** 逻辑删除，按用户名（9-6） */
    public boolean deleteByUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        return userMapper.logicDeleteByUsername(username.trim()) > 0;
    }

    /** 硬删除（9-6 里留的练习）：真的把行从表里删掉 */
    public boolean hardDelete(Long id) {
        return userMapper.hardDeleteById(id) > 0;
    }

    /**
     * 分页查询（9-7）。
     * keyword 为空就是普通分页；不为空就是"分页 + 用户名模糊"——
     * 视频里这块是留作作业的，这里一并做了，前端翻到第二页关键字也不会丢。
     *
     * @param page     页码，从 1 开始
     * @param pageSize 每页条数
     * @param keyword  用户名关键词，可空
     */
    public PageBean<User> page(int page, int pageSize, String keyword) {
        if (page < 1) {
            page = 1;
        }
        if (pageSize < 1) {
            pageSize = 5;
        }
        if (pageSize > 100) {
            pageSize = 100;
        }

        // 起始行 = (页码 - 1) × 每页条数；第一页从 0 开始
        int start = (page - 1) * pageSize;
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        String kw = hasKeyword ? keyword.trim() : null;

        long total = hasKeyword ? userMapper.countByKeyword(kw) : userMapper.countAll();
        List<User> rows = hasKeyword
                ? userMapper.selectPageByKeyword(start, pageSize, kw)
                : userMapper.selectPage(start, pageSize);

        return PageBean.of(total, rows);
    }

    /** 用户名模糊查询，不分页（9-7 的第二种查询方式） */
    public List<User> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return userMapper.findAll();
        }
        return userMapper.findByKeyword(keyword.trim());
    }

    public int count() {
        return userMapper.countAll();
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
