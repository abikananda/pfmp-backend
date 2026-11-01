package com.techconsulting.pfmp.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;

@Getter
@Setter
@ToString
public class LogDto {
    private String level;
    private ArrayList<Object> additional;
    private String message;
    private String timestamp;
    private String fileName;
    private String lineNumber;
    private String columnNumber;
    private String username;
}
