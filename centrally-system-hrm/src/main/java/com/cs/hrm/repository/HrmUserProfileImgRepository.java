package com.cs.hrm.repository;

import com.cs.hrm.entity.HrmUserProfileImg;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HrmUserProfileImgRepository extends JpaRepository<HrmUserProfileImg, Integer> {
	Optional<HrmUserProfileImg> findByFileUrl(String fileUrl);
	Optional<HrmUserProfileImg> findByFileName(String fileName);
} 