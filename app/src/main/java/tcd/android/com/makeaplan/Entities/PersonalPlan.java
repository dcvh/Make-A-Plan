package tcd.android.com.makeaplan.Entities;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by ADMIN on 05/07/2017.
 */

public class PersonalPlan extends Plan implements Serializable {

    private String note;
    private String imageUrl;

    public PersonalPlan() {}

    public PersonalPlan(String name, long dateTime, String tag, String owner) {
        super(name, dateTime, tag);
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
