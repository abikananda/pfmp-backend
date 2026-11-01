package com.techconsulting.pfmp.controller;

import com.techconsulting.pfmp.domain.InvestedLoan;
import com.techconsulting.pfmp.model.DashStats;
import com.techconsulting.pfmp.model.InvestedLoanPojo;
import com.techconsulting.pfmp.model.LogDto;
import com.techconsulting.pfmp.model.ResponseMessage;
import com.techconsulting.pfmp.security.services.UserDetailsImpl;
import com.techconsulting.pfmp.service.ExcelService;
import com.techconsulting.pfmp.service.InvestedLoanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
public class LoanController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoanController.class);
    private static final String REQUEST_LOG = "Method requested: {} - By User: {}";
    @Autowired
    InvestedLoanService investedLoanService;
    @Autowired
    ExcelService excelService;

    @PostMapping("/api/add_invested_loan_details")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ResponseMessage> add_invested_loan_details(@AuthenticationPrincipal final UserDetailsImpl userDetailsImpl, @RequestParam("file") MultipartFile file) {
        LOGGER.info(REQUEST_LOG, Thread.currentThread().getStackTrace()[1].getMethodName(), userDetailsImpl.getUsername());
        String message;
        try {
            //storageService.save(file);
            List<InvestedLoan> loans = excelService.getLoansFromExcel(file);
//            investedLoanService.store_invested_loan_details(loans);
            message = "Uploaded the file successfully: " + file.getOriginalFilename();
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
        } catch (Exception e) {
            message = "Could not upload the file: " + file.getOriginalFilename() + ". Error: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseMessage(message));
        }
    }

    @GetMapping("/api/getAllManualLendingDetails")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<InvestedLoanPojo>> getAllManualLendingDetails(@AuthenticationPrincipal final UserDetailsImpl userDetailsImpl) {
        LOGGER.info(REQUEST_LOG, Thread.currentThread().getStackTrace()[1].getMethodName(), userDetailsImpl.getUsername());
        try {
            List<InvestedLoanPojo> investedLoans = investedLoanService.getAllManualLendingDetails();
            return ResponseEntity.status(HttpStatus.OK).body(investedLoans);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }

    @PostMapping("/api/logs")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> captureUILogs(@AuthenticationPrincipal final UserDetailsImpl userDetailsImpl, @RequestBody LogDto logDto) {
        logDto.setUsername(userDetailsImpl.getUsername());
        LOGGER.debug("Captured UI log: {}", logDto);
        LOGGER.info(String.valueOf(logDto));
        return null;
    }

    @GetMapping("/api/auth/collectLoanDetails")
    public ResponseEntity<String> collectLoanDetails() {
        LOGGER.debug("Starting loan details collection");
        try {
            investedLoanService.collectLoanDetails();
            LOGGER.info("Loan details collected successfully");
            return ResponseEntity.ok("Loan fetched and saved.");
        } catch (Exception e) {
            LOGGER.error("Error collecting loan details", e);
            throw e;
        }
    }
}