package sk.henrichg.phoneprofilesplus;

public class Contact {
	public long contactId = 0;
    public String name = "";
	public long phoneId = 0;
    public String phoneNumber = "";
    public long photoId = 0;
    public boolean checked = false;

    public Contact() {
    }

    public String toString() {
        return name;
    }

    public void toggleChecked() {
        checked = !checked;
    }
}