package com.techconsulting.pfmp.repository;

import com.techconsulting.pfmp.domain.DhanPortfolio;
import com.techconsulting.pfmp.domain.InvestmentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DhanPortfolioRepository extends JpaRepository<DhanPortfolio, Long> {

    Optional<DhanPortfolio> findByInvestmentTypeAndUserId(InvestmentType investmentType, Long id);
}
