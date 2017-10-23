package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.os.Handler;

class MobileDataStateChangedContentObserver extends ContentObserver {

    //public static boolean internalChange = false;

    private static boolean previousState = false;

    private final Context context;

    MobileDataStateChangedContentObserver(Context c, Handler handler) {
        super(handler);

        context=c;

        //Log.e("### MobileDataStateChangedContentObserver", "xxx");

        previousState = ActivateProfileHelper.isMobileData(context);
    }

    /*
    @Override
    public boolean deliverSelfNotifications() {
        return super.deliverSelfNotifications();
    }
    */

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);

        PPApplication.logE("##### MobileDataStateChangedContentObserver", "onChange");

        CallsCounter.logCounter(context, "MobileDataStateChangedContentObserver.onChange", "MobileDataStateChangedContentObserver_onChange");

        if (PPApplication.hasSystemFeature(context, PackageManager.FEATURE_TELEPHONY)) {
            boolean actualState = ActivateProfileHelper.isMobileData(context);
            if (previousState != actualState) {

                if (Event.getGlobalEventsRunning(context)) {
                    EventsHandlerJob.startForSensor(context, EventsHandler.SENSOR_TYPE_RADIO_SWITCH);
                }

                previousState = actualState;
            }
        }
    }

}
