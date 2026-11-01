package com.techconsulting.pfmp.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.OffsetDateTime;


@Getter
@Setter
@ToString
public class FixedInvestmentPojo {
    private Long id;
    private String particular;
    private Double invAmount;
    private Double invReturn;
    private Double invWeight;
    private Double weightWithReturn;
    private LocalDate maturityDate;
    private Double otherIncome;
    private String investmentStatus;
    private String comments;

    private OffsetDateTime dateCreated;
    private OffsetDateTime lastUpdated;
    private String createdBy;
    private String updatedBy;

}
