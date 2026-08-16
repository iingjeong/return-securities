package com.app.returns.domain.mapper;

import com.app.returns.domain.dto.RegistrableStockDTO;
import com.app.returns.domain.dto.request.RegistrableStockRequestDTO;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class RegistrableStockMapperTest {

    private static PooledDataSource dataSource;
    private static SqlSessionFactory sqlSessionFactory;

    private SqlSession sqlSession;
    private RegistrableStockMapper registrableStockMapper;

    @BeforeAll
    static void configureMyBatis() throws IOException {
        try (Reader reader = Resources.getResourceAsReader("mybatis-registrablestock-test-config.xml")) {
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        }
        dataSource = (PooledDataSource) sqlSessionFactory
                .getConfiguration()
                .getEnvironment()
                .getDataSource();
    }

    @BeforeEach
    void setUpDatabase() throws SQLException {
        resetSchema();
        sqlSession = sqlSessionFactory.openSession(true);
        registrableStockMapper = sqlSession.getMapper(RegistrableStockMapper.class);
    }

    @AfterEach
    void closeSession() {
        if (sqlSession != null) {
            sqlSession.close();
        }
    }

    @AfterAll
    static void closeDataSource() {
        if (dataSource != null) {
            dataSource.forceCloseAll();
        }
    }

    @Test
    @DisplayName("일치하는 ciHash·상품이 있으면 값을 반환한다")
    void findHeldQtyReturnsValueWhenMatchExists() {
        insertGeneralCustomer(1L, "hash-1");
        insertGeneralAccount(1L, 1L);
        insertRegistrableStock(1L, 1L, BigDecimal.valueOf(100));

        RegistrableStockRequestDTO request = RegistrableStockRequestDTO.builder()
                .ciHash("hash-1")
                .foreignProductId(1L)
                .build();

        Optional<RegistrableStockDTO> result = registrableStockMapper.findHeldQty(request);

        assertThat(result).isPresent();
        assertThat(result.get().getHeldQty()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    @DisplayName("ciHash는 같지만 상품이 다르면 결과가 없다")
    void findHeldQtyReturnsEmptyWhenProductDiffers() {
        insertGeneralCustomer(1L, "hash-1");
        insertGeneralAccount(1L, 1L);
        insertRegistrableStock(1L, 1L, BigDecimal.valueOf(100));

        RegistrableStockRequestDTO request = RegistrableStockRequestDTO.builder()
                .ciHash("hash-1")
                .foreignProductId(2L)
                .build();

        Optional<RegistrableStockDTO> result = registrableStockMapper.findHeldQty(request);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("상품은 같지만 ciHash가 다르면 결과가 없다")
    void findHeldQtyReturnsEmptyWhenCiHashDiffers() {
        insertGeneralCustomer(1L, "hash-1");
        insertGeneralAccount(1L, 1L);
        insertRegistrableStock(1L, 1L, BigDecimal.valueOf(100));

        RegistrableStockRequestDTO request = RegistrableStockRequestDTO.builder()
                .ciHash("hash-2")
                .foreignProductId(1L)
                .build();

        Optional<RegistrableStockDTO> result = registrableStockMapper.findHeldQty(request);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("같은 고객이 일반계좌 여러 개에 나눠 보유하면 보유수량을 합산한다")
    void findHeldQtySumsAcrossMultipleGeneralAccountsForSameCustomer() {
        insertGeneralCustomer(1L, "hash-1");
        insertGeneralAccount(1L, 1L);
        insertGeneralAccount(2L, 1L);
        insertRegistrableStock(1L, 1L, BigDecimal.valueOf(60));
        insertRegistrableStock(2L, 1L, BigDecimal.valueOf(40));

        RegistrableStockRequestDTO request = RegistrableStockRequestDTO.builder()
                .ciHash("hash-1")
                .foreignProductId(1L)
                .build();

        Optional<RegistrableStockDTO> result = registrableStockMapper.findHeldQty(request);

        assertThat(result).isPresent();
        assertThat(result.get().getHeldQty()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    @DisplayName("다른 고객의 일반계좌 보유수량은 합산에 섞이지 않는다")
    void findHeldQtyDoesNotMixOtherCustomersGeneralAccounts() {
        insertGeneralCustomer(1L, "hash-1");
        insertGeneralCustomer(2L, "hash-2");
        insertGeneralAccount(1L, 1L);
        insertGeneralAccount(2L, 2L);
        insertRegistrableStock(1L, 1L, BigDecimal.valueOf(60));
        insertRegistrableStock(2L, 1L, BigDecimal.valueOf(40));

        RegistrableStockRequestDTO request = RegistrableStockRequestDTO.builder()
                .ciHash("hash-1")
                .foreignProductId(1L)
                .build();

        Optional<RegistrableStockDTO> result = registrableStockMapper.findHeldQty(request);

        assertThat(result).isPresent();
        assertThat(result.get().getHeldQty()).isEqualByComparingTo(BigDecimal.valueOf(60));
    }

    @Test
    @DisplayName("여러 lot 중 매수일이 가장 이른 lot의 출처 정보를 대표값으로 반환한다")
    void findHeldQtyUsesEarliestPurchaseDateLotAsRepresentative() {
        insertGeneralCustomer(1L, "hash-1");
        insertGeneralAccount(1L, 1L);
        insertGeneralAccount(2L, 1L);
        insertRegistrableStock(
                1L, 1L, BigDecimal.valueOf(60), LocalDateTime.of(2026, 3, 10, 9, 0),
                BigDecimal.valueOf(180));
        insertRegistrableStock(
                2L, 1L, BigDecimal.valueOf(40), LocalDateTime.of(2026, 1, 5, 9, 0),
                BigDecimal.valueOf(150));

        RegistrableStockRequestDTO request = RegistrableStockRequestDTO.builder()
                .ciHash("hash-1")
                .foreignProductId(1L)
                .build();

        Optional<RegistrableStockDTO> result = registrableStockMapper.findHeldQty(request);

        assertThat(result).isPresent();
        assertThat(result.get().getGeneralAccountId()).isEqualTo(2L);
        assertThat(result.get().getPurchasePrice()).isEqualByComparingTo(BigDecimal.valueOf(150));
    }

    private void resetSchema() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DROP ALL OBJECTS");
            statement.execute("""
                    CREATE TABLE general_customer (
                        general_customer_id BIGINT PRIMARY KEY,
                        ci_hash VARCHAR(64) NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE general_account (
                        general_account_id BIGINT PRIMARY KEY,
                        general_customer_id BIGINT NOT NULL,
                        account_no VARCHAR(10) NOT NULL,
                        account_type VARCHAR(30) NOT NULL,
                        status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE'
                    )
                    """);
            statement.execute("""
                    CREATE TABLE registrable_stock (
                        registrable_stock_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        general_account_id BIGINT NOT NULL,
                        foreign_product_id BIGINT NOT NULL,
                        held_qty DECIMAL(15, 4) NOT NULL,
                        source_broker VARCHAR(20),
                        recorded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        purchase_date DATETIME NOT NULL,
                        purchase_price DECIMAL(15, 4) NOT NULL,
                        purchase_currency VARCHAR(10) NOT NULL,
                        purchase_fx_rate DECIMAL(15, 4) NOT NULL
                    )
                    """);
        }
    }

    private void insertGeneralCustomer(Long generalCustomerId, String ciHash) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    INSERT INTO general_customer (general_customer_id, ci_hash)
                    VALUES (%d, '%s')
                    """.formatted(generalCustomerId, ciHash));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void insertGeneralAccount(Long generalAccountId, Long generalCustomerId) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    INSERT INTO general_account
                    (general_account_id, general_customer_id, account_no, account_type, status)
                    VALUES (%d, %d, '%010d', 'BROKERAGE', 'ACTIVE')
                    """.formatted(generalAccountId, generalCustomerId, generalAccountId));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void insertRegistrableStock(Long generalAccountId, Long foreignProductId, BigDecimal heldQty) {
        insertRegistrableStock(
                generalAccountId, foreignProductId, heldQty, LocalDateTime.now(),
                BigDecimal.valueOf(150.25));
    }

    private void insertRegistrableStock(
            Long generalAccountId,
            Long foreignProductId,
            BigDecimal heldQty,
            LocalDateTime purchaseDate,
            BigDecimal purchasePrice) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    INSERT INTO registrable_stock
                    (general_account_id, foreign_product_id, held_qty, purchase_date, purchase_price, purchase_currency, purchase_fx_rate)
                    VALUES (%d, %d, %s, '%s', %s, 'USD', 1320.5)
                    """.formatted(
                    generalAccountId, foreignProductId, heldQty, purchaseDate, purchasePrice
            ));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}