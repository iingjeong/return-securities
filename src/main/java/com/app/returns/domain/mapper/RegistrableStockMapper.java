package com.app.returns.domain.mapper;

import com.app.returns.domain.dto.RegistrableStockDTO;
import com.app.returns.domain.dto.request.RegistrableStockRequestDTO;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RegistrableStockMapper {
    Optional<RegistrableStockDTO> findHeldQty(RegistrableStockRequestDTO requestDTO);

    List<RegistrableStockDTO> findLotsByCiHashAndProduct(RegistrableStockRequestDTO requestDTO);

    List<RegistrableStockDTO> findCustomerLotsByCiHash(String ciHash);
}
