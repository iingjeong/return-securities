package com.app.returns.domain.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class DomesticTradeRequestDTO {

    @NotNull(message = "ciHash는 필수입니다.")
    private String ciHash;
}
