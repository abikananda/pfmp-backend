package com.techconsulting.pfmp.utility;

import com.techconsulting.pfmp.controller.InvestmentController;
import com.techconsulting.pfmp.domain.InvestedLoan;
import com.techconsulting.pfmp.model.InvestmentPojo;
import com.techconsulting.pfmp.security.services.UserDetailsImpl;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class ExcelHelper {
    public static final String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final Logger LOGGER = LoggerFactory.getLogger(InvestmentController.class);
    private static final ThreadLocal<DataFormatter> DATA_FORMATTER = ThreadLocal.withInitial(() -> new DataFormatter(Locale.ROOT));

    private static final String[] LOAN_ID_HEADERS = {"Loan ID"};
    private static final String[] DATE_HEADERS = {"Disbursement Date"};
    private static final String[] INVESTED_AMOUNT_HEADERS = {"Disbursed Amount (₹)", "Invested Amount (₹)"};
    private static final String[] INTEREST_RATE_HEADERS = {"Interest Rate (%)", "Interest Rate (%p.a.)"};
    private static final String[] TENURE_HEADERS = {"Tenure (months)", "Tenure (in months)"};
    private static final String[] RECEIVED_AMOUNT_HEADERS = {"Total Amount Received (₹)", "Total Received (₹)"};
    private static final String[] STATUS_HEADERS = {"Loan Status"};
    private static final String[] DPD_HEADERS = {"DPD (days past due)", "DPD"};
    private static final String[] EXPECTED_AMOUNT_HEADERS = {"Total Repayment Amount (illustrative) (₹)"};

    private ExcelHelper() {
    }

    public static boolean hasExcelFormat(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        return TYPE.equalsIgnoreCase(contentType)
                || (filename != null && filename.toLowerCase(Locale.ROOT).endsWith(".xlsx"));
    }

    /**
     * Validate the report by its workbook contents and lender ID. LenDenClub has
     * changed the export filename and worksheet name, so neither is used as a
     * report-format identifier.
     */
    public static boolean validateExcelFile(MultipartFile file) throws IOException {
        LOGGER.debug("Validating manual lending Excel file: {}", file == null ? null : file.getOriginalFilename());
        if (!hasExcelFormat(file)) {
            return false;
        }

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            UserDetailsImpl user = SecurityUtil.getLoggedInUser();
            if (user == null || user.getOtherDetails() == null) {
                LOGGER.warn("Unable to determine the logged-in lender while validating report");
                return false;
            }

            Object lenderIdValue = user.getOtherDetails().get("lendenClub_lender_id");
            String lenderId = lenderIdValue == null ? null : lenderIdValue.toString().trim();
            if (lenderId == null || lenderId.isEmpty()) {
                LOGGER.warn("Logged-in user does not have a LenDenClub lender ID");
                return false;
            }

            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                int headerRow = findHeaderRow(sheet);
                if (headerRow < 0) {
                    continue;
                }

                String reportLenderId = findReportLenderId(sheet);
                boolean valid = lenderId.equalsIgnoreCase(reportLenderId);
                LOGGER.info("Manual lending report validation: sheet={}, headerRow={}, lenderMatch={}",
                        sheet.getSheetName(), headerRow, valid);
                return valid;
            }
            LOGGER.warn("No supported manual lending report headers found in workbook");
            return false;
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException e) {
            LOGGER.error("Error validating manual lending Excel file", e);
            throw e;
        }
    }

    private static String findReportLenderId(Sheet sheet) {
        for (Row row : sheet) {
            for (Cell cell : row) {
                if ("User ID".equalsIgnoreCase(getStringValueSafely(cell).trim())) {
                    return getStringValueSafely(row.getCell(cell.getColumnIndex() + 1)).trim();
                }
            }
        }
        return null;
    }

    private static int findHeaderRow(Sheet sheet) {
        if (sheet == null) {
            return -1;
        }
        for (Row row : sheet) {
            Map<String, Integer> columns = getHeaderColumns(row);
            if (hasRequiredColumns(columns)) {
                return row.getRowNum();
            }
        }
        return -1;
    }

    private static Map<String, Integer> getHeaderColumns(Row row) {
        Map<String, Integer> columns = new HashMap<>();
        if (row == null) {
            return columns;
        }
        for (Cell cell : row) {
            String header = normalizeHeader(getStringValueSafely(cell));
            if (!header.isEmpty()) {
                columns.put(header, cell.getColumnIndex());
            }
        }
        return columns;
    }

    private static boolean hasRequiredColumns(Map<String, Integer> columns) {
        return hasAny(columns, LOAN_ID_HEADERS)
                && hasAny(columns, DATE_HEADERS)
                && hasAny(columns, INVESTED_AMOUNT_HEADERS)
                && hasAny(columns, INTEREST_RATE_HEADERS)
                && hasAny(columns, TENURE_HEADERS)
                && hasAny(columns, RECEIVED_AMOUNT_HEADERS)
                && hasAny(columns, STATUS_HEADERS)
                && hasAny(columns, DPD_HEADERS);
    }

    private static boolean hasAny(Map<String, Integer> columns, String... aliases) {
        for (String alias : aliases) {
            if (columns.containsKey(normalizeHeader(alias))) {
                return true;
            }
        }
        return false;
    }

    private static int findColumn(Map<String, Integer> columns, String... aliases) {
        for (String alias : aliases) {
            Integer index = columns.get(normalizeHeader(alias));
            if (index != null) {
                return index;
            }
        }
        return -1;
    }

    private static String normalizeHeader(String value) {
        return value == null ? "" : value.replace('\u00A0', ' ').trim()
                .replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private static Double getNumericValueSafely(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                double value = cell.getNumericCellValue();
                return Double.isFinite(value) ? value : null;
            }
            String value = DATA_FORMATTER.get().formatCellValue(cell).trim();
            if (value.isEmpty() || "-".equals(value) || "—".equals(value) || "–".equals(value)) {
                return null;
            }
            value = value.replace(",", "").replace("₹", "").replace("%", "").trim();
            double parsed = Double.parseDouble(value);
            return Double.isFinite(parsed) ? parsed : null;
        } catch (Exception e) {
            LOGGER.warn("Could not parse numeric value from cell {}", cell.getAddress(), e);
            return null;
        }
    }

    private static String getStringValueSafely(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return "";
        }
        try {
            return DATA_FORMATTER.get().formatCellValue(cell).trim();
        } catch (Exception e) {
            LOGGER.warn("Could not read string value from cell {}", cell.getAddress(), e);
            return "";
        }
    }

    private static String getDateValueSafely(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
            String value = getStringValueSafely(cell);
            if (value.isEmpty() || "—".equals(value) || "-".equals(value)) {
                return null;
            }
            for (DateTimeFormatter formatter : List.of(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                    DateTimeFormatter.ISO_LOCAL_DATE,
                    DateTimeFormatter.ofPattern("dd-MM-yyyy"))) {
                try {
                    return LocalDate.parse(value, formatter).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                } catch (DateTimeParseException ignored) {
                    // Try the next supported date format.
                }
            }
            LOGGER.warn("Unrecognised disbursement date value: {}", value);
            return null;
        } catch (Exception e) {
            LOGGER.warn("Could not parse date from cell {}", cell.getAddress(), e);
            return null;
        }
    }

    public static List<InvestedLoan> excelToInvestedLoans(InputStream is) {
        LOGGER.debug("Converting manual lending Excel report to InvestedLoan records");
        List<InvestedLoan> loans = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(is)) {
            Sheet reportSheet = null;
            int headerRowIndex = -1;
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet candidate = workbook.getSheetAt(i);
                int candidateHeaderRow = findHeaderRow(candidate);
                if (candidateHeaderRow >= 0) {
                    reportSheet = candidate;
                    headerRowIndex = candidateHeaderRow;
                    break;
                }
            }

            if (reportSheet == null) {
                throw new IllegalArgumentException("Unsupported manual lending report: required loan columns were not found");
            }

            Map<String, Integer> columns = getHeaderColumns(reportSheet.getRow(headerRowIndex));
            int loanIdColumn = findColumn(columns, LOAN_ID_HEADERS);
            int dateColumn = findColumn(columns, DATE_HEADERS);
            int investedAmountColumn = findColumn(columns, INVESTED_AMOUNT_HEADERS);
            int interestRateColumn = findColumn(columns, INTEREST_RATE_HEADERS);
            int tenureColumn = findColumn(columns, TENURE_HEADERS);
            int receivedAmountColumn = findColumn(columns, RECEIVED_AMOUNT_HEADERS);
            int statusColumn = findColumn(columns, STATUS_HEADERS);
            int dpdColumn = findColumn(columns, DPD_HEADERS);
            int expectedAmountColumn = findColumn(columns, EXPECTED_AMOUNT_HEADERS);

            for (int rowIndex = headerRowIndex + 1; rowIndex <= reportSheet.getLastRowNum(); rowIndex++) {
                Row dataRow = reportSheet.getRow(rowIndex);
                if (dataRow == null) {
                    continue;
                }

                String loanId = getStringValueSafely(dataRow.getCell(loanIdColumn));
                Double investedAmount = getNumericValueSafely(dataRow.getCell(investedAmountColumn));
                String loanStatus = getStringValueSafely(dataRow.getCell(statusColumn));

                // Ignore blank rows, report footers and summary rows.
                if (loanId.isEmpty() || investedAmount == null || loanStatus.isEmpty()) {
                    continue;
                }

                if ("CANCELLED".equalsIgnoreCase(loanStatus)) {
                    continue;
                }

                InvestedLoan loan = new InvestedLoan();
                loan.setSchemeId(loanId);
                loan.setInvestmentDate(getDateValueSafely(dataRow.getCell(dateColumn)));
                loan.setInvestedAmount(investedAmount);
                loan.setBorrowerAPRPercent(getNumericValueSafely(dataRow.getCell(interestRateColumn)));
                loan.setTenure(getNumericValueSafely(dataRow.getCell(tenureColumn)));

                // IMPORTANT: map gross Total Received, not Net Received (sent to bank).
                // This preserves the existing portfolio calculation behaviour.
                Double grossTotalReceived = getNumericValueSafely(dataRow.getCell(receivedAmountColumn));
                loan.setAmountReceived(grossTotalReceived == null ? 0.0 : grossTotalReceived);

                if (expectedAmountColumn >= 0) {
                    loan.setExpectedAmount(getNumericValueSafely(dataRow.getCell(expectedAmountColumn)));
                }

                loan.setLoanStatus("NPA".equalsIgnoreCase(loanStatus) ? "CLOSED" : loanStatus.toUpperCase(Locale.ROOT));
                Double dpd = getNumericValueSafely(dataRow.getCell(dpdColumn));
                loan.setDpd(dpd == null ? "0" : String.valueOf(dpd.longValue()));
                loans.add(loan);
            }
        } catch (IOException e) {
            LOGGER.error("An error occurred while parsing the manual lending Excel report: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Unable to read manual lending Excel report", e);
        }

        if (loans.isEmpty()) {
            throw new IllegalArgumentException("No valid loan rows were found in the manual lending report");
        }
        LOGGER.info("Extracted {} manual lending loans from report", loans.size());
        return loans;
    }

    public static List<InvestmentPojo> excelToInvestmentPojo(InputStream is) {
        try (Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheet("Mother's dhan account");
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet Mother's dhan account was not found");
            }
            Iterator<Row> rows = sheet.iterator();
            List<InvestmentPojo> loans = new ArrayList<>();
            int rowNumber = 0;
            boolean endRead = false;
            while (rows.hasNext() && !endRead) {
                Row currentRow = rows.next();
                if (rowNumber++ == 0) {
                    continue;
                }

                InvestmentPojo loan = new InvestmentPojo();
                for (int cellIdx = 0; cellIdx < currentRow.getLastCellNum(); cellIdx++) {
                    Cell currentCell = currentRow.getCell(cellIdx);
                    switch (cellIdx) {
                        case 0:
                            String cellValue = getStringValueSafely(currentCell);
                            if ("Total Investment Amount".equalsIgnoreCase(cellValue)) {
                                endRead = true;
                                break;
                            }
                            loan.setParticular(cellValue);
                            break;
                        case 1:
                            loan.setInvAmount(getNumericValueSafely(currentCell));
                            break;
                        case 3:
                            loan.setMarketValue(getNumericValueSafely(currentCell));
                            break;
                        case 8:
                            loan.setInvestmentStatus(getStringValueSafely(currentCell));
                            break;
                        default:
                            break;
                    }
                }
                if (!endRead) {
                    loans.add(loan);
                }
            }
            return loans;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage(), e);
        }
    }
}
