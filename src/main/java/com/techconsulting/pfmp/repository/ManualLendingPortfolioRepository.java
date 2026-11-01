package com.techconsulting.pfmp.repository;

import com.techconsulting.pfmp.domain.InvestmentType;
import com.techconsulting.pfmp.domain.ManualLendingPortfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ManualLendingPortfolioRepository extends JpaRepository<ManualLendingPortfolio, Long> {

    Optional<ManualLendingPortfolio> findByInvestmentTypeAndUserId(InvestmentType investmentType, Long userId);
}
