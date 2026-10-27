package com.app.returns.domain.generalaccount.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.app.returns.domain.generalaccount.dto.response.GeneralAccountResponseDTO;
import com.app.returns.domain.generalaccount.exception.GeneralAccountException;
import com.app.returns.domain.generalaccount.exception.GeneralAccountNotFoundException;
import com.app.returns.domain.generalaccount.service.GeneralAccountService;
import com.app.returns.domain.generalaccount.type.GeneralStatus;
import com.app.returns.global.exception.GlobalExceptionHandler;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

class GeneralAccountApiTest {

    private GeneralAccountService generalAccountService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        generalAccountService = mock(GeneralAccountService.class);
        GeneralAccountApi controller = new GeneralAccountApi(generalAccountService);
        MethodValidationPostProcessor postProcessor = new MethodValidationPostProcessor();
        postProcessor.afterPropertiesSet();
        Object proxied =
                postProcessor.postProcessAfterInitialization(controller, "generalAccountApi");
        mockMvc =
                MockMvcBuilders.standaloneSetup(proxied)
                        .setControllerAdvice(new GlobalExceptionHandler())
                        .build();
    }

    @Test
    void verifyGeneralAccountReturnsVerifiedAccount() throws Exception {
        GeneralAccountResponseDTO response =
                GeneralAccountResponseDTO.builder()
                        .generalAccountId(10L)
                        .accountNo("1000000001")
                        .status(GeneralStatus.ACTIVE)
                        .build();
        when(generalAccountService.verifyGeneralAccount(any())).thenReturn(response);

        mockMvc.perform(
                        post("/api/general-accounts/verify")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"ciHash":"ci-owner","generalAccountId":10}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("일반계좌 검증 성공"))
                .andExpect(jsonPath("$.data.generalAccountId").value(10))
                .andExpect(jsonPath("$.data.accountNo").value("1000000001"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        verify(generalAccountService).verifyGeneralAccount(any());
    }

    @Test
    void verifyGeneralAccountRejectsBlankCiHashBeforeCallingService() throws Exception {
        mockMvc.perform(
                        post("/api/general-accounts/verify")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"ciHash":"   ","generalAccountId":10}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ciHash는 필수입니다."));

        verify(generalAccountService, never()).verifyGeneralAccount(any());
    }

    @Test
    void verifyGeneralAccountRejectsMissingAccountIdBeforeCallingService() throws Exception {
        mockMvc.perform(
                        post("/api/general-accounts/verify")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"ciHash":"ci-owner"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("generalAccountId는 필수입니다."));

        verify(generalAccountService, never()).verifyGeneralAccount(any());
    }

    @Test
    void verifyGeneralAccountRejectsNonPositiveAccountIdBeforeCallingService() throws Exception {
        mockMvc.perform(
                        post("/api/general-accounts/verify")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"ciHash":"ci-owner","generalAccountId":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("generalAccountId는 양수여야 합니다."));

        verify(generalAccountService, never()).verifyGeneralAccount(any());
    }

    @Test
    void verifyGeneralAccountReturnsNotFoundForMissingCustomerOrAccount() throws Exception {
        when(generalAccountService.verifyGeneralAccount(any()))
                .thenThrow(new GeneralAccountNotFoundException("계좌를 찾을 수 없습니다."));

        mockMvc.perform(
                        post("/api/general-accounts/verify")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"ciHash":"ci-owner","generalAccountId":999}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("계좌를 찾을 수 없습니다."));
    }

    @Test
    void verifyGeneralAccountReturnsBadRequestForOwnershipOrStatusFailure() throws Exception {
        when(generalAccountService.verifyGeneralAccount(any()))
                .thenThrow(new GeneralAccountException("해당 고객 소유의 계좌가 아닙니다."));

        mockMvc.perform(
                        post("/api/general-accounts/verify")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"ciHash":"ci-owner","generalAccountId":10}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("해당 고객 소유의 계좌가 아닙니다."));
    }

    @Test
    void findByCiHashReturnsAccountList() throws Exception {
        GeneralAccountResponseDTO account =
                GeneralAccountResponseDTO.builder()
                        .generalAccountId(10L)
                        .accountNo("1000000001")
                        .status(GeneralStatus.ACTIVE)
                        .build();
        when(generalAccountService.findByCiHash("ci-owner")).thenReturn(List.of(account));

        mockMvc.perform(
                        get("/api/general-accounts")
                                .accept(MediaType.APPLICATION_JSON)
                                .param("ciHash", "ci-owner"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("일반계좌 목록 조회 성공"))
                .andExpect(jsonPath("$.data[0].generalAccountId").value(10))
                .andExpect(jsonPath("$.data[0].accountNo").value("1000000001"));

        verify(generalAccountService).findByCiHash("ci-owner");
    }

    @Test
    void findByCiHashRejectsBlankCiHash() throws Exception {
        mockMvc.perform(
                        get("/api/general-accounts")
                                .accept(MediaType.APPLICATION_JSON)
                                .param("ciHash", "   "))
                .andExpect(status().isBadRequest());

        verify(generalAccountService, never()).findByCiHash(any());
    }
}
