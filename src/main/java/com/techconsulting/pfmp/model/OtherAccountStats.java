package com.techconsulting.pfmp.model;

import lombok.Data;

@Data
public class OtherAccountStats {
    private Double currentAccBalance = 0.0;
    private Double currentFYPrincipalAmt = 0.0;
    private Double currentFYReturnPer = 0.0;
    private Double currentFYUnrealisedReturnPer = 0.0;
    private Double currentFYReturnAmount = 0.0;
    private Double currentFYUnrealisedReturnAmount = 0.0;
    private Double totAmtTransferToAcc = 0.0;
    private Double totalReturnPer = 0.0;
    private Double totalReturnAmount = 0.0;

}
