package sk.henrichg.phoneprofilesplus;

import androidx.annotation.NonNull;

class DayOfWeek {
    String name = "";
    String value = "";
    boolean checked = false;

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