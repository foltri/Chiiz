package com.example.folti.chiiz;

import android.content.Context;

/**
 * Created by Jasper on 23/02/2015.
 */
public class User {

    private String name;
    private String id;

    public User(String id) {
        this.setId(id);
    }

    public void setId(String id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getId() {
        return this.id;
    }
    public String getName() {
        return this.name;
    }

}
