package com.techconsulting.pfmp.repository;

import com.techconsulting.pfmp.domain.ManualLendingInvestment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface ManualLendingInvestmentRepository extends JpaRepository<ManualLendingInvestment, Long> {

    ManualLendingInvestment findBySchemeIdIgnoreCaseAndManualLendingPortfolioId(String schemeId, Long id);

    boolean existsBySchemeIdIgnoreCaseAndManualLendingPortfolioId(String schemeId, Long portfolioId);

    @Query(value = """
        SELECT
            DATE_FORMAT(MIN(investment_date), '%b %Y') AS month,
            COUNT(*) AS total,
            SUM(CASE WHEN isnpa = TRUE THEN 1 ELSE 0 END) AS npa,
            SUM(CASE WHEN loan_status = 'ACTIVE' THEN 1 ELSE 0 END) AS active,
            SUM(CASE WHEN loan_status = 'CLOSED' THEN 1 ELSE 0 END) AS closed
        FROM manual_lending_investment
        WHERE created_by=:username and investment_date >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH)
        GROUP BY YEAR(investment_date), MONTH(investment_date)
        ORDER BY YEAR(investment_date), MONTH(investment_date)
        """, nativeQuery = true)
    List<Map<String, Object>> getLast12MonthSummary(@Param("username") String username);

    @Query(value = """
        SELECT
            COUNT(*) AS total,
            COALESCE(SUM(CASE WHEN isnpa = TRUE THEN 1 ELSE 0 END), 0) AS npa,
            COALESCE(SUM(CASE WHEN loan_status = 'ACTIVE' THEN 1 ELSE 0 END), 0) AS active,
            COALESCE(SUM(CASE WHEN loan_status = 'CLOSED' THEN 1 ELSE 0 END), 0) AS closed
        FROM manual_lending_investment WHERE created_by=:username
        """, nativeQuery = true)
    List<Map<String, Object>> getLoanSummary(@Param("username") String username);

    @Query(value = """
        SELECT
            DATE_FORMAT(MIN(investment_date), '%b %Y') AS month,
            SUM(inv_amount) AS inv_amount_total,
            SUM(CASE WHEN loan_status = 'ACTIVE' THEN inv_amount ELSE 0 END) AS inv_amount_active,
            SUM(CASE WHEN isnpa = TRUE THEN inv_amount-amt_credit_in_bank_account ELSE 0 END) AS inv_amount_npa,
            SUM(CASE WHEN loan_status = 'CLOSED' THEN inv_amount ELSE 0 END) AS inv_amount_closed
        FROM manual_lending_investment
        WHERE created_by=:username and investment_date >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH)
        GROUP BY YEAR(investment_date), MONTH(investment_date)
        ORDER BY YEAR(investment_date), MONTH(investment_date)
        """, nativeQuery = true)
    List<Map<String, Object>> getLast12MonthInvestmentAmounts(@Param("username") String username);

    @Query(value = """
        SELECT
            COALESCE(SUM(inv_amount), 0) AS inv_amount_total,
            COALESCE(SUM(CASE WHEN loan_status = 'ACTIVE' THEN inv_amount ELSE 0 END), 0) AS inv_amount_active,
            COALESCE(SUM(CASE WHEN isnpa = TRUE THEN inv_amount-amt_credit_in_bank_account ELSE 0 END), 0) AS inv_amount_npa,
            COALESCE(SUM(CASE WHEN loan_status = 'CLOSED' THEN inv_amount ELSE 0 END), 0) AS inv_amount_closed
        FROM manual_lending_investment WHERE created_by=:username
        """, nativeQuery = true)
    List<Map<String, Object>> getInvestmentAmounts(@Param("username") String username);

    @Query(value = """
        SELECT
            DATE_FORMAT(MIN(investment_date), '%d %b') AS month,
            COUNT(*) AS total,
            SUM(CASE WHEN isnpa = TRUE THEN 1 ELSE 0 END) AS npa,
            SUM(CASE WHEN loan_status = 'ACTIVE' THEN 1 ELSE 0 END) AS active,
            SUM(CASE WHEN loan_status = 'CLOSED' THEN 1 ELSE 0 END) AS closed
        FROM manual_lending_investment
        WHERE created_by=:username and investment_date >= DATE_SUB(CURDATE(), INTERVAL 2 MONTH)
        GROUP BY MONTH(investment_date), DAY(investment_date)
        ORDER BY MONTH(investment_date), DAY(investment_date)
        """, nativeQuery = true)
    List<Map<String, Object>> getLast60DaysSummary(@Param("username") String username);

    @Query(value = """
        SELECT
            DATE_FORMAT(MIN(investment_date), '%d %b') AS month,
            SUM(inv_amount) AS inv_amount_total,
            SUM(CASE WHEN loan_status = 'ACTIVE' THEN inv_amount ELSE 0 END) AS inv_amount_active,
            SUM(CASE WHEN isnpa = TRUE THEN inv_amount-amt_credit_in_bank_account ELSE 0 END) AS inv_amount_npa,
            SUM(CASE WHEN loan_status = 'CLOSED' THEN inv_amount ELSE 0 END) AS inv_amount_closed
        FROM manual_lending_investment
        WHERE created_by=:username and investment_date >= DATE_SUB(CURDATE(), INTERVAL 2 MONTH)
        GROUP BY MONTH(investment_date), DAY(investment_date)
        ORDER BY MONTH(investment_date), DAY(investment_date)
        """, nativeQuery = true)
    List<Map<String, Object>> getLast60DaysInvestmentAmounts(@Param("username") String username);
}
