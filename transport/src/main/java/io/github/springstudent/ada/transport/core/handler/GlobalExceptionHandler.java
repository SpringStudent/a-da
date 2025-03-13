package io.github.springstudent.ada.transport.core.handler;

import io.github.springstudent.ada.common.utils.EmptyUtils;
import io.github.springstudent.ada.transport.core.bean.ResponseResult;
import io.github.springstudent.ada.transport.file.bean.FileException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author ZhouNing
 * @date 2024/12/31 11:22
 **/
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ResponseResult<Object> exceptionHandler(HttpServletRequest req, HttpServletResponse rep, Exception e) {
        if (EmptyUtils.isNotEmpty(e.getMessage()) && e.getMessage().indexOf("提示") != -1) {
            return ResponseResult.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
        }
        return ResponseResult.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务内部错误");
    }

    @ExceptionHandler(value = FileException.class)
    @ResponseBody
    public ResponseResult<Object> fileExceptionHandler(HttpServletRequest req, HttpServletResponse rep, Exception e) throws Exception {
        return ResponseResult.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
    }

}

