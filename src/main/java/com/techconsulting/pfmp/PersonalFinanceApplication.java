package com.techconsulting.pfmp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;

@Slf4j
@SpringBootApplication
public class PersonalFinanceApplication implements CommandLineRunner {

    public static void main(String[] args) {
        log.info("Starting PersonalFinanceApplication...");
        SpringApplication.run(PersonalFinanceApplication.class, args);
        log.info("PersonalFinanceApplication started successfully.");
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length > 0) {
            log.info("Application started with arguments: {}", Arrays.toString(args));
        } else {
            log.info("No command-line arguments provided.");
        }
    }
}
