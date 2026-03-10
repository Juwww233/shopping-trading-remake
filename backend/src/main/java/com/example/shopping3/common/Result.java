package com.example.shopping3.common;

import lombok.Data;

@Data
public class Result<T> {
    // 前端判断的核心字段：200=成功，500=失败
    private Integer code;
    // 提示信息
    private String msg;
    // 返回的数据
    private T data;

    
    // 成功响应：固定code=200，msg=操作成功，携带数据
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200); // 关键：必须是200
        result.setMsg("操作成功");
        result.setData(data);
        return result;
    }

    // 失败响应：固定code=500，携带错误信息，data=null
    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMsg(msg);
        result.setData(null);
        return result;
    }
}