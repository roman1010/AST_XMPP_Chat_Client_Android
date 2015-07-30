package com.tubiapp.demochatxmpp.apis.model;

/**
 * Copyright © 2015 AsianTech inc.
 * Created by Justin on 5/28/15.
 */
public class User {
    private String email;
    private String password;

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return email.split("@")[0];
    }
}
