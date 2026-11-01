package com.techconsulting.pfmp.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techconsulting.pfmp.annotation.FeatureEnabled;
import com.techconsulting.pfmp.domain.*;
import com.techconsulting.pfmp.model.DashStats;
import com.techconsulting.pfmp.model.InvestmentPojo;
import com.techconsulting.pfmp.model.OtherAccountStats;
import com.techconsulting.pfmp.model.ResponseMessage;
import com.techconsulting.pfmp.repository.*;
import com.techconsulting.pfmp.security.services.UserDetailsImpl;
import com.techconsulting.pfmp.service.*;
import com.techconsulting.pfmp.utility.SecurityUtil;
import jakarta.validation.Valid;
import org.json.JSONException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
public class InvestmentController {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvestmentController.class);
    private static final String REQUEST_LOG = "Method requested: {} - By User: {}";
    @Autowired
    DhanPortfolioRepository dhanPortfolioRepository;
    @Autowired
    ManualLendingPortfolioRepository manualLendingPortfolioRepository;
    @Autowired
    CommonRepository commonRepository;
    @Autowired
    @Qualifier("MOM_DHAN_ACCOUNT")
    MomDhanAccountInvestmentService dhanInvestmentService;
    @Autowired
    @Qualifier("MANUAL_LENDING")
    ManualLendingInvestmentService manualInvestmentService;
    @Autowired
    OtherAccountService otherAccountService;
    @Autowired
    ExcelService excelService;
    @Autowired
    ModelMapper mapper;
    @Autowired
    TrackingGainTxnsRepository trackingGainTxnsRepository;
    @Autowired
    TrackingOthersAccountRepository trackingOthersAccountRepository;

    @PostMapping("/api/addAmtToWallet/{walletType}/{amt}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ResponseMessage> addAmtToWallet(@AuthenticationPrincipal final UserDetailsImpl userDetailsImpl, @PathVariable String walletType, @PathVariable Double amt) {
        LOGGER.info(REQUEST_LOG, Thread.currentThread().getStackTrace()[1].getMethodName(), userDetailsImpl.getUsername());
        String message;
        try {
            if (walletType.equalsIgnoreCase(InvestmentType.MOM_DHAN_ACCOUNT.toString())) {
                dhanInvestmentService.loadAmountToPortfolio(amt);
            } else if (walletType.equalsIgnoreCase(InvestmentType.MANUAL_LENDING.toString())) {
                manualInvestmentService.createOrUpdatePortfolio(amt);
            }
            message = "successfully: ";
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
        } catch (Exception e) {
            message = "Error: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseMessage(message));
        }
    }

    @PostMapping("/api/updateActualWalletBalance/{walletType}/{amt}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ResponseMessage> updateActualWalletBalance(@AuthenticationPrincipal final UserDetailsImpl userDetailsImpl, @PathVariable String walletType, @PathVariable Double amt) {
        LOGGER.info(REQUEST_LOG, Thread.currentThread().getStackTrace()[1].getMethodName(), userDetailsImpl.getUsername());
        String message;
        try {
            if (walletType.equalsIgnoreCase(InvestmentType.MOM_DHAN_ACCOUNT.toString())) {
                dhanInvestmentService.updateActualWalletBalance(amt);
            }
            message = "successfully: ";
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
        } catch (Exception e) {
            message = "Error: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseMessage(message));
        }
    }

    @PostMapping("/api/transferAmtToBank/{walletType}/{amt}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ResponseMessage> transferAmtToBank(@AuthenticationPrincipal final UserDetailsImpl userDetailsImpl, @PathVariable String walletType, @PathVariable Double amt) {
        LOGGER.info(REQUEST_LOG, Thread.currentThread().getStackTrace()[1].getMethodName(), userDetailsImpl.getUsername());
        String message;
        try {
            if (walletType.equalsIgnoreCase(InvestmentType.MOM_DHAN_ACCOUNT.toString())) {
                dhanInvestmentService.withdrawFromWalletToBank(amt);
            }
            message = "successfully: ";
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
        } catch (Exception e) {
            message = "Error: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseMessage(message));
        }
    }

    @PostMapping("/api/doInvestment/{invType}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @FeatureEnabled("manual_lending")
    public ResponseEntity<ResponseMessage> doInvestment(@AuthenticationPrincipal final UserDetailsImpl userDetailsImpl, @PathVariable String invType, @Valid @RequestBody InvestmentPojo investment) {
        LOGGER.info(REQUEST_LOG, Thread.currentThread().getStackTrace()[1].getMethodName(), userDetailsImpl.getUsername());
        String message;
        try {
            if (invType.equalsIgnoreCase(InvestmentType.MOM_DHAN_ACCOUNT.toString())) {
                dhanInvestmentService.doInvestment(mapper.map(investment, DhanInvestment.class));
            }
            message = "successfully invested: ";
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
        } catch (Exception e) {
            message = "Error: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseMessage(message));
        }
    }

    @PostMapping("/api/sellInvestment/{invType}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @FeatureEnabled("manual_lending")
    public ResponseEntity<ResponseMessage> updateInvestment(@AuthenticationPrincipal final UserDetailsImpl userDetailsImpl, @PathVariable String invType, @Valid @RequestBody InvestmentPojo investment) {
        LOGGER.info(REQUEST_LOG, Thread.currentThread().getStackTrace()[1].getMethodName(), userDetailsImpl.getUsername());
        String message;
        try {
            if (invType.equalsIgnoreCase(InvestmentType.MOM_DHAN_ACCOUNT.toString())) {
                dhanInvestmentService.updateInvestment(mapper.map(investment, DhanInvestment.class));
            } else if (invType.equalsIgnoreCase(InvestmentType.MANUAL_LENDING.toString())) { //for now use for update some field of manual lending
                manualInvestmentService.updateInvestment(mapper.map(investment, ManualLendingInvestment.class));
            }
            message = "successfully updated: ";
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
        } catch (Exception e) {
            message = "Error: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseMessage(message));
        }
    }

    @GetMapping(value = "/api/getAllInvestment/{invType}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @FeatureEnabled("manual_lending")
    public ResponseEntity<?> getAllInvestment(@PathVariable String invType) throws JSONException {
        UserDetailsImpl userDetails = SecurityUtil.getLoggedInUser();
        List<InvestmentPojo> investmentPojoList = new ArrayList<>();
        if (invType.equalsIgnoreCase(InvestmentType.MOM_DHAN_ACCOUNT.toString())) {
            Optional<DhanPortfolio> portfolio = dhanPortfolioRepository.findByInvestmentTypeAndUserId(InvestmentType.MOM_DHAN_ACCOUNT, userDetails.getId());
            if (portfolio.isPresent()) {
                investmentPojoList = portfolio.get().getDhanInvestments().stream().map(a -> mapper.map(a, InvestmentPojo.class)).toList();
            }
        } else if (invType.equalsIgnoreCase(InvestmentType.MANUAL_LENDING.toString())) {
            Optional<ManualLendingPortfolio> portfolio = manualLendingPortfolioRepository.findByInvestmentTypeAndUserId(InvestmentType.MANUAL_LENDING, userDetails.getId());
            if (portfolio.isPresent()) {
                investmentPojoList = portfolio.get().getManualLendingInvestments().stream()
                        .filter(inv->inv.getLoanStatus().equals(InvestmentStatus.ACTIVE))
                        .map(a -> mapper.map(a, InvestmentPojo.class)).toList();
            }
        }
        return new ResponseEntity<>(investmentPojoList, HttpStatus.OK);
    }

    @GetMapping("/api/getInvestmentStatistics/{invType}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @FeatureEnabled("manual_lending")
    public ResponseEntity<DashStats> getInvestmentStatistics(@AuthenticationPrincipal final UserDetailsImpl userDetailsImpl, @PathVariable String invType) {
        LOGGER.info(REQUEST_LOG, Thread.currentThread().getStackTrace()[1].getMethodName(), userDetailsImpl.getUsername());
        UserDetailsImpl userDetails = SecurityUtil.getLoggedInUser();
        DashStats investmentStatistics = new DashStats();
        if (invType.equalsIgnoreCase(InvestmentType.MOM_DHAN_ACCOUNT.toString())) {
            Optional<DhanPortfolio> portfolio = dhanPortfolioRepository.findByInvestmentTypeAndUserId(InvestmentType.MOM_DHAN_ACCOUNT, userDetails.getId());
            if (portfolio.isPresent()) {
                investmentStatistics.setTotalFundInvested(portfolio.get().getTotalInvestmentAmt());
                investmentStatistics.setRealisedReturnPer(portfolio.get().getPortfolioReturn());
                investmentStatistics.setRealisedReturnAmt(portfolio.get().getTotalExcessReturnAmt());
                investmentStatistics.setAmountLoadedToWallet(portfolio.get().getAmountLoadedToWallet());
                investmentStatistics.setBalanceAmountInBank(portfolio.get().getAmountTransferToBank());
                investmentStatistics.setTotalFundInWallet(portfolio.get().getTotalFundInWallet());
                investmentStatistics.setCharges(portfolio.get().getChargerAmt());
                investmentStatistics.setActualAmountInWallet(portfolio.get().getActualAmtInWallet());
            }
        } else if (invType.equalsIgnoreCase(InvestmentType.MANUAL_LENDING.toString())) {
            Optional<ManualLendingPortfolio> portfolio = manualLendingPortfolioRepository.findByInvestmentTypeAndUserId(InvestmentType.MANUAL_LENDING, userDetails.getId());
            if (portfolio.isPresent()) {
                investmentStatistics.setTotalFundInvested(portfolio.get().getTotalFundOnLendenInvested());
                investmentStatistics.setRealisedReturnPer(portfolio.get().getPortfolioReturnPer());
                investmentStatistics.setRealisedReturnAmt(portfolio.get().getPortfolioReturnAmt());
                investmentStatistics.setAmountLoadedToWallet(portfolio.get().getInvestmentPrincipalAmt());
                investmentStatistics.setBalanceAmountInBank(portfolio.get().getAmtYetToBeInvest());
                investmentStatistics.setTotalFundInWallet(portfolio.get().getTotalNPA()+0.0);
                investmentStatistics.setCharges(Math.abs(portfolio.get().getManualLendingInvestments().stream().map(ManualLendingInvestment::getInvReturnAmt).filter(invReturnAmt -> invReturnAmt <0).mapToDouble(Double::doubleValue).sum()));
                investmentStatistics.setActualAmountInWallet(Math.abs(portfolio.get().getManualLendingInvestments().stream().filter(ManualLendingInvestment::isNPA).filter(a->a.getLoanStatus().equals(InvestmentStatus.CLOSED)).count())+0.0);
                investmentStatistics.setTotalNumberOfLoans((long) portfolio.get().getManualLendingInvestments().size());
                investmentStatistics.setNumberOfActiveLoans(portfolio.get().getManualLendingInvestments().stream().filter(a->a.getLoanStatus().equals(InvestmentStatus.ACTIVE)).count());
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(investmentStatistics);
    }

    @PostMapping("/api/uploadManualLendingAndInvest")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @FeatureEnabled("manual_lending")
    public ResponseEntity<ResponseMessage> uploadManualLendingAndInvest(@AuthenticationPrincipal final UserDetailsImpl userDetailsImpl, @RequestParam("file") MultipartFile file) throws IOException {
        LOGGER.info(REQUEST_LOG, Thread.currentThread().getStackTrace()[1].getMethodName(), userDetailsImpl.getUsername());
        String message;
        try {
            if (excelService.validateExcelFile(file)){
                manualInvestmentService.cleanLoanLogTable();
                List<InvestedLoan> loans = excelService.getLoansFromExcel(file);
                manualInvestmentService.uploadManualLendingAndInvest(loans);
                commonRepository.insertLoanTransitionMetrics();
                message = "Uploaded the file successfully: " + file.getOriginalFilename();
                return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
            }else {
                message = "Invalid excel report " + file.getOriginalFilename();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseMessage(message));
            }
        } catch (Exception e) {
            LOGGER.error("An error occurred while uploadManualLendingAndInvest", e);
            message = "Could not upload the file: " + file.getOriginalFilename() + ". Error: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseMessage(message));
        }
    }

    @GetMapping("/api/transferAmtToOtherAccount")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ResponseMessage> transferAmtToOtherAccount(@AuthenticationPrincipal final UserDetailsImpl userDetailsImpl, @RequestParam("amount") Double amount, @RequestParam("type") String type, @RequestParam("comment") String comment) throws IOException {
        LOGGER.info(REQUEST_LOG, Thread.currentThread().getStackTrace()[1].getMethodName(), userDetailsImpl.getUsername());
        String message;
        try {
            otherAccountService.transferAmtToOtherAccount(amount, type, comment);
            message = "successfully";
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
        } catch (Exception e) {
            message = "failed with Error: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseMessage(message));
        }
    }
    @GetMapping("/api/addOrUpdateGainTxns")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ResponseMessage> addOrUpdateGainTxns(@AuthenticationPrincipal final UserDetailsImpl userDetailsImpl, @RequestParam("gainAmount") Double gainAmount, @RequestParam("particular") String particular, @RequestParam("financialYear") String financialYear, @RequestParam("status") String status) throws IOException {
        LOGGER.info(REQUEST_LOG, Thread.currentThread().getStackTrace()[1].getMethodName(), userDetailsImpl.getUsername());
        String message;
        try {
            otherAccountService.addOrUpdateGainTxns(gainAmount, particular, financialYear, status);
            message = "successfully";
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
        } catch (Exception e) {
            message = "failed with Error: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseMessage(message));
        }
    }
    @GetMapping(value = "/api/getGainTxnList")
    public ResponseEntity<?> getGainTxnList(@AuthenticationPrincipal final UserDetailsImpl userDetailsImpl) throws JSONException, JsonProcessingException {
        LOGGER.info(REQUEST_LOG, Thread.currentThread().getStackTrace()[1].getMethodName(), userDetailsImpl.getUsername());
        UserDetailsImpl userDetails = SecurityUtil.getLoggedInUser();
        Optional<TrackingOthersAccount> portfolio = trackingOthersAccountRepository.findByUserId(userDetails.getId());
        AtomicReference<List<TrackingGainTxns>> trackingGainTxns = new AtomicReference<>(new ArrayList<>());
        portfolio.ifPresent(
                a-> trackingGainTxns.set(trackingGainTxnsRepository.findByTrackingOthersAccountId(a.getId()))
        );
        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String, Object>> jsonList = new ArrayList<>();
        for (TrackingGainTxns trackingGainTxn: trackingGainTxns.get()){
            Map<String, Object> json = new HashMap<>();
            json.put("id", trackingGainTxn.getId());
            json.put("particular", trackingGainTxn.getParticular());
            json.put("gainAmt", trackingGainTxn.getGainAmt());
            json.put("financialYear", trackingGainTxn.getFinancialYear().getYearFormat());
            json.put("status", trackingGainTxn.getStatus());
            json.put("comments", trackingGainTxn.getComments());
            json.put("lastUpdated", trackingGainTxn.getLastUpdated().toString());
            jsonList.add(json);
        }
        return new ResponseEntity<>(objectMapper.writeValueAsString(jsonList), HttpStatus.OK);
    }
    @GetMapping("/api/getOtherAccountStatistics")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<OtherAccountStats> getOtherAccountStatistics(@AuthenticationPrincipal final UserDetailsImpl userDetailsImpl) {
        LOGGER.info(REQUEST_LOG, Thread.currentThread().getStackTrace()[1].getMethodName(), userDetailsImpl.getUsername());
        OtherAccountStats investmentStatistics = otherAccountService.getOtherAccountStatistics();
        return ResponseEntity.status(HttpStatus.OK).body(investmentStatistics);
    }
    @GetMapping("/api/resetCurrentFYPrincipalAmt")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ResponseMessage> resetCurrentFYPrincipalAmt(@AuthenticationPrincipal final UserDetailsImpl userDetailsImpl) {
        LOGGER.info(REQUEST_LOG, Thread.currentThread().getStackTrace()[1].getMethodName(), userDetailsImpl.getUsername());
        String message;
        try {
            otherAccountService.resetCurrentFYPrincipalAmt();
            message = "successfully";
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
        } catch (Exception e) {
            message = "failed with Error: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseMessage(message));
        }
    }
    @GetMapping("/api/loans/monthly-summary")
    public List<Map<String,Object>> getMonthlyLoanSummary() {
        return manualInvestmentService.getMonthlyLoanSummary();
    }
    @GetMapping("/api/loans/summary")
    public List<Map<String,Object>> getLoanSummary() {
        return manualInvestmentService.getLoanSummary();
    }
    @GetMapping("/api/investment/monthly-summary")
    public List<Map<String, Object>> getMonthlyInvestmentSummary() {
        return manualInvestmentService.getMonthlyInvestmentSummary();
    }
    @GetMapping("/api/investment/summary")
    public List<Map<String, Object>> getInvestmentSummary() {
        return manualInvestmentService.getInvestmentSummary();
    }
    @GetMapping("/api/loans/day-wise-summary")
    public List<Map<String,Object>> getDayWiseLoanSummary() {
        return manualInvestmentService.getDayWiseLoanSummary();
    }
    @GetMapping("/api/investment/day-wise-summary")
    public List<Map<String, Object>> getDayWiseInvestmentSummary() {
        return manualInvestmentService.getDayWiseInvestmentSummary();
    }
}