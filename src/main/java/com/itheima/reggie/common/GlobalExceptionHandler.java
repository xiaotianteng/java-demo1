package com.itheima.reggie.common;
/*
* 全局异常处理
* */

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

@ControllerAdvice(annotations = {RestController.class, Controller.class}) // 拦截这两个注解的Controller
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {
    /*
    * 异常处理方法
    * */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)   // 指定异常类
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex){
        log.error(ex.getMessage());
        if(ex.getMessage().contains("Duplicate entry")){    // 判断异常信息是否包含Duplicate entry
            String[] split = ex.getMessage().split(" ");// 包含则存入数组，空格切割每个串
            String msg = split[2] + "已存在";
            return R.error(msg);
        }
        return R.error("未知错误");
    }



    /*
     * 异常处理方法
     * */
    @ExceptionHandler(CustomException.class)   // 指定异常类
    public R<String> exceptionHandler(CustomException ex){
        log.error(ex.getMessage());

        return R.error(ex.getMessage());
    }


}
