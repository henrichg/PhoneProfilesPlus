package sk.henrichg.phoneprofilesplus;

class DayOfWeek {
    public String name = "";
    public String value = "";
    public boolean checked = false;

    DayOfWeek() {
    }

    public String toString() {
        return name;
    }

    void toggleChecked() {
        checked = !checked;
    }
}