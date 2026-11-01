package com.techconsulting.pfmp;

import com.techconsulting.pfmp.controller.AuthController;
import com.techconsulting.pfmp.controller.InvestmentController;
import com.techconsulting.pfmp.controller.LoanController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class AllControllerTest {
    @Autowired
    private AuthController authController;
    @Autowired
    private InvestmentController investmentController;
    @Autowired
    private LoanController loanController;

    @Test
    void contextLoads() throws Exception {
        assertThat(authController).isNotNull();
        assertThat(investmentController).isNotNull();
        assertThat(loanController).isNotNull();
    }
}
