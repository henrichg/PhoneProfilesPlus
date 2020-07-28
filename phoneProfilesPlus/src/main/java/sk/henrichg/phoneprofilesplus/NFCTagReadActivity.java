package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

//import me.drakeet.support.toast.ToastCompat;

public class NFCTagReadActivity extends AppCompatActivity {

    private NFCTagReadWriteManager nfcManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        nfcManager = new NFCTagReadWriteManager(this);
        nfcManager.onActivityCreate();

        nfcManager.setOnTagReadListener(new NFCTagReadWriteManager.TagReadListener() {

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
                    NFCTagReadActivity.this.finish();
                } catch (Exception ignored) {};
            }
            */

            @Override
            public void onTagRead(String tagData) {
                if (Event.getGlobalEventsRunning()) {
                    PPApplication.showToast(getApplicationContext(), "(" + getString(R.string.ppp_app_name) + ") " + getString(R.string.read_nfc_tag_read) + ": " + tagData, Toast.LENGTH_LONG);

                    final String _tagData = tagData;

                    Calendar now = Calendar.getInstance();
                    int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                    final long _time = now.getTimeInMillis() + gmtOffset;

                    final Context appContext = getApplicationContext();
                    PPApplication.startHandlerThread(/*"NFCTagReadActivity.OnTagReadListener.onTagRead"*/);
                    final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            PPApplication.logE("[HANDLER CALL] PPApplication.startHandlerThread", "START run - from=NFCTagReadActivity.OnTagReadListener.onTagRead");

                            PPApplication.logE("[EVENTS_HANDLER] NFCTagReadActivity,onCreate", "sensorType=SENSOR_TYPE_NFC_TAG");
                            EventsHandler eventsHandler = new EventsHandler(appContext);
                            eventsHandler.setEventNFCParameters(_tagData, _time);
                            eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_NFC_TAG);

                            //PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=NFCTagReadActivity.OnTagReadListener.onTagRead");
                        }
                    });

                    try {
                        NFCTagReadActivity.this.finish();
                    } catch (Exception e) {
                        PPApplication.recordException(e);
                    }
                }
            }
        });

        /*nfcManager.setOnTagWriteListener(new NFCTagReadWriteManager.TagWriteListener() {
            @Override
            public void onTagWritten() {
                ToastCompat.makeText(getApplicationContext(), "tag write finished", Toast.LENGTH_LONG).show();
                try {
                    NFCTagReadActivity.this.finish();
                } catch (Exception ignored) {};
            }
        });

        nfcManager.setOnTagWriteErrorListener(new NFCTagReadWriteManager.TagWriteErrorListener() {
            @Override
            public void onTagWriteError(NFCTagWriteException exception) {
                //ToastCompat.makeText(getApplicationContext(), exception.getType().toString(), Toast.LENGTH_LONG).show();
                ToastCompat.makeText(getApplicationContext(), "("+getString(R.string.ppp_app_name)+") "+getString(R.string.write_nfc_tag_error), Toast.LENGTH_LONG).show();
                try {
                    NFCTagReadActivity.this.finish();
                } catch (Exception ignored) {};
            }
        });
        */

    }

    @Override
    protected void onResume() {
        super.onResume();
        nfcManager.onActivityResume();
        //Log.d("NFCTagReadActivity.onResume", "xxx");
    }

    @Override
    protected void onPause() {
        nfcManager.onActivityPause();
        super.onPause();
        //Log.d("NFCTagReadActivity.onPause", "xxx");
    }

    @Override
    public void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        nfcManager.onActivityNewIntent(intent);
        //Log.d("NFCTagReadActivity.onNewIntent", "xxx");
    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, 0);
    }

}
