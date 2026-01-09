package com.cs.gateway.filter;

import com.cs.gateway.dao.RoleChangeEventDao;
import com.cs.gateway.entity.RoleChangeEvent;
import com.cs.gateway.service.JwtService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.r2dbc.postgresql.codec.Json;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 *  📌 RoleChangeValidationFilter
 *
 *  1) 실시간 권한 이벤트 기록
 *     ──────────────────────────────────────────────────────────────────
 *       • 엔드포인트 :  /auth/user-permissions
 *       • 메서드    :  POST | PUT | PATCH | DELETE
 *       • 동작
 *         ▸ 요청 Body (JSON) → gateway_role_change_event INSERT
 *         ▸ 단, **수정 대상이 자기 자신(userId 동일)** 이면 이벤트를 남기지 않고 통과
 *
 *
 *  2) 권한 변동 알림(로그아웃 유도)
 *     ──────────────────────────────────────────────────────────────────
 *       • 일반 API 호출 시
 *         ▸ 해당 사용자의 미확인 이벤트가 존재 ⇒
 *             ▸ checked_at = NOW() 로 마킹
 *             ▸ 401 + X-Reason: ROLE_CHANGED 응답  → 프런트는 즉시 로그아웃
 *
 *
 *  3) 로그인 성공 후 이벤트 소거
 *     ──────────────────────────────────────────────────────────────────
 *       • 엔드포인트 :  /auth/login  (POST)
 *       • 동작
 *         ▸ 마이크로서비스 응답이 **2xx** 이고,
 *           Set-Cookie 헤더에서 JWT 추출 → userId 획득
 *         ▸ 해당 userId 의 미확인 이벤트 checked_at 업데이트
 *         ▸ 이후 정상 응답 그대로 전달
 *
 *
 *  ✦ JWT 가 없거나 유효하지 않을 경우
 *    ─ /auth/user-permissions 이외의 경로 : 그대로 통과
 *    ─ /auth/user-permissions             : 401 Unauthorized
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Component
@RequiredArgsConstructor
public class RoleChangeValidationFilter implements WebFilter {

    private final RoleChangeEventDao eventDao;
    private final ObjectMapper om = new ObjectMapper();
    private final JwtService jwtService;

    // 권한 변경 엔드포인트
    private static final String TARGET  = "/auth/user-permissions";
    // “실시간 권한 확인”을 건너뛰어야 하는 엔드포인트(예: 로그인)
    private static final String LOGIN_API    = "/auth/login";
    // 권한 변경 HTTP 메서드(쉼표 구분)
    private static final Set<String> MUTATE = Set.of("POST","PUT","PATCH","DELETE");
    // 중복 DB 데이터 저장 방지
    private static final String ATTR_EVT_INSERTED = "role-change-inserted";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        ServerHttpRequest req = exchange.getRequest();
        String path   = req.getURI().getPath();
        String method = req.getMethod() != null ? req.getMethod().name() : "";
        
        boolean isLoginCall = path.equals(LOGIN_API) && "POST".equals(method);
        if (isLoginCall) {
            // ↓ 마이크로서비스로 그대로 전달
            return chain.filter(exchange)
                        .then(afterLoginSucceeded(exchange));   // ★ 아래 함수
        }
        
        /* ── JWT 쿠키 추출 ─────────────────────────────────────────────── */
        String jwt = JwtAuthFilter.extractJwtFromCookie(req);
        boolean isTargetCall = path.equals(TARGET) && MUTATE.contains(method);

        /* (A)  /auth/user-permissions  ➜  JWT 필수 ---------------------- */
        if (isTargetCall) {
            if (!org.springframework.util.StringUtils.hasText(jwt) || !jwtService.validateToken(jwt)) {
                return unauthorized(exchange);                // 401 - 로그인 만료 처리
            }
        }
        /* (B)  그 외 경로 ➜  JWT 없으면 그냥 통과 ----------------------- */
        else if (!org.springframework.util.StringUtils.hasText(jwt) || !jwtService.validateToken(jwt)) {
            return chain.filter(exchange);
        }

        /* ── 여기까지 오면 JWT 는 유효함 ──────────────────────────────── */
        Integer userId = jwtService.getClaim(jwt, "userId", Integer.class);
        if (userId == null) {                      // 예상 밖: claim 없음 → 통과
            return chain.filter(exchange);
        }

        /* (1) 권한 변경 API ------------------------------------------------- */
        if (isTargetCall) {
            return cacheBodyAndInsert(exchange, chain, req, userId, method);
        }

        /* (2) 일반 요청 → 미확인 이벤트 체크 ------------------------------- */
        return eventDao.hasUnChecked(userId)
                       .flatMap(pending -> pending
                           ? eventDao.markChecked(userId).then(roleChanged(exchange))
                           : chain.filter(exchange));
    }

    /* ----------------------------------------------------------------- */

    private Mono<Void> cacheBodyAndInsert(ServerWebExchange ex,
                                          WebFilterChain chain,
                                          ServerHttpRequest req,
                                          int userId,
                                          String method) {

        return DataBufferUtils.join(req.getBody())
            .defaultIfEmpty(ex.getResponse().bufferFactory().wrap(new byte[0]))
            .flatMap(buf -> {

            	/* ---------- ① Body → String ---------- */
                byte[] bytes = new byte[buf.readableByteCount()];
                buf.read(bytes);
                DataBufferUtils.release(buf);
                String body = new String(bytes, StandardCharsets.UTF_8);

                /* ---------- ② Body 파싱 ---------- */
                JsonNode json   = parse(body);
                Long bodyUserId  = json.path("userId").asLong(userId); // fallback
                String bodyEmail    = json.path("email").asText("");
                String service  = json.path("serviceName").asText("");
                String roleName = json.path("roleNameDetail").asText("");
//                String action   = json.has("삭제") ? "DELETE" : method;

                /* ---------- ③ Body 재주입 ---------- */
                ServerHttpRequest decorated = new ServerHttpRequestDecorator(req) {
                    @Override public Flux<DataBuffer> getBody() {
                        return Flux.just(ex.getResponse()
                                           .bufferFactory().wrap(bytes));
                    }
                };
                Mono<Void> proceed = chain.filter(
                        ex.mutate().request(decorated).build());

                /* ---------- ④ 셀프 수정? → 바로 통과 ---------- */
                if (bodyUserId == userId) {
                    return proceed;
                }

                /* ---------- ⑤ 이미 INSERT 했으면 패스 ---------- */
                if (Boolean.TRUE.equals(ex.getAttribute(ATTR_EVT_INSERTED))) {
                    return proceed;
                }
                ex.getAttributes().put(ATTR_EVT_INSERTED, true);

                /* ---------- ⑥ INSERT ---------- */
                RoleChangeEvent evt = new RoleChangeEvent();
                evt.setUserId(bodyUserId);
                evt.setUserEmail(bodyEmail);
                evt.setService(service);
                evt.setRoleName(roleName);
//                evt.setAction(action);
                evt.setHttpMethod(method);
                evt.setPayload(Json.of(body));
                evt.setCreatedAt(LocalDateTime.now());

                return eventDao.save(evt).then(proceed);
            });
    }
    
    /* ───────────────────────────────────────────────────────── */
    /** 로그인 호출이 200 OK 인지 확인 후 checked_at 갱신 */
    private Mono<Void> afterLoginSucceeded(ServerWebExchange ex) {

        return Mono.defer(() -> {
            /* 1) 2xx 인지 확인 */
            HttpStatusCode sc = ex.getResponse().getStatusCode();
            if (sc == null || !sc.is2xxSuccessful()) {
                return Mono.empty();
            }

            /* 2) Set-Cookie 헤더에서 JWT 추출 */
            // 여러 개의 Set-Cookie 헤더 중 "jwt=" 로 시작하는 것을 찾는다
            String token = ex.getResponse()
                             .getHeaders()
                             .getOrEmpty("Set-Cookie")
                             .stream()
                             .filter(c -> c.startsWith("jwt="))
                             .findFirst()
                             .map(c -> {
                                 // "jwt=xxx; Path=/; ..." → xxx 부분만 추출
                                 int eq = c.indexOf('=');
                                 int scIdx = c.indexOf(';');
                                 return (eq >= 0 && scIdx > eq) ? c.substring(eq + 1, scIdx)
                                                                : null;
                             })
                             .orElse(null);

            if (token == null) {
                return Mono.empty();   // 토큰이 없으면 그대로 종료
            }

            /* 3) userId claim 꺼내기 */
            Integer uid = jwtService.getClaim(token, "userId", Integer.class);
            if (uid == null) {
                return Mono.empty();
            }

            /* 4) 미확인 권한 이벤트 checked_at 갱신 */
            return eventDao.markChecked(uid);
        });
    }

	
	private void addCorsHeaders(ServerWebExchange ex) {
	    String origin = ex.getRequest().getHeaders().getOrigin();
	    if (origin == null) return;

	    ex.getResponse().getHeaders().add("Access-Control-Allow-Origin", origin);
	    ex.getResponse().getHeaders().add("Access-Control-Allow-Credentials", "true");
	    ex.getResponse().getHeaders().add("Access-Control-Allow-Headers",
	        "Content-Type, Authorization, X-Func-Vue, X-Menu-Vue, X-Reason");
	    ex.getResponse().getHeaders().add("Access-Control-Expose-Headers", "X-Reason");
	}

    private JsonNode parse(String body) {
        try { return om.readTree(body.isEmpty() ? "{}" : body); }
        catch (Exception e) { return om.createObjectNode(); }
    }

    private Mono<Void> unauthorized(ServerWebExchange ex) {
        ex.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return ex.getResponse().setComplete();
    }
    
    // 권한이 바뀐 경우 : 401 + 식별 헤더
	private Mono<Void> roleChanged(ServerWebExchange ex) {
	    addCorsHeaders(ex);
	    ex.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
	    ex.getResponse().getHeaders().add("X-Reason", "ROLE_CHANGED");
	    return ex.getResponse().setComplete();
	}
}

