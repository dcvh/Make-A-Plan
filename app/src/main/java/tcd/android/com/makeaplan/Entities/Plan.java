package tcd.android.com.makeaplan.Entities;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by ADMIN on 07/05/2017.
 */

public class Plan implements Serializable {
    private String id;
    private String name;
    private long dateTime;
    private String tag;

    public Plan() {}

    public Plan(String name, long dateTime, String tag) {
        this.name = name;
        this.dateTime = dateTime;
        this.tag = tag;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    String getFormattedDate(Calendar date, String format) {
        return new SimpleDateFormat(format).format(date.getTime());
    }

}
