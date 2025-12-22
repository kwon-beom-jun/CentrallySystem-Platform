package com.cs.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cs.info.entity.InfoPostAttachment;

/**
 * 첨부파일 레포지토리
 */
@Repository
public interface InfoPostAttachmentRepository extends JpaRepository<InfoPostAttachment, Long> {
}

