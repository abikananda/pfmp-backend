package com.techconsulting.pfmp.service;

import com.techconsulting.pfmp.domain.FixedInvestment;
import com.techconsulting.pfmp.domain.FixedPortfolio;
import com.techconsulting.pfmp.exception.InvestmentException;

public interface FixedInvestmentService {
    void calculatePortfolio(FixedPortfolio portfolio);

    void doInvestment(FixedInvestment investment) throws InvestmentException;

    void updateInvestment(FixedInvestment investment) throws InvestmentException;

}
