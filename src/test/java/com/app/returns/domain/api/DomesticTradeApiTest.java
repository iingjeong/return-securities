package com.app.returns.domain.api;

import com.app.returns.domain.dto.response.DomesticTradeResponseDTO;
import com.app.returns.domain.service.DomesticTradeService;
import com.app.returns.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DomesticTradeApiTest {

    private DomesticTradeService domesticTradeService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        domesticTradeService = mock(DomesticTradeService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new DomesticTradeApi(domesticTradeService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getDomesticTradesAcceptsValidRequest() throws Exception {
        DomesticTradeResponseDTO trade = DomesticTradeResponseDTO.builder()
                .tradeType("BUY")
                .stockCode("005930")
                .qty(BigDecimal.valueOf(10))
                .price(BigDecimal.valueOf(70000))
                .executedAt(LocalDateTime.now())
                .build();
        when(domesticTradeService.findByCiHash(any())).thenReturn(List.of(trade));

        mockMvc.perform(get("/api/domestic-trades")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("ciHash", "hash-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("조회 성공"))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].stockCode").value("005930"));

        verify(domesticTradeService).findByCiHash(any());
    }

    @Test
    void getDomesticTradesRejectsMissingCiHash() throws Exception {
        mockMvc.perform(get("/api/domestic-trades")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ciHash는 필수입니다."));

        verify(domesticTradeService, never()).findByCiHash(any());
    }

    @Test
    void getDomesticTradesReturnsEmptyListWhenNoTradesExist() throws Exception {
        when(domesticTradeService.findByCiHash(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/domestic-trades")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("ciHash", "hash-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }
}