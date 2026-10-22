package com.app.returns.domain.service;

import com.app.returns.domain.dto.request.RegistrableStockRequestDTO;
import com.app.returns.domain.dto.response.RegistrableStockResponseDTO;
import com.app.returns.domain.exception.RegistrableStockNotFoundException;
import com.app.returns.domain.mapper.RegistrableStockMapper;
import com.app.returns.domain.generalaccount.type.GeneralStatus;
import com.app.returns.domain.repository.RegistrableStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class RegistrableStockServiceImpl implements RegistrableStockService {

    private final RegistrableStockMapper registrableStockMapper;
    private final RegistrableStockRepository registrableStockRepository;

    @Override
    public RegistrableStockResponseDTO findHeldQty(RegistrableStockRequestDTO requestDTO) {
        return registrableStockMapper.findHeldQty(requestDTO)
                .map(RegistrableStockResponseDTO::new)
                .orElseThrow(() -> new RegistrableStockNotFoundException("등록가능 보유수량 조회 실패"));
    }

    @Override
    public List<RegistrableStockResponseDTO> findLots(RegistrableStockRequestDTO requestDTO) {
        return registrableStockMapper.findLotsByCiHashAndProduct(requestDTO).stream()
                .map(RegistrableStockResponseDTO::new)
                .toList();
    }

    @Override
    @Transactional(transactionManager = "jpaTransactionManager", readOnly = true)
    public List<RegistrableStockResponseDTO> findCustomerLots(String ciHash) {
        return registrableStockRepository.findCustomerLots(ciHash, GeneralStatus.ACTIVE).stream()
                .map(stock -> RegistrableStockResponseDTO.builder()
                        .registrableStockId(stock.getRegistrableStockId())
                        .foreignProductId(stock.getForeignProductId())
                        .generalAccountId(stock.getGeneralAccount().getGeneralAccountId())
                        .accountNo(stock.getGeneralAccount().getAccountNo())
                        .accountType(stock.getGeneralAccount().getAccountType())
                        .heldQty(stock.getHeldQty())
                        .sourceBroker(stock.getSourceBroker())
                        .purchaseDate(stock.getPurchaseDate())
                        .purchasePrice(stock.getPurchasePrice())
                        .purchaseCurrency(stock.getPurchaseCurrency())
                        .purchaseFxRate(stock.getPurchaseFxRate())
                        .build())
                .toList();
    }
}
