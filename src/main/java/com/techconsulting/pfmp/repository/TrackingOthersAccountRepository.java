package com.techconsulting.pfmp.repository;

import com.techconsulting.pfmp.domain.TrackingOthersAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrackingOthersAccountRepository extends JpaRepository<TrackingOthersAccount, Long> {

    Optional<TrackingOthersAccount> findByUserId(Long id);
}
