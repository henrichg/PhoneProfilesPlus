package sk.henrichg.phoneprofilesplus;

class Contact {
    long contactId = 0;
    String name = "";
    long phoneId = 0;
    String phoneNumber = "";
    long photoId = 0;
    boolean checked = false;

    Contact() {
    }

    public String toString() {
        return name;
    }

    void toggleChecked() {
        checked = !checked;
    }
}