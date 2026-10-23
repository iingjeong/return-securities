package com.app.returns.domain.generalaccount.service;

import com.app.returns.domain.generalaccount.dto.request.GeneralAccountRequestDTO;
import com.app.returns.domain.generalaccount.dto.response.GeneralAccountResponseDTO;
import java.util.List;

public interface GeneralAccountService {
    GeneralAccountResponseDTO verifyGeneralAccount(GeneralAccountRequestDTO requestDTO);

    List<GeneralAccountResponseDTO> findByCiHash(String ciHash);
}
