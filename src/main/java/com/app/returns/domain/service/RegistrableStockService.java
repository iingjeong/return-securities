package com.app.returns.domain.service;

import com.app.returns.domain.dto.request.RegistrableStockRequestDTO;
import com.app.returns.domain.dto.response.RegistrableStockResponseDTO;

import java.util.List;

public interface RegistrableStockService {

    RegistrableStockResponseDTO findHeldQty(RegistrableStockRequestDTO requestDTO);

    List<RegistrableStockResponseDTO> findLots(RegistrableStockRequestDTO requestDTO);
}
