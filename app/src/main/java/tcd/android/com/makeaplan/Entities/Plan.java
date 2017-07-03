package tcd.android.com.makeaplan.Entities;

/**
 * Created by ADMIN on 07/05/2017.
 */

public class Plan {
    private String name;
    private String date;
    private String tag;

    public Plan(String name, String date, String tag) {
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
