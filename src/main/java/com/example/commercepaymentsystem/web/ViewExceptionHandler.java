package com.example.commercepaymentsystem.web;

import com.example.commercepaymentsystem.common.exception.BusinessException;
import com.example.commercepaymentsystem.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;

/**
 * 화면 전용 예외 처리.
 *
 * GlobalExceptionHandler는 @RestControllerAdvice라 화면 요청에도 JSON을 돌려준다.
 * basePackageClasses로 web 패키지 컨트롤러에만 걸고, @Order로 GlobalExceptionHandler보다
 * 먼저 검사되게 해서 화면 요청만 가로챈다. (advice가 여럿이면 우선순위가 높은 쪽이 이긴다)
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice(basePackageClasses = ViewController.class)
public class ViewExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ModelAndView handleBusinessException(BusinessException e) {
        return errorView(e.getErrorCode(), e.getMessage());
    }

    /** /products/abc 처럼 경로 변수 타입이 안 맞는 경우. 놔두면 아래 catch-all 이 500 으로 만든다 */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ModelAndView handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return errorView(ErrorCode.INVALID_INPUT, ErrorCode.INVALID_INPUT.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(Exception e) {
        log.error("화면 처리 중 오류 발생", e);
        return errorView(ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.getMessage());
    }

    private ModelAndView errorView(ErrorCode errorCode, String message) {
        ModelAndView modelAndView = new ModelAndView("error/business", errorCode.getStatus());
        modelAndView.addObject("code", errorCode.getCode());
        modelAndView.addObject("message", message);
        return modelAndView;
    }
}
