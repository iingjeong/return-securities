package com.app.returns.domain.dto.response;

import com.app.returns.domain.dto.RegistrableStockDTO;
import com.app.returns.domain.generalaccount.type.GeneralAccountType;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter @ToString @Builder
public class RegistrableStockResponseDTO {

    private Long registrableStockId;
    private Long foreignProductId;
    private Long generalAccountId;
    private String accountNo;
    private GeneralAccountType accountType;
    private BigDecimal heldQty;
    private String sourceBroker;
    private LocalDateTime purchaseDate;
    private BigDecimal purchasePrice;
    private String purchaseCurrency;
    private BigDecimal purchaseFxRate;

    public RegistrableStockResponseDTO(RegistrableStockDTO dto) {
        this.registrableStockId = dto.getRegistrableStockId();
        this.foreignProductId = dto.getForeignProductId();
        this.generalAccountId = dto.getGeneralAccountId();
        this.accountNo = dto.getAccountNo();
        this.accountType = dto.getAccountType();
        this.heldQty = dto.getHeldQty();
        this.sourceBroker = dto.getSourceBroker();
        this.purchaseDate = dto.getPurchaseDate();
        this.purchasePrice = dto.getPurchasePrice();
        this.purchaseCurrency = dto.getPurchaseCurrency();
        this.purchaseFxRate = dto.getPurchaseFxRate();
    }
}
