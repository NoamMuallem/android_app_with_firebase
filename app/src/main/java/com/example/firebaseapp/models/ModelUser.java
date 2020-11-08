package com.example.firebaseapp.models;

public class ModelUser {
    //use same name as firebase
    String search, name, email, phone, image, cover, uid;

    public ModelUser() {
    }

    public ModelUser(String search, String name, String email, String phone, String image, String cover, String uid) {
        this.search = search;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.image = image;
        this.cover = cover;
        this.uid = uid;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
