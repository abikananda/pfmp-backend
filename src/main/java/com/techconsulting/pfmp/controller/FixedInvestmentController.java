package com.techconsulting.pfmp.controller;

import com.techconsulting.pfmp.domain.FixedInvestment;
import com.techconsulting.pfmp.domain.FixedPortfolio;
import com.techconsulting.pfmp.domain.InvestmentType;
import com.techconsulting.pfmp.model.FixedDashStats;
import com.techconsulting.pfmp.model.FixedInvestmentPojo;
import com.techconsulting.pfmp.model.InvestmentAllocation;
import com.techconsulting.pfmp.model.ResponseMessage;
import com.techconsulting.pfmp.repository.FixedPortfolioRepository;
import com.techconsulting.pfmp.security.services.UserDetailsImpl;
import com.techconsulting.pfmp.service.BondInvestmentService;
import com.techconsulting.pfmp.service.FixedInvestmentService;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
public class FixedInvestmentController {
    private static final Logger LOGGER = LoggerFactory.getLogger(FixedInvestmentController.class);
    private static final String REQUEST_LOG = "Method requested: {} - By User: {}";
    @Autowired
    FixedPortfolioRepository fixedPortfolioRepository;
    @Autowired
    @Qualifier("FD_RD")
    FixedInvestmentService fixedInvestmentService;
    @Autowired
    @Qualifier("MUTUAL_FUND")
    FixedInvestmentService mutualFundInvestmentService;
    @Autowired
    @Qualifier("P2P")
    FixedInvestmentService p2pInvestmentService;
    @Autowired
    @Qualifier("BOND")
    BondInvestmentService bondInvestmentService;
    @Autowired
    @Qualifier("SAVING_BALANCE")
    FixedInvestmentService savingInvestmentService;
    @Autowired
    @Qualifier("FINAL_PORTFOLIO")
    FixedInvestmentService portfolioInvestmentService;
    @Autowired
    ModelMapper modelMapper;

    @PostMapping("/api/doFixedInvestment/{invType}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ResponseMessage> doInvestment(@AuthenticationPrincipal final UserDetailsImpl userDetailsImpl, @PathVariable String invType, @Valid @RequestBody FixedInvestmentPojo investment) {
        LOGGER.info(REQUEST_LOG, Thread.currentThread().getStackTrace()[1].getMethodName(), userDetailsImpl.getUsername());
        String message;
        try {
            if (invType.equalsIgnoreCase(InvestmentType.FD_RD.toString())) {
                fixedInvestmentService.doInvestment(modelMapper.map(investment, FixedInvestment.class));
            } else if (invType.equalsIgnoreCase(InvestmentType.P2P.toString())) {
                p2pInvestmentService.doInvestment(modelMapper.map(investment, FixedInvestment.class));
            } else if (invType.equalsIgnoreCase(InvestmentType.BOND.toString())) {
                bondInvestmentService.doInvestment(modelMapper.map(investment, FixedInvestment.class));
            } else if (invType.equalsIgnoreCase(InvestmentType.MUTUAL_FUND.toString())) {
                mutualFundInvestmentService.doInvestment(modelMapper.map(investment, FixedInvestment.class));
            } else if (invType.equalsIgnoreCase(InvestmentType.SAVING_BALANCE.toString())) {
                savingInvestmentService.doInvestment(modelMapper.map(investment, FixedInvestment.class));
            } else if (invType.equalsIgnoreCase(InvestmentType.FINAL_PORTFOLIO.toString())) {
                portfolioInvestmentService.doInvestment(modelMapper.map(investment, FixedInvestment.class));
            }
            message = "successfully invested: ";
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
        } catch (Exception e) {
            message = "Error: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseMessage(message));
        }
    }

    @PostMapping("/api/sellFixedInvestment/{invType}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ResponseMessage> updateInvestment(@AuthenticationPrincipal final UserDetailsImpl userDetailsImpl, @PathVariable String invType, @Valid @RequestBody FixedInvestmentPojo investment) {
        LOGGER.info(REQUEST_LOG, Thread.currentThread().getStackTrace()[1].getMethodName(), userDetailsImpl.getUsername());
        String message;
        try {
            if (invType.equalsIgnoreCase(InvestmentType.FD_RD.toString())) {
                fixedInvestmentService.updateInvestment(modelMapper.map(investment, FixedInvestment.class));
            } else if (invType.equalsIgnoreCase(InvestmentType.P2P.toString())) {
                p2pInvestmentService.updateInvestment(modelMapper.map(investment, FixedInvestment.class));
            } else if (invType.equalsIgnoreCase(InvestmentType.BOND.toString())) {
                bondInvestmentService.updateInvestment(modelMapper.map(investment, FixedInvestment.class));
            } else if (invType.equalsIgnoreCase(InvestmentType.MUTUAL_FUND.toString())) {
                mutualFundInvestmentService.updateInvestment(modelMapper.map(investment, FixedInvestment.class));
            } else if (invType.equalsIgnoreCase(InvestmentType.SAVING_BALANCE.toString())) {
                savingInvestmentService.updateInvestment(modelMapper.map(investment, FixedInvestment.class));
            } else if (invType.equalsIgnoreCase(InvestmentType.FINAL_PORTFOLIO.toString())) {
                portfolioInvestmentService.updateInvestment(modelMapper.map(investment, FixedInvestment.class));
            }
            message = "successfully invested: ";
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
        } catch (Exception e) {
            message = "Error: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseMessage(message));
        }
    }

    @GetMapping(value = "/api/getAllFixedInvestment/{invType}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getAllInvestment(@AuthenticationPrincipal final UserDetailsImpl userDetailsImpl, @PathVariable String invType) throws JSONException {
        LOGGER.info(REQUEST_LOG, Thread.currentThread().getStackTrace()[1].getMethodName(), userDetailsImpl.getUsername());
        UserDetailsImpl userDetails = SecurityUtil.getLoggedInUser();
        List<FixedInvestmentPojo> investmentPojoList = new ArrayList<>();
        if (invType.equalsIgnoreCase(InvestmentType.FD_RD.toString())) {
            Optional<FixedPortfolio> portfolio = fixedPortfolioRepository.findByInvestmentTypeAndUserId(InvestmentType.FD_RD, userDetails.getId());
            if (portfolio.isPresent()) {
                investmentPojoList = portfolio.get().getFixedInvestments().stream().map(a -> modelMapper.map(a, FixedInvestmentPojo.class)).toList();
            }
        } else if (invType.equalsIgnoreCase(InvestmentType.P2P.toString())) {
            Optional<FixedPortfolio> portfolio = fixedPortfolioRepository.findByInvestmentTypeAndUserId(InvestmentType.P2P, userDetails.getId());
            if (portfolio.isPresent()) {
                investmentPojoList = portfolio.get().getFixedInvestments().stream().map(a -> modelMapper.map(a, FixedInvestmentPojo.class)).toList();
            }
        } else if (invType.equalsIgnoreCase(InvestmentType.BOND.toString())) {
            Optional<FixedPortfolio> portfolio = fixedPortfolioRepository.findByInvestmentTypeAndUserId(InvestmentType.BOND, userDetails.getId());
            if (portfolio.isPresent()) {
                investmentPojoList = portfolio.get().getFixedInvestments().stream().map(a -> modelMapper.map(a, FixedInvestmentPojo.class)).toList();
            }
        } else if (invType.equalsIgnoreCase(InvestmentType.MUTUAL_FUND.toString())) {
            Optional<FixedPortfolio> portfolio = fixedPortfolioRepository.findByInvestmentTypeAndUserId(InvestmentType.MUTUAL_FUND, userDetails.getId());
            if (portfolio.isPresent()) {
                investmentPojoList = portfolio.get().getFixedInvestments().stream().map(a -> modelMapper.map(a, FixedInvestmentPojo.class)).toList();
            }
        } else if (invType.equalsIgnoreCase(InvestmentType.SAVING_BALANCE.toString())) {
            Optional<FixedPortfolio> portfolio = fixedPortfolioRepository.findByInvestmentTypeAndUserId(InvestmentType.SAVING_BALANCE, userDetails.getId());
            if (portfolio.isPresent()) {
                investmentPojoList = portfolio.get().getFixedInvestments().stream().map(a -> modelMapper.map(a, FixedInvestmentPojo.class)).toList();
            }
        } else if (invType.equalsIgnoreCase(InvestmentType.FINAL_PORTFOLIO.toString())) {
            Optional<FixedPortfolio> portfolio = fixedPortfolioRepository.findByInvestmentTypeAndUserId(InvestmentType.FINAL_PORTFOLIO, userDetails.getId());
            if (portfolio.isPresent()) {
                investmentPojoList = portfolio.get().getFixedInvestments().stream().map(a -> modelMapper.map(a, FixedInvestmentPojo.class)).toList();
            }
        }
        return new ResponseEntity<>(investmentPojoList, HttpStatus.OK);
    }

    @GetMapping("/api/getFixedInvestmentStatistics/{invType}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<FixedDashStats> getManualLendingStatistics(@AuthenticationPrincipal final UserDetailsImpl userDetailsImpl, @PathVariable String invType) {
        LOGGER.info(REQUEST_LOG, Thread.currentThread().getStackTrace()[1].getMethodName(), userDetailsImpl.getUsername());
        UserDetailsImpl userDetails = SecurityUtil.getLoggedInUser();
        FixedDashStats investmentStatistics = new FixedDashStats();
        if (invType.equalsIgnoreCase(InvestmentType.FD_RD.toString())) {
            Optional<FixedPortfolio> portfolio = fixedPortfolioRepository.findByInvestmentTypeAndUserId(InvestmentType.FD_RD, userDetails.getId());
            if (portfolio.isPresent()) {
                investmentStatistics.setTotalFundInvested(portfolio.get().getTotalInvestmentAmt());
                investmentStatistics.setRealisedReturnPer(portfolio.get().getPortfolioReturn());
            }
        } else if (invType.equalsIgnoreCase(InvestmentType.P2P.toString())) {
            Optional<FixedPortfolio> portfolio = fixedPortfolioRepository.findByInvestmentTypeAndUserId(InvestmentType.P2P, userDetails.getId());
            if (portfolio.isPresent()) {
                investmentStatistics.setTotalFundInvested(portfolio.get().getTotalInvestmentAmt());
                investmentStatistics.setRealisedReturnPer(portfolio.get().getPortfolioReturn());
            }
        } else if (invType.equalsIgnoreCase(InvestmentType.BOND.toString())) {
            Optional<FixedPortfolio> portfolio = fixedPortfolioRepository.findByInvestmentTypeAndUserId(InvestmentType.BOND, userDetails.getId());
            if (portfolio.isPresent()) {
                investmentStatistics.setTotalFundInvested(portfolio.get().getTotalInvestmentAmt());
                investmentStatistics.setRealisedReturnPer(portfolio.get().getPortfolioReturn());
            }
        } else if (invType.equalsIgnoreCase(InvestmentType.MUTUAL_FUND.toString())) {
            Optional<FixedPortfolio> portfolio = fixedPortfolioRepository.findByInvestmentTypeAndUserId(InvestmentType.MUTUAL_FUND, userDetails.getId());
            if (portfolio.isPresent()) {
                investmentStatistics.setTotalFundInvested(portfolio.get().getTotalInvestmentAmt());
                investmentStatistics.setRealisedReturnPer(portfolio.get().getPortfolioReturn());
            }
        } else if (invType.equalsIgnoreCase(InvestmentType.SAVING_BALANCE.toString())) {
            Optional<FixedPortfolio> portfolio = fixedPortfolioRepository.findByInvestmentTypeAndUserId(InvestmentType.SAVING_BALANCE, userDetails.getId());
            if (portfolio.isPresent()) {
                investmentStatistics.setTotalFundInvested(portfolio.get().getTotalInvestmentAmt());
                investmentStatistics.setRealisedReturnPer(portfolio.get().getPortfolioReturn());
            }
        } else if (invType.equalsIgnoreCase(InvestmentType.FINAL_PORTFOLIO.toString())) {
            Optional<FixedPortfolio> portfolio = fixedPortfolioRepository.findByInvestmentTypeAndUserId(InvestmentType.FINAL_PORTFOLIO, userDetails.getId());
            if (portfolio.isPresent()) {
                investmentStatistics.setTotalFundInvested(portfolio.get().getTotalInvestmentAmt());
                investmentStatistics.setRealisedReturnPer(portfolio.get().getPortfolioReturn());
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(investmentStatistics);
    }

    @GetMapping("/api/investmentAllocations")
    public List<InvestmentAllocation> getInvestmentAllocations() {
        LOGGER.debug("Fetching investment allocations");
        try {
            UserDetailsImpl userDetails = SecurityUtil.getLoggedInUser();
            Optional<FixedPortfolio> portfolio = fixedPortfolioRepository.findByInvestmentTypeAndUserId(InvestmentType.FINAL_PORTFOLIO, userDetails.getId());
            double fixed = 0;
            double equity = 0;
            double lending = 0;
            double bond = 0;
            double other = 0;
            if (portfolio.isPresent()) {
                fixed = portfolio.get().getFixedInvestments().stream().filter(a-> a.getParticular().equalsIgnoreCase("FD & RD")).mapToDouble(FixedInvestment::getInvWeight).sum();
                equity = portfolio.get().getFixedInvestments().stream().filter(a-> a.getParticular().equalsIgnoreCase("Mutual Fund")
                            || a.getParticular().equalsIgnoreCase("Stocks(Zerodha)")
                            || a.getParticular().equalsIgnoreCase("Stocks(Univest)")
                            || a.getParticular().equalsIgnoreCase("Stocks(dhan)")
                            || a.getParticular().equalsIgnoreCase("Stocks(parents-trading)")
                        )
                        .mapToDouble(FixedInvestment::getInvWeight).sum();
                lending = portfolio.get().getFixedInvestments().stream().filter(a-> a.getParticular().equalsIgnoreCase("P2P")).mapToDouble(FixedInvestment::getInvWeight).sum();
                bond = portfolio.get().getFixedInvestments().stream().filter(a-> a.getParticular().equalsIgnoreCase("BOND")).mapToDouble(FixedInvestment::getInvWeight).sum();
                other = portfolio.get().getFixedInvestments().stream().filter(a-> a.getParticular().equalsIgnoreCase("Saving Account Balance")).mapToDouble(FixedInvestment::getInvWeight).sum();
                LOGGER.debug("Investment allocations calculated - Equity: {}, Fixed: {}, Lending: {}, Bond: {}, Other: {}", equity, fixed, lending, bond, other);
            } else {
                LOGGER.debug("No final portfolio found for user");
            }
            return Arrays.asList(
                    new InvestmentAllocation("Equity", 40, equity*100),
                    new InvestmentAllocation("Fixed", 35, fixed*100),
                    new InvestmentAllocation("Lending", 10, lending*100),
                    new InvestmentAllocation("Bond", 14, bond*100),
                    new InvestmentAllocation("Other", 1, other*100)
            );
        } catch (Exception e) {
            LOGGER.error("Error fetching investment allocations", e);
            throw e;
        }
    }
}