//package com.cs.core.entity;
//
//import lombok.Getter;
//import org.springframework.data.annotation.CreatedBy;
//import org.springframework.data.annotation.LastModifiedBy;
//import org.springframework.data.jpa.domain.support.AuditingEntityListener;
//
//import jakarta.persistence.Column;
//import jakarta.persistence.EntityListeners;
//import jakarta.persistence.MappedSuperclass;
//
//
///**
// * <br> TODO : Auditing (BaseEntity : 등록일, 수정일, 등록자, 수정자) | TestCase : MemberTest.class
// * <br>     BaseTimeEntity을 상속받아 등록일, 수정일, 등록자, 수정자를 모두 갖음
// */
//@EntityListeners(value = {AuditingEntityListener.class})
//@MappedSuperclass
//@Getter
//public abstract class AuditEntity extends AuditTimeEntity {
//
//    @CreatedBy
//    @Column(updatable = false)
//    private String createdBy;
//
//    @LastModifiedBy
//    private String modifiedBy;
//
//}
