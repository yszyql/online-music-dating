package org.x.backend.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 统一响应结果类
 */
// 同一响应结果类
@NoArgsConstructor // 无参构造器
@AllArgsConstructor // 全参构造器
@Data
public class Result<T> {
    private Integer code; // 业务状态码  0:成功  1:失败
    private String message; // 描述信息
    private T data; // 响应数据
    private Map<String, String> errors; // 新增：错误详情字段

    // 快速返回成功响应结果，带结果集
    public static <E> Result<E> success(E date){
        return new Result<>(0,"操作成功",date,null);
    }

    // 快速返回成功响应结果，不带结果集
    public static Result success(){
        return new Result(0,"操作成功",null,null);
    }

    // 快速返回失败响应结果
    public static Result error(String message){
        return new Result(1,message,null,null);
    }

    // 错误返回（带错误详情）
    public static <T> Result<T> error(int code,String message, Map<String, String> errors) {
        return new Result<>(code, message, null, errors);
    }

}

