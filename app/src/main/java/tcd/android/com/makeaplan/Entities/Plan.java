package tcd.android.com.makeaplan.Entities;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by ADMIN on 07/05/2017.
 */

public class Plan {
    private String name;
    private Calendar date;
    private String tag;

    public Plan(String name, Calendar date, String tag) {
        this.name = name;
        this.date = date;
        this.tag = tag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDateString() {
        return getFormattedDate(date, "dd/MM/yyyy");
    }

    public Calendar getDate() {
        return this.date;
    }

    public void setDate(Calendar date) {
        this.date = date;
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
