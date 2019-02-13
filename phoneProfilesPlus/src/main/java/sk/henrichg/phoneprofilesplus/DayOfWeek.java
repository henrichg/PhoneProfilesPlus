package sk.henrichg.phoneprofilesplus;

import android.support.annotation.NonNull;

class DayOfWeek {
    public String name = "";
    public String value = "";
    public boolean checked = false;

    DayOfWeek() {
    }

    @NonNull
    public String toString() {
        return name;
    }

    void toggleChecked() {
        checked = !checked;
    }
}