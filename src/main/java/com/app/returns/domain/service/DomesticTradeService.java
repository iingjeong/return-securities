package com.app.returns.domain.service;

import com.app.returns.domain.dto.request.DomesticTradeRequestDTO;
import com.app.returns.domain.dto.response.DomesticTradeResponseDTO;

import java.util.List;

public interface DomesticTradeService {
    List<DomesticTradeResponseDTO> findByCiHash(DomesticTradeRequestDTO requestDTO);
}
