package com.techconsulting.pfmp;

import com.techconsulting.pfmp.model.SignUpForm;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.HashSet;
import java.util.Set;

public class DataUtility {
    private static final String randomStr = RandomStringUtils.randomAlphanumeric(9);

    public static SignUpForm getSignUpForm(){
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setEmail(randomStr+"@gmail.com");
        signUpForm.setName(randomStr.toUpperCase());
        signUpForm.setUsername(getUsername());
        signUpForm.setPassword(getPassword());
        Set<String> role= new HashSet<>();
        role.add("user");
        signUpForm.setRole(role);
        return signUpForm;
    }
    public static String getUsername(){
        return randomStr.toUpperCase();
    }
    public static String getPassword(){
        return randomStr;
    }
}
