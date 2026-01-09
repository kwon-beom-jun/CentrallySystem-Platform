package com.cs.hrm.repository;

import com.cs.hrm.entity.HrmEmploymentType;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HrmEmploymentTypeRepository extends JpaRepository<HrmEmploymentType, Integer> {
	
}
