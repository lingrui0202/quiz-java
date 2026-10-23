package com.lingrui.quiz.common;

/**
 * 统一返回结构，前端不用为每个接口写一套解析逻辑。
 */
public record Result<T>(int code, String message, T data) {

    public static <T> Result<T> ok(T data) {
        return new Result<>(0, "ok", data);
    }

    public static <T> Result<T> ok() {
        return new Result<>(0, "ok", null);
    }

    public static <T> Result<T> fail(String message) {
        return new Result<>(1, message, null);
    }
}
