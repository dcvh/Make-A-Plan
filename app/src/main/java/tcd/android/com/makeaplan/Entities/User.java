package tcd.android.com.makeaplan.Entities;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by ADMIN on 06/05/2017.
 */

public class User implements Serializable{
    private String id;
    private String name;
    private String email;
    private HashMap<String, String> friendsList;          // user's friends
    private HashMap<String, String> plansList;            // all plans belong to user

    public User() {}

    public User(String name, String email, String id) {
        this.name = name;
        this.email = email;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public HashMap<String, String> getFriendsList() {
        return friendsList;
    }

    public void setFriendsList(HashMap<String, String> friendsList) {
        this.friendsList = friendsList;
    }

    public HashMap<String, String> getPlansList() {
        return plansList;
    }

    public void setPlansList(HashMap<String, String> plansList) {
        this.plansList = plansList;
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
}
