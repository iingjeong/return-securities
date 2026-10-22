package com.app.returns.domain.entity;

import com.app.returns.domain.generalaccount.type.GeneralAccountType;
import com.app.returns.domain.generalaccount.type.GeneralStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "general_account")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GeneralAccountEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "general_account_id")
    private Long generalAccountId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "general_customer_id", nullable = false)
    private GeneralCustomerEntity generalCustomer;

    @Column(name = "account_no", nullable = false, length = 10)
    private String accountNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 30)
    private GeneralAccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private GeneralStatus status;
}
