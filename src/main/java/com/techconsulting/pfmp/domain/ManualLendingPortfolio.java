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
import java.util.List;

@Entity
@Table(
        name = "ManualLendingPortfolio",
        uniqueConstraints = @UniqueConstraint(name = "UK_ManualLendingPortfolio_user",columnNames = {"user_id"})
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class ManualLendingPortfolio {
    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "totalInvestmentAmt")
    private Double totalInvestmentAmt;
    @Column(name = "amountCreditInBankAccount")
    private Double amountCreditInBankAccount;
    @Column(name = "totalFundOnLendenInvested")
    private Double totalFundOnLendenInvested;
    @Column(name = "portfolioReturnPer")
    private Double portfolioReturnPer;
    @Column(name = "portfolioReturnAmt")
    private Double portfolioReturnAmt;
    @Column(name = "investmentPrincipalAmt")
    private Double investmentPrincipalAmt;
    @Column(name = "amtYetToBeInvest")
    private Double amtYetToBeInvest;
    @Column(name = "totalNPA")
    private Integer totalNPA;

    @Enumerated(EnumType.STRING)
    @Column(name = "investmentType")
    private InvestmentType investmentType;

    // One portfolio can have many investments
    @OneToMany(mappedBy = "manualLendingPortfolio")
    @Column(name = "investments")
    private List<ManualLendingInvestment> manualLendingInvestments;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id",foreignKey = @ForeignKey(name = "FK_ManualLendingPortfolio_user"))
    private User user;

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
