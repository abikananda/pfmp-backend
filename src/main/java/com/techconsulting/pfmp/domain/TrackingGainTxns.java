package com.techconsulting.pfmp.domain;

import com.techconsulting.pfmp.service.FinancialYearConverter;
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
@Table(name = "tracking_gain_txns")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class TrackingGainTxns {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String particular;
    private Double gainAmt;
    @Convert(converter = FinancialYearConverter.class) // Converts enum <-> String
    @Column(name = "financial_year", nullable = false)
    private FinancialYear financialYear;
    @Enumerated(EnumType.STRING)
    private InvestmentStatus status;
    private String comments;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false,foreignKey = @ForeignKey(name = "FK_TrackingGainTxns_account"))
    private TrackingOthersAccount trackingOthersAccount;


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
