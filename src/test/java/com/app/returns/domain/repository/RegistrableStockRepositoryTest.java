package com.app.returns.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;

import com.app.returns.domain.dto.response.RegistrableStockResponseDTO;
import com.app.returns.domain.entity.RegistrableStockEntity;
import com.app.returns.domain.generalaccount.type.GeneralAccountType;
import com.app.returns.domain.generalaccount.type.GeneralStatus;
import com.app.returns.domain.mapper.RegistrableStockMapper;
import com.app.returns.domain.service.RegistrableStockService;
import com.app.returns.domain.service.RegistrableStockServiceImpl;
import com.app.returns.global.config.PersistenceConfig;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.jpa.show-sql=false",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@Import({PersistenceConfig.class, RegistrableStockServiceImpl.class})
@Transactional(transactionManager = "jpaTransactionManager")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RegistrableStockRepositoryTest {
    @Autowired private JdbcTemplate jdbc;
    @Autowired private EntityManager entityManager;
    @Autowired private RegistrableStockRepository repository;
    @Autowired private RegistrableStockService service;
    @MockitoBean private RegistrableStockMapper legacyMapper;

    @Autowired
    @Qualifier("transactionManager")
    private PlatformTransactionManager legacyTransactionManager;

    @Autowired
    @Qualifier("jpaTransactionManager")
    private PlatformTransactionManager jpaTransactionManager;

    @BeforeAll
    void createTestSchema() {
        // H2 test database only. Do not execute the destructive production DDL.
        jdbc.execute("""
                CREATE TABLE general_customer (
                    general_customer_id BIGINT PRIMARY KEY, ci_hash VARCHAR(64) NOT NULL UNIQUE,
                    name VARCHAR(50) NOT NULL, birth_date DATE NOT NULL
                )
                """);
        jdbc.execute("""
                CREATE TABLE general_account (
                    general_account_id BIGINT PRIMARY KEY,
                    general_customer_id BIGINT NOT NULL REFERENCES general_customer(general_customer_id),
                    account_no VARCHAR(10) NOT NULL, account_type VARCHAR(30) NOT NULL,
                    status VARCHAR(10) NOT NULL
                )
                """);
        jdbc.execute("""
                CREATE TABLE registrable_stock (
                    registrable_stock_id BIGINT PRIMARY KEY,
                    general_account_id BIGINT NOT NULL REFERENCES general_account(general_account_id),
                    foreign_product_id BIGINT NOT NULL, held_qty DECIMAL(15,4) NOT NULL,
                    source_broker VARCHAR(20), recorded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    purchase_date TIMESTAMP NOT NULL, purchase_price DECIMAL(15,4) NOT NULL,
                    purchase_currency VARCHAR(10) NOT NULL, purchase_fx_rate DECIMAL(15,4) NOT NULL
                )
                """);
    }

    @BeforeEach
    void seedData() {
        jdbc.update("""
                INSERT INTO general_customer (general_customer_id, ci_hash, name, birth_date)
                VALUES (1, 'jpa-ci-1', 'Test customer 1', '2000-01-01'),
                       (2, 'jpa-ci-2', 'Test customer 2', '2000-01-02'),
                       (3, 'jpa-ci-3', 'No accounts', '2000-01-03')
                """);
        jdbc.update("""
                INSERT INTO general_account
                    (general_account_id, general_customer_id, account_no, account_type, status)
                VALUES (10, 1, '0000000010', 'BROKERAGE', 'ACTIVE'),
                       (11, 1, '0000000011', 'CMA', 'CLOSED'),
                       (12, 1, '0000000012', 'CMA', 'ACTIVE'),
                       (20, 2, '0000000020', 'BROKERAGE', 'ACTIVE')
                """);
        insertLot(101, 10, 2, "2025-01-03 10:00:00");
        insertLot(102, 12, 1, "2025-01-02 10:00:00");
        insertLot(103, 10, 1, "2025-01-01 10:00:00");
        insertLot(104, 10, 1, "2025-01-01 10:00:00");
        insertLot(105, 11, 1, "2024-01-01 10:00:00");
        insertLot(106, 20, 1, "2023-01-01 10:00:00");
    }

    @Test
    @DisplayName("다른 고객의 적격 원본은 반환하지 않는다")
    void filtersByCustomerCiHash() {
        assertThat(service.findCustomerLots("jpa-ci-2"))
                .extracting(RegistrableStockResponseDTO::getRegistrableStockId)
                .containsExactly(106L);
    }

    @Test
    @DisplayName("해지된 일반계좌는 제외하고 활성 일반계좌를 모두 조회한다")
    void excludesClosedAccounts() {
        List<RegistrableStockResponseDTO> result = service.findCustomerLots("jpa-ci-1");
        assertThat(result).hasSize(4);
        assertThat(result).extracting(RegistrableStockResponseDTO::getGeneralAccountId)
                .containsOnly(10L, 12L);
    }

    @Test
    @DisplayName("종목·매수일·원본 ID 순서로 동일 매수일의 순서까지 고정한다")
    void ordersLotsDeterministically() {
        assertThat(service.findCustomerLots("jpa-ci-1"))
                .extracting(RegistrableStockResponseDTO::getRegistrableStockId)
                .containsExactly(103L, 104L, 102L, 101L);
    }

    @Test
    @DisplayName("적격 원본이 없거나 고객이 없으면 빈 목록을 반환한다")
    void returnsEmptyForNoLots() {
        assertThat(service.findCustomerLots("jpa-ci-3")).isEmpty();
        assertThat(service.findCustomerLots("missing-ci")).isEmpty();
    }

    @Test
    @DisplayName("원본·종목·출처와 소수점 수량을 응답 DTO로 변환하고 MyBatis는 호출하지 않는다")
    void mapsResponseUsingJpaOnly() {
        RegistrableStockResponseDTO response = service.findCustomerLots("jpa-ci-1").get(0);
        entityManager.clear();

        assertThat(response.getRegistrableStockId()).isEqualTo(103L);
        assertThat(response.getForeignProductId()).isEqualTo(1L);
        assertThat(response.getGeneralAccountId()).isEqualTo(10L);
        assertThat(response.getAccountType()).isEqualTo(GeneralAccountType.BROKERAGE);
        assertThat(response.getHeldQty()).isEqualByComparingTo("100.1234");
        assertThat(response.getPurchasePrice()).isEqualByComparingTo("180.1234");
        assertThat(response.getPurchaseCurrency()).isEqualTo("USD");
        assertThat(response.getPurchaseFxRate()).isEqualByComparingTo("1380.5678");
        assertThat(response.getSourceBroker()).isNull();
        verifyNoInteractions(legacyMapper);
    }

    @Test
    @DisplayName("응답에 필요한 일반계좌는 원본 조회 시 함께 가져온다")
    void fetchesAccountWithLots() {
        List<RegistrableStockEntity> lots =
                repository.findCustomerLots("jpa-ci-1", GeneralStatus.ACTIVE);
        entityManager.clear();

        assertThat(lots.get(0).getGeneralAccount().getAccountType())
                .isEqualTo(GeneralAccountType.BROKERAGE);
    }

    @Test
    @DisplayName("기존 JDBC 트랜잭션과 신규 JPA 트랜잭션을 구분한다")
    void keepsSeparateTransactionManagers() {
        assertThat(legacyTransactionManager).isInstanceOf(DataSourceTransactionManager.class);
        assertThat(jpaTransactionManager).isInstanceOf(JpaTransactionManager.class);
        assertThat(entityManager.isJoinedToTransaction()).isTrue();
    }

    private void insertLot(long id, long accountId, long productId, String purchasedAt) {
        jdbc.update("""
                INSERT INTO registrable_stock
                    (registrable_stock_id, general_account_id, foreign_product_id, held_qty,
                     purchase_date, purchase_price, purchase_currency, purchase_fx_rate)
                VALUES (?, ?, ?, 100.1234, ?, 180.1234, 'USD', 1380.5678)
                """, id, accountId, productId, purchasedAt);
    }
}
