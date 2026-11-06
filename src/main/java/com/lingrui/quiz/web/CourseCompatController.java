package com.lingrui.quiz.web;

import com.lingrui.quiz.common.PageBean;
import com.lingrui.quiz.common.Result;
import com.lingrui.quiz.entity.User;
import com.lingrui.quiz.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 课堂示例兼容层（不是本项目的主接口）。
 *
 * 为什么要有这一层：课程笔记 2.4 / 2.5 里，管理端前端调的是
 *   GET  /users?page=&pageSize=      →  data = { total, row }
 *   GET  /findUser?keyword=          →  data = [ ... ]
 *   POST /register                   →  JSON
 *   GET  /deleteById?id=             →  JSON
 * 这些路径是平的（没有 /api 前缀），字段名也和本项目的实体不一样。
 *
 * 本项目自己的接口在 /api/users（见 UserController），风格是 REST + 统一 Result。
 * 两边共用同一套 Service，所以行为完全一致，只是"外壳"不同：
 * 照着笔记写前端时，axios 那几行可以原样抄，不用翻译。
 */
@RestController
public class CourseCompatController {

    private final UserService userService;

    public CourseCompatController(UserService userService) {
        this.userService = userService;
    }

    /** 分页列表：笔记里读的是 response.data.data.row */
    @GetMapping("/users")
    public Result<CoursePage<CourseUser>> users(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int pageSize) {
        PageBean<User> bean = userService.page(page, pageSize, null);
        List<CourseUser> rows = bean.list().stream().map(CourseUser::of).toList();
        return Result.ok(new CoursePage<>((int) bean.total(), rows));
    }

    /** 用户名模糊查询：笔记里读的是 response.data.data（直接是数组） */
    @GetMapping("/findUser")
    public Result<List<CourseUser>> findUser(@RequestParam(required = false) String keyword) {
        return Result.ok(userService.search(keyword).stream().map(CourseUser::of).toList());
    }

    /**
     * 注册。笔记里前端传的是表单对象，字段名 username / password / checkpassword。
     * checkpassword 传了就校验两次一致——笔记里的后端签名有它，但示例没实现。
     */
    @PostMapping("/register")
    public Result<CourseUser> register(@RequestBody RegisterForm form) {
        if (form.checkpassword() != null && !form.checkpassword().isBlank()
                && !form.checkpassword().equals(form.password())) {
            throw new IllegalArgumentException("两次输入的密码不一致");
        }
        User user = new User();
        user.setUsername(form.username());
        user.setPasswordHash(form.password());
        user.setEmail(form.email());
        return Result.ok(CourseUser.of(userService.register(user)));
    }

    /** 按 id 删除（逻辑删除）：笔记里是 GET /deleteById?id= */
    @GetMapping("/deleteById")
    public Result<String> deleteById(@RequestParam Long id) {
        return Result.ok(userService.deleteById(id) ? "用户已删除" : "用户不存在或已被删除");
    }

    // ------------------------------------------------------------------
    // 下面两个是"给课堂前端看的形状"，字段名对齐笔记里的 PageBean / User
    // ------------------------------------------------------------------

    /** 笔记里的 PageBean：total + row */
    public record CoursePage<T>(int total, List<T> row) {
    }

    /**
     * 笔记里的 User：id / userName / userPassword / isDelete / userRole / createTime / updateTime。
     * 额外给了 name、date 两个别名，因为示例表格里读的是 scope.row.name / scope.row.date，
     * 而笔记正文又说要改成 userName —— 两个都认，省得前端来回试。
     * userPassword 恒为 null：这个项目从来不下发密码。
     */
    public record CourseUser(Long id,
                             String userName,
                             String name,
                             String userPassword,
                             Integer isDelete,
                             Integer userRole,
                             LocalDateTime createTime,
                             LocalDateTime updateTime,
                             String date) {

        static CourseUser of(User u) {
            LocalDateTime created = u.getCreatedAt();
            return new CourseUser(
                    u.getId(),
                    u.getUsername(),
                    u.getUsername(),
                    null,
                    u.getIsDelete() == null ? 0 : u.getIsDelete(),
                    1,
                    created,
                    created,
                    created == null ? "" : created.toLocalDate().toString());
        }
    }

    /** 笔记里 /register 的入参形状 */
    public record RegisterForm(String username, String password, String checkpassword, String email) {
    }
}
