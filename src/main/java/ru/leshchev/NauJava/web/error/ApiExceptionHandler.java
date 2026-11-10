package ru.leshchev.NauJava.web.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.rest.core.RepositoryConstraintViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler({ResourceNotFoundException.class,
            org.springframework.data.rest.webmvc.ResourceNotFoundException.class})
    public ResponseEntity<Object> notFound(Exception exception, HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, "Resource not found", request.getRequestURI());
    }

    @ExceptionHandler({IllegalArgumentException.class, ConstraintViolationException.class,
            RepositoryConstraintViolationException.class})
    public ResponseEntity<Object> badRequest(Exception exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "Invalid request data", request.getRequestURI());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> conflict(Exception exception, HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, "Request conflicts with existing data", request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> unexpected(Exception exception, HttpServletRequest request) {
        log.error("Request failed: {}", request.getRequestURI(), exception);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", request.getRequestURI());
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception exception, Object body,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        HttpStatus resolved = HttpStatus.valueOf(status.value());
        return new ResponseEntity<>(new ApiError(status.value(), resolved.getReasonPhrase(),
                resolved.getReasonPhrase(), path), headers, status);
    }

    private ResponseEntity<Object> response(HttpStatus status, String message, String path) {
        return ResponseEntity.status(status).body(
                new ApiError(status.value(), status.getReasonPhrase(), message, path));
    }
}
