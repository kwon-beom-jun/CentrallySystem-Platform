package com.cs.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.cs.auth.entity.AuthSocialLinkToken;
import java.util.Optional;

public interface AuthSocialLinkTokenRepository extends JpaRepository<AuthSocialLinkToken, Long> {

    // tokenValue 로 DB에서 찾기
    Optional<AuthSocialLinkToken> findByTokenValue(String tokenValue);

}
