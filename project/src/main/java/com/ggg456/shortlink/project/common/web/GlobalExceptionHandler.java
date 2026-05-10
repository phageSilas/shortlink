package com.ggg456.shortlink.project.common.web;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.ggg456.shortlink.project.common.convention.exception.AbstractException;
import com.ggg456.shortlink.project.common.convention.result.Result;
import com.ggg456.shortlink.project.common.convention.result.Results;
import com.ggg456.shortlink.project.common.convention.result.errorcode.BaseErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Optional;

/**
 * 全局异常处理
 */
@Component
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 拦截参数验证异常
     */
    @SneakyThrows
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Result validExceptionHandler(HttpServletRequest request, MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        FieldError firstFieldError = CollectionUtil.getFirst(bindingResult.getFieldErrors());
        String exceptionStr = Optional.ofNullable(firstFieldError)
                .map(FieldError::getDefaultMessage)
                .orElse(StrUtil.EMPTY);
        log.error("[{}] {} [ex] {}", request.getMethod(), getUrl(request), exceptionStr);
        return Results.failure(BaseErrorCode.CLIENT_ERROR.code(), exceptionStr);
    }

    /**
     * 拦截应用内抛出的异常
     */
    @ExceptionHandler(value = {AbstractException.class})
    public Result abstractException(HttpServletRequest request, AbstractException ex) {
        if (ex.getCause() != null) {
            log.error("[{}] {} [ex] {}", request.getMethod(), request.getRequestURL().toString(), ex.toString(), ex.getCause());
            return Results.failure(ex);
        }
        log.error("[{}] {} [ex] {}", request.getMethod(), request.getRequestURL().toString(), ex.toString());
        return Results.failure(ex);
    }

    /**
     * 拦截未捕获异常
     */
    @ExceptionHandler(value = Throwable.class)
    public Result defaultErrorHandler(HttpServletRequest request, Throwable throwable) {
        log.error("[{}] {} ", request.getMethod(), getUrl(request), throwable);
        return Results.failure();
    }

    //--------------//
    /**
     * URL 格式错误
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result illegalArgumentExceptionHandler(HttpServletRequest request, IllegalArgumentException ex) {
        log.warn("[{}] {} - 参数错误: {}", request.getMethod(), getUrl(request), ex.getMessage());
        return Results.failure(BaseErrorCode.URL_FORMAT_ERROR.code(), "URL 格式错误：" + ex.getMessage());
    }

    /**
     * 域名解析失败（主机不存在）
     */
    @ExceptionHandler(java.net.UnknownHostException.class)
    public Result unknownHostExceptionHandler(HttpServletRequest request, java.net.UnknownHostException ex) {
        log.error("[{}] {} - 域名解析失败: {}", request.getMethod(), getUrl(request), ex.getMessage());
        return Results.failure(BaseErrorCode.DOMAIN_RESOLVE_ERROR.code(),
                "域名无法解析：" + ex.getMessage());
    }

    /**
     * 连接被拒绝
     */
    @ExceptionHandler(java.net.ConnectException.class)
    public Result connectExceptionHandler(HttpServletRequest request, java.net.ConnectException ex) {
        log.error("[{}] {} - 连接被拒绝: {}", request.getMethod(), getUrl(request), ex.getMessage());
        return Results.failure(BaseErrorCode.REMOTE_CONNECTION_REFUSED_ERROR.code(),
                "连接被拒绝：" + ex.getMessage());
    }

    /**
     * 连接或读取超时
     */
    @ExceptionHandler(java.net.SocketTimeoutException.class)
    public Result socketTimeoutExceptionHandler(HttpServletRequest request, java.net.SocketTimeoutException ex) {
        log.error("[{}] {} - 请求超时: {}", request.getMethod(), getUrl(request), ex.getMessage());
        return Results.failure(BaseErrorCode.REMOTE_TIMEOUT_ERROR.code(),
                "请求目标网页超时：" + ex.getMessage());
    }

    /**
     * 远程返回非 2xx 状态码
     */
    @ExceptionHandler(org.jsoup.HttpStatusException.class)
    public Result httpStatusExceptionHandler(HttpServletRequest request, org.jsoup.HttpStatusException ex) {
        log.error("[{}] {} - HTTP 状态码异常: {} {}", request.getMethod(), getUrl(request),
                ex.getStatusCode(), ex.getMessage());
        return Results.failure(BaseErrorCode.REMOTE_HTTP_STATUS_ERROR.code(),
                "目标服务器返回 " + ex.getStatusCode() + "：" + ex.getMessage());
    }

    /**
     * 其他 IO 异常兜底
     */
    @ExceptionHandler(java.io.IOException.class)
    public Result ioExceptionHandler(HttpServletRequest request, java.io.IOException ex) {
        log.error("[{}] {} - IO 异常: {}", request.getMethod(), getUrl(request), ex.getMessage());
        return Results.failure(BaseErrorCode.REMOTE_ERROR.code(), "网络请求失败：" + ex.getMessage());
    }
//---------------//
    private String getUrl(HttpServletRequest request) {
        if (StrUtil.isBlank(request.getQueryString())) {
            return request.getRequestURL().toString();
        }
        return request.getRequestURL().toString() + "?" + request.getQueryString();
    }
}
