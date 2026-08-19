package com.app.returns.domain.dto.response;

import com.app.returns.domain.dto.DomesticTradeDTO;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class DomesticTradeResponseDTO {
    private String tradeType;
    private String stockCode;
    private BigDecimal qty;
    private BigDecimal price;
    private LocalDateTime executedAt;

    public DomesticTradeResponseDTO(DomesticTradeDTO dto) {
        this.tradeType = dto.getTradeType();
        this.stockCode = dto.getStockCode();
        this.qty = dto.getQty();
        this.price = dto.getPrice();
        this.executedAt = dto.getExecutedAt();
    }
}
