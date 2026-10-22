package com.app.returns.domain.repository;

import com.app.returns.domain.entity.RegistrableStockEntity;
import com.app.returns.domain.generalaccount.type.GeneralStatus;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/** 조회 전용. 고객 CI와 활성 상태를 서버가 지정하고 원본 ID까지 정렬을 고정한다. */
public interface RegistrableStockRepository extends Repository<RegistrableStockEntity, Long> {
    @Query("""
            select stock
            from RegistrableStockEntity stock
            join fetch stock.generalAccount account
            join account.generalCustomer customer
            where customer.ciHash = :ciHash
              and account.status = :status
            order by stock.foreignProductId asc, stock.purchaseDate asc, stock.registrableStockId asc
            """)
    List<RegistrableStockEntity> findCustomerLots(
            @Param("ciHash") String ciHash, @Param("status") GeneralStatus status);
}
