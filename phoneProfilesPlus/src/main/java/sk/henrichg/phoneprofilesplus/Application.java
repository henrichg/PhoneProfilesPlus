package sk.henrichg.phoneprofilesplus;

import android.graphics.drawable.Drawable;

public class Application {
    public String appLabel = "";
    public String packageName = "";
    //private String versionName = "";
    //private int versionCode = 0;
    public Drawable icon;
    public boolean checked = false;

    public Application() {
    }

    public String toString() {
        return appLabel;
    }

    public void toggleChecked() {
        checked = !checked;
    }
}