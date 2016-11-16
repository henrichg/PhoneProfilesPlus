package sk.henrichg.phoneprofilesplus;

class ContactGroup {
    public long groupId = 0;
    public String name = "";
    public int count = 0;
    public boolean checked = false;

    ContactGroup() {
    }

    public String toString() {
        return name;
    }

    public void toggleChecked() {
        checked = !checked;
    }
}