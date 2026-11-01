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
        name = "fixedPortfolio",
        uniqueConstraints = @UniqueConstraint(name = "UK_FixedPortfolio_user_investment",columnNames = {"user_id","investment_type"})
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class FixedPortfolio {
    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "totalInvestmentAmt")
    private Double totalInvestmentAmt;
    @Column(name = "portfolioReturn")
    private Double portfolioReturn;

    @Enumerated(EnumType.STRING)
    @Column(name = "investment_type",nullable = false)
    private InvestmentType investmentType;

    @OneToMany(mappedBy = "fixedPortfolio")
    @Column(name = "fixedInvestments")
    private List<FixedInvestment> fixedInvestments;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false,foreignKey = @ForeignKey(name = "FK_FixedPortfolio_user"))
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
