package org.example.model;

public class Token {
    private String subject;
    private String token;

    public Token(String subject, String token) {
        this.subject = subject;
        this.token = token;
    }

    public Token() {

    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
