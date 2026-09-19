package com.example.common.result;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 统一返回结果
 * 替代各 Controller / Service 里手写的 Map&lt;String, Object&gt;
 *
 * JSON 序列化后顶层字段：success / msg / data / 以及通过 with(...) 添加的额外字段
 *
 * 说明：项目未启用 Lombok，此处手写 getter/setter。
 */
public class Result<T> implements Serializable {

    private boolean success;
    private String msg;
    private T data;

    /** 额外字段，序列化时平铺到 JSON 顶层（不嵌套在 extra 对象里） */
    private final Map<String, Object> extra = new HashMap<>();

    public Result() {
    }

    public Result(boolean success, String msg) {
        this.success = success;
        this.msg = msg;
    }

    public Result(boolean success, String msg, T data) {
        this.success = success;
        this.msg = msg;
        this.data = data;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    /** 追加额外字段，支持链式调用。序列化后字段会平铺到 JSON 顶层 */
    public Result<T> with(String key, Object value) {
        this.extra.put(key, value);
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getExtra() {
        return extra;
    }

    @JsonAnySetter
    public void add(String key, Object value) {
        this.extra.put(key, value);
    }

    // ---- 静态工厂方法 ----

    public static <T> Result<T> ok() {
        return new Result<>(true, "ok");
    }

    public static <T> Result<T> ok(String msg) {
        return new Result<>(true, msg);
    }

    public static <T> Result<T> ok(String msg, T data) {
        return new Result<>(true, msg, data);
    }

    public static <T> Result<T> fail(String msg) {
        return new Result<>(false, msg);
    }

    public static <T> Result<T> fail(String msg, T data) {
        return new Result<>(false, msg, data);
    }
}
