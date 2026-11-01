package com.techconsulting.pfmp.utility;

import com.techconsulting.pfmp.domain.InvestedLoan;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExcelHelperTest {

    @Test
    void parsesNewInvestmentReportAndUsesGrossTotalReceived() throws Exception {
        byte[] report = createNewReport();

        List<InvestedLoan> loans = ExcelHelper.excelToInvestedLoans(new ByteArrayInputStream(report));

        assertEquals(1, loans.size());
        InvestedLoan loan = loans.get(0);
        assertEquals("LOA-NEW12345", loan.getSchemeId());
        assertEquals("31/10/2026", loan.getInvestmentDate());
        assertEquals(2000.0, loan.getInvestedAmount(), 0.001);
        assertEquals(35.52, loan.getBorrowerAPRPercent(), 0.001);
        assertEquals(6.0, loan.getTenure(), 0.001);
        assertEquals("ACTIVE", loan.getLoanStatus());
        assertEquals("0", loan.getDpd());

        // Total Received is ₹277.70; Net Received is ₹270.20.
        // Existing portfolio calculations intentionally continue using gross receipts.
        assertEquals(277.70, loan.getAmountReceived(), 0.001);
    }

    @Test
    void continuesToParseLegacyManualLendingReport() throws Exception {
        byte[] report = createLegacyReport();

        List<InvestedLoan> loans = ExcelHelper.excelToInvestedLoans(new ByteArrayInputStream(report));

        assertEquals(1, loans.size());
        InvestedLoan loan = loans.get(0);
        assertEquals("LOA-OLD12345", loan.getSchemeId());
        assertEquals("04/10/2026", loan.getInvestmentDate());
        assertEquals(500.0, loan.getInvestedAmount(), 0.001);
        assertEquals(36.48, loan.getBorrowerAPRPercent(), 0.001);
        assertEquals(4.0, loan.getTenure(), 0.001);
        assertEquals(25.50, loan.getAmountReceived(), 0.001);
        assertEquals("ACTIVE", loan.getLoanStatus());
        assertEquals("1", loan.getDpd());
    }

    private byte[] createNewReport() throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("Loan Level Summary");
            Row lenderRow = sheet.createRow(3);
            lenderRow.createCell(0).setCellValue("User ID");
            lenderRow.createCell(1).setCellValue("IAKI7TL1UT6K");

            Row header = sheet.createRow(23);
            String[] headers = {
                    "Lending Type", "Product", "Order ID", "Loan ID", "Repayment Type",
                    "Disbursement Date", "Repayment Start Date", "Invested Amount (₹)",
                    "Interest Rate (%p.a.)", "Tenure (in months)", "LDC Score", "Loan Status",
                    "DPD", "Total Received (₹)", "Principal Received (₹)", "Interest Received (₹)",
                    "NPA Recovery (₹)", "Platform Fee (₹)", "Recovery Fees (₹)",
                    "Recovery Expenses (₹)", "Net Received  (sent to bank) (₹)",
                    "Absolute Return (%)", "Annualised Net Return (%)",
                    "Principal Outstanding (₹)", "NPA (₹)", "Closure / NPA Date"
            };
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }

            Row data = sheet.createRow(24);
            data.createCell(0).setCellValue("Manual");
            data.createCell(1).setCellValue("Personal Loan");
            data.createCell(2).setCellValue("84027121873586");
            data.createCell(3).setCellValue("LOA-NEW12345");
            data.createCell(4).setCellValue("Monthly");
            data.createCell(5).setCellValue("31/10/2026");
            data.createCell(6).setCellValue("01/12/2026");
            data.createCell(7).setCellValue(2000);
            data.createCell(8).setCellValue(35.52);
            data.createCell(9).setCellValue(6);
            data.createCell(11).setCellValue("ACTIVE");
            data.createCell(12).setCellValue("0");
            data.createCell(13).setCellValue(277.70);
            data.createCell(20).setCellValue(270.20);

            workbook.write(output);
            return output.toByteArray();
        }
    }

    private byte[] createLegacyReport() throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("Sheet");
            Row header = sheet.createRow(19);
            String[] headers = {
                    "Order ID", "Loan ID", "Disbursement Date", "Disbursed Amount (₹)",
                    "Repayment Type", "Repayment Start Date (expected)",
                    "Total Repayment Amount (illustrative) (₹)", "Total Amount Received (₹)",
                    "Principal Received (₹)", "Interest Received (₹)", "Platform Fee (₹)",
                    "Profit & Loss (₹)", "NPA", "Loan Status", "Loan Closure/NPA Date",
                    "DPD (days past due)", "Interest Rate (%)", "Tenure (months)", "LenDenClub Score"
            };
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }

            Row data = sheet.createRow(20);
            data.createCell(0).setCellValue("39737614322362");
            data.createCell(1).setCellValue("LOA-OLD12345");
            data.createCell(2).setCellValue("04/10/2026");
            data.createCell(3).setCellValue(500);
            data.createCell(4).setCellValue("Monthly");
            data.createCell(5).setCellValue("01/11/2026");
            data.createCell(6).setCellValue(560.79);
            data.createCell(7).setCellValue(25.50);
            data.createCell(13).setCellValue("ACTIVE");
            data.createCell(15).setCellValue(1);
            data.createCell(16).setCellValue(36.48);
            data.createCell(17).setCellValue(4);

            workbook.write(output);
            return output.toByteArray();
        }
    }
}
