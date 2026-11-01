package com.techconsulting.pfmp.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DatabaseInitializer {

    @Autowired
    CommonRepository commonRepository;
    @Value("${sql.initialize.flag}")
    boolean initializeFlag;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        log.info("SQL script flag is: {}", initializeFlag);
        System.out.println("✅ SQL script flag is " + initializeFlag);
        if (initializeFlag){
            log.info("First time initialization - running all SQL scripts");
            System.out.println("✅ First time! So running all SQL scripts ");
            try {
                commonRepository.insertRoles();
                log.debug("Roles inserted successfully");

                commonRepository.createVew();
                log.debug("View created successfully");

                commonRepository.createNpaLoanLogTable();
                log.debug("NPA Loan Log table created successfully");

                commonRepository.createTriggers();
                log.debug("Triggers created successfully");

                commonRepository.createLoanTransitionMetricsTable();
                log.debug("Loan Transition Metrics table created successfully");

                commonRepository.createDefaultBorrowersTable();
                log.debug("Default Borrowers table created successfully");

                commonRepository.createTrustedBorrowerTable();
                log.debug("Trusted Borrower table created successfully");

                commonRepository.createBorrowerLoansTable();
                log.debug("Borrower Loans table created successfully");

                log.info("All SQL initialization scripts completed successfully");
            } catch (Exception e) {
                log.error("Error during SQL initialization", e);
                throw e;
            }
        }else {
            log.info("All SQL setup already done");
            System.out.println("✅ All SQL setup already done! ");
        }

    }

}

