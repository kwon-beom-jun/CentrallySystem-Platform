package com.cs.core.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.hibernate.annotations.Comment;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;


/**
 * <br> TODO : Auditing (BaseTimeEntity : 등록일, 수정일)
 * <br>     보통 테이블에 등록일, 수정일, 등록자, 수정자를 모두 다 넣어주지만,
 * <br>     어떤 테이블은 등록자, 수정자를 넣지 않는 테이블도 있을 수 있으므로 BaseTimeEntity를 만들어서 구분
 * <br>     (현 프로젝트는 전부 사용)
 * <br>
 * <br> TODO 어노테이션 설명
 * <br> 	@MappedSuperclass
 * <br>     - JPA에서 사용하는 어노테이션
 * <br>     - 상속 관계에서 부모 클래스에 적용되며, 부모 클래스가 테이블과 직접 매핑되지 않고
 * <br>       부모 클래스를 상속 받는 자식 클래스에 매핑 정보만 제공
 * <br>     - 부모 클래스는 직접적인 데이터베이스 테이블과 매핑되지 않지만,
 * <br>       그 속성들은 상속받는 자식 클래스의 테이블에 포함될 수 있음
 * <br>     - 공통 매핑 정보가 필요할 때 사용하는 어노테이션
 * <br>
 * <br>     @EntityListeners
 * <br>         - Auditing을 적용
 * <br>     @CreatedDate
 * <br>         - 엔티티가 생성되어 저장될 때 시간을 자동으로 저장
 * <br>     @LastModifiedDate
 * <br>         - 엔티티의 값을 변경할 때 시간을 자동으로 저장
 * <br>
 * <br> TODO : Auditing (BaseEntity : 등록일, 수정일, 등록자, 수정자) | TestCase : MemberTest.class
 * <br>     BaseTimeEntity을 상속받아 등록일, 수정일, 등록자, 수정자를 모두 갖음
 * <br>
 * <br>
 * <br> @FilterDef와 @Filter를 부모 클래스인 AuditTimeEntity에 선언하면,
 * <br> 이 클래스를 상속받는 모든 엔티티에서 del_yn = :delYN 필터를 적용할 수 있음
 * <br>
 * <br> @Filter(name = "deletedFilter", condition = "del_yn = :delYN")를 선언하면,
 * <br> Hibernate Session에서 이 필터를 활성화할 때 delYN 값을 설정해야 함(예: 'N').
 * <br>
 * <br> -----------------------------------------------------------------------------------------
 * <br>
 * <br> ✅ Soft Delete (`delYN`) 및 Hibernate 필터 적용 방식 정리
 * <br>
 * <br> 1. Soft Delete (`delYN = 'Y'`) 적용 방식
 * <br>    - 실제로 데이터를 삭제하는 `.remove()` 또는 `removeIf(...)`를 사용하지 않음
 * <br>    - 대신 `delYN` 값을 'Y'로 변경하여 논리적 삭제 처리
 * <br>    - `updateUserPermission`, `deleteUserWithRole` 등 모든 삭제 관련 메서드에서 동일 방식 적용
 * <br>
 * <br> 2. Hibernate 필터 전역 적용
 * <br>    - AOP를 활용하여 `@Transactional` 범위 내 모든 DB 조회 시 `del_yn='N'` 조건을 자동 적용
 * <br>    - Hibernate 필터를 전역적으로 적용하기 위해 `session.enableFilter("deletedFilter").setParameter("delYN", "N")` 사용
 * <br>    - 주의: 이미 영속성 컨텍스트(Persistence Context)에 로드된 엔티티에는 필터가 적용되지 않을 수 있음
 * <br>      → 해결 방법:
 * <br>      (a) Lazy Loading(지연 로딩) 설정하여 필터 적용 시점에 쿼리 실행
 * <br>      (b) `.stream().filter(ur -> "N".equals(ur.getDelYN()))` 와 같은 추가 필터링 적용
 * <br>
 * <br> 3. Hibernate 필터 적용 방식(AOP + @Transactional)
 * <br>    - `@Transactional`이 붙은 서비스 메서드가 호출될 때 스프링이 트랜잭션 프록시를 생성하고 Hibernate Session을 연결
 * <br>    - 트랜잭션 시작 시점(`@Around("@annotation(Transactional)")`)에서 `session.enableFilter("deletedFilter").setParameter("delYN", "N")` 적용
 * <br>    - 트랜잭션이 종료될 때까지 해당 필터 유지
 * <br>
 * <br> ✅ 장점
 * <br>    - 모든 트랜잭션에서 자동으로 `del_yn = 'N'` 필터 적용
 * <br>    - 별도의 조건을 추가하지 않아도 soft delete 적용 가능
 * <br>
 * <br> ⚠️ 단점
 * <br>    - 복잡한 로드 타이밍(예: 이미 영속성 컨텍스트가 존재하던 시점)에서는 부분적으로 로드된 엔티티가 있을 수 있음
 * <br>    - 트랜잭션 시작 전에 로드된 엔티티는 필터 적용이 되지 않을 수도 있음 → Lazy Loading 권장
 * <br>
 * <br> 4. 트랜잭션과 동시 실행 시 고려 사항
 * <br>    - Hibernate Session이 새로 생성될 때마다 필터 설정이 초기화됨
 * <br>    - AOP로 트랜잭션 진입 시 enableFilter를 적용하면 해당 트랜잭션에서는 필터가 적용됨
 * <br>    - 하지만 다른 트랜잭션(=다른 스레드)에서는 필터가 적용되지 않으므로 새 트랜잭션마다 enableFilter 호출 필요
 * <br>    - 동시성 문제라기보다는, 각 세션마다 필터를 개별적으로 설정해야 하는 Hibernate 구조 때문
 * <br>
 * <br> 5. @Where(clause = "del_yn = 'N'") 주의 사항
 * <br>    - Spring Boot 6.3 이상에서는 `@Where` 사용이 제거됨
 * <br>    - Hibernate Filter(`@FilterDef` + `@Filter`)를 활용하여 `del_yn`을 관리하는 방식으로 변경 필요
 * <br>
 * <br> -----------------------------------------------------------------------------------------
 * <br>
 * <br> ⚠️ delYN 사용 실패함
 * <br>
 * <br>		- 전체적으로 소프트(논리) 삭제를 적용해 보려고 했으나 실패
 * <br>
 * <br> 	//delYN 필터 정의 (이름: deletedFilter, 파라미터: delYN)
 * <br> 	@FilterDef(
 * <br> 	    	name = "deletedFilter",
 * <br> 	    	parameters = @ParamDef(name = "delYN", type = String.class) 
 * <br> 		)
 * <br> 	//필터 적용: del_yn = :delYN
 * <br> 	@Filter(name = "deletedFilter", condition = "Del_YN = :delYN")
 * <br> 
 * <br> 	1. 기본키 등 키에 대한 설계
 * <br> 		- 논리 삭제 후 조회 했을때는 없다 나오지만 추가하려하면 해당 데이터가 있어서 키에 대한 오류 발생
 * <br> 	  	  ( 예를 들어 회원 탈퇴 후 다시 회원 가입 시 오류 발생 ) 
 * <br> 	2. 설계 관련
 * <br>		 	- 모든 테이블마다 CRUD 실행 시 Del_YN 값을 확인해야함(시간적 제한)
 * <br> 	3. 로그인 관련
 * <br> 		- 로그인시 시큐리티에서 DB 검사하는 자체 스프링 로직에서 커스텀으로 수정해야 하는것으로 보임 
 * <br> 
 * <br> 	해서 현실적으로 아직 내 실력으론(+시간포함) 설정이 힘들다고 판단하여 필요하다면 필요 부분만 논리 삭제 적용할 예정
 * <br>  
 */
@Getter
@Setter
@EntityListeners(value = {AuditingEntityListener.class})
@MappedSuperclass
public abstract class AuditTimeEntity {

    // updatable = false
    //  - 특정 엔티티 필드나 컬럼이 데이터베이스에 한 번 저장된 후에는 수정이 불가능하게 설정하는 역할
    @CreatedDate
    @Column(updatable = false)
    @Comment("생성 시간")
    private LocalDateTime regTime;

    @LastModifiedDate
    @Comment("수정 시간")
    private LocalDateTime updateTime;

    @CreatedBy
    @Column(updatable = false)
    @Comment("생성자")
    private String createdBy;

    @LastModifiedBy
    @Comment("수정자")
    private String modifiedBy;
    
//    @Column(name = "Del_YN", length = 1, nullable = false)
//    @ColumnDefault("'N'")  // DB 테이블 기본값 설정
//    private String delYN = "N";  // 기본값을 "N"으로 설정

}
