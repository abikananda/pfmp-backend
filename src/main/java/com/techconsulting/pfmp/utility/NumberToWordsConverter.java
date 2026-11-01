package com.techconsulting.pfmp.utility;

import lombok.extern.slf4j.Slf4j;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
public class NumberToWordsConverter {

    private static final String[] ones = {
            "", "one", "two", "three", "four", "five", "six",
            "seven", "eight", "nine", "ten", "eleven", "twelve",
            "thirteen", "fourteen", "fifteen", "sixteen",
            "seventeen", "eighteen", "nineteen"
    };

    private static final String[] tens = {
            "", "", "twenty", "thirty", "forty", "fifty",
            "sixty", "seventy", "eighty", "ninety"
    };

    private static String convertTwoDigits(int number) {
        if (number < 20) {
            return ones[number];
        }
        return tens[number / 10] + (number % 10 != 0 ? " " + ones[number % 10] : "");
    }

    private static String convertThreeDigits(int number) {
        if (number == 0) return "";
        if (number < 100) return convertTwoDigits(number);
        return ones[number / 100] + " hundred" + (number % 100 != 0 ? " " + convertTwoDigits(number % 100) : "");
    }

    public static String convert(long number) {
        log.debug("Converting number to words: {}", number);
        if (number == 0) {
            log.debug("Number is zero");
            return "zero";
        }

        String result = "";

        int crore = (int) (number / 10000000);
        number %= 10000000;

        int lakh = (int) (number / 100000);
        number %= 100000;

        int thousand = (int) (number / 1000);
        number %= 1000;

        int hundred = (int) number;

        if (crore > 0) {
            result += convertThreeDigits(crore) + " crore ";
        }
        if (lakh > 0) {
            result += convertThreeDigits(lakh) + " lakh ";
        }
        if (thousand > 0) {
            result += convertThreeDigits(thousand) + " thousand ";
        }
        if (hundred > 0) {
            result += convertThreeDigits(hundred);
        }

        return result.trim().replaceAll("\\s+", " ");
    }

//    public static void main(String[] args) {
//        long number = 305000;
//        System.out.println(number + " in words: " + convert(number));
//    }

    public static String getDayWithSuffix(LocalDate today) {
        String dayWithSuffix = "";
        int day = today.getDayOfMonth();

        if (day >= 11 && day <= 13) {
            dayWithSuffix = day + "th";
        }else {
            switch (day % 10) {
                case 1:  dayWithSuffix = day + "st";break;
                case 2:  dayWithSuffix = day + "nd";break;
                case 3:  dayWithSuffix = day + "rd";break;
                default: dayWithSuffix = day + "th";
            }
        }
        String formattedDate = dayWithSuffix + " " + today.format(DateTimeFormatter.ofPattern("MMMM"));
        System.out.println(formattedDate);
        return "update on "+formattedDate;
    }
}

