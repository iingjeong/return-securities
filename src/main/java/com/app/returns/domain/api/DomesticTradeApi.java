package com.app.returns.domain.api;

import com.app.returns.domain.dto.request.DomesticTradeRequestDTO;
import com.app.returns.domain.dto.response.DomesticTradeResponseDTO;
import com.app.returns.domain.service.DomesticTradeService;
import com.app.returns.global.response.ApiResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/domestic-trades")
public class DomesticTradeApi {

    private final DomesticTradeService domesticTradeService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<DomesticTradeResponseDTO>>> getDomesticTrades(
            @Valid DomesticTradeRequestDTO request) {
        List<DomesticTradeResponseDTO> result = domesticTradeService.findByCiHash(request);
        return ResponseEntity.ok(ApiResponseDTO.of("조회 성공", result));
    }
}
