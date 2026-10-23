package com.lingrui.quiz.controller;

import com.lingrui.quiz.common.Result;
import com.lingrui.quiz.entity.User;
import com.lingrui.quiz.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户接口。
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Result<List<User>> list() {
        return Result.ok(userService.list());
    }

    @PostMapping
    public Result<User> register(@RequestBody User user) {
        return Result.ok(userService.register(user));
    }
}
