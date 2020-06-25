package com.fci_zu_eng_gemy_95.emoclone.model;

public class Contacts {
    private String username;
    private String status ;
    private String imgUri;
    private String uid;

    public Contacts() {
    }

    public Contacts(String username, String status, String imgUri, String uid) {
        this.username = username;
        this.status = status;
        this.imgUri = imgUri;
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImgUri() {
        return imgUri;
    }

    public void setImgUri(String imgUri) {
        this.imgUri = imgUri;
    }
}
