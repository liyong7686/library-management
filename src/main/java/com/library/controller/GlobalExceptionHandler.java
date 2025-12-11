package com.library.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

/**
 * 全局异常处理器
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RuntimeException.class)
    public ModelAndView handleRuntimeException(RuntimeException e, Model model) {
        logger.error("运行时异常: {}", e.getMessage(), e);
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("error", e.getMessage());
        mav.addObject("message", "发生错误：" + e.getMessage());
        return mav;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(Exception e, Model model) {
        logger.error("系统异常: {}", e.getMessage(), e);
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("error", "系统错误");
        mav.addObject("message", "发生未知错误：" + e.getMessage());
        return mav;
    }
}

