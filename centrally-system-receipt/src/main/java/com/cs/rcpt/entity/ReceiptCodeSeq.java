package com.cs.rcpt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "receipt_code_seq")
@Getter @Setter
@NoArgsConstructor
public class ReceiptCodeSeq {

    @Id
    @Comment("년월")
    private String yymm;      // ‘2506’

    @Column(name = "last_no")
    @Comment("마지막 번호")
    private int lastNo;       // 0, 1, 2 …

    public int next() { return ++lastNo; }
}
