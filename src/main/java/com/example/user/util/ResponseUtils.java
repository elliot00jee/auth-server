package com.example.user.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

public class ResponseUtils {
    @Getter
    @Setter
    @ToString
    @RequiredArgsConstructor
    public static class ApiResponse<T> {
        private final ResultCd resultCd;
        private final T data;
        private final String resultMessage;
    }

    public enum ResultCd {S, BE}

    /**
     * 컨트롤러 단에서 정상 응답 보낼 때 호출
     * @param response data 필드에 넣어 반환할 객체
     */
    public static <T> ResponseEntity<?> success(T response) {
        return new ResponseEntity<>(
                new ApiResponse<>(ResultCd.S, response, null),
                HttpStatus.OK);
    }

    public static ResponseEntity<?> success() {
        return success(null);
    }

    public static ResponseEntity<?> success(String key, String value) {
        Map<String, String> response = new HashMap<>();
        response.put(key, value);

        return success(response);
    }

    /**
     * 컨트롤러 단에서 에러 응답 보낼 때 호출
     * 직접 호출하지 않고, 예외 발생 시 @ExceptionHandler에서 처리
     */
    public static ResponseEntity<?> error(String resultMessage, HttpStatus errorStatus) {
        return new ResponseEntity<>(
                new ApiResponse<>(ResultCd.BE, null, resultMessage),
                errorStatus);
    }

}
