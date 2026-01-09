package com.cs.gateway.filter;

import com.cs.gateway.dao.UserActivityLogDao;
import com.cs.gateway.entity.UserActivityLog;
import com.cs.gateway.service.JwtService;
import com.cs.gateway.util.UserAgentParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * ActivityLogFilter - 사용자 활동 이력 저장 필터
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * 
 * [역할]
 * Gateway를 통과하는 모든 POST, PUT, PATCH, DELETE 요청의 이력을 DB에 저장
 * (CUD: Create, Update, Delete 작업 추적)
 * 
 * [필터 실행 순서]
 * 1. JwtAuthFilter: JWT 검증 및 인증 정보 추출
 * 2. ActivityLogFilter(현재): 요청/응답 이력 기록
 * 3. 각 마이크로서비스로 라우팅 (Auth, HRM, Receipt 등)
 * 
 * [처리 흐름]
 * - GET 요청: 이력 저장 제외 (조회는 로그 안남김)
 * - 제외 경로 (/bulk-lookup 등): 조회 목적 POST도 제외
 * - POST /auth/login: 특별 처리 (성공/실패 모두 기록)
 * - 나머지 POST/PUT/PATCH/DELETE: 요청 본문 기록
 * 
 * [저장 위치]
 * PostgreSQL > gateway_user_activity_log 테이블
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
@Component
@RequiredArgsConstructor
public class ActivityLogFilter implements WebFilter {

    private static final Logger log = Loggers.getLogger(ActivityLogFilter.class);

    private final UserActivityLogDao userActivityLogDao;
    private final JwtService jwtService;

    // 최대 길이 제한 (너무 긴 로그가 남지 않도록)
    private static final int MAX_BODY_LENGTH = 1000;

    // 활동 로그에서 제외할 경로 패턴 (조회 목적의 POST 요청들)
    private static final String[] EXCLUDED_PATHS = {
        "/bulk-lookup",
        "/batch-query",
        "/auth/refresh"  // 리프레시 토큰 갱신 요청 제외
        // 필요시 추가 경로 등록
    };

    /**
     * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
     * 메인 필터 메서드 - 모든 요청을 가로채서 이력 저장 여부 판단
     * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String method = (request.getMethod() != null) ? request.getMethod().name() : "";
        String path = request.getURI().getPath();

        // ──────────────────────────────────────────────────────────────────────
        // 프론트에서 쿠키로 전달된 메뉴/기능 정보 추출
        // ──────────────────────────────────────────────────────────────────────
        // - Vue에서 axios 요청 시 쿠키에 X-Menu-Vue, X-Func-Vue 설정
        // - 예: "인사관리", "사원 등록"
        String menu = extractCookieValue(exchange, "X-Menu-Vue");
        String func = extractCookieValue(exchange, "X-Func-Vue");

        // ──────────────────────────────────────────────────────────────────────
        // CASE 1: OPTIONS 요청 (CORS preflight)
        // ──────────────────────────────────────────────────────────────────────
        // - 브라우저가 본 요청 전에 보내는 사전 요청
        // - 이력 저장 불필요
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return chain.filter(exchange);
        }

        // ──────────────────────────────────────────────────────────────────────
        // CASE 2: GET 요청 (조회)
        // ──────────────────────────────────────────────────────────────────────
        // - 조회 작업은 이력에 남기지 않음
        // - 로그가 너무 많아지는 것을 방지
        // - 쿠키 체크(CASE 4)와는 별개로, GET 요청은 항상 이력 저장 제외
        if ("GET".equalsIgnoreCase(method)) {
            return chain.filter(exchange);
        }

        // ──────────────────────────────────────────────────────────────────────
        // CASE 3: 제외 경로 체크 (조회 목적의 POST 요청)
        // ──────────────────────────────────────────────────────────────────────
        // - /bulk-lookup, /batch-query 등 조회 목적이지만 POST를 사용하는 경우
        // - 이력 저장 불필요
        for (String excludedPath : EXCLUDED_PATHS) {
            if (path.contains(excludedPath)) {
                return chain.filter(exchange);
            }
        }

        // ──────────────────────────────────────────────────────────────────────
        // CASE 4: 쿠키 체크 (메뉴/기능 정보가 없으면 이력 저장 건너뛰기)
        // ──────────────────────────────────────────────────────────────────────
        // - X-Menu-Vue, X-Func-Vue 쿠키가 모두 없으면 이력 저장하지 않음
        // - 이 체크는 POST/PUT/PATCH/DELETE 요청에서만 실행됨 (GET은 이미 CASE 2에서 제외)
        // - 프론트엔드에서 쿠키를 설정하지 않은 CUD 작업은 이력에 기록하지 않음
        if (!StringUtils.hasText(menu) && !StringUtils.hasText(func)) {
            return chain.filter(exchange);
        }

        // ──────────────────────────────────────────────────────────────────────
        // CASE 5: 로그인 요청 특별 처리
        // ──────────────────────────────────────────────────────────────────────
        // - POST /auth/login: 성공/실패 모두 이력 저장
        // - JWT가 발급되기 전이므로 별도 처리 필요
        if ("/auth/login".equals(path) && "POST".equalsIgnoreCase(method)) {
            return handleLoginRequest(exchange, chain, menu, func);
        }

        // ──────────────────────────────────────────────────────────────────────
        // CASE 6: 일반 POST/PUT/PATCH/DELETE 요청
        // ──────────────────────────────────────────────────────────────────────
        // - JWT를 통해 인증된 사용자의 CUD 작업 이력 저장
        // - 요청 본문을 읽어서 로그에 기록
        return DataBufferUtils.join(request.getBody())
            .defaultIfEmpty(exchange.getResponse().bufferFactory().wrap(new byte[0]))
            .flatMap(dataBuffer -> {

                // ──────────────────────────────────────────────────────────────
                // JWT 토큰에서 userEmail, username 추출
                // ──────────────────────────────────────────────────────────────
                // - JwtAuthFilter에서 이미 검증된 JWT 쿠키 사용
                // - 인증되지 않은 요청은 이 필터까지 오지 못함
                String jwtToken = JwtAuthFilter.extractJwtFromCookie(request);
                String userEmail = "N/A";
                String username = "N/A";
                if (StringUtils.hasText(jwtToken) && jwtService.validateToken(jwtToken)) {
                    userEmail = jwtService.getUserEmail(jwtToken);
                    username = jwtService.getUsername(jwtToken);
                }
                
                // ──────────────────────────────────────────────────────────────
                // 요청 본문 읽기
                // ──────────────────────────────────────────────────────────────
                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(bytes);
                DataBufferUtils.release(dataBuffer); // 메모리 해제

                // 본문 문자열 변환 및 널 문자 제거
                String bodyString = new String(bytes, StandardCharsets.UTF_8).replace("\u0000", "");
                String processedBody = bodyString;

                // Content-Type이 multipart/form-data인 경우 각 파트를 분리하여 처리
                String contentType = request.getHeaders().getFirst("Content-Type");
                if (contentType != null && contentType.contains(MediaType.MULTIPART_FORM_DATA_VALUE)) {
                    processedBody = processMultipartBody(bodyString, contentType);
                }

                // JSON 형식인 경우 처리
                String trimmedBody = processedBody.trim();
                if (trimmedBody.startsWith("{") && trimmedBody.endsWith("}")) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode jsonNode = mapper.readTree(trimmedBody);
                        boolean isNested = false;
                        if (jsonNode.isObject()) {
                            Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
                            while (fields.hasNext()) {
                                Map.Entry<String, JsonNode> entry = fields.next();
                                if (entry.getValue().isContainerNode()) {
                                    isNested = true;
                                    break;
                                }
                            }
                        }
                        if (isNested) {
                            // 중첩 JSON이면 커스텀 포매터로 들여쓰기 및 줄바꿈 적용
                            processedBody = formatJsonNode(jsonNode, 0);
                        } else {
                            // 평면 JSON이면 key-value 쌍을 한 줄에 표시 (한 칸 띄어쓰기로 구분)
                            Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
                            StringBuilder sb = new StringBuilder();
                            while (fields.hasNext()) {
                                Map.Entry<String, JsonNode> entry = fields.next();
                                sb.append("\"").append(entry.getKey()).append("\" : ")
                                  .append("\"").append(entry.getValue().asText()).append("\"");
                                if (fields.hasNext()) {
                                    sb.append("\n");
                                }
                            }
                            processedBody = sb.toString();
                        }
                    } catch (Exception e) {
                        // 파싱 실패 시 기존 방식으로 처리 (fallback)
                        processedBody = trimmedBody.substring(1, trimmedBody.length() - 1);
                        processedBody = processedBody.replaceAll(":(?!\\s)", " : ");
                        processedBody = processedBody.replaceAll(",", "\n");
                    }
                }

                // 최대 길이 제한 적용
                if (processedBody.length() > MAX_BODY_LENGTH) {
                    processedBody = processedBody.substring(0, MAX_BODY_LENGTH) + " [TRUNCATED]";
                }

                // 캐싱 후 body 재공급
                DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
                Flux<DataBuffer> cachedFlux = Flux.defer(() -> {
                    DataBuffer buffer = bufferFactory.wrap(bytes);
                    return Flux.just(buffer);
                });
                ServerHttpRequest decoratedRequest = new ServerHttpRequestDecorator(request) {
                    @Override
                    public Flux<DataBuffer> getBody() {
                        return cachedFlux;
                    }
                };
                ServerWebExchange mutatedExchange = exchange.mutate().request(decoratedRequest).build();

                return saveLogAndContinue(
                    mutatedExchange, chain,
                    userEmail, username, menu, func,
                    method, processedBody
                );
            });
    }
    

    /**
     * 재귀적으로 JSON 노드를 포맷하여 원하는 형식의 문자열을 생성
     * 
     * - 평면 필드는 "키":"밸류" 형태로 한 줄에 표시
     * - 컨테이너(객체)인 경우엔 "키": 를 출력하고, 다음 줄부터 들여쓰기를 적용하여 내부 필드를 출력
     * 
     * @param node JSON 노드
     * @param indentLevel 현재 들여쓰기 레벨 (0이면 최상위)
     * @return 포맷된 문자열
     */
    private String formatJsonNode(JsonNode node, int indentLevel) {
        StringBuilder sb = new StringBuilder();
        String indent = "    ".repeat(indentLevel);
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String key = entry.getKey();
                JsonNode value = entry.getValue();
                if (value.isContainerNode()) {
                    sb.append(indent).append("\"").append(key).append("\" : ").append("\n");
                    sb.append(formatJsonNode(value, indentLevel + 1));
                } else {
                    sb.append(indent)
                      .append("\"").append(key).append("\" : ")
                      .append("\"").append(value.asText()).append("\"");
                }
                if (fields.hasNext()) {
                    sb.append("\n");
                }
            }
        } else if (node.isArray()) {
            for (JsonNode element : node) {
                sb.append(formatJsonNode(element, indentLevel));
                sb.append("\n");
            }
        } else {
            sb.append(indent).append(node.asText());
        }
        return sb.toString();
    }
    
    
    /**
     * multipart/form-data 본문을 분석하여
     * - 각 파트의 헤더에서 필드명(name)과 filename(파일인 경우)을 추출하고,
     * - 파일인 경우 "[FILE: 필드명=파일이름]"으로, 그 외는 "필드명: 값"으로 표현하여
     * 최종 문자열을 생성한다.
     */
    private String processMultipartBody(String bodyString, String contentType) {
        // Content-Type 예: multipart/form-data; boundary=----WebKitFormBoundaryvRMw7NI64AcGchih
        String[] typeParts = contentType.split("boundary=");
        if (typeParts.length < 2) {
            return bodyString;
        }
        String boundary = "--" + typeParts[1].trim();
        // boundary로 분리 (정규표현식 사용)
        String[] parts = bodyString.split(Pattern.quote(boundary));
        StringBuilder sb = new StringBuilder();

        // 필드명 추출 정규표현식 (name="필드명")
        Pattern namePattern = Pattern.compile("name=\"([^\"]+)\"");
        // 파일 이름 추출 정규표현식 (filename="파일명")
        Pattern fileNamePattern = Pattern.compile("filename=\"([^\"]*)\"");

        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty() || part.equals("--")) {
                continue;
            }
            // 헤더와 본문을 분리 (헤더와 내용 사이에 두 개의 CRLF가 있음)
            String[] splitPart = part.split("\r\n\r\n", 2);
            String headers = splitPart.length > 0 ? splitPart[0] : "";
            String content = splitPart.length > 1 ? splitPart[1] : "";
            String fieldName = "";
            Matcher nameMatcher = namePattern.matcher(headers);
            if (nameMatcher.find()) {
                fieldName = nameMatcher.group(1);
            }
            // 파일 파트인지 여부
            Matcher fileNameMatcher = fileNamePattern.matcher(headers);
            if (fileNameMatcher.find()) {
                // 파일인 경우: 파일 이름만 출력
                String fileName = fileNameMatcher.group(1);
                sb.append("[FILE: ").append(fieldName).append("=").append(fileName).append("]\n");
            } else {
                // 파일이 아닌 일반 필드: 필드명과 값을 그대로 출력
                sb.append(fieldName).append(": ").append(content.trim()).append("\n");
            }
        }
        return sb.toString().trim();
    }

    
    private Mono<Void> saveLogAndContinue(ServerWebExchange exchange, WebFilterChain chain,
                                            String userEmail, String username,
                                            String menu, String func,
                                            String method, String bodyString) {
        // User-Agent 헤더에서 브라우저/OS 정보 추출 (Enum 사용)
        // String userAgent = exchange.getRequest().getHeaders().getFirst("User-Agent");
        // String browser = UserAgentParser.getBrowserString(userAgent);  // 법적 문제로 수집 중단
        // String os = UserAgentParser.getOSString(userAgent);  // 법적 문제로 수집 중단
        
        UserActivityLog logEntity = new UserActivityLog();
        logEntity.setUserEmail(userEmail);
        logEntity.setUsername(username);
        logEntity.setMenu(menu);
        logEntity.setFunction(func);
        // logEntity.setIp(exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());  // 법적 문제로 수집 중단
        logEntity.setIp("");  // 법적 문제로 수집 중단 - 공백 저장
        logEntity.setHttpMethod(method);
        logEntity.setHttpBody(bodyString);
        // logEntity.setBrowser(browser);  // 법적 문제로 수집 중단
        logEntity.setBrowser("");  // 법적 문제로 수집 중단 - 공백 저장
        // logEntity.setOs(os);
        logEntity.setOs("");  // 법적 문제로 수집 중단 - 공백 저장
        logEntity.setTimestamp(LocalDateTime.now());

        log.debug(">>> ActivityLogFilter: email={}, name={}, menu={}, func={}, path={}, method={}, body={}",
                userEmail, username, menu, func,
                exchange.getRequest().getURI().getPath(), method, bodyString);

        // DB 로그 저장 후 다음 필터로 전달
        return userActivityLogDao.saveLog(logEntity)
                .then(chain.filter(exchange));
    }

    
    private String extractCookieValue(ServerWebExchange exchange, String cookieName) {
        if (exchange.getRequest().getCookies().containsKey(cookieName)) {
            return URLDecoder.decode(
                exchange.getRequest().getCookies().getFirst(cookieName).getValue(),
                StandardCharsets.UTF_8
            );
        }
        return null;
    }

    /**
     * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
     * 로그인 요청 특별 처리 메서드
     * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
     * 
     * [호출 경로]
     * 1. 프론트엔드(Vue) → Gateway(현재 필터) → Auth 서비스(AuthLoginService)
     * 2. Gateway의 라우팅 설정에 의해 /auth/** 경로는 Auth 서비스로 프록시됨
     * 3. Auth 서비스에서 인증 처리 후 응답 반환
     * 4. 현재 필터에서 응답을 intercept하여 성공/실패 여부 확인
     * 
     * [처리 흐름]
     * 1. 요청 본문(Request Body) 읽기
     *    - POST /auth/login 요청의 본문에서 userEmail 추출
     *    - 예: {"userEmail": "test@example.com", "password": "1234"}
     * 
     * 2. 본문 재공급 준비
     *    - WebFlux는 본문을 한 번 읽으면 소진되므로 캐싱 필요
     *    - ServerHttpRequestDecorator로 본문을 다시 제공
     *    - Auth 서비스가 본문을 정상적으로 읽을 수 있도록 보장
     * 
     * 3. 응답 Intercept
     *    - ServerHttpResponseDecorator로 응답을 가로채서 처리
     *    - Auth 서비스의 응답 상태 코드(200, 401, 403 등) 확인
     * 
     * 4. 성공/실패 로그 저장
     *    - 성공(2xx): JWT 쿠키에서 실제 사용자 정보 추출하여 저장
     *    - 실패(4xx, 5xx): 요청 본문의 이메일과 에러 정보로 저장
     * 
     * [저장 데이터]
     * - username: 사용자 이름 (성공 시 JWT에서 추출, 실패 시 "로그인 실패")
     * - userEmail: 이메일 (성공 시 JWT에서 추출, 실패 시 요청 본문에서 추출)
     * - menu: "인증" (쿠키 X-Menu-Vue가 없으면 기본값)
     * - function: "로그인 성공" 또는 "로그인 실패 (상태코드)"
     * - httpMethod: "POST"
     * - httpBody: 요청 본문 (비밀번호 제외, userEmail만)
     * - timestamp: 현재 시각
     * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
     */
    private Mono<Void> handleLoginRequest(ServerWebExchange exchange, WebFilterChain chain,
                                           String menu, String func) {
        ServerHttpRequest request = exchange.getRequest();
        
        // ──────────────────────────────────────────────────────────────────────
        // STEP 1: 요청 본문 읽기
        // ──────────────────────────────────────────────────────────────────────
        // - DataBufferUtils.join()으로 분산된 버퍼를 하나로 합침
        // - 본문이 비어있으면 빈 바이트 배열로 처리
        return DataBufferUtils.join(request.getBody())
            .defaultIfEmpty(exchange.getResponse().bufferFactory().wrap(new byte[0]))
            .flatMap(dataBuffer -> {
                // 바이트 배열로 변환
                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(bytes);
                DataBufferUtils.release(dataBuffer); // 메모리 누수 방지
                
                // JSON 본문을 문자열로 변환하고 userEmail 추출
                String bodyString = new String(bytes, StandardCharsets.UTF_8);
                String userEmailFromBody = extractUserEmailFromLoginBody(bodyString);
                
                // ──────────────────────────────────────────────────────────────────────
                // STEP 2: 본문 재공급을 위한 Decorated Request 생성
                // ──────────────────────────────────────────────────────────────────────
                // - WebFlux는 본문 스트림을 한 번 읽으면 소진됨
                // - Auth 서비스가 본문을 읽을 수 있도록 캐싱된 본문을 다시 제공
                DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
                Flux<DataBuffer> cachedFlux = Flux.defer(() -> {
                    DataBuffer buffer = bufferFactory.wrap(bytes);
                    return Flux.just(buffer);
                });
                ServerHttpRequest decoratedRequest = new ServerHttpRequestDecorator(request) {
                    @Override
                    public Flux<DataBuffer> getBody() {
                        return cachedFlux; // 캐싱된 본문 반환
                    }
                };
                
                // ──────────────────────────────────────────────────────────────────────
                // STEP 3: 응답 Intercept를 위한 Decorated Response 생성
                // ──────────────────────────────────────────────────────────────────────
                // - Auth 서비스의 응답을 가로채서 상태 코드 확인
                // - 성공/실패에 따라 다른 로그 저장 로직 실행
                ServerHttpResponse originalResponse = exchange.getResponse();
                ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                    @Override
                    public Mono<Void> writeWith(org.reactivestreams.Publisher<? extends DataBuffer> body) {
                        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                        // Auth 서비스에서 응답이 돌아온 시점
                        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                        
                        // 응답 상태 코드 추출
                        // - 200: 로그인 성공
                        // - 401: 인증 실패 (비밀번호 틀림, 이메일 없음)
                        // - 403: 권한 없음 (비활성 계정, 승인 대기 등)
                        // - 500: 서버 에러
                        HttpStatusCode statusCode = getDelegate().getStatusCode();
                        
                        // ──────────────────────────────────────────────────────────
                        // CASE 1: 로그인 성공 (2xx)
                        // ──────────────────────────────────────────────────────────
                        if (statusCode != null && statusCode.is2xxSuccessful()) {
                            log.info(">>> [로그인 성공] 상태 코드: {}, 이메일: {}", statusCode, userEmailFromBody);
                            
                            // 응답 쿠키에서 JWT 토큰 추출
                            // - Auth 서비스가 Set-Cookie 헤더로 JWT를 전달
                            // - 예: Set-Cookie: jwt=eyJhbGc...; Path=/; HttpOnly
                            List<String> setCookieHeaders = getDelegate().getHeaders().get("Set-Cookie");
                            String jwtToken = extractJwtFromSetCookie(setCookieHeaders);
                            
                            // final 변수 사용 (람다 내부에서 사용하기 위함)
                            final AtomicReference<String> userEmailRef = new AtomicReference<>(userEmailFromBody);
                            final AtomicReference<String> usernameRef = new AtomicReference<>("로그인 성공");
                            
                            // JWT에서 실제 사용자 정보 추출
                            if (StringUtils.hasText(jwtToken) && jwtService.validateToken(jwtToken)) {
                                try {
                                    // JWT Claims에서 userEmail과 username 파싱
                                    userEmailRef.set(jwtService.getUserEmail(jwtToken));
                                    usernameRef.set(jwtService.getUsername(jwtToken));
                                    log.info(">>> [JWT 파싱 성공] 이메일: {}, 이름: {}", 
                                            userEmailRef.get(), usernameRef.get());
                                } catch (Exception e) {
                                    log.warn(">>> [JWT 파싱 실패] 기본값 사용", e);
                                }
                            } else {
                                log.warn(">>> [JWT 없음 또는 유효하지 않음] 요청 본문의 이메일 사용");
                            }
                            
                            // User-Agent 헤더에서 브라우저/OS 정보 추출 (Enum 사용)
                            // String userAgent = exchange.getRequest().getHeaders().getFirst("User-Agent");
                            // String browser = UserAgentParser.getBrowserString(userAgent);  // 법적 문제로 수집 중단
                            // String os = UserAgentParser.getOSString(userAgent);  // 법적 문제로 수집 중단
                            
                            // 성공 로그 엔티티 생성
                            UserActivityLog logEntity = new UserActivityLog();
                            logEntity.setUserEmail(userEmailRef.get());
                            logEntity.setUsername(usernameRef.get());
                            logEntity.setMenu(menu != null ? menu : "인증");
                            logEntity.setFunction(func != null ? func : "로그인 성공");
                            // logEntity.setIp(exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());  // 법적 문제로 수집 중단
                            logEntity.setIp("");  // 법적 문제로 수집 중단 - 공백 저장
                            logEntity.setHttpMethod("POST");
                            logEntity.setHttpBody("\"userEmail\" : \"" + userEmailFromBody + "\"");
                            // logEntity.setBrowser(browser);  // 법적 문제로 수집 중단
                            logEntity.setBrowser("");  // 법적 문제로 수집 중단 - 공백 저장
                            // logEntity.setOs(os);  // 법적 문제로 수집 중단
                            logEntity.setOs("");  // 법적 문제로 수집 중단 - 공백 저장
                            logEntity.setTimestamp(LocalDateTime.now());
                            
                            // DB에 비동기 저장 (응답 지연 방지)
                            userActivityLogDao.saveLog(logEntity).subscribe(
                                saved -> log.info(">>> [DB 저장 성공] 로그인 성공 이력: email={}, name={}", 
                                        userEmailRef.get(), usernameRef.get()),
                                error -> log.error(">>> [DB 저장 실패] 로그인 성공 이력", error)
                            );
                        } 
                        // ──────────────────────────────────────────────────────────
                        // CASE 2: 로그인 실패 (4xx, 5xx)
                        // ──────────────────────────────────────────────────────────
                        else if (statusCode != null) {
                            log.warn(">>> [로그인 실패] 상태 코드: {}, 이메일: {}", statusCode, userEmailFromBody);
                            
                            // User-Agent 헤더에서 브라우저/OS 정보 추출 (Enum 사용)
                            // String userAgent = exchange.getRequest().getHeaders().getFirst("User-Agent");
                            // String browser = UserAgentParser.getBrowserString(userAgent);  // 법적 문제로 수집 중단
                            // String os = UserAgentParser.getOSString(userAgent);  // 법적 문제로 수집 중단
                            
                            // 실패 로그 엔티티 생성
                            UserActivityLog logEntity = new UserActivityLog();
                            logEntity.setUserEmail(userEmailFromBody); // 요청 본문의 이메일 사용
                            logEntity.setUsername("로그인 실패"); // 실패 시 기본 이름
                            logEntity.setMenu(menu != null ? menu : "인증");
                            logEntity.setFunction("로그인 실패 (" + statusCode.value() + ")");
                            // logEntity.setIp(exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());  // 법적 문제로 수집 중단
                            logEntity.setIp("");  // 법적 문제로 수집 중단 - 공백 저장
                            logEntity.setHttpMethod("POST");
                            logEntity.setHttpBody("\"userEmail\" : \"" + userEmailFromBody + "\"");
                            // logEntity.setBrowser(browser);  // 법적 문제로 수집 중단
                            logEntity.setBrowser("");  // 법적 문제로 수집 중단 - 공백 저장
                            // logEntity.setOs(os);  // 법적 문제로 수집 중단
                            logEntity.setOs("");  // 법적 문제로 수집 중단 - 공백 저장
                            logEntity.setTimestamp(LocalDateTime.now());
                            
                            // DB에 비동기 저장
                            userActivityLogDao.saveLog(logEntity).subscribe(
                                saved -> log.info(">>> [DB 저장 성공] 로그인 실패 이력: email={}, 상태={}", 
                                        userEmailFromBody, statusCode.value()),
                                error -> log.error(">>> [DB 저장 실패] 로그인 실패 이력", error)
                            );
                        }
                        
                        // 원본 응답을 그대로 클라이언트에게 전달
                        return super.writeWith(body);
                    }
                };
                
                // ──────────────────────────────────────────────────────────────────────
                // STEP 4: 수정된 요청/응답으로 다음 필터 체인 실행
                // ──────────────────────────────────────────────────────────────────────
                // - decoratedRequest: 본문을 재공급할 수 있는 요청
                // - decoratedResponse: 응답을 intercept하는 응답
                // - 이제 Auth 서비스로 전달됨
                ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(decoratedRequest)
                    .response(decoratedResponse)
                    .build();
                
                // Auth 서비스 호출 (chain.filter 실행)
                // → AuthLoginService.login() 메서드가 실행됨
                // → 응답이 돌아오면 위의 decoratedResponse.writeWith()가 실행됨
                return chain.filter(mutatedExchange);
            });
    }

    /**
     * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
     * 로그인 요청 본문에서 userEmail 추출
     * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
     * 
     * [입력 예시]
     * {
     *   "userEmail": "test@example.com",
     *   "password": "1234"
     * }
     * 
     * [출력]
     * "test@example.com"
     * 
     * [실패 시]
     * "N/A" 반환
     * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
     */
    private String extractUserEmailFromLoginBody(String bodyString) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(bodyString);
            JsonNode emailNode = jsonNode.get("userEmail");
            if (emailNode != null) {
                return emailNode.asText();
            }
        } catch (Exception e) {
            log.warn(">>> [JSON 파싱 실패] 로그인 본문에서 userEmail 추출 실패", e);
        }
        return "N/A";
    }

    /**
     * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
     * Set-Cookie 헤더에서 JWT 토큰 추출
     * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
     * 
     * [Auth 서비스 응답 헤더]
     * Set-Cookie: jwt=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...; Path=/; HttpOnly; Max-Age=3600
     * 
     * [처리 과정]
     * 1. Set-Cookie 헤더 리스트 순회
     * 2. "jwt="로 시작하는 쿠키 찾기
     * 3. ";" 기준으로 분리하여 토큰값만 추출
     * 4. "jwt=" 제거하여 순수 토큰 반환
     * 
     * [반환]
     * - 성공: JWT 토큰 문자열
     * - 실패: null
     * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
     */
    private String extractJwtFromSetCookie(List<String> setCookieHeaders) {
        if (setCookieHeaders == null || setCookieHeaders.isEmpty()) {
            return null;
        }
        
        for (String setCookie : setCookieHeaders) {
            if (setCookie.startsWith("jwt=")) {
                // jwt=토큰값; Path=/; HttpOnly 형식에서 토큰값만 추출
                String[] parts = setCookie.split(";");
                if (parts.length > 0) {
                    String jwtPart = parts[0].trim();
                    if (jwtPart.startsWith("jwt=")) {
                        return jwtPart.substring(4); // "jwt=" 제거
                    }
                }
            }
        }
        return null;
    }
}
