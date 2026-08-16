package com.app.returns.domain.mapper;

import com.app.returns.domain.dto.DomesticTradeDTO;
import com.app.returns.domain.dto.request.DomesticTradeRequestDTO;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DomesticTradeMapperTest {

    private static PooledDataSource dataSource;
    private static SqlSessionFactory sqlSessionFactory;

    private SqlSession sqlSession;
    private DomesticTradeMapper domesticTradeMapper;

    @BeforeAll
    static void configureMyBatis() throws IOException {
        try (Reader reader = Resources.getResourceAsReader("mybatis-domestictrade-test-config.xml")) {
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
        domesticTradeMapper = sqlSession.getMapper(DomesticTradeMapper.class);
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
    @DisplayName("ciHash로 조회하면 매매내역을 매매일 오름차순으로 반환한다")
    void selectByCiHashReturnsTradesOrderedByExecutedAtAscending() {
        insertGeneralCustomer(1L, "hash-1");
        insertDomesticTrade(
                1L, "BUY", "005930", BigDecimal.valueOf(10), BigDecimal.valueOf(70000),
                LocalDateTime.of(2026, 3, 10, 9, 0));
        insertDomesticTrade(
                1L, "SELL", "005930", BigDecimal.valueOf(5), BigDecimal.valueOf(72000),
                LocalDateTime.of(2026, 3, 5, 9, 0));

        DomesticTradeRequestDTO request = DomesticTradeRequestDTO.builder().ciHash("hash-1").build();

        List<DomesticTradeDTO> result = domesticTradeMapper.selectByCiHash(request);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getExecutedAt()).isEqualTo(LocalDateTime.of(2026, 3, 5, 9, 0));
        assertThat(result.get(0).getTradeType()).isEqualTo("SELL");
        assertThat(result.get(1).getExecutedAt()).isEqualTo(LocalDateTime.of(2026, 3, 10, 9, 0));
    }

    @Test
    @DisplayName("다른 고객의 매매내역은 섞이지 않는다")
    void selectByCiHashExcludesOtherCustomersTrades() {
        insertGeneralCustomer(1L, "hash-1");
        insertGeneralCustomer(2L, "hash-2");
        insertDomesticTrade(
                1L, "BUY", "005930", BigDecimal.valueOf(10), BigDecimal.valueOf(70000),
                LocalDateTime.of(2026, 3, 5, 9, 0));
        insertDomesticTrade(
                2L, "BUY", "000660", BigDecimal.valueOf(20), BigDecimal.valueOf(150000),
                LocalDateTime.of(2026, 3, 6, 9, 0));

        DomesticTradeRequestDTO request = DomesticTradeRequestDTO.builder().ciHash("hash-1").build();

        List<DomesticTradeDTO> result = domesticTradeMapper.selectByCiHash(request);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStockCode()).isEqualTo("005930");
    }

    @Test
    @DisplayName("일치하는 매매내역이 없으면 빈 목록을 반환한다")
    void selectByCiHashReturnsEmptyListWhenNoMatch() {
        insertGeneralCustomer(1L, "hash-1");

        DomesticTradeRequestDTO request = DomesticTradeRequestDTO.builder().ciHash("hash-1").build();

        List<DomesticTradeDTO> result = domesticTradeMapper.selectByCiHash(request);

        assertThat(result).isEmpty();
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
                    CREATE TABLE domestic_trade (
                        trade_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        general_customer_id BIGINT NOT NULL,
                        trade_type VARCHAR(10) NOT NULL,
                        stock_code VARCHAR(20) NOT NULL,
                        qty DECIMAL(15,4) NOT NULL,
                        price DECIMAL(15,4) NOT NULL,
                        executed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
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

    private void insertDomesticTrade(
            Long generalCustomerId,
            String tradeType,
            String stockCode,
            BigDecimal qty,
            BigDecimal price,
            LocalDateTime executedAt) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    INSERT INTO domestic_trade
                    (general_customer_id, trade_type, stock_code, qty, price, executed_at)
                    VALUES (%d, '%s', '%s', %s, %s, '%s')
                    """.formatted(
                    generalCustomerId, tradeType, stockCode, qty, price, executedAt
            ));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}