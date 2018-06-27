package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Handler;

public class PackageReplacedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        CallsCounter.logCounter(context, "PackageReplacedReceiver.onReceive", "PackageReplacedReceiver_onReceive");

        if ((intent != null) && (intent.getAction() != null) && intent.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
            PPApplication.logE("##### PackageReplacedReceiver.onReceive", "xxx");

            //PackageReplacedJob.start(context.getApplicationContext());

            // if startedOnBoot = true, do not perform any actions, for example ActivateProfileHelper.lockDevice()
            PPApplication.startedOnBoot = true;
            PPApplication.startHandlerThread("PackageReplacedReceiver.onReceive");
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    PPApplication.logE("PackageReplacedReceiver.onReceive", "delayed boot up");
                    PPApplication.startedOnBoot = false;
                }
            }, 30000);

            final Context appContext = context.getApplicationContext();
            if (PPApplication.getApplicationStarted(appContext, false) && PPApplication.isNewVersion(appContext))
            {
                PPApplication.logE("@@@ PackageReplacedReceiver.onReceive", "start PhoneProfilesService");

                if (PhoneProfilesService.instance != null) {
                    PPApplication.logE("@@@ PackageReplacedReceiver.onReceive", "instance != null");
                    // stop PhoneProfilesService
                    appContext.stopService(new Intent(appContext, PhoneProfilesService.class));
                    PPApplication.sleep(2000);
                    startService(appContext);
                }
                else {
                    PPApplication.logE("@@@ PackageReplacedReceiver.onReceive", "instance == null");
                    startService(appContext);
                }
            }
        }
    }

    private void startService(Context context) {
        PPApplication.logE("@@@ PackageReplacedReceiver.startService", "xxx");

        // must by false for avoiding starts/pause events before restart events
        PPApplication.setApplicationStarted(context, false);

        // start PhoneProfilesService
        Intent serviceIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
        serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, true);
        serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, false);
        serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_PACKAGE_REPLACE, true);
        PPApplication.startPPService(context, serviceIntent);
    }

}
