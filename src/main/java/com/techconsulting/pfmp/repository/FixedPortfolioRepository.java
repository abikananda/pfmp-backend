package com.techconsulting.pfmp.repository;

import com.techconsulting.pfmp.domain.FixedPortfolio;
import com.techconsulting.pfmp.domain.InvestmentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FixedPortfolioRepository extends JpaRepository<FixedPortfolio, Long> {
    Optional<FixedPortfolio> findByInvestmentTypeAndUserId(InvestmentType investmentType, Long userId);
}
 