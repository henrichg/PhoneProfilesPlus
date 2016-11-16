package sk.henrichg.phoneprofilesplus;

import android.graphics.drawable.Drawable;

class Application {
    boolean shortcut = false;
    String appLabel = "";
    String packageName = "";
    String activityName = "";
    long shortcutId = 0;
    Drawable icon;
    boolean checked = false;

    Application() {
    }

    public String toString() {
        return appLabel;
    }

    void toggleChecked() {
        checked = !checked;
    }
}