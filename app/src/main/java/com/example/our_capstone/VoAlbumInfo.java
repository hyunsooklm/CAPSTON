package com.example.our_capstone;

public class VoAlbumInfo {
    private String key;
    private String name;
    private String photo;

    public VoAlbumInfo(String key, String name, String photo){
        this.key  = key;
        this.name = name;
        this.photo = photo;
    }

    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPhoto() {
        return photo;
    }
    public void setPhoto(String photo) {
        this.name = photo;
    }
}
