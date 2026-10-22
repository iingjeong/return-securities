package com.app.returns.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 증권사 내부 고객 마스터. RIA의 customer 테이블과는 CI 값으로만 연결한다. */
@Entity
@Table(name = "general_customer")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GeneralCustomerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "general_customer_id")
    private Long generalCustomerId;

    @Column(name = "ci_hash", nullable = false, length = 64, unique = true)
    private String ciHash;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;
}
