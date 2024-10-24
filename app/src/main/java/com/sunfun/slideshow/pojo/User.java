package com.sunfun.slideshow.pojo;

public class User {
    private int id;
    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    private String email;

    public User() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User(int id, String username, String email) {
        super();
        this.id = id;
        this.username = username;
        this.email = email;
    }

    // getters and setters are omitted
}