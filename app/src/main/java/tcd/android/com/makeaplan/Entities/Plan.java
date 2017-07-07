package tcd.android.com.makeaplan.Entities;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by ADMIN on 07/05/2017.
 */

public class Plan implements Serializable {
    private String name;
    private String date;
    private String time;
    private String tag;

    public Plan() {}

    public Plan(String name, String date, String time, String tag) {
        this.name = name;
        this.date = date;
        this.time = time;
        this.tag = tag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
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
