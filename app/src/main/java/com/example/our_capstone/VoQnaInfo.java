package com.example.our_capstone;

public class VoQnaInfo {
    private String key;
    private String title;
    private String author;
    private String content;

    public VoQnaInfo(String key, String title, String author, String content){
        this.key  = key;
        this.title = title;
        this.author = author;
        this.content = content;
    }

    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
