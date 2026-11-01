package com.techconsulting.pfmp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techconsulting.pfmp.domain.InvestedLoan;
import com.techconsulting.pfmp.domain.ManualLendingInvestment;
import com.techconsulting.pfmp.model.InvestedLoanPojo;
import com.techconsulting.pfmp.repository.CommonRepository;
import com.techconsulting.pfmp.repository.InvestedLoanRepository;
import com.techconsulting.pfmp.repository.ManualLendingInvestmentRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.http.*;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class InvestedLoanService {

    @Autowired
    InvestedLoanRepository investedLoanRepository;
    @Autowired
    ManualLendingInvestmentRepository manualLendingInvestmentRepository;
    @Autowired
    CommonRepository commonRepository;
    @Value("${loan.api.url}")
    private String apiUrl;

    @Value("${loan.api.auth}")
    private String authToken;

    @Value("${loan.api.key}")
    private String ldcKey;

    public List<InvestedLoanPojo> getAllManualLendingDetails() {
        List<InvestedLoan> loans = investedLoanRepository.findAll();
        List<InvestedLoanPojo> loanPojos = new ArrayList<>();
        ModelMapper modelMapper = new ModelMapper();
        for (InvestedLoan loan : loans) {
            InvestedLoanPojo investedLoanPojo = modelMapper.map(loan, InvestedLoanPojo.class);
            loanPojos.add(investedLoanPojo);
        }
        return loanPojos;
    }
    public void collectLoanDetails() {
        List<String> loanList = new ArrayList<>();
        List<InvestedLoan> investedLoanList = investedLoanRepository.findAll();
        List<String> alreadyFetchedLoanList = investedLoanList.stream().map(InvestedLoan::getLoanId).toList();
        List<String> loansFromBorrowerLoans = commonRepository.getLoanIdFromBorrowerLoans();
        List<ManualLendingInvestment> manualLendingInvestmentList = manualLendingInvestmentRepository.findAll();
        loanList = manualLendingInvestmentList.stream()
//                .filter(ManualLendingInvestment::isNPA)
                .filter(mv->ObjectUtils.isEmpty(mv.getParticular()))
                .map(ManualLendingInvestment::getSchemeId)
//                .filter(schemeId -> !alreadyFetchedLoanList.contains(schemeId))
//                .filter(schemeId -> !loansFromBorrowerLoans.contains(schemeId))
                .toList();
        System.out.println(loanList);
        for (String loan: loanList){
            fetchLoanFromApiAndSave(loan);
        }
    }
    public void fetchLoanFromApiAndSave(String loanId){
        try {
            // 🔧 Build the URI with query parameters
            URI uri = UriComponentsBuilder.fromHttpUrl(apiUrl)
                    .queryParam("partner_code", "LDC")
                    .queryParam("loan_id", loanId)
                    .queryParam("source", "LMS") //PP
                    .queryParam("investor_id", "IAKI7TL1UT6K")
                    .queryParam("is_upcoming_investments_details", "False")
                    .queryParam("partner_id", "")
                    .build()
                    .toUri();
            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("authorization", authToken);
            headers.set("x-ldc-key", ldcKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            RestTemplate restTemplate = new RestTemplate();
            // Call API
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
            System.out.println(response);

            if (response.getStatusCode().is2xxSuccessful()) {
                String body = response.getBody();
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(body);
                JsonNode data = root.path("data");

                JsonNode loan = data.path("loan");
                JsonNode personal = data.path("personal");
                JsonNode bureau = data.path("bureau");
                JsonNode prof = data.path("professional");

                InvestedLoan investedLoan = new InvestedLoan();
                InvestedLoan invLoan = investedLoanRepository.findByLoanIdIgnoreCase(data.path("loan_id").asText());
                if (!ObjectUtils.isEmpty(invLoan)){
                   investedLoan.setId(invLoan.getId());
                }
                investedLoan.setLoanId(data.path("loan_id").asText());
                investedLoan.setLoanType(data.path("loan_type").asText());

                investedLoan.setTenure(safeDouble(loan.path("tenure")));
                investedLoan.setTenureType(loan.path("tenure_type").asText());
                investedLoan.setLoanAmount(loan.path("amount").asText());
                investedLoan.setAmountReceived(safeDouble(loan.path("amount_received")));
                investedLoan.setBorrowerAPRPercent(safePercent(loan.path("borrower_apr_percent")));
                investedLoan.setCollectionCharges(loan.path("collection_charges").asText());
                investedLoan.setReceivableAPRPercent(loan.path("receivable_apr_percent").asText());
                investedLoan.setInvestmentDate(loan.path("investment_date").asText());
                investedLoan.setRisk(loan.path("risk").asText());
                investedLoan.setLendenScore(loan.path("lenden_score").asText());
                investedLoan.setFacilitationFee(loan.path("facilitation_fee").asText());
                investedLoan.setRecoveryFee(loan.path("recovery_fee").asText());
                investedLoan.setLoanPurpose(loan.path("loan_purpose").asText());
                investedLoan.setLoanStatus(loan.path("loan_status").asText());
                investedLoan.setDpd(loan.path("dpd").asText());
                investedLoan.setDefaultRepaymentMode(loan.path("default_repayment_mode").asText());

                investedLoan.setBorrowerName(personal.path("name").asText());
                investedLoan.setBorrowerAge(personal.path("age").asText());
                investedLoan.setBorrowerGender(personal.path("gender").asText());
                investedLoan.setBorrowerMaritalStatus(personal.path("marital_status").asText());
                investedLoan.setBorrowerStayType(personal.path("stay_type").asText());
                investedLoan.setBorrowerCity(personal.path("city").asText());

                investedLoan.setScoreRange(bureau.path("score_range").asText());

                investedLoan.setProfession(prof.path("profession").asText());
                investedLoan.setAnnualTurnover(prof.path("annual_turnover").asText());
                investedLoan.setEntityName(prof.path("entity_name").asText());
                investedLoan.setBusinessVintage(prof.path("business_vintage").asText());
                investedLoan.setBusinessHq(prof.path("business_hq").asText());
                investedLoan.setEmployer(prof.path("employer").asText());
                investedLoan.setIncome(prof.path("income").asText());
                investedLoan.setEmploymentType(prof.path("employment_type").asText());
                investedLoan.setEmploymentStatus(prof.path("employment_status").asText());

                // You can also calculate marketValue, returnAmt, etc. here if needed
                // investedLoan.setMarketValue(...);
                // investedLoan.setReturnAmt(...);

                investedLoanRepository.save(investedLoan);
                System.out.println("✅ Loan saved to DB via JPA!");
            } else {
                System.err.println("❌ Failed to fetch: " + response.getStatusCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private Double safeDouble(JsonNode node) {
        if (node == null || node.asText().isBlank() || node.asText().equals("-")) return null;
        try {
            return Double.parseDouble(node.asText());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double safePercent(JsonNode node) {
        if (node == null || node.asText().isBlank() || node.asText().equals("-")) return null;
        try {
            return Double.parseDouble(node.asText().replace("%", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }


}
