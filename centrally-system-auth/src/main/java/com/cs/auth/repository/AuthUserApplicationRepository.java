package com.cs.auth.repository;

import com.cs.auth.entity.AuthUserApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AuthUserApplicationRepository extends JpaRepository<AuthUserApplication, Long> {

    /**
     * 특정 유저와 특정 어플리케이션의 매핑이 존재하는지 확인
     */
    Optional<AuthUserApplication> findByUserUserIdAndApplicationApplicationId(Integer userId, Integer applicationId);

}
