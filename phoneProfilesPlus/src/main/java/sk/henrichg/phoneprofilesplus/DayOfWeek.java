package sk.henrichg.phoneprofilesplus;

public class DayOfWeek {
    public String name = "";
    public String value = "";
    public boolean checked = false;

    public DayOfWeek() {
    }

    public String toString() {
        return name;
    }

    public void toggleChecked() {
        checked = !checked;
    }
}