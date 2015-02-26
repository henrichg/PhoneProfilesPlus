package sk.henrichg.phoneprofilesplus;

public class ContactGroup {
	public long groupId = 0;
    public String name = "";
    public boolean checked = false;

    public ContactGroup() {
    }

    public String toString() {
        return name;
    }

    public void toggleChecked() {
        checked = !checked;
    }
}