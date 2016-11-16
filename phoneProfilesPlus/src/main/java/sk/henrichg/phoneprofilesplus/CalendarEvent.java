package sk.henrichg.phoneprofilesplus;

class CalendarEvent {
    long calendarId = 0;
    String name = "";
    int color = 0;
    boolean checked = false;

    CalendarEvent() {
    }

    public String toString() {
        return name;
    }

    void toggleChecked() {
        checked = !checked;
    }
}