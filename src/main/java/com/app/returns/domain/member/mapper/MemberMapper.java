package com.app.returns.domain.member.mapper;

import com.app.returns.domain.member.dto.MemberDTO;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MemberMapper {
    public Optional<MemberDTO> selectById(@Param("id") String mid);
}
