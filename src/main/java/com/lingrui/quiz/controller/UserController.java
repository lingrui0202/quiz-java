package com.lingrui.quiz.controller;

import com.lingrui.quiz.common.PageBean;
import com.lingrui.quiz.common.Result;
import com.lingrui.quiz.entity.User;
import com.lingrui.quiz.service.UserService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户接口。
 *
 * 第七周（9-4 ~ 9-7）在这套接口上补齐了：
 * 新增时的用户名查重、按 id / username 的逻辑删除、分页查询、用户名模糊查询。
 *
 * 两个约定，先记下来免得后面看糊涂：
 * 1) 返回结构统一走 Result，`code = 0` 表示成功（课堂例子是 code=1 成功，
 *    这里沿用项目原有的 0，前端拦截器按 0 判断，不改就不跳坑）。
 * 2) 删除一律是逻辑删除（is_delete 置 1），另有一个 /hard 接口演示硬删除。
 *
 * 注意：任何响应里都不会出现密码（User.passwordHash 标了 WRITE_ONLY）。
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 全量列表（只给老前端用）。
     * 返回数组而不是分页对象，是为了不破坏第五次作业已经上线的那份前端。
     */
    @GetMapping
    public Result<List<User>> list() {
        return Result.ok(userService.list());
    }

    /** 分页查询（9-7）：page 从 1 开始，默认 1 / 5；keyword 可选，按用户名模糊匹配 */
    @GetMapping("/page")
    public Result<PageBean<User>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int pageSize,
            @RequestParam(required = false) String keyword) {
        return Result.ok(userService.page(page, pageSize, keyword));
    }

    /** 用户名模糊查询，不分页（9-7 的第二种查询方式） */
    @GetMapping("/search")
    public Result<List<User>> search(@RequestParam(required = false) String keyword) {
        return Result.ok(userService.search(keyword));
    }

    /** 用户名是否已存在（9-5）；前端注册前可以先调这个做即时提示 */
    @GetMapping("/exists")
    public Result<Boolean> exists(@RequestParam String username) {
        return Result.ok(userService.exists(username));
    }

    /**
     * 逻辑删除（9-6），GET 方式，和课堂一致：可以按 id，也可以按 username。
     * 例如：GET /api/users/delete?id=3
     *       GET /api/users/delete?username=tom
     */
    @GetMapping("/delete")
    public Result<String> deleteLogic(@RequestParam(required = false) Long id,
                                      @RequestParam(required = false) String username) {
        if (id != null) {
            return Result.ok(userService.deleteById(id) ? "用户已删除" : "用户不存在或已被删除");
        }
        if (username != null && !username.isBlank()) {
            return Result.ok(userService.deleteByUsername(username) ? "用户已删除" : "用户不存在或已被删除");
        }
        throw new IllegalArgumentException("请给出 id 或 username");
    }

    @GetMapping("/{id}")
    public Result<User> detail(@PathVariable Long id) {
        return Result.ok(userService.get(id));
    }

    @PostMapping
    public Result<User> register(@RequestBody User user) {
        return Result.ok(userService.register(user));
    }

    @PutMapping("/{id}")
    public Result<User> update(@PathVariable Long id, @RequestBody User user) {
        return Result.ok(userService.update(id, user));
    }

    /** 逻辑删除的 RESTful 写法，第五次作业的前端调的是这个 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.deleteById(id);
        return Result.ok();
    }

    /** 硬删除（9-6 留的练习）：数据真的从表里消失，谨慎用 */
    @DeleteMapping("/{id}/hard")
    public Result<String> hardDelete(@PathVariable Long id) {
        if (!userService.hardDelete(id)) {
            throw new IllegalArgumentException("用户不存在：id=" + id);
        }
        return Result.ok("用户已彻底删除");
    }
}
