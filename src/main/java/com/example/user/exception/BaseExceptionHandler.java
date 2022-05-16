package com.example.user.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static com.example.user.util.ResponseUtils.error;

/**
 * @ExceptionHandler와 함께 사용하여 예외 처리를 모든 컨트롤러 전반에 걸쳐 적용(※ 적용 범위 설정 가능)
 * 처리하고 싶은 RuntimeException 예외를 이 클래스에 등록하여 사용한다.
 */
@Slf4j
@ControllerAdvice
public class BaseExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<?> handleMethodArgumentNotValid(Exception e) {
        String message = ((MethodArgumentNotValidException) e).getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return getResponse(message, HttpStatus.OK);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<?> handleUnauthorizedException(Exception e) {
        return getResponse(e.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({
            UnauthenticatedException.class,
            AuthenticationException.class
    })
    public ResponseEntity<?> handleUnauthenticatedException(Exception e) {
        return getResponse(e.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            IllegalStateException.class,
            GeneralBusinessException.class
    })
    public ResponseEntity<?> handleBadRequestException(Exception e) {
        return getResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({
            Exception.class,
            RuntimeException.class,
            JsonException.class
    })
    public ResponseEntity<?> handleException(Exception e) {
        log.error("Internal server error occurred: {}", e.getMessage(), e);
        return getResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<?> getResponse(String message, HttpStatus status) {
        return error(message, status);
    }
}
