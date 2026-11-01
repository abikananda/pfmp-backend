package com.techconsulting.pfmp.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Repository
public class CommonRepository {
    @PersistenceContext
    EntityManager entityManager;
    @Autowired
    RoleRepository roleRepository;

    @Transactional
    public void deleteData() {
        entityManager.createNativeQuery("DELETE FROM npa_loan_log")
                .executeUpdate();
        entityManager.clear();
        System.out.println("✅ npa_loan_log clean successfully!");
    }
    @Transactional
    public void insertRoles() {
        if (roleRepository.count() == 0){
            entityManager.createNativeQuery("INSERT INTO roles(name) VALUES('ROLE_USER')").executeUpdate();
            entityManager.createNativeQuery("INSERT INTO roles(name) VALUES('ROLE_PM')").executeUpdate();
            entityManager.createNativeQuery("INSERT INTO roles(name) VALUES('ROLE_ADMIN')").executeUpdate();
            System.out.println("✅ Roles inserted successfully!");
        }else {
            System.out.println("✅ Roles already exist!");
        }
    }
    @Transactional
    public void createVew() {
        entityManager.createNativeQuery("DROP VIEW IF EXISTS npa_loans").executeUpdate();
        entityManager.createNativeQuery(
                "CREATE VIEW npa_loans AS " +
                        "SELECT * FROM manual_lending_investment inv WHERE inv.isnpa = TRUE"
        ).executeUpdate();
        System.out.println("✅ npa_loans view created successfully!");
    }
    @Transactional
    public void createNpaLoanLogTable(){
        entityManager.createNativeQuery("DROP TABLE IF EXISTS npa_loan_log").executeUpdate();
        entityManager.createNativeQuery("CREATE TABLE npa_loan_log LIKE manual_lending_investment").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE npa_loan_log MODIFY COLUMN id INT").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE npa_loan_log DROP PRIMARY KEY").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE npa_loan_log DROP INDEX FK_ManualLendingInvestment_portfolio").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE npa_loan_log DROP INDEX UK_ManualLendingInvestment_scheme_portfolio").executeUpdate();
        System.out.println("✅ npa_loan_log table created successfully!");
    }
    @Transactional
    public void createTriggers(){
        entityManager.createNativeQuery("DROP TRIGGER IF EXISTS before_update_manual_lending_investment").executeUpdate();
        entityManager.createNativeQuery("CREATE TRIGGER before_update_manual_lending_investment " +
                "BEFORE UPDATE ON manual_lending_investment " +
                "FOR EACH ROW " +
                "BEGIN " +
                "    INSERT INTO npa_loan_log " +
                "    SELECT * FROM manual_lending_investment WHERE scheme_id = OLD.scheme_id AND portfolio_id=OLD.portfolio_id; " +
                "END;").executeUpdate();
        entityManager.createNativeQuery("DROP TRIGGER IF EXISTS after_update_manual_lending_investment").executeUpdate();
        entityManager.createNativeQuery("CREATE TRIGGER after_update_manual_lending_investment " +
                "AFTER UPDATE ON manual_lending_investment " +
                "FOR EACH ROW " +
                "BEGIN " +
                "    INSERT INTO npa_loan_log " +
                "    SELECT * FROM manual_lending_investment WHERE scheme_id = OLD.scheme_id AND portfolio_id=OLD.portfolio_id; " +
                "END;").executeUpdate();
        System.out.println("✅ Triggers created successfully!");
        }

    @Transactional
    public List<String> getLoanIdFromBorrowerLoans() {
        String sql = "SELECT loanId FROM borrower_loans";
        return entityManager.createNativeQuery(sql, String.class).getResultList();
    }

    @Transactional
    public void insertLoanTransitionMetrics() {
        String sql = """
            INSERT INTO loan_transition_metrics (
                loans_to_npa,
                loans_npa_to_non_npa,
                loans_to_closed,
                total_npa_loans,
                total_non_npa_loans,
                total_amount_difference,
                created_date,
                created_by
            )
            WITH ranked_logs AS (
                SELECT 
                    id,
                    amt_credit_in_bank_account,
                    isnpa,
                    loan_status,
                    last_updated,
                    created_by,
                    ROW_NUMBER() OVER (PARTITION BY id ORDER BY last_updated DESC) AS rn
                FROM npa_loan_log
            ),
            latest AS (
                SELECT * FROM ranked_logs WHERE rn = 1
            ),
            previous AS (
                SELECT * FROM ranked_logs WHERE rn = 2
            ),
            transitions AS (
                SELECT 
                    l.id,
                    l.amt_credit_in_bank_account AS latest_amount,
                    p.amt_credit_in_bank_account AS prev_amount,
                    l.isnpa AS latest_isnpa,
                    p.isnpa AS prev_isnpa,
                    l.loan_status AS latest_status,
                    p.loan_status AS prev_status,
                    (l.amt_credit_in_bank_account - COALESCE(p.amt_credit_in_bank_account, 0)) AS amount_difference,
                    l.created_by
                FROM latest l
                LEFT JOIN previous p ON l.id = p.id
            )
            SELECT
                SUM(CASE WHEN prev_isnpa = 0 AND latest_isnpa = 1 THEN 1 ELSE 0 END) AS loans_to_npa,
                SUM(CASE WHEN prev_isnpa = 1 AND latest_isnpa = 0 THEN 1 ELSE 0 END) AS loans_npa_to_non_npa,
                SUM(CASE WHEN (prev_status NOT LIKE '%CLOSED%' OR prev_status IS NULL)
                          AND latest_status LIKE '%CLOSED%' THEN 1 ELSE 0 END) AS loans_to_closed,
                SUM(CASE WHEN latest_isnpa = 1 THEN 1 ELSE 0 END) AS total_npa_loans,
                SUM(CASE WHEN latest_isnpa = 0 THEN 1 ELSE 0 END) AS total_non_npa_loans,
                SUM(amount_difference) AS total_amount_difference,
                CURRENT_TIMESTAMP AS created_date,
                MAX(created_by) AS created_by
            FROM transitions
            """;
        entityManager.createNativeQuery(sql).executeUpdate();
    }

    @Transactional
    public void createLoanTransitionMetricsTable() {
        String sql = """
            CREATE TABLE loan_transition_metrics (
                id SERIAL PRIMARY KEY,
                loans_to_npa INT,
                loans_npa_to_non_npa INT,
                loans_to_closed INT,
                total_npa_loans INT,
                total_non_npa_loans INT,
                total_amount_difference NUMERIC,
                created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                created_by VARCHAR(100)
            )
            """;
        entityManager.createNativeQuery("DROP TABLE IF EXISTS loan_transition_metrics").executeUpdate();
        entityManager.createNativeQuery(sql).executeUpdate();
    }

    @Transactional
    public void syncParticularWithBorrowerLoans() {
        String sql = """
            UPDATE manual_lending_investment a
            JOIN borrower_loans b ON a.scheme_id = b.loanId
            SET a.particular = b.name
            """;
        entityManager.createNativeQuery(sql).executeUpdate();
    }
    @Transactional
    public String getFirstNameByLoanId(String loanId) {
        String sql = """
        SELECT name
        FROM borrower_loans
        WHERE loanId = :loanId
        LIMIT 1
        """;

        List<String> results = entityManager.createNativeQuery(sql)
                .setParameter("loanId", loanId)
                .getResultList();

        return results.isEmpty() ? null : results.get(0);
    }

    @Transactional
    public void createTrustedBorrowerTable() {
        String sql = """
            CREATE TABLE trusted_borrower (
                id INT AUTO_INCREMENT PRIMARY KEY,
                loanId VARCHAR(20),
                creditScore DECIMAL(5,1),
                lendenScore DECIMAL(5,1),
                income DECIMAL(10,2),
                loanAmount DECIMAL(10,2),
                borrowerType ENUM('SALARIED', 'SELF_EMPLOYED'),
                interestRate DECIMAL(5,2),
                name VARCHAR(100),
                age INT,
                lendingAmount DECIMAL(10,2),
                tenure INT,
                created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;
        entityManager.createNativeQuery("DROP TABLE IF EXISTS trusted_borrower").executeUpdate();
        entityManager.createNativeQuery(sql).executeUpdate();
    }

    @Transactional
    public void createDefaultBorrowersTable() {
        String sql = """
            CREATE TABLE default_borrowers (
                id INT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                created_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;
        entityManager.createNativeQuery("DROP TABLE IF EXISTS default_borrowers").executeUpdate();
        entityManager.createNativeQuery(sql).executeUpdate();
    }

    @Transactional
    public void createBorrowerLoansTable() {
        String sql = """
            CREATE TABLE borrower_loans (
                id INT AUTO_INCREMENT PRIMARY KEY,
                loanId VARCHAR(20),
                creditScore DECIMAL(5,1),
                lendenScore DECIMAL(5,1),
                income DECIMAL(10,2),
                loanAmount DECIMAL(10,2),
                borrowerType ENUM('SALARIED', 'SELF_EMPLOYED'),
                interestRate DECIMAL(5,2),
                name VARCHAR(100),
                age INT,
                lendingAmount DECIMAL(10,2),
                tenure INT,
                created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                user VARCHAR(50)
            )
            """;
        entityManager.createNativeQuery("DROP TABLE IF EXISTS borrower_loans").executeUpdate();
        entityManager.createNativeQuery(sql).executeUpdate();
    }

}
