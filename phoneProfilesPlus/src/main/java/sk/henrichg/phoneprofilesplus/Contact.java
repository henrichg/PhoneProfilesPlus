package sk.henrichg.phoneprofilesplus;

import java.util.List;

import androidx.annotation.NonNull;

class Contact {
    long contactId = 0;
    List<Long> groups = null;
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