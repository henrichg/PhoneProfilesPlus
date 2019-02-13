package sk.henrichg.phoneprofilesplus;

import android.support.annotation.NonNull;

class ContactGroup {
    long groupId = 0;
    String name = "";
    int count = 0;
    boolean checked = false;

    ContactGroup() {
    }

    @NonNull
    public String toString() {
        return name;
    }

    void toggleChecked() {
        checked = !checked;
    }
}