package com.cs.hrm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cs.hrm.controller.response.HrmUserBasicDto;
import com.cs.hrm.controller.response.HrmUserIdNameView;
import com.cs.hrm.controller.response.HrmUserWithRolesDto;
import com.cs.hrm.controller.response.HrmUserWithRolesView;
import com.cs.hrm.entity.HrmUser;
import com.cs.hrm.enums.StyleCategory;
import com.cs.hrm.enums.MainCardStyle;

@Repository
public interface HrmUserRepository extends JpaRepository<HrmUser, Integer> {
	
	Optional<HrmUser> findByEmail(String email);

	/**
	 * [비활성 미포함] ID 목록으로 사용자 기본 정보 조회
	 */
	@Query("""
	           SELECT
	               u.userId AS userId,
	               u.name AS name,
	               p.fileUrl AS profileImgUrl,
	               COALESCE(d.departmentName, '') AS departmentName,
	               COALESCE(t.teamName, '') AS teamName,
	               COALESCE(pos.positionName, '') AS positionName,
	               COALESCE(
	                   (SELECT s.styleCode 
	                    FROM HrmUserStyle us 
	                    JOIN us.style s 
	                    WHERE us.userId = u.userId 
	                    AND us.styleCategory = 'MAIN_CARD'),
	                   'default'
	               ) AS cardStyle
	           FROM HrmUser u
	           LEFT JOIN u.profileImg p
	           LEFT JOIN u.team t
	           LEFT JOIN t.department d
	           LEFT JOIN u.position pos
	           WHERE u.userId IN :ids
	           ORDER BY u.name ASC
	           """)
	List<HrmUserIdNameView> getUsersByIds(@Param("ids") List<Integer> ids);

	/**
	 * [비활성 포함] ID 목록으로 사용자 기본 정보 조회 (네이티브 쿼리 사용)
	 * - 'MAIN_CARD': StyleCategory.MAIN_CARD
	 * - 'default': MainCardStyle.DEFAULT
	 */
	@Query(
		value = """
			SELECT
				u.user_id   AS userId,
				u.name      AS name,
				p.file_url  AS profileImgUrl,
				COALESCE(d.department_name,'') AS departmentName,
				COALESCE(t.team_name,      '') AS teamName,
				COALESCE(pos.position_name,'') AS positionName,
				COALESCE(
					(SELECT s.style_code 
					 FROM hrm_user_styles us 
					 JOIN hrm_styles s ON us.style_id = s.style_id
					 WHERE us.user_id = u.user_id 
					 AND us.style_category = 'MAIN_CARD'), 
					'default'
				) AS cardStyle
			FROM hrm_users u
			LEFT JOIN hrm_user_profile_img p ON u.profile_img_id = p.profile_img_id
			LEFT JOIN hrm_teams t            ON u.team_id       = t.team_id
			LEFT JOIN hrm_departments d      ON t.department_id = d.department_id
			LEFT JOIN hrm_positions pos      ON u.position_id   = pos.position_id
			WHERE u.user_id IN (:ids)
			ORDER BY u.name ASC
		""",
		nativeQuery = true) 
	List<HrmUserIdNameView> getUsersByIdsIncludingDisabled(@Param("ids") List<Integer> ids);

	/**
	 * [비활성 포함] ID 목록으로 사용자 기본 정보와 개별 권한을 조회 (그룹화 X)
	 */
	@Query(
	    value = """
	        SELECT
	            u.user_id AS userId,
	            u.name,
                r.role_name AS roles
	        FROM
	            hrm_users u
	        LEFT JOIN hrm_user_roles ur ON u.user_id = ur.user_id
	        LEFT JOIN hrm_roles r ON ur.role_id = r.role_id
	        WHERE
	            u.user_id IN (:ids)
	        ORDER BY
	            u.name ASC, roles ASC
	    """,
	    nativeQuery = true)
	List<HrmUserWithRolesView> findUsersWithRolesByIdsIncludingDisabled(@Param("ids") List<Integer> ids);
	
	/**
	 * [활성 사용자만] ID 목록으로 사용자 기본 정보와 개별 권한을 조회 (JPQL)
	 */
	@Query("""
	    SELECT new com.cs.hrm.controller.response.HrmUserWithRolesDto(
	        u.userId,
	        u.name,
            r.roleName AS roles
	    )
	    FROM HrmUser u
	    LEFT JOIN u.userRoles ur
	    LEFT JOIN ur.role r
	    WHERE u.userId IN :ids
	      AND u.enabled = true
	    ORDER BY u.name ASC, r.roleNameDetail ASC
	""")
	List<HrmUserWithRolesDto> findActiveUsersWithRolesByIds(@Param("ids") List<Integer> ids);
    
    /* 전체 간단 목록 */
    @Query("""
           SELECT new com.cs.hrm.controller.response.HrmUserBasicDto(
               u.userId,
               u.name,
               u.email,
               COALESCE(d.departmentName, '미지정'),
               COALESCE(t.teamName,       '미지정')
           )
           FROM HrmUser u
           LEFT JOIN u.team        t
           LEFT JOIN t.department  d
           ORDER BY u.name ASC, u.email ASC
           """)
    List<HrmUserBasicDto> findAllBasic();

    /* 특정 ID 목록만 간단 조회 */
    @Query("""
           SELECT new com.cs.hrm.controller.response.HrmUserBasicDto(
               u.userId,
               u.name,
               u.email,
               COALESCE(d.departmentName, '미지정'),
               COALESCE(t.teamName,       '미지정')
           )
           FROM HrmUser u
           LEFT JOIN u.team        t
           LEFT JOIN t.department  d
           WHERE u.userId IN :ids
           ORDER BY u.name ASC, u.email ASC
           """)
    List<HrmUserBasicDto> findBasicByIds(List<Integer> ids);
    
    

    /* 활성/비활성 구분 없이 모두 가져오기  */
    @Query(
        value = """
            SELECT *
              FROM hrm_users
          ORDER BY name ASC, email ASC
            """,
        nativeQuery = true)
    List<HrmUser> findAllIncludingDisabled();

    
    /* 비활성 사용자도 단건 조회  */
    @Query(
	    value = "SELECT * FROM hrm_users WHERE user_id = :id",
	    nativeQuery = true)
	Optional<HrmUser> findByIdIncludingDisabled(@Param("id") Integer id);
    
    
		// COALESCE(d.departmentName,'미지정'),
		// COALESCE(t.teamName,      '미지정')
    /* 실시간 검색 – Pageable 로 LIMIT 처리 */
    @Query("""
		SELECT new com.cs.hrm.controller.response.HrmUserBasicDto(
		        u.userId ,
		        u.name   ,
		        u.email  ,
		        COALESCE(d.departmentName,''),
		        COALESCE(t.teamName,      '')
		)
		FROM HrmUser u
		LEFT JOIN u.team            t
		LEFT JOIN t.department      d
		LEFT JOIN u.userRoles       ur
		LEFT JOIN ur.role           r
		WHERE ( LOWER(u.name)  LIKE LOWER(:kw)
		     OR LOWER(u.email) LIKE LOWER(:kw) )
		  AND ( :includeDisabled = TRUE OR u.enabled = TRUE )
		  AND ( :service IS NULL
		        OR r.serviceName = :service )
		  AND ( :roles   IS NULL
		        OR r.roleName        IN :roles
		        OR r.roleNameDetail  IN :roles )
		GROUP BY u.userId, u.name, u.email, d.departmentName, t.teamName
		ORDER BY u.name ASC , u.email ASC
	""")
	List<HrmUserBasicDto> searchUsers(@Param("kw")              String kw,
	                                  @Param("includeDisabled") boolean includeDisabled,
	                                  @Param("service")         String service,
	                                  @Param("roles")           List<String> roles,
	                                  Pageable                  pageable);



    /** 특정 팀을 참조하던 사용자들의 team FK 를 NULL 로 끊는다 */
    @Modifying(clearAutomatically = true)   // flush + 1차 캐시 정리까지 자동
    @Query("""
        UPDATE HrmUser u
           SET u.team = NULL
         WHERE u.team.teamId = :tid
    """)
    void detachTeam(@Param("tid") Integer teamId);

    /* 메일 발송용: 전체 활성 사용자 이메일 */
    @Query(value = "SELECT email FROM hrm_users WHERE enabled = true AND email IS NOT NULL", nativeQuery = true)
    List<String> findAllActiveEmails();

    /* 메일 발송용: 부서 ID 목록으로 활성 사용자 이메일 */
    @Query(value = """
        SELECT u.email
          FROM hrm_users u
          LEFT JOIN hrm_teams t ON u.team_id = t.team_id
         WHERE u.enabled = true
           AND u.email IS NOT NULL
           AND t.department_id IN (:deptIds)
    """, nativeQuery = true)
    List<String> findActiveEmailsByDepartmentIds(@Param("deptIds") List<Integer> departmentIds);

    /* 메일 발송용: 팀 ID 목록으로 활성 사용자 이메일 */
    @Query(value = """
        SELECT u.email
          FROM hrm_users u
         WHERE u.enabled = true
           AND u.email IS NOT NULL
           AND u.team_id IN (:teamIds)
    """, nativeQuery = true)
    List<String> findActiveEmailsByTeamIds(@Param("teamIds") List<Integer> teamIds);

    /* 메일 발송용: 고용형태별 필터 (정규직/비정규직) */
    @Query(value = """
        SELECT u.email
          FROM hrm_users u
         WHERE u.enabled = true
           AND u.email IS NOT NULL
           AND (
                 (:includeRegular = true  AND u.employment_type_id = 1)
              OR (:includeNonregular = true AND u.employment_type_id = 2)
           )
    """, nativeQuery = true)
    List<String> findActiveEmailsByEmploymentFlags(@Param("includeRegular") boolean includeRegular,
                                                   @Param("includeNonregular") boolean includeNonregular);

    /* 메일 발송용: 부서 ID + 고용형태별 필터 */
    @Query(value = """
        SELECT u.email
          FROM hrm_users u
          LEFT JOIN hrm_teams t ON u.team_id = t.team_id
         WHERE u.enabled = true
           AND u.email IS NOT NULL
           AND t.department_id IN (:deptIds)
           AND (
                 (:includeRegular = true  AND u.employment_type_id = 1)
              OR (:includeNonregular = true AND u.employment_type_id = 2)
           )
    """, nativeQuery = true)
    List<String> findActiveEmailsByDepartmentIdsAndEmploymentFlags(@Param("deptIds") List<Integer> departmentIds,
                                                                   @Param("includeRegular") boolean includeRegular,
                                                                   @Param("includeNonregular") boolean includeNonregular);

    /* 메일 발송용: 팀 ID + 고용형태별 필터 */
    @Query(value = """
        SELECT u.email
          FROM hrm_users u
         WHERE u.enabled = true
           AND u.email IS NOT NULL
           AND u.team_id IN (:teamIds)
           AND (
                 (:includeRegular = true  AND u.employment_type_id = 1)
              OR (:includeNonregular = true AND u.employment_type_id = 2)
           )
    """, nativeQuery = true)
    List<String> findActiveEmailsByTeamIdsAndEmploymentFlags(@Param("teamIds") List<Integer> teamIds,
                                                             @Param("includeRegular") boolean includeRegular,
                                                             @Param("includeNonregular") boolean includeNonregular);

    /* ===== INFO 서비스용 역할 기반 메일 조회 쿼리 ===== */

    /**
     * 역할 기반 전체 이메일 조회
     */
    @Query(value = """
        SELECT DISTINCT u.email
          FROM hrm_users u
          INNER JOIN hrm_user_roles ur ON u.user_id = ur.user_id
          INNER JOIN hrm_roles r ON ur.role_id = r.role_id
         WHERE u.enabled = true
           AND u.email IS NOT NULL
           AND r.role_name IN (:roles)
    """, nativeQuery = true)
    List<String> findActiveEmailsByRoles(@Param("roles") List<String> roles);

    /**
     * 역할 기반 이메일 조회 (부서 필터)
     */
    @Query(value = """
        SELECT DISTINCT u.email
          FROM hrm_users u
          INNER JOIN hrm_user_roles ur ON u.user_id = ur.user_id
          INNER JOIN hrm_roles r ON ur.role_id = r.role_id
          LEFT JOIN hrm_teams t ON u.team_id = t.team_id
         WHERE u.enabled = true
           AND u.email IS NOT NULL
           AND r.role_name IN (:roles)
           AND t.department_id IN (:deptIds)
    """, nativeQuery = true)
    List<String> findActiveEmailsByRolesAndDepartments(
        @Param("roles") List<String> roles,
        @Param("deptIds") List<Integer> departmentIds
    );
    
    /**
     * 역할 기반 이메일 조회 (팀 필터)
     */
    @Query(value = """
        SELECT DISTINCT u.email
          FROM hrm_users u
          INNER JOIN hrm_user_roles ur ON u.user_id = ur.user_id
          INNER JOIN hrm_roles r ON ur.role_id = r.role_id
         WHERE u.enabled = true
           AND u.email IS NOT NULL
           AND r.role_name IN (:roles)
           AND u.team_id IN (:teamIds)
    """, nativeQuery = true)
    List<String> findActiveEmailsByRolesAndTeams(
        @Param("roles") List<String> roles,
        @Param("teamIds") List<Integer> teamIds
    );

}
