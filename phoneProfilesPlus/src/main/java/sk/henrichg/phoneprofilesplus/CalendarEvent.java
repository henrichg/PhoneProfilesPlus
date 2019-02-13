package sk.henrichg.phoneprofilesplus;

import android.support.annotation.NonNull;

class CalendarEvent {
    long calendarId = 0;
    String name = "";
    int color = 0;
    boolean checked = false;

    CalendarEvent() {
    }

    @NonNull
    public String toString() {
        return name;
    }

    void toggleChecked() {
        checked = !checked;
    }
}