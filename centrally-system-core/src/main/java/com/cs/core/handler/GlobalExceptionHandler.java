package com.cs.core.handler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import lombok.extern.slf4j.Slf4j;

/**
	✅ 400번대 (클라이언트 오류)
		🔸 400 Bad Request
			잘못된 요청 파라미터, 잘못된 데이터 형식 등
			대표 예외
				IllegalArgumentException
				HttpMessageNotReadableException
				BindException (Spring Validation)
				MethodArgumentNotValidException (DTO 검증 실패)
				MissingServletRequestParameterException (필수 파라미터 누락)
				TypeMismatchException (타입 변환 실패)
				ConstraintViolationException (Bean Validation)
				CustomValidationException (커스텀 검증 오류 등)
		
		🔸 401 Unauthorized
			인증 실패 (로그인 안 했거나 잘못된 인증)
			대표 예외
				BadCredentialsException (아이디/비밀번호 틀림)
				AuthenticationException (Spring Security 전반)
				InsufficientAuthenticationException (인증 부족)
				UsernameNotFoundException
				CredentialsExpiredException, AccountExpiredException
		
		🔸 403 Forbidden
			인증은 되었지만 권한 없음
			대표 예외
				AccessDeniedException (인가 실패)
				AccessDeniedHandler로 처리 가능 (Spring Security)
				DeniedException (직접 구현 가능)

		🔸 404 Not Found
			존재하지 않는 URL 또는 리소스
			대표 예외
				NoHandlerFoundException (요청 URL 매핑 실패)
				EntityNotFoundException (JPA에서 ID로 엔티티 못 찾았을 때)
				ResourceNotFoundException (커스텀으로 많이 만듬)
		
		🔸 405 Method Not Allowed
			지원되지 않는 HTTP 메서드 호출
			대표 예외
				HttpRequestMethodNotSupportedException
		
		🔸 406 Not Acceptable
			클라이언트가 요구하는 Accept 헤더 타입을 지원하지 않음
			대표 예외
				HttpMediaTypeNotAcceptableException
		
		🔸 415 Unsupported Media Type
			요청의 Content-Type을 지원하지 않음
			대표 예외
				HttpMediaTypeNotSupportedException
		
		🔸 429 Too Many Requests
			클라이언트의 요청이 너무 많음 (Rate Limit)
			직접 처리 또는 AOP 기반으로 구현 필요
			ex) Redis, Bucket4j, RateLimiter 등을 이용한 예외 처리
	
	🔴 500번대 (서버 오류)
		🔸 500 Internal Server Error
			서버 내부 예외 (코드 상의 NullPointerException 등)
			대표 예외
				NullPointerException
				IllegalStateException
				RuntimeException
				Exception (모든 처리되지 않은 예외)
				DataAccessException (DB 오류)
				InternalAuthenticationServiceException
				HttpMessageConversionException
				NoSuchElementException
		
		🔸 501 Not Implemented
			아직 구현되지 않은 기능 요청
			Spring에서는 직접 예외를 만들어서 처리
		
		🔸 502 Bad Gateway
			게이트웨이나 프록시 서버가 잘못된 응답을 받았을 때
			Spring Cloud Gateway 등에서 외부 시스템 장애 발생 시
			직접 처리 또는 로깅용 예외 커스터마이징
		
		🔸 503 Service Unavailable
			서버가 일시적으로 사용 불가능한 경우 (예: 유지보수, DB 다운)
			대표 예외
				ServiceUnavailableException (직접 정의)
				Spring Cloud Gateway에서 외부 서비스 미등록 시 503 발생
				ex) "No servers available for service" 오류 등
		
		🔸 504 Gateway Timeout
			프록시 서버에서 요청 타임아웃
			Spring Cloud Gateway 등에서 API 호출 응답 지연 시 발생
			예: 외부 API 호출 타임아웃, WebClient 타임아웃 등
			
 */
// DispatcherServlet 이후(컨트롤러 안)에서 난 예외만 처리
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
	
    
    @Value("${spring.servlet.multipart.max-file-size:10MB}")
    private String multipartMaxFileSize;
    
    @Value("${spring.servlet.multipart.max-request-size:10MB}")
    private String multipartMaxRequestSize;
    
    public static final String CC = "[CENTSYS_ERROR] ";


    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededE(MaxUploadSizeExceededException ex) {
    	log.error("파일 업로드 용량 초과", ex);
    	ErrorResponse error = new ErrorResponse(
    			"BAD_REQUEST", 
    			"파일 업로드 총 사이즈("+multipartMaxRequestSize+"), 파일 단일 사이즈("+multipartMaxFileSize+")를 준수해주세요"
			);
    	return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    // 전반적인 커스텀 에러 처리
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException ex) {
    	log.error("런타임 예외 발생", ex);
    	String msg = ex.getMessage();
    	ErrorResponse error;
        if (msg != null && msg.contains(CC)) {
        	// 내가 직접 던진 예외 확인 후 메시지 그대로 전달
            error = new ErrorResponse(CC, msg.replace(CC, ""));
        } else {
        	// 라이브러리나 다른 코드에서 발생한 일반 IAE
        	error = new ErrorResponse(CC, "잘못된 요청입니다.");
        }
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
    	log.error("서버 내부 예외 발생", ex);
        ErrorResponse error = new ErrorResponse("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.");
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    
    // ErrorResponse 클래스 정의 (내부 정적 클래스나 별도 파일)
    public static class ErrorResponse {
        private String code;
        private String message;
    
        public ErrorResponse(String code, String message) {
            this.code = code;
            this.message = message;
        }
    
        public String getCode() {
            return code;
        }
    
        public String getMessage() {
            return message;
        }
    }
}

