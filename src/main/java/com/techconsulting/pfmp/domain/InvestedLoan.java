package com.techconsulting.pfmp.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;


@Entity
@Table(name = "InvestedLoans")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class InvestedLoan {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String loanId;

    @Column(length = 50)
    private String loanType;

    @Column(length = 50)
    private Double tenure;

    @Column(length = 50)
    private Double marketValue;

    @Column(length = 50)
    private Double returnAmt;

    @Column(length = 50)
    private Double returnPer;

    @Column(length = 50)
    private String tenureType;

    @Column(length = 50)
    private String dpd;

    @Column(length = 50)
    private String defaultRepaymentMode;

    @Column(length = 50)
    private String loanAmount;

    @Column(length = 50)
    private Double amountReceived;
    @Column(length = 50)
    private Double expectedAmount;
    @Column(length = 50)
    private Double investedAmount;

    @Column(length = 50)
    private String schemeId;
    @Column(length = 50)
    private String source;

    @Column(length = 50)
    private Double borrowerAPRPercent;

    @Column(length = 50)
    private String collectionCharges;

    @Column(length = 50)
    private String facilitationFee;

    @Column(length = 50)
    private String investmentDate;

    @Column(length = 50)
    private String loanPurpose;

    @Column(length = 50)
    private String loanStatus;

    @Column(length = 50)
    private String receivableAPRPercent;

    @Column(length = 50)
    private String recoveryFee;

    @Column(length = 50)
    private String remainingAmount;

    @Column(length = 50)
    private String borrowerName;

    @Column(length = 50)
    private String borrowerAge;

    @Column(length = 50)
    private String borrowerGender;

    @Column(length = 50)
    private String borrowerMaritalStatus;

    @Column(length = 50)
    private String borrowerStayType;

    @Column(length = 50)
    private String borrowerCity;

    @Column(length = 50)
    private String scoreRange;

    @Column(length = 50)
    private String lendenScore;

    @Column(length = 50)
    private String risk;

    @Column(length = 50)
    private String profession;

    @Column(length = 50)
    private String employmentStatus;

    @Column(length = 50)
    private String income;

    @Column(length = 50)
    private String employmentType;

    @Column(length = 50)
    private String entityName;

    @Column(length = 50)
    private String employer;

    @Column(length = 50)
    private String businessVintage;

    @Column(length = 50)
    private String businessHq;

    @Column(length = 50)
    private String annualTurnover;

    @Column(length = 50)
    private Double amountRefunded;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @LastModifiedDate
    @Column(nullable = false)
    private OffsetDateTime lastUpdated;

    @CreatedBy
    @Column(nullable = false, updatable = false)
    private String createdBy;

    @LastModifiedBy
    @Column(nullable = false)
    private String updatedBy;

}
