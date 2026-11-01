//package com.techconsulting.pfmp;
//
//import com.techconsulting.pfmp.model.SignUpForm;
//import io.restassured.RestAssured;
//import io.restassured.parsing.Parser;
//import org.apache.commons.lang3.RandomStringUtils;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.annotation.Rollback;
//import org.springframework.test.context.jdbc.Sql;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.HashSet;
//import java.util.Set;
//
//import static io.restassured.RestAssured.given;
//import static org.hamcrest.Matchers.equalTo;
//
//@SpringBootTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
//@Transactional // ensures rollback after each test
//public class SystemTest {
//    static SignUpForm signUpForm;
//    static String randomStr;
//
//    @BeforeAll
//    public static void initialSetup() {
//        userSetup();
//    }
//
//    public static void userSetup() {
//        RestAssured.registerParser("text/plain", Parser.JSON);
//        RestAssured.port = 8080; // Port where your Spring Boot app runs
//        randomStr = RandomStringUtils.randomAlphanumeric(10);
//        signUpForm=new SignUpForm();
//        signUpForm.setEmail(randomStr+"@gmail.com");
//        signUpForm.setName(randomStr.toUpperCase());
//
//        signUpForm.setUsername(randomStr);
//        signUpForm.setPassword(randomStr);
//        Set<String> role= new HashSet<>();
//        role.add("user");
//        signUpForm.setRole(role);
//
//    }
//    @Sql(scripts = "/script-before.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
//    @Test
//    @Rollback(value = true) // Use this to keep data for checking; otherwise, transaction rollback will remove it
//    void testUserCreation() {
//        given()
//                .header("Content-Type", "application/json")
//                .body(signUpForm)
//                .when()
//                .post("/api/auth/signUp")
//                .then()
//                .assertThat()
//                .statusCode(200)
//                .body("message", equalTo("User registered successfully!"));
//    }
//}
