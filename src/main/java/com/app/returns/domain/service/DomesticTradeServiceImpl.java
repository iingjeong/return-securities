package com.app.returns.domain.service;

import com.app.returns.domain.dto.request.DomesticTradeRequestDTO;
import com.app.returns.domain.dto.response.DomesticTradeResponseDTO;
import com.app.returns.domain.mapper.DomesticTradeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class DomesticTradeServiceImpl implements DomesticTradeService {

    private final DomesticTradeMapper domesticTradeMapper;

    @Override
    public List<DomesticTradeResponseDTO> findByCiHash(DomesticTradeRequestDTO requestDTO) {
        return domesticTradeMapper.selectByCiHash(requestDTO).stream()
                .map(DomesticTradeResponseDTO::new)
                .toList();
    }
}
