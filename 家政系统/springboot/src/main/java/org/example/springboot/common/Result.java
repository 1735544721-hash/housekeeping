package org.example.springboot.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "统一响应结果")
public class Result<T> {
    @Schema(description = "状态码")
    private String code;
    
    @Schema(description = "返回消息")
    private String msg;
    
    @Schema(description = "返回数据")
    private T data;
    
    public Result() {
    }

    public Result(T data) {
        this.data = data;
    }
    
    public Result(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    
    public Result(String code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg());
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), data);
    }

    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), msg, data);
    }

    public static <T> Result<T> error() {
        return new Result<>(ResultCode.ERROR.getCode(), ResultCode.ERROR.getMsg());
    }

    public static <T> Result<T> error(String msg) {
        return new Result<>(ResultCode.ERROR.getCode(), msg);
    }

    public static <T> Result<T> error(String code, String msg) {
        return new Result<>(code, msg);
    }

    public static <T> Result<T> error(String code, String msg, T data) {
        return new Result<>(code, msg, data);
    }
}
