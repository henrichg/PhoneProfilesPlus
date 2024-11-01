package sk.henrichg.phoneprofilesplus;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Calendar;

//import me.drakeet.support.toast.ToastCompat;

/** @noinspection ExtractMethodRecommender*/
public class NFCTagReadForegroundActivity extends AppCompatActivity {

    private NFCTagReadWriteManager nfcManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalGUIRoutines.setTheme(this, false, true, false, false, false, false); // must by called before super.onCreate()
        //GlobalGUIRoutines.setLanguage(this);

        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

//        PPApplicationStatic.logE("[BACKGROUND_ACTIVITY] NFCTagReadActivity.onCreate", "xxx");
//        Log.e("NFCTagReadForegroundActivity.onCreate", "xxx");

        setContentView(R.layout.activity_nfc_read_tag);
        setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.ppp_app_name)));

        Toolbar toolbar = findViewById(R.id.read_nfc_tag__toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle(R.string.nfc_tag_pref_dlg_readNfcTag_title);
            getSupportActionBar().setElevation(0/*GlobalGUIRoutines.dpToPx(1)*/);
        }

        nfcManager = new NFCTagReadWriteManager(this);
        nfcManager.onActivityCreate();

        /*
        @Override
        public void onUidRead(String uid) {
            ToastCompat.makeText(getApplicationContext(), "("+getString(R.string.ppp_app_name)+") "+getString(R.string.read_nfc_tag_read)+": "+uid, Toast.LENGTH_LONG).show();

            final String _uid = uid;

            Calendar now = Calendar.getInstance();
            int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
            final long _time = now.getTimeInMillis() + gmtOffset;

            final Context appContext = getApplicationContext();
            PPApplication.startHandlerThread("NFCTagReadActivity.OnTagReadListener.onUidRead");
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    // get tag name from uid
                    String tagName = DatabaseHandler.getInstance(appContext).getNFCTagNameByUid(_uid);
                    if (!tagName.isEmpty()) {
                        EventsHandler eventsHandler = new EventsHandler(appContext);
                        eventsHandler.setEventNFCParameters(tagName, _time);
                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_NFC_TAG);
                    }
                }
            });

            try {
                nfcManager.activity.finish();
            } catch (Exception ignored) {};
        }
        */
        nfcManager.setOnTagReadListener(tagData -> {
//                    PPApplicationStatic.logE("[IN_LISTENER] NFCTagReadActivity.onTagRead", "xxx");
//            Log.e("NFCTagReadForegroundActivity.OnTagRead", "xxxxx");

            if (EventStatic.getGlobalEventsRunning(this)) {
                PPApplication.showToast(getApplicationContext(), "(" + getString(R.string.ppp_app_name) + ") " + getString(R.string.read_nfc_tag_read) + StringConstants.STR_COLON_WITH_SPACE + tagData, Toast.LENGTH_LONG);

                final String _tagData = tagData;

                Calendar now = Calendar.getInstance();
                int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                final long _time = now.getTimeInMillis() + gmtOffset;

                final Context appContext = getApplicationContext();
                Runnable runnable = () -> {
//                            PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=NFCTagReadActivity.OnTagReadListener.onTagRead");

                    //Context appContext= appContextWeakRef.get();

                    //if (appContext != null) {
//                        PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] NFCTagReadForegroundActivity.onCreate", "SENSOR_TYPE_NFC_TAG");
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

        Button button = findViewById(R.id.read_nfc_tag_button);
        //noinspection DataFlowIssue
        button.setOnClickListener(view -> finish());

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcManager != null)
            nfcManager.onActivityResume();
//        Log.e("NFCTagReadForegroundActivity.onResume", "xxx");
    }

/*
    @Override
    protected void onPause() {
        //if (nfcManager != null)
        //    nfcManager.onActivityPause();
        super.onPause();
//        Log.e("NFCTagReadForegroundActivity.onPause", "xxx");
    }
*/
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

    /** @noinspection NullableProblems*/
    @Override
    public void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        if (nfcManager != null)
            nfcManager.onActivityNewIntent(intent);
//        Log.e("NFCTagReadForegroundActivity.onNewIntent", "xxx");
    }

    /*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }
    */

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, 0);
//        Log.e("NFCTagReadForegroundActivity.finish", "xxx");
    }

}
