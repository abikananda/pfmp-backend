package com.techconsulting.pfmp.model;

import lombok.Data;

@Data
public class DashStats {
    private Double realisedReturnAmt = 0.0;
    private Double realisedReturnPer = 0.0;
    private Double totalFundInvested = 0.0;
    private Double totalFundInWallet = 0.0;
    private Double amountLoadedToWallet = 0.0;
    private Double actualAmountInWallet = 0.0;
    private Double balanceAmountInBank = 0.0;
    private Double charges = 0.0;
    private Long totalNumberOfLoans = 0L;
    private Long numberOfActiveLoans = 0L;

}
