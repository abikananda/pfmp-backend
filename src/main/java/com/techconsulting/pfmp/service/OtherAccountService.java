package com.techconsulting.pfmp.service;

import com.techconsulting.pfmp.domain.FinancialYear;
import com.techconsulting.pfmp.domain.InvestmentStatus;
import com.techconsulting.pfmp.domain.TrackingGainTxns;
import com.techconsulting.pfmp.domain.TrackingOthersAccount;
import com.techconsulting.pfmp.model.OtherAccountStats;
import com.techconsulting.pfmp.repository.TrackingGainTxnsRepository;
import com.techconsulting.pfmp.repository.TrackingOthersAccountRepository;
import com.techconsulting.pfmp.repository.UserRepository;
import com.techconsulting.pfmp.security.services.UserDetailsImpl;
import com.techconsulting.pfmp.utility.SecurityUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OtherAccountService {
    @Autowired
    TrackingGainTxnsRepository trackingGainTxnsRepository;
    @Autowired
    TrackingOthersAccountRepository trackingOthersAccountRepository;
    @Autowired
    UserRepository userRepository;
    @PersistenceContext
    EntityManager entityManager;
    @Autowired
    ModelMapper mapper;


    public void transferAmtToOtherAccount(Double amount, String type, String comment) {
        UserDetailsImpl userDetails = SecurityUtil.getLoggedInUser();
        Optional<TrackingOthersAccount> trackingOthersAccountData = trackingOthersAccountRepository.findByUserId(userDetails.getId());
        TrackingOthersAccount trackingOthersAccount = trackingOthersAccountData.orElseGet(TrackingOthersAccount::new);
        if (type.equalsIgnoreCase("WITHDRAW") && trackingOthersAccount.getCurrentAccBalance()<amount){
            throw new RuntimeException("No balance to withdraw");
        }
        if (type.equalsIgnoreCase("DEPOSIT")){
            trackingOthersAccount.setTotAmtTransferToAcc(trackingOthersAccount.getTotAmtTransferToAcc()+amount);
            trackingOthersAccount.setCurrentFYPrincipalAmt(trackingOthersAccount.getCurrentFYPrincipalAmt()+amount);
        } else if (type.equalsIgnoreCase("WITHDRAW")){
            trackingOthersAccount.setTotAmtTransferToAcc(trackingOthersAccount.getTotAmtTransferToAcc()-amount);
            trackingOthersAccount.setCurrentFYPrincipalAmt(trackingOthersAccount.getCurrentFYPrincipalAmt()-amount);
        } else {
            System.out.println("wrong type");
            return;
        }

        trackingOthersAccount.setUser(userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found with username: " + userDetails.getUsername())));
        trackingOthersAccount.setComments(comment);
        trackingOthersAccountRepository.save(trackingOthersAccount);
        calculateAccountSummery();
    }
    public void addOrUpdateGainTxns(Double gainAmount, String particular, String financialYear, String status) {
        UserDetailsImpl userDetails = SecurityUtil.getLoggedInUser();
        Optional<TrackingOthersAccount> trackingOthersAccountData = trackingOthersAccountRepository.findByUserId(userDetails.getId());
        TrackingOthersAccount trackingOthersAccount = trackingOthersAccountData.orElseThrow(() -> new RuntimeException("account not found"));
        Optional<TrackingGainTxns> trackingGainTxnsData = trackingGainTxnsRepository.findByParticular(particular);
        TrackingGainTxns trackingGainTxns = trackingGainTxnsData.orElseGet(TrackingGainTxns::new);
        trackingGainTxns.setTrackingOthersAccount(trackingOthersAccount);
        trackingGainTxns.setGainAmt(gainAmount);
        trackingGainTxns.setComments("");
        trackingGainTxns.setParticular(particular);
        trackingGainTxns.setFinancialYear(FinancialYear.fromString(financialYear));
        if (status.equalsIgnoreCase(InvestmentStatus.IN_PROGRESS.name())){
            trackingGainTxns.setStatus(InvestmentStatus.IN_PROGRESS);
        } else if (status.equalsIgnoreCase(InvestmentStatus.COMPLETED.name())){
            trackingGainTxns.setStatus(InvestmentStatus.COMPLETED);
        } else {
            System.out.println("wrong status");
            return;
        }
        trackingGainTxnsRepository.save(trackingGainTxns);
        calculateAccountSummery();
    }

    public void resetCurrentFYPrincipalAmt() {
        UserDetailsImpl userDetails = SecurityUtil.getLoggedInUser();
        Optional<TrackingOthersAccount> trackingOthersAccountData = trackingOthersAccountRepository.findByUserId(userDetails.getId());
        TrackingOthersAccount trackingOthersAccount = trackingOthersAccountData.orElseThrow(() -> new RuntimeException("account not found"));
        trackingOthersAccount.setCurrentFYPrincipalAmt(trackingOthersAccount.getCurrentAccBalance());
        trackingOthersAccountRepository.save(trackingOthersAccount);
    }

    private void calculateAccountSummery() {
        entityManager.clear();
        UserDetailsImpl userDetails = SecurityUtil.getLoggedInUser();
        Optional<TrackingOthersAccount> trackingOthersAccountData = trackingOthersAccountRepository.findByUserId(userDetails.getId());
        TrackingOthersAccount trackingOthersAccount = trackingOthersAccountData.orElseThrow(() -> new RuntimeException("account not found"));
        List<TrackingGainTxns> trackingGainTxnsList = trackingGainTxnsRepository.findByTrackingOthersAccountId(trackingOthersAccount.getId());
        trackingOthersAccount.setCurrentFYReturnAmount(trackingGainTxnsList.stream().filter(a->(a.getFinancialYear().getYearFormat().equals(FinancialYear.getCurrentFinancialYear())) && (a.getStatus().equals(InvestmentStatus.COMPLETED))).mapToDouble(TrackingGainTxns::getGainAmt).sum());
        trackingOthersAccount.setCurrentFYReturnPer(trackingOthersAccount.getCurrentFYReturnAmount()*100/trackingOthersAccount.getCurrentFYPrincipalAmt());
        trackingOthersAccount.setTotalReturnAmount(trackingGainTxnsList.stream().filter(a->a.getStatus().equals(InvestmentStatus.COMPLETED)).mapToDouble(TrackingGainTxns::getGainAmt).sum());
        trackingOthersAccount.setTotalReturnPer(trackingOthersAccount.getTotalReturnAmount()*100/trackingOthersAccount.getTotAmtTransferToAcc());
        trackingOthersAccount.setCurrentAccBalance(trackingOthersAccount.getTotAmtTransferToAcc()+ trackingOthersAccount.getTotalReturnAmount());
        trackingOthersAccountRepository.save(trackingOthersAccount);
    }

    public OtherAccountStats getOtherAccountStatistics() {
        UserDetailsImpl userDetails = SecurityUtil.getLoggedInUser();
        OtherAccountStats investmentStatistics = new OtherAccountStats();
        Optional<TrackingOthersAccount> portfolio = trackingOthersAccountRepository.findByUserId(userDetails.getId());
        portfolio.ifPresent(trackingOthersAccount -> mapper.map(trackingOthersAccount, investmentStatistics));
        portfolio.ifPresent(trackingOthersAccount -> investmentStatistics.setCurrentFYUnrealisedReturnAmount(trackingOthersAccount.getTrackingGainTxns().stream().filter(a -> a.getFinancialYear().getYearFormat().equals(FinancialYear.getCurrentFinancialYear())).mapToDouble(TrackingGainTxns::getGainAmt).sum()));
        investmentStatistics.setCurrentFYUnrealisedReturnPer(investmentStatistics.getCurrentFYUnrealisedReturnAmount()*100/investmentStatistics.getCurrentFYPrincipalAmt());
        return investmentStatistics;
    }

}
