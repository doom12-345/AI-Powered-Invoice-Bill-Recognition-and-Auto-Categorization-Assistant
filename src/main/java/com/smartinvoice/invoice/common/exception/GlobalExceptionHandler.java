package com.smartinvoice.invoice.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 参数校验失败（如空文件、格式不对）
    @ExceptionHandler(IllegalArgumentException.class)
    public Map<String, Object> handleIllegalArgument(IllegalArgumentException e) {
        return build(1, e.getMessage());
    }

    // 上传文件超过限制
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Map<String, Object> handleMaxUploadSize(MaxUploadSizeExceededException e) {
        return build(1, "图片大小超过限制（最大 10MB）");
    }

    // 其它未知错误
    @ExceptionHandler(Exception.class)
    public Map<String, Object> handleOther(Exception e) {
        log.error("服务器内部错误", e);
        return build(500, "服务器内部错误，请查看控制台日志");
    }

    private Map<String, Object> build(int code, String message) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", code);
        map.put("message", message);
        map.put("data", null);
        return map;
    }
}
