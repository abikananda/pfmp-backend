package com.techconsulting.pfmp.domain;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FinancialYear {
    private final String yearFormat;

    // Predefined values
    public static final FinancialYear UNKNOWN = new FinancialYear("UNKNOWN");

    // Dynamic storage
    private static final Map<String, FinancialYear> DYNAMIC_YEARS = new ConcurrentHashMap<>();

    private FinancialYear(String yearFormat) {
        this.yearFormat = yearFormat;
    }

    public String getYearFormat() {
        return yearFormat;
    }

    // ✅ Return predefined values or create a new instance dynamically
    public static FinancialYear fromString(String yearString) {
        if (yearString == null || !yearString.matches("^FY\\d{4}_\\d{2}$")) {
            return UNKNOWN;
        }

        // ✅ Store and return the dynamic value
        return DYNAMIC_YEARS.computeIfAbsent(yearString, FinancialYear::new);
    }
    public static String getCurrentFinancialYear() {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();
        int startYear = (month >= 4) ? year : year - 1;
        int endYear = startYear + 1;

        return "FY" + startYear + "_" + (endYear % 100);
    }
    @Override
    public String toString() {
        return yearFormat;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        FinancialYear that = (FinancialYear) obj;
        return yearFormat.equals(that.yearFormat);
    }

    @Override
    public int hashCode() {
        return yearFormat.hashCode();
    }
}
