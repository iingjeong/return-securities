package com.app.returns.domain.mapper;

import com.app.returns.domain.dto.DomesticTradeDTO;
import com.app.returns.domain.dto.request.DomesticTradeRequestDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DomesticTradeMapper {
    List<DomesticTradeDTO> selectByCiHash(DomesticTradeRequestDTO requestDTO);
}
