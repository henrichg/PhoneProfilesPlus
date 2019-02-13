package sk.henrichg.phoneprofilesplus;

import android.support.annotation.NonNull;

class Contact {
    long contactId = 0;
    String name = "";
    long phoneId = 0;
    String phoneNumber = "";
    long photoId = 0;
    boolean checked = false;

    Contact() {
    }

    @NonNull
    public String toString() {
        return name;
    }

    void toggleChecked() {
        checked = !checked;
    }
}