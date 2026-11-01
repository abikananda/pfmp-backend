package com.techconsulting.pfmp.utility;

import com.techconsulting.pfmp.controller.InvestmentController;
import com.techconsulting.pfmp.domain.InvestedLoan;
import com.techconsulting.pfmp.domain.InvestmentStatus;
import com.techconsulting.pfmp.model.InvestmentPojo;
import com.techconsulting.pfmp.security.services.UserDetailsImpl;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class ExcelHelper {
    public static String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final Logger LOGGER = LoggerFactory.getLogger(InvestmentController.class);
    static String SHEET = "Sheet";
    static Set<String> expectedHeaders = new HashSet<>();
    static {
        expectedHeaders.add("Loan ID");
        expectedHeaders.add("Disbursement Date");
        expectedHeaders.add("Disbursed Amount (₹)");
        expectedHeaders.add("Interest Rate (%)");
        expectedHeaders.add("Repayment Start Date (expected)");
        expectedHeaders.add("Tenure (months)");
        expectedHeaders.add("Repayment Type");
        expectedHeaders.add("Principal Received (₹)");
        expectedHeaders.add("Interest Received (₹)");
        expectedHeaders.add("Platform Fee (₹)");
        expectedHeaders.add("Profit & Loss (₹)");
        expectedHeaders.add("Total Amount Received (₹)");
        expectedHeaders.add("Total Repayment Amount (illustrative) (₹)");
//        expectedHeaders.add("Refunded Amount (₹)");
        expectedHeaders.add("Loan Status");
        expectedHeaders.add("DPD (days past due)");
        expectedHeaders.add("NPA");
    }

    public static boolean hasExcelFormat(MultipartFile file) {
        LOGGER.debug("Checking if file has Excel format: {}", file.getContentType());
        boolean result = TYPE.equals(file.getContentType());
        LOGGER.debug("Excel format validation result: {}", result);
        return result;
    }
    public static boolean validateExcelFile(MultipartFile file) throws IOException {
        LOGGER.debug("Validating Excel file: {}", file.getOriginalFilename());
        if (!hasExcelFormat(file)){
            LOGGER.warn("File does not have Excel format");
            return  false;
        }
        try {
            UserDetailsImpl user = SecurityUtil.getLoggedInUser();
            String lender_id = (String) user.getOtherDetails().get("lendenClub_lender_id");
            if (!Objects.requireNonNull(file.getOriginalFilename()).startsWith("MANUAL_LENDING_REPORT_"+lender_id)){
                LOGGER.warn("File name validation failed for user: {}", user.getUsername());
                return false;
            }

            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheet(SHEET);

            for (Row row : sheet) {
                Set<String> foundHeaders = new HashSet<>();
                for (Cell cell : row) {
                    if (cell.getCellType() == CellType.STRING) {
                        String cellValue = cell.getStringCellValue();
                        if (expectedHeaders.contains(cellValue)) {
                            foundHeaders.add(cellValue);
                        }
                    }
                }
                if (foundHeaders.size() == expectedHeaders.size() && foundHeaders.containsAll(expectedHeaders)) {
                    LOGGER.info("Excel file validation successful");
                    return true; // All headers found
                }
            }
            LOGGER.warn("Excel file validation failed - headers not found");
            return false;
        } catch (Exception e) {
            LOGGER.error("Error validating Excel file", e);
            throw e;
        }
    }
    public static int findHeaderRow(Sheet sheet, Set<String> expectedHeadersss) {
        LOGGER.debug("Finding header row in sheet");
        for (Row row : sheet) {
            Set<String> foundHeaders = new HashSet<>();
            for (Cell cell : row) {
                if (cell.getCellType() == CellType.STRING) {
                    String cellValue = cell.getStringCellValue();
                    if (expectedHeaders.contains(cellValue)) {
                        foundHeaders.add(cellValue);
                    }
                }
            }
            if (foundHeaders.size() == expectedHeaders.size() && foundHeaders.containsAll(expectedHeaders)) {
                LOGGER.debug("Header row found at row number: {}", row.getRowNum());
                return row.getRowNum(); // All headers found
            }
        }
        LOGGER.warn("Header row not found in sheet");
        return -1; // Header not found
    }
    private static Double getNumericValueSafely(Cell cell) {
        if (cell == null) {
            return null;
        }
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                double value = cell.getNumericCellValue();
                if (Double.isNaN(value) || Double.isInfinite(value)) {
                    LOGGER.warn("Invalid numeric value (NaN or Infinity) found in cell: {}", cell.getAddress());
                    return null;
                }
                return value;
            } else if (cell.getCellType() == CellType.STRING) {
                String stringValue = cell.getStringCellValue().trim();
                if (stringValue.isEmpty()) {
                    return null;
                }
                try {
                    double value = Double.parseDouble(stringValue);
                    if (Double.isNaN(value) || Double.isInfinite(value)) {
                        LOGGER.warn("Invalid numeric value in string cell: {}", cell.getAddress());
                        return null;
                    }
                    return value;
                } catch (NumberFormatException e) {
                    LOGGER.warn("Could not parse numeric value from string cell: {} with value: {}", cell.getAddress(), stringValue);
                    return null;
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Error reading numeric value from cell: {}", cell.getAddress(), e);
        }
        return null;
    }

    private static String getStringValueSafely(Cell cell) {
        if (cell == null) {
            return null;
        }
        try {
            if (cell.getCellType() == CellType.STRING) {
                return cell.getStringCellValue();
            } else if (cell.getCellType() == CellType.NUMERIC) {
                return String.valueOf((long) cell.getNumericCellValue());
            }
        } catch (Exception e) {
            LOGGER.warn("Error reading string value from cell: {}", cell.getAddress(), e);
        }
        return null;
    }

    public static List<InvestedLoan> excelToInvestedLoans(InputStream is) {
        LOGGER.debug("Converting Excel file to InvestedLoan list");
        List<InvestedLoan> loans = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheet(SHEET);
            int headerRowIndex = findHeaderRow(sheet, expectedHeaders);
            if (headerRowIndex == -1) {
                LOGGER.error("Header row not found in the sheet");
                return new ArrayList<>();
            }
            System.out.println("Header row found at index: " + headerRowIndex);
            // Process data rows below the header
            for (int i = headerRowIndex + 1; i <= sheet.getLastRowNum(); i++) {
                Row dataRow = sheet.getRow(i);
                if (dataRow != null) {
                    InvestedLoan loan = new InvestedLoan();
                    int cellIdx = 0;
                    for (Cell cell : dataRow) {
                        switch (cellIdx) {
                            case 1:
                                loan.setSchemeId(getStringValueSafely(cell));
                                break;

                            case 2:
                                loan.setInvestmentDate(getStringValueSafely(cell));
                                break;

                            case 3:
                                loan.setInvestedAmount(getNumericValueSafely(cell));//disburse amt
                                break;

                            case 16:
                                loan.setBorrowerAPRPercent(getNumericValueSafely(cell));
                                break;
                            case 17:
                                loan.setTenure(getNumericValueSafely(cell));
                                break;
                            case 7:
                                loan.setAmountReceived(getNumericValueSafely(cell));
                                break;
                            case 6:
                                loan.setExpectedAmount(getNumericValueSafely(cell));
                                break;
//                            case 14:
//                                loan.setAmountRefunded(cell.getNumericCellValue());///not present in new report
//                                break;
                            case 13:
                                loan.setLoanStatus(getStringValueSafely(cell));
                                break;
                            case 15:
                                Double dpdValue = getNumericValueSafely(cell);
                                loan.setDpd(dpdValue != null ? String.valueOf(dpdValue.longValue()) : null);
                                break;

                            default:
                                break;
                        }
                        cellIdx++;
                    }
                    if (loan.getLoanStatus().equals("NPA")) {
                        loan.setLoanStatus("CLOSED");
                    }
                    if (loan.getLoanStatus() != null && "CANCELLED".equalsIgnoreCase(loan.getLoanStatus())){/// chk if any cancel in report
//                        loan.setLoanStatus("CLOSED");
//                        loan.setAmountReceived(loan.getAmountRefunded());
                        continue;// skip cancelled loans
                    }
                    loans.add(loan);
                }
            }
        } catch (IOException e) {
            LOGGER.error("An error occurred while performing the operation: {}", e.getMessage(), e);
        }
        return loans;
    }

    public static List<InvestmentPojo> excelToInvestmentPojo(InputStream is) {
        try {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheet("Mother's dhan account");
            Iterator<Row> rows = sheet.iterator();
            List<InvestmentPojo> loans = new ArrayList<>();
            int rowNumber = 0;
            boolean endRead = false;
            while (rows.hasNext()) {
                if (endRead)
                    break;
                Row currentRow = rows.next();
                // skip header
                if (rowNumber <= 0) {
                    rowNumber++;
                    continue;
                }
                Iterator<Cell> cellsInRow = currentRow.iterator();
                InvestmentPojo loan = new InvestmentPojo();
                int cellIdx = 0;
                while (cellsInRow.hasNext()) {
                    if (endRead)
                        break;
                    Cell currentCell = cellsInRow.next();
                    switch (cellIdx) {
                        case 0:
                            String cellValue = getStringValueSafely(currentCell);
                            if ("Total Investment Amount".equalsIgnoreCase(cellValue != null ? cellValue.trim() : "")) {
                                endRead = true;
                                break;
                            }
                            loan.setParticular(cellValue);
                            break;
                        case 1:
                            Double invAmount = getNumericValueSafely(currentCell);
                            loan.setInvAmount(invAmount);
                            break;
                        case 3:
                            Double marketValue = getNumericValueSafely(currentCell);
                            loan.setMarketValue(marketValue);
                            break;
                        case 8:
                            loan.setInvestmentStatus(getStringValueSafely(currentCell));
                            break;
                        default:
                            break;
                    }
                    cellIdx++;
                }
                if (!endRead)
                    loans.add(loan);
            }
            workbook.close();
            return loans;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }
}
