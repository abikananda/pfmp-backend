package com.techconsulting.pfmp.repository;


import com.techconsulting.pfmp.domain.InvestedLoan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestedLoanRepository extends JpaRepository<InvestedLoan, Long> {

    boolean existsBySchemeIdIgnoreCase(String schemeId);

    InvestedLoan findBySchemeIdIgnoreCase(String schemeId);

    InvestedLoan findByLoanIdIgnoreCase(String loanId);
}
