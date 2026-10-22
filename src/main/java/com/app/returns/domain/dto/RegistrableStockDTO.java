package com.app.returns.domain.dto;

import com.app.returns.domain.generalaccount.type.GeneralAccountType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class RegistrableStockDTO {
    private Long registrableStockId;
    private Long generalAccountId;
    private String accountNo;
    private GeneralAccountType accountType;
    private Long foreignProductId;
    private BigDecimal heldQty;
    private String sourceBroker;
    private LocalDateTime recordedAt;
    private LocalDateTime purchaseDate;
    private BigDecimal purchasePrice;
    private String purchaseCurrency;
    private BigDecimal purchaseFxRate;
}
