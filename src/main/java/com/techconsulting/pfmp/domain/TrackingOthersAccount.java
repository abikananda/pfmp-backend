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
@Table(name = "tracking_others_account")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class TrackingOthersAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // this is the current total balance
    private Double currentAccBalance;
    // this is the total amount Present on April 1st
    private Double currentFYPrincipalAmt;
    private Double currentFYReturnPer;
    private Double currentFYReturnAmount;
    // this is the total amount transfer from my account to Mom's account
    private Double totAmtTransferToAcc;
    // this is the total return and profit amount on amt sent to Mom's account
    private Double totalReturnPer;
    private Double totalReturnAmount;
    private String comments;

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

    @OneToMany(mappedBy = "trackingOthersAccount",fetch = FetchType.LAZY)
    @Column(name = "tracking_gain_txns")
    private List<TrackingGainTxns> trackingGainTxns;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false,foreignKey = @ForeignKey(name = "FK_TrackingOthersAccount_user"))
    private User user;

    public TrackingOthersAccount() {
        this.currentAccBalance = 0.0;
        this.currentFYPrincipalAmt = 300000.0;//Mom's added this to make double
        this.currentFYReturnPer = 0.0;
        this.currentFYReturnAmount = 0.0;
        this.totAmtTransferToAcc = 300000.0;//Mom's added this to make double
        this.totalReturnPer = 0.0;
        this.totalReturnAmount = 0.0;
    }
}
