package com.aelkz.springboot.skeleton.model;

public class Info {

    private String version;
    private String author;
    private String email;

    public Info(String version, String author, String email) {
        this.version = version;
        this.author = author;
        this.email = email;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
