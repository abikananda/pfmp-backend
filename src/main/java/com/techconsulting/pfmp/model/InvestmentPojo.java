package com.techconsulting.pfmp.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.OffsetDateTime;


@Getter
@Setter
@ToString
public class InvestmentPojo {

    private Long id;
    private String particular;
    private String schemeId;
    private Double invAmount;
    private Double otherIncome;
    private Double marketValue;
    private Double excessReturn;
    private Double invReturnPer;
    private Double amtCreditInBankAccount;
    private Double invWeight;
    private Double weightWithReturn;
    private String investmentStatus;
    private String loanStatus;
    private Double reinvestedAmt;
    private LocalDate maturityDate;
    private LocalDate investmentDate;
    private LocalDate lastEMIPaidOn;
    private Double tenure;
    private String npa;
    private Integer dpd;
    private String comments;
    private OffsetDateTime dateCreated;
    private OffsetDateTime lastUpdated;
    private String createdBy;
    private String updatedBy;

}
