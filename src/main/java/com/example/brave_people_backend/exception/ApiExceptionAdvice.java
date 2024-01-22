package com.example.brave_people_backend.exception;

import com.example.brave_people_backend.dto.ApiExceptionDto;
import org.springframework.http.HttpHeaders;
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
public class ApiExceptionAdvice extends ResponseEntityExceptionHandler {

    // 아이디, 비밀번호 불일치 시 발생하는 예외
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiExceptionDto exceptionHandler(final BadCredentialsException e) {
        return ApiExceptionDto.builder()
                .status(HttpStatus.UNAUTHORIZED.toString())
                .errorMessage("아이디, 비밀번호를 확인해주세요.")
                .build();
    }

/*    // 비회원 접근 시
    @ExceptionHandler(InsufficientAuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiExceptionDto exceptionHandler(final InsufficientAuthenticationException e) {
        return ApiExceptionDto.builder()
                .status(HttpStatus.UNAUTHORIZED.toString())
                .errorMessage("로그인 후 이용해주세요.")
                .build();
    }*/

    // CustomExceptionHandler
    @ExceptionHandler(CustomException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiExceptionDto exceptionHandler(final CustomException e) {
        return ApiExceptionDto.builder()
                .status(HttpStatus.BAD_REQUEST.toString())
                .errorMessage(e.getMessage())
                .build();
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        BindingResult bindingResult = ex.getBindingResult();
        StringBuilder sb = new StringBuilder();

        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            sb.append("[(").append(fieldError.getField()).append(")(은)는 ");
            sb.append(fieldError.getDefaultMessage()).append(".");
            sb.append(" 입력된 값: (").append(fieldError.getRejectedValue()).append(")] ");
        }

        ApiExceptionDto apiExceptionDto = ApiExceptionDto.builder()
                .status(HttpStatus.BAD_REQUEST.toString())
                .errorMessage(sb.toString())
                .build();

        return new ResponseEntity<>(apiExceptionDto, HttpStatus.BAD_REQUEST);
    }
}
