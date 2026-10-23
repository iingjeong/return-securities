package com.app.returns.domain.mapper;

import com.app.returns.domain.dto.DomesticTradeDTO;
import com.app.returns.domain.dto.request.DomesticTradeRequestDTO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DomesticTradeMapper {
    List<DomesticTradeDTO> selectByCiHash(DomesticTradeRequestDTO requestDTO);
}
