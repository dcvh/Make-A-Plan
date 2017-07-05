package tcd.android.com.makeaplan.Entities;

/**
 * Created by ADMIN on 05/07/2017.
 */

public class GroupPlanOption {
    private String title;
    private String value;
    private int drawableId;

    public GroupPlanOption(String title, String value, int drawableId) {
        this.title = title;
        this.value = value;
        this.drawableId = drawableId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getDrawableId() {
        return drawableId;
    }

    public void setDrawableId(int drawableId) {
        this.drawableId = drawableId;
    }
}
