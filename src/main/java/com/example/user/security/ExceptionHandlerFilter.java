package com.example.user.security;

import com.example.user.exception.GeneralBusinessException;
import com.example.user.util.ResponseUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.example.user.util.ResponseUtils.*;

@RequiredArgsConstructor
@Component
public class ExceptionHandlerFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try{
            filterChain.doFilter(request, response);
        } catch(GeneralBusinessException e) {
            sendErrorResponse(HttpStatus.BAD_REQUEST, response, e);
        } catch(AuthenticationException e) {
            sendErrorResponse(HttpStatus.UNAUTHORIZED, response, e);
        } catch(Exception e) {
            sendErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, response, e);
        }
    }

    public void sendErrorResponse(HttpStatus status, HttpServletResponse response, Throwable e){
        response.setStatus(status.value());
        response.setContentType("application/json; charset=UTF-8");
        try {
            String message = objectMapper.writeValueAsString(
                    new ApiResponse(ResultCd.BE, null, e.getMessage()));
            response.getWriter().write(message);
        } catch (JsonProcessingException jsonProcessingException) {
            jsonProcessingException.printStackTrace();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
