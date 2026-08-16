package com.app.returns.domain.service;

import com.app.returns.domain.dto.RegistrableStockDTO;
import com.app.returns.domain.dto.request.RegistrableStockRequestDTO;
import com.app.returns.domain.dto.response.RegistrableStockResponseDTO;
import com.app.returns.domain.exception.RegistrableStockNotFoundException;
import com.app.returns.domain.mapper.RegistrableStockMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrableStockServiceImplTest {

    @Mock
    private RegistrableStockMapper registrableStockMapper;

    @InjectMocks
    private RegistrableStockServiceImpl registrableStockService;

    @Test
    void findHeldQtyReturnsResponseWhenStockExists() {
        RegistrableStockRequestDTO request = RegistrableStockRequestDTO.builder()
                .ciHash("ci-hash-1")
                .foreignProductId(1L)
                .build();

        RegistrableStockDTO dto = RegistrableStockDTO.builder()
                .registrableStockId(1L)
                .generalAccountId(1L)
                .foreignProductId(1L)
                .heldQty(BigDecimal.valueOf(100))
                .sourceBroker(null)
                .recordedAt(LocalDateTime.now())
                .purchaseDate(LocalDateTime.now())
                .purchasePrice(BigDecimal.valueOf(150.25))
                .purchaseCurrency("USD")
                .purchaseFxRate(BigDecimal.valueOf(1320.5))
                .build();
        when(registrableStockMapper.findHeldQty(request)).thenReturn(Optional.of(dto));

        RegistrableStockResponseDTO result = registrableStockService.findHeldQty(request);

        assertThat(result.getGeneralAccountId()).isEqualTo(1L);
        assertThat(result.getHeldQty()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(result.getPurchaseCurrency()).isEqualTo("USD");
    }

    @Test
    void findHeldQtyThrowsNotFoundExceptionWhenStockDoesNotExist() {
        RegistrableStockRequestDTO request = RegistrableStockRequestDTO.builder()
                .ciHash("ci-hash-1")
                .foreignProductId(1L)
                .build();

        when(registrableStockMapper.findHeldQty(request)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registrableStockService.findHeldQty(request))
                .isInstanceOf(RegistrableStockNotFoundException.class)
                .hasMessage("등록가능 보유수량 조회 실패");
    }

    @Test
    void findLotsReturnsAllLotsMappedToResponseDTOs() {
        RegistrableStockRequestDTO request = RegistrableStockRequestDTO.builder()
                .ciHash("ci-hash-1")
                .foreignProductId(1L)
                .build();

        RegistrableStockDTO lot1 = RegistrableStockDTO.builder()
                .registrableStockId(1L)
                .generalAccountId(10L)
                .foreignProductId(1L)
                .heldQty(BigDecimal.valueOf(40))
                .purchaseDate(LocalDateTime.of(2026, 1, 5, 9, 0))
                .purchasePrice(BigDecimal.valueOf(150.25))
                .purchaseCurrency("USD")
                .purchaseFxRate(BigDecimal.valueOf(1320.5))
                .build();
        RegistrableStockDTO lot2 = RegistrableStockDTO.builder()
                .registrableStockId(2L)
                .generalAccountId(20L)
                .foreignProductId(1L)
                .heldQty(BigDecimal.valueOf(60))
                .purchaseDate(LocalDateTime.of(2026, 3, 10, 9, 0))
                .purchasePrice(BigDecimal.valueOf(180))
                .purchaseCurrency("USD")
                .purchaseFxRate(BigDecimal.valueOf(1330))
                .build();
        when(registrableStockMapper.findLotsByCiHashAndProduct(request))
                .thenReturn(List.of(lot1, lot2));

        List<RegistrableStockResponseDTO> result = registrableStockService.findLots(request);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getGeneralAccountId()).isEqualTo(10L);
        assertThat(result.get(0).getHeldQty()).isEqualByComparingTo(BigDecimal.valueOf(40));
        assertThat(result.get(1).getGeneralAccountId()).isEqualTo(20L);
        assertThat(result.get(1).getHeldQty()).isEqualByComparingTo(BigDecimal.valueOf(60));
    }

    @Test
    void findLotsReturnsEmptyListWhenNoLotsExist() {
        RegistrableStockRequestDTO request = RegistrableStockRequestDTO.builder()
                .ciHash("ci-hash-1")
                .foreignProductId(1L)
                .build();
        when(registrableStockMapper.findLotsByCiHashAndProduct(request)).thenReturn(List.of());

        List<RegistrableStockResponseDTO> result = registrableStockService.findLots(request);

        assertThat(result).isEmpty();
    }
}