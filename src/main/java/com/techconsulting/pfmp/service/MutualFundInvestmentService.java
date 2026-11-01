package com.techconsulting.pfmp.service;

import com.techconsulting.pfmp.domain.FixedInvestment;
import com.techconsulting.pfmp.domain.FixedPortfolio;
import com.techconsulting.pfmp.domain.InvestmentStatus;
import com.techconsulting.pfmp.domain.InvestmentType;
import com.techconsulting.pfmp.exception.InvestmentException;
import com.techconsulting.pfmp.repository.FixedInvestmentRepository;
import com.techconsulting.pfmp.repository.FixedPortfolioRepository;
import com.techconsulting.pfmp.repository.UserRepository;
import com.techconsulting.pfmp.security.services.UserDetailsImpl;
import com.techconsulting.pfmp.utility.NumberToWordsConverter;
import com.techconsulting.pfmp.utility.SecurityUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDate;
import java.util.Optional;

@Service("MUTUAL_FUND")
public class MutualFundInvestmentService implements FixedInvestmentService {
    @Autowired
    FixedPortfolioRepository fixedPortfolioRepository;
    @Autowired
    FixedInvestmentRepository fixedInvestmentRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    @Qualifier("FINAL_PORTFOLIO")
    FixedInvestmentService portfolioInvestmentService;
    @PersistenceContext
    EntityManager entityManager;
    @Override
    public void calculatePortfolio(FixedPortfolio portfolio) {
        entityManager.clear();
        Optional<FixedPortfolio> portfolioData = fixedPortfolioRepository.findById(portfolio.getId());
        if (portfolioData.isPresent()) {
            portfolio = portfolioData.get();
            if (ObjectUtils.isEmpty(portfolio.getFixedInvestments())) {
                System.out.println("There is no investments yet");
            } else {
                portfolio.setTotalInvestmentAmt(portfolio.getFixedInvestments().stream().filter(a -> a.getInvestmentStatus().equals(InvestmentStatus.ACTIVE)).map(FixedInvestment::getInvAmount).mapToDouble(Double::doubleValue).sum());
                for (FixedInvestment inv : portfolio.getFixedInvestments().stream().filter(a -> a.getInvestmentStatus().equals(InvestmentStatus.ACTIVE)).toList()) {
                    inv.setInvWeight(inv.getInvAmount() / portfolio.getTotalInvestmentAmt());
                    inv.setWeightWithReturn(inv.getInvWeight() * inv.getInvReturn());
                }
                portfolio.setPortfolioReturn(portfolio.getFixedInvestments().stream().filter(a -> a.getInvestmentStatus().equals(InvestmentStatus.ACTIVE)).map(FixedInvestment::getWeightWithReturn).mapToDouble(Double::doubleValue).sum());
                fixedPortfolioRepository.save(portfolio);
            }
        }
        FixedInvestment investment = new FixedInvestment();
        investment.setParticular("Mutual Fund");
        investment.setInvAmount(portfolioData.get().getTotalInvestmentAmt());
        investment.setInvReturn(portfolioData.get().getPortfolioReturn());
        try{
            portfolioInvestmentService.updateInvestment(investment);
        } catch (InvestmentException ex){
            if ("investment not exist".equalsIgnoreCase(ex.getMessage()) || "Portfolio not exist".equalsIgnoreCase(ex.getMessage())){
                portfolioInvestmentService.doInvestment(investment);
            }
        }

    }

    @Override
    public void updateInvestment(FixedInvestment investment) throws InvestmentException {
        UserDetailsImpl userDetails = SecurityUtil.getLoggedInUser();
        Optional<FixedPortfolio> portfolioData = fixedPortfolioRepository.findByInvestmentTypeAndUserId(InvestmentType.MUTUAL_FUND, userDetails.getId());
        if (portfolioData.isEmpty()){
            System.out.println("Portfolio not exist");
            throw new InvestmentException("Portfolio not exist");
        }
        Optional<FixedInvestment> investmentData = fixedInvestmentRepository.findByParticularAndInvestmentStatusAndFixedPortfolioId(investment.getParticular(), InvestmentStatus.ACTIVE, portfolioData.get().getId());
        if (investmentData.isEmpty()) {
            System.out.println("investment not exist");
            throw new InvestmentException("investment not exist");
        } else {
            if (!ObjectUtils.isEmpty(investment.getInvestmentStatus()) && (InvestmentStatus.ACTIVE.equals(investment.getInvestmentStatus()) || InvestmentStatus.IN_ACTIVE.equals(investment.getInvestmentStatus()))) {
                investmentData.get().setInvestmentStatus(investment.getInvestmentStatus());
            }
            if (!ObjectUtils.isEmpty(investment.getInvAmount())) {
                investmentData.get().setInvAmount(investment.getInvAmount());
            }
            if (!ObjectUtils.isEmpty(investment.getInvReturn())) {
                investmentData.get().setInvReturn(investment.getInvReturn());
            }
            if (!ObjectUtils.isEmpty(investment.getOtherIncome())) {
                investmentData.get().setOtherIncome(investment.getOtherIncome());
            }

            if (!ObjectUtils.isEmpty(investment.getComments())) {
                investmentData.get().setComments(investment.getComments());
            }else {
                investmentData.get().setComments(NumberToWordsConverter.getDayWithSuffix(LocalDate.now()));
            }
            if (!ObjectUtils.isEmpty(investment.getMaturityDate())) {
                investmentData.get().setMaturityDate(investment.getMaturityDate());
            }
            fixedInvestmentRepository.save(investmentData.get());
        }
        calculatePortfolio(portfolioData.get());
    }

    @Override
    public void doInvestment(FixedInvestment investment) throws InvestmentException {
        UserDetailsImpl userDetails = SecurityUtil.getLoggedInUser();
        Optional<FixedPortfolio> portfolioData = fixedPortfolioRepository.findByInvestmentTypeAndUserId(InvestmentType.MUTUAL_FUND, userDetails.getId());
        FixedPortfolio portfolio = new FixedPortfolio();
        if (portfolioData.isEmpty()) {
            System.out.println("Portfolio not present. So creating...");
            portfolio.setPortfolioReturn(0.0);
            portfolio.setTotalInvestmentAmt(0.0);
            portfolio.setInvestmentType(InvestmentType.MUTUAL_FUND);
            portfolio.setUser(userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new RuntimeException("User not found with username: " + userDetails.getUsername())));
            portfolio = fixedPortfolioRepository.save(portfolio);
        }else {
            portfolio = portfolioData.get();
        }
        Optional<FixedInvestment> investmentData = fixedInvestmentRepository.findByParticularAndInvestmentStatusAndFixedPortfolioId(investment.getParticular(), InvestmentStatus.ACTIVE, portfolio.getId());
        if (investmentData.isPresent()) {
            System.out.println("Particular exist with ACTIVE status, so choose different name");
            throw new InvestmentException("Particular exist with ACTIVE status, so choose different name");
        } else {
            FixedInvestment fixedInvestment = new FixedInvestment();
            fixedInvestment.setParticular(investment.getParticular());
            fixedInvestment.setInvAmount(investment.getInvAmount());
            fixedInvestment.setInvReturn(investment.getInvReturn());
            fixedInvestment.setInvestmentStatus(InvestmentStatus.ACTIVE);
            fixedInvestment.setComments(NumberToWordsConverter.getDayWithSuffix(LocalDate.now()));
            fixedInvestment.setOtherIncome(0.0);
            fixedInvestment.setInvWeight(0.0);
            fixedInvestment.setWeightWithReturn(0.0);
            fixedInvestment.setFixedPortfolio(portfolio);
            fixedInvestmentRepository.save(fixedInvestment);
        }
        calculatePortfolio(portfolio);
    }
}
