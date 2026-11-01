package com.techconsulting.pfmp.model;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;


@Getter
@Setter
public class InvestedLoanPojo {

    private Long id;
    private Double tenure;
    private Double marketValue;
    private Double returnAmt;
    private Double returnPer;
    private Double amountReceived;
    private Double investedAmount;
    private String schemeId;
    private String investmentDate;
    private OffsetDateTime dateCreated;
    private OffsetDateTime lastUpdated;
    private String createdBy;
    private String updatedBy;

}
