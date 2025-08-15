package com.ceos.beatbuddy.global;

import com.ceos.beatbuddy.global.code.ErrorCode;
import com.ceos.beatbuddy.global.config.jwt.SecurityUtils;
import com.ceos.beatbuddy.global.discord.DiscordErrorNotifier;
import com.ceos.beatbuddy.global.discord.ErrorNotice;
import com.ceos.beatbuddy.global.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.ceos.beatbuddy.global.discord.CachingRequestFilter.MAX_BODY_PREVIEW;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {
        private final DiscordErrorNotifier notifier;

    /** 모든 예외를 처리하는 최종 핸들러 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleAllExceptions(Exception ex, HttpServletRequest req) {
        String traceId = (String) req.getAttribute("traceId");
        Long memberId = SecurityUtils.getCurrentMemberId();
        String query = (String) req.getAttribute("req.query");
        String clientIp = (String) req.getAttribute("req.clientIp");
        String userAgent = (String) req.getAttribute("req.userAgent");

        // 여기서 바디 추출
        String bodyPreview = "(empty)";
        if (req instanceof ContentCachingRequestWrapper wrapper) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                bodyPreview = new String(buf, StandardCharsets.UTF_8);
                if (bodyPreview.length() > MAX_BODY_PREVIEW) {
                    bodyPreview = bodyPreview.substring(0, MAX_BODY_PREVIEW) + "…(+truncated)";
                }
            }
        }

        ErrorNotice notice = new ErrorNotice(
                System.getenv().getOrDefault("ENV", "local"),
                req.getMethod(), req.getRequestURI(), 500,
                memberId, ex.getClass().getSimpleName(),
                safeMessage(ex), safeMessage(ex),
                traceId, OffsetDateTime.now(ZoneId.of("Asia/Seoul")).toString(),
                query,
                bodyPreview,
                clientIp,
                userAgent
        );

        notifier.send(notice)
                .doOnError(e -> log.warn("Discord notify failed", e))
                .subscribe();

        log.error("Unhandled Exception", ex);

        ApiCode internalServerError = new ApiCode() {
            @Override public HttpStatus getStatus() { return HttpStatus.INTERNAL_SERVER_ERROR; }
            @Override public String getMessage() { return "예상치 못한 서버 오류가 발생했습니다."; }
            @Override public String name() { return "INTERNAL_SERVER_ERROR"; }
        };

        return ResponseEntity
                .status(internalServerError.getStatus())
                .body(new ErrorResponseDTO(internalServerError));
    }

    private static String safeMessage(Throwable ex) {
        return Optional.ofNullable(ex.getMessage()).orElse("unexpected error");
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("Validation Error: {}", errors);
            ApiCode badRequest = new ApiCode() {
            @Override public HttpStatus getStatus() { return HttpStatus.BAD_REQUEST; }
            @Override public String getMessage() { return "요청 값이 유효하지 않습니다."; }
            @Override public String name() { return "BAD_REQUEST_VALIDATION"; }
        };
        return ResponseEntity
                .status(badRequest.getStatus())
                .body(new ErrorResponseDTO(badRequest, errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getConstraintViolations().forEach(violation -> {
            String field = violation.getPropertyPath().toString(); // 예: "searchPosts.keyword"
            String fieldName = field.contains(".") ? field.substring(field.lastIndexOf(".") + 1) : field;
            String message = violation.getMessage();
            errors.put(fieldName, message);
        });

        log.warn("Constraint Violation: {}", errors);

        ApiCode badRequest = new ApiCode() {
            @Override public HttpStatus getStatus() { return HttpStatus.BAD_REQUEST; }
            @Override public String getMessage() { return "요청 값이 유효하지 않습니다."; }
            @Override public String name() { return "BAD_REQUEST_VALIDATION"; }
        };

        return ResponseEntity
                .status(badRequest.getStatus())
                .body(new ErrorResponseDTO(badRequest, errors));
    }

    @ExceptionHandler(ResponseException.class)
    protected ResponseEntity<ErrorResponseDTO> handleResponseException(final ResponseException e) {
        ApiCode apiCode = e.getApiCode();

        log.error("Custom Exception Occurred: [Code: {}, Message: {}]", apiCode.name(), apiCode.getMessage(), e);

        return ResponseEntity
                .status(apiCode.getStatus())
                .body(new ErrorResponseDTO(apiCode));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String param = ex.getName();
        Class<?> requiredType = ex.getRequiredType();

        String message;

        if ("startDate".equals(param) || "endDate".equals(param)) {
            message = "날짜 형식이 잘못되었습니다. yyyy-MM-dd 형식이어야 합니다.";
        } else if (requiredType != null && requiredType.isEnum()) {
            message = String.format("'%s'은(는) 허용되지 않는 값입니다. 올바른 값을 입력해 주세요.", ex.getValue());
        } else {
            message = String.format("파라미터 '%s'의 형식이 올바르지 않습니다.", param);
        }

        log.warn("Parameter Type Mismatch: parameter={}, value={}, requiredType={}", 
                 param, ex.getValue(), requiredType != null ? requiredType.getSimpleName() : "unknown");

        return ResponseEntity
                .status(ErrorCode.INVALID_PARAMETER_TYPE.getStatus())
                .body(new ErrorResponseDTO(ErrorCode.INVALID_PARAMETER_TYPE, message));
    }

}