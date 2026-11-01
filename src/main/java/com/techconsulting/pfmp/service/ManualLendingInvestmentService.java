package com.techconsulting.pfmp.service;

import com.techconsulting.pfmp.domain.*;
import com.techconsulting.pfmp.exception.InvestmentException;
import com.techconsulting.pfmp.repository.CommonRepository;
import com.techconsulting.pfmp.repository.ManualLendingInvestmentRepository;
import com.techconsulting.pfmp.repository.ManualLendingPortfolioRepository;
import com.techconsulting.pfmp.repository.UserRepository;
import com.techconsulting.pfmp.security.services.UserDetailsImpl;
import com.techconsulting.pfmp.utility.SecurityUtil;
import io.opentelemetry.api.internal.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service("MANUAL_LENDING")
public class ManualLendingInvestmentService {

    private static final Logger logger = LoggerFactory.getLogger(ManualLendingInvestmentService.class);

    @Autowired
    ManualLendingPortfolioRepository manualLendingPortfolioRepository;
    @Autowired
    ManualLendingInvestmentRepository manualLendingInvestmentRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CommonRepository commonRepository;


    public void updateInvestment(ManualLendingInvestment manualLendingInvestment) throws InvestmentException {
        UserDetailsImpl userDetails = SecurityUtil.getLoggedInUser();
        Optional<ManualLendingPortfolio> portfolio = manualLendingPortfolioRepository.findByInvestmentTypeAndUserId(InvestmentType.MANUAL_LENDING, userDetails.getId());
        if (portfolio.isEmpty()){
            System.out.println("Portfolio not exist");
            throw new InvestmentException("Portfolio not exist");
        }
        ManualLendingInvestment inv = manualLendingInvestmentRepository.findBySchemeIdIgnoreCaseAndManualLendingPortfolioId(manualLendingInvestment.getSchemeId(), portfolio.get().getId());
        if (ObjectUtils.isEmpty(inv)){
            throw new InvestmentException("Loan not present");
        } else {
            inv.setDpd(manualLendingInvestment.getDpd());
            inv.setComments(manualLendingInvestment.getComments());
            manualLendingInvestmentRepository.save(inv);
        }
    }

    @Transactional
    public void uploadManualLendingAndInvest(List<InvestedLoan> loans) throws InvestmentException {
        logger.info("========== START uploadManualLendingAndInvest ==========");
        logger.info("Total loans to process: {}", loans.size());

        UserDetailsImpl userDetails = SecurityUtil.getLoggedInUser();
        Optional<ManualLendingPortfolio> portfolio = manualLendingPortfolioRepository.findByInvestmentTypeAndUserId(InvestmentType.MANUAL_LENDING, userDetails.getId());
        if (portfolio.isEmpty()){
            logger.error("Portfolio not exist for user: {}", userDetails.getId());
            System.out.println("Portfolio not exist");
            throw new InvestmentException("Portfolio not exist");
        }
        logger.info("Portfolio found with ID: {}, Investment Principal: {}", portfolio.get().getId(), portfolio.get().getInvestmentPrincipalAmt());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        int loanIndex = 0;
        for (InvestedLoan loan : loans) {
            loanIndex++;
            try {
                logger.debug("Processing loan #{}: SchemeId={}, InvestedAmount={}, AmountReceived={}, Tenure={}, DPD={}",
                    loanIndex, loan.getSchemeId(), loan.getInvestedAmount(), loan.getAmountReceived(), loan.getTenure(), loan.getDpd());

                if (!loanExists(loan.getSchemeId(), portfolio.get().getId())) {
                    ManualLendingInvestment dhanInvestment = new ManualLendingInvestment();
                    dhanInvestment.setSchemeId(loan.getSchemeId());
                    dhanInvestment.setParticular("");
                    dhanInvestment.setInvAmount(loan.getInvestedAmount());
                    dhanInvestment.setMarketValue(loan.getInvestedAmount());
                    dhanInvestment.setInvestmentStatus(InvestmentStatus.NOT_SOLD);
                    LocalDate investmentDate = StringUtils.isNullOrEmpty(loan.getInvestmentDate()) ? LocalDate.now() : LocalDate.parse(loan.getInvestmentDate(), formatter);
                    dhanInvestment.setInvestmentDate(investmentDate);
                    dhanInvestment.setLastEMIPaidOn(investmentDate);
                    dhanInvestment.setInvReturnAmt(0.0);
                    dhanInvestment.setInvReturnPer(0.0);
                    dhanInvestment.setAmtCreditInBankAccount(0.0);
                    dhanInvestment.setTenure(loan.getTenure());
                    dhanInvestment.setLoanStatus(InvestmentStatus.ACTIVE);
                    dhanInvestment.setDpd((int) Double.parseDouble(loan.getDpd()));
                    dhanInvestment.setNPA(false);
                    dhanInvestment.setComments("New Loan");
                    dhanInvestment.setManualLendingPortfolio(portfolio.get());
                    logger.debug("Before getFirstNameByLoanId for SchemeId: {}", loan.getSchemeId());
                    String name = commonRepository.getFirstNameByLoanId(loan.getSchemeId());
                    if (!StringUtils.isNullOrEmpty(name)){
                        dhanInvestment.setParticular(name);
                    }
                    manualLendingInvestmentRepository.save(dhanInvestment);
                    logger.debug("New loan created and saved for SchemeId: {}", loan.getSchemeId());
                }
                ManualLendingInvestment dhanInvestment = manualLendingInvestmentRepository.findBySchemeIdIgnoreCaseAndManualLendingPortfolioId(loan.getSchemeId(), portfolio.get().getId());

                // LOG AND VALIDATE BEFORE CALCULATION
                logger.debug("Investment retrieved - InvAmount: {}, Tenure: {}", dhanInvestment.getInvAmount(), dhanInvestment.getTenure());
                if (dhanInvestment.getTenure() == null || dhanInvestment.getTenure() == 0) {
                    logger.error("CRITICAL ERROR - Tenure is null or zero! SchemeId: {}, Tenure: {}", loan.getSchemeId(), dhanInvestment.getTenure());
                    dhanInvestment.setComments("ERROR: Tenure is 0 - Cannot calculate EMI");
                    manualLendingInvestmentRepository.save(dhanInvestment);
                    continue;
                }

                double perMonthEMI = dhanInvestment.getInvAmount() / dhanInvestment.getTenure();
                logger.debug("PerMonthEMI calculated: {}, isNaN: {}", perMonthEMI, Double.isNaN(perMonthEMI));

                if (Double.isNaN(perMonthEMI) || Double.isInfinite(perMonthEMI)) {
                    logger.error("CRITICAL ERROR - NaN/Infinite PerMonthEMI! SchemeId: {}, InvAmount: {}, Tenure: {}",
                        loan.getSchemeId(), dhanInvestment.getInvAmount(), dhanInvestment.getTenure());
                    dhanInvestment.setComments("ERROR: NaN in EMI calculation");
                    manualLendingInvestmentRepository.save(dhanInvestment);
                    continue;
                }
            Double amountReceived = loan.getAmountReceived() != null ? loan.getAmountReceived() : 0.0;
            if (!Objects.equals(dhanInvestment.getAmtCreditInBankAccount(), amountReceived)){
                double interestAmt = 0;
                if (perMonthEMI <= amountReceived) {
                    double totEmi = 0;
                    double totAmt = perMonthEMI;
                    while (totAmt <= amountReceived) {
                        totEmi = totAmt;
                        totAmt = totAmt + perMonthEMI;
                    }
                    interestAmt = amountReceived - totEmi;
                    dhanInvestment.setComments("");
                    dhanInvestment.setLastEMIPaidOn(LocalDate.now());
                }
                else {
                    dhanInvestment.setComments("amount received "+amountReceived+" is less than per month EMI "+perMonthEMI);
                }

                if (amountReceived > dhanInvestment.getInvAmount()){
                    dhanInvestment.setInvestmentStatus(InvestmentStatus.SOLD);
                }
                dhanInvestment.setAmtCreditInBankAccount(amountReceived);
                dhanInvestment.setMarketValue(dhanInvestment.getInvAmount() + interestAmt);
                dhanInvestment.setInvReturnAmt(interestAmt);

                // LOG AND VALIDATE RETURN PERCENTAGE
                logger.debug("Before InvReturnPer calc - InvReturnAmt: {}, InvAmount: {}", dhanInvestment.getInvReturnAmt(), dhanInvestment.getInvAmount());
                if (dhanInvestment.getInvAmount() == null || dhanInvestment.getInvAmount() == 0) {
                    logger.error("CRITICAL ERROR - InvAmount is zero/null for return percentage calculation! SchemeId: {}, InvAmount: {}",
                        loan.getSchemeId(), dhanInvestment.getInvAmount());
                    dhanInvestment.setInvReturnPer(0.0);
                } else {
                    double returnPer = dhanInvestment.getInvReturnAmt() * 100 / dhanInvestment.getInvAmount();
                    logger.debug("InvReturnPer calculated: {}, isNaN: {}", returnPer, Double.isNaN(returnPer));
                    if (Double.isNaN(returnPer) || Double.isInfinite(returnPer)) {
                        logger.error("CRITICAL ERROR - NaN/Infinite InvReturnPer! SchemeId: {}, InvReturnAmt: {}, InvAmount: {}",
                            loan.getSchemeId(), dhanInvestment.getInvReturnAmt(), dhanInvestment.getInvAmount());
                        dhanInvestment.setInvReturnPer(0.0);
                    } else {
                        dhanInvestment.setInvReturnPer(returnPer);
                    }
                }
            }
            if (loan.getLoanStatus().equals("ACTIVE")) {
                dhanInvestment.setLoanStatus(InvestmentStatus.ACTIVE);
            } else if (loan.getLoanStatus().equals("CLOSED")) {
                dhanInvestment.setLoanStatus(InvestmentStatus.CLOSED);
            } else {
                System.out.println("Loan with other status");
            }
            //npa logic
            //make all as no npa by default
            dhanInvestment.setNPA(false);
            double marketValue = BigDecimal.valueOf(dhanInvestment.getMarketValue()).setScale(3, RoundingMode.DOWN).doubleValue();
            double amtCreditInBankAccount = BigDecimal.valueOf(dhanInvestment.getAmtCreditInBankAccount()).setScale(3, RoundingMode.DOWN).doubleValue();
            if (dhanInvestment.getDpd() > 7){
                //dhanInvestment.setNPA(true);
                dhanInvestment.setComments("DPD is "+dhanInvestment.getDpd());
            } else if ((amtCreditInBankAccount < marketValue) && (dhanInvestment.getLoanStatus().equals(InvestmentStatus.CLOSED))) {
                dhanInvestment.setNPA(true);
                dhanInvestment.setComments("Loan closed before receiving investment amount");
            }
            LocalDate today = LocalDate.now();
            long monthsBetween = ChronoUnit.MONTHS.between(dhanInvestment.getLastEMIPaidOn(), today);
            long monthsFromInvestment = ChronoUnit.MONTHS.between(dhanInvestment.getInvestmentDate(), today);
            long daysBetween = ChronoUnit.DAYS.between(dhanInvestment.getLastEMIPaidOn(), today);
            if ((amtCreditInBankAccount < marketValue) && (monthsFromInvestment > dhanInvestment.getTenure())){
                dhanInvestment.setNPA(true);
                dhanInvestment.setComments(monthsFromInvestment+" is out of tenure "+ dhanInvestment.getTenure()+" months");
            }
            if ((dhanInvestment.getAmtCreditInBankAccount() < perMonthEMI) && (monthsBetween > 1)  && (monthsFromInvestment <= dhanInvestment.getTenure())){
                dhanInvestment.setNPA(true);
                dhanInvestment.setComments("No EMI in last "+monthsBetween+" months");
            }
            if ((dhanInvestment.getAmtCreditInBankAccount() < perMonthEMI) && (daysBetween > 35) && (monthsBetween <= 1)){
                dhanInvestment.setNPA(true);
                dhanInvestment.setComments("No EMI in last "+daysBetween+" days");
            }
            if (loan.getLoanStatus().equals("CLOSED") && amountReceived < loan.getInvestedAmount()) {
                dhanInvestment.setMarketValue(amountReceived);
                dhanInvestment.setInvReturnAmt(amountReceived - dhanInvestment.getInvAmount());
                logger.debug("CLOSED Loan (partial return) - Before InvReturnPer calc - InvAmount: {}, InvReturnAmt: {}",
                    dhanInvestment.getInvAmount(), dhanInvestment.getInvReturnAmt());
                if (dhanInvestment.getInvAmount() != null && dhanInvestment.getInvAmount() != 0) {
                    double returnPer = dhanInvestment.getInvReturnAmt() * 100 / dhanInvestment.getInvAmount();
                    logger.debug("CLOSED Loan return percentage: {}, isNaN: {}", returnPer, Double.isNaN(returnPer));
                    if (Double.isNaN(returnPer) || Double.isInfinite(returnPer)) {
                        logger.error("CRITICAL ERROR - NaN in CLOSED loan return! SchemeId: {}, InvReturnAmt: {}, InvAmount: {}",
                            loan.getSchemeId(), dhanInvestment.getInvReturnAmt(), dhanInvestment.getInvAmount());
                        dhanInvestment.setInvReturnPer(0.0);
                    } else {
                        dhanInvestment.setInvReturnPer(returnPer);
                    }
                } else {
                    logger.error("CRITICAL ERROR - InvAmount is zero/null for CLOSED loan! SchemeId: {}", loan.getSchemeId());
                    dhanInvestment.setInvReturnPer(0.0);
                }
                dhanInvestment.setNPA(true);
                dhanInvestment.setComments("Loan closed before receiving investment amount. so NPA");
                dhanInvestment.setInvestmentStatus(InvestmentStatus.SOLD);
            }else if (loan.getLoanStatus().equals("CLOSED")) {
                dhanInvestment.setMarketValue(amountReceived);
                dhanInvestment.setInvReturnAmt(amountReceived - dhanInvestment.getInvAmount());
                logger.debug("CLOSED Loan (full return) - Before InvReturnPer calc - InvAmount: {}, InvReturnAmt: {}",
                    dhanInvestment.getInvAmount(), dhanInvestment.getInvReturnAmt());
                if (dhanInvestment.getInvAmount() != null && dhanInvestment.getInvAmount() != 0) {
                    double returnPer = dhanInvestment.getInvReturnAmt() * 100 / dhanInvestment.getInvAmount();
                    logger.debug("CLOSED Loan return percentage: {}, isNaN: {}", returnPer, Double.isNaN(returnPer));
                    if (Double.isNaN(returnPer) || Double.isInfinite(returnPer)) {
                        logger.error("CRITICAL ERROR - NaN in CLOSED loan return! SchemeId: {}, InvReturnAmt: {}, InvAmount: {}",
                            loan.getSchemeId(), dhanInvestment.getInvReturnAmt(), dhanInvestment.getInvAmount());
                        dhanInvestment.setInvReturnPer(0.0);
                    } else {
                        dhanInvestment.setInvReturnPer(returnPer);
                    }
                } else {
                    logger.error("CRITICAL ERROR - InvAmount is zero/null for CLOSED loan! SchemeId: {}", loan.getSchemeId());
                    dhanInvestment.setInvReturnPer(0.0);
                }
                dhanInvestment.setComments("LOAN CLOSED");
            }
            if (dhanInvestment.getLoanStatus().equals(InvestmentStatus.CLOSED) && dhanInvestment.getInvestmentStatus().equals(InvestmentStatus.SOLD) && dhanInvestment.getMarketValue()>dhanInvestment.getInvAmount()) {
                dhanInvestment.setComments(dhanInvestment.getComments()+": Successfully Closed");
            }
            String name = commonRepository.getFirstNameByLoanId(loan.getSchemeId());
            if (!StringUtils.isNullOrEmpty(name)){
                dhanInvestment.setParticular(name);
            }
            manualLendingInvestmentRepository.save(dhanInvestment);
            } catch (Exception e) {
                logger.error("ERROR processing loan #{} with SchemeId: {}", loanIndex, loan.getSchemeId(), e);
                throw e;
            }
        }
        System.out.println("NPA loans check completed");

        // LOG AND VALIDATE PORTFOLIO CALCULATIONS
        logger.info("========== PORTFOLIO CALCULATIONS START ==========");
        try {
            double totalInvestAmt = portfolio.get().getManualLendingInvestments().stream()
                    .map(ManualLendingInvestment::getInvAmount)
                    .mapToDouble(Double::doubleValue).sum();
            logger.debug("Total Investment Amount: {}", totalInvestAmt);
            portfolio.get().setTotalInvestmentAmt(totalInvestAmt);

            double amountCreditInBankAccount = portfolio.get().getManualLendingInvestments().stream()
                    .map(ManualLendingInvestment::getAmtCreditInBankAccount)
                    .mapToDouble(Double::doubleValue).sum();
            logger.debug("Amount Credit In Bank Account: {}", amountCreditInBankAccount);
            portfolio.get().setAmountCreditInBankAccount(amountCreditInBankAccount);

            double portfolioReturnAmt = portfolio.get().getManualLendingInvestments().stream()
                    .map(ManualLendingInvestment::getInvReturnAmt)
                    .mapToDouble(Double::doubleValue).sum();
            logger.debug("Portfolio Return Amount: {}", portfolioReturnAmt);
            portfolio.get().setPortfolioReturnAmt(portfolioReturnAmt);

            double totalFundOnLendenInvested = totalInvestAmt - amountCreditInBankAccount;
            logger.debug("Total Fund On Lenden Invested: {}", totalFundOnLendenInvested);
            portfolio.get().setTotalFundOnLendenInvested(totalFundOnLendenInvested);

            // CRITICAL: Check for division by zero before calculating return percentage
            logger.debug("Portfolio InvestmentPrincipalAmt: {}", portfolio.get().getInvestmentPrincipalAmt());
            if (portfolio.get().getInvestmentPrincipalAmt() == null || portfolio.get().getInvestmentPrincipalAmt() == 0) {
                logger.error("CRITICAL ERROR - InvestmentPrincipalAmt is zero/null! Portfolio ID: {}", portfolio.get().getId());
                portfolio.get().setPortfolioReturnPer(0.0);
            } else {
                double portfolioReturnPer = portfolioReturnAmt * 100 / portfolio.get().getInvestmentPrincipalAmt();
                logger.debug("Portfolio Return Percentage: {}, isNaN: {}", portfolioReturnPer, Double.isNaN(portfolioReturnPer));
                if (Double.isNaN(portfolioReturnPer) || Double.isInfinite(portfolioReturnPer)) {
                    logger.error("CRITICAL ERROR - NaN/Infinite PortfolioReturnPer! Portfolio ID: {}, ReturnAmt: {}, PrincipalAmt: {}",
                        portfolio.get().getId(), portfolioReturnAmt, portfolio.get().getInvestmentPrincipalAmt());
                    portfolio.get().setPortfolioReturnPer(0.0);
                } else {
                    portfolio.get().setPortfolioReturnPer(portfolioReturnPer);
                }
            }

            double amtYetToBeInvest = (portfolio.get().getInvestmentPrincipalAmt() + portfolio.get().getPortfolioReturnAmt())
                    - totalFundOnLendenInvested;
            logger.debug("Amount Yet To Be Invest: {}", amtYetToBeInvest);
            portfolio.get().setAmtYetToBeInvest(amtYetToBeInvest);

            long totalNPA = portfolio.get().getManualLendingInvestments().stream()
                    .filter(ManualLendingInvestment::isNPA).count();
            logger.debug("Total NPA: {}", totalNPA);
            portfolio.get().setTotalNPA((int) totalNPA);

            manualLendingPortfolioRepository.save(portfolio.get());
            logger.info("========== PORTFOLIO CALCULATIONS COMPLETED ==========");
        } catch (Exception e) {
            logger.error("ERROR in portfolio calculations", e);
            throw e;
        }
        logger.info("========== END uploadManualLendingAndInvest ==========");
    }

    private boolean loanExists(final String schemeId, final Long portfolioId) {
        return manualLendingInvestmentRepository.existsBySchemeIdIgnoreCaseAndManualLendingPortfolioId(schemeId, portfolioId);
    }

    public void createOrUpdatePortfolio(Double amt) {
        UserDetailsImpl userDetails = SecurityUtil.getLoggedInUser();
        Optional<ManualLendingPortfolio> portfolioData = manualLendingPortfolioRepository.findByInvestmentTypeAndUserId(InvestmentType.MANUAL_LENDING, userDetails.getId());
        if (portfolioData.isPresent()) {
            portfolioData.get().setInvestmentPrincipalAmt(portfolioData.get().getInvestmentPrincipalAmt() + amt);
            manualLendingPortfolioRepository.save(portfolioData.get());
        } else {
            ManualLendingPortfolio newDhanPortfolio = new ManualLendingPortfolio();
            newDhanPortfolio.setInvestmentType(InvestmentType.MANUAL_LENDING);
            newDhanPortfolio.setPortfolioReturnAmt(0.0);
            newDhanPortfolio.setPortfolioReturnPer(0.0);
            newDhanPortfolio.setTotalInvestmentAmt(0.0);
            newDhanPortfolio.setAmtYetToBeInvest(0.0);
            newDhanPortfolio.setAmountCreditInBankAccount(0.0);
            newDhanPortfolio.setInvestmentPrincipalAmt(0.0);
            newDhanPortfolio.setTotalFundOnLendenInvested(0.0);
            newDhanPortfolio.setTotalNPA(0);
            newDhanPortfolio.setInvestmentPrincipalAmt(amt);
            newDhanPortfolio.setUser(userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new RuntimeException("User not found with username: " + userDetails.getUsername())));
            manualLendingPortfolioRepository.save(newDhanPortfolio);
        }
    }

    public void cleanLoanLogTable() {
        commonRepository.deleteData();
    }

    public List<Map<String, Object>> getMonthlyLoanSummary() {
        UserDetailsImpl userDetails = SecurityUtil.getLoggedInUser();
        return manualLendingInvestmentRepository.getLast12MonthSummary(userDetails.getUsername());
    }
    public List<Map<String, Object>> getLoanSummary() {
        UserDetailsImpl userDetails = SecurityUtil.getLoggedInUser();
        return manualLendingInvestmentRepository.getLoanSummary(userDetails.getUsername());
    }

    public List<Map<String, Object>> getMonthlyInvestmentSummary() {
        UserDetailsImpl userDetails = SecurityUtil.getLoggedInUser();
        return manualLendingInvestmentRepository.getLast12MonthInvestmentAmounts(userDetails.getUsername());
    }
    public List<Map<String, Object>> getInvestmentSummary() {
        UserDetailsImpl userDetails = SecurityUtil.getLoggedInUser();
        return manualLendingInvestmentRepository.getInvestmentAmounts(userDetails.getUsername());
    }
    public List<Map<String, Object>> getDayWiseLoanSummary() {
        UserDetailsImpl userDetails = SecurityUtil.getLoggedInUser();
        return manualLendingInvestmentRepository.getLast60DaysSummary(userDetails.getUsername());
    }
    public List<Map<String, Object>> getDayWiseInvestmentSummary() {
        UserDetailsImpl userDetails = SecurityUtil.getLoggedInUser();
        return manualLendingInvestmentRepository.getLast60DaysInvestmentAmounts(userDetails.getUsername());
    }
}
