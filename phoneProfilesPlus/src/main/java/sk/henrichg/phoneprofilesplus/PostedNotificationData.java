package sk.henrichg.phoneprofilesplus;

public class PostedNotificationData {

    public String packageName;
    public long time;

    public PostedNotificationData() {
    }

    public PostedNotificationData(String packageName, long time)
    {
        this.packageName = packageName;
        this.time = time;
    }

    public String getPackageName() {
        if (packageName != null)
            return packageName;
        else
            return "";
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

}
