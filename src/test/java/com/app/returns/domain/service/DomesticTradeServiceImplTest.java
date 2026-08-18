package com.app.returns.domain.service;

import com.app.returns.domain.dto.DomesticTradeDTO;
import com.app.returns.domain.dto.request.DomesticTradeRequestDTO;
import com.app.returns.domain.dto.response.DomesticTradeResponseDTO;
import com.app.returns.domain.mapper.DomesticTradeMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DomesticTradeServiceImplTest {

    @Mock
    private DomesticTradeMapper domesticTradeMapper;

    @InjectMocks
    private DomesticTradeServiceImpl domesticTradeService;

    @Test
    void findByCiHashReturnsAllTradesMappedToResponseDTOs() {
        DomesticTradeRequestDTO request = DomesticTradeRequestDTO.builder()
                .ciHash("ci-hash-1")
                .build();

        DomesticTradeDTO trade1 = DomesticTradeDTO.builder()
                .tradeId(1L)
                .generalCustomerId(10L)
                .tradeType("BUY")
                .stockCode("005930")
                .qty(BigDecimal.valueOf(10))
                .price(BigDecimal.valueOf(70000))
                .executedAt(LocalDateTime.of(2026, 3, 5, 9, 0))
                .build();
        DomesticTradeDTO trade2 = DomesticTradeDTO.builder()
                .tradeId(2L)
                .generalCustomerId(10L)
                .tradeType("SELL")
                .stockCode("005930")
                .qty(BigDecimal.valueOf(5))
                .price(BigDecimal.valueOf(72000))
                .executedAt(LocalDateTime.of(2026, 3, 10, 9, 0))
                .build();
        when(domesticTradeMapper.selectByCiHash(request)).thenReturn(List.of(trade1, trade2));

        List<DomesticTradeResponseDTO> result = domesticTradeService.findByCiHash(request);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getStockCode()).isEqualTo("005930");
        assertThat(result.get(0).getTradeType()).isEqualTo("BUY");
        assertThat(result.get(1).getTradeType()).isEqualTo("SELL");
    }

    @Test
    void findByCiHashReturnsEmptyListWhenNoTradesExist() {
        DomesticTradeRequestDTO request = DomesticTradeRequestDTO.builder()
                .ciHash("ci-hash-1")
                .build();
        when(domesticTradeMapper.selectByCiHash(request)).thenReturn(List.of());

        List<DomesticTradeResponseDTO> result = domesticTradeService.findByCiHash(request);

        assertThat(result).isEmpty();
    }
}
