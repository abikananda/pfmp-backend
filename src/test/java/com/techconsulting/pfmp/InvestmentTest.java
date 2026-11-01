//package com.techconsulting.pfmp;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//
//import static io.restassured.RestAssured.given;
//import static org.hamcrest.Matchers.equalTo;
//
//public class InvestmentTest extends UserSetupTest {
//    @BeforeAll
//    public static void initialSetup() {
//        UserSetupTest.userSetup();
//    }
//
//    @Test
//    public void testAddToWalletAndInvest() throws JSONException {
//        double amtAddToWallet = 200;
//        double amtToInvest = 190;
//        double actualWalletBalance = 10;
//        String stock="Zomato";
//        accessToken = doLogin();
//        System.out.println(accessToken);
//        //emply inv and portfolio table
//        // run below testcase and assert the expected values
//        addAmtToWallet(amtAddToWallet);
//        doInvestment(stock,amtToInvest);
//        updateActualWalletBalance(actualWalletBalance);
//        testReport();
//    }
//    @Test
//    public void testReport() {
//        given()
//                .when()
//                .get("/api/auth/report/MOM_DHAN_ACCOUNT")
//                .then()
//                .statusCode(200)
//                .body("message", equalTo("Hello World"));
//    }
//    private void addAmtToWallet(double amtAddToWallet) {
//        given()
//                .header("Content-Type", "application/json").and()
//                .header("Authorization", "Bearer "+ accessToken)
//                .when()
//                .post("/api/addAmtToWallet/MOM_DHAN_ACCOUNT/"+amtAddToWallet)
//                .then()
//                .assertThat()
//                .statusCode(200)
//                .body("message", equalTo("successfully: "));
//    }
//    private void updateActualWalletBalance(double actualWalletBalance) {
//        given()
//                .header("Content-Type", "application/json").and()
//                .header("Authorization", "Bearer "+ accessToken)
//                .when()
//                .post("/api/updateActualWalletBalance/MOM_DHAN_ACCOUNT/"+actualWalletBalance)
//                .then()
//                .assertThat()
//                .statusCode(200)
//                .body("message", equalTo("successfully: "));
//    }
//    private void doInvestment(String particular, double amtToInvest) throws JSONException {
//        JSONObject invData = new JSONObject();
//        invData.put("particular",particular);
//        invData.put("invAmount",amtToInvest);
//
//        given()
//                .header("Content-Type", "application/json").and()
//                .header("Authorization", "Bearer "+ accessToken)
//                .body(invData.toString())
//                .when()
//                .post("/api/doInvestment/MOM_DHAN_ACCOUNT")
//                .then()
//                .assertThat()
//                .statusCode(200)
//                .body("message", equalTo("successfully invested: "));
//    }
//}
