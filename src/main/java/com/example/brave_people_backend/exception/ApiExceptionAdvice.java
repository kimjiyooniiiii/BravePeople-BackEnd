package com.example.brave_people_backend.exception;

import com.example.brave_people_backend.exception.dto.ApiExceptionDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

// 예외를 처리하는 class
@RestControllerAdvice
@Slf4j
public class ApiExceptionAdvice extends ResponseEntityExceptionHandler {

    // 아이디, 비밀번호 불일치 시 발생하는 예외
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiExceptionDto exceptionHandler(final BadCredentialsException e) {
        log.error(e.getMessage());
        return ApiExceptionDto.builder()
                .status(HttpStatus.UNAUTHORIZED.toString())
                .errorMessage("아이디, 비밀번호를 확인해주세요.")
                .build();
    }

    // CustomExceptionHandler
    @ExceptionHandler(CustomException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiExceptionDto exceptionHandler(final CustomException e) {
        log.error("[Object : " + e.getObject() + "], [Message : "  + e.getMessage()+"]");
        return ApiExceptionDto.builder()
                .status(HttpStatus.BAD_REQUEST.toString())
                .errorMessage(e.getMessage())
                .build();
    }

    @ExceptionHandler(Custom404Exception.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiExceptionDto exceptionHandler(final Custom404Exception e) {
        log.error("[Object : " + e.getObject() + "], [Message : "  + e.getMessage()+"]");
        return ApiExceptionDto.builder()
                .status(HttpStatus.NOT_FOUND.toString())
                .errorMessage(e.getMessage())
                .build();
    }

    // 유효성 검사 Exception
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {

        ApiExceptionDto apiExceptionDto = ApiExceptionDto.builder()
                .status(HttpStatus.BAD_REQUEST.toString())
                .errorMessage("Invalid request content.")
                .build();

        BindingResult bindingResult = ex.getBindingResult();
        StringBuilder builder = new StringBuilder();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            builder.append("[");
            builder.append(fieldError.getField());
            builder.append("](은)는 ");
            builder.append(fieldError.getDefaultMessage());
            builder.append(" [입력된 값: ");
            builder.append(fieldError.getRejectedValue());
            builder.append("],");
        }
        log.error(builder.toString());

        return new ResponseEntity<>(apiExceptionDto, HttpStatus.BAD_REQUEST);
    }

    // Refresh Token 만료
    @ExceptionHandler(JwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiExceptionDto exceptionHandler(final JwtException e) {
        log.error("Refresh Token 만료");

        return ApiExceptionDto.builder()
                .status(HttpStatus.UNAUTHORIZED.toString())
                .errorMessage("Refresh Token 만료")
                .build();
    }
}
