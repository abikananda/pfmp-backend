package com.techconsulting.pfmp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class InvestmentAllocation {
    private String name;
    private double expected;
    private double actual;
}
