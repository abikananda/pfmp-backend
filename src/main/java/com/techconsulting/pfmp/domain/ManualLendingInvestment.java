package com.techconsulting.pfmp.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "ManualLendingInvestment",
        uniqueConstraints = @UniqueConstraint(name = "UK_ManualLendingInvestment_scheme_portfolio",columnNames = {"scheme_id", "portfolio_id"})
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class ManualLendingInvestment {
    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String schemeId;
    private String particular;
    private Double invAmount;
    private Double marketValue;
    private Double invReturnAmt;
    private Double invReturnPer;

    @Enumerated(EnumType.STRING)
    private InvestmentStatus investmentStatus;
    private Double amtCreditInBankAccount;
    private Double tenure;
    @Enumerated(EnumType.STRING)
    private InvestmentStatus loanStatus;
    private boolean isNPA;
    private Integer dpd;
    private String comments;
    private LocalDate investmentDate;
    @Column(name = "last_emi_paid_on")
    private LocalDate lastEMIPaidOn;

    // Many investments belong to one portfolio
    @ManyToOne
    @JoinColumn(name = "portfolio_id", nullable = false,foreignKey = @ForeignKey(name = "FK_ManualLendingInvestment_portfolio"))
    private ManualLendingPortfolio manualLendingPortfolio;

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
