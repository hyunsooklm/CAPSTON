package com.example.our_capstone;

public class VoReplyInfo {
    private String key;
    private String photo;
    private String author;
    private String content;

    public VoReplyInfo(String key, String photo, String author, String content){
        this.key  = key;
        this.photo = photo;
        this.author = author;
        this.content = content;
    }

    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
