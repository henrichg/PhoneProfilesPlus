package sk.henrichg.phoneprofilesplus;

class PostedNotificationData {

    public String packageName;
    public long time;

    //public PostedNotificationData() {
    //}

    PostedNotificationData(String packageName, long time)
    {
        this.packageName = packageName;
        this.time = time;
    }

    String getPackageName() {
        if (packageName != null)
            return packageName;
        else
            return "";
    }

    void setPackageName(String packageName) {
        this.packageName = packageName;
    }

}
