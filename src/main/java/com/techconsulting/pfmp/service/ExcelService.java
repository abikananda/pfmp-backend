package com.techconsulting.pfmp.service;

import com.techconsulting.pfmp.domain.InvestedLoan;
import com.techconsulting.pfmp.model.InvestmentPojo;
import com.techconsulting.pfmp.utility.ExcelHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class ExcelService {

    public List<InvestedLoan> getLoansFromExcel(MultipartFile file) {
        log.debug("Processing Excel file for loans: {}", file.getOriginalFilename());
        List<InvestedLoan> loans;
        try {
            if (file == null || file.isEmpty()) {
                log.error("Excel file is empty or null");
                throw new RuntimeException("Excel file is empty or invalid");
            }
            loans = ExcelHelper.excelToInvestedLoans(file.getInputStream());
            log.info("Successfully extracted {} loans from Excel file", loans.size());
            if (loans.isEmpty()) {
                log.warn("No loans were extracted from Excel file - file may not have valid data");
            }
        } catch (IOException e) {
            log.error("Failed to process Excel file for loans: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("fail to store excel data: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while processing Excel file for loans: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }
        return loans;
    }

    public List<InvestmentPojo> getStocksFromExcel(MultipartFile file) {
        log.debug("Processing Excel file for stocks: {}", file.getOriginalFilename());
        List<InvestmentPojo> stocks;
        try {
            if (file == null || file.isEmpty()) {
                log.error("Excel file is empty or null");
                throw new RuntimeException("Excel file is empty or invalid");
            }
            stocks = ExcelHelper.excelToInvestmentPojo(file.getInputStream());
            log.info("Successfully extracted {} stocks from Excel file", stocks.size());
            if (stocks.isEmpty()) {
                log.warn("No stocks were extracted from Excel file - file may not have valid data");
            }
        } catch (IOException e) {
            log.error("Failed to process Excel file for stocks: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("fail to store excel data: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while processing Excel file for stocks: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }
        return stocks;
    }

    public boolean validateExcelFile(MultipartFile file) {
        log.debug("Validating Excel file: {}", file.getOriginalFilename());
        boolean isValid;
        try {
            isValid = ExcelHelper.validateExcelFile(file);
            log.info("Excel file validation result: {} for file: {}", isValid, file.getOriginalFilename());
        } catch (Exception e) {
            log.error("Failed to validate Excel file: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("fail to validate excel file: " + e.getMessage());
        }
        return isValid;
    }
}
