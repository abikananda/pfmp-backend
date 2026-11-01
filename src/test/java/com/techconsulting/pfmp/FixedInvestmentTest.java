//package com.techconsulting.pfmp;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.techconsulting.pfmp.model.DashStats;
//import com.techconsulting.pfmp.model.JwtResponse;
//import org.json.JSONObject;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.jdbc.Sql;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.MvcResult;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.nio.charset.StandardCharsets;
//import java.util.Base64;
//
//import static org.springframework.test.util.AssertionErrors.assertEquals;
//import static org.springframework.test.util.AssertionErrors.assertNotNull;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@Sql(scripts = "/script-before.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
//@Transactional
//public class FixedInvestmentTest {
//    @Autowired
//    private MockMvc mockMvc;
//    @Autowired
//    private ObjectMapper objectMapper;
//    private String accessToken;
//
//    @Test
//    public void testInvest() throws Exception {
////        mockMvc.perform(get("/h2-console"))
////                .andExpect(status().isOk());
//
//        doRegister();
//        accessToken = doLogin();
//        assertNotNull("No accessToken returned", accessToken);
//        double amtToInvest = 100000;
//        double invReturn = 15;
//        String stock="CRED_12month";
//        System.out.println(accessToken);
//        doInvestment(stock,amtToInvest,invReturn);
//        amtToInvest = 200000;
//        invReturn = 11;
//        stock="CRED_2month";
//        assertNotNull("No accessToken returned", accessToken);
//        //doInvestment(stock,amtToInvest,invReturn);
//        stock="CRED_12month";
//        //updateInvestment(stock,"IN_ACTIVE",null,null,null,null);
//        //testGetFixedInvestmentStatistics();
//        //testReport();
//    }
//    private void testReport() throws Exception {
//        mockMvc.perform(get("/api/auth/reportFixed/FD_RD")
//                        .header("Authorization", "Bearer "+ accessToken))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("Hello World"));
//    }
//    void testGetFixedInvestmentStatistics() throws Exception {
//        MvcResult result = mockMvc.perform(get("/api/getFixedInvestmentStatistics/FD_RD")
//                        .contentType("application/json")
//                        .header("Authorization", "Bearer "+ accessToken))
//                .andExpect(status().isOk()).andReturn();
//        String responseContent = result.getResponse().getContentAsString();
//        DashStats dashStats = objectMapper.readValue(responseContent, DashStats.class);
//        assertNotNull("dashStats should not be null", dashStats);
//        assertEquals("portfolio return ",11.0,dashStats.getRealisedReturnPer());
//        assertEquals("total investment ",200000.0,dashStats.getTotalFundInvested());
//    }
//    void doInvestment(String particular, double amtToInvest, double invReturn) throws Exception {
//        JSONObject invData = new JSONObject();
//        invData.put("particular",particular);
//        invData.put("invAmount",amtToInvest);
//        invData.put("invReturn",invReturn);
//        mockMvc.perform(post("/api/doFixedInvestment/FD_RD")
//                        .contentType("application/json")
//                        .header("Authorization", "Bearer "+ accessToken)
//                        .content(invData.toString()))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("successfully invested: "));
//    }
//    void updateInvestment(String particular, String investmentStatus, String comments, Double invReturn,Double otherIncome,String maturityDate) throws Exception {
//        JSONObject invData = new JSONObject();
//        invData.put("particular",particular);
//        invData.put("investmentStatus",investmentStatus);
//        invData.put("comments",comments);
//        invData.put("invReturn",invReturn);
//        invData.put("otherIncome",otherIncome);
//        invData.put("maturityDate",maturityDate);
//        mockMvc.perform(post("/api/sellFixedInvestment/FD_RD")
//                        .contentType("application/json")
//                        .header("Authorization", "Bearer "+ accessToken)
//                        .content(invData.toString()))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("successfully invested: "));
//    }
//    void doRegister() throws Exception {
//        mockMvc.perform(post("/api/auth/signUp")
//                        .contentType("application/json")
//                        .content(objectMapper.writeValueAsString(DataUtility.getSignUpForm())))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("User registered successfully!"));
//    }
//    String doLogin() throws Exception {
//        MvcResult result = mockMvc.perform(post("/api/auth/signIn")
//                        .header("Authorization", "Basic "+ Base64.getEncoder().encodeToString((DataUtility.getUsername()+":"+DataUtility.getPassword()).getBytes(StandardCharsets.UTF_8))))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.username").value(DataUtility.getUsername())).andReturn();
//        String responseContent = result.getResponse().getContentAsString();
//        JwtResponse jwtResponse = objectMapper.readValue(responseContent, JwtResponse.class);
//        return jwtResponse.getAccessToken();
//    }
//}
