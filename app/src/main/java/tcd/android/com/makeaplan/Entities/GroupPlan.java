package tcd.android.com.makeaplan.Entities;

import com.google.android.gms.location.places.Place;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by ADMIN on 05/07/2017.
 */

public class GroupPlan extends Plan implements Serializable {

    private String owner;
    private String placeName;
    private String placeLatLng;
    private String placeAddress;
    private HashMap<String, String> invitees;

    public GroupPlan(String name, String date, String time, String tag, String owner) {
        super(name, date, time, tag);
        this.owner = owner;
    }

//    public GroupPlan(String name, String date, String time, String tag, Place place, ArrayList<String> invitees) {
//        super(name, date, time, tag);
//        this.invitees = invitees;
//    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getPlaceLatLng() {
        return placeLatLng;
    }

    public void setPlaceLatLng(String placeLatLng) {
        this.placeLatLng = placeLatLng;
    }

    public String getPlaceAddress() {
        return placeAddress;
    }

    public void setPlaceAddress(String placeAddress) {
        this.placeAddress = placeAddress;
    }

    public HashMap<String, String> getinvitees() {
        return invitees;
    }

    public void setinvitees(HashMap<String, String> invitees) {
        this.invitees = invitees;
    }
}
