package com.techconsulting.pfmp.service;

import com.techconsulting.pfmp.domain.ManualLendingInvestment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManualLendingInvestmentServiceTest {

    @Test
    void clearsNpaWhenGrossAmountReceivedExceedsInvestedAmount() {
        ManualLendingInvestment investment = new ManualLendingInvestment();
        investment.setInvAmount(1000.0);
        investment.setNPA(true);
        investment.setComments("DPD is 90");

        ManualLendingInvestmentService.applyRecoveryNpaOverride(investment, 1000.01);

        assertFalse(investment.isNPA());
        org.junit.jupiter.api.Assertions.assertEquals(
                "Total received exceeds invested amount; NPA not applicable",
                investment.getComments());
    }

    @Test
    void keepsExistingNpaWhenGrossAmountReceivedDoesNotExceedInvestedAmount() {
        ManualLendingInvestment investment = new ManualLendingInvestment();
        investment.setInvAmount(1000.0);
        investment.setNPA(true);

        ManualLendingInvestmentService.applyRecoveryNpaOverride(investment, 1000.0);
        assertTrue(investment.isNPA());

        ManualLendingInvestmentService.applyRecoveryNpaOverride(investment, 999.99);
        assertTrue(investment.isNPA());
    }
}
