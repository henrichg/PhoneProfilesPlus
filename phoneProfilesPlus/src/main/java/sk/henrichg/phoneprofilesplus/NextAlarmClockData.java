package sk.henrichg.phoneprofilesplus;

class NextAlarmClockData {

    String packageName;
    long time;

    // constructor is required for GSon !!!
    @SuppressWarnings("unused")
    NextAlarmClockData() {
    }

    NextAlarmClockData(String packageName, long time)
    {
        this.packageName = packageName;
        this.time = time;
    }

}
