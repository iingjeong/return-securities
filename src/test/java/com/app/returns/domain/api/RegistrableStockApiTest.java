package com.app.returns.domain.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.app.returns.domain.dto.response.RegistrableStockResponseDTO;
import com.app.returns.domain.exception.RegistrableStockNotFoundException;
import com.app.returns.domain.service.RegistrableStockService;
import com.app.returns.global.exception.GlobalExceptionHandler;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

class RegistrableStockApiTest {

    private RegistrableStockService registrableStockService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        registrableStockService = mock(RegistrableStockService.class);
        RegistrableStockApi controller = new RegistrableStockApi(registrableStockService);
        MethodValidationPostProcessor postProcessor = new MethodValidationPostProcessor();
        postProcessor.afterPropertiesSet();
        Object proxied =
                postProcessor.postProcessAfterInitialization(controller, "registrableStockApi");
        mockMvc =
                MockMvcBuilders.standaloneSetup(proxied)
                        .setControllerAdvice(new GlobalExceptionHandler())
                        .build();
    }

    @Test
    void getRegistrableStockAcceptsValidRequest() throws Exception {
        RegistrableStockResponseDTO response =
                RegistrableStockResponseDTO.builder()
                        .generalAccountId(1L)
                        .heldQty(BigDecimal.valueOf(100))
                        .sourceBroker(null)
                        .purchaseDate(LocalDateTime.now())
                        .purchasePrice(BigDecimal.valueOf(150.25))
                        .purchaseCurrency("USD")
                        .purchaseFxRate(BigDecimal.valueOf(1320.5))
                        .build();
        when(registrableStockService.findHeldQty(any())).thenReturn(response);

        mockMvc.perform(
                        get("/api/registrable-stocks")
                                .accept(MediaType.APPLICATION_JSON)
                                .param("ciHash", "hash-1")
                                .param("foreignProductId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("조회 성공"))
                .andExpect(jsonPath("$.data.heldQty").value(100));

        verify(registrableStockService).findHeldQty(any());
    }

    @Test
    void getRegistrableStockRejectsMissingCiHash() throws Exception {
        mockMvc.perform(
                        get("/api/registrable-stocks")
                                .accept(MediaType.APPLICATION_JSON)
                                .param("foreignProductId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ciHash는 필수입니다."));

        verify(registrableStockService, never()).findHeldQty(any());
    }

    @Test
    void getRegistrableStockRejectsMissingForeignProductId() throws Exception {
        mockMvc.perform(
                        get("/api/registrable-stocks")
                                .accept(MediaType.APPLICATION_JSON)
                                .param("ciHash", "hash-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("foreignProductId는 필수입니다."));

        verify(registrableStockService, never()).findHeldQty(any());
    }

    @Test
    void getRegistrableStockLotsAcceptsValidRequest() throws Exception {
        RegistrableStockResponseDTO lot1 =
                RegistrableStockResponseDTO.builder()
                        .generalAccountId(10L)
                        .heldQty(BigDecimal.valueOf(40))
                        .purchaseDate(LocalDateTime.now())
                        .purchasePrice(BigDecimal.valueOf(150.25))
                        .purchaseCurrency("USD")
                        .purchaseFxRate(BigDecimal.valueOf(1320.5))
                        .build();
        RegistrableStockResponseDTO lot2 =
                RegistrableStockResponseDTO.builder()
                        .generalAccountId(20L)
                        .heldQty(BigDecimal.valueOf(60))
                        .purchaseDate(LocalDateTime.now())
                        .purchasePrice(BigDecimal.valueOf(180))
                        .purchaseCurrency("USD")
                        .purchaseFxRate(BigDecimal.valueOf(1330))
                        .build();
        when(registrableStockService.findLots(any())).thenReturn(List.of(lot1, lot2));

        mockMvc.perform(
                        get("/api/registrable-stocks/lots")
                                .accept(MediaType.APPLICATION_JSON)
                                .param("ciHash", "hash-1")
                                .param("foreignProductId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("조회 성공"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].generalAccountId").value(10))
                .andExpect(jsonPath("$.data[1].generalAccountId").value(20));

        verify(registrableStockService).findLots(any());
    }

    @Test
    void getRegistrableStockLotsRejectsMissingCiHash() throws Exception {
        mockMvc.perform(
                        get("/api/registrable-stocks/lots")
                                .accept(MediaType.APPLICATION_JSON)
                                .param("foreignProductId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ciHash는 필수입니다."));

        verify(registrableStockService, never()).findLots(any());
    }

    @Test
    void getCustomerLotsAcceptsValidCiHash() throws Exception {
        when(registrableStockService.findCustomerLots("hash-1")).thenReturn(List.of());

        mockMvc.perform(
                        get("/api/registrable-stocks/customer-lots")
                                .accept(MediaType.APPLICATION_JSON)
                                .param("ciHash", "hash-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("조회 성공"));

        verify(registrableStockService).findCustomerLots("hash-1");
    }

    @Test
    void getCustomerLotsRejectsBlankCiHash() throws Exception {
        mockMvc.perform(
                        get("/api/registrable-stocks/customer-lots")
                                .accept(MediaType.APPLICATION_JSON)
                                .param("ciHash", "   "))
                .andExpect(status().isBadRequest());

        verify(registrableStockService, never()).findCustomerLots(any());
    }

    @Test
    void getRegistrableStockReturnsNotFoundWhenStockMissing() throws Exception {
        when(registrableStockService.findHeldQty(any()))
                .thenThrow(new RegistrableStockNotFoundException("등록가능 보유수량 조회 실패"));

        mockMvc.perform(
                        get("/api/registrable-stocks")
                                .accept(MediaType.APPLICATION_JSON)
                                .param("ciHash", "hash-1")
                                .param("foreignProductId", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("등록가능 보유수량 조회 실패"));
    }
}
