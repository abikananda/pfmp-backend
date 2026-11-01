package com.techconsulting.pfmp.service;

import com.techconsulting.pfmp.domain.FinancialYear;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Converter(autoApply = true)
public class FinancialYearConverter implements AttributeConverter<FinancialYear, String> {

    @Override
    public String convertToDatabaseColumn(FinancialYear financialYear) {
        log.debug("Converting FinancialYear to database column");
        if (financialYear != null) {
            String yearFormat = financialYear.getYearFormat();
            log.debug("Converted FinancialYear to: {}", yearFormat);
            return yearFormat;
        }
        log.debug("FinancialYear is null, returning null");
        return null;
    }

    @Override
    public FinancialYear convertToEntityAttribute(String yearString) {
        log.debug("Converting String to FinancialYear: {}", yearString);
        FinancialYear result = FinancialYear.fromString(yearString);
        log.debug("Successfully converted String to FinancialYear");
        return result;
    }
}
