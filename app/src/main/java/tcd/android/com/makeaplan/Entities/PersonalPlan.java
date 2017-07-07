package tcd.android.com.makeaplan.Entities;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by ADMIN on 05/07/2017.
 */

public class PersonalPlan extends Plan implements Serializable {

    public PersonalPlan() {}

    public PersonalPlan(String name, String date, String time, String tag, String owner) {
        super(name, date, time, tag);
    }
}
