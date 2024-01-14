package sk.henrichg.phoneprofilesplus;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

//import me.drakeet.support.toast.ToastCompat;

public class NFCTagReadForegroundActivity extends AppCompatActivity {

    private NFCTagReadWriteManager nfcManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalGUIRoutines.setTheme(this, true, true, true, false, false, false);

        super.onCreate(savedInstanceState);

        PPApplicationStatic.logE("NFCTagReadForegroundActivity.onCreate", "xxx");

        setContentView(R.layout.activity_nfc_read_tag);
        setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.ppp_app_name)));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_activity_nfc_tag_read);
            getSupportActionBar().setElevation(0/*GlobalGUIRoutines.dpToPx(1)*/);
        }

        nfcManager = new NFCTagReadWriteManager(this);
        nfcManager.onActivityCreate();

        nfcManager.setOnTagReadListener(tagData -> {
            if (EventStatic.getGlobalEventsRunning(this)) {
                PPApplication.showToast(getApplicationContext(), "(" + getString(R.string.ppp_app_name) + ") " + getString(R.string.read_nfc_tag_read) + StringConstants.STR_COLON_WITH_SPACE + tagData, Toast.LENGTH_LONG);

                final String _tagData = tagData;

                Calendar now = Calendar.getInstance();
                int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                final long _time = now.getTimeInMillis() + gmtOffset;

                final Context appContext = getApplicationContext();
                Runnable runnable = () -> {
//                            PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=NFCTagReadForegroundActivity.OnTagReadListener.onTagRead");

                    //Context appContext= appContextWeakRef.get();

                    //if (appContext != null) {
//                            PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] NFCTagReadForegroundActivity,onCreate", "sensorType=SENSOR_TYPE_NFC_TAG");
                        EventsHandler eventsHandler = new EventsHandler(appContext);
                        eventsHandler.setEventNFCParameters(_tagData, _time);
                        eventsHandler.handleEvents(new int[]{EventsHandler.SENSOR_TYPE_NFC_TAG});
                    //}

                };
                PPApplicationStatic.createEventsHandlerExecutor();
                PPApplication.eventsHandlerExecutor.submit(runnable);

                try {
                    nfcManager.activity.finish();
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
            }
        });

        /*nfcManager.setOnTagWriteListener(new NFCTagReadWriteManager.TagWriteListener() {
            @Override
            public void onTagWritten() {
                ToastCompat.makeText(getApplicationContext(), "tag write finished", Toast.LENGTH_LONG).show();
                try {
                    nfcManager.activity.finish();
                } catch (Exception ignored) {};
            }
        });

        nfcManager.setOnTagWriteErrorListener(new NFCTagReadWriteManager.TagWriteErrorListener() {
            @Override
            public void onTagWriteError(NFCTagWriteException exception) {
                //ToastCompat.makeText(getApplicationContext(), exception.getType().toString(), Toast.LENGTH_LONG).show();
                ToastCompat.makeText(getApplicationContext(), "("+getString(R.string.ppp_app_name)+") "+getString(R.string.write_nfc_tag_error), Toast.LENGTH_LONG).show();
                try {
                    nfcManager.activity.finish();
                } catch (Exception ignored) {};
            }
        });
        */

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onResume() {
        super.onResume();
        nfcManager.onActivityResume();
        //Log.d("NFCTagReadForegroundActivity.onResume", "xxx");
    }

    @Override
    protected void onPause() {
        nfcManager.onActivityPause();
        super.onPause();
        //Log.d("NFCTagReadForegroundActivity.onPause", "xxx");
    }

    @Override
    protected void onStart() {
        super.onStart();
        GlobalGUIRoutines.lockScreenOrientation(this, true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        GlobalGUIRoutines.unlockScreenOrientation(this);
    }

    @Override
    public void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        nfcManager.onActivityNewIntent(intent);
        //Log.d("NFCTagReadForegroundActivity.onNewIntent", "xxx");
    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, 0);
    }

}
