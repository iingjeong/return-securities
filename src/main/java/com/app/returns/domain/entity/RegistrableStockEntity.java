package com.app.returns.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 기준일 적격 원본 조회용 매핑. heldQty는 현재 일반계좌 잔고가 아니다. */
@Entity
@Table(name = "registrable_stock")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RegistrableStockEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "registrable_stock_id")
    private Long registrableStockId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "general_account_id", nullable = false)
    private GeneralAccountEntity generalAccount;

    @Column(name = "foreign_product_id", nullable = false)
    private Long foreignProductId;

    @Column(name = "held_qty", nullable = false, precision = 15, scale = 4)
    private BigDecimal heldQty;

    @Column(name = "source_broker", length = 20)
    private String sourceBroker;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Column(name = "purchase_date", nullable = false)
    private LocalDateTime purchaseDate;

    @Column(name = "purchase_price", nullable = false, precision = 15, scale = 4)
    private BigDecimal purchasePrice;

    @Column(name = "purchase_currency", nullable = false, length = 10)
    private String purchaseCurrency;

    @Column(name = "purchase_fx_rate", nullable = false, precision = 15, scale = 4)
    private BigDecimal purchaseFxRate;
}
