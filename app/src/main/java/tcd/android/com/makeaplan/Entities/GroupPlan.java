package tcd.android.com.makeaplan.Entities;

import com.google.android.gms.location.places.Place;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by ADMIN on 05/07/2017.
 */

public class GroupPlan extends Plan implements Serializable {

    private Place place;
    private ArrayList<String> friends;

    public GroupPlan(String name, Calendar date, String tag) {
        super(name, date, tag);
    }

    public GroupPlan(String name, Calendar date, String tag, Place place, ArrayList<String> friends) {
        super(name, date, tag);
        this.place = place;
        this.friends = friends;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public ArrayList<String> getFriends() {
        return friends;
    }

    public void setFriends(ArrayList<String> friends) {
        this.friends = friends;
    }
}
