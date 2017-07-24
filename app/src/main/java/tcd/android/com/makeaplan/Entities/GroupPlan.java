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
    private String placeId;
    private HashMap<String, String> invitees;
    private HashMap<String, Integer> inviteesStatus;

    public GroupPlan() {}

    public GroupPlan(String name, long dateTime, String tag, String owner) {
        super(name, dateTime, tag);
        this.owner = owner;
    }

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

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public HashMap<String, String> getInvitees() {
        return invitees;
    }

    public void setInvitees(HashMap<String, String> invitees) {
        this.invitees = invitees;
    }

    public HashMap<String, Integer> getInviteesStatus() {
        return inviteesStatus;
    }

    public void setInviteesStatus(HashMap<String, Integer> inviteesStatus) {
        this.inviteesStatus = inviteesStatus;
    }
}
