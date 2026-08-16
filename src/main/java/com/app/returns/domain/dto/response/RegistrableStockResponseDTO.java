package com.app.returns.domain.dto.response;

import com.app.returns.domain.dto.RegistrableStockDTO;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter @ToString @Builder
public class RegistrableStockResponseDTO {

    private Long generalAccountId;
    private BigDecimal heldQty;
    private String sourceBroker;
    private LocalDateTime purchaseDate;
    private BigDecimal purchasePrice;
    private String purchaseCurrency;
    private BigDecimal purchaseFxRate;

    public RegistrableStockResponseDTO(RegistrableStockDTO dto) {
        this.generalAccountId = dto.getGeneralAccountId();
        this.heldQty = dto.getHeldQty();
        this.sourceBroker = dto.getSourceBroker();
        this.purchaseDate = dto.getPurchaseDate();
        this.purchasePrice = dto.getPurchasePrice();
        this.purchaseCurrency = dto.getPurchaseCurrency();
        this.purchaseFxRate = dto.getPurchaseFxRate();
    }
}
