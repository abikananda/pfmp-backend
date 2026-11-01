package com.techconsulting.pfmp.config;

import com.techconsulting.pfmp.domain.DhanInvestment;
import com.techconsulting.pfmp.domain.ManualLendingInvestment;
import com.techconsulting.pfmp.model.InvestmentPojo;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ModelMappingConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        Converter<Boolean, String> npaConverter = ctx -> ctx.getSource() ? "Yes" : "No";
        modelMapper.typeMap(ManualLendingInvestment.class, InvestmentPojo.class)
                .addMappings(mapper -> {
                    mapper.map(ManualLendingInvestment::getInvReturnAmt, InvestmentPojo::setExcessReturn);
                    mapper.map(ManualLendingInvestment::getSchemeId, InvestmentPojo::setParticular);
                    mapper.map(ManualLendingInvestment::getInvestmentStatus, InvestmentPojo::setInvestmentStatus);
                    mapper.using(npaConverter).map(ManualLendingInvestment::isNPA, InvestmentPojo::setNpa);
                });
        modelMapper.typeMap(DhanInvestment.class, InvestmentPojo.class)
                .addMappings(mapper -> mapper.map(DhanInvestment::getInvReturn, InvestmentPojo::setInvReturnPer));

        return modelMapper;
    }

}
