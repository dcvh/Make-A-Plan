package tcd.android.com.makeaplan.Entities;

import java.util.HashMap;

/**
 * Created by ADMIN on 06/05/2017.
 */

public class User {
    private String name;
    private String email;
    private HashMap<String, String> friendsList;          // user's friends
    private HashMap<String, String> plansList;            // all plans belong to user

    public User() {}

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public HashMap<String, String> getFriendsInfo() {
        return friendsList;
    }

    public void setFriendsInfo(HashMap<String, String> friendsList) {
        this.friendsList = friendsList;
    }

    public HashMap<String, String> getPlansInfo() {
        return plansList;
    }

    public void setPlansInfo(HashMap<String, String> plansList) {
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
