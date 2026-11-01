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
        name = "DhanInvestment",
        uniqueConstraints = @UniqueConstraint(name = "UK_DhanInvestment_particular_portfolio",columnNames = {"particular", "portfolio_id"})
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class DhanInvestment {
    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String particular;
    private Double invAmount;
    private Double otherIncome;
    private Double marketValue;
    private Double excessReturn;
    private Double invReturn;
    private Double invWeight;
    private Double weightWithReturn;

    @Enumerated(EnumType.STRING)
    private InvestmentStatus investmentStatus;
    private Double reinvestedAmt;
    private LocalDate maturityDate;

    // Many investments belong to one portfolio
    @ManyToOne
    @JoinColumn(name = "portfolio_id", nullable = false,foreignKey = @ForeignKey(name = "FK_DhanInvestment_portfolio"))
    private DhanPortfolio dhanPortfolio;

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
