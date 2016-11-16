package sk.henrichg.phoneprofilesplus;

class CalendarEvent {
    public long calendarId = 0;
    public String name = "";
    public int color = 0;
    public boolean checked = false;

    CalendarEvent() {
    }

    public String toString() {
        return name;
    }

    public void toggleChecked() {
        checked = !checked;
    }
}