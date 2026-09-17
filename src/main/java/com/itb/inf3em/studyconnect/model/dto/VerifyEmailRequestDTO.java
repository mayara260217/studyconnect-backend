package com.itb.inf3em.studyconnect.model.dto;

public class VerifyEmailRequestDTO {
    private String email;
    private String code;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
