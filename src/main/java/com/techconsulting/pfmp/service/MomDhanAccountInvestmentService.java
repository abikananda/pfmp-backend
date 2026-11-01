package com.techconsulting.pfmp.service;

import com.techconsulting.pfmp.domain.DhanInvestment;
import com.techconsulting.pfmp.domain.DhanPortfolio;
import com.techconsulting.pfmp.domain.InvestmentStatus;
import com.techconsulting.pfmp.domain.InvestmentType;
import com.techconsulting.pfmp.exception.InvestmentException;
import com.techconsulting.pfmp.repository.DhanInvestmentRepository;
import com.techconsulting.pfmp.repository.DhanPortfolioRepository;
import com.techconsulting.pfmp.repository.UserRepository;
import com.techconsulting.pfmp.security.services.UserDetailsImpl;
import com.techconsulting.pfmp.utility.Base64Util;
import com.techconsulting.pfmp.utility.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service("MOM_DHAN_ACCOUNT")
public class MomDhanAccountInvestmentService {

    @Autowired
    DhanPortfolioRepository dhanPortfolioRepository;
    @Autowired
    DhanInvestmentRepository dhanInvestmentRepository;
    @Autowired
    UserRepository userRepository;

    private DhanPortfolio initializePortfolio(double amount) {
        DhanPortfolio dhanPortfolio = new DhanPortfolio();
        dhanPortfolio.setInvestmentType(InvestmentType.MOM_DHAN_ACCOUNT);
        dhanPortfolio.setAmountLoadedToWallet(amount);
        //initialize other so it will not throw null pointer
        dhanPortfolio.setAmountTransferToBank(0.0);
        dhanPortfolio.setTotalExcessReturnAmt(0.0);
        dhanPortfolio.setActualAmtInWallet(0.0);
        dhanPortfolio.setTotalInvestmentAmt(0.0);
        dhanPortfolio.setChargerAmt(0.0);
        dhanPortfolio.setPortfolioReturn(0.0);
        dhanPortfolio.setTotalReInvestAmt(0.0);
        dhanPortfolio.setTotalFundInWallet(0.0);
        UserDetailsImpl userDetails = SecurityUtil.getLoggedInUser();
        dhanPortfolio.setUser(userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found with username: " + userDetails.getUsername())));
        return dhanPortfolio;
    }

    private DhanInvestment initializeInvestment(DhanInvestment dhanInvestmentData, DhanPortfolio dhanPortfolio) {
        DhanInvestment dhanInvestment = new DhanInvestment();
        Optional<DhanInvestment> investmentData = dhanInvestmentRepository.findByParticularAndDhanPortfolioId(dhanInvestmentData.getParticular(), dhanPortfolio.getId());
        if (investmentData.isPresent()) {
            System.out.println("Particular exist, so choose different name");
            dhanInvestmentData.setParticular(dhanInvestmentData.getParticular() + "_" + Base64Util.generateRandomValue(5));
        }
        dhanInvestment.setParticular(dhanInvestmentData.getParticular());
        dhanInvestment.setInvAmount(dhanInvestmentData.getInvAmount());
        dhanInvestment.setMarketValue(dhanInvestmentData.getInvAmount());
        dhanInvestment.setOtherIncome(0.0);
        dhanInvestment.setReinvestedAmt(0.0);
        dhanInvestment.setExcessReturn(0.0);
        dhanInvestment.setInvWeight(0.0);
        dhanInvestment.setInvReturn(0.0);
        dhanInvestment.setWeightWithReturn(0.0);
        dhanInvestment.setInvestmentStatus(InvestmentStatus.NOT_SOLD);
        dhanInvestment.setDhanPortfolio(dhanPortfolio);
        return dhanInvestment;
    }


    public void calculatePortfolio() throws InvestmentException {
        UserDetailsImpl userDetails = SecurityUtil.getLoggedInUser();
        Optional<DhanPortfolio> portfolioData = dhanPortfolioRepository.findByInvestmentTypeAndUserId(InvestmentType.MOM_DHAN_ACCOUNT, userDetails.getId());
        if (portfolioData.isPresent()) {
            DhanPortfolio dhanPortfolio = portfolioData.get();
            if (ObjectUtils.isEmpty(dhanPortfolio.getDhanInvestments())) {
                //no investments yet
                dhanPortfolio.setTotalFundInWallet(dhanPortfolio.getAmountLoadedToWallet() - dhanPortfolio.getTotalInvestmentAmt() - dhanPortfolio.getAmountTransferToBank());
                dhanPortfolio.setChargerAmt(dhanPortfolio.getTotalFundInWallet() - dhanPortfolio.getActualAmtInWallet());
            } else {
                dhanPortfolio.setTotalReInvestAmt(dhanPortfolio.getDhanInvestments().stream().map(DhanInvestment::getReinvestedAmt).mapToDouble(Double::doubleValue).sum());
                dhanPortfolio.setTotalInvestmentAmt(dhanPortfolio.getDhanInvestments().stream().map(DhanInvestment::getInvAmount).mapToDouble(Double::doubleValue).sum() - dhanPortfolio.getTotalReInvestAmt());
                for (DhanInvestment inv : dhanPortfolio.getDhanInvestments()) {
                    inv.setExcessReturn((inv.getMarketValue() - inv.getInvAmount())+inv.getOtherIncome());
                    inv.setInvWeight(inv.getInvAmount() / dhanPortfolio.getDhanInvestments().stream().map(DhanInvestment::getInvAmount).mapToDouble(Double::doubleValue).sum());
                    inv.setInvReturn((inv.getExcessReturn() / inv.getInvAmount()) * 100);
                    inv.setWeightWithReturn(inv.getInvWeight() * inv.getInvReturn());
                    inv.setReinvestedAmt(inv.getInvestmentStatus().equals(InvestmentStatus.SOLD) ? inv.getMarketValue()+inv.getOtherIncome() : 0.0);
                    dhanInvestmentRepository.save(inv);
                }
                dhanPortfolio.setTotalFundInWallet(dhanPortfolio.getAmountLoadedToWallet() - dhanPortfolio.getTotalInvestmentAmt() - dhanPortfolio.getAmountTransferToBank());
                dhanPortfolio.setTotalExcessReturnAmt(dhanPortfolio.getDhanInvestments().stream().map(DhanInvestment::getExcessReturn).mapToDouble(Double::doubleValue).sum());
                dhanPortfolio.setPortfolioReturn((dhanPortfolio.getTotalExcessReturnAmt() / (dhanPortfolio.getTotalInvestmentAmt() + dhanPortfolio.getTotalFundInWallet())) * 100);
                dhanPortfolio.setChargerAmt(dhanPortfolio.getTotalFundInWallet() - dhanPortfolio.getActualAmtInWallet());
            }
            dhanPortfolioRepository.save(dhanPortfolio);
        } else {
            System.out.println("Portfolio not exist");
            throw new InvestmentException("Portfolio not exist");
        }
    }


    public void updateInvestment(DhanInvestment dhanInvestment) throws InvestmentException {
        UserDetailsImpl userDetails = SecurityUtil.getLoggedInUser();
        Optional<DhanPortfolio> portfolio = dhanPortfolioRepository.findByInvestmentTypeAndUserId(InvestmentType.MOM_DHAN_ACCOUNT, userDetails.getId());
        if (portfolio.isEmpty()){
            System.out.println("Portfolio not exist");
            throw new InvestmentException("Portfolio not exist");
        }
        Optional<DhanInvestment> investmentData = dhanInvestmentRepository.findByParticularAndInvestmentStatusAndDhanPortfolioId(dhanInvestment.getParticular(), InvestmentStatus.NOT_SOLD,portfolio.get().getId());
        if (investmentData.isEmpty()) {
            System.out.println("No investment to update");
            throw new InvestmentException("No investment to update");
        } else {
            if (!ObjectUtils.isEmpty(dhanInvestment.getInvestmentStatus()) && (InvestmentStatus.NOT_SOLD.equals(dhanInvestment.getInvestmentStatus()) || InvestmentStatus.SOLD.equals(dhanInvestment.getInvestmentStatus()))) {
                investmentData.get().setInvestmentStatus(dhanInvestment.getInvestmentStatus());
            }
            if (!ObjectUtils.isEmpty(dhanInvestment.getMarketValue())) {
                investmentData.get().setMarketValue(dhanInvestment.getMarketValue());
            }
            if (!ObjectUtils.isEmpty(dhanInvestment.getInvAmount())) {
                investmentData.get().setInvAmount(dhanInvestment.getInvAmount());
            }
            if (!ObjectUtils.isEmpty(dhanInvestment.getOtherIncome())) {
                investmentData.get().setOtherIncome(dhanInvestment.getOtherIncome());
            }
            investmentData.get().setMaturityDate(dhanInvestment.getMaturityDate());
            dhanInvestmentRepository.save(investmentData.get());
        }
        calculatePortfolio();
        //portfolioData.get().setTotalFundInWallet(portfolioData.get().getTotalFundInWallet() + investment.getMarketValue());
    }


    public void updateActualWalletBalance(double amount) throws InvestmentException {
        UserDetailsImpl userDetails = SecurityUtil.getLoggedInUser();
        Optional<DhanPortfolio> portfolioData = dhanPortfolioRepository.findByInvestmentTypeAndUserId(InvestmentType.MOM_DHAN_ACCOUNT, userDetails.getId());
        if (portfolioData.isPresent()) {
            portfolioData.get().setActualAmtInWallet(amount);
            dhanPortfolioRepository.save(portfolioData.get());
            calculatePortfolio();
        } else {
            System.out.println("Portfolio not exist");
            throw new InvestmentException("Portfolio not exist");
        }
    }

    public void loadAmountToPortfolio(double amount) throws InvestmentException {
        UserDetailsImpl userDetails = SecurityUtil.getLoggedInUser();
        Optional<DhanPortfolio> portfolioData = dhanPortfolioRepository.findByInvestmentTypeAndUserId(InvestmentType.MOM_DHAN_ACCOUNT, userDetails.getId());
        if (portfolioData.isPresent()) {
            portfolioData.get().setAmountLoadedToWallet(portfolioData.get().getAmountLoadedToWallet() + amount);
            dhanPortfolioRepository.save(portfolioData.get());
        } else {
            DhanPortfolio dhanPortfolio = initializePortfolio(amount);
            dhanPortfolioRepository.save(dhanPortfolio);
        }
        calculatePortfolio();
    }


    public void withdrawFromWalletToBank(double amount) throws InvestmentException {
        UserDetailsImpl userDetails = SecurityUtil.getLoggedInUser();
        Optional<DhanPortfolio> portfolioData = dhanPortfolioRepository.findByInvestmentTypeAndUserId(InvestmentType.MOM_DHAN_ACCOUNT, userDetails.getId());
        if (portfolioData.isPresent()) {
            if (portfolioData.get().getActualAmtInWallet() >= amount) {
                portfolioData.get().setTotalFundInWallet(portfolioData.get().getTotalFundInWallet() - amount);
                portfolioData.get().setAmountTransferToBank(portfolioData.get().getAmountTransferToBank() + amount);
                dhanPortfolioRepository.save(portfolioData.get());
                calculatePortfolio();
            } else {
                System.out.println("No sufficient balance to withdraw " + portfolioData.get().getActualAmtInWallet());
                throw new InvestmentException("No sufficient balance to withdraw " + portfolioData.get().getActualAmtInWallet());
            }
        } else {
            System.out.println("Portfolio not exist");
            throw new InvestmentException("Portfolio not exist");
        }
    }


    @Transactional
    public void doInvestment(DhanInvestment dhanInvestmentData) throws InvestmentException {
        UserDetailsImpl userDetails = SecurityUtil.getLoggedInUser();
        Optional<DhanPortfolio> portfolioData = dhanPortfolioRepository.findByInvestmentTypeAndUserId(InvestmentType.MOM_DHAN_ACCOUNT, userDetails.getId());
        if (portfolioData.isPresent()) {
            Optional<DhanInvestment> investmentData2 = dhanInvestmentRepository.findByParticularAndInvestmentStatusAndDhanPortfolioId(dhanInvestmentData.getParticular(), InvestmentStatus.NOT_SOLD, portfolioData.get().getId());
            if (investmentData2.isPresent()) {
                System.out.println("Particular exist with NOT_SOLD status, so call update to update the stock");
                throw new InvestmentException("Particular exist with NOT_SOLD status, so call update to update the stock");
            } else {
                if (dhanInvestmentData.getInvAmount() > portfolioData.get().getActualAmtInWallet()) {
                    System.out.println("In-sufficient balance in wallet");
                    if (dhanInvestmentData.getInvAmount() > (portfolioData.get().getActualAmtInWallet() + portfolioData.get().getAmountTransferToBank())) {
                        System.out.println("In-sufficient balance in wallet and bank combine");
                        throw new InvestmentException("In-sufficient balance in wallet and bank combine");
                    } else {
                        double amtToDeductFromBank = dhanInvestmentData.getInvAmount() - portfolioData.get().getTotalFundInWallet();
                        portfolioData.get().setAmountTransferToBank(portfolioData.get().getAmountTransferToBank() - amtToDeductFromBank);
                        portfolioData.get().setTotalFundInWallet(portfolioData.get().getTotalFundInWallet() - (dhanInvestmentData.getInvAmount() - amtToDeductFromBank));
                    }
                } else {
                    portfolioData.get().setTotalFundInWallet(portfolioData.get().getTotalFundInWallet() - dhanInvestmentData.getInvAmount());
                }
                DhanInvestment dhanInvestment = initializeInvestment(dhanInvestmentData, portfolioData.get());
                dhanInvestment = dhanInvestmentRepository.save(dhanInvestment);
                if (ObjectUtils.isEmpty(portfolioData.get().getDhanInvestments())) {
                    List<DhanInvestment> l1 = new ArrayList<>();
                    l1.add(dhanInvestment);
                    portfolioData.get().setDhanInvestments(l1);
                } else {
                    portfolioData.get().getDhanInvestments().add(dhanInvestment);
                }
                dhanPortfolioRepository.save(portfolioData.get());
                calculatePortfolio();
            }
        } else {
            System.out.println("Portfolio not exist");
            throw new InvestmentException("Portfolio not exist");
        }
    }
}
