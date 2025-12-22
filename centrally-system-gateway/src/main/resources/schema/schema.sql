
/* ─────────────────────────────────────────────
   1. 사용자 활동 로그  (변경 없음)
   ───────────────────────────────────────────── */
CREATE TABLE IF NOT EXISTS gateway_user_activity_log (
    activity_id  BIGSERIAL   PRIMARY KEY,
    username     VARCHAR(255),
    user_email   VARCHAR(255),
    menu         VARCHAR(255),
    function     VARCHAR(255),
    ip           VARCHAR(50),
    http_method  VARCHAR(10),
    http_body    VARCHAR(2000),
    browser      VARCHAR(50),
    os           VARCHAR(50),
    timestamp    TIMESTAMP   NOT NULL,
    del_yn       VARCHAR(1)
);

/* ─────────────────────────────────────────────
   2. 실시간 권한 변동 이벤트  (★ user_email 사용)
   ───────────────────────────────────────────── */
CREATE TABLE IF NOT EXISTS gateway_role_change_event (
    event_id    BIGSERIAL   PRIMARY KEY,
    user_id     INT         NOT NULL,
    user_email   VARCHAR(255) NOT NULL,
    service     VARCHAR(50)  NOT NULL,
    role_name   VARCHAR(100) NOT NULL,
    http_method VARCHAR(10)  NOT NULL,
    payload     JSONB        NOT NULL,     -- 그대로 jsonb
    created_at  TIMESTAMP    NOT NULL DEFAULT now(),
    checked_at  TIMESTAMP
);



