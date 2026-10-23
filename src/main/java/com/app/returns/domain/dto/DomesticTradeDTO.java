package com.app.returns.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class DomesticTradeDTO {
    private Long tradeId;
    private Long generalCustomerId;
    private String tradeType;
    private String stockCode;
    private BigDecimal qty;
    private BigDecimal price;
    private LocalDateTime executedAt;
}
