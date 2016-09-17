package sk.henrichg.phoneprofilesplus;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

public class MobileCellsRegistrationService extends Service {

    public static String ACTION_COUNT_DOWN_TICK = "sk.henrichg.phoneprofilesplus.ACTION_COUNT_DOWN_TICK";
    public static String EXTRA_COUNTDOWN = "countdown";

    CountDownTimer countDownTimer = null;

    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.d("MobileCellsRegistrationService", "START");

        int remainingDuration = GlobalData.getMobileCellsAutoRegistrationRemainingDuration(this);

        final Context context = this;

        countDownTimer = new CountDownTimer(remainingDuration * 1000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                Log.d("MobileCellsRegistrationService", "Countdown seconds remaining: " + millisUntilFinished / 1000);
                GlobalData.setMobileCellsAutoRegistrationRemainingDuration(context, (int) millisUntilFinished / 1000);

                // broadcast for registration editor
                Intent intent = new Intent(ACTION_COUNT_DOWN_TICK);
                intent.putExtra(EXTRA_COUNTDOWN, millisUntilFinished);
                sendBroadcast(intent);
            }

            @Override
            public void onFinish() {
                Log.d("MobileCellsRegistrationService", "Timer finished");
                GlobalData.phoneProfilesService.phoneStateScanner.enabledAutoRegistration = false;
                GlobalData.setMobileCellsAutoRegistration(context, false);
                stopSelf();
            }
        };

        countDownTimer.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {

        countDownTimer.cancel();
        Log.d("MobileCellsRegistrationService", "Timer cancelled");
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

}
