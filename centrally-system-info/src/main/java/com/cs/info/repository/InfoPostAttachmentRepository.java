package com.cs.info.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cs.info.entity.InfoPostAttachment;

/**
 * 첨부파일 레포지토리
 */
@Repository
public interface InfoPostAttachmentRepository extends JpaRepository<InfoPostAttachment, Long> {
    
    /**
     * 저장된 파일명으로 첨부파일 조회
     */
    Optional<InfoPostAttachment> findByFileOriginName(String fileOriginName);
}

