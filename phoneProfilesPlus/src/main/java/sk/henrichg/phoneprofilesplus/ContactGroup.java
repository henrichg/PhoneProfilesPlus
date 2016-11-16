package sk.henrichg.phoneprofilesplus;

class ContactGroup {
    long groupId = 0;
    String name = "";
    int count = 0;
    boolean checked = false;

    ContactGroup() {
    }

    public String toString() {
        return name;
    }

    void toggleChecked() {
        checked = !checked;
    }
}