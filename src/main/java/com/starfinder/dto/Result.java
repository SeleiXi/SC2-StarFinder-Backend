package com.starfinder.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 后端统一返回结果
 * 
 * @param <T>
 */
@Data
public class Result<T> implements Serializable {

    private Integer code; // 编码：1成功，0和其它数字为失败
    private String msg; // 错误信息
    private T data; // 数据

    public static <T> Result<T> success() {
        Result<T> result = new Result<T>();
        result.code = 200;
        return result;
    }

    public static <T> Result<T> success(T object) {
        Result<T> result = new Result<T>();
        result.data = object;
        result.code = 200;
        return result;
    }

    public static <T> Result<T> InternalServerError(String msg) {
        Result<T> result = new Result<T>();
        result.msg = msg;
        result.code = 500;
        return result;
    }

    public static <T> Result<T> BadRequest(String msg) {
        Result<T> result = new Result<T>();
        result.msg = msg;
        result.code = 400;
        return result;
    }

    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<T>();
        result.msg = msg;
        result.code = 0;
        return result;
    }

}