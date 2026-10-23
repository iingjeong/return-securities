package com.app.returns.domain.dto;

import java.time.LocalDate;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class GeneralCustomerDTO {
    private Long generalCustomerId;
    private String ciHash;
    private String name;
    private LocalDate birthDate;
}
