package com.techconsulting.pfmp.repository;

import com.techconsulting.pfmp.domain.DhanInvestment;
import com.techconsulting.pfmp.domain.InvestmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DhanInvestmentRepository extends JpaRepository<DhanInvestment, Long> {

    Optional<DhanInvestment> findByParticularAndInvestmentStatusAndDhanPortfolioId(String particular, InvestmentStatus investmentStatus, Long id);

    Optional<DhanInvestment> findByParticularAndDhanPortfolioId(String particular, Long id);
}
