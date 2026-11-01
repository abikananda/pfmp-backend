package com.techconsulting.pfmp.repository;

import com.techconsulting.pfmp.domain.FixedInvestment;
import com.techconsulting.pfmp.domain.InvestmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FixedInvestmentRepository extends JpaRepository<FixedInvestment, Long> {

    Optional<FixedInvestment> findByParticularAndInvestmentStatusAndFixedPortfolioId(String particular, InvestmentStatus investmentStatus, Long id);
}
