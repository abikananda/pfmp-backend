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
        name = "DhanPortfolio",
        uniqueConstraints = @UniqueConstraint(name = "UK_DhanPortfolio_user",columnNames = {"user_id"})
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class DhanPortfolio {
    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "totalInvestmentAmt")
    private Double totalInvestmentAmt;
    @Column(name = "portfolioReturn")
    private Double portfolioReturn;
    @Column(name = "totalExcessReturnAmt")
    private Double totalExcessReturnAmt;
    @Column(name = "totalFundInWallet")
    private Double totalFundInWallet;
    @Column(name = "actualAmtInWallet")
    private Double actualAmtInWallet;
    @Column(name = "chargerAmt")
    private Double chargerAmt;
    @Column(name = "amountTransferToBank")
    private Double amountTransferToBank;
    @Column(name = "amountLoadedToWallet")
    private Double amountLoadedToWallet;

    @Enumerated(EnumType.STRING)
    @Column(name = "investmentType")
    private InvestmentType investmentType;
    @Column(name = "totalReInvestAmt")
    private Double totalReInvestAmt;

    // One portfolio can have many investments
    @OneToMany(mappedBy = "dhanPortfolio")
    @Column(name = "investments")
    private List<DhanInvestment> dhanInvestments;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id",foreignKey = @ForeignKey(name = "FK_DhanPortfolio_user"))
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
