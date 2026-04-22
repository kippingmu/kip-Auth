package xyz.kip.auth.config.handler;

import xyz.kip.open.common.base.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/**
 * 全局异常处理器
 * @author xiaoshichuan
 * @version 2026-02-28
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理认证异常
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Result<?>> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        LOGGER.warn("Authentication failed", ex);
        Result<?> response = Result.failure("认证失败");
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    /**
     * 处理数据库异常，不向前端暴露 SQL、表结构、驱动等内部细节。
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Result<?>> handleDataAccessException(DataAccessException ex, WebRequest request) {
        LOGGER.error("Database access failed", ex);
        Result<?> response = Result.failure("系统繁忙，请稍后再试");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<?>> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        Result<?> response = Result.failure("参数错误：" + ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 处理所有异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<?>> handleGlobalException(Exception ex, WebRequest request) {
        LOGGER.error("Unhandled system error", ex);
        Result<?> response = Result.failure("系统繁忙，请稍后再试");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
