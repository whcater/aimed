package com.example.entity;

public class EmailRequest {
    private String targetMail;
    private String title;
    private String content;

    // 提供getter和setter方法
    public String getTargetMail() {
        return targetMail;
    }

    public void setTargetMail(String targetMail) {
        this.targetMail = targetMail;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
