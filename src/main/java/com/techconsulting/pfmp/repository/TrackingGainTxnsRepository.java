package com.techconsulting.pfmp.repository;

import com.techconsulting.pfmp.domain.TrackingGainTxns;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrackingGainTxnsRepository extends JpaRepository<TrackingGainTxns, Long> {

    List<TrackingGainTxns> findByTrackingOthersAccountId(Long id);

    Optional<TrackingGainTxns> findByParticular(String particular);
}
