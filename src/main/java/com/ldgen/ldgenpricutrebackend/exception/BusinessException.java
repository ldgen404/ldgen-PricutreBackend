package com.ldgen.ldgenpricutrebackend.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.ldgen.ldgenpricutrebackend.common.BaseResponse;
import com.ldgen.ldgenpricutrebackend.common.ResultUtils;
import lombok.Getter;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }



}
